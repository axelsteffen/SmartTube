---
name: upstream-merge
description: >-
  Merges upstream SmartTube, MediaServiceCore, and SharedModules from yuliskov
  into this fork. Trigger commands: sync yuliskov, sync with yuliskov, merge
  upstream, upstream merge, upstream sync, pull upstream, upstream status,
  continue merge, behind upstream. Runs merge-upstream.sh, reports divergence,
  and helps resolve merge conflicts. Routed via fork-commands rule.
---

# Upstream Merge (SmartTube Fork)

AI-assisted workflow for merging upstream changes into this three-repo fork.

**Invoked via short commands:** `sync yuliskov`, `upstream status`, `continue merge` (see [fork-docs/COMMANDS.md](../../../fork-docs/COMMANDS.md) and [fork-commands rule](../../rules/fork-commands.mdc)).

## Before You Start

Read [fork-docs/architecture/UPSTREAM_MERGE.md](../../../fork-docs/architecture/UPSTREAM_MERGE.md) for topology and merge order.

Merge order: **SharedModules → MediaServiceCore → SmartTube**

## Workflow

### 1. Fetch and report (always first)

```bash
./fork-docs/scripts/merge-upstream.sh --fetch-only
```

Summarize for the user:
- Commits behind/ahead per repo
- Whether submodules are on detached HEAD
- Any dirty working trees

### 2. Confirm and merge

Ask user confirmation unless they explicitly requested merge.

```bash
./fork-docs/scripts/merge-upstream.sh --all
```

Single repo if requested:

```bash
./fork-docs/scripts/merge-upstream.sh --repo mediaservicecore
```

### 3. On conflicts

When the script exits non-zero:

1. Read [reference.md](reference.md) for fork touch points and resolution rules
2. Read each conflicted file — understand fork vs upstream intent
3. Propose resolution to user; apply after confirmation
4. In the conflicted repo:

```bash
git add -A && git commit --no-edit
```

5. Continue:

```bash
./fork-docs/scripts/merge-upstream.sh --continue
```

### 4. Post-merge

- Commit submodule pointer updates in SmartTube root if MSC/SharedModules SHAs changed
- Run smoke build: `./gradlew assembleStbetaDebug` (or user-preferred variant)
- Remind user to update [fork-docs/CHANGELOG.md](../../../fork-docs/CHANGELOG.md)
- If MSC fork files changed during resolution, update [MediaServiceCore/CHANGELOG_FORK.md](../../../MediaServiceCore/CHANGELOG_FORK.md)

## Safety Rules

- Never `git push --force` to main/master
- Never skip hooks unless user explicitly requests
- Never change git config
- Script does not push — user pushes manually after verification
- Do not commit unless user asks

## Key Paths

| Resource | Path |
|----------|------|
| Merge script | `fork-docs/scripts/merge-upstream.sh` |
| Merge guide | `fork-docs/architecture/UPSTREAM_MERGE.md` |
| Conflict reference | `.cursor/skills/upstream-merge/reference.md` |
| Fork changelog | `fork-docs/CHANGELOG.md` |
| MSC changelog | `MediaServiceCore/CHANGELOG_FORK.md` |
