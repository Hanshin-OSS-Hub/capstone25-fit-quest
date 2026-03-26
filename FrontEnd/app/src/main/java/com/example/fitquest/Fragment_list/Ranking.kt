package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
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
import androidx.constraintlayout.widget.ConstraintLayout

// 랭킹 사용자 데이터 클래스
data class RankingUser(
    val name: String,
    val score: Int
)

// RecyclerView Adapter
class RankingAdapter(private var users: List<RankingUser>, private val typeface: Typeface?) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rankText)
        val nameText: TextView = itemView.findViewById(R.id.userName)
        val scoreText: TextView = itemView.findViewById(R.id.userScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        // 글꼴 적용
        applyFont(view, typeface)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val user = users[position]
        holder.rankText.text = (position + 1).toString()
        holder.nameText.text = user.name
        holder.scoreText.text = "${user.score}XP"
    }

    override fun getItemCount(): Int = users.size

    // ← 수정: XP 높은 순으로 자동 정렬
    fun updateData(newUsers: List<RankingUser>) {
        users = newUsers.sortedByDescending { it.score }
        notifyDataSetChanged()
    }

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

// RankingFragment
class RankingFragment : Fragment() {

    private lateinit var adapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ranking, container, false)

        // Inter Regular 폰트 로드
        val interRegular = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)

        // ← 수정: 샘플 데이터 (정렬되지 않은 상태로 추가)
        val sampleUsers = listOf(
            RankingUser("홍길동1", 120),
            RankingUser("홍길동2", 110),
            RankingUser("홍길동3", 100),
            RankingUser("홍길동4", 90),
            RankingUser("홍길동5", 2000)
        )

        //RecyclerView 세팅
        val recyclerView = RecyclerView(requireContext())
        recyclerView.id = View.generateViewId()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ← 수정: 어댑터에 전달하면 자동으로 정렬됨
        adapter = RankingAdapter(sampleUsers.sortedByDescending { it.score }, interRegular)
        recyclerView.adapter = adapter

        //ConstraintLayout 참조
        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.ranking)
        val todayText = view.findViewById<TextView>(R.id.rank2)

        //RecyclerView에 ConstraintLayout 파라미터 설정
        val layoutParams = ConstraintLayout.LayoutParams(
            0, // width: 0dp (match_constraint)
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            //제약 조건 설정
            topToBottom = R.id.rank2
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin = 32 // 위쪽 여백
            marginStart = 16 // 좌측 여백
            marginEnd = 16 // 우측 여백
        }
        recyclerView.layoutParams = layoutParams

        //RecyclerView 추가
        constraintLayout.addView(recyclerView)

        // 글꼴 전체 적용 (TextView + MaterialButton)
        applyFont(view, interRegular)

        //loadRankingData()

        return view
    }

    private fun loadRankingData() {
        // val url = "https://fitquest25.xyz/api/ranking/"
        // API에서 데이터를 받아올 때:
        // val realUsers = listOf(...)
        // adapter.updateData(realUsers) // ← 자동으로 정렬됨
    }

    // ← 수정: 외부에서 데이터 업데이트 시 자동 정렬
    fun updateRankingData(users: List<RankingUser>) {
        if (::adapter.isInitialized) {
            adapter.updateData(users) // updateData에서 자동 정렬됨
        }
    }

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