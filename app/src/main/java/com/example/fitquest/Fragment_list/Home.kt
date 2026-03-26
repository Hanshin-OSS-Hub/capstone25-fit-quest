package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.fitquest.Activitiy_list.Health
import com.example.fitquest.Activitiy_list.Login
import com.example.fitquest.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    interface RefreshListener {
        fun onRefreshRequested()
    }

    private lateinit var fightingText: TextView
    private lateinit var timeTextView: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvExp: TextView
    private lateinit var expProgress: ProgressBar
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalDuration: TextView
    private lateinit var tvTotalCalories: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val quoteInterval = 5000L
    private lateinit var quoteRunnable: Runnable
    private var currentIndex = 0

    private val quotes = listOf(
        "오늘도 한 걸음 더 나아갔어요! 🎉",
        "꾸준함이 만드는 변화, 당신이 증명하고 있어요 💪",
        "어제보다 나은 오늘을 만들고 있어요 ✨",
        "작은 습관이 큰 변화를 만듭니다 🌱",
        "당신의 노력이 결실을 맺고 있어요 🌟",
        "매일매일 성장하는 당신, 정말 멋져요! 🚀",
        "포기하지 않는 당신이 진정한 챔피언이에요 🏆"
    )

    private val timeFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    private val updateTimeTask = object : Runnable {
        override fun run() {
            timeTextView.text = timeFormat.format(System.currentTimeMillis())
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        //업적 테스트
        view.findViewById<Button>(R.id.btn_test_achievements).setOnClickListener {
            testApi("https://fitquest25.xyz/api/achievements/", "achievement_test")
        }

        view.findViewById<Button>(R.id.btn_test_titles).setOnClickListener {
            testApi("https://fitquest25.xyz/api/titles/", "achievement_test")
        }

        view.findViewById<Button>(R.id.btn_test_me).setOnClickListener {
            testApi("https://fitquest25.xyz/api/auth/me/", "achievement_test")
        }

        timeTextView = view.findViewById(R.id.timeTextView)
        fightingText = view.findViewById(R.id.fighting_text)
        tvUsername = view.findViewById(R.id.tv_username)
        tvLevel = view.findViewById(R.id.tv_level)
        tvTitle = view.findViewById(R.id.tv_title)
        tvExp = view.findViewById(R.id.tv_exp)
        expProgress = view.findViewById(R.id.exp_progress)
        tvTotalDistance = view.findViewById(R.id.tv_total_distance)
        tvTotalDuration = view.findViewById(R.id.tv_total_duration)
        tvTotalCalories = view.findViewById(R.id.tv_total_calories)

        val goMainButton = view.findViewById<Button>(R.id.gologin)
        goMainButton.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }

        val goHealthButton = view.findViewById<Button>(R.id.goHealth)
        goHealthButton.setOnClickListener {
            val intent = Intent(requireContext(), Health::class.java)
            startActivity(intent)
        }

        quoteRunnable = object : Runnable {
            override fun run() {
                fightingText.text = quotes[currentIndex]
                currentIndex = (currentIndex + 1) % quotes.size
                handler.postDelayed(this, quoteInterval)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeTask)
        handler.post(quoteRunnable)

        val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", android.content.Context.MODE_PRIVATE)

        // 유저 정보
        val userInfoStr = sharedPref.getString("user_info", null)
        if (userInfoStr != null) {
            try {
                val userJson = JSONObject(userInfoStr)
                val nickname = userJson.optString("nickname", "사용자")
                val level = userJson.optInt("level", 1)
                val exp = userJson.optInt("exp", 0)
                val title = userJson.optString("title", "")

                tvUsername.text = nickname
                tvLevel.text = "LV $level"
                tvTitle.text = if (title.isEmpty()) "[칭호 없음]" else "[$title]"
                tvExp.text = "EXP $exp / 100"
                expProgress.progress = exp
            } catch (e: Exception) {
                tvUsername.text = "사용자"
                tvLevel.text = "LV 1"
                tvTitle.text = "[칭호 없음]"
                tvExp.text = "EXP 0 / 100"
                expProgress.progress = 0
            }
        }

        // 누적 기록
        val statsStr = sharedPref.getString("running_stats_data", null)
        if (statsStr != null) {
            try {
                val statsJson = JSONObject(statsStr)
                val totalDistance = statsJson.optDouble("total_distance_km", 0.0)
                val totalDuration = statsJson.optInt("total_duration_sec", 0)
                val totalCalories = statsJson.optDouble("total_calories", 0.0)

                tvTotalDistance.text = "${"%.1f".format(totalDistance)} km"
                tvTotalDuration.text = "${totalDuration / 60} 분"
                tvTotalCalories.text = "${"%.0f".format(totalCalories)} kcal"
            } catch (e: Exception) {
                tvTotalDistance.text = "- km"
                tvTotalDuration.text = "- 분"
                tvTotalCalories.text = "- kcal"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyFont(view.getChildAt(i), typeface)
            }
        } else if (view is TextView) {
            view.typeface = typeface
        }
    }

    private fun testApi(url: String, tag: String) {
        val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", android.content.Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        val request = object : com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            { response ->
                android.util.Log.i(tag, "✅ $url 성공!")
                android.util.Log.i(tag, "응답: $response")
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: -1
                android.util.Log.e(tag, "❌ $url 실패 코드: $statusCode")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $accessToken",
                    "Content-Type" to "application/json"
                )
            }
        }

        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }
}