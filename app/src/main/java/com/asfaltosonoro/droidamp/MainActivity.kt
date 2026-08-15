package com.asfaltosonoro.droidamp

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

/**
 * FASE 1 - SCAFFOLD
 *
 * Cosa fa già:
 *  - Carica Webamp (bundle statico in assets/webamp/) completamente offline.
 *  - In PORTRAIT: mostra la WebView a schermo intero con lo stack classico
 *    (player + EQ + playlist) così com'è di default in Webamp.
 *  - In LANDSCAPE: passa a un ViewPager2 con 3 pagine (player / EQ / playlist),
 *    ognuna una WebView separata che carica la stessa app ma con CSS iniettato
 *    per mostrare solo quella finestra a tutto schermo.
 *
 * Cosa NON fa ancora (Fase 2 e 3, arriveranno dopo):
 *  - Bridge audio nativo (playback reale via Media3/ExoPlayer) — per ora
 *    Webamp gira "finto", senza audio vero collegato.
 *  - Sincronizzazione dello stato (traccia in play, posizione, EQ) tra le
 *    3 WebView del landscape: al momento sono indipendenti.
 *  - projectM (visualizzazioni MilkDrop-style).
 *
 * TODO Fase 2: creare un DroidAmpAudioBridge (JS interface) iniettato in
 * tutte le WebView, appoggiato a un unico AudioPlayerService in background,
 * cosi' tutte le pagine restano sincronizzate sullo stesso stato.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.root_container)
        renderForOrientation(resources.configuration.orientation)
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
        // Nessuna iniezione CSS: usa lo stack verticale di default di Webamp
        // (player in cima, EQ sotto, playlist sotto ancora).
        webView.loadUrl("file:///android_asset/webamp/index.html")
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
