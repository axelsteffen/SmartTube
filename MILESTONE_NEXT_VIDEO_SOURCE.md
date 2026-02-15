# Meilenstein: Reihenfolge des nächsten Videos – Sektion vs. Next-API

**Status:** ✅ Abgeschlossen (2026-02-15)

**Ziel:** Eine Einstellung einführen, mit der der Nutzer entscheiden kann, ob das Sektionsvideo oder das Next-API-Video als nächstes bevorzugt wird, wenn kein Playlist-Video vorhanden ist.

---

## Ausgangslage

Aktuelle Logik in `SuggestionsController.getNext()`:

1. **Playlist** – falls vorhanden: Video aus der Warteschlange
2. **Sektion** – falls kein Playlist-Video: nächstes Video aus der Vorschlagszeile/Sektion
3. **Next-API** – falls kein Sektionsvideo: Vorschlag aus der Next-API (YouTube-Metadaten)

**Bereits gesehene Videos:** Wenn `Hide watched from suggestions` aktiv ist, werden gesehene Videos übersprungen. Bei Bedarf wird `findNextUnwatched()` aufgerufen, um das nächste nicht gesehene Video in der Sektion zu finden.

---

## Geplante Änderung

- **Neue Einstellung:** Präferenz zwischen Sektionsvideo und Next-API-Video.
- **Option A (Standard):** Sektionsvideo bevorzugen – aktuelle Reihenfolge bleibt.
- **Option B:** Next-API-Video bevorzugen – Next-API vor Sektion prüfen.

- **Wichtig:** Die Funktion „bereits gesehene Videos ignorieren“ bleibt unverändert und muss bei beiden Optionen greifen.

---

## Kurzformulierungen (für Roadmap/Changelog)

- „Einstellung für bevorzugte Quelle des nächsten Videos: Sektionszeile oder Next-API.“
- „Nutzer können wählen, ob das nächste Video aus der Sektion oder aus der Next-API bevorzugt wird.“

---

## Technischer Ansatz

### Betroffene Logik: `getNext()`

Aktuell (vereinfacht):
```
Playlist → Sektion (mit watched-Check) → Next-API (mit watched-Check)
```

Mit neuer Einstellung „Next-API bevorzugen“:
```
Playlist → Next-API (mit watched-Check) → Sektion (mit watched-Check)
```

### Berücksichtigung bereits gesehener Videos

- `shouldSkipWatched(Video)` prüft, ob ein Video als gesehen gilt (ab 90 %).
- Ist „Hide watched from suggestions“ aktiv und das Kandidaten-Video gesehen: `findNextUnwatched()` sucht das nächste nicht gesehene Video in der Sektion.
- Für Next-API: Wenn das vorgeschlagene Video gesehen ist, wird ebenfalls `findNextUnwatched()` genutzt (Fallback auf Sektion).

Diese Logik bleibt unverändert, nur die Reihenfolge Sektion vs. Next-API wird durch die Einstellung gesteuert.

---

## Implementierungsschritte

### Schritt 1: Einstellung in PlayerTweaksData hinzufügen ✓

**Datei:** `common/src/main/java/com/liskovsoft/smartyoutubetv2/common/prefs/PlayerTweaksData.java`

**Aufgabe:**
- Neue Variable `mIsPreferNextApiOverSectionEnabled` (Standard: `false` = Sektion bevorzugen, aktuelle Logik).
- Getter/Setter: `isPreferNextApiOverSectionEnabled()`, `setPreferNextApiOverSectionEnabled(boolean)`.
- In `restoreData()` und `persistDataInt()` einbinden (neuer Index, z. B. 61).
- Migration: Bestehende Nutzer behalten Standard (Sektion zuerst).

### Schritt 2: getNext() in SuggestionsController anpassen ✓

**Datei:** `common/src/main/java/com/liskovsoft/smartyoutubetv2/common/app/models/playback/controllers/SuggestionsController.java`

**Aufgabe:**
- `getNext()` um die Einstellung erweitern.
- Wenn `isPreferNextApiOverSectionEnabled()`:
  - Zuerst Next-API (current.nextMediaItem) prüfen – mit `shouldSkipWatched` und ggf. `findNextUnwatched`.
  - Danach Sektion (mNextSectionVideo) mit gleicher Logik.
- Wenn `false` (Standard): aktuelle Reihenfolge beibehalten (Sektion → Next-API).

### Schritt 3: Einstellung in den Allgemeinen Einstellungen anzeigen ✓

**Datei:** `common/src/main/java/com/liskovsoft/smartyoutubetv2/common/app/presenters/settings/GeneralSettingsPresenter.java`

**Aufgabe:**
- Eine neue Option hinzufügen (z. B. neben „Thematisch passende Vorschläge bevorzugen“ und „Gesehene Videos in Vorschlägen ausblenden“).
- Label z. B.: „Nächstes Video: Next-API statt Sektion bevorzugen“ (bzw. passende Strings).

### Schritt 4: String-Ressourcen ✓

**Dateien:** `common/src/main/res/values/strings.xml`, `common/src/main/res/values-de/strings.xml`

**Aufgabe:**
- Neuer String, z. B. `prefer_next_api_over_section`: „Next-API-Vorschlag statt Sektionsvideo bevorzugen“.

### Schritt 5: Tests und manuelles Testen ✓

