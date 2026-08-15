package com.asfaltosonoro.droidamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WindowFragment : Fragment() {

    // NB: nella pagina reale Webamp si monta dentro un div con id "webamp"
    // (#webamp #main-window, ecc). Prefissiamo anche noi con #webamp per
    // avere una specificita' CSS almeno pari alle regole originali di Webamp,
    // cosi' il nostro !important vince in modo affidabile.
    enum class WindowType(val cssSelectorToShow: String) {
        PLAYER("#webamp #main-window"),
        EQUALIZER("#webamp #equalizer-window"),
        PLAYLIST("#webamp #playlist-window")
    }

    private lateinit var type: WindowType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = requireArguments().getString(ARG_TYPE)!!
        type = WindowType.valueOf(name)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val webView = WebView(requireContext())
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        val assetLoader = AssetLoaderHelper.buildAssetLoader(requireContext())

        // Un solo WebViewClient che fa DUE cose:
        // 1) shouldInterceptRequest: serve gli assets dal dominio virtuale
        //    https://appassets.androidplatform.net/ invece di file://
        // 2) onPageFinished: inietta il CSS che isola una sola finestra
        //    Webamp a tutto schermo (le altre due nascoste)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                val css = """
                    #webamp #main-window, #webamp #equalizer-window, #webamp #playlist-window {
                        display: none !important;
                    }
                    ${type.cssSelectorToShow} {
                        display: block !important;
                        position: fixed !important;
                        top: 0 !important; left: 0 !important;
                        width: 100vw !important; height: 100vh !important;
                        box-sizing: border-box !important;
                        margin: 0 !important;
                        transform: none !important;
                    }
                """.trimIndent().replace("\n", " ")
                view.evaluateJavascript(
                    """
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = "$css";
                        document.head.appendChild(style);
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }

        webView.loadUrl(AssetLoaderHelper.WEBAMP_INDEX_URL)
        return webView
    }

    companion object {
        private const val ARG_TYPE = "type"
        fun newInstance(type: WindowType): WindowFragment {
            val f = WindowFragment()
            f.arguments = Bundle().apply { putString(ARG_TYPE, type.name) }
            return f
        }
    }
}
