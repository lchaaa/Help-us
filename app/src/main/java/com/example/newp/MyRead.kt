package com.example.newp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

//나의 게시글
class MyRead : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CustomAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.myread)
        listView = findViewById(R.id.listView2)
        adapter = CustomAdapter(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        loadFirestoreData()
    }
    // Firestore에서 데이터 가져오기
    private fun loadFirestoreData() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = Firebase.auth.currentUser
        //로그인한 사용자의 z 데이터만 가져와서 리스트에 표시
        currentUser?.uid?.let { userId ->
            db.collection("users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val zData = document.get("z") as? List<Map<*, *>>
                        zData?.forEach { data ->
                            val title = data["title"] as? String
                            val content = data["content"] as? String
                            val adress = data["adress"] as? String
                            val price = data["price"] as? String
                            val timestamp = data["timestamp"] as? String
                            val imageUrl = data["imageUrl"] as? String
                            adapter.addItem(title, content,adress,price, timestamp, userId, imageUrl)
                        }
                    }
                    listView.adapter = adapter

                    listView.setOnItemClickListener { _, _, position, _ ->
                        val selectedItem = adapter.getItem(position) as CustomAdapter.CustomItem
                        val intent = Intent(this, DetailActivity::class.java)
                        intent.putExtra("title", selectedItem.title)
                        intent.putExtra("content", selectedItem.content)
                        intent.putExtra("adress", selectedItem.adress)
                        intent.putExtra("price", selectedItem.price)
                        intent.putExtra("timestamp", selectedItem.timestamp)
                        intent.putExtra("imageUrl", selectedItem.imageUrl)
                        intent.putExtra("userId", selectedItem.userId)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                }
        }
    }
}