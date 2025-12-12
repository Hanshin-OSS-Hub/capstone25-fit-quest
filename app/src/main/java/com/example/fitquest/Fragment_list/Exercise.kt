package com.example.fitquest.Fragment_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import com.example.fitquest.R

class ExerciseFragment : Fragment(R.layout.exercise) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stretchButton = view.findViewById<Button>(R.id.exerxise2)

        stretchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, StretchingFragment())
                .addToBackStack(null) // 뒤로가기
                .commit()
        }

        val lightexerButton = view.findViewById<Button>(R.id.exerxise1)

        lightexerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, lightexerciseFragment())
                .addToBackStack(null) // 뒤로가기
                .commit()
        }
    }
}