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
    // (#webamp #main-window, ecc).
    enum class WindowType(val cssId: String) {
        PLAYER("#main-window"),
        EQUALIZER("#equalizer-window"),
        PLAYLIST("#playlist-window")
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

        // FIX GLITCH A GRIGLIA: le WebView dentro un ViewPager2 con
        // accelerazione hardware attiva a volte "sporcano" il buffer
        // grafico quando vengono riciclate durante lo swipe, producendo
        // un effetto a griglia/mosaico ripetuto (bug noto della
        // combinazione WebView + RecyclerView/ViewPager2 su alcune GPU
        // Android). Forzare il rendering software su queste WebView lo
        // risolve; e' un filo piu' lento ma qui non e' un problema dato
        // che sono schermate statiche, non scrolling continuo.
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

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
        // 2) onPageFinished: nasconde le altre 2 finestre, mostra la nostra
        //    alla sua dimensione naturale, e chiama droidampFitToScreen()
        //    (definita in index.html) per scalarla/centrarla SENZA
        //    deformarla, riempiendo lo schermo il piu' possibile.
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                val js = """
                    (function waitForWebamp(tries) {
                        var el = document.querySelector('${type.cssId}');
                        if (!el) {
                            if (tries > 0) { setTimeout(function(){ waitForWebamp(tries - 1); }, 100); }
                            return;
                        }
                        var style = document.createElement('style');
                        style.innerHTML = "#main-window, #equalizer-window, #playlist-window { display: none !important; } ${type.cssId} { display: block !important; }";
                        document.head.appendChild(style);
                        window.__droidampFitSelector = '${type.cssId}';
                        if (window.droidampFitToScreen) {
                            window.droidampFitToScreen('${type.cssId}');
                        }
                    })(30);
                """.trimIndent()
                view.evaluateJavascript(js, null)
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
