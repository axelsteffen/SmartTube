# Changelog – Fork-specific changes (SmartTube)

Changes in this fork against [upstream SmartTube](https://github.com/yuliskov/SmartTube).

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## Fork Touch Points

Stable merge checklist of files that differ from [upstream](https://github.com/yuliskov/SmartTube). Chronological detail stays under `[Unreleased]` below. Kept in sync with [.cursor/skills/upstream-merge/reference.md](../.cursor/skills/upstream-merge/reference.md).

### Upstream files with fork hooks (conflict hotspots)

| File / area | Fork change | Merge hint |
|-------------|-------------|------------|
| `settings.gradle` | Applies `PlexServiceCore/core_settings.gradle` (incl. `:plexserviceinterfaces`, `:plexapi`) | Keep PlexServiceCore apply |
| `common/build.gradle` | Depends on `plexserviceinterfaces` + `plexapi` | Keep deps |
| `common/.../data/Video.java` | `mediaSource`, `isPlex()` / `isYouTube()`, serialize | Keep field + helpers around upstream edits |
| `common/.../presenters/BrowsePresenter.java` | Sidebar registry, `TYPE_PLEX`, pagination, Plex errors | Keep Plex branches |
| `common/.../presenters/ChannelUploadsPresenter.java` | Plex show/season/library grid | Keep Plex `obtainUploadsObservable` branch |
| `common/.../presenters/SignInPresenter.java` | Dispatches to `PlexSignInPresenter` | Keep dispatch |
| `common/.../misc/AppDataSourceManager.java` | Plex settings tile | Keep when enabled |
| `common/.../playback/controllers/VideoLoaderController.java` | Plex format load, subs, audio reload, transcode | Keep Plex paths |
| `common/.../playback/controllers/VideoStateController.java` | Plex timeline + preferred audio | Keep Plex branches |
| `common/.../playback/controllers/SuggestionsController.java` | Skip YT metadata for Plex | Keep `isPlex()` guard |
| `common/.../playback/controllers/ErrorFixerController.java` | Transcode fallback + skip YT remediations | Keep Plex error path |
| `common/.../playback/controllers/PlayerUIController.java` | Skip Like/Subscribe for Plex | Keep guards |
| `common/.../playback/controllers/CommentsController.java` | Skip comments for Plex | Keep guard |
| `common/.../playback/controllers/ChatController.java` | Skip live chat for Plex | Keep guard |
| `common/.../playback/controllers/SponsorBlockController.java` | SponsorBlock only for YouTube | Keep `isYouTube()` check |
| `common/.../playback/controllers/HQDialogController.java` | Plex HLS audio track list | Keep Plex audio UI |
| `common/.../playback/manager/PlayerEngine.java` | Subtitle/audio overloads | Keep API additions |
| `common/.../exoplayer/ExoMediaSourceFactory.java` | `mergeExternalSubtitles()` | Keep helper |
| `common/.../exoplayer/controller/ExoPlayerController.java` | Subtitle merge + preferred audio lang | Keep overloads |
| `common/.../exoplayer/selector/TrackSelectorManager.java` | `setPreferredAudioLanguage` | Keep method |
| `common/src/main/res/values/strings.xml` | Plex UI / error strings | Keep `plex_*` keys |
| `smarttubetv/.../PlaybackFragment.java` | Implements subtitle/audio open APIs | Keep overrides |
| `smarttubetv/.../EmbedPlayerView.java` | Same as PlaybackFragment | Keep overrides |
| `smarttubetv/.../StoryboardManager.java` | `MediaSourceRegistry.getServiceManager()` | Keep registry call |
| `smarttubetv/.../VideoCardPresenter.java` | Null-safe Glide load (placeholder if no thumb) | Keep null guard |
| `common/.../ChannelGroupServiceWrapper.java` | Registry / service access | Keep registry; accept upstream fixes around it |
| ~20 presenters/controllers/misc | `YouTubeServiceManager.instance()` → `MediaSourceRegistry.getServiceManager()` | Re-apply if upstream reintroduces direct calls |

### Fork-only (no upstream counterpart — low merge risk)

| Path | Role |
|------|------|
| `PlexServiceCore/` (submodule) | Fork-only Plex API (`plexserviceinterfaces` + `plexapi`) — see [PlexServiceCore/CHANGELOG.md](../PlexServiceCore/CHANGELOG.md) |
| `common/.../misc/MediaSourceRegistry.java` | Source registry (`YOUTUBE` / `PLEX`) |
| `common/.../misc/SidebarSectionRegistry.java` | Sidebar sections id ≥ 100 |
| `common/.../misc/PlexPlaybackHelper.java` | Format resolve, timeline, audio, errors |
| `common/.../presenters/PlexBrowsePresenter.java` | Library browse + pagination |
| `common/.../presenters/PlexSignInPresenter.java` | PIN sign-in |
| `common/.../presenters/dialogs/PlexServerSelectionPresenter.java` | Server picker |
| `common/.../presenters/settings/PlexSettingsPresenter.java` | Plex settings |
| `common/.../errors/PlexDisabledError.java`, `PlexSignInError.java`, `PlexMessageError.java` | Browse error fragments |
| `fork-docs/`, `.cursor/skills/`, `.cursor/rules/fork-*.mdc` | Docs, merge/commit skills, command router |

### MediaServiceCore (submodule)

See [MediaServiceCore/CHANGELOG_FORK.md](../MediaServiceCore/CHANGELOG_FORK.md). Summary: category field on `MediaItemFormatInfo` + parsing; fork-only `openapi-youtube-api-in-code.yaml`.

### leanbackassistant

| File / area | Fork change | Merge hint |
|-------------|-------------|------------|
| `leanbackassistant/.../misc/ServiceManagerProvider.java` | Fork-only holder (Phase 5.3) | Keep entirely |
| `VideoContentProvider.java`, `Playlist.java` | Use `ServiceManagerProvider.get()` | Keep; no direct `YouTubeServiceManager` at call sites |
| Init from `SplashPresenter` | `ServiceManagerProvider.init(MediaSourceRegistry.getServiceManager())` | Keep init in `common` |

ATV search/channels remain YouTube-backed. Fallback inside provider still uses `YouTubeServiceManager` until splash init.

---

## [Unreleased]

- **Plex Home Rows** ([MILESTONE_PLEX_HOME_ROWS.md](milestones/MILESTONE_PLEX_HOME_ROWS.md)): Sidebar inserts Plex under Home; browse emits Continue Watching, Discover Watchlist (movies), Recently Added, and Grid+Hub recommended rows (TV without watchlist).
- **SidebarSectionRegistry.appendExtraSections**: Insert after `MediaGroup.TYPE_HOME` instead of appending at end.
- **PlexBrowsePresenter**: Home-style row emit order; pagination for onDeck / recentlyAdded / watchlist kinds.
- **common strings**: `plex_row_*` row titles (en + de).
- **PlexMediaItemAdapter.isMovie** / **Video.isEmpty**: Plex movies were dropped as empty (YouTube `isMovie` = "Free with Ads"). Adapter always returns false; `Video.isEmpty` ignores `isMovie` for Plex. Fixes endless browse spinner with empty Movie rows.
- **Video.isMembersOnly** / **PlexMediaItemAdapter.getDurationMs**: Plex TV shows (playlist containers, `videoId` null) were dropped when PMS sent `duration`. `isMembersOnly` now requires no playlist/reload keys; containers report duration 0.
- **BrowsePresenter.updateVideoRows**: Hide progress on complete; skip empty `VideoGroup` after adapt; null-safe view checks.
- **PlexBrowsePresenter**: Emit each library row as it loads (spinner clears after first section; one slow library no longer blocks the UI).
- **PlexMediaItemImpl** / **VideoCardPresenter**: Null thumb fix — Plex fallback paths + Glide placeholder when URL missing (`Received null model`).
- **PlexServerImpl.pickBaseUrl**: Remote access — if `local=false` exists, ignore private `local=true` (Docker/`172.18.0.3`/`*.plex.direct`). Re-select server after update.
- **PlexSignInServiceImpl** / **PlexSignInPresenter**: Fixed Plex PIN sign-in — ran on main thread (`NetworkOnMainThreadException` → log `Plex sign-in error: null`); now `RxHelper.createLong`, short PIN (`strong=false`), QR via `plex.tv/api/v2/pins/qr/{code}`, clearer error text.
- **SplashPresenter**: `PlexServiceManager.init(context)` after global prefs.
- **smarttubetv/build.gradle** (`stbeta`): `applicationId` → `org.smarttube.plex` so the fork can coexist with upstream SmartTube beta on the same TV.
- **smarttubetv/google-services.json**: Client entry for `org.smarttube.plex` (Gradle Google Services plugin).
- **smarttubetv/src/stbeta/.../strings.xml**: Launcher name → `SmartTube Plex`.
- **leanbackassistant/src/stbeta/**: `search_authority` / searchable provider URIs updated to `org.smarttube.plex`.
- **common/.../Utils.java**: Added `org.smarttube.plex` to `KNOWN_PACKAGES`.
- **Phase 5.4**: Extracted `plexserviceinterfaces` + `plexapi` into git submodule `PlexServiceCore` (`axelsteffen/PlexServiceCore`). `settings.gradle` applies `PlexServiceCore/core_settings.gradle` (same pattern as MediaServiceCore). In-tree `plexapi/` / `plexserviceinterfaces/` removed.
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: 5.4 marked done; module layout updated.
- **.gitmodules**: Added `PlexServiceCore` → `https://github.com/axelsteffen/PlexServiceCore.git`.

### fork-docs
- **.cursor/rules/fork-upstream-minimal.mdc** (new): Always-apply rule — prefer fork-only code, thin upstream hooks, `// FORK:` … `// END FORK` markers, keep Fork Touch Points + changelog in sync. Linked from `fork-docs/README.md`, `architecture/UPSTREAM_MERGE.md`, upstream-merge `reference.md`.
- **fork-docs/CHANGELOG.md** / **milestones/MILESTONE_PLEX_INTEGRATION.md**: Phase 5.3 done — leanbackassistant routed via `ServiceManagerProvider`.
- **fork-docs/CHANGELOG.md**: Phase 5.2 — Upstream merge test 2026-07-15: `merge-upstream.sh --fetch-only` → SharedModules / MediaServiceCore / SmartTube all **0 behind** upstream (no merge needed after Phase 4 polish + 5.1 docs). Smoke: `:common:compileStbetaDebugJavaWithJavac` OK. (`:plexapi:testStbetaDebugUnitTest` still hits pre-existing `RoboCookieManager` classpath failures — not an upstream-merge regression.)
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: 5.2 marked done.
- **fork-docs/CHANGELOG.md**: Phase 5.1 — added stable **Fork Touch Points** section (upstream hotspots + fork-only modules + MSC summary).
- **.cursor/skills/upstream-merge/reference.md**: Synced Known Fork Touch Points with Phase 5.1 list (removed “planned” Plex stub).
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: 5.1 marked done; Phase 5 progress split per step.
- **fork-docs/** (new): Central folder for fork changelog, milestones, architecture notes, and maintenance scripts.
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: Phase 0 complete (0.1–0.5). Phase 1.1–1.7 done. Phase 2.1–2.5 done. Phase 3.1–3.5 done (sidebar, browse, pagination, PIN settings). Phase 4.1 done (resume sync). Phase 4.2 done (external subtitles). Phase 4.3 done (audio track selection + HLS reload). Phase 4.4 done (disable YouTube-only features for Plex). Phase 4.5 done (Direct Play → forced HLS transcode on engine error). Phase 4.6 done (Plex error classification + skip YT remediations). Upstream merge 2026-07-15 (`upstream/master` → `main`): 0 behind after merge; 4 conflicts resolved keeping `MediaSourceRegistry` / Plex module hooks (`Video`, `ErrorFixerController`, `ChannelGroupServiceWrapper`, `settings.gradle`). Upstream brought e.g. SponsorBlock toggle fix, error fixer updates, hide member-only videos.
- **fork-docs/COMMANDS.md** (new): Quick reference for short agent commands (`sync yuliskov`, `plex status`, …).
- **.cursor/rules/fork-commands.mdc** (new): Command router — maps short commands to skills/workflows.
- **.cursor/skills/fork-git/** (new): Conventional Commits commit/push workflow.
- **.cursor/skills/upstream-merge/** (new): Cursor skill for AI-assisted upstream merges.
- **fork-docs/scripts/merge-upstream.sh** (new): Fetch and merge helper for SmartTube, MediaServiceCore, and SharedModules.

### common
- **common/.../presenters/SplashPresenter.java**: `ServiceManagerProvider.init(MediaSourceRegistry.getServiceManager())` in `runOnceTasks` (Phase 5.3).
- **common/src/main/java/.../misc/MediaSourceRegistry.java** (new): Fork-only registry for media sources (`YOUTUBE`, `PLEX`). Central `getServiceManager()` accessor; Plex disabled until Phase 1.
- **common/** (24 files): Replaced direct `YouTubeServiceManager.instance()` with `MediaSourceRegistry.getServiceManager()` in presenters, playback controllers, and misc services.
- **common/.../misc/SidebarSectionRegistry.java** (new): Fork-only sidebar extension point for extra sections (id ≥ 100). Phase 3.1: when Plex enabled + auth/server ready → `TYPE_ROW`; else `TYPE_ERROR` with `PlexSignInError`; disabled flag still uses `PlexDisabledError`.
- **common/.../errors/PlexDisabledError.java** (new): Placeholder content for disabled Plex sidebar section.
- **common/.../errors/PlexSignInError.java** (new): Sign-in prompt for Plex sidebar when not authenticated / no server (Phase 3.1; settings UI in 3.5).
- **common/.../misc/MediaSourceRegistry.java**: `isPlexEnabled()` returns `true` (Phase 3.1).
- **common/.../presenters/BrowsePresenter.java**: Hooks into `SidebarSectionRegistry` for extra sections.
- **common/src/main/res/values/strings.xml**: `header_plex`, `plex_not_available`, `plex_signin_to_browse`, `plex_select_server`, `plex_no_servers`, `plex_no_server_selected`, `plex_current_server` strings.
- **common/.../models/data/Video.java**: Fork-only `mediaSource` field (`MediaSourceRegistry.Source`); set from `PlexBackedMediaItem` in `from(MediaItem)`; copy + serialize/deserialize + `isPlex()`/`isYouTube()` (Phase 2.4).
- **common/build.gradle**: Depends on `plexserviceinterfaces` (marker) and `plexapi` (playback routing, Phase 2.5).
- **common/.../misc/PlexPlaybackHelper.java** (new): Resolves Plex `MediaItemFormatInfo` via `PlexServiceManager` + adapters for `Video.isPlex()`; uses `RxHelper` for IO/main scheduling (Phase 2.5).
- **common/.../playback/controllers/VideoLoaderController.java**: Per-video Plex branch in `loadFormatInfo`; VOD HLS via `openHlsUrl` when `containsHlsUrl()` (Phase 2.5).
- **common/.../playback/controllers/SuggestionsController.java**: Skip YouTube metadata/suggestions for Plex videos (Phase 2.5).
- **common/.../presenters/PlexBrowsePresenter.java** (new): Loads Plex movie/show libraries as `MediaGroup` rows; resolves show/season children for drill-down (Phase 3.2–3.3); pagination + full library grid via `continueGroupObserve` / `getLibraryGridObserve` (Phase 3.4).
- **common/.../presenters/ChannelUploadsPresenter.java**: Plex branch in `obtainUploadsObservable` for show/season drill-down via `PlexBrowsePresenter` (Phase 3.3); library grid + Plex `continueGroupObserve` (Phase 3.4).
- **common/.../presenters/BrowsePresenter.java**: Registers `TYPE_PLEX` row mapping from `PlexBrowsePresenter` when Plex is enabled (Phase 3.2); Plex pagination hook in `continueGroup` (Phase 3.4); refreshes sidebar mappings on `updateSections` after sign-in (Phase 3.5).
- **common/.../presenters/PlexSignInPresenter.java** (new): PIN auth via existing `SignInView` / `signInWithPinObserve` (Phase 3.5).
- **common/.../presenters/dialogs/PlexServerSelectionPresenter.java** (new): PMS picker dialog after PIN; updates browse sections (Phase 3.5).
- **common/.../presenters/settings/PlexSettingsPresenter.java** (new): Settings entry for sign-in, server change, sign-out (Phase 3.5).
- **common/.../presenters/SignInPresenter.java**: Dispatches to `PlexSignInPresenter` when waiting (Phase 3.5).
- **common/.../errors/PlexSignInError.java**: Action starts PIN or server picker (Phase 3.5).
- **common/.../misc/AppDataSourceManager.java**: Plex settings tile when enabled (Phase 3.5).
- **common/src/main/res/values/strings.xml**: Plex PIN/server strings (Phase 3.5).
- **common/.../misc/PlexPlaybackHelper.java**: Public `resolvePlexMediaItem()`; playlistId fallback for container stubs (Phase 3.3).
- **common/.../misc/PlexPlaybackHelper.java**: Applies PMS `viewOffset` on format resolve; `updateProgress()` → `/:/timeline` (Phase 4.1).
- **common/.../playback/controllers/VideoStateController.java**: Plex branch in `updateHistory` + per-tickle progress while playing (Phase 4.1).
- **common/.../exoplayer/ExoMediaSourceFactory.java**: `mergeExternalSubtitles()` — `MergingMediaSource` + `SingleSampleMediaSource` for sidecar text tracks (Phase 4.2).
- **common/.../exoplayer/controller/ExoPlayerController.java**: `openHlsUrl`/`openUrlList` overloads merge external subs; DASH+HLS merge flag separate from subtitle merge (Phase 4.2).
- **common/.../playback/manager/PlayerEngine.java**: Overloads `openHlsUrl`/`openUrlList` with `MediaItemFormatInfo` (Phase 4.2).
- **common/.../playback/controllers/VideoLoaderController.java**: Passes formatInfo into HLS/URL open for subtitle merge (Phase 4.2).
- **smarttubetv/.../PlaybackFragment.java**, **EmbedPlayerView.java**: Implement subtitle-aware open overloads (Phase 4.2).
- **common/.../misc/PlexPlaybackHelper.java**: Audio session + preferred language + override stream id for HLS switch (Phase 4.3).
- **common/.../playback/controllers/VideoLoaderController.java**: `reloadPlexAudio()` mid-playback HLS reload; preferred audio language on open (Phase 4.3).
- **common/.../playback/controllers/HQDialogController.java**: Plex HLS audio list → decision reload (Phase 4.3).
- **common/.../playback/controllers/VideoStateController.java**: Apply preferred audio language for Plex (Phase 4.3).
- **common/.../exoplayer/selector/TrackSelectorManager.java**, **ExoPlayerController.java**, **PlayerEngine.java**: `setPreferredAudioLanguage` (Phase 4.3).
- **smarttubetv/.../PlaybackFragment.java**, **EmbedPlayerView.java**: Preferred audio language API (Phase 4.3).
- **common/.../playback/controllers/SponsorBlockController.java**: `checkVideo` requires `isYouTube()`; no SponsorBlock toggle from Plex items (Phase 4.4).
- **common/.../playback/controllers/PlayerUIController.java**: Skip Like/Dislike/Subscribe metadata + clicks for Plex (Phase 4.4).
- **common/.../playback/controllers/CommentsController.java**: Skip comments dialog for Plex (Phase 4.4).
- **common/.../playback/controllers/ChatController.java**: Skip live chat for Plex (Phase 4.4).
- **common/.../misc/PlexPlaybackHelper.java**: Force-transcode one-shot flag + `canAttemptTranscodeFallback` (Phase 4.5).
- **common/.../playback/controllers/VideoLoaderController.java**: `reloadPlexTranscode()`; clear Plex session on `onNewVideo` (Phase 4.5).
- **common/.../playback/controllers/ErrorFixerController.java**: On Plex SOURCE/RENDERER/UNEXPECTED errors, once retry via forced HLS transcode (Phase 4.5).
- **common/src/main/res/values/strings.xml**: `plex_transcode_fallback` (Phase 4.5).
- **common/.../misc/PlexPlaybackHelper.java**: `classifyError` / `getUserMessage` for AUTH / OFFLINE / GENERIC (Phase 4.6).
- **common/.../playback/controllers/ErrorFixerController.java**: Plex format/engine errors → user toast, no YT fixes/reload loops (Phase 4.6).
- **common/.../presenters/BrowsePresenter.java**: Plex section load errors → `PlexSignInError` / `PlexMessageError` (Phase 4.6).
- **common/.../errors/PlexMessageError.java** (new): Browse error fragment with short Plex message (Phase 4.6).
- **common/src/main/res/values/strings.xml**: `plex_error_server_offline`, `plex_error_auth_expired`, `plex_error_playback_failed`, `plex_error_load_failed` (Phase 4.6).

### smarttubetv
- **smarttubetv/build.gradle** + **src/stbeta/**: Fork install ID `org.smarttube.plex` / display name `SmartTube Plex` (coexist with upstream beta).
- **smarttubetv/.../VideoCardPresenter.java**: Skip Glide null model — load placeholder when thumb URL missing.
- **smarttubetv/.../StoryboardManager.java**: Uses `MediaSourceRegistry.getServiceManager()` instead of direct `YouTubeServiceManager`.

### PlexServiceCore (submodule)

As of Phase 5.4, live sources live in [PlexServiceCore/](../PlexServiceCore/) — see [PlexServiceCore/CHANGELOG.md](../PlexServiceCore/CHANGELOG.md). Entries below are historical (pre-extraction paths).

### plexserviceinterfaces
- **plexserviceinterfaces/** (new): Fork-only Plex API contracts — `PlexServiceManager`, sign-in/server/library/media services, and data interfaces (`PlexServer`, `PlexLibrary`, `PlexMediaItem`, `PlexStreamInfo`, `PlexAuthPin`).
- **plexserviceinterfaces/.../data/PlexServer.java**: Added `getAccessToken()` for per-server PMS auth.
- **plexserviceinterfaces/.../data/PlexBackedMediaItem.java** (new): Marker interface so `Video.from` can tag Plex without depending on plexapi (Phase 2.4).
- **plexserviceinterfaces/.../data/PlexMediaPage.java** (new): Paginated library/children page contract (Phase 3.4).
- **plexserviceinterfaces/.../PlexLibraryService.java**: `getMoviesPageObserve`, `getShowsPageObserve`, `getChildrenPageObserve` (Phase 3.4).
- **plexserviceinterfaces/.../PlexMediaService.java**: `getStreamInfoObserve(..., forceTranscode)` (Phase 4.5).

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
- **plexserviceinterfaces/.../PlexLibraryService.java**: `getShowsObserve`, `getChildrenObserve` (Phase 3.3).
- **plexapi/.../network/PlexPmsApi.java**: `TYPE_SHOW`, `getMetadataChildren` (Phase 3.3).
- **plexapi/.../service/PlexLibraryServiceImpl.java**: Show section items + metadata children fetch (Phase 3.3).
- **plexapi/.../adapter/PlexMediaItemAdapter.java**: Show/season as `TYPE_PLAYLIST` containers; movie/episode playable (Phase 3.3).
- **plexapi/.../adapter/PlexMediaGroupAdapter.java**: `fromContainer()` for show/season child grids (Phase 3.3).
- **plexapi/openapi-plex-pms-in-use.yaml**: `/library/metadata/{ratingKey}/children` (Phase 3.3).
- **plexapi/src/test/.../PlexLibraryServiceImplTest.java**: Show + children unit tests (Phase 3.3).
- **plexapi/src/test/.../PlexMediaItemAdapterTest.java**: Show/season/episode mapping tests (Phase 3.3).
- **plexapi/src/test/.../PlexMediaGroupAdapterTest.java**: `fromContainer` unit test (Phase 3.3).
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
- **plexapi/.../library/PlexPage.java** (new): `PlexMediaPage` impl with offset/totalSize/next offset (Phase 3.4).
- **plexapi/.../service/PlexLibraryServiceImpl.java**: Paginated section/children fetch; `getMoviesPageObserve` / `getShowsPageObserve` / `getChildrenPageObserve` (Phase 3.4).
- **plexapi/.../adapter/PlexMediaGroupAdapter.java**: `getNextPageKey` from PMS paging; `continueFrom`, `fromLibraryGrid`, browse stub in rows (Phase 3.4).
- **plexapi/.../adapter/PlexMediaItemAdapter.java**: `fromLibraryBrowse` stub (`reloadPageKey` + `params` = library type) opens full grid (Phase 3.4).
- **plexapi/src/test/.../PlexMediaGroupAdapterTest.java**: Pagination + library grid unit tests (Phase 3.4).
- **plexapi/src/test/.../PlexLibraryServiceImplTest.java**: Offset page unit test (Phase 3.4).
- **plexserviceinterfaces/.../data/PlexMediaItem.java**: `getViewOffsetMs()` (Phase 4.1).
- **plexserviceinterfaces/.../PlexMediaService.java**: `updateProgressObserve` + timeline state constants (Phase 4.1).
- **plexserviceinterfaces/.../data/PlexStreamInfo.java**: `getViewOffsetMs()` (Phase 4.1).
- **plexapi/.../network/dto/PlexMetadata.java**: Parse `viewOffset` (Phase 4.1).
- **plexapi/.../library/PlexMediaItemImpl.java**: Store/map `viewOffsetMs` (Phase 4.1).
- **plexapi/.../adapter/PlexMediaItemAdapter.java**: Map viewOffset → `percentWatched` / `startTimeSeconds` (Phase 4.1).
- **plexapi/.../network/PlexPmsApi.java**: `reportTimeline` (`GET /:/timeline`) (Phase 4.1).
- **plexapi/.../service/PlexMediaServiceImpl.java**: Capture viewOffset on stream resolve; implement progress report (Phase 4.1).
- **plexapi/.../media/PlexStreamInfoImpl.java**: Carry `viewOffsetMs` (Phase 4.1).
- **plexapi/openapi-plex-pms-in-use.yaml**: `/:/timeline` (Phase 4.1).
- **plexapi/src/test/...**: Timeline + viewOffset unit tests (Phase 4.1).
- **plexserviceinterfaces/.../data/PlexSubtitle.java** (new): External sidecar subtitle contract (Phase 4.2).
- **plexserviceinterfaces/.../data/PlexStreamInfo.java**: `getSubtitles()` (Phase 4.2).
- **plexapi/.../network/dto/PlexStream.java** (new): PMS `Stream` under Part (Phase 4.2).
- **plexapi/.../network/dto/PlexPart.java**: Parse `Stream` children (Phase 4.2).
- **plexapi/.../media/PlexSubtitleImpl.java** (new): Immutable subtitle track (Phase 4.2).
- **plexapi/.../adapter/PlexMediaSubtitle.java** (new): Maps to MSC `MediaSubtitle` (srt/ass/vtt/ttml; skips PGS) (Phase 4.2).
- **plexapi/.../media/PlexStreamInfoImpl.java**: Carry subtitle list (Phase 4.2).
- **plexapi/.../service/PlexMediaServiceImpl.java**: Collect external `streamType=3` + `key` from metadata (Phase 4.2).
- **plexapi/.../adapter/PlexMediaItemFormatInfo.java**: `getSubtitles()` from stream (Phase 4.2).
- **plexapi/openapi-plex-pms-in-use.yaml**: `Stream` schema under Part (Phase 4.2).
- **plexapi/src/test/...**: Subtitle collect/map unit tests (Phase 4.2).
- **plexserviceinterfaces/.../data/PlexAudioTrack.java** (new): Audio stream contract (Phase 4.3).
- **plexserviceinterfaces/.../data/PlexStreamInfo.java**: `getAudioTracks()` / `getSelectedAudioStreamId()` (Phase 4.3).
- **plexserviceinterfaces/.../PlexMediaService.java**: `getStreamInfoObserve(item, audioStreamId, preferredLanguage)` (Phase 4.3).
- **plexapi/.../media/PlexAudioTrackImpl.java** (new): Immutable audio track (Phase 4.3).
- **plexapi/.../network/dto/PlexStream.java**: `TYPE_AUDIO` + `channels` (Phase 4.3).
- **plexapi/.../network/PlexPmsApi.java**: `audioStreamID` on decision (Phase 4.3).
- **plexapi/.../service/PlexMediaServiceImpl.java**: Collect/pick audio; pass `audioStreamID` on decision (Phase 4.3).
- **plexapi/.../media/PlexStreamInfoImpl.java**: Carry audio tracks + selected id (Phase 4.3).
- **plexapi/openapi-plex-pms-in-use.yaml**: Stream `channels` + decision audioStreamID note (Phase 4.3).
- **plexapi/src/test/...**: Audio collect/pick unit tests (Phase 4.3).
- **plexapi/.../service/PlexMediaServiceImpl.java**: Skip Direct Play when forced; decision with `directPlay=0`/`directStream=0` (Phase 4.5).
- **plexapi/openapi-plex-pms-in-use.yaml**: Force-transcode decision note (Phase 4.5).
- **plexapi/src/test/.../PlexMediaServiceImplTest.java**: Force-transcode unit test (Phase 4.5).

### leanbackassistant
- **leanbackassistant/.../misc/ServiceManagerProvider.java** (new): Holds `ServiceManager` for ATV helpers; fallback to `YouTubeServiceManager` until init (Phase 5.3). Avoids circular dep on `common` / `MediaSourceRegistry`.
- **leanbackassistant/.../search/VideoContentProvider.java**, **.../media/Playlist.java**: Use `ServiceManagerProvider.get()` instead of direct `YouTubeServiceManager` call sites (Phase 5.3).

### MediaServiceCore (summary)
- See [MediaServiceCore/CHANGELOG_FORK.md](../MediaServiceCore/CHANGELOG_FORK.md) for full detail.
- OpenAPI spec for in-code YouTube APIs (`openapi-youtube-api-in-code.yaml`).
- YouTube video category support in format info and interfaces.
