# Upstream Merge Guide

How to merge changes from upstream (yuliskov) into this fork.

## Repository Topology

| Repo | Path | Fork remote | Upstream remote | Working branch | Upstream branch |
|------|------|-------------|-----------------|----------------|-----------------|
| SmartTube | `.` | `origin` → axelsteffen/SmartTube | `upstream` → yuliskov/SmartTube | `main` | `master` |
| MediaServiceCore | `MediaServiceCore/` | `origin` → axelsteffen/MediaServiceCore | `upstream` → yuliskov/MediaServiceCore | `master` | `master` |
| SharedModules | `SharedModules/` | `origin` → yuliskov/SharedModules | _(same, no separate fork)_ | `master` | `master` |

## Merge Order

Always merge **submodules before the parent repo**:

```text
1. SharedModules
2. MediaServiceCore
3. SmartTube  (+ commit updated submodule pointers)
```

## Tooling

| Tool | Path |
|------|------|
| Merge script | [../scripts/merge-upstream.sh](../scripts/merge-upstream.sh) |
| Cursor skill | [.cursor/skills/upstream-merge/SKILL.md](../../.cursor/skills/upstream-merge/SKILL.md) |
| Conflict reference | [.cursor/skills/upstream-merge/reference.md](../../.cursor/skills/upstream-merge/reference.md) |

### Typical Workflow

```bash
# Step 1: See how far behind upstream we are
./fork-docs/scripts/merge-upstream.sh --fetch-only

# Step 2: Merge (stops on conflicts)
./fork-docs/scripts/merge-upstream.sh --all

# Step 3: After resolving conflicts in a repo
./fork-docs/scripts/merge-upstream.sh --continue
```

Use the **upstream-merge** Cursor skill for AI-assisted conflict resolution.

## Known Conflict Hotspots

| Area | Risk | Resolution hint |
|------|------|-----------------|
| `MediaServiceCore/mediaserviceinterfaces/` | High | Keep fork additions (e.g. category field); merge upstream interface changes carefully |
| `MediaServiceCore/youtubeapi/` | Medium | Prefer upstream bugfixes; preserve fork-only files (OpenAPI yaml) |
| `common/`, `smarttubetv/` | Low–Medium | Keep fork hooks; accept upstream feature/fix changes |
| Submodule pointers in SmartTube | Expected | After MSC/SM merge, commit new SHAs in parent repo |

## Post-Merge Checklist

- [ ] All three repos merged without unresolved conflicts
- [ ] Submodule pointers committed in SmartTube (`MediaServiceCore`, `SharedModules`)
- [ ] `./gradlew assembleStbetaDebug` (or similar) builds successfully
- [ ] Update [CHANGELOG.md](../CHANGELOG.md) if upstream brought notable changes
- [ ] Update [MediaServiceCore/CHANGELOG_FORK.md](../../MediaServiceCore/CHANGELOG_FORK.md) if MSC fork files were touched during conflict resolution

## Safety Rules

- Never `git push --force` to `main`/`master`
- Never skip hooks (`--no-verify`) unless explicitly requested
- Never change git config
- Script does not push — push manually after verification
