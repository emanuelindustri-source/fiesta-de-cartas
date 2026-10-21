package com.fiestadecartas.app

import android.app.Application
import com.fiestadecartas.app.data.GameConfig
import com.fiestadecartas.app.data.Profile
import com.google.android.gms.ads.MobileAds

class FiestaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Profile.init(this)
        GameConfig.init(this)
        runCatching { MobileAds.initialize(this) }
    }
}
