package com.example.newp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//시작페이지
class Start : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
        val read = findViewById<TextView>(R.id.readtextview)
        val name = findViewById<TextView>(R.id.nametextview)
        val logoutCardView = findViewById<CardView>(R.id.logoutCardView)
        val writeCardView = findViewById<CardView>(R.id.writeCardView)
        val readCardView = findViewById<CardView>(R.id.readCardView)
        val chatlistCardView = findViewById<CardView>(R.id.chatlistCardView)
        val chatlistCardView2 = findViewById<CardView>(R.id.chatlistCardView2)
        val myreadCardView = findViewById<CardView>(R.id.myreadCardView)
        val currentUserID = MyApplication.auth.currentUser?.uid
        val nameTextView = findViewById<TextView>(R.id.nametextview)

        //로그인한 사용자 이름 가지고 오기
        if (currentUserID != null) {
            val usersRef = FirebaseFirestore.getInstance().collection("users")
            usersRef.document(currentUserID).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userName = documentSnapshot.getString("name")
                        nameTextView.text = "환영합니다 $userName 님"
                    }
                }
                .addOnFailureListener { e ->
                }
        }

        // 읽지 않은 메시지 개수를 텍스트뷰에 표시
        val chatRoomsRef = FirebaseDatabase.getInstance().reference.child("messages")
        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadMessageCount = 0
               // 파이어베이스 receiverId 를 보고 read == false 인 개수를 가지고온다
                for (chatRoomSnapshot in snapshot.children) {
                    val messagesRef = chatRoomSnapshot.ref
                    messagesRef.orderByChild("receiverId").equalTo(currentUserID)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(messagesSnapshot: DataSnapshot) {
                                for (messageSnapshot in messagesSnapshot.children) {
                                    val message = messageSnapshot.getValue(Message::class.java)
                                    if (message?.read == false) {
                                        unreadMessageCount++
                                    }
                                }
                                read.text = "새로운 메세지: $unreadMessageCount"
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        //로그아웃
        logoutCardView.setOnClickListener {
            val nextIntent = Intent(this, AuthActivity::class.java)
            nextIntent.putExtra("mode", "logout")
            MyApplication.auth.signOut()
            MyApplication.email = null
            startActivity(nextIntent)
        }
        //도와주세요
        writeCardView.setOnClickListener {
            val nextIntent = Intent(this, com.example.newp.Write::class.java)
            startActivity(nextIntent)
        }
        //도와줄게요
        readCardView.setOnClickListener {
            val nextIntent = Intent(this, Read::class.java)
            startActivity(nextIntent)
        }
        //나의 메세지
        chatlistCardView.setOnClickListener {
            val nextIntent = Intent(this, ChatListActivity::class.java)
            startActivity(nextIntent)
        }
        //나의 메세지 2
        chatlistCardView2.setOnClickListener {
            val nextIntent = Intent(this, ChatListActivity::class.java)
            startActivity(nextIntent)
        }
        //나의게시글
        myreadCardView.setOnClickListener {
            val nextIntent = Intent(this, MyRead::class.java)
            startActivity(nextIntent)
        }
    }
}