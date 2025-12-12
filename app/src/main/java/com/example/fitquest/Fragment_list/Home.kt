package com.example.fitquest.Fragment_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.widget.Button
import android.os.Handler
import android.os.Looper

import android.widget.TextView
import com.example.fitquest.R
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    // 문장 움직이기
    private lateinit var fightingText: TextView
    private val quotes = listOf(
        "오늘도 한 걸음 더 나아갔어요! 🎉",
        "꾸준함이 만드는 변화, 당신이 증명하고 있어요 💪",
        "어제보다 나은 오늘을 만들고 있어요 ✨",
        "작은 습관이 큰 변화를 만듭니다 🌱",
        "당신의 노력이 결실을 맺고 있어요 🌟",
        "매일매일 성장하는 당신, 정말 멋져요! 🚀",
        "포기하지 않는 당신이 진정한 챔피언이에요 🏆"
    )
    private var currentIndex = 0
    private lateinit var quoteRunnable: Runnable
    private val quoteInterval = 5000L //

    //시계표시 - 아시아, 서울 기준 실시간
    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    // 문장 움직이기2
    private val updateTimeTask = object : Runnable {
        override fun run() {
            val currentTime = timeFormat.format(System.currentTimeMillis())
            timeTextView.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }

    // 시계 및 문장 시간 설정
    override fun onResume() {
        super.onResume()
        handler.post(updateTimeTask)
        handler.post(quoteRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeTask)
        handler.removeCallbacks(quoteRunnable)
    }


    // 로그인 화면 이동 버튼
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeTextView = view.findViewById(R.id.timeTextView)

        fightingText = view.findViewById(R.id.fighting_text)



        val goMainButton = view.findViewById<Button>(R.id.gologin)

        goMainButton.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }

        // 문장 움직이기3
        quoteRunnable = object : Runnable {
            override fun run() {
                fightingText.text = quotes[currentIndex]
                currentIndex = (currentIndex + 1) % quotes.size
                handler.postDelayed(this, quoteInterval)
            }
        }
    }
}
