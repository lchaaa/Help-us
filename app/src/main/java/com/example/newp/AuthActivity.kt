package com.example.newp
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.newp.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore

//user정보
data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val gender: String = "",
    val phoneNumber: String = "" ,
)

//로그인 여부에 따라 ui설정
class AuthActivity: AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mode = intent?.getStringExtra("mode")

        if (mode == "logout") {
            changeVisibility("logout")
        } else {
            if (MyApplication.checkAuth()) {
                changeVisibility("login")
            } else {
                changeVisibility("logout")
            }
        }

        //로그아웃
        binding.logoutBtn.setOnClickListener {
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
        }
        //시작
        binding.a.setOnClickListener {
            val nextIntent = Intent(this, Start::class.java)
            startActivity(nextIntent)

        }
        //회원가입
        binding.goSignInBtn.setOnClickListener {
            changeVisibility("signin")
        }

        //회원가입
        binding.signBtn.setOnClickListener {
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            val name = binding.nameEditText.text.toString()
            val gender = binding.genderEditText.text.toString()
            val phoneNumber = binding.phoneEditText.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        val currentUser = MyApplication.auth.currentUser
                        currentUser?.let {
                            val user = User(
                                currentUser.uid,
                                email,
                                name,
                                gender,
                                phoneNumber,
                            )
                            val db = Firebase.firestore
                            db.collection("users").document(currentUser.uid)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(baseContext, "회원가입 성공", Toast.LENGTH_SHORT)
                                        .show()
                                    MyApplication.auth.signOut()
                                    MyApplication.email = null
                                    changeVisibility("logout")
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    } else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //로그인
        binding.loginBtn.setOnClickListener {
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        val currentUser = MyApplication.auth.currentUser
                        // 로그인 성공 시 Firestore 에서 정보 가지고 오기
                        if (currentUser != null) {
                            val db = Firebase.firestore
                            db.collection("users").document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val userData = document.toObject(User::class.java)
                                        val name = userData?.name
                                        val gender = userData?.gender
                                        val phoneNumber = userData?.phoneNumber
                                        MyApplication.email = email
                                        changeVisibility("login")
                                    } else {
                                        Toast.makeText(
                                            baseContext,
                                            "사용자 정보가 없습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        baseContext,
                                        "사용자 정보를 가져오는 데 실패했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    //로그인
    fun changeVisibility(mode: String){
        if(mode === "login"){
            val nextIntent = Intent(this, Start::class.java)
            startActivity(nextIntent)
        }
        //로그아웃
        else if(mode === "logout"){
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                a.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                nameEditText.visibility = View.GONE
                genderEditText.visibility = View.GONE
                phoneEditText.visibility = View.GONE
            }
            //회원가입
        }else if(mode === "signin"){
            binding.run {
                logoutBtn.visibility = View.GONE
                a.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                nameEditText.visibility = View.VISIBLE
                genderEditText.visibility = View.VISIBLE
                phoneEditText.visibility = View.VISIBLE
            }
        }
    }
}

