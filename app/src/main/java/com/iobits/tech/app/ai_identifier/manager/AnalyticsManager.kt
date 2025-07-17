package com.iobits.tech.app.ai_identifier.manager

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {
    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(eventName: String) {
        firebaseAnalytics.logEvent(eventName, null)
    }

}