package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fitquest.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import android.util.Log

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
                val url  = URL("https://fitquest25.xyz/api/workout/workouts/")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val response  = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                val items = mutableListOf<Map<String, String>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("category") == "stretching") {
                        items.add(mapOf(
                            "name"             to obj.getString("name"),
                            "target_muscle"    to obj.getString("target_muscle"),
                            "equipment"        to obj.getString("equipment"),
                            "duration_or_reps" to obj.getString("duration_or_reps"),
                            "image_url"        to (if (obj.has("image_url") && !obj.isNull("image_url"))
                                obj.getString("image_url") else "")
                        ))
                    }
                }

                requireActivity().runOnUiThread {
                    allItems = items

                    val muscles = items.map { it["target_muscle"] ?: "" }
                        .distinct().filter { it.isNotBlank() }

                    muscles.forEach { muscle ->
                        chipGroup.addView(Chip(requireContext()).apply {
                            text        = muscle
                            isCheckable = true
                            typeface    = interRegular
                        })
                    }

                    if (chipGroup.childCount > 0) {
                        (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                        showMuscle(container, muscles.firstOrNull() ?: "")
                    }

                    chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                        val selected = checkedIds.firstOrNull()
                            ?.let { group.findViewById<Chip>(it) }
                        showMuscle(container, selected?.text?.toString() ?: "")
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun showMuscle(container: LinearLayout, muscle: String) {
        container.removeAllViews()
        allItems.filter { it["target_muscle"] == muscle }
            .forEach { container.addView(buildCard(it)) }
    }

    private fun encodeImageUrl(raw: String): String {
        return if (raw.startsWith("http")) raw
        else "https://fitquest25.xyz$raw"
    }

    private fun buildCard(item: Map<String, String>): View {
        Log.d("ImageURL", "raw = ${item["image_url"]}")
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
        addRow("기구",      item["equipment"]        ?: "")
        addRow("횟수/시간", item["duration_or_reps"] ?: "")

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
                Log.d("ImageURL", "raw = $raw")
                val fullUrl = encodeImageUrl(raw)
                Log.d("ImageURL", "encoded = $fullUrl")
                if (raw.isNotEmpty()) {
                    val fullUrl = encodeImageUrl(raw)
                    Glide.with(imageView.context)
                        .load(fullUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                            override fun onLoadFailed(
                                e: com.bumptech.glide.load.engine.GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("ImageURL", "로드 실패: $fullUrl")
                                Log.e("ImageURL", "에러: ${e?.message}")
                                return false
                            }
                            override fun onResourceReady(
                                resource: android.graphics.drawable.Drawable,
                                model: Any,
                                target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                dataSource: com.bumptech.glide.load.DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d("ImageURL", "로드 성공: $fullUrl")
                                return false
                            }
                        })
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