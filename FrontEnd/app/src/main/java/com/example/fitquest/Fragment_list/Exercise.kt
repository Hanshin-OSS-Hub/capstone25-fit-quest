package com.example.fitquest.Fragment_list

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.fitquest.R
import com.google.android.material.button.MaterialButton

class ExerciseFragment : Fragment(R.layout.exercise) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interRegular: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        applyFont(view, interRegular)

        view.findViewById<Button>(R.id.btn_stretching).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.exercise_fragment_container, StretchingFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<Button>(R.id.btn_strength).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.exercise_fragment_container, StrengthFragment())
                .addToBackStack(null)
                .commit()
        }
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