package com.asfaltosonoro.droidamp

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader

/**
 * Webamp usa fetch() per caricare il file .wsz della skin. La WebView Android
 * ha problemi noti con fetch() su file://. La soluzione raccomandata da
 * Google è servire gli assets su un dominio virtuale HTTPS locale
 * (https://appassets.androidplatform.net/...) tramite WebViewAssetLoader:
 * dal punto di vista della pagina sembra un sito HTTPS vero, ma in realtà
 * legge dagli assets dell'APK, offline al 100%.
 *
 * Usare SEMPRE questo helper per caricare pagine da assets/webamp/, sia in
 * MainActivity (portrait) sia in WindowFragment/ProjectMFragment (landscape),
 * così tutte le WebView si comportano allo stesso modo.
 */
object AssetLoaderHelper {

    const val WEBAMP_INDEX_URL =
        "https://appassets.androidplatform.net/assets/webamp/index.html"

    fun buildAssetLoader(context: Context): WebViewAssetLoader {
        return WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                WebViewAssetLoader.AssetsPathHandler(context)
            )
            .build()
    }

    /** Uso semplice: nessuna logica aggiuntiva in onPageFinished (caso portrait). */
    fun attach(context: Context, webView: WebView) {
        val assetLoader = buildAssetLoader(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
    }

    /**
     * Come attach(), ma in piu' fa il polling dell'elemento indicato da
     * [targetSelector] (es. "#webamp" per tutto lo stack in portrait) e,
     * appena appare, chiama window.droidampFitToScreen() definita in
     * index.html. Il polling serve perche' Webamp si disegna in modo
     * asincrono (fetch della skin + parsing), quindi l'elemento potrebbe
     * non esistere ancora quando la pagina "finisce di caricare" in senso
     * stretto (onPageFinished).
     *
     * [extraJs] e' codice JS opzionale eseguito PRIMA del fit (es. per
     * nascondere le altre finestre in landscape, vedi WindowFragment.kt).
     */
    fun attachWithFitToScreen(
        context: Context,
        webView: WebView,
        targetSelector: String,
        extraJs: String = ""
    ) {
        val assetLoader = buildAssetLoader(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                val js = """
                    (function droidAmpWaitAndFit(triesLeft) {
                        var el = document.querySelector('$targetSelector');
                        if (!el) {
                            if (triesLeft > 0) {
                                setTimeout(function () { droidAmpWaitAndFit(triesLeft - 1); }, 100);
                            }
                            return;
                        }
                        $extraJs
                        window.__droidampFitSelector = '$targetSelector';
                        if (window.droidampFitToScreen) {
                            window.droidampFitToScreen('$targetSelector');
                        }
                    })(50);
                """.trimIndent()
                view.evaluateJavascript(js, null)
            }
        }
    }
}
