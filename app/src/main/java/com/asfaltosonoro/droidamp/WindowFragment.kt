package com.asfaltosonoro.droidamp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment

class WindowFragment : Fragment() {

    // NB: nella pagina reale Webamp si monta dentro un div con id "webamp"
    // (#webamp #main-window, ecc.), ma essendo id univoci nel documento
    // possiamo selezionarli direttamente.
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
        // risolve.
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Prima di chiamare il fit-to-screen (dentro attachWithFitToScreen),
        // nascondiamo le altre 2 finestre e mostriamo la nostra.
        val hideOthersJs = """
            var style = document.createElement('style');
            style.innerHTML = "#main-window, #equalizer-window, #playlist-window { display: none !important; } ${type.cssId} { display: block !important; }";
            document.head.appendChild(style);
        """.trimIndent()

        AssetLoaderHelper.attachWithFitToScreen(
            requireContext(),
            webView,
            type.cssId,
            extraJs = hideOthersJs
        )

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
