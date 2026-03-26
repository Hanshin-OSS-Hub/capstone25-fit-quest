package com.example.fitquest.Activitiy_list

import android.app.Application
import com.example.fitquest.R
import com.kakao.sdk.common.KakaoSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
    }

}