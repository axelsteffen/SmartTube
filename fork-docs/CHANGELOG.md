# Changelog – Fork-specific changes (SmartTube)

Changes in this fork against [upstream SmartTube](https://github.com/yuliskov/SmartTube).

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased]

### fork-docs
- **fork-docs/** (new): Central folder for fork changelog, milestones, architecture notes, and maintenance scripts.
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: Phase 0 complete (0.1–0.5). Phase 1.1–1.7 done. Phase 2.1–2.5 done. Phase 3.1–3.2 done (sidebar + movie library rows via `PlexBrowsePresenter`). Upstream merge verified 2026-07-15 — all repos up to date, no conflicts.
- **fork-docs/COMMANDS.md** (new): Quick reference for short agent commands (`sync yuliskov`, `plex status`, …).
- **.cursor/rules/fork-commands.mdc** (new): Command router — maps short commands to skills/workflows.
- **.cursor/skills/fork-git/** (new): Conventional Commits commit/push workflow.
- **.cursor/skills/upstream-merge/** (new): Cursor skill for AI-assisted upstream merges.
- **fork-docs/scripts/merge-upstream.sh** (new): Fetch and merge helper for SmartTube, MediaServiceCore, and SharedModules.

### common
- **common/src/main/java/.../misc/MediaSourceRegistry.java** (new): Fork-only registry for media sources (`YOUTUBE`, `PLEX`). Central `getServiceManager()` accessor; Plex disabled until Phase 1.
- **common/** (24 files): Replaced direct `YouTubeServiceManager.instance()` with `MediaSourceRegistry.getServiceManager()` in presenters, playback controllers, and misc services.
- **common/.../misc/SidebarSectionRegistry.java** (new): Fork-only sidebar extension point for extra sections (id ≥ 100). Phase 3.1: when Plex enabled + auth/server ready → `TYPE_ROW`; else `TYPE_ERROR` with `PlexSignInError`; disabled flag still uses `PlexDisabledError`.
- **common/.../errors/PlexDisabledError.java** (new): Placeholder content for disabled Plex sidebar section.
- **common/.../errors/PlexSignInError.java** (new): Sign-in prompt for Plex sidebar when not authenticated / no server (Phase 3.1; settings UI in 3.5).
- **common/.../misc/MediaSourceRegistry.java**: `isPlexEnabled()` returns `true` (Phase 3.1).
- **common/.../presenters/BrowsePresenter.java**: Hooks into `SidebarSectionRegistry` for extra sections.
- **common/src/main/res/values/strings.xml**: `header_plex`, `plex_not_available`, `plex_signin_to_browse`, `plex_signin_coming_soon` strings.
- **common/.../models/data/Video.java**: Fork-only `mediaSource` field (`MediaSourceRegistry.Source`); set from `PlexBackedMediaItem` in `from(MediaItem)`; copy + serialize/deserialize + `isPlex()`/`isYouTube()` (Phase 2.4).
- **common/build.gradle**: Depends on `plexserviceinterfaces` (marker) and `plexapi` (playback routing, Phase 2.5).
- **common/.../misc/PlexPlaybackHelper.java** (new): Resolves Plex `MediaItemFormatInfo` via `PlexServiceManager` + adapters for `Video.isPlex()`; uses `RxHelper` for IO/main scheduling (Phase 2.5).
- **common/.../playback/controllers/VideoLoaderController.java**: Per-video Plex branch in `loadFormatInfo`; VOD HLS via `openHlsUrl` when `containsHlsUrl()` (Phase 2.5).
- **common/.../playback/controllers/SuggestionsController.java**: Skip YouTube metadata/suggestions for Plex videos (Phase 2.5).
- **common/.../presenters/PlexBrowsePresenter.java** (new): Loads Plex movie libraries as `MediaGroup` rows (sequential first-page fetch via `PlexMediaGroupAdapter`) for Browse UI (Phase 3.2).
- **common/.../presenters/BrowsePresenter.java**: Registers `TYPE_PLEX` row mapping from `PlexBrowsePresenter` when Plex is enabled (Phase 3.2).

### smarttubetv
- **smarttubetv/.../StoryboardManager.java**: Uses `MediaSourceRegistry.getServiceManager()` instead of direct `YouTubeServiceManager`.

### plexserviceinterfaces
- **plexserviceinterfaces/** (new): Fork-only Plex API contracts — `PlexServiceManager`, sign-in/server/library/media services, and data interfaces (`PlexServer`, `PlexLibrary`, `PlexMediaItem`, `PlexStreamInfo`, `PlexAuthPin`).
- **plexserviceinterfaces/.../data/PlexServer.java**: Added `getAccessToken()` for per-server PMS auth.
- **plexserviceinterfaces/.../data/PlexBackedMediaItem.java** (new): Marker interface so `Video.from` can tag Plex without depending on plexapi (Phase 2.4).

### plexapi
- **plexapi/** (new): Fork-only Plex API implementation module (Retrofit deps). Entry point `com.liskovsoft.plexapi.PlexServiceManager`.
- **plexapi/.../prefs/PlexPrefs.java**: Persists auth token, selected server (id/name/baseUrl/accessToken), and stable `X-Plex-Client-Identifier` UUID.
- **plexapi/.../network/**: `PlexTvApi`, Retrofit helper, `PlexHeadersInterceptor` for plex.tv calls.
- **plexapi/.../service/PlexSignInServiceImpl.java**: PIN auth (`signInWithPinObserve`) + `setAuthToken`/`signOut` with prefs persistence (Phase 1.3). `signOut` clears selected server.
- **plexapi/.../service/PlexServerServiceImpl.java**: Server discovery via plex.tv `/api/v2/resources` (Phase 1.4); selects connection (local HTTPS preferred); persists selection in `PlexPrefs`.
- **plexapi/.../server/PlexServerImpl.java**: Maps `PlexResource` → `PlexServer` (incl. per-server `accessToken`).
- **plexapi/.../service/PlexLibraryServiceImpl.java**: Lists sections + first page of movies via PMS (`type=1`, page size 50) using selected server + accessToken (Phase 1.5).
- **plexapi/.../service/PlexMediaServiceImpl.java**: Resolves stream URL — Direct Play from metadata `Part.key`, HLS decision fallback (Phase 1.6).
- **plexapi/.../adapter/PlexMediaItemAdapter.java** (new): Wraps `PlexMediaItem` as MSC `MediaItem` (`videoId` = `ratingKey`); enables `Video.from(MediaItem)` (Phase 2.1). Implements `PlexBackedMediaItem` for `Video.mediaSource` tagging (Phase 2.4).
- **plexapi/.../adapter/PlexMediaGroupAdapter.java** (new): Wraps `PlexLibrary` + movie page as MSC `MediaGroup` (`TYPE_MOVIES`, `params` = library key); enables `VideoGroup.from(MediaGroup)` (Phase 2.2).
- **plexapi/.../adapter/PlexMediaItemFormatInfo.java** (new): Maps `PlexStreamInfo` + item metadata to MSC `MediaItemFormatInfo` — Direct Play → UrlFormats; transcoded HLS → `getHlsManifestUrl()` (Phase 2.3; VOD HLS opened in VideoLoaderController 2.5).
- **plexapi/.../adapter/PlexMediaFormat.java** (new): Minimal `MediaFormat` (`FORMAT_TYPE_REGULAR`) for progressive Direct Play URLs.
- **plexapi/build.gradle**: Depends on `mediaserviceinterfaces` for the adapters.
- **plexapi/src/test/.../PlexMediaItemAdapterTest.java**: Mapping + equality unit tests (Phase 2.1).
- **plexapi/src/test/.../PlexMediaGroupAdapterTest.java**: Title/items/empty/null-safe unit tests (Phase 2.2).
- **plexapi/src/test/.../PlexMediaItemFormatInfoTest.java**: Direct Play UrlFormats + HLS manifest mapping unit tests (Phase 2.3).
- **plexapi/.../media/PlexStreamInfoImpl.java**: Immutable stream info + MIME hints for ExoPlayer.
- **plexapi/.../library/PlexLibraryImpl.java**, **PlexMediaItemImpl.java**: Maps PMS Directory/Metadata; absolute thumb URLs via `PlexUrlHelper`.
- **plexapi/.../network/PlexPmsApi.java**: Explicit `X-Plex-Token` on library/metadata/decision calls.
- **plexapi/src/test/.../PlexMediaServiceImplTest.java**: MockWebServer unit tests for Direct Play + decision fallback (Phase 1.7).
- **plexapi/openapi-plex-pms-in-use.yaml** (new): Scoped OpenAPI (PMS 1.2.2 + plex.tv) for PoC endpoints — identity, sections, section items, metadata, parts, playback decision, resources.
- **plexapi/.../network/PlexPmsApi.java**, **PlexTvResourcesApi.java**: Retrofit contracts for PMS + server discovery.
- **plexapi/.../network/dto/**: Gson DTOs (`MediaContainer`, `PlexDirectory`, `PlexMetadata`, `PlexMedia`, `PlexPart`, `PlexResource`).
- **plexapi/.../network/PlexUrlHelper.java**: Absolute Direct Play / thumb URLs with `X-Plex-Token` query.
- **plexapi/.../network/PlexHeadersInterceptor.java**: Attaches auth token from `PlexPrefs` when present.

### leanbackassistant
- _(unchanged — `common` already depends on `leanbackassistant`; circular dep prevents using `MediaSourceRegistry` here)_

### MediaServiceCore (summary)
- See [MediaServiceCore/CHANGELOG_FORK.md](../MediaServiceCore/CHANGELOG_FORK.md) for full detail.
- OpenAPI spec for in-code YouTube APIs (`openapi-youtube-api-in-code.yaml`).
- YouTube video category support in format info and interfaces.
