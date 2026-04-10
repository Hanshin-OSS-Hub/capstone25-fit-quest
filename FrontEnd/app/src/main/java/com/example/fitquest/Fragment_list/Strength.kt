package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.fitquest.R
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class StrengthFragment : Fragment(R.layout.fragment_strength) {

    private var allItems = listOf<Map<String, String>>()
    private var interRegular: Typeface? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interRegular = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        val container = view.findViewById<LinearLayout>(R.id.strength_list_container)

        val levelButtons = listOf(
            view.findViewById<Button>(R.id.btn_lv1),
            view.findViewById<Button>(R.id.btn_lv2),
            view.findViewById<Button>(R.id.btn_lv3),
            view.findViewById<Button>(R.id.btn_lv4),
            view.findViewById<Button>(R.id.btn_lv5)
        )

        levelButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                showLevel(container, index + 1)
            }
        }

        // API 로드
        thread {
            try {
                val url = URL("https://fitquest25.xyz/api/workout/workouts/")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val response = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                val items = mutableListOf<Map<String, String>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("category") == "strength") {
                        items.add(mapOf(
                            "name" to obj.getString("name"),
                            "target_muscle" to obj.getString("target_muscle"),
                            "equipment" to obj.getString("equipment"),
                            "duration_or_reps" to obj.getString("duration_or_reps"),
                            "level" to obj.getInt("level").toString()
                        ))
                    }
                }

                requireActivity().runOnUiThread {
                    allItems = items
                    // 기본으로 Lv1 표시
                    showLevel(container, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showLevel(container: LinearLayout, level: Int) {
        container.removeAllViews()
        val filtered = allItems.filter { it["level"] == level.toString() }

        for (item in filtered) {
            val card = MaterialCardView(requireContext()).apply {
                radius = 16f * resources.displayMetrics.density
                cardElevation = 4f * resources.displayMetrics.density
                setCardBackgroundColor(0xCCFFFFFF.toInt())
                val marginPx = (8 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, marginPx) }
            }

            val inner = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                val padPx = (16 * resources.displayMetrics.density).toInt()
                setPadding(padPx, padPx, padPx, padPx)
            }

            inner.addView(TextView(requireContext()).apply {
                text = item["name"]
                textSize = 15f
                setTextColor(0xFF000000.toInt())
                setTypeface(interRegular, Typeface.BOLD)
                val pb = (6 * resources.displayMetrics.density).toInt()
                setPadding(0, 0, 0, pb)
            })

            fun addRow(label: String, value: String) {
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    val pb = (4 * resources.displayMetrics.density).toInt()
                    setPadding(0, 0, 0, pb)
                }
                row.addView(TextView(requireContext()).apply {
                    text = label
                    textSize = 13f
                    setTextColor(0xFF555555.toInt())
                    typeface = interRegular
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                row.addView(TextView(requireContext()).apply {
                    text = value
                    textSize = 13f
                    setTextColor(0xFF000000.toInt())
                    typeface = interRegular
                    gravity = android.view.Gravity.END
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                inner.addView(row)
            }

            addRow("운동 부위", item["target_muscle"] ?: "")
            addRow("기구", item["equipment"] ?: "")
            addRow("횟수/세트", item["duration_or_reps"] ?: "")

            card.addView(inner)
            container.addView(card)
        }
    }
}