package com.asfaltosonoro.droidamp

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

/**
 * FASE 1 - SCAFFOLD
 *
 * Cosa fa già:
 *  - Carica Webamp (bundle statico in assets/webamp/) completamente offline.
 *  - Fullscreen immersiva vera: status bar e barra di navigazione nascoste,
 *    richiamabili con uno swipe dal bordo (tornano nascoste da sole dopo poco).
 *  - In PORTRAIT: mostra la WebView a schermo intero con lo stack classico
 *    (player + EQ + playlist) così com'è di default in Webamp.
 *  - In LANDSCAPE: passa a un ViewPager2 con 4 pagine (player / EQ / playlist /
 *    projectM), ognuna a schermo intero, navigabili a swipe.
 *
 * Cosa NON fa ancora (Fase 2 e 3, arriveranno dopo):
 *  - Bridge audio nativo (playback reale via Media3/ExoPlayer) — per ora
 *    Webamp gira "finto", senza audio vero collegato.
 *  - Sincronizzazione dello stato (traccia in play, posizione, EQ) tra le
 *    pagine del landscape: al momento sono indipendenti.
 *  - Caricamento skin da file locale (verrà insieme al bridge audio, stesso
 *    meccanismo di "ponte" JS↔Kotlin).
 *  - projectM vero (per ora la 4ª pagina è un placeholder nero).
 *
 * TODO Fase 2: creare un DroidAmpAudioBridge (JS interface) iniettato in
 * tutte le WebView, appoggiato a un unico AudioPlayerService in background,
 * cosi' tutte le pagine restano sincronizzate sullo stesso stato. Stesso
 * bridge esporrà anche il caricamento skin da file locale.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.root_container)
        renderForOrientation(resources.configuration.orientation)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        renderForOrientation(newConfig.orientation)
    }

    private fun renderForOrientation(orientation: Int) {
        container.removeAllViews()
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLandscapeSwipe()
        } else {
            showPortraitSingle()
        }
    }

    private fun showPortraitSingle() {
        val webView = buildWebView()
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        AssetLoaderHelper.attach(this, webView)
        webView.loadUrl(AssetLoaderHelper.WEBAMP_INDEX_URL)
        container.addView(webView)
    }

    private fun showLandscapeSwipe() {
        val viewPager = ViewPager2(this)
        viewPager.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        viewPager.adapter = LandscapePagerAdapter(this)
        container.addView(viewPager)
    }

    private fun buildWebView(): WebView {
        val webView = WebView(this)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        return webView
    }
}
