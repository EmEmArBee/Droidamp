package com.asfaltosonoro.droidamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class WindowFragment : Fragment() {

    enum class WindowType(val cssSelectorToShow: String) {
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
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        // Dopo che la pagina ha finito di caricare, iniettiamo CSS che:
        // 1) nasconde tutte le finestre Webamp
        // 2) mostra SOLO quella di questo fragment
        // 3) la fa espandere a riempire tutto lo schermo
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val css = """
                    #main-window, #equalizer-window, #playlist-window {
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

        webView.loadUrl("file:///android_asset/webamp/index.html")
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