**Aufgabe:**
- Manuell testen:
  1. Einstellung „Sektion bevorzugen“ (Standard): Verhalten wie bisher.
  2. Einstellung „Next-API bevorzugen“: Next-API-Vorschlag wird zuerst genutzt.
  3. Mit „Gesehene Videos ausblenden“: Beide Modi – gesehene Kandidaten werden übersprungen, Fallback funktioniert.

### Schritt 6: CHANGELOG_FORK.md aktualisieren ✓

**Datei:** `CHANGELOG_FORK.md`

**Aufgabe:**
- Eintrag unter `[Unreleased]` / `common`, z. B.:
  - „Einstellung: Bevorzugung von Next-API gegenüber Sektionsvideo für das nächste Video.“

---

## Abhängigkeiten zwischen den Schritten

- Schritt 1 ist Voraussetzung für Schritt 2 und 3.
- Schritt 2 nutzt die neue Einstellung aus Schritt 1.
- Schritt 4 ist für Schritt 3 nötig (UI-Text).
- Schritt 5 und 6 am Ende.

---

## Kurzübersicht der betroffenen Dateien

| Datei | Änderung |
|-------|----------|
| `PlayerTweaksData.java` | Neue Einstellung `preferNextApiOverSection` |
| `SuggestionsController.java` | Anpassung von `getNext()` je nach Einstellung |
| `GeneralSettingsPresenter.java` | Neue Option in den Einstellungen |
| `values/strings.xml`, `values-de/strings.xml` | Neue Beschriftung |
| `CHANGELOG_FORK.md` | Eintrag zu der Änderung |

---

## TV-Kontext

Die Änderung ist ressourcenschonend: keine zusätzlichen API-Aufrufe, kein zusätzlicher Cache. Es wird lediglich die Reihenfolge der Abfrage angepasst. Die bestehende Logik für gesehene Videos bleibt unverändert.

---

## Machbarkeitsprüfung

**Datum:** 15.02.2025  
**Ergebnis:** ✅ **Machbar** – alle betroffenen Stellen im Code identifiziert, keine Blocker erkannt.

### 1. Code-Struktur bestätigt

| Komponente | Datei | Verifiziert | Anmerkung |
|------------|-------|-------------|-----------|
| `getNext()` | `SuggestionsController.java` (Zeilen 279–297) | ✅ | Klare Reihenfolge: Playlist → Sektion → Next-API. Umstellung per if-Verzweigung möglich. |
| Einstellungs-Persistenz | `PlayerTweaksData.java` | ✅ | Index 61 frei. Letzter Index: 60 (`mIsThematicSuggestionsEnabled`). |
| UI-Einstellung | `GeneralSettingsPresenter.java` | ✅ | `appendHideVideos()` enthält passende Stelle (z. B. nach `thematic_suggestions`). |
| String-Ressourcen | `values/strings.xml`, `values-de/strings.xml` | ✅ | Übliche String-Einträge ergänzbar. |

### 2. Technische Details

**`getNext()` (aktuelle Logik):**
- Zeilen 287–290: Sektion (inkl. Fallback bei gesehen)
- Zeilen 291–295: Next-API (`current.nextMediaItem`)
- `findNextUnwatched(skipped, current)` nutzt `current.getGroup()` (Sektion) als Quelle für den Fallback – geeignet für beide Modi.

**`PlayerTweaksData`:**
- `restoreData()`: neue Variable mit Index 61
- `persistDataInt()`: zusätzlicher Wert in `Helpers.mergeData(...)`

**Randfälle:**
- **Sektion deaktiviert** (`isSectionPlaylistEnabled == false`): `mNextSectionVideo == null` → Einstellung hat keine Wirkung, Next-API wird ohnehin genutzt. Akzeptabel.
- **Next-API-Video gesehen:** `findNextUnwatched` fällt zurück auf die Sektion (da `nextFromMetadata` typischerweise nicht in der Sektion ist, wird ab `current` gesucht). Unverändert zur bestehenden Logik.

### 3. Risiken und Abhängigkeiten

| Risiko | Einschätzung |
|--------|--------------|
| Index 61 in `PlayerTweaksData` verdrängt zukünftige Einstellungen | Gering – übliches Muster, Indexwahl dokumentiert. |
| `Helpers.mergeData` Reihenfolge | Muss in `restoreData()` und `persistDataInt()` konsistent angepasst werden. |
| Shuffle-Modus | `findNextSectionVideoIfNeeded` nutzt `findRandomSectionVideo` – betrifft nur Sektion, nicht Next-API. Keine weitere Änderung nötig. |

### 4. Implementierungsaufwand (geschätzt)

- Schritt 1 (PlayerTweaksData): ~15 Min.
- Schritt 2 (SuggestionsController): ~20 Min.
- Schritt 3 (GeneralSettingsPresenter): ~10 Min.
- Schritt 4 (Strings): ~5 Min.
- Schritt 5–6 (Test, Changelog): ~30 Min.

**Gesamt:** ca. 1–1,5 Stunden.

### 5. Fazit

Der Meilenstein ist technisch umsetzbar. Die Strukturen im Code sind vorhanden, die Änderungen sind lokal begrenzt und ressourcenschonend. Empfohlen: Umsetzung gemäß den geplanten Implementierungsschritten.
