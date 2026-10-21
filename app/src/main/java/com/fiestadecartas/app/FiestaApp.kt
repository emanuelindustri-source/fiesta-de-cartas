package com.fiestadecartas.app

import android.app.Application
import com.fiestadecartas.app.data.BotSettings
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.HoneygainSupport
import com.fiestadecartas.app.data.Profile
import com.fiestadecartas.app.ui.Feedback
import com.google.android.gms.ads.MobileAds

class FiestaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Profile.init(this)
        GameConfig.init(this)
        BotSettings.init(this)
        Feedback.init(this)
        HoneygainSupport.init(this)
        runCatching { MobileAds.initialize(this) }
    }
}
