package com.example.fitquest.Fragment_list

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

import android.graphics.Typeface
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class QuestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inter Regular 폰트 로드
        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)

        // Fragment 내부 모든 TextView + MaterialButton에 폰트 적용
        applyFont(view, interRegular)

        // toggleGroup을 XML에서 추가해야 함
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val dailyButton = view.findViewById<MaterialButton>(R.id.dailyButton)
        val weeklyButton = view.findViewById<MaterialButton>(R.id.weeklyButton)
        val monthlyButton = view.findViewById<MaterialButton>(R.id.monthlyButton)

        val buttons = listOf(dailyButton, weeklyButton, monthlyButton)

        //일일, 배경/글씨 색상 초기화
        toggleGroup.check(dailyButton.id) // ★
        buttons.forEach { btn ->
            btn.setBackgroundColor(Color.parseColor("#CCCCCC")) // 회색 배경
            btn.setTextColor(Color.BLACK)
        }
        dailyButton.setBackgroundColor(Color.WHITE) // 선택된 버튼 흰색
        dailyButton.setTextColor(Color.BLACK)

        // 선택 변화 리스너
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener // 체크 해제 시 무시
            buttons.forEach { btn ->
                btn.setBackgroundColor(Color.parseColor("#CCCCCC")) // 회색 배경
                btn.setTextColor(Color.BLACK)
            }
            val selectedButton = view.findViewById<MaterialButton>(checkedId)
            selectedButton.setBackgroundColor(Color.WHITE) // 선택된 버튼 흰색
            selectedButton.setTextColor(Color.BLACK)
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
