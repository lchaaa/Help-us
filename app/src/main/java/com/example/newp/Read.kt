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

//도와줄게요
class Read : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CustomAdapter
    private lateinit var userLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val MY_PERMISSIONS_REQUEST_LOCATION = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read)
        listView = findViewById(R.id.listView)
        adapter = CustomAdapter(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 권한이 있는지 확인하고, 없다면 권한 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            // 위치 권한이 있는 경우, 사용자의 현재 위치 가져오기
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = location
                        loadFirestoreData()
                    }
                }
                .addOnFailureListener { exception ->
                }
        }
    }
    // Firestore에서 데이터 가져오기
    private fun loadFirestoreData() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = Firebase.auth.currentUser

        db.collection("users")
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
                        val userId = data["userId"] as? String
                        val imageUrl = data["imageUrl"] as? String
                        val latitude = data["latitude"] as? Double
                        val longitude = data["longitude"] as? Double

                        // 현재 로그인한 사용자와 동일한 경우, CustomAdapter에 추가하지 않음
                        if (userId != null && userId == currentUser?.uid) {
                            return@forEach
                        }

                        if (latitude != null && longitude != null) {
                            val location = Location("document_location")
                            location.latitude = latitude
                            location.longitude = longitude

                            // 일정 범위 이내에 있는 경우에만 데이터 추가
                            val distance = userLocation.distanceTo(location)
                            if (distance <= 10000) { // 10000m (10km) 이내의 범위로 설정
                                adapter.addItem(title, content,adress,price, timestamp,userId,imageUrl)
                            }
                        }
                    }
                }
                listView.adapter = adapter

                // ListView의 아이템 클릭 이벤트 처리
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