package com.example.newp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

//리스트 추가 내용
class DetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var adressTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var timestampTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var sendButton2: Button
    private lateinit var Button2: Button
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        adressTextView = findViewById(R.id.adressTextView)
        priceTextView = findViewById(R.id.priceTextView)
        timestampTextView = findViewById(R.id.timestampTextView)
        imageView = findViewById(R.id.imageView)
        sendButton2 = findViewById(R.id.sendButton2)
        Button2 = findViewById(R.id.Button2)
        firestore = FirebaseFirestore.getInstance() // Firebase 초기화

        // Intent에서 데이터 가져오기
        val title = intent.getStringExtra("title")
        val adress = intent.getStringExtra("adress")
        val price = intent.getStringExtra("price")
        val content = intent.getStringExtra("content")
        val timestamp = intent.getStringExtra("timestamp")
        val userId = intent.getStringExtra("userId")
        val imageUrl = intent.getStringExtra("imageUrl")

        // 가져온 데이터를 TextView와 ImageView에 설정
        titleTextView.text = "$title"
        adressTextView.text = "주소: $adress"
        priceTextView.text = "가격: $price 원"
        contentTextView.text = "내용: $content"
        timestampTextView.text = "날짜: $timestamp"

        // 이미지를 Picasso를 사용하여 ImageView에 설정
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(imageView)
        }

        // userId,title ChatActivity로 전달
        sendButton2.setOnClickListener {
            val nextIntent = Intent(this, ChatActivity::class.java)
            nextIntent.putExtra("userId", userId)
            nextIntent.putExtra("title", title)
            startActivity(nextIntent)
        }

        //Read(메세지보내기 버튼 만 표시) Myread(삭제 버튼 만 표시)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.uid == userId) {
            Button2.visibility = View.VISIBLE
            sendButton2.visibility = View.GONE
        } else {
            Button2.visibility = View.GONE
        }

        //삭제버튼
        Button2.setOnClickListener {
            deleteZField(currentUser, title, content,adress,price, timestamp)
        }
    }

    //z 필드 들의 제목,가격,내용 등 과 비교하여 게시글 삭제
    private fun deleteZField(user: FirebaseUser?, intentTitle: String?, intentContent: String?,intentadress: String?,intentprice: String?, intentTimestamp: String?)
    {
        val db = FirebaseFirestore.getInstance()
        val userId = user?.uid
        userId?.let {
            db.collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val zData = documentSnapshot.get("z") as? List<Map<String, Any>>
                    zData?.let { zList ->
                        val updatedZList = zList.filter {
                            it["timestamp"] != intentTimestamp || it["title"] != intentTitle || it["content"] != intentContent || it["adress"] != intentadress
                                    || it["price"] != intentprice
                        }
                        documentSnapshot.reference.update("z", updatedZList)
                            .addOnSuccessListener {
                                Toast.makeText(this, "z 필드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                }
        } ?: run {
        }
    }
}