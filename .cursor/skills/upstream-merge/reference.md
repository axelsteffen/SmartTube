# Upstream Merge — Conflict Reference

## Conflict Resolution Rules

| Situation | Resolution |
|-----------|------------|
| Fork-only file (e.g. `openapi-youtube-api-in-code.yaml`) | Keep fork version entirely |
| Fork addition in shared file (e.g. category field in `MediaItemFormatInfo`) | Keep fork addition; merge upstream changes around it |
| Upstream bugfix in `youtubeapi/` | Accept upstream fix; re-apply fork additions if needed |
| Upstream refactor in `common/` or `smarttubetv/` | Accept upstream; re-apply fork hooks (sidebar, registry) |
| Submodule pointer in SmartTube | Use merged MSC/SharedModules commit SHA |
| Unsure | Ask user; prefer smaller diff |

## Known Fork Touch Points

### MediaServiceCore (submodule)

| File / area | Fork change |
|-------------|-------------|
| `mediaserviceinterfaces/.../MediaItemFormatInfo.java` | Video category field |
| `youtubeapi/.../VideoInfo.java` | Category parsing |
| `youtubeapi/.../PlayerResult.kt` | Category from player |
| `youtubeapi/.../MediaItemFormatInfoImpl.kt` | Category enrichment |
| `youtubeapi/openapi-youtube-api-in-code.yaml` | Fork-only OpenAPI spec |

### SmartTube (main repo)

| File / area | Fork change |
|-------------|-------------|
| `fork-docs/` | Fork documentation and scripts |
| `.cursor/skills/upstream-merge/` | Merge skill |
| `.cursor/rules/changelog-fork.mdc` | Updated changelog paths |

Future fork hooks (planned — Plex milestone):
- `MediaSourceRegistry`, sidebar extensions in `common/`

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
