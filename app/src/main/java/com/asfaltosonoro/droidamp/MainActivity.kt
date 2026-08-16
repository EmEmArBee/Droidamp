package com.asfaltosonoro.droidamp

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

/**
 * FASE 1 - SCAFFOLD
 *
 * Cosa fa gia':
 *  - Carica Webamp (bundle statico in assets/webamp/) completamente offline.
 *  - Fullscreen immersiva vera: status bar e barra di navigazione nascoste.
 *  - Zoom nativo della WebView (NativeZoomBridge) per riempire lo schermo
 *    il piu' possibile senza deformare e senza toccare il CSS interno di
 *    Webamp (i tentativi precedenti basati su CSS transform/position
 *    rompevano il posizionamento dei controlli interni delle finestre).
 *  - In PORTRAIT: mostra la WebView a schermo intero con lo stack classico
 *    (player + EQ + playlist).
 *  - In LANDSCAPE: ViewPager2 con 4 pagine (player / EQ / playlist /
 *    projectM), ognuna a schermo intero, navigabili a swipe.
 *
 * Cosa NON fa ancora (Fase 2 e 3):
 *  - Bridge audio nativo (playback reale via Media3/ExoPlayer).
 *  - Sincronizzazione dello stato tra le pagine del landscape.
 *  - Caricamento skin da file locale.
 *  - projectM vero (per ora placeholder nero).
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

        NativeZoomBridge.enableZoomSupport(webView)

        val assetLoader = AssetLoaderHelper.buildAssetLoader(this)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Webamp ha link interni (es. "about") che puntano a
                // webamp.org: siamo offline e non li vogliamo aprire
                // dentro l'app, quindi blocchiamo tutto cio' che non e'
                // il nostro contenuto locale.
                return !AssetLoaderHelper.isLocalAssetUrl(request.url.toString())
            }

            override fun onPageFinished(view: WebView, url: String) {
                val js = """
                    (function droidAmpWaitAndZoom(triesLeft) {
                        var el = document.querySelector('#webamp');
                        if (!el) {
                            if (triesLeft > 0) {
                                setTimeout(function () { droidAmpWaitAndZoom(triesLeft - 1); }, 100);
                            }
                            return;
                        }
                        ${NativeZoomBridge.measureAndReportScaleJs("el")}
                    })(50);
                """.trimIndent()
                view.evaluateJavascript(js, null)
            }
        }

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
        }
        return webView
    }
}
