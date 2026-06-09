package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fitquest.R
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.util.Log

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
            btn.setOnClickListener { showLevel(container, index + 1) }
        }

        thread {
            try {
                val url  = URL("https://fitquest25.xyz/api/workout/workouts/")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val response  = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                val items = mutableListOf<Map<String, String>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("category") == "strength") {
                        items.add(mapOf(
                            "name"             to obj.getString("name"),
                            "target_muscle"    to obj.getString("target_muscle"),
                            "equipment"        to obj.getString("equipment"),
                            "duration_or_reps" to obj.getString("duration_or_reps"),
                            "level"            to obj.getInt("level").toString(),
                            "image_url"        to (if (obj.has("image_url") && !obj.isNull("image_url"))
                                obj.getString("image_url") else "")
                        ))
                    }
                }

                requireActivity().runOnUiThread {
                    allItems = items
                    showLevel(container, 1)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun showLevel(container: LinearLayout, level: Int) {
        container.removeAllViews()
        allItems.filter { it["level"] == level.toString() }
            .forEach { container.addView(buildCard(it)) }
    }

    private fun encodeImageUrl(raw: String): String {
        return if (raw.startsWith("http")) raw
        else "https://fitquest25.xyz$raw"
    }

    private fun buildCard(item: Map<String, String>): View {
        val dp = resources.displayMetrics.density

        val card = MaterialCardView(requireContext()).apply {
            radius        = 16f * dp
            cardElevation = 4f  * dp
            setCardBackgroundColor(0xCCFFFFFF.toInt())
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, (8 * dp).toInt()) }
            isClickable = true
            isFocusable = true
        }

        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val p = (16 * dp).toInt()
            setPadding(p, p, p, p)
        }

        val headerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val titleTv = TextView(requireContext()).apply {
            text      = item["name"]
            textSize  = 15f
            setTextColor(0xFF000000.toInt())
            setTypeface(interRegular, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val arrowTv = TextView(requireContext()).apply {
            text      = "▼"
            textSize  = 12f
            setTextColor(0xFF888888.toInt())
        }
        headerRow.addView(titleTv)
        headerRow.addView(arrowTv)
        inner.addView(headerRow)

        inner.addView(View(requireContext()).apply {
            setBackgroundColor(0x22000000)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (1 * dp).toInt()
            ).apply { setMargins(0, (8 * dp).toInt(), 0, (8 * dp).toInt()) }
        })

        fun addRow(label: String, value: String) {
            inner.addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, (4 * dp).toInt())
                addView(TextView(requireContext()).apply {
                    text = label; textSize = 13f
                    setTextColor(0xFF555555.toInt()); typeface = interRegular
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                addView(TextView(requireContext()).apply {
                    text = value; textSize = 13f
                    setTextColor(0xFF000000.toInt()); typeface = interRegular
                    gravity = android.view.Gravity.END
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
            })
        }
        addRow("운동 부위", item["target_muscle"]    ?: "")
        addRow("기구",      item["equipment"]        ?: "")
        addRow("횟수/세트", item["duration_or_reps"] ?: "")

        val imageView = ImageView(requireContext()).apply {
            visibility       = View.GONE
            scaleType        = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            layoutParams     = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (200 * dp).toInt()
            ).apply { setMargins(0, (10 * dp).toInt(), 0, 0) }
        }
        inner.addView(imageView)
        card.addView(inner)

        var expanded = false
        card.setOnClickListener {
            expanded = !expanded
            arrowTv.text = if (expanded) "▲" else "▼"
            if (expanded) {
                imageView.visibility = View.VISIBLE
                val raw = item["image_url"]?.toString().orEmpty()
                if (raw.isNotEmpty()) {
                    val fullUrl = encodeImageUrl(raw)
                    Glide.with(imageView.context)
                        .load(fullUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imageView)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                imageView.visibility = View.GONE
                Glide.with(imageView.context).clear(imageView)
            }
        }

        return card
    }
}