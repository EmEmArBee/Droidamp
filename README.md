# 🤖amp (DroidAmp)

Clone Android di Winamp Classic, skin-compatibile, 100% offline.
Progetto di [drag0n] / Asfalto Sonoro.

## Stato: Fase 1 - Scaffold

Cosa c'è già:
- Progetto Android buildabile via GitHub Actions (vedi `.github/workflows/build.yml`,
  produce un APK debug scaricabile dalla tab "Actions" del repo dopo ogni push).
- `MainActivity` che carica una pagina offline dagli assets:
  - **Portrait**: schermo intero, stack verticale unico (player + EQ + playlist).
  - **Landscape**: `ViewPager2` con 3 pagine a swipe, una per finestra, a tutto schermo.

Cosa manca ancora (arriverà nei prossimi passaggi):
- La build reale di **Webamp** (https://github.com/captbaritone/webamp) al posto
  del placeholder in `app/src/main/assets/webamp/index.html`.
- Il bridge audio nativo (playback vero via Media3/ExoPlayer).
- Sincronizzazione dello stato fra le 3 WebView in landscape.
- projectM per le visualizzazioni MilkDrop-style.
- Icona app, easter egg, skin di default incorporata.

## Come caricarlo su GitHub

1. Crea un nuovo repository (es. `droidamp`), vuoto, senza README/licenza
   auto-generati.
2. Copia dentro TUTTI i file e le cartelle di questo pacchetto, mantenendo
   esattamente la stessa struttura di percorsi.
3. Commit + push sul branch `main`.
4. Vai nella tab "Actions" del repo: partirà da solo il workflow "Build APK".
   A fine build trovi l'APK scaricabile come artifact.

## Prossimo passo consigliato

Sostituire `app/src/main/assets/webamp/index.html` (e i file JS/CSS a
corredo) con una build statica reale di Webamp, generata da:
https://github.com/captbaritone/webamp (cartella `packages/webamp` ha le
istruzioni di build; il risultato è un bundle JS/CSS/HTML statico da
copiare paro paro dentro `assets/webamp/`).
