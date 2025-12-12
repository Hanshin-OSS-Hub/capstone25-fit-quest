package com.example.fitquest.Fragment_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.fitquest.R
import com.example.fitquest.Stretching_list.Neck_and_shoulders
import com.example.fitquest.Stretching_list.Shoulder
import com.example.fitquest.Stretching_list.Upperbody_abdomen
import com.example.fitquest.Stretching_list.Arm
import com.example.fitquest.Stretching_list.Pelvis_waist
import com.example.fitquest.Stretching_list.Legs_and_calfs
import com.example.fitquest.Stretching_list.Foot_ankle
import com.example.fitquest.Stretching_list.Hand_wrist
import com.google.android.material.card.MaterialCardView

class StretchingFragment : Fragment(R.layout.stretching) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.stretching, container, false)

        // 버튼 연결
        val goExerButton = view.findViewById<Button>(R.id.go_exercise)

        // 뒤로가기 기능
        goExerButton.setOnClickListener {
            val containerId = container?.id ?: 0
            replaceFragment(containerId, ExerciseFragment())
        }

        val card1 = view.findViewById<MaterialCardView>(R.id.card1)
        val card2 = view.findViewById<MaterialCardView>(R.id.card2)
        val card3 = view.findViewById<MaterialCardView>(R.id.card3)
        val card4 = view.findViewById<MaterialCardView>(R.id.card4)
        val card5 = view.findViewById<MaterialCardView>(R.id.card5)
        val card6 = view.findViewById<MaterialCardView>(R.id.card6)
        val card7 = view.findViewById<MaterialCardView>(R.id.card7)
        val card8 = view.findViewById<MaterialCardView>(R.id.card8)

        val containerId = container?.id ?: 0

        // 1번 카드
        card1.setOnClickListener {
            replaceFragment(containerId, Neck_and_shoulders())
        }

        // 2번 카드
        card2.setOnClickListener {
            replaceFragment(containerId, Shoulder())
        }

        // 3번 카드
        card3.setOnClickListener {
            replaceFragment(containerId, Upperbody_abdomen())
        }

        // 4번 카드
        card4.setOnClickListener {
            replaceFragment(containerId, Arm())
        }

        // 5번 카드
        card5.setOnClickListener {
            replaceFragment(containerId, Pelvis_waist())
        }

        // 6번 카드
        card6.setOnClickListener {
            replaceFragment(containerId, Legs_and_calfs())
        }

        // 7번 카드
        card7.setOnClickListener {
            replaceFragment(containerId, Foot_ankle())
        }

        // 8번 카드
        card8.setOnClickListener {
            replaceFragment(containerId, Hand_wrist())
        }

        return view
    }

    private fun replaceFragment(containerId: Int, fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}