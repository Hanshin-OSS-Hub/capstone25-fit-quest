package com.example.fitquest.Fragment_list

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.Volley
import com.example.fitquest.R
import org.json.JSONArray
import org.json.JSONObject

class QuestFragment : Fragment() {

    private lateinit var dailyButton: Button
    private lateinit var weeklyButton: Button
    private lateinit var monthlyButton: Button
    private lateinit var questContainer: LinearLayout

    // 달성률 카드 뷰
    private lateinit var tvAchievementPercent: TextView
    private lateinit var progressAchievement: ProgressBar
    private lateinit var tvDailyCount: TextView
    private lateinit var tvWeeklyCount: TextView
    private lateinit var tvMonthlyCount: TextView

    // 전체 퀘스트 데이터 (cycle 기준으로 분류)
    private val allQuests = mutableListOf<JSONObject>()

    private var currentTab = "daily"
    private var lastLoadedDate: String = ""
    private var currentWeekKey: String = "" // 서버 응답에서 추출한 이번 주 cycle_key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        dailyButton   = view.findViewById(R.id.dailyButton)
        weeklyButton  = view.findViewById(R.id.weeklyButton)
        monthlyButton = view.findViewById(R.id.monthlyButton)
        questContainer = view.findViewById(R.id.quest_card_container)

        tvAchievementPercent = view.findViewById(R.id.tv_achievement_percent)
        progressAchievement  = view.findViewById(R.id.progress_achievement)
        tvDailyCount         = view.findViewById(R.id.tv_daily_count)
        tvWeeklyCount        = view.findViewById(R.id.tv_weekly_count)
        tvMonthlyCount       = view.findViewById(R.id.tv_monthly_count)

        val buttons = listOf(dailyButton, weeklyButton, monthlyButton)
        selectButton(dailyButton, buttons)

        dailyButton.setOnClickListener {
            currentTab = "daily"
            selectButton(dailyButton, buttons)
            renderQuestCards()
        }
        weeklyButton.setOnClickListener {
            currentTab = "weekly"
            selectButton(weeklyButton, buttons)
            renderQuestCards()
        }
        monthlyButton.setOnClickListener {
            currentTab = "monthly"
            selectButton(monthlyButton, buttons)
            renderQuestCards()
        }

        // 테스트 버튼: 퀘스트 강제 재로드 + cycle_key 로그 출력
        view.findViewById<Button>(R.id.btn_test_quest_api).setOnClickListener {
            lastLoadedDate = "" // 강제 초기화 → 날짜 무관하게 재조회
            fetchMyQuests()
        }

