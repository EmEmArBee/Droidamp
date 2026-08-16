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
        // grafico quando vengono riciclate durante lo swipe. Forzare il
        // rendering software su queste WebView lo risolve.
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            // NIENTE setSupportZoom/builtInZoomControls: altrimenti il
            // pinch entra in conflitto con lo swipe tra le pagine e col
            // trascinamento degli slider dell'equalizzatore. Lo zoom e'
            // fisso, calcolato via CSS "zoom" (droidampFitToScreen).
        }

        val assetLoader = AssetLoaderHelper.buildAssetLoader(requireContext())

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
                return !AssetLoaderHelper.isLocalAssetUrl(request.url.toString())
            }

            override fun onPageFinished(view: WebView, url: String) {
                val js = """
                    (function droidAmpWaitIsolateAndFit(triesLeft) {
                        var el = document.querySelector('${type.cssId}');
                        if (!el) {
                            if (triesLeft > 0) {
                                setTimeout(function () { droidAmpWaitIsolateAndFit(triesLeft - 1); }, 100);
                            }
                            return;
                        }
                        var style = document.createElement('style');
                        style.innerHTML = "#main-window, #equalizer-window, #playlist-window { display: none !important; } ${type.cssId} { display: block !important; }";
                        document.head.appendChild(style);
                        if (window.droidampFitToScreen) {
                            window.droidampFitToScreen('${type.cssId}');
                        }
                    })(50);
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
