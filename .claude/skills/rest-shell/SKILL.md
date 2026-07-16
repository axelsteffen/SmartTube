---
name: rest-shell
description: >-
  Use rest-shell to explore and call REST APIs via DSL (dot notation, schemas,
  variables, auth) and CLI commands (connect, eval, run, shell, session).
  Use when writing or editing .rest scripts, running rest CLI commands,
  debugging API sessions, or preferring rest-shell over curl/httpie in this
  project.
---

# rest-shell

REST DSL shell for zsh/bash. Prefer `rest` over raw HTTP clients when exploring APIs or authoring API workflows in this repo.

Full docs: `docs/en/` (English) · `docs/de/` (Deutsch). Terminal help: `rest help` · `rest help <command>`.

## When to use

| Goal | Approach |
|------|----------|
| One-off request | `rest eval '…'` |
| Multi-step workflow | `.rest` file + `rest run` |
| Interactive exploration | `rest shell` |
| Inspect state | `rest session` |

Session data lives in `~/.config/rest-shell/session.json` (connections, schemas, variables).

## CLI essentials

```bash
rest connect <url> [--name api]
rest disconnect <name>
rest eval '<expr>' [--json] [--quiet]
rest run <file.rest> [--json]
rest shell          # or: rest-repl
rest session
rest schema rm <name>
rest help [command]
```

Always quote DSL in the shell (`'…'`). In the REPL, quoting is not required.

Auth is **not** a CLI flag — set it in the DSL `Rest.connect` call.

## DSL cheat sheet

```rest
# Connection
api = Rest.connect('https://api.example.com')
api = Rest.connect('https://api.example.com', auth: 'bearer $API_TOKEN')
api = Rest.connect('https://api.example.com', auth: 'x-api-key $API_KEY')

# GET
api.users                    # GET /users
api.users.id = 1             # GET /users/1
api.users.id = 1.posts       # GET /users/1/posts

# POST (JSON RHS)
api.posts = {title: "Hi", body: "World", userId: 1}

# Field projection
api.users[ id, name ]
user[name, email]

# Schema (validates + formats; name inferred from path)
api.users
Users {
  id: number
  name: string
  email: string
  phone?: string
  role: enum(admin, user)
  tags: string[]
}

# Variables
user = api.users.id = 1
user.name
user[name, email]
```

### Auth formats

| Format | DSL |
|--------|-----|
| Bearer | `auth: 'bearer $TOKEN'` |
| Custom header | `auth: 'x-api-key $KEY'` |
| Explicit API key | `auth: 'apikey x-api-token $TOKEN'` |
| JWT from saved var | `auth: 'bearer $login.token'` or `${login.token}` |

Resolve secrets from env vars or saved variables — never hardcode tokens in scripts or `session.json`.

### Schema name inference

| Request | Schema name |
|---------|--------------|
| `api.users` | `Users` |
| `api.users.id = 1` | `User` |
| `api.posts` | `Posts` |

Types: `string` · `number` · `boolean` · `object` · `string[]` · `field?` · `enum(a, b)`.

## Agent workflow

1. Ensure the binary is available (`npm run build && npm link` if needed).
2. Connect (CLI or DSL), then `rest session` to confirm.
3. Explore with `rest eval`; add schemas once the shape is known.
4. For repeatable flows, write a `.rest` file and `rest run` it.
5. Use `--json` when the caller needs machine-readable output; `--quiet` to suppress save/connect chatter.
6. After login/register responses, save to a variable and wire `auth: 'bearer $var.token'`.

## Script conventions

- One concern per `.rest` file; start with `Rest.connect` (or assume an existing session connection).
- Prefer `#` comments; blank lines between statements are fine.
- Do not combine field projection `[…]` with assignment `=` on the same expression.
- Example: `examples/jsonplaceholder.rest`.

## Common errors

| Error | Fix |
|-------|-----|
| `Parse error` | Check DSL syntax / shell quoting |
| `Unknown connection` | `rest connect` or `Rest.connect(…)` |
| `Unknown variable` | Assign with `name = …` first |
| `HTTP 401` | Fix `auth` / env vars |
| Schema validation warnings | Non-fatal; adjust schema or response |

## Further reading

- [docs/en/dsl-reference.md](../../../docs/en/dsl-reference.md)
- [docs/en/cli-commands.md](../../../docs/en/cli-commands.md)
- [docs/en/authentication.md](../../../docs/en/authentication.md)
- [docs/en/schemas.md](../../../docs/en/schemas.md)
- [docs/en/variables.md](../../../docs/en/variables.md)
