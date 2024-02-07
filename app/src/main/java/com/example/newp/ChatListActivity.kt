package com.example.newp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

//나의 메세지
class ChatListActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var chatRooms: MutableList<String>
    private lateinit var chatRoomInfoList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        listView = findViewById(R.id.listView)
        chatRooms = mutableListOf()
        chatRoomInfoList = mutableListOf()
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val databaseReference = FirebaseDatabase.getInstance().reference.child("messages")

        // Firebase Realtime Database 레퍼런스 가져오기
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firestoreDB = FirebaseFirestore.getInstance()
                var roomProcessedCount = 0 // 각 채팅방 처리 횟수 카운트

                for (roomSnapshot in snapshot.children) {
                    val roomID = roomSnapshot.key
                    roomID?.let {
                        // 현재 사용자가 포함된 채팅방인지 확인
                        if (it.contains(currentUserID)) {
                            chatRooms.add(it)
                            val usersInRoom = it.split("-")
                            val otherUserId = usersInRoom.find { id -> id != currentUserID } ?: ""

                            // 파이어스토어에서 사용자 이름 가져오기
                            firestoreDB.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val userName = document.getString("name")
                                        userName?.let { name ->
                                            val query = FirebaseDatabase.getInstance().reference
                                                .child("messages")
                                                .child(it)
                                                .orderByKey()

                                            //Firebase Realtime Database에서 데이터를 쿼리
                                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    var firstTitle = ""      //작성글
                                                    var lastMessage = ""        //마지막 메세지 내용
                                                    var lastTimestamp = ""        //마지막 메세지 시간
                                                    var unreadMessagesCount = 0 // 읽지 않은 메시지 개수

                                                    //작성글 가져오기
                                                    val firstMessageSnapshot = dataSnapshot.children.firstOrNull()
                                                    firstMessageSnapshot?.let {
                                                        firstTitle = it.child("title").value as String
                                                    }

                                                    //마지막 메세지 내용,마지막 메세지 시간 가져오기
                                                    val lastMessageSnapshot = dataSnapshot.children.lastOrNull()
                                                    lastMessageSnapshot?.let {
                                                        lastMessage = it.child("content").value as String
                                                        lastTimestamp = it.child("time").value as String
                                                    }

                                                    // 읽지 않은 메시지 개수 가져오기  (!it.read!! 는 read false 상태)
                                                    for (messageSnapshot in dataSnapshot.children) {
                                                        val message = messageSnapshot.getValue(Message::class.java)
                                                        message?.let {
                                                            if (it.receiverId == currentUserID && !it.read!!) {
                                                                unreadMessagesCount++
                                                            }
                                                        }
                                                    }

                                                    chatRoomInfoList.add("작성글:$firstTitle"+
                                                            "\n대화상대:$name\n$lastMessage $lastTimestamp ($unreadMessagesCount)"
                                                            )

                                                    roomProcessedCount++
                                                    if (roomProcessedCount == chatRooms.size) {
                                                        updateUI()
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {
                                                }
                                            })
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            //채팅방 아이디를 "-" 기준으로 분리 후 현재 사용자 아이디와 같지 않은 id를 챗 액티비티에 보낸다
            val selectedRoom = chatRooms[position]
            val usersInRoom = selectedRoom.split("-")
            val userId: String = usersInRoom.find { it != currentUserID } ?: ""
            if (userId.isNotEmpty()) {
                val nextIntent = Intent(this, ChatActivity::class.java)
                nextIntent.putExtra("userId", userId)
                startActivity(nextIntent)
            } else {
            }
        }
    }

    private fun updateUI() {
        val adapter = ArrayAdapter(
            this@ChatListActivity,
            android.R.layout.simple_list_item_1,
            chatRoomInfoList
        )
        listView.adapter = adapter
    }
}