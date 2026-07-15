# Changelog – Fork-specific changes (SmartTube)

Changes in this fork against [upstream SmartTube](https://github.com/yuliskov/SmartTube).

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased]

### fork-docs
- **fork-docs/** (new): Central folder for fork changelog, milestones, architecture notes, and maintenance scripts.
- **fork-docs/COMMANDS.md** (new): Quick reference for short agent commands (`sync yuliskov`, `plex status`, …).
- **.cursor/rules/fork-commands.mdc** (new): Command router — maps short commands to skills/workflows.
- **.cursor/skills/fork-git/** (new): Conventional Commits commit/push workflow.
- **.cursor/skills/upstream-merge/** (new): Cursor skill for AI-assisted upstream merges.
- **fork-docs/scripts/merge-upstream.sh** (new): Fetch and merge helper for SmartTube, MediaServiceCore, and SharedModules.

### common
- _(no changes yet)_

### smarttubetv
- _(no changes yet)_

### MediaServiceCore (summary)
- See [MediaServiceCore/CHANGELOG_FORK.md](../MediaServiceCore/CHANGELOG_FORK.md) for full detail.
- OpenAPI spec for in-code YouTube APIs (`openapi-youtube-api-in-code.yaml`).
- YouTube video category support in format info and interfaces.
