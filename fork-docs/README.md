# Fork Documentation

Fork-specific documentation for this SmartTube fork. Not part of upstream SmartTube.

## Directory Map

| Path | Purpose |
|------|---------|
| [COMMANDS.md](COMMANDS.md) | **Short commands** for steering the agent (`sync yuliskov`, `plex status`, …) |
| [CHANGELOG.md](CHANGELOG.md) | Main fork changelog (SmartTube repo) |
| [milestones/](milestones/) | One milestone document per feature |
| [architecture/](architecture/) | Design notes, merge strategy |
| [scripts/](scripts/) | Fork maintenance scripts |
| [PLAN_FORK_DOCS.md](PLAN_FORK_DOCS.md) | Implementation plan for this folder |

## Submodule Changelogs

| Submodule | Changelog |
|-----------|-----------|
| MediaServiceCore | [MediaServiceCore/CHANGELOG_FORK.md](../MediaServiceCore/CHANGELOG_FORK.md) |
| PlexServiceCore | [PlexServiceCore/CHANGELOG.md](../PlexServiceCore/CHANGELOG.md) (fork-only, no upstream) |

Summaries appear in [CHANGELOG.md](CHANGELOG.md) under the matching section.

## Conventions

- **Changelog:** Add entries under `[Unreleased]` in the relevant subproject section (`common`, `smarttubetv`, `MediaServiceCore` summary).
- **Milestones:** One file per feature: `milestones/MILESTONE_<SHORT_DESCRIPTION>.md`
- **Language:** Technical documentation is written in **English**.
- **Minimal upstream surface:** Prefer fork-only code; keep thin hooks in upstream files; mark with `// FORK:` … `// END FORK`. Rule: [`.cursor/rules/fork-upstream-minimal.mdc`](../.cursor/rules/fork-upstream-minimal.mdc).
- **Upstream merges:** Use the [upstream-merge skill](../.cursor/skills/upstream-merge/SKILL.md) and [merge-upstream.sh](scripts/merge-upstream.sh). See [architecture/UPSTREAM_MERGE.md](architecture/UPSTREAM_MERGE.md).

## Short Commands

See [COMMANDS.md](COMMANDS.md). Examples:

- `sync yuliskov` — merge upstream (AI-assisted)
- `upstream status` — report only
- `plex status` — milestone progress
- `fork help` — list all commands
- `commit` / `push` / `commit push` — git with Conventional Commits

## Upstream Merge (Quick Start)

```bash
# 1. Report only (no merge)
./fork-docs/scripts/merge-upstream.sh --fetch-only

# 2. Merge all repos (SharedModules → MediaServiceCore → SmartTube)
./fork-docs/scripts/merge-upstream.sh --all
```

On conflicts, ask the agent to help using the `upstream-merge` skill.
