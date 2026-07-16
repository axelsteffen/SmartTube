---
name: fork-git
description: >-
  Git commit and push for this SmartTube fork using Conventional Commits.
  Trigger commands: git commit, commit, commit changes, git push, push,
  push changes, commit push, commit and push, ship. Handles submodule
  commits (MediaServiceCore, PlexServiceCore) before parent repo. Routed via
  fork-commands rule.
---

# Fork Git — Commit & Push

Conventional Commits for this fork. Invoked via `commit`, `push`, `commit push` (see [fork-docs/COMMANDS.md](../../../fork-docs/COMMANDS.md)).

## Conventional Commits Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Types

| Type | Use for |
|------|---------|
| `feat` | New user-facing feature |
| `fix` | Bug fix |
| `docs` | Documentation only (fork-docs, skills, rules, changelogs) |
| `chore` | Tooling, scripts, submodule pointers, deps |
| `refactor` | Code change without feature/fix |
| `test` | Tests only |
| `build` | Gradle, build config |

### Scopes (examples)

| Scope | Area |
|-------|------|
| `fork-docs` | fork-docs/, skills, cursor rules |
| `msc` | MediaServiceCore submodule |
| `psc` | PlexServiceCore submodule |
| `common` | common module |
| `smarttubetv` | smarttubetv module |
| `plex` | Plex integration (SmartTube hooks) |
| `merge` | Upstream merge result |

- Scope is optional but preferred when clear.
- Subject: imperative mood, lowercase, no period, max ~72 chars.
- Body: explain **why**, not what (when needed).

### Examples

```
docs(fork-docs): add command router and upstream merge skill

chore(msc): update submodule pointer after upstream merge

feat(plex): add PlexServiceManager proof of concept

fix(common): restore playback after upstream merge conflict
```

---

## Commit Workflow

Run these **in parallel** first:

```bash
git status
git diff
git diff --staged
git log -5 --oneline
```

Also check submodules:

```bash
git status MediaServiceCore SharedModules PlexServiceCore
cd MediaServiceCore && git status -sb && git diff --stat
cd ../PlexServiceCore && git status -sb && git diff --stat
```

### Submodule order

If **MediaServiceCore** or **PlexServiceCore** has changes:

1. Commit inside the submodule first (`MediaServiceCore` → axelsteffen/MediaServiceCore; `PlexServiceCore` → axelsteffen/PlexServiceCore).
2. Then commit SmartTube root (includes updated submodule pointer(s)).

If only SmartTube root changed, commit root only.

### Steps

1. Analyze all staged/unstaged changes across root and submodules.
2. Draft **one Conventional Commit message per repo** that has changes.
3. Show message(s) to user briefly if ambiguous; otherwise proceed.
4. Stage relevant files (`git add` — never commit secrets).
5. Commit via HEREDOC:

```bash
git commit -m "$(cat <<'EOF'
type(scope): subject

Optional body explaining why.
EOF
)"
```

6. Verify: `git status`

### Safety (mandatory)

- NEVER update git config
- NEVER `--force`, `--hard`, skip hooks unless user explicitly requests
- NEVER `--amend` unless all amend conditions met (user rule)
- NEVER commit `.env`, credentials, secrets — warn user
- Do not commit unless user triggered `commit` command or explicitly asked

---

## Push Workflow

Only when user says `push`, `git push`, or `commit push`.

### Before push

```bash
git status -sb
git log origin/main..HEAD --oneline 2>/dev/null || git log origin/master..HEAD --oneline
```

Check submodules were pushed if committed:

```bash
cd MediaServiceCore && git status -sb
cd ../PlexServiceCore && git status -sb
```

### Push order

1. Push **MediaServiceCore** and/or **PlexServiceCore** first if they have unpushed commits:

```bash
cd MediaServiceCore && git push -u origin HEAD
cd ../PlexServiceCore && git push -u origin HEAD
```

2. Push **SmartTube** root:

```bash
git push -u origin HEAD
```

### Safety

- NEVER force-push to `main`/`master` — warn user if requested
- Do not push unless user triggered `push` or `commit push`

---

## Combined: `commit push`

1. Full commit workflow (all repos with changes).
2. Full push workflow.
3. Report commit SHAs and remote URLs.

---

## Multi-commit Strategy

Prefer **one commit per repo per logical change**. If changes span unrelated areas (e.g. docs + feature), ask user whether to split — default to single commit if clearly one task.

---

## Key Paths

| Resource | Path |
|----------|------|
| Fork changelog | `fork-docs/CHANGELOG.md` |
| MSC changelog | `MediaServiceCore/CHANGELOG_FORK.md` |
| Commands | `fork-docs/COMMANDS.md` |

After feature commits, remind user to run `log change` if changelog not yet updated.
