package com.asfaltosonoro.droidamp

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 4 pagine: 0 = player, 1 = equalizzatore, 2 = playlist, 3 = projectM.
 * Le prime 3 sono WindowFragment (WebView con Webamp, una finestra a testa).
 * La 4a è ProjectMFragment (per ora placeholder, vera integrazione in Fase 3).
 *
 * NB: gli id CSS/JS delle finestre di Webamp (#main-window, #equalizer-window,
 * #playlist-window) sono presi dal progetto open source Webamp
 * (https://github.com/captbaritone/webamp). Se il bundle che metti in
 * assets/webamp/ usa una build diversa, controlla che questi selettori
 * combacino: vanno aggiornati in WindowFragment.kt.
 */
class LandscapePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WindowFragment.newInstance(WindowFragment.WindowType.PLAYER)
            1 -> WindowFragment.newInstance(WindowFragment.WindowType.EQUALIZER)
            2 -> WindowFragment.newInstance(WindowFragment.WindowType.PLAYLIST)
            else -> ProjectMFragment()
        }
    }
}
