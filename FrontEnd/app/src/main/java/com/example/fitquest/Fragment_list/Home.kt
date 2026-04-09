package com.example.fitquest.Fragment_list

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.fitquest.Activitiy_list.Health
import com.example.fitquest.Activitiy_list.Login
import com.example.fitquest.R
import org.json.JSONArray
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        // 달성업적 조회 https://fitquest25.xyz/api/workout/achievements/
        // 전체업적 조회 https://fitquest25.xyz/api/auth/achievements/
        // 랭킹 관련 조회 https://fitquest25.xyz/api/auth/ranking/
        // 내 정보 조회 https://fitquest25.xyz/api/auth/me/
        view.findViewById<Button>(R.id.btn_test_achievements).setOnClickListener {
            testApi("https://fitquest25.xyz/api/workout/achievements/", "achievement_test")
        }
        view.findViewById<Button>(R.id.btn_test_titles).setOnClickListener {
            testApi("https://fitquest25.xyz/api/auth/titles/", "achievement_test")
        }
        view.findViewById<Button>(R.id.btn_test_me).setOnClickListener {
            testApi("https://fitquest25.xyz/api/auth/me/", "achievement_test")
        }

        timeTextView    = view.findViewById(R.id.timeTextView)
        fightingText    = view.findViewById(R.id.fighting_text)
        tvUsername      = view.findViewById(R.id.tv_username)
        tvLevel         = view.findViewById(R.id.tv_level)
        tvTitle         = view.findViewById(R.id.tv_title)
        tvExp           = view.findViewById(R.id.tv_exp)
        expProgress     = view.findViewById(R.id.exp_progress)
        tvTotalDistance = view.findViewById(R.id.tv_total_distance)
        tvTotalDuration = view.findViewById(R.id.tv_total_duration)
        tvTotalCalories = view.findViewById(R.id.tv_total_calories)

        view.findViewById<Button>(R.id.gologin).setOnClickListener {
            startActivity(Intent(requireContext(), Login::class.java))
        }
        view.findViewById<Button>(R.id.goHealth).setOnClickListener {
            startActivity(Intent(requireContext(), Health::class.java))
        }
        view.findViewById<TextView>(R.id.tv_achievement_btn).setOnClickListener {
            showAchievementDialog()
        }
        view.findViewById<TextView>(R.id.tv_change_title_btn).setOnClickListener {
            showTitleSelectDialog()
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
        fetchUserInfo()
        fetchRunningStats()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    fun refreshStats() {
        fetchUserInfo()
        fetchRunningStats()
    }

    private fun fetchUserInfo() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) {
            tvUsername.text = "사용자"; tvLevel.text = "LV 1"; tvTitle.text = "[칭호 없음]"
            tvExp.text = "EXP 0 / 100"; expProgress.progress = 0
            return
        }

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/auth/me/",
            { response ->
                try {
                    val json     = JSONObject(response)
                    val newId    = json.optInt("id", -1)
                    val nickname = json.optString("nickname", "사용자")
                    val level    = json.optInt("level", 1)
                    val exp      = json.optInt("exp", 0)
                    val title    = json.optString("current_title", "")

                    // 유저 변경 감지 → 누적기록 캐시 초기화
                    val cachedInfo = sharedPref.getString("user_info", null)
                    val cachedId   = if (cachedInfo != null) {
                        try { JSONObject(cachedInfo).optInt("id", -1) } catch (e: Exception) { -1 }
                    } else -1

                    if (newId != -1 && cachedId != -1 && newId != cachedId) {
                        android.util.Log.i("HomeFragment", "유저 변경 감지 ($cachedId → $newId) 캐시 초기화")
                        sharedPref.edit()
                            .remove("running_stats_data")
                            .remove("today_sessions")
                            .remove("sent_sessions")
                            .apply()
                        tvTotalDistance.text = "- km"
                        tvTotalDuration.text = "- 분"
                        tvTotalCalories.text = "- kcal"
                        fetchRunningStats()
                    }

                    sharedPref.edit().putString("user_info", response).apply()
                    tvUsername.text      = nickname
                    tvLevel.text         = "LV $level"
                    tvTitle.text         = if (title.isEmpty() || title == "null") "[칭호 없음]" else "[$title]"
                    tvExp.text           = "EXP $exp / 100"
                    expProgress.progress = exp

                    fetchAchievementCount()
                    fetchUserRanking()

                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "유저정보 파싱 오류: ${e.message}")
                }
            },
            { error ->
                android.util.Log.e("HomeFragment", "유저정보 실패: ${error.networkResponse?.statusCode ?: -1}")
                val cached = sharedPref.getString("user_info", null)
                if (cached != null) {
                    try {
                        val json  = JSONObject(cached)
                        val title = json.optString("current_title", "")
                        val streakDays  = json.optInt("streak_days", 0)

                        tvUsername.text      = json.optString("nickname", "사용자")
                        tvLevel.text         = "LV ${json.optInt("level", 1)}"
                        tvTitle.text         = if (title.isEmpty() || title == "null") "[칭호 없음]" else "[$title]"
                        val exp              = json.optInt("exp", 0)
                        tvExp.text           = "EXP $exp / 100"
                        expProgress.progress = exp
                        view?.findViewById<TextView>(R.id.tv_streak)?.text = "${streakDays}일"
                    } catch (e: Exception) { }
                }
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchAchievementCount() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val tvAchievementCount = view?.findViewById<TextView>(R.id.tv_achievement_count) ?: return

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/workout/achievements/",
            { response ->
                try {
                    val arr   = JSONArray(response)
                    val count = (0 until arr.length()).count {
                        arr.getJSONObject(it).optBoolean("is_achieved", false)
                    }
                    tvAchievementCount.text = "${count}/10개"
                } catch (e: Exception) { }
            },
            { _ -> }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse) =
                try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchUserRanking() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val tvRanking   = view?.findViewById<TextView>(R.id.tv_ranking) ?: return

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/auth/ranking/",
            { response ->
                try {
                    val arr    = JSONArray(response)
                    val myInfo = sharedPref.getString("user_info", null)
                    if (myInfo != null) {
                        val myNick = JSONObject(myInfo).optString("nickname", "")

                        val users = (0 until arr.length()).map { arr.getJSONObject(it) }
                        val sorted = users.sortedWith(
                            compareByDescending<JSONObject> { it.optInt("level", 0) }
                                .thenByDescending { it.optInt("exp", 0) }
                                .thenByDescending { it.optInt("point", 0) }
                        )
                        val rank = sorted.indexOfFirst { it.optString("nickname") == myNick } + 1
                        tvRanking.text = if (rank > 0) "${rank}위" else "-위"
                    }
                } catch (e: Exception) { }
            },
            { _ -> }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse) =
                try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun fetchRunningStats() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) {
            tvTotalDistance.text = "- km"; tvTotalDuration.text = "- 분"; tvTotalCalories.text = "- kcal"
            return
        }

        val cached = sharedPref.getString("running_stats_data", null)
        if (cached != null) {
            try {
                val json = JSONObject(cached)
                tvTotalDistance.text = "${"%.1f".format(json.optDouble("total_distance_km", 0.0))} km"

                val cachedDuration = json.optInt("total_duration_sec", 0)
                val cd = cachedDuration / 86400
                val ch = (cachedDuration % 86400) / 3600
                val cm = (cachedDuration % 3600) / 60
                tvTotalDuration.text = when {
                    cd > 0 -> "${cd}일 ${ch}시간 ${cm}분"
                    ch > 0 -> "${ch}시간 ${cm}분"
                    else   -> "${cm}분"
                }

                tvTotalCalories.text = "${"%.0f".format(json.optDouble("total_calories", 0.0))} kcal"
            } catch (e: Exception) { }
        }

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/workout/running/stats/",
            { response ->
                try {
                    val json          = JSONObject(response)
                    val totalDistance = json.optDouble("total_distance_km", 0.0)
                    val totalDuration = json.optInt("total_duration_sec", 0)
                    val totalCalories = json.optDouble("total_calories", 0.0)
                    sharedPref.edit().putString("running_stats_data", response).apply()
                    tvTotalDistance.text = "${"%.1f".format(totalDistance)} km"

                    val days    = totalDuration / 86400
                    val hours   = (totalDuration % 86400) / 3600
                    val minutes = (totalDuration % 3600) / 60
                    tvTotalDuration.text = when {
                        days > 0  -> "${days}일 ${hours}시간 ${minutes}분"
                        hours > 0 -> "${hours}시간 ${minutes}분"
                        else      -> "${minutes}분"
                    }

                    tvTotalCalories.text = "${"%.0f".format(totalCalories)} kcal"
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "러닝통계 파싱 오류: ${e.message}")
                }
            },
            { error ->
                android.util.Log.e("HomeFragment", "러닝통계 실패: ${error.networkResponse?.statusCode ?: -1}")
                val cached = sharedPref.getString("running_stats_data", null)
                if (cached != null) {
                    try {
                        val json = JSONObject(cached)
                        tvTotalDistance.text = "${"%.1f".format(json.optDouble("total_distance_km", 0.0))} km"
                        tvTotalDuration.text = "${json.optInt("total_duration_sec", 0) / 60} 분"
                        tvTotalCalories.text = "${"%.0f".format(json.optDouble("total_calories", 0.0))} kcal"
                    } catch (e: Exception) { }
                } else {
                    tvTotalDistance.text = "- km"; tvTotalDuration.text = "- 분"; tvTotalCalories.text = "- kcal"
                }
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showAchievementDialog() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/workout/achievements/",
            { response ->
                try { val arr = JSONArray(response); activity?.runOnUiThread { buildAchievementDialog(arr) } }
                catch (e: Exception) { android.util.Log.e("HomeFragment", "업적 파싱 오류: ${e.message}") }
            },
            { error -> android.util.Log.e("HomeFragment", "업적 로드 실패: ${error.networkResponse?.statusCode ?: -1}") }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun buildAchievementDialog(arr: JSONArray) {
        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density
        val scrollView = ScrollView(ctx)
        val container  = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((12*dp).toInt(), (8*dp).toInt(), (12*dp).toInt(), (8*dp).toInt())
        }
        scrollView.addView(container)

        for (i in 0 until arr.length()) {
            val item        = arr.getJSONObject(i)
            val name        = item.optString("name", "")
            val description = item.optString("description", "")
            val metric      = item.optString("metric", "")
            val targetValue = item.optInt("target_value", 0)
            val rewardTitle = item.optString("reward_title", "")
            val isAchieved  = item.optBoolean("is_achieved", false)

            val conditionText = when (metric) {
                "total_distance" -> "${targetValue}km 달성"
                "total_calories" -> "${targetValue}kcal 소모"
                "total_duration" -> { val days = targetValue/86400; val hours = targetValue/3600; if (days>=1) "누적 운동 시간 ${days}일 달성" else "${hours}시간 운동" }
                "total_days" -> "누적 ${targetValue}일 출석"
                else -> "$metric $targetValue"
            }

            val nameColor   = if (isAchieved) Color.parseColor("#4A148C") else Color.parseColor("#AAAAAA")
            val bodyColor   = if (isAchieved) Color.parseColor("#333333") else Color.parseColor("#BBBBBB")
            val rewardColor = if (isAchieved) Color.parseColor("#9B6FD4") else Color.parseColor("#CCCCCC")

            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((14*dp).toInt(), (12*dp).toInt(), (14*dp).toInt(), (12*dp).toInt())
                setBackgroundColor(if (isAchieved) Color.parseColor("#F3EEFF") else Color.parseColor("#F5F5F5"))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { setMargins(0, (5*dp).toInt(), 0, (5*dp).toInt()) }
            }
            card.addView(TextView(ctx).apply { text = if (isAchieved) "🏅 $name" else "🔒 $name"; textSize = 15f; setTextColor(nameColor); setTypeface(null, Typeface.BOLD) })
            card.addView(TextView(ctx).apply { text = description; textSize = 12f; setTextColor(bodyColor); setPadding(0,(3*dp).toInt(),0,0) })
            card.addView(TextView(ctx).apply { text = "달성 조건: $conditionText"; textSize = 12f; setTextColor(bodyColor); setPadding(0,(2*dp).toInt(),0,0) })
            card.addView(TextView(ctx).apply { text = "보상 칭호: [$rewardTitle]"; textSize = 12f; setTextColor(rewardColor); setPadding(0,(2*dp).toInt(),0,0) })
            if (isAchieved) card.addView(TextView(ctx).apply { text = "✅ 달성완료"; textSize = 12f; setTextColor(Color.parseColor("#7B4FBF")); setTypeface(null, Typeface.BOLD); setPadding(0,(6*dp).toInt(),0,0) })
            container.addView(card)
        }
        AlertDialog.Builder(ctx).setTitle("업적 목록").setView(scrollView).setPositiveButton("닫기", null).show()
    }

    private fun showTitleSelectDialog() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET, "https://fitquest25.xyz/api/auth/titles/",
            { response ->
                try { val arr = JSONArray(response); activity?.runOnUiThread { buildTitleSelectDialog(arr) } }
                catch (e: Exception) { android.util.Log.e("HomeFragment", "칭호 파싱 오류: ${e.message}") }
            },
            { error -> android.util.Log.e("HomeFragment", "칭호 로드 실패: ${error.networkResponse?.statusCode ?: -1}") }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun buildTitleSelectDialog(arr: JSONArray) {
        if (arr.length() == 0) {
            AlertDialog.Builder(requireContext()).setTitle("보유 칭호").setMessage("아직 획득한 칭호가 없습니다.\n업적을 달성해 칭호를 얻어보세요!").setPositiveButton("확인", null).show()
            return
        }
        val titles        = mutableListOf<String>()
        val equippedFlags = mutableListOf<Boolean>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            titles.add(item.optString("title", ""))
            equippedFlags.add(item.optBoolean("is_equipped", false))
        }
        val displayTitles = titles.mapIndexed { idx, t -> if (equippedFlags[idx]) "✅  $t  (착용 중)" else "      $t" }.toTypedArray()
        AlertDialog.Builder(requireContext()).setTitle("칭호 선택").setItems(displayTitles) { _, which -> patchTitle(titles[which]) }.setNegativeButton("취소", null).show()
    }

    private fun patchTitle(title: String) {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val request = object : com.android.volley.toolbox.JsonObjectRequest(
            Method.PATCH, "https://fitquest25.xyz/api/auth/me/", JSONObject().put("current_title", title),
            { _ ->
                activity?.runOnUiThread {
                    tvTitle.text = "[$title]"
                    val cached = sharedPref.getString("user_info", null)
                    if (cached != null) { try { val j = JSONObject(cached); j.put("current_title", title); sharedPref.edit().putString("user_info", j.toString()).apply() } catch (e: Exception) {} }
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode ?: -1
                android.util.Log.e("HomeFragment", "칭호 변경 실패 $code")
                if (code != 400) activity?.runOnUiThread { tvTitle.text = "[$title]" }
            }
        ) {
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyFont(view.getChildAt(i), typeface)
        } else if (view is TextView) {
            if (view.id == R.id.tv_title) return  // 칭호는 시스템 폰트 유지
            view.typeface = typeface
        }
    }

    private fun testApi(url: String, tag: String) {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val request = object : com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            { response ->
                android.util.Log.i(tag, "✅ $url 성공!")
                try {
                    val pretty = if (response.trim().startsWith("[")) JSONArray(response).toString(2) else JSONObject(response).toString(2)
                    android.util.Log.i(tag, "응답:\n$pretty")
                } catch (e: Exception) { android.util.Log.i(tag, "응답: $response") }
            },
            { error -> android.util.Log.e(tag, "❌ $url 실패 코드: ${error.networkResponse?.statusCode ?: -1}") }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try { com.android.volley.Response.success(String(response.data, Charsets.UTF_8), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)) }
                catch (e: Exception) { com.android.volley.Response.error(com.android.volley.ParseError(e)) }
            }
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $accessToken", "Content-Type" to "application/json")
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }
}