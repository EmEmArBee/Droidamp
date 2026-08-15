package com.asfaltosonoro.droidamp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Schermata 4 dello swipe landscape: in Fase 1 è solo un placeholder nero.
 *
 * TODO Fase 3: sostituire con una Surface/GLSurfaceView che ospita il render
 * projectM vero, pilotato dai dati audio (FFT/waveform) prodotti dal
 * DroidAmpAudioBridge nativo (stesso bridge usato per il playback reale).
 */
class ProjectMFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val label = TextView(requireContext()).apply {
            text = "ProjectM (placeholder)\n\nLe visualizzazioni MilkDrop-style\narriveranno in Fase 3."
            setTextColor(Color.parseColor("#00FF00"))
            typeface = Typeface.MONOSPACE
            textSize = 16f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return label
    }
}
