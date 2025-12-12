package com.example.fitquest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.content.Intent
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

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