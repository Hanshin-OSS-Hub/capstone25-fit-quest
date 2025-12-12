package com.example.fitquest.Stretching_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.fitquest.R
import com.example.fitquest.Fragment_list.StretchingFragment

class Arm : Fragment(R.layout.fragment_arm) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 버튼
        val backButton = view.findViewById<Button>(R.id.go_stretching)

        backButton.setOnClickListener {
            // StretchingFragment로 교체 - 이하 동일
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, StretchingFragment())
                .commit()
        }
    }
}