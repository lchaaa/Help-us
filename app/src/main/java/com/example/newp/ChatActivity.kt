package com.example.newp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//채팅방
class ChatActivity : AppCompatActivity() {
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSendMessage: Button
    private lateinit var dbutton: CardView
    private lateinit var chatTextView: TextView // 텍스트뷰 추가
    private lateinit var currentUserID: String
    private lateinit var otherUserID: String
    private lateinit var title: String
    private lateinit var chatRoomID: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSendMessage = findViewById(R.id.buttonSendMessage)
        dbutton = findViewById(R.id.dbutton)
        chatTextView = findViewById(R.id.chatTextView)

        // Intent에서 상대방 사용자 ID 제목 가져오기
        otherUserID = intent.getStringExtra("userId") ?: ""
        title = intent.getStringExtra("title") ?: ""
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser  //현재 로그인한 사용자 id
        currentUserID = currentUser?.uid ?: ""

        //현재로그인한사용자id - 글쓴사용자id 채팅방 생성
        val userIdList = listOf(currentUserID, otherUserID)
        val sortedUserIdList = userIdList.sorted()
        chatRoomID = sortedUserIdList.joinToString("-")

        buttonSendMessage.setOnClickListener {
            sendMessage()
        }

        readMessages() // 채팅 메시지 읽기

        dbutton.setOnClickListener {
            deleteChatRoom() // d 버튼 클릭 시 채팅방 삭제
        }
    }

     // 채팅방 삭제
    private fun deleteChatRoom() {
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("messages").child(chatRoomID)
        databaseReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "채팅방이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 채팅 액티비티 종료 혹은 다른 작업 수행
            }
            .addOnFailureListener {
                Toast.makeText(this, "채팅방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    //메세지 보내기
    private fun sendMessage() {
        val messageContent = editTextMessage.text.toString().trim()
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(currentDate)
        if (messageContent.isNotEmpty()) {
            val message =
                Message(currentUserID, otherUserID, messageContent, title, time, read = false)
            val databaseReference =
                FirebaseDatabase.getInstance().reference.child("messages").child(chatRoomID)
            databaseReference.push().setValue(message)
                .addOnSuccessListener {
                    editTextMessage.text.clear()
                    Toast.makeText(this, "메시지 전송 완료", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "메시지를 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

     // 사용자가 채팅방에 들어와 있는지 여부
    private var isUserInChatRoom = false
    override fun onStart() {
        super.onStart()
        isUserInChatRoom = true
    }
    override fun onStop() {
        super.onStop()
        isUserInChatRoom = false
    }
    //메세지 읽기
    private fun readMessages() {
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("messages").child(chatRoomID)
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val receivedMessage = snapshot.getValue(Message::class.java)
                receivedMessage?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        val otherUserName = getOtherUserNameFromFirestore()
                        val message = if (it.senderId == currentUserID) {
                            "나 : ${it.content}\n\n"
                        } else {
                            "$otherUserName : ${it.content}\n\n"
                        }
                        chatTextView.append(message)

                        // 메시지를 받은 사용자가 로그인한 사용자일 경우에만 isRead 업데이트
                        if (it.receiverId == currentUserID && isUserInChatRoom) {
                            updateReadStatus(snapshot.key)
                        }
                        val scrollAmount =
                            chatTextView.layout.getLineTop(chatTextView.lineCount) - chatTextView.height
                        if (scrollAmount > 0) {
                            chatTextView.scrollTo(0, scrollAmount)
                        } else {
                            chatTextView.scrollTo(0, 0)
                        }
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

     // 상대방 name 가져오기
    private suspend fun getOtherUserNameFromFirestore(): String {
        val otherUserDoc = FirebaseFirestore.getInstance().collection("users").document(otherUserID)
        return try {
            otherUserDoc.get().await().getString("name") ?: "상대방"
        } catch (e: Exception) {
            "상대방"
        }
    }

    //읽음 표시 업데이트
    private fun updateReadStatus(messageKey: String?) {
        messageKey?.let {
            val databaseReference =
                FirebaseDatabase.getInstance().reference.child("messages").child(chatRoomID)
                    .child(it)
            databaseReference.child("read").setValue(true)
        }
    }
}