# Upstream Merge — Conflict Reference

## Conflict Resolution Rules

| Situation | Resolution |
|-----------|------------|
| Fork-only file (e.g. `openapi-youtube-api-in-code.yaml`, `plexapi/`) | Keep fork version entirely |
| Fork addition in shared file (e.g. `Video.mediaSource`, category field) | Keep fork addition; merge upstream changes around it |
| Upstream bugfix in `youtubeapi/` | Accept upstream fix; re-apply fork additions if needed |
| Upstream refactor in `common/` or `smarttubetv/` | Accept upstream; re-apply fork hooks (registry, Plex branches) |
| Submodule pointer in SmartTube | Use merged MSC/SharedModules commit SHA |
| Unsure | Ask user; prefer smaller diff |

## Known Fork Touch Points

Canonical copy (with more detail): [fork-docs/CHANGELOG.md § Fork Touch Points](../../../fork-docs/CHANGELOG.md#fork-touch-points).

### MediaServiceCore (submodule)

| File / area | Fork change |
|-------------|-------------|
| `mediaserviceinterfaces/.../MediaItemFormatInfo.java` | Video category field |
| `youtubeapi/.../VideoInfo.java` | Category parsing |
| `youtubeapi/.../PlayerResult.kt` (+ extensions) | Category from player |
| `youtubeapi/.../MediaItemFormatInfoImpl.kt` | Category enrichment |
| `youtubeapi/.../YouTubeMediaItemFormatInfo.java` | Category field |
| `youtubeapi/openapi-youtube-api-in-code.yaml` | Fork-only OpenAPI spec |

### SmartTube — Gradle / modules

| File / area | Fork change |
|-------------|-------------|
| `settings.gradle` | `include ':plexserviceinterfaces', ':plexapi'` |
| `common/build.gradle` | Depends on plex modules |
| `plexserviceinterfaces/`, `plexapi/` | Fork-only Plex stack (keep entirely) |
| `fork-docs/`, `.cursor/skills/`, `.cursor/rules/fork-*.mdc` | Docs and agent tooling |

### SmartTube — upstream files with Plex / registry hooks

| File / area | Fork change | Merge hint |
|-------------|-------------|------------|
| `common/.../data/Video.java` | `mediaSource`, `isPlex()` / `isYouTube()` | Keep field + helpers |
| `common/.../presenters/BrowsePresenter.java` | Sidebar, `TYPE_PLEX`, pagination, errors | Keep Plex branches |
| `common/.../presenters/ChannelUploadsPresenter.java` | Show/season/library grid | Keep Plex branch |
| `common/.../presenters/SignInPresenter.java` | → `PlexSignInPresenter` | Keep dispatch |
| `common/.../misc/AppDataSourceManager.java` | Plex settings tile | Keep when enabled |
| `common/.../playback/controllers/VideoLoaderController.java` | Format / subs / audio / transcode | Keep Plex paths |
| `common/.../playback/controllers/VideoStateController.java` | Timeline + preferred audio | Keep Plex branches |
| `common/.../playback/controllers/SuggestionsController.java` | Skip YT metadata for Plex | Keep guard |
| `common/.../playback/controllers/ErrorFixerController.java` | Transcode + no YT remediations | Keep Plex path |
| `common/.../playback/controllers/PlayerUIController.java` | Skip Like/Subscribe | Keep guards |
| `common/.../playback/controllers/CommentsController.java` | Skip comments | Keep guard |
| `common/.../playback/controllers/ChatController.java` | Skip live chat | Keep guard |
| `common/.../playback/controllers/SponsorBlockController.java` | YouTube-only | Keep `isYouTube()` |
| `common/.../playback/controllers/HQDialogController.java` | Plex HLS audio | Keep Plex UI |
| `common/.../playback/manager/PlayerEngine.java` | Subtitle/audio overloads | Keep APIs |
| `common/.../exoplayer/ExoMediaSourceFactory.java` | External subtitle merge | Keep helper |
| `common/.../exoplayer/controller/ExoPlayerController.java` | Subs + preferred audio | Keep overloads |
| `common/.../exoplayer/selector/TrackSelectorManager.java` | Preferred audio language | Keep method |
| `common/src/main/res/values/strings.xml` | `plex_*` strings | Keep keys |
| `smarttubetv/.../PlaybackFragment.java` | Subtitle/audio open APIs | Keep overrides |
| `smarttubetv/.../EmbedPlayerView.java` | Same | Keep overrides |
| `smarttubetv/.../StoryboardManager.java` | `MediaSourceRegistry` | Keep registry |
| `common/.../ChannelGroupServiceWrapper.java` | Registry / service access | Keep registry |
| ~20 presenters/controllers/misc | `MediaSourceRegistry.getServiceManager()` | Re-apply if upstream uses `YouTubeServiceManager` again |

### SmartTube — fork-only Java (examples)

`MediaSourceRegistry`, `SidebarSectionRegistry`, `PlexPlaybackHelper`, `PlexBrowsePresenter`, `PlexSignInPresenter`, `PlexServerSelectionPresenter`, `PlexSettingsPresenter`, `PlexDisabledError`, `PlexSignInError`, `PlexMessageError`.

### leanbackassistant

Still calls `YouTubeServiceManager` directly (circular dep blocks `MediaSourceRegistry`). Leave as-is unless Phase 5.3 finds a safe path.

## Submodule Pointer Update

After merging MediaServiceCore or SharedModules:

```bash
cd /path/to/SmartTube
git add MediaServiceCore SharedModules
git commit -m "chore: update submodule pointers after upstream merge"
```

## Post-Merge Checklist

- [ ] All repos: no unresolved conflicts (`git diff --check`)
- [ ] Submodule pointers committed in SmartTube
- [ ] `./gradlew assembleStbetaDebug` succeeds
- [ ] `fork-docs/CHANGELOG.md` updated if notable upstream changes merged
- [ ] `MediaServiceCore/CHANGELOG_FORK.md` updated if MSC fork files touched
- [ ] Fork Touch Points section still accurate if new upstream hooks were added

## Branch Notes

| Repo | Fork branch | Upstream branch |
|------|-------------|-----------------|
| SmartTube | `main` | `master` |
| MediaServiceCore | `master` | `master` |
| SharedModules | `master` | `master` |

SharedModules may start on detached HEAD — script checks out `master` automatically.

## Short Commands

Routed via [fork-commands.mdc](../../rules/fork-commands.mdc):

| Command | Effect |
|---------|--------|
| `sync yuliskov` | Full workflow (this skill) |
| `upstream status` | `--fetch-only` only |
| `continue merge` | `--continue` after conflict resolution |
