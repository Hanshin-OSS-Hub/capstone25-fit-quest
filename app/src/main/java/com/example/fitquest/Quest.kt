package com.example.fitquest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class QuestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // toggleGroup을 XML에서 추가해야 함
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup) // ★
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
}
