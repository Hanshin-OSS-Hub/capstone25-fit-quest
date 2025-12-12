package com.example.fitquest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.TextView




class RunningFragment : Fragment() {

    private var isRunning = false
    private var seconds = 0.0
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private lateinit var runTimeTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton = view.findViewById<Button>(R.id.running_start)
        runTimeTextView = view.findViewById(R.id.run_time)

        handler = Handler(Looper.getMainLooper())

        runnable = object : Runnable {
            override fun run() {
                seconds += 1.0
                // 시:분:초 형식
                val hrs = (seconds / 3600).toInt()
                val mins = ((seconds % 3600) / 60).toInt()
                val secs = (seconds % 60).toInt()
                runTimeTextView.text = String.format("%02d:%02d:%02d", hrs, mins, secs)

                handler.postDelayed(this, 1000)
            }
        }

        startButton.setOnClickListener {
            if (!isRunning) {
                // 러닝 시작
                isRunning = true
                handler.post(runnable)
                startButton.text = "초기화"
            } else {
                // 러닝 종료 / 초기화
                isRunning = false
                handler.removeCallbacks(runnable)
                startButton.text = "러닝 시작"
                seconds = 0.0
                runTimeTextView.text = "00:00:00"  // 초기화
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 러닝 프래그먼트의 레이아웃 지정
        //return inflater.inflate(R.layout.running, container, false)
        val view = inflater.inflate(R.layout.running, container, false)

        //버튼색깔
        val button1 = view.findViewById<Button>(R.id.easy)
        val button2 = view.findViewById<Button>(R.id.nomal)
        val button3 = view.findViewById<Button>(R.id.hard)
        val buttons = listOf(button1, button2, button3)

        // 버튼별 다른 내용
        val card2Easy = view.findViewById<View>(R.id.card2_easy)
        val card2Nomal = view.findViewById<View>(R.id.card2_nomal)
        val card2Hard = view.findViewById<View>(R.id.card2_hard)
        val cardViews = listOf(card2Easy, card2Nomal, card2Hard)

        // 내용별 색
        val card2 = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card2)

        //버튼 색깔과 내용 교체
        buttons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                // 버튼 선택 상태 변경
                buttons.forEachIndexed { i, b ->
                    b.isSelected = (i == index)
                    b.setTextColor(if (b.isSelected) Color.WHITE else Color.BLACK)
                }

                // card2 내용 교체
                cardViews.forEachIndexed { i, v ->
                    v.visibility = if (i == index) View.VISIBLE else View.GONE
                }

                //카드별 색상
                when(index) {
                    0 -> { // 입문
                        card2.setCardBackgroundColor(Color.parseColor("#d0f6b5"))  // 배경색
                        card2.strokeColor = Color.parseColor("#5ec812")             // 테두리
                    }
                    1 -> { // 초급
                        card2.setCardBackgroundColor(Color.parseColor("#dbe2fa"))
                        card2.strokeColor = Color.parseColor("#093adc")
                    }
                    2 -> { // 중급
                        card2.setCardBackgroundColor(Color.parseColor("#fcdede"))
                        card2.strokeColor = Color.parseColor("#f8bebe")
                    }
                }
            }
        }


// 초기 상태: 첫 번째 버튼 선택
        buttons.forEachIndexed { index, b ->
            b.isSelected = (index == 0)
            b.setTextColor(if (b.isSelected) Color.WHITE else Color.BLACK)
        }

        return view

    }

}