        fetchMyQuests()
    }

    override fun onResume() {
        super.onResume()
        val todayStr = java.time.LocalDate.now().toString()
        // 날짜가 바뀌었거나 아직 한 번도 로드 안 한 경우에만 API 재조회
        if (todayStr != lastLoadedDate) {
            fetchMyQuests()
        }
    }

    // ──────────────────────────────────────────
    //  API: 내 퀘스트 목록 불러오기
    // ──────────────────────────────────────────
    private fun fetchMyQuests() {
        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) {
            Log.e("QuestFragment", "토큰 없음")
            return
        }

        showLoading(true)

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET,
            "https://fitquest25.xyz/api/workout/my-quests/",
            { response ->
                showLoading(false)
                try {
                    val arr = JSONArray(response)
                    allQuests.clear()
                    for (i in 0 until arr.length()) {
                        allQuests.add(arr.getJSONObject(i))
                    }
                    // 서버 응답에서 이번 주 cycle_key 직접 추출 (YYYY-WXX 형식)
                    currentWeekKey = allQuests
                        .map { it.optString("cycle_key", "") }
                        .firstOrNull { it.matches(Regex("\\d{4}-W\\d{2}")) } ?: ""
                    lastLoadedDate = java.time.LocalDate.now().toString()

                    // ── 디버그: 받은 cycle_key 전체 출력 ──
                    val today = java.time.LocalDate.now()
                    Log.i("quest_cycle", "===== cycle_key 목록 (총 ${allQuests.size}개) =====")
                    Log.i("quest_cycle", "앱 계산값 -> daily:$today / weekly:$currentWeekKey / monthly:${buildMonthKey(today)}")
                    for (q in allQuests) {
                        Log.i("quest_cycle", "id=${q.optInt("id")} | cycle_key=[${q.optString("cycle_key")}] | ${q.optString("quest_name")}")
                    }
                    Log.i("quest_cycle", "===========================================")

                    updateAchievementStats()
                    renderQuestCards()
                    Log.i("QuestFragment", "퀘스트 ${allQuests.size}개 로드 완료 (날짜: $lastLoadedDate)")
                } catch (e: Exception) {
                    Log.e("QuestFragment", "파싱 오류: ${e.message}")
                    showError("퀘스트를 불러오지 못했습니다.")
                }
            },
            { error ->
                showLoading(false)
                val code = error.networkResponse?.statusCode ?: -1
                Log.e("QuestFragment", "퀘스트 로드 실패 코드: $code")
                showError("네트워크 오류 ($code)")
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse) =
                try {
                    com.android.volley.Response.success(
                        String(response.data, Charsets.UTF_8),
                        com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)
                    )
                } catch (e: Exception) {
                    com.android.volley.Response.error(com.android.volley.ParseError(e))
                }

            override fun getHeaders() = mutableMapOf(
                "Authorization" to "Bearer $accessToken",
                "Content-Type"  to "application/json"
            )
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    // ──────────────────────────────────────────
    //  달성률 통계 업데이트
    // ──────────────────────────────────────────
    private fun updateAchievementStats() {
        val today      = java.time.LocalDate.now()
        val todayStr   = today.toString()
        val weekStr    = currentWeekKey
        val monthStr   = buildMonthKey(today)

        var dailyTotal = 0; var dailyDone = 0
        var weeklyTotal = 0; var weeklyDone = 0
        var monthlyTotal = 0; var monthlyDone = 0

        for (q in allQuests) {
            val cycleKey    = q.optString("cycle_key", "")
            val isCompleted = q.optBoolean("is_completed", false)
            val isClaimed   = q.optBoolean("is_reward_claimed", false)
            val counted     = isCompleted || isClaimed

            when {
                cycleKey == todayStr -> { dailyTotal++; if (counted) dailyDone++ }
                cycleKey == weekStr  -> { weeklyTotal++; if (counted) weeklyDone++ }
                cycleKey == monthStr -> { monthlyTotal++; if (counted) monthlyDone++ }
            }
        }

        val totalAll  = dailyTotal + weeklyTotal + monthlyTotal
        val doneAll   = dailyDone  + weeklyDone  + monthlyDone
        val pct       = if (totalAll > 0) (doneAll * 100 / totalAll) else 0

        activity?.runOnUiThread {
            tvAchievementPercent.text    = "$pct%"
            progressAchievement.progress = pct
            tvDailyCount.text   = "$dailyDone/$dailyTotal"
            tvWeeklyCount.text  = "$weeklyDone/$weeklyTotal"
            tvMonthlyCount.text = "$monthlyDone/$monthlyTotal"
        }
    }

    // ──────────────────────────────────────────
    //  현재 탭에 맞는 퀘스트 카드 렌더링
    // ──────────────────────────────────────────
    private fun renderQuestCards() {
        questContainer.removeAllViews()

        val today    = java.time.LocalDate.now()
        val todayStr = today.toString()
        val weekStr  = currentWeekKey
        val monthStr = buildMonthKey(today)

        val filtered = allQuests.filter { q ->
            val cycleKey = q.optString("cycle_key", "")
            when (currentTab) {
                "daily"   -> cycleKey == todayStr
                "weekly"  -> cycleKey == weekStr
                "monthly" -> cycleKey.length == 7 && !cycleKey.contains("W")
                else      -> false
            }
        }

        if (filtered.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text    = "퀘스트가 없습니다."
                textSize = 14f
                setTextColor(Color.parseColor("#cccccc"))
                setPadding(0, 40, 0, 0)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            questContainer.addView(tv)
            return
        }

        for (quest in filtered) {
            questContainer.addView(buildQuestCard(quest))
        }
    }

    // ──────────────────────────────────────────
    //  퀘스트 카드 뷰 동적 생성
    // ──────────────────────────────────────────
    private fun buildQuestCard(quest: JSONObject): View {
        val ctx         = requireContext()
        val dp          = ctx.resources.displayMetrics.density

        val progressId  = quest.optInt("id", -1)
        val questName   = quest.optString("quest_name", "퀘스트")
        val questDesc   = quest.optString("quest_desc", "")
        val targetValue = quest.optDouble("target_value", 1.0)
        val progressVal = quest.optDouble("progress_value", 0.0)
        val isCompleted = quest.optBoolean("is_completed", false)
        val isClaimed   = quest.optBoolean("is_reward_claimed", false)
        val rewardXp    = quest.optInt("reward_xp", 0)
        val cycleKey    = quest.optString("cycle_key", "")

        // 아이콘 선택 (퀘스트 이름/설명 키워드 기반)
        val icon = when {
            questName.contains("달리") || questDesc.contains("달리") || questDesc.contains("km") -> "🏃"
            questName.contains("걷") || questDesc.contains("걷") -> "🚶"
            questName.contains("칼로리") || questDesc.contains("칼로리") || questDesc.contains("kcal") -> "🔥"
            questName.contains("스트레칭") || questDesc.contains("스트레칭") -> "🧘"
            questName.contains("근력") || questName.contains("팔굽") -> "💪"
            questName.contains("수영") -> "🏊"
            questName.contains("자전거") -> "🚴"
            else -> "🏅"
        }

        // 진행도 퍼센트 (0~100)
        val progressPct = if (targetValue > 0) ((progressVal / targetValue) * 100).toInt().coerceIn(0, 100) else 0

        // 단위 추출
        val unit = when {
            questDesc.contains("km") -> "km"
            questDesc.contains("kcal") -> "kcal"
            questDesc.contains("분") -> "분"
            questDesc.contains("개") -> "개"
            questDesc.contains("일") && cycleKey.length > 7 -> "일"
            else -> ""
        }
        val progressText = if (unit.isNotEmpty()) {
            val prog = if (progressVal == progressVal.toLong().toDouble()) progressVal.toLong().toString() else "%.1f".format(progressVal)
            val tgt  = if (targetValue == targetValue.toLong().toDouble()) targetValue.toLong().toString() else "%.1f".format(targetValue)
            "$prog / $tgt $unit"
        } else {
            "${progressVal.toLong()} / ${targetValue.toLong()}"
        }

        // ── 카드 MaterialCardView (programmatic) ──
        val card = com.google.android.material.card.MaterialCardView(ctx).apply {
            radius          = 16 * dp
            cardElevation   = 4 * dp
            setCardBackgroundColor(Color.parseColor("#CCffffff"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, (12 * dp).toInt()) }
        }

        // 카드 내부 루트 ConstraintLayout 대신 LinearLayout으로 간단하게 구성
        val innerLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((14 * dp).toInt(), (12 * dp).toInt(), (14 * dp).toInt(), (12 * dp).toInt())
        }

        // 왼쪽: 아이콘
        val tvIcon = TextView(ctx).apply {
            text     = icon
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER_VERTICAL }
        }

        // 가운데: 텍스트 + 프로그레스바
        val contentLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { setMargins((10 * dp).toInt(), 0, (8 * dp).toInt(), 0) }
        }

        val tvTitle = TextView(ctx).apply {
            text  = questName
            textSize = 14f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
        }
        val tvBadge = TextView(ctx).apply {
            text  = "+$rewardXp EXP"
            textSize = 11f
            setTextColor(Color.parseColor("#9B6FD4"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, (2 * dp).toInt(), 0, 0)
        }
        val progressBar = ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal).apply {
            max      = 100
            progress = progressPct
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (6 * dp).toInt()
            ).apply { setMargins(0, (6 * dp).toInt(), 0, 0) }
        }
        val tvProgressText = TextView(ctx).apply {
            text     = progressText
            textSize = 10f
            setTextColor(Color.parseColor("#9B6FD4"))
            setPadding(0, (3 * dp).toInt(), 0, 0)
        }

        // 퀘스트 설명 (선택적)
        if (questDesc.isNotEmpty()) {
            val tvDesc = TextView(ctx).apply {
                text     = questDesc
                textSize = 11f
                setTextColor(Color.parseColor("#666666"))
                setPadding(0, (2 * dp).toInt(), 0, 0)
            }
            contentLayout.addView(tvTitle)
            contentLayout.addView(tvDesc)
            contentLayout.addView(tvBadge)
        } else {
            contentLayout.addView(tvTitle)
            contentLayout.addView(tvBadge)
        }
        contentLayout.addView(progressBar)
        contentLayout.addView(tvProgressText)

        // 오른쪽: 달성하기 버튼
        val btnComplete = Button(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                (72 * dp).toInt(), (32 * dp).toInt()
            ).apply {
                gravity = android.view.Gravity.CENTER_VERTICAL
                setMargins((4 * dp).toInt(), 0, 0, 0)
            }
            textSize = 10f
            setPadding(0, 0, 0, 0)
            (this as? Button)?.let {
                // inset 제거를 위해 insetTop/Bottom은 XML에서만 가능 → 패딩으로 대체
            }

            when {
                isClaimed -> {
                    text = "완료"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9e9f9d"))
                    setTextColor(Color.WHITE)
                    isEnabled = false
                }
                isCompleted -> {
                    text = "달성하기"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9B6FD4"))
                    setTextColor(Color.WHITE)
                    isEnabled = true
                }
                else -> {
                    text = "진행중"
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
                    setTextColor(Color.parseColor("#888888"))
                    isEnabled = false
                }
            }
        }

        // 달성하기 클릭 → claim API 호출
        if (isCompleted && !isClaimed) {
            btnComplete.setOnClickListener {
                claimQuestReward(progressId, btnComplete)
            }
        }

        innerLayout.addView(tvIcon)
        innerLayout.addView(contentLayout)
        innerLayout.addView(btnComplete)
        card.addView(innerLayout)
        return card
    }

    // ──────────────────────────────────────────
    //  API: 보상 클레임 POST
    // ──────────────────────────────────────────
    private fun claimQuestReward(progressId: Int, btn: Button) {
        if (progressId == -1) {
            Log.e("QuestFragment", "유효하지 않은 progressId")
            return
        }

        val sharedPref  = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""
        val url         = "https://fitquest25.xyz/api/workout/quests/claim/$progressId/"

        btn.isEnabled = false
        btn.text      = "처리중..."

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.POST, url,
            { response ->
                Log.i("QuestFragment", "클레임 성공: $response")

                val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", Context.MODE_PRIVATE)
                val userInfo = sharedPref.getString("user_info", null)
                val userId = if (userInfo != null) {
                    try { JSONObject(userInfo).optInt("id", -1) } catch (e: Exception) { -1 }
                } else -1

                if (userId != -1) {
                    val key = "total_quests_claimed_$userId"
                    val current = sharedPref.getInt(key, 0)
                    sharedPref.edit().putInt(key, current + 1).apply()
                }

                activity?.runOnUiThread {
                    btn.text = "완료"
                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9e9f9d"))
                    btn.setTextColor(Color.WHITE)
                    btn.isEnabled = false

                    Toast.makeText(requireContext(), "보상을 획득했습니다! 🎉", Toast.LENGTH_SHORT).show()

                    // 데이터 갱신
                    fetchMyQuests()
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode ?: -1
                val body = error.networkResponse?.let { String(it.data) } ?: "응답 없음"
                Log.e("QuestFragment", "클레임 실패 $code: $body")
                activity?.runOnUiThread {
                    btn.text = "달성하기"
                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9B6FD4"))
                    btn.setTextColor(Color.WHITE)
                    btn.isEnabled = true
                    Toast.makeText(requireContext(), "오류가 발생했습니다. ($code)", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse) =
                try {
                    com.android.volley.Response.success(
                        String(response.data, Charsets.UTF_8),
                        com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)
                    )
                } catch (e: Exception) {
                    com.android.volley.Response.error(com.android.volley.ParseError(e))
                }

            override fun getHeaders() = mutableMapOf(
                "Authorization" to "Bearer $accessToken",
                "Content-Type"  to "application/json"
            )

            // POST body 없이 보내는 경우 빈 body
            override fun getBody(): ByteArray = ByteArray(0)
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    // ──────────────────────────────────────────
    //  UI 유틸
    // ──────────────────────────────────────────
    private fun showLoading(show: Boolean) {
        activity?.runOnUiThread {
            view?.findViewById<ProgressBar>(R.id.progress_loading)?.visibility =
                if (show) View.VISIBLE else View.GONE
        }
    }

    private fun showError(msg: String) {
        activity?.runOnUiThread {
            questContainer.removeAllViews()
            val tv = TextView(requireContext()).apply {
                text     = msg
                textSize = 14f
                setTextColor(Color.parseColor("#ffaaaa"))
                setPadding(0, 40, 0, 0)
                gravity  = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            questContainer.addView(tv)
        }
    }

    private fun selectButton(selected: Button, all: List<Button>) {
        all.forEach { btn ->
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9e9f9d"))
            btn.setTextColor(Color.BLACK)
        }
        selected.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
        selected.setTextColor(Color.WHITE)
    }

    // ──────────────────────────────────────────
    //  cycle_key 생성 헬퍼
    // ──────────────────────────────────────────

    /** 서버의 월간 cycle_key 형식: 2026-04 */
    private fun buildMonthKey(date: java.time.LocalDate): String {
        return "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
    }

    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyFont(view.getChildAt(i), typeface)
        } else if (view is TextView) {
            view.typeface = typeface
        } else if (view is Button) {
            view.typeface = typeface
        }
    }
}