package com.example

import android.app.Application
import android.util.Log
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Purchases.debugLogsEnabled = false
            Purchases.configure(
                PurchasesConfiguration.Builder(
                    this,
                    "goog_LWFrTvMNWEuEHdQjinisyZfgyUI"
                ).build()
            )
            Log.d("MainApplication", "RevenueCat initialized successfully")
        } catch (e: Exception) {
            Log.w("MainApplication", "RevenueCat initialization skipped or unavailable on this device: ${e.message}")
        }
    }
}
