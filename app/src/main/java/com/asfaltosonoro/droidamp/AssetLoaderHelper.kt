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
 * Google e' servire gli assets su un dominio virtuale HTTPS locale
 * (https://appassets.androidplatform.net/...) tramite WebViewAssetLoader:
 * dal punto di vista della pagina sembra un sito HTTPS vero, ma in realta'
 * legge dagli assets dell'APK, offline al 100%.
 *
 * Usare SEMPRE questo helper per caricare pagine da assets/webamp/, sia in
 * MainActivity (portrait) sia in WindowFragment/ProjectMFragment (landscape).
 */
object AssetLoaderHelper {

    const val WEBAMP_INDEX_URL =
        "https://appassets.androidplatform.net/assets/webamp/index.html"

    private const val LOCAL_DOMAIN = "https://appassets.androidplatform.net/"

    /** True se l'URL e' servito dal nostro WebViewAssetLoader (contenuto locale). */
    fun isLocalAssetUrl(url: String): Boolean = url.startsWith(LOCAL_DOMAIN)

    fun buildAssetLoader(context: Context): WebViewAssetLoader {
        return WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                WebViewAssetLoader.AssetsPathHandler(context)
            )
            .build()
    }

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
}
