package com.example.fitquest.Activitiy_list

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.example.fitquest.Fragment_list.ExerciseFragment
import com.example.fitquest.Fragment_list.HomeFragment
import com.example.fitquest.Fragment_list.QuestFragment
import com.example.fitquest.Fragment_list.RankingFragment
import com.example.fitquest.Fragment_list.RunningFragment
import com.example.fitquest.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Main : AppCompatActivity(), HomeFragment.RefreshListener {

    private val TAG       = "Health_test"
    private val TAG_DATA  = "ss_data"
    private val TAG_SEND  = "send_data"
    private val TAG_STATS = "running_stats"
    private val TAG_INFO  = "user_info"
    private val TAG_DUP   = "dup_check"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        replaceFragment(HomeFragment())

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        replaceFragment(HomeFragment())
        bottomNav.itemIconSize = 120
        bottomNav.selectedItemId = R.id.home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.running  -> replaceFragment(RunningFragment())
                R.id.exercise -> replaceFragment(ExerciseFragment())
                R.id.home     -> replaceFragment(HomeFragment())
                R.id.quest    -> replaceFragment(QuestFragment())
                R.id.ranking  -> replaceFragment(RankingFragment())
            }
            true
        }
        lifecycleScope.launch {
            readAndSendHealthData()
            notifyHomeFragment()
        }
        lifecycleScope.launch { getUserInfo() }
    }

    override fun onRefreshRequested() {
        lifecycleScope.launch {
            readAndSendHealthData()
            notifyHomeFragment()
        }
    }

    // ★ 현재 표시 중인 HomeFragment에 갱신 요청
    private fun notifyHomeFragment() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is HomeFragment && fragment.isAdded && !fragment.isDetached) {
                runOnUiThread { fragment.refreshStats() }
                return
            }
        }
    }

    private suspend fun getUserInfo() {
        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        try {
            val response    = userApi.getMe("Bearer $accessToken")
            val responseStr = response.string()
            Log.i(TAG_INFO, "사용자 정보: $responseStr")
            sharedPref.edit().putString("user_info", responseStr).apply()
        } catch (e: Exception) {
            Log.e(TAG_INFO, "❌ 오류: ${e.message}")
        }
    }

    private suspend fun getRunningStats() {
        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        try {
            val response    = statsApi.getRunningList("Bearer $accessToken")
            val responseStr = response.string()
            Log.i(TAG_STATS, "러닝 기록: $responseStr")
            sharedPref.edit().putString("running_stats_data", responseStr).apply()
        } catch (e: Exception) {
            Log.e(TAG_STATS, "❌ 오류: ${e.message}")
        }
    }

    private suspend fun readAndSendHealthData() {
        val sharedPref  = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        sharedPref.edit().remove("today_sessions").apply()

        Log.i(TAG, "토큰: $accessToken")

        if (accessToken.isEmpty()) {
            Log.e(TAG, "❌ 토큰 없음 - 로그인 필요")
            return
        }

        // 유저 ID 추출
        val userInfo = sharedPref.getString("user_info", null)
        val userId = if (userInfo != null) {
            try { JSONObject(userInfo).optInt("id", -1) } catch (e: Exception) { -1 }
        } else -1

        if (userId == -1) {
            Log.e(TAG, "❌ 유저 ID 없음 - 전송 중단")
            getRunningStats()
            return
        }

        val sentSessionsKey = "sent_sessions_$userId"  // ★ 계정별 키
        Log.i(TAG, "토큰: $accessToken / 유저ID: $userId")


        val sdkStatus = HealthConnectClient.getSdkStatus(this)
        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
            Log.e(TAG, "❌ Health Connect 사용 불가")
            return
        }

        val client = HealthConnectClient.getOrCreate(this)

        val today      = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay   = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val timeRange  = TimeRangeFilter.between(startOfDay, endOfDay)

        try {
            val exerciseResponse = client.readRecords(
                ReadRecordsRequest(recordType = ExerciseSessionRecord::class, timeRangeFilter = timeRange)
            )

            if (exerciseResponse.records.isEmpty()) {
                Log.e(TAG, "❌ 오늘 운동 기록 없음")
                getRunningStats()   // 운동 없어도 최신 통계는 가져옴
                return
            }

            val formatter    = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"))
            val sentSessionsKey = "sent_sessions_$userId"
            val sentSessions = sharedPref.getStringSet(sentSessionsKey, mutableSetOf()) ?: mutableSetOf()

            for ((index, session) in exerciseResponse.records.withIndex()) {
                val sessionStart = session.startTime
                val sessionEnd   = session.endTime
                val durationSec  = (sessionEnd.epochSecond - sessionStart.epochSecond).toInt()

                val distanceResponse = client.readRecords(
                    ReadRecordsRequest(DistanceRecord::class, TimeRangeFilter.between(sessionStart, sessionEnd))
                )
                val distanceKm = distanceResponse.records.sumOf { it.distance.inKilometers }

                val calorieResponse = client.readRecords(
                    ReadRecordsRequest(TotalCaloriesBurnedRecord::class, TimeRangeFilter.between(sessionStart, sessionEnd))
                )
                val calories = calorieResponse.records.sumOf { it.energy.inKilocalories }
                val avgPace  = if (distanceKm > 0) (durationSec / 60.0) / distanceKm else 0.0

                val startTimeStr = formatter.format(sessionStart)
                val endTimeStr   = formatter.format(sessionEnd)
                val sessionKey = "${startTimeStr}_${distanceKm}_${durationSec}"

                Log.i(TAG_DATA, "===== 세션 ${index + 1} 데이터 =====")
                Log.i(TAG_DATA, "distance_km: $distanceKm / duration_sec: $durationSec / calories: $calories")

                if (distanceKm <= 0.0) {
                    Log.i(TAG, "세션 ${index + 1} 스킵 - 거리 0")
                    continue
                }

                // 오늘 세션 저장
                val sessionJson = JSONObject().apply {
                    put("distance_km", distanceKm)
                    put("duration_sec", durationSec)
                    put("calories_burned", calories)
                    put("start_time", startTimeStr)
                    put("end_time", endTimeStr)
                    put("avg_pace", avgPace)
                }
                val existingStr   = sharedPref.getString("today_sessions", "[]")
                val existingArray = JSONArray(existingStr)
                var alreadySaved  = false
                for (i in 0 until existingArray.length()) {
                    if (existingArray.getJSONObject(i).optString("start_time") == startTimeStr) {
                        alreadySaved = true; break
                    }
                }
                if (!alreadySaved) {
                    existingArray.put(sessionJson)
                    sharedPref.edit().putString("today_sessions", existingArray.toString()).apply()
                    Log.i(TAG, "✅ 세션 저장 완료: $startTimeStr / 세션 수: ${existingArray.length()}")
                }

                // 중복 전송 방지
                if (sentSessions.contains(sessionKey)) {
                    Log.i(TAG_DUP, "세션 ${index + 1} 이미 전송됨 - 스킵: $sessionKey")
                    continue
                }

                val requestData = RunningSessionRequest(
                    distance_km             = distanceKm,
                    duration_sec            = durationSec,
                    avg_pace_min_per_km     = avgPace,
                    current_pace_min_per_km = avgPace,
                    calories_burned         = calories,
                    start_time              = startTimeStr,
                    end_time                = endTimeStr
                )

                Log.i(TAG, "===== 세션 ${index + 1} 전송 시작 =====")
                try {
                    val response = api.sendRunningSession("Bearer $accessToken", requestData)
                    Log.i(TAG, "응답 코드: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.i(TAG, "✅ 전송 성공!")
                        val updated = sentSessions.toMutableSet()
                        updated.add(sessionKey)
                        sharedPref.edit().putStringSet(sentSessionsKey, updated).apply()
                        Log.i(TAG_DUP, "전송 완료 저장: $sessionKey")
                    } else {
                        Log.e(TAG, "❌ 전송 실패 ${response.code()}: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 네트워크 오류: ${e.message}")
                }
                Log.i(TAG, "===== 세션 ${index + 1} 전송 종료 =====")
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 권한 없음: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 오류: ${e.message}")
        }

        // ★ 모든 전송 완료 후 통계 갱신 (순서 보장)
        getRunningStats()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://fitquest25.xyz/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api      by lazy { retrofit.create(RunningApi::class.java) }
    private val userApi  by lazy { retrofit.create(UserApi::class.java) }
    private val statsApi by lazy { retrofit.create(StatsApi::class.java) }

    private fun replaceFragment(fragment: RunningFragment)  { supportFragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit() }
    private fun replaceFragment(fragment: ExerciseFragment) { supportFragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit() }
    private fun replaceFragment(fragment: HomeFragment)     { supportFragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit() }
    private fun replaceFragment(fragment: QuestFragment)    { supportFragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit() }
    private fun replaceFragment(fragment: RankingFragment)  { supportFragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit() }
}

data class RunningSessionRequest(
    val distance_km: Double,
    val duration_sec: Int,
    val avg_pace_min_per_km: Double,
    val current_pace_min_per_km: Double,
    val calories_burned: Double,
    val start_time: String,
    val end_time: String
)

interface RunningApi {
    @POST("api/workout/running/")
    suspend fun sendRunningSession(
        @Header("Authorization") token: String,
        @Body request: RunningSessionRequest
    ): Response<ResponseBody>
}

interface UserApi {
    @GET("api/auth/me/")
    suspend fun getMe(@Header("Authorization") token: String): ResponseBody
}

interface StatsApi {
    @GET("api/workout/running/stats/")
    suspend fun getRunningList(@Header("Authorization") token: String): ResponseBody
}