package com.fiestadecartas.app.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.fiestadecartas.app.data.Prefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Replace these three IDs with the ones from your AdMob account before publishing.
 * The values below are Google's official TEST ids and never generate revenue.
 */
object AdsConfig {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    var gamesSinceAd = 0
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdsConfig.BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object Interstitial {
    private var ad: InterstitialAd? = null
    private var loading = false

    fun preload(context: Context) {
        if (ad != null || loading) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            AdsConfig.INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    ad = interstitialAd
                    loading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    ad = null
                    loading = false
                }
            }
        )
    }

    /** Shows an ad every third finished match, never interrupting active play. */
    fun maybeShow(activity: Activity?, onDone: () -> Unit = {}) {
        if (activity == null || Prefs.adsConsent == false) { onDone(); return }
        AdsConfig.gamesSinceAd += 1
        if (AdsConfig.gamesSinceAd < 3) { onDone(); return }
        AdsConfig.gamesSinceAd = 0
        val current = ad
        if (current != null) {
            ad = null
            current.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { preload(activity); onDone() }
                override fun onAdFailedToShowFullScreenContent(e: AdError) { preload(activity); onDone() }
            }
            current.show(activity)
        } else {
            preload(activity)
            onDone()
        }
    }
}
