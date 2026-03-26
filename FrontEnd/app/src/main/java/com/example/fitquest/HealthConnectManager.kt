package com.example.fitquest

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

object HealthConnectManager {
    private var client: HealthConnectClient? = null

    fun getClient(context: Context): HealthConnectClient {
        if (client == null) {
            client = HealthConnectClient.getOrCreate(context.applicationContext)
        }
        return client!!
    }
}