package com.example.fitquest.Activitiy_list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder

class Login : AppCompatActivity() {

    private val TAG = "Login_test"

    private lateinit var healthConnectClient: HealthConnectClient

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    private val requestPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.isNotEmpty()) {
            Log.d("HealthConnect", "Health Connect 권한 허용됨")
        } else {
            Log.d("HealthConnect", "Health Connect 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        //앱 끄면 다시 로그인
        //getSharedPreferences("FitQuestPrefs", MODE_PRIVATE).edit().clear().apply()

        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkHealthConnectPermission()

        findViewById<MaterialButton>(R.id.fit_login).setOnClickListener {
            showLoginDialog()
        }

        findViewById<MaterialButton>(R.id.fit_SignUp).setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        findViewById<Button>(R.id.test).setOnClickListener { goToMainActivity() }
        findViewById<Button>(R.id.test2).setOnClickListener { goToHActivity() }

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (tokenInfo != null) {
                Log.i(TAG, "이미 카카오 로그인 상태")
                goToMainActivity()
            }
        }

        findViewById<ImageButton>(R.id.kakao_login).setOnClickListener {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                } else {
                    goToMainActivity()
                }
            }
        }
    }

    private fun checkHealthConnectPermission() {
        val sdkStatus = HealthConnectClient.getSdkStatus(this)
        when (sdkStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Toast.makeText(this, "이 기기는 Health Connect를 지원하지 않습니다", Toast.LENGTH_LONG).show()
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Toast.makeText(this, "Health Connect 업데이트가 필요합니다", Toast.LENGTH_LONG).show()
                val uri = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(this)
                checkAndRequestPermissions()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            try {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                if (granted.containsAll(permissions)) {
                    Log.d("HealthConnect", "이미 권한 있음")
                } else {
                    Log.d("HealthConnect", "권한 요청 실행")
                    requestPermissions.launch(permissions)
                }
            } catch (e: Exception) {
                Log.e("HealthConnect", "권한 확인 오류 ${e.message}")
            }
        }
    }

    private fun showLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.login_email_input)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.login_password_input)
        val loginButton = dialogView.findViewById<MaterialButton>(R.id.dialog_login_button)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.dialog_cancel_button)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performLogin(email, password)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performLogin(email: String, password: String) {
        val url = "https://fitquest25.xyz/api/auth/login/"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        Log.i(TAG, "===== 로그인 요청 시작 =====")
        Log.i(TAG, "전송 URL: $url")
        Log.i(TAG, "전송 이메일: $email")

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                Log.i(TAG, "응답 코드: 200")
                Log.i(TAG, "✅ 로그인 성공!")
                Log.i(TAG, "응답 데이터: $response")
                Log.i(TAG, "===== 로그인 종료 =====")

                val accessToken = response.optString("access", "")
                val refreshToken = response.optString("refresh", "")
                if (accessToken.isNotEmpty()) {
                    saveTokens(accessToken, refreshToken)
                }
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                val errorBody = error.networkResponse?.let { String(it.data) } ?: "응답 없음"
                val errorMessage = error.message ?: "메세지 없음"
                val errorCause = error.cause?.message ?: "원인 없음"
                val errorClass = error.javaClass.simpleName

                Log.e(TAG, "응답 코드: $statusCode")
                Log.e(TAG, "❌ 로그인 실패")
                Log.e(TAG, "실패 코드: $statusCode")
                Log.e(TAG, "실패 메시지: $errorBody")
                Log.e(TAG, "에러 타입: $errorClass")
                Log.e(TAG, "에러 메세지: $errorMessage")
                Log.e(TAG, "에러 원인: $errorCause")
                Log.i(TAG, "===== 로그인 종료 =====")

                Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json")
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.getQueryParameter("code")?.let {
            requestAccessToken(it)
        }
    }

    private fun requestAccessToken(authCode: String) {
        val url = "https://kauth.kakao.com/oauth/token"
        val redirectUri = "kakao${getString(R.string.kakao_native_app_key)}://oauth"
        val params = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to getString(R.string.kakao_native_app_key),
            "redirect_uri" to redirectUri,
            "code" to authCode
        )
        val requestBody = params.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }

        val request = object : StringRequest(Method.POST, url,
            { response ->
                goToMainActivity()
            },
            { error ->
                Log.e(TAG, "카카오 토큰 요청 실패: ${error.message}")
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun goToMainActivity() {
        val intent = Intent(this, Main::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun goToHActivity() {
        val intent = Intent(this, Health::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}