package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.fitquest.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class StretchingFragment : Fragment(R.layout.fragment_stretching) {

    private var allItems = listOf<Map<String, String>>()
    private var interRegular: Typeface? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interRegular = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        val container = view.findViewById<LinearLayout>(R.id.stretching_list_container)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_muscle)

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
                    if (obj.getString("category") == "stretching") {
                        items.add(mapOf(
                            "name"             to obj.getString("name"),
                            "target_muscle"    to obj.getString("target_muscle"),
                            "equipment"        to obj.getString("equipment"),
                            "duration_or_reps" to obj.getString("duration_or_reps")
                        ))
                    }
                }

                requireActivity().runOnUiThread {
                    allItems = items

                    // 중복 없는 부위 목록 (등장 순서 유지)
                    val muscles = items.map { it["target_muscle"] ?: "" }
                        .distinct()
                        .filter { it.isNotBlank() }

                    // Chip 생성
                    muscles.forEach { muscle ->
                        val chip = Chip(requireContext()).apply {
                            text = muscle
                            isCheckable = true
                            typeface = interRegular
                            setTextColor(resources.getColorStateList(
                                com.google.android.material.R.color.mtrl_choice_chip_text_color,
                                requireContext().theme
                            ))
                        }
                        chipGroup.addView(chip)
                    }

                    // 첫 번째 부위 자동 선택
                    if (chipGroup.childCount > 0) {
                        (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                        showMuscle(container, muscles.firstOrNull() ?: "")
                    }

                    // Chip 선택 리스너
                    chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                        val selectedChip = checkedIds.firstOrNull()
                            ?.let { group.findViewById<Chip>(it) }
                        showMuscle(container, selectedChip?.text?.toString() ?: "")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showMuscle(container: LinearLayout, muscle: String) {
        container.removeAllViews()
        val filtered = allItems.filter { it["target_muscle"] == muscle }

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

            addRow("기구", item["equipment"] ?: "")
            addRow("횟수/시간", item["duration_or_reps"] ?: "")

            card.addView(inner)
            container.addView(card)
        }
    }
}