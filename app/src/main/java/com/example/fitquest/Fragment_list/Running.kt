package com.example.fitquest.Fragment_list

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class RunningFragment : Fragment() {

    private lateinit var sessionContainer: LinearLayout
    private lateinit var btnRefresh: Button
    private val handler = Handler(Looper.getMainLooper())

    private val sessionTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.running, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        sessionContainer = view.findViewById(R.id.session_container)
        btnRefresh = view.findViewById(R.id.btn_refresh)

        val button1 = view.findViewById<Button>(R.id.easy)
        val button2 = view.findViewById<Button>(R.id.nomal)
        val button3 = view.findViewById<Button>(R.id.hard)
        val buttons = listOf(button1, button2, button3)

        val card2Easy = view.findViewById<LinearLayout>(R.id.card2_easy)
        val card2Nomal = view.findViewById<LinearLayout>(R.id.card2_nomal)
        val card2Hard = view.findViewById<LinearLayout>(R.id.card2_hard)
        val cardViews = listOf(card2Easy, card2Nomal, card2Hard)

        fun updateButtons(selectedIndex: Int) {
            buttons.forEachIndexed { i, btn ->
                if (i == selectedIndex) {
                    btn.setTextColor(Color.WHITE)
                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.BLACK)
                } else {
                    btn.setTextColor(Color.BLACK)
                    btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#9e9f9d"))
                }
            }
            cardViews.forEachIndexed { i, v ->
                v.visibility = if (i == selectedIndex) View.VISIBLE else View.GONE
            }
        }

        updateButtons(0)
        buttons.forEachIndexed { index, btn ->
            btn.setOnClickListener { updateButtons(index) }
        }

        btnRefresh.setOnClickListener {
            btnRefresh.isEnabled = false
            btnRefresh.text = "로딩중..."
            (activity as? HomeFragment.RefreshListener)?.onRefreshRequested()
            handler.postDelayed({
                if (isAdded && context != null) {
                    val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", android.content.Context.MODE_PRIVATE)
                    loadTodaySessions(sharedPref)
                    btnRefresh.isEnabled = true
                    btnRefresh.text = "새로고침"
                }
            }, 3000)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", android.content.Context.MODE_PRIVATE)
        handler.postDelayed({
            if (isAdded && context != null) {
                loadTodaySessions(sharedPref)
            }
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun loadTodaySessions(sharedPref: android.content.SharedPreferences) {
        val sessionsStr = sharedPref.getString("today_sessions", null)
        sessionContainer.removeAllViews()

        if (sessionsStr == null) {
            addEmptySessionCard()
            return
        }

        try {
            val sessionsJson = JSONArray(sessionsStr)
            if (sessionsJson.length() == 0) {
                addEmptySessionCard()
                return
            }

            val totalCount = sessionsJson.length()
            for (i in totalCount - 1 downTo 0) {
                val session = sessionsJson.getJSONObject(i)
                val distanceKm = session.optDouble("distance_km", 0.0)
                val durationSec = session.optInt("duration_sec", 0)
                val calories = session.optDouble("calories_burned", 0.0)
                val startTime = session.optString("start_time", "")
                val endTime = session.optString("end_time", "")
                val avgPace = session.optDouble("avg_pace", 0.0)
                addSessionCard(totalCount - i, distanceKm, durationSec, calories, startTime, endTime, avgPace)
            }
        } catch (e: Exception) {
            addEmptySessionCard()
        }
    }

    private fun formatPace(paceMinPerKm: Double): String {
        if (paceMinPerKm <= 0.0) return "--:--"
        val min = paceMinPerKm.toInt()
        val sec = ((paceMinPerKm - min) * 60).toInt()
        return "${min}:${String.format("%02d", sec)}"
    }

    private fun addSessionCard(
        index: Int,
        distanceKm: Double,
        durationSec: Int,
        calories: Double,
        startTimeStr: String,
        endTimeStr: String,
        avgPace: Double
    ) {
        val startFormatted = try {
            val date = isoFormat.parse(startTimeStr)
            sessionTimeFormat.format(date!!)
        } catch (e: Exception) { startTimeStr }

        val endFormatted = try {
            val date = isoFormat.parse(endTimeStr)
            sessionTimeFormat.format(date!!)
        } catch (e: Exception) { endTimeStr }

        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12.dpToPx() }
            radius = 16.dpToPx().toFloat()
            cardElevation = 4.dpToPx().toFloat()
            setCardBackgroundColor(Color.parseColor("#CCffffff"))
        }

        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // 헤더 (기록 번호 + 시작~종료 시간)
        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val sessionTitle = TextView(requireContext()).apply {
            text = "기록 $index"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#9B6FD4"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val sessionTime = TextView(requireContext()).apply {
            text = "$startFormatted ~ $endFormatted"
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.marginStart = 12.dpToPx() }
        }

        headerLayout.addView(sessionTitle)
        headerLayout.addView(sessionTime)

        // 1행: 거리 / 시간 / 평균페이스
        val statsRow1 = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 12.dpToPx() }
        }

        val min = durationSec / 60
        val sec = durationSec % 60
        statsRow1.addView(makeStatItem("🏃", "${"%.1f".format(distanceKm)} km", "거리"))
        statsRow1.addView(makeStatItem("⏱️", "${min}분 ${sec}초", "시간"))
        statsRow1.addView(makeStatItem("📈", "${formatPace(avgPace)} /km", "평균 페이스"))

        // 2행: 칼로리 / 시작시간 / 종료시간
        val statsRow2 = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 8.dpToPx() }
        }

        statsRow2.addView(makeStatItem("🔥", "${"%.0f".format(calories)} kcal", "칼로리"))
        statsRow2.addView(makeStatItem("🕐", startFormatted, "시작"))
        statsRow2.addView(makeStatItem("🕑", endFormatted, "종료"))

        inner.addView(headerLayout)
        inner.addView(statsRow1)
        inner.addView(statsRow2)
        card.addView(inner)
        sessionContainer.addView(card)
    }

    private fun makeStatItem(emoji: String, value: String, label: String): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            addView(TextView(requireContext()).apply {
                text = emoji
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })
            addView(TextView(requireContext()).apply {
                text = value
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })
            addView(TextView(requireContext()).apply {
                text = label
                textSize = 11f
                setTextColor(Color.parseColor("#888888"))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }

    private fun addEmptySessionCard() {
        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = 16.dpToPx().toFloat()
            cardElevation = 4.dpToPx().toFloat()
            setCardBackgroundColor(Color.parseColor("#CCffffff"))
        }

        val tv = TextView(requireContext()).apply {
            text = "오늘 운동 기록이 없어요 💪"
            textSize = 14f
            setTextColor(Color.parseColor("#888888"))
            gravity = android.view.Gravity.CENTER
            setPadding(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        card.addView(tv)
        sessionContainer.addView(card)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyFont(view.getChildAt(i), typeface)
            }
        } else if (view is TextView) {
            view.typeface = typeface
        } else if (view is MaterialButton) {
            view.typeface = typeface
        }
    }
}