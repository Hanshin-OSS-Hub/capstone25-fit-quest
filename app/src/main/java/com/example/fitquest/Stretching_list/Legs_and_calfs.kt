package com.example.fitquest.Stretching_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.example.fitquest.R
import android.widget.Button
import com.example.fitquest.Fragment_list.StretchingFragment

class Legs_and_calfs : Fragment(R.layout.fragment_legs_and_calfs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<Button>(R.id.go_stretching)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, StretchingFragment())
                .commit()
        }
    }
}