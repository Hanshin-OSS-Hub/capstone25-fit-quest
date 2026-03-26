package com.example.fitquest.Activitiy_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.example.fitquest.R
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import okhttp3.ResponseBody
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Health : AppCompatActivity() {

//    //전송 성공 여부
//    private val TAG = "Health_test"
//    //삼성 헬스에서 가져오는 데이터
//    private val TAG_DATA = "ss_data"
//    //벡으로 보내는 데이터
//    private val TAG_SEND = "send_data"
//    //유저의 러닝 누적 기록
//    private val TAG_STATS = "running_stats"
//    //사용자 기본 정보
//    private val TAG_INFO = "user_info"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health)

        // lifecycleScope.launch { readAndSendHealthData() }
        // lifecycleScope.launch { getUserInfo() }
        // lifecycleScope.launch { getRunningStats() }

        findViewById<Button>(R.id.gomain).setOnClickListener {
            val intent = Intent(this, Main::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}

//    private suspend fun getUserInfo() {
//        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
//        val accessToken = sharedPref.getString("access_token", "") ?: ""
//
//        try {
//            val response = userApi.getMe("Bearer $accessToken")
//            Log.i(TAG_INFO, "사용자 정보: ${response.string()}")
//        } catch (e: Exception) {
//            Log.e(TAG_INFO, "❌ 오류: ${e.message}")
//        }
//    }
//
//    private suspend fun getRunningStats() {
//        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
//        val accessToken = sharedPref.getString("access_token", "") ?: ""
//
//        try {
//            val response = statsApi.getRunningList("Bearer $accessToken")
//            Log.i(TAG_STATS, "러닝 기록: ${response.string()}")
//        } catch (e: Exception) {
//            Log.e(TAG_STATS, "❌ 오류: ${e.message}")
//        }
//    }
//
//    private suspend fun readAndSendHealthData() {
//        val sharedPref = getSharedPreferences("FitQuestPrefs", MODE_PRIVATE)
//        val accessToken = sharedPref.getString("access_token", "") ?: ""
//
//        Log.i(TAG, "토큰: $accessToken")
//
//        if (accessToken.isEmpty()) {
//            Log.e(TAG, "❌ 토큰 없음 - 로그인 필요")
//            return
//        }
//
//        val sdkStatus = HealthConnectClient.getSdkStatus(this)
//        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
//            Log.e(TAG, "❌ Health Connect 사용 불가")
//            return
//        }
//
//        val client = HealthConnectClient.getOrCreate(this)
//
//        val today = LocalDate.now()
//        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
//        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
//        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)
//
//        try {
//            val exerciseResponse = client.readRecords(
//                ReadRecordsRequest(
//                    recordType = ExerciseSessionRecord::class,
//                    timeRangeFilter = timeRange
//                )
//            )
//
//            if (exerciseResponse.records.isEmpty()) {
//                Log.e(TAG, "❌ 오늘 운동 기록 없음")
//                return
//            }
//
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
//                .withZone(ZoneId.of("UTC"))
//
//            for ((index, session) in exerciseResponse.records.withIndex()) {
//                val sessionStart = session.startTime
//                val sessionEnd = session.endTime
//                val durationSec = (sessionEnd.epochSecond - sessionStart.epochSecond).toInt()
//
//                val distanceResponse = client.readRecords(
//                    ReadRecordsRequest(
//                        recordType = DistanceRecord::class,
//                        timeRangeFilter = TimeRangeFilter.between(sessionStart, sessionEnd)
//                    )
//                )
//                val distanceKm = distanceResponse.records.sumOf { it.distance.inKilometers }
//
//                val calorieResponse = client.readRecords(
//                    ReadRecordsRequest(
//                        recordType = TotalCaloriesBurnedRecord::class,
//                        timeRangeFilter = TimeRangeFilter.between(sessionStart, sessionEnd)
//                    )
//                )
//                val calories = calorieResponse.records.sumOf { it.energy.inKilocalories }
//
//                val avgPace = if (distanceKm > 0) (durationSec / 60.0) / distanceKm else 0.0
//
//                val startTimeStr = formatter.format(sessionStart)
//                val endTimeStr = formatter.format(sessionEnd)
//
//                Log.i(TAG_DATA, "===== 세션 ${index + 1} 데이터 =====")
//                Log.i(TAG_DATA, "distance_km: $distanceKm")
//                Log.i(TAG_DATA, "duration_sec: $durationSec")
//                Log.i(TAG_DATA, "avg_pace_min_per_km: $avgPace")
//                Log.i(TAG_DATA, "current_pace_min_per_km: $avgPace")
//                Log.i(TAG_DATA, "calories_burned: $calories")
//                Log.i(TAG_DATA, "start_time: $startTimeStr")
//                Log.i(TAG_DATA, "end_time: $endTimeStr")
//
//                if (distanceKm <= 0.0) {
//                    Log.i(TAG, "세션 ${index + 1} 스킵 - 거리 0")
//                    continue
//                }
//
//                val requestData = RunningSessionRequest(
//                    distance_km = distanceKm,
//                    duration_sec = durationSec,
//                    avg_pace_min_per_km = avgPace,
//                    current_pace_min_per_km = avgPace,
//                    calories_burned = calories,
//                    start_time = startTimeStr,
//                    end_time = endTimeStr
//                )
//
//                Log.i(TAG_SEND, "===== 세션 ${index + 1} 전송 데이터 =====")
//                Log.i(TAG_SEND, "distance_km: ${requestData.distance_km}")
//                Log.i(TAG_SEND, "duration_sec: ${requestData.duration_sec}")
//                Log.i(TAG_SEND, "avg_pace_min_per_km: ${requestData.avg_pace_min_per_km}")
//                Log.i(TAG_SEND, "current_pace_min_per_km: ${requestData.current_pace_min_per_km}")
//                Log.i(TAG_SEND, "calories_burned: ${requestData.calories_burned}")
//                Log.i(TAG_SEND, "start_time: ${requestData.start_time}")
//                Log.i(TAG_SEND, "end_time: ${requestData.end_time}")
//
//                Log.i(TAG, "===== 세션 ${index + 1} 전송 시작 =====")
//                try {
//                    val response = api.sendRunningSession("Bearer $accessToken", requestData)
//                    Log.i(TAG, "응답 코드: ${response.code()}")
//                    if (response.isSuccessful) {
//                        Log.i(TAG, "✅ 전송 성공!")
//                    } else {
//                        Log.e(TAG, "❌ 전송 실패")
//                        Log.e(TAG, "실패 코드: ${response.code()}")
//                        Log.e(TAG, "실패 메시지: ${response.errorBody()?.string()}")
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "❌ 네트워크 오류: ${e.message}")
//                }
//                Log.i(TAG, "===== 세션 ${index + 1} 전송 종료 =====")
//            }
//
//        } catch (e: SecurityException) {
//            Log.e(TAG, "❌ 권한 없음: ${e.message}")
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ 오류: ${e.message}")
//        }
//    }
//
//    private val retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://fitquest25.xyz/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    private val api by lazy { retrofit.create(RunningApi::class.java) }
//    private val userApi by lazy { retrofit.create(UserApi::class.java) }
//    private val statsApi by lazy { retrofit.create(StatsApi::class.java) }
//}
//
//// Main.kt로 이동했으므로 주석처리
//// data class RunningSessionRequest(
////     val distance_km: Double,
////     val duration_sec: Int,
////     val avg_pace_min_per_km: Double,
////     val current_pace_min_per_km: Double,
////     val calories_burned: Double,
////     val start_time: String,
////     val end_time: String
//// )
//// interface RunningApi {
////     @POST("api/workout/running/hc/")
////     suspend fun sendRunningSession(
////         @Header("Authorization") token: String,
////         @Body request: RunningSessionRequest
////     ): Response<ResponseBody>
//// }
//
//interface UserApi {
//    @GET("api/auth/me/")
//    suspend fun getMe(
//        @Header("Authorization") token: String
//    ): ResponseBody
//}
//
//interface StatsApi {
//    @GET("api/workout/running/stats/")
//    suspend fun getRunningList(
//        @Header("Authorization") token: String
//    ): ResponseBody
//}