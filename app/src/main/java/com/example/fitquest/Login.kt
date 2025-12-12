package com.example.fitquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import org.json.JSONObject
import java.net.URLEncoder
import com.android.volley.toolbox.JsonObjectRequest


class Login : AppCompatActivity() {

    private val TAG = "KakaoLoginDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))

        // 화면 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 테스트 버튼
        findViewById<Button>(R.id.test).setOnClickListener {
            goToMainActivity()
        }

        // 이미 로그인되어 있으면 바로 Main으로 이동
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (tokenInfo != null) {
                Log.i(TAG, "이미 로그인 되어 있음. 토큰 ID: ${tokenInfo.id}")
                goToMainActivity()
            } else {
                error?.let { Log.e(TAG, "토큰 정보 없음: $it") }
            }
        }

        // 카카오 로그인 버튼
        findViewById<Button>(R.id.login).setOnClickListener {
            Log.i(TAG, "카카오 로그인 시작")
           // Toast.makeText(this, "카카오 로그인 시작", Toast.LENGTH_SHORT).show()

            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "SDK 로그인 호출 에러: $error")
                    //Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(TAG, "SDK 로그인 호출 완료, 브라우저로 이동")

                }
            }
        }
    }

    // 브라우저에서 앱 복귀 시 인증 코드 받기
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.data?.let { uri ->
            val code = uri.getQueryParameter("code")
            if (code != null) {
                Log.i("KakaoRedirect", "인증 코드 받음: $code")
                //Toast.makeText(this, "인증 코드 받음", Toast.LENGTH_SHORT).show()
                requestAccessToken(code)
            } else {
                Log.e("KakaoRedirect", "코드 없음")
            }
        }
    }

    // 인증 코드 → 토큰 요청 (REST API)
    private fun requestAccessToken(authCode: String) {
        val url = "https://kauth.kakao.com/oauth/token"

        val redirectUri = "kakao${getString(R.string.kakao_native_app_key)}://oauth"
        val params = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to getString(R.string.kakao_native_app_key),
            "redirect_uri" to redirectUri,
            "code" to authCode
        )

        // URL 인코딩 적용
        val requestBody = params.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.i("KakaoToken", "토큰 응답: $response")
                val json = JSONObject(response)
                val accessToken = json.getString("access_token")
                val refreshToken = json.getString("refresh_token")
                Log.i("KakaoToken", "accessToken: $accessToken")
                Log.i("KakaoToken", "refreshToken: $refreshToken")

                //벡엔드 전송
               // sendTokenToBackend(accessToken)

                // 토큰 받으면 바로 Main으로 이동
                goToMainActivity()
            },
            { error ->
                Log.e("KakaoToken", "토큰 요청 실패: $error")
                //Toast.makeText(this, "토큰 요청 실패", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String = "application/x-www-form-urlencoded"
            override fun getBody(): ByteArray = requestBody.toByteArray(Charsets.UTF_8)
        }

        Volley.newRequestQueue(this).add(request)
    }

    //메인으로 이동
    private fun goToMainActivity() {
        val intent = Intent(this, Main::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    //벡엔드로 토큰 전송
    private fun sendTokenToBackend(accessToken: String) {

        val url = "https://fitquest25.xyz/api/auth/kakao/"

        // JSON 객체 생성 (access_token만 포함)
        val jsonBody = JSONObject().apply {
            put("access_token", accessToken)
        }

        // JsonObjectRequest 보내기
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                Log.i("BackendAuth", "백엔드 인증 성공 응답: $response")
                //Toast.makeText(this, "백엔드 인증 성공!", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            },
            { error ->
                Log.e("BackendAuth", "백엔드 인증 실패: $error")
                //Toast.makeText(this, "백엔드 인증 실패", Toast.LENGTH_SHORT).show()
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
