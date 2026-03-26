package com.example.fitquest.Fragment_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import com.example.fitquest.R

import android.graphics.Typeface
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton

class ExerciseFragment : Fragment(R.layout.exercise) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stretchButton = view.findViewById<Button>(R.id.btn_exerxise2)

        // Inter Regular 폰트 로드
        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)

        // Fragment 내부 모든 TextView + MaterialButton에 폰트 적용
        applyFont(view, interRegular)

        stretchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, StretchingFragment())
                .addToBackStack(null) // 뒤로가기
                .commit()
        }

        val lightexerButton = view.findViewById<Button>(R.id.btn_exerxise1)

        lightexerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, lightexerciseFragment())
                .addToBackStack(null) // 뒤로가기
                .commit()
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