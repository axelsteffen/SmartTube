# Changelog – Fork-spezifische Änderungen

Dieses Dokument listet alle Änderungen auf, die dieser Fork gegenüber dem Original [SmartTube](https://github.com/yuliskov/SmartTube) enthält.

---

## [Unreleased]

### smarttubetv
- **smarttubetv/build.gradle**: `signingConfig signingConfigs.debug` für Release-Builds ergänzt (lokal, uncommitted)

---

## 2026-02-15

### common – Meilenstein: Next-Video-Quelle (Sektion vs. Next-API)

- **PlayerTweaksData.java**: Neue Einstellung `preferNextApiOverSection` (Index 61, Standard: false)
- **SuggestionsController.java**: `getNext()` um Einstellung erweitert – Reihenfolge Sektion/Next-API steuerbar via `tryNextFromSection()` / `tryNextFromMetadata()`
- **GeneralSettingsPresenter.java**: Neue Option in Allgemeinen Einstellungen (bei „Videos ausblenden“)
- **strings.xml**, **values-de/strings.xml**: String `prefer_next_api_over_section`
- **Feature:** Einstellung für bevorzugte Quelle des nächsten Videos: Sektionszeile oder Next-API (Allgemeine Einstellungen)

---

## 2026-02-13

### common
- **Video.java** (`common/app/models/data/Video.java`): Neues Feld `contentTopic` für thematische Klassifizierung von Videos (z. B. Music, Gaming)
- **Video.java**: Kategorie-Updates propagieren nun konsistent während des Syncs
- **SuggestionsController.java** (`common/playback/controllers/SuggestionsController.java`): Erweiterte Vorschlagslogik mit Kategorie-Matching; Nutzung von `contentTopic` statt `category`
- **VideoStateService.java** (`common/app/models/playback/service/VideoStateService.java`): Erweiterung zur Speicherung/Kategorisierung für thematische Vorschläge
- **GeneralSettingsPresenter.java**: Option „Bereits gesehene Videos aus Vorschlägen ausblenden“ von Player-Einstellungen nach Allgemeine Einstellungen verschoben
- **PlayerTweaksData.java** (`common/prefs/PlayerTweaksData.java`): Neue Einstellungen für „Hide watched from Suggestions“ und „Prefer thematically relevant suggestions“
- **WatchedFilterProcessor.java** (neu): Verarbeitet das Ausblenden bereits gesehener Videos aus den Vorschlägen
- **BrowseProcessorManager.java**: Integration des WatchedFilterProcessor
- **strings.xml** (values + values-de): Neue UI-Texte für die Fork-Funktionen

### MediaServiceCore (Submodul)
- Siehe [MediaServiceCore/CHANGELOG_FORK.md](MediaServiceCore/CHANGELOG_FORK.md) für Details
- Unterstützung für das Abrufen von YouTube-Videokategorien (VideoInfo, PlayerResult, MediaItemFormatInfo)
- Submodul-Referenz aktualisiert (Commits e642ae08b, 912dbb5b1)

---

## 2026-02-12

### common
- **SuggestionsController.java**: Filter für bereits gesehene Videos in Vorschlägen
- **PlayerSettingsPresenter.java** (ursprünglich): Option „Hide watched from Suggestions“ (später nach GeneralSettings verschoben)
- **PlayerTweaksData.java**: Einstellung `hideWatchedFromSuggestions`
- **WatchedFilterProcessor.java** (neu): Filtert bereits gesehene Videos aus den Vorschlägen
- **strings.xml**: String für „Bereits gesehene Videos aus Vorschlägen ausblenden“

---

## Zusammenfassung der neuen Funktionen

| Funktion | Beschreibung | Einstellung |
|----------|--------------|-------------|
| Hide watched from Suggestions | Bereits gesehene Videos werden nicht mehr in den Vorschlägen angezeigt | Allgemeine Einstellungen |
| Prefer thematically relevant suggestions | Vorschläge werden bevorzugt nach thematischer Relevanz (Kategorie/Kanal) sortiert | Allgemeine Einstellungen |
| Prefer Next-API over section for next video | Nächstes Video aus Next-API statt aus Sektionszeile bevorzugen (wenn keine Playlist) | Allgemeine Einstellungen |

### Technische Änderungen
- **contentTopic** (Video): Neues Feld zur thematischen Klassifizierung
- **category** (Video): Logik verbessert für konsistente Updates bei Sync
- **WatchedFilterProcessor**: Neuer Processor für die Watched-Filterung
- **SuggestionsController**: Kategorie- und Kanal-basiertes Matching in der Vorschlagslogik
