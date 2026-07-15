# Changelog – Fork-specific changes (SmartTube)

Changes in this fork against [upstream SmartTube](https://github.com/yuliskov/SmartTube).

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased]

### fork-docs
- **fork-docs/** (new): Central folder for fork changelog, milestones, architecture notes, and maintenance scripts.
- **fork-docs/milestones/MILESTONE_PLEX_INTEGRATION.md**: Phase 0 complete (0.1–0.5). Phase 1.1–1.4 done (modules + PIN auth + server discovery). Upstream merge verified 2026-07-15 — all repos up to date, no conflicts.
- **fork-docs/COMMANDS.md** (new): Quick reference for short agent commands (`sync yuliskov`, `plex status`, …).
- **.cursor/rules/fork-commands.mdc** (new): Command router — maps short commands to skills/workflows.
- **.cursor/skills/fork-git/** (new): Conventional Commits commit/push workflow.
- **.cursor/skills/upstream-merge/** (new): Cursor skill for AI-assisted upstream merges.
- **fork-docs/scripts/merge-upstream.sh** (new): Fetch and merge helper for SmartTube, MediaServiceCore, and SharedModules.

### common
- **common/src/main/java/.../misc/MediaSourceRegistry.java** (new): Fork-only registry for media sources (`YOUTUBE`, `PLEX`). Central `getServiceManager()` accessor; Plex disabled until Phase 1.
- **common/** (24 files): Replaced direct `YouTubeServiceManager.instance()` with `MediaSourceRegistry.getServiceManager()` in presenters, playback controllers, and misc services.
- **common/.../misc/SidebarSectionRegistry.java** (new): Fork-only sidebar extension point for extra sections (id ≥ 100).
- **common/.../errors/PlexDisabledError.java** (new): Placeholder content for disabled Plex sidebar section.
- **common/.../presenters/BrowsePresenter.java**: Hooks into `SidebarSectionRegistry` for extra sections.
- **common/src/main/res/values/strings.xml**: `header_plex`, `plex_not_available` strings.

### smarttubetv
- **smarttubetv/.../StoryboardManager.java**: Uses `MediaSourceRegistry.getServiceManager()` instead of direct `YouTubeServiceManager`.

### plexserviceinterfaces
- **plexserviceinterfaces/** (new): Fork-only Plex API contracts — `PlexServiceManager`, sign-in/server/library/media services, and data interfaces (`PlexServer`, `PlexLibrary`, `PlexMediaItem`, `PlexStreamInfo`, `PlexAuthPin`).
- **plexserviceinterfaces/.../data/PlexServer.java**: Added `getAccessToken()` for per-server PMS auth.

### plexapi
- **plexapi/** (new): Fork-only Plex API implementation module (Retrofit deps). Entry point `com.liskovsoft.plexapi.PlexServiceManager`.
- **plexapi/.../prefs/PlexPrefs.java**: Persists auth token, selected server (id/name/baseUrl/accessToken), and stable `X-Plex-Client-Identifier` UUID.
- **plexapi/.../network/**: `PlexTvApi`, Retrofit helper, `PlexHeadersInterceptor` for plex.tv calls.
- **plexapi/.../service/PlexSignInServiceImpl.java**: PIN auth (`signInWithPinObserve`) + `setAuthToken`/`signOut` with prefs persistence (Phase 1.3). `signOut` clears selected server.
- **plexapi/.../service/PlexServerServiceImpl.java**: Server discovery via plex.tv `/api/v2/resources` (Phase 1.4); selects connection (local HTTPS preferred); persists selection in `PlexPrefs`.
- **plexapi/.../server/PlexServerImpl.java**: Maps `PlexResource` → `PlexServer` (incl. per-server `accessToken`).
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
