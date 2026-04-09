package com.example.fitquest.Fragment_list

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray

data class RankingUser(
    val name: String,
    val title: String,
    val level: Int,
    val exp: Int,
    val point: Int
)

class RankingAdapter(
    private var users: List<RankingUser>,
    private val typeface: Typeface?
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rankText)
        val nameText: TextView = itemView.findViewById(R.id.userName)
        val levelText: TextView = itemView.findViewById(R.id.userLevel)
        val titleText: TextView = itemView.findViewById(R.id.userTitle)
        val scoreText: TextView = itemView.findViewById(R.id.userScore)
        val card: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        applyFont(view, typeface)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val user = users[position]
        val rank = position + 1

        holder.nameText.text  = user.name
        holder.levelText.text = "LV ${user.level}"
        // ★ 칭호는 시스템 폰트로 강제 (한글 깨짐 방지)
        holder.titleText.typeface = Typeface.DEFAULT
        holder.titleText.text = if (user.title.isEmpty() || user.title == "null") "[칭호 없음]" else "[${user.title}]"
        holder.scoreText.text = "${user.exp} XP"

        when (rank) {
            1 -> {
                holder.rankText.text = "🥇"
                holder.rankText.textSize = 24f
                holder.card.setCardBackgroundColor(Color.parseColor("#FFF8E1"))
            }
            2 -> {
                holder.rankText.text = "🥈"
                holder.rankText.textSize = 24f
                holder.card.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }
            3 -> {
                holder.rankText.text = "🥉"
                holder.rankText.textSize = 24f
                holder.card.setCardBackgroundColor(Color.parseColor("#FBE9E7"))
            }
            else -> {
                holder.rankText.text = rank.toString()
                holder.rankText.textSize = 18f
                holder.card.setCardBackgroundColor(Color.parseColor("#CCffffff"))
                holder.rankText.setTextColor(Color.parseColor("#000000"))
            }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<RankingUser>) {
        users = newUsers.sortedWith(
            compareByDescending<RankingUser> { it.level }
                .thenByDescending { it.exp }
                .thenByDescending { it.point }
        )
        notifyDataSetChanged()
    }

    // ★ userTitle(R.id.userTitle)은 폰트 적용 제외
    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyFont(view.getChildAt(i), typeface)
        } else if (view is TextView) {
            if (view.id == R.id.userTitle) return   // 칭호는 시스템 폰트 유지
            view.typeface = typeface
        } else if (view is MaterialButton) {
            view.typeface = typeface
        }
    }
}

class RankingFragment : Fragment() {

    private lateinit var adapter: RankingAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 30_000L

    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchRanking()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        val recyclerView = view.findViewById<RecyclerView>(R.id.ranking_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = RankingAdapter(emptyList(), interRegular)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun fetchRanking() {
        val sharedPref = requireActivity().getSharedPreferences("FitQuestPrefs", android.content.Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", "") ?: ""

        if (accessToken.isEmpty()) return

        val request = object : com.android.volley.toolbox.StringRequest(
            Method.GET,
            "https://fitquest25.xyz/api/auth/ranking/",
            { response ->
                android.util.Log.i("RankingFragment", "랭킹 응답: $response")
                try {
                    val jsonArray = JSONArray(response)
                    val users = mutableListOf<RankingUser>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        users.add(RankingUser(
                            name  = obj.optString("nickname", "알 수 없음"),
                            title = obj.optString("current_title", ""),
                            level = obj.optInt("level", 1),
                            exp   = obj.optInt("exp", 0),
                            point = obj.optInt("point", 0)
                        ))
                    }
                    adapter.updateData(users)
                } catch (e: Exception) {
                    android.util.Log.e("RankingFragment", "랭킹 파싱 오류: ${e.message}")
                }
            },
            { error ->
                android.util.Log.e("RankingFragment", "랭킹 실패 코드: ${error.networkResponse?.statusCode ?: -1}")
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): com.android.volley.Response<String> {
                return try {
                    com.android.volley.Response.success(
                        String(response.data, Charsets.UTF_8),
                        com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)
                    )
                } catch (e: Exception) {
                    com.android.volley.Response.error(com.android.volley.ParseError(e))
                }
            }
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $accessToken",
                    "Content-Type" to "application/json"
                )
            }
        }
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request)
    }

    fun updateRankingData(users: List<RankingUser>) {
        if (::adapter.isInitialized) adapter.updateData(users)
    }

    private fun applyFont(view: View, typeface: Typeface?) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyFont(view.getChildAt(i), typeface)
        } else if (view is TextView) {
            view.typeface = typeface
        } else if (view is MaterialButton) {
            view.typeface = typeface
        }
    }
}