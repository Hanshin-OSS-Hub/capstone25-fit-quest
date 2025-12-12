package com.example.fitquest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.content.Intent

class lightexerciseFragment : Fragment(R.layout.lightexercise) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.lightexercise, container, false)

        val goExerciseButton = view.findViewById<Button>(R.id.go_exercise)

        goExerciseButton.setOnClickListener {
            val containerId = container?.id ?: 0
            parentFragmentManager.beginTransaction()
                .replace(containerId, ExerciseFragment()) // ExerciseFragment로 이동
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}