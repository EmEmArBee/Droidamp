package com.asfaltosonoro.droidamp

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 3 pagine: 0 = player, 1 = equalizzatore, 2 = playlist.
 * Ogni pagina è un WindowFragment che carica lo stesso bundle Webamp
 * ma inietta CSS/JS diverso per isolare solo la finestra che le compete.
 *
 * NB: gli id CSS/JS delle finestre di Webamp (#main-window, #equalizer-window,
 * #playlist-window) sono presi dal progetto open source Webamp
 * (https://github.com/captbaritone/webamp). Se il bundle che metti in
 * assets/webamp/ usa una build diversa, controlla che questi selettori
 * combacino: vanno aggiornati in WindowFragment.kt.
 */
class LandscapePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val window = when (position) {
            0 -> WindowFragment.WindowType.PLAYER
            1 -> WindowFragment.WindowType.EQUALIZER
            else -> WindowFragment.WindowType.PLAYLIST
        }
        return WindowFragment.newInstance(window)
    }
}
