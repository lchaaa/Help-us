package com.example.newp
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

//도와주세요
class Write : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var title: String // 추가
    private lateinit var contentEditText: EditText
    private lateinit var EditText: EditText
    private lateinit var EditText2: EditText
    private lateinit var selectImageButton: Button
    private lateinit var sendButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedImageUri: Uri? = null
    private val MY_PERMISSIONS_REQUEST_LOCATION = 123
    private val db = FirebaseFirestore.getInstance()

    //이미지 선택
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
            selectImageButton.text = "사진이 선택되었습니다"
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.write)

        contentEditText = findViewById(R.id.contentEditText)
        EditText = findViewById(R.id.EditText)
        EditText2 = findViewById(R.id.EditText2)
        selectImageButton = findViewById(R.id.selectImageButton)
        sendButton = findViewById(R.id.sendButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // 스피너 아이템 설정
        val categories = arrayOf("운반수리","청소","배달,퀵","벌레잡기","대행")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter
        // 스피너 아이템 선택 이벤트 리스너
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                title = categories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //이미지 선택
        selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        //글 등록
        sendButton.setOnClickListener {
            Toast.makeText(this, "글 쓰기 완료.", Toast.LENGTH_SHORT).show()
            val content = contentEditText.text.toString()
            val adress = EditText.text.toString()
            val price = EditText2.text.toString()

            //위치 권한 설정
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val userId = Firebase.auth.currentUser?.uid
                            //글의 제목,내용,주소,가격,이미지 URL,작성시간,위도,경도,사용자 ID data 맵에 저장
                            userId?.let { uid ->
                                val storage = FirebaseStorage.getInstance()
                                val storageRef = storage.reference
                                val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
                                imageRef.putFile(selectedImageUri!!)
                                    .addOnSuccessListener { taskSnapshot ->
                                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                                            val imageUrl = uri.toString()
                                            val currentDate = Calendar.getInstance().time
                                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            val formattedDate = sdf.format(currentDate)
                                            val data = hashMapOf(
                                                "title" to title,
                                                "content" to content,
                                                "adress" to adress,
                                                "price" to price,
                                                "imageUrl" to imageUrl,
                                                "timestamp" to formattedDate,
                                                "latitude" to latitude,
                                                "longitude" to longitude,
                                                "userId" to uid
                                            )
                                            //data맵 z필드에 추가
                                            db.collection("users").document(uid)
                                                .update("z", FieldValue.arrayUnion(data))
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener { e ->
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                    }
                            }
                        } else {
                            Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}