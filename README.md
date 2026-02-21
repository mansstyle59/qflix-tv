# Qflix (Android TV)

Projet Android TV (Leanback) + Player (Media3/ExoPlayer) + interface type catalogue.
✅ Sources: playlists M3U légitimes + URLs directes  
✅ Métadonnées: optionnel via TMDB (clé API)

## Ouvrir dans Android Studio
- File > Open > sélectionner le dossier `QflixAndroidTV`
- Laisser Android Studio faire le Sync

## Ajouter ta clé TMDB (optionnel)
Dans `local.properties` (créé automatiquement par Android Studio) :
TMDB_API_KEY=xxxx

## Générer un APK (sans taper gradlew)
Android Studio :
Build > Build Bundle(s) / APK(s) > Build APK(s)

## Note
Utilise uniquement des flux/contenus que tu as le droit de lire.


## Fonctionnalités ajoutées
- Reprendre (historique + reprise de lecture)
- Favoris
- Recherche (dans le catalogue chargé)
- Cache local des métadonnées TMDB (7 jours)


## 📺 TV en direct
- Détection automatique des chaînes LIVE (#EXTINF:-1 dans M3U)
- Ligne dédiée 'TV en direct'
- Pas de reprise sur les flux live


## 🗓️ EPG (Guide TV) XMLTV
- Ajoute une URL **XMLTV** lors de l’ajout de source (optionnel)
- Affiche *Maintenant / Ensuite* sur les chaînes LIVE (si données disponibles)
- Cache EPG local (6h)

## ⏩ Zapping (TV en direct)
- En lecture LIVE : **DPAD ← / →** pour chaîne précédente/suivante


## 📡 Guide TV complet
- Ecran grille chaînes
- Programmes maintenant / ensuite
- Ouverture par appui long depuis accueil

## 🚀 Démarrage auto TV Live
- Option interne SettingsStore
