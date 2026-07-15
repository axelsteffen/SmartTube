# Fork Documentation Folder (`fork-docs/`)

Implementation plan for fork-specific documentation, upstream-merge tooling, and Cursor integration.

## Context

- Fork-specific docs were not present at repo root (no `CHANGELOG_FORK.md`, no `MILESTONE_*.md`).
- Cursor rules in `.cursor/rules/changelog-fork.mdc` and `.cursor/rules/milestone-progress.mdc` referenced root-level paths — updated to `fork-docs/`.
- **MediaServiceCore** is a separate git submodule (`.gitmodules`); its fork changelog stays **inside the submodule** (`MediaServiceCore/CHANGELOG_FORK.md`) so it travels with that repo. The main `fork-docs/` folder links to it, not replaces it.

## Target Structure

```text
fork-docs/
├── README.md                          # Index, conventions, links to submodules
├── CHANGELOG.md                       # Main SmartTube fork changelog
├── PLAN_FORK_DOCS.md                  # This plan document
├── milestones/
│   └── MILESTONE_PLEX_INTEGRATION.md  # First milestone
├── architecture/
│   ├── README.md                      # Design notes stub
│   └── UPSTREAM_MERGE.md              # Merge order, remotes, conflict hotspots
└── scripts/
    └── merge-upstream.sh              # Fetch + merge helper (stops on conflicts)

.cursor/skills/upstream-merge/
├── SKILL.md                           # Agent workflow for AI-assisted upstream merges
└── reference.md                       # Conflict resolution guide, fork touch points
```

## Upstream Merge Tooling

### Script: `fork-docs/scripts/merge-upstream.sh`

- `--all` — merge all three repos in order (default)
- `--repo smarttube|mediaservicecore|sharedmodules` — single repo
- `--fetch-only` — fetch upstream, print divergence report, no merge
- `--continue` — after conflict resolution, continue remaining repos
- `--force-dirty` — allow non-clean working tree

Merge order: **SharedModules → MediaServiceCore → SmartTube** (submodules before parent).

On conflict: stop, print conflict file list, exit non-zero — agent resolves via skill.

Safety: no `git push`, no `--force`, no config changes, no hook skip.

### Skill: `.cursor/skills/upstream-merge/`

Trigger: user asks to merge upstream, sync with yuliskov, update fork from upstream.

Agent workflow:

1. Read `fork-docs/architecture/UPSTREAM_MERGE.md`
2. Run `fork-docs/scripts/merge-upstream.sh --fetch-only` first
3. Run merge (with user confirmation): `--all` or per-repo
4. On conflicts: read conflict files, use `reference.md` + `fork-docs/CHANGELOG.md`
5. Resolve conflicts (keep fork additions, accept upstream fixes elsewhere)
6. Run `./gradlew` smoke build if merge completes
7. Update changelogs; commit submodule pointer in SmartTube

## Cursor Rules Updates

| Rule file | Old reference | New reference |
|-----------|---------------|---------------|
| `changelog-fork.mdc` | `CHANGELOG_FORK.md` (root) | `fork-docs/CHANGELOG.md` |
| `changelog-fork.mdc` | — | `fork-docs/milestones/` for milestone progress |
| `milestone-progress.mdc` | `MILESTONE_*.md` at root | `fork-docs/milestones/MILESTONE_*.md` |

## Out of Scope

- No application code or Gradle changes (except merge script)
- No Plex implementation (documentation only)
- No automatic push to origin/upstream
