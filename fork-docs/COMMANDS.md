# Fork Commands — Quick Reference

Short commands for steering the agent. Say any alias in chat.

The agent matches these via [`.cursor/rules/fork-commands.mdc`](../.cursor/rules/fork-commands.mdc).

---

## Upstream / Merge

| Say | Action |
|-----|--------|
| **`sync yuliskov`** | Full upstream-merge workflow (fetch report → merge with confirmation) |
| `sync with yuliskov`, `merge upstream`, `upstream sync`, `pull upstream` | _(same)_ |
| **`upstream status`** | Divergence report only — no merge |
| `behind upstream`, `fork status`, `upstream report` | _(same)_ |
| **`continue merge`** | Resume after conflict resolution |
| `merge continue`, `conflicts resolved` | _(same)_ |

Skill: [`.cursor/skills/upstream-merge/SKILL.md`](../.cursor/skills/upstream-merge/SKILL.md)

---

## Milestones

| Say | Action |
|-----|--------|
| **`plex status`** | Show Plex milestone progress |
| **`next step`** | Outline next open milestone step (discuss before code) |
| `nächster schritt`, `continue milestone` | _(same)_ |

---

## Git (Conventional Commits)

| Say | Action |
|-----|--------|
| **`commit`** | Stage + commit with Conventional Commits message (no push) |
| `git commit`, `commit changes`, `commit it` | _(same)_ |
| **`push`** | Push to origin (MSC submodule first if needed) |
| `git push`, `push changes`, `push it` | _(same)_ |
| **`commit push`** | Commit then push in one workflow |
| `commit and push`, `ship`, `ship it` | _(same)_ |

Skill: [`.cursor/skills/fork-git/SKILL.md`](../.cursor/skills/fork-git/SKILL.md)

**Message format:** `type(scope): subject` — e.g. `docs(fork-docs): add command router`

Types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `build`

---

## Documentation

| Say | Action |
|-----|--------|
| **`log change`** | Update fork changelog(s) |
| `changelog`, `fork changelog` | _(same)_ |
| **`fork help`** | Show this command list |
| `commands`, `hilfe fork` | _(same)_ |

---

## Examples

```text
sync yuliskov          → fetch report, then merge (with confirmation)
upstream status        → how many commits behind upstream?
continue merge         → pick up after fixing conflicts
plex status            → milestone progress table
next step              → what's the next Plex task?
log change             → add entry to fork-docs/CHANGELOG.md
commit                 → conventional commit (no push)
push                   → push to origin
commit push            → commit then push
```
