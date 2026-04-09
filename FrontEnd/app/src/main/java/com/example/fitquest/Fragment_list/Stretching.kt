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
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class StretchingFragment : Fragment(R.layout.fragment_stretching) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<LinearLayout>(R.id.stretching_list_container)
        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)

        thread {
            try {
                val url = URL("https://fitquest25.xyz/api/workout/workouts/")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val response = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                val stretchingItems = mutableListOf<Triple<String, String, Pair<String, String>>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("category") == "stretching") {
                        stretchingItems.add(
                            Triple(
                                obj.getString("name"),
                                obj.getString("target_muscle"),
                                Pair(obj.getString("equipment"), obj.getString("duration_or_reps"))
                            )
                        )
                    }
                }

                requireActivity().runOnUiThread {
                    for ((name, muscle, info) in stretchingItems) {
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

                        // 운동 이름
                        inner.addView(TextView(requireContext()).apply {
                            text = name
                            textSize = 15f
                            setTextColor(0xFF000000.toInt())
                            setTypeface(interRegular, Typeface.BOLD)
                            val pb = (6 * resources.displayMetrics.density).toInt()
                            setPadding(0, 0, 0, pb)
                        })

                        // 정보 행 추가 함수
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

                        addRow("운동 부위", muscle)
                        addRow("기구", info.first)
                        addRow("횟수/시간", info.second)

                        card.addView(inner)
                        container.addView(card)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}