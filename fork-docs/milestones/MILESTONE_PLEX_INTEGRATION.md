# Milestone: Plex Integration

## Goal

Play Plex media (primarily own library on Plex Media Server) inside SmartTube without switching apps, while keeping **SmartTube** and **MediaServiceCore** updatable from upstream.

## Starting Point / Current State

| Component | Status |
|-----------|--------|
| `MediaServiceCore/mediaserviceinterfaces` | YouTube-centric interfaces |
| `MediaServiceCore/youtubeapi` | YouTube via `YouTubeServiceManager` |
| `common/` / `smarttubetv/` | Hardcoded `YouTubeServiceManager.instance()` (~30 places) |
| Playback | ExoPlayer, source-agnostic (HLS/DASH/MP4) |
| MediaServiceCore fork | Category field in interfaces + OpenAPI spec |
| Plex | Modules + PIN auth + discovery + library/movies + stream URL + MediaItem/MediaGroup/FormatInfo adapters + Video.mediaSource + playback routing (2.1–2.5) + sidebar section (3.1) |

## Architecture Principles

1. **Plex code lives in SmartTube fork only** — not in MediaServiceCore
2. **Do not extend `mediaserviceinterfaces` for Plex** — fork-only interfaces + adapters
3. **Minimal hooks in upstream files** — registry, sidebar extension, source routing
4. **Reuse existing UI/Player** where possible
5. **TV resource constraints** — no bulk caching, lazy loading, on-demand API calls

## Module Layout (Target)

```text
SmartTube/                          (Fork)
├── MediaServiceCore/               (Fork Submodule — minimal changes)
│   ├── mediaserviceinterfaces/
│   └── youtubeapi/
├── plexserviceinterfaces/          NEW, fork-only
├── plexapi/                        NEW, fork-only
├── common/                         fork hooks
└── smarttubetv/                    fork hooks
```

## Implementation Phases

### Phase 0: Foundation

| Step | Description | Merge risk |
|------|-------------|------------|
| 0.1 | Milestone doc (this file) | None |
| 0.2 | `MediaSourceRegistry` in fork-only class | Low |
| 0.3 | Replace direct `YouTubeServiceManager.instance()` with registry (central place) | Low–Medium |
| 0.4 | Sidebar extension point for extra sections | Low |
| 0.5 | Verify upstream merge still works | — |

**Exit criterion:** App behaves as today; registry exists; sidebar can show disabled Plex entry.

### Phase 1: Plex API Proof of Concept

| Step | Description |
|------|-------------|
| 1.1 | Gradle module `plexserviceinterfaces` |
| 1.2 | Gradle module `plexapi` |
| 1.3 | Plex auth (PIN or token) |
| 1.4 | Server discovery |
| 1.5 | Fetch one library movie list |
| 1.6 | Resolve stream URL for one movie |
| 1.7 | Integration test against local Plex server |

**Exit criterion:** Login → pick library → play one movie via ExoPlayer (test/debug screen).

### Phase 2: Adapter Layer

| Step | Description |
|------|-------------|
| 2.1 | `PlexMediaItem` implements `MediaItem` |
| 2.2 | `PlexMediaGroup` → `VideoGroup` mapping |
| 2.3 | `PlexFormatInfo` → ExoPlayer-compatible HLS URL |
| 2.4 | `Video.mediaSource` field (fork-only) |
| 2.5 | Playback routing by source |

**Exit criterion:** One Plex movie plays through existing `PlaybackPresenter`.

### Phase 3: Browse UI

| Step | Description |
|------|-------------|
| 3.1 | Plex sidebar section (id ≥ 100) |
| 3.2 | `PlexBrowsePresenter` — libraries as rows |
| 3.3 | Drill-down: Library → Movies/Shows → Season → Episodes |
| 3.4 | Reuse grid/row fragments |
| 3.5 | Plex settings (server, credentials) |

**Exit criterion:** Full browse flow; click movie → playback.

### Phase 4: Playback Polish

| Step | Description |
|------|-------------|
| 4.1 | Resume position sync with Plex |
| 4.2 | Subtitles |
| 4.3 | Audio track selection |
| 4.4 | Disable YouTube-only features for Plex (SponsorBlock, Like, Comments) |
| 4.5 | Transcode fallback |
| 4.6 | Error handling |

### Phase 5: Hardening

| Step | Description |
|------|-------------|
| 5.1 | Document fork touch points in CHANGELOG |
| 5.2 | Upstream merge test |
| 5.3 | Reduce direct `YouTubeServiceManager` usages |
| 5.4 | Optional: extract to `PlexServiceCore` submodule |

## MediaServiceCore Fork Policy

| Allowed | Discouraged |
|---------|-------------|
| `youtubeapi/` enhancements | New modules in MSC |
| Upstream-compatible bugfixes | Plex code in MSC |
| Document in MSC CHANGELOG_FORK | Changes to `mediaserviceinterfaces/` unless unavoidable |

## What NOT to Do

- Plex module inside `MediaServiceCore/`
- Extending `ContentService` with Plex methods
- Large refactors in `common/` before PoC
- Plex AVOD ad-skipping in v1 (own library is ad-free)

## Progress

| Phase | Step | Status |
|-------|------|--------|
| 0 | 0.1 Milestone doc | done |
| 0 | 0.2 MediaSourceRegistry | done |
| 0 | 0.3 Service registry | done |
| 0 | 0.4 Sidebar extension | done |
| 0 | 0.5 Upstream merge verify | done |
| 1 | 1.1 plexserviceinterfaces | done |
| 1 | 1.2 plexapi module | done |
| 1 | 1.3 Auth (PIN + prefs) | done |
| 1 | 1.4 Server discovery | done |
| 1 | 1.5 Library movie list | done |
| 1 | 1.6 Stream URL resolve | done |
| 1 | 1.7 Stream unit test (MockWebServer) | done |
| 2 | 2.1 PlexMediaItem → MediaItem adapter | done |
| 2 | 2.2 PlexMediaGroup → MediaGroup adapter | done |
| 2 | 2.3 PlexFormatInfo → MediaItemFormatInfo | done |
| 2 | 2.4 Video.mediaSource field (fork-only) | done |
| 2 | 2.5 Playback routing by source | done |
| 3 | 3.1 Plex sidebar section (id ≥ 100) | done |
| 3 | 3.2 PlexBrowsePresenter — libraries as rows | open |
| 3 | 3.3 Drill-down: Library → Movies/Shows → Season → Episodes | open |
| 3 | 3.4 Reuse grid/row fragments | open |
| 3 | 3.5 Plex settings (server, credentials) | open |
| 4 | 4.1–4.6 Playback polish | open |
| 5 | 5.1–5.4 Hardening | open |

## Rough Effort

| Phase | Estimate |
|-------|----------|
| 0 Foundation | 2–4 days |
| 1 Plex API PoC | 1–2 weeks |
| 2 Adapters | 3–5 days |
| 3 Browse UI | 1–2 weeks |
| 4 Playback polish | 1–2 weeks |
| 5 Hardening | 3–5 days |

**Total:** ~6–10 weeks part-time for solid v1 (own library, Direct Play + basic transcode).
