package com.example.fitquest.Activitiy_list

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class SignUp : AppCompatActivity() {

    private val TAG = "SignUpDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val emailInput = findViewById<TextInputEditText>(R.id.email_input)
        val nicknameInput = findViewById<TextInputEditText>(R.id.nickname_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)
        val signupButton = findViewById<MaterialButton>(R.id.signup_button)
        val goToLogin = findViewById<TextView>(R.id.go_to_login)

        // 회원가입 버튼 클릭
        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val nickname = nicknameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // 유효성 검사
            if (email.isEmpty() || nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nickname.length < 2) {
                Toast.makeText(this, "닉네임은 최소 2자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 회원가입 요청
            performSignUp(email, nickname, password)
        }

        // 로그인 화면으로 돌아가기
        goToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performSignUp(email: String, nickname: String, password: String) {
        val url = "https://fitquest25.xyz/api/auth/signup/"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("nickname", nickname)  // 백엔드가 username을 사용
            put("password", password)
        }

        Log.i(TAG, "회원가입 요청: $jsonBody")

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                Log.i(TAG, "회원가입 성공: $response")
                Toast.makeText(this, "회원가입 성공! 로그인해주세요", Toast.LENGTH_LONG).show()
                finish() // 로그인 화면으로 돌아가기
            },
            { error ->
                Log.e(TAG, "회원가입 실패: ${error.message}")
                Log.e(TAG, "에러 응답: ${error.networkResponse?.let { String(it.data) }}")

                val errorMessage = try {
                    val errorBody = String(error.networkResponse?.data ?: ByteArray(0))
                    val errorJson = JSONObject(errorBody)
                    errorJson.toString(2) // 보기 좋게 포맷
                } catch (e: Exception) {
                    "회원가입 실패: 서버 오류"
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}