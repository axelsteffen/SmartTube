# Milestone: Plex Home Rows & Sidebar Position

## Goal

Restructure the Plex browse section into a Home-like layout (Continue Watching, Watchlist, Recently Added, Grid + Hub recommendations) and place the Plex sidebar entry directly under **Home / Startseite**.

## Starting Point / Current State

| Area | Status |
|------|--------|
| Plex sidebar (`TYPE_PLEX = 100`) | Appended at end of sidebar via `SidebarSectionRegistry.appendExtraSections` |
| Library rows | One row per movie/show library: browse stub + first page of `/all` |
| PMS API | Sections, `/all`, metadata, children, decision, timeline — no onDeck / hubs / recentlyAdded |
| Discover Watchlist | Not implemented |

## Decisions

| Topic | Choice |
|-------|--------|
| Watchlist | Official-app style → Discover (`discover.provider.plex.tv`) |
| Recommended | Recently Added row **and** Hub recommendations |
| TV watchlist | No watchlist row for TV Shows |
| Empty rows | Skip emission |
| Multi-library same type | Merge On Deck / Recently Added; grid stub uses first library |

## Target Layout

**Sidebar:** Home → Plex → … (other sections)

**Movies block** (if ≥1 movie library):

1. Continue Watching — no grid stub  
2. Watchlist (Discover, movies) — no grid stub  
3. Recently Added — no grid stub  
4. Movies — grid stub + Hub recommendation items  

**TV-Shows block** (if ≥1 show library):

1. Continue Watching — no grid stub  
2. Recently Added — no grid stub  
3. TV Shows — grid stub + Hub recommendation items  

## Planned Improvements

1. Insert Plex after `MediaGroup.TYPE_HOME` when appending extra sections  
2. PMS endpoints: section `onDeck`, `recentlyAdded`, `hubs/sections/{id}`  
3. Discover watchlist client for movies  
4. Rewrite `PlexBrowsePresenter.getLibraryRowsObserve` emit order  
5. Row title strings + changelog  

## Implementation Steps

| Step | Description | Depends on |
|------|-------------|------------|
| 1 | Milestone doc (this file) | — |
| 2 | Sidebar insert after Home | 1 |
| 3 | PMS onDeck / recentlyAdded / hubs + adapters | 1 |
| 4 | Discover watchlist (movies) | 1 |
| 5 | Rebuild `getLibraryRowsObserve` + pagination | 3, 4 |
| 6 | Strings, CHANGELOG, graphify update | 2–5 |

## Affected Files

- `common/.../SidebarSectionRegistry.java`
- `common/.../PlexBrowsePresenter.java`
- `common/.../res/values/strings.xml` (+ de if present)
- `PlexServiceCore/plexapi/.../PlexPmsApi.java`, library service/impl, adapters, DTOs
- Discover Retrofit client under `PlexServiceCore/plexapi`
- `fork-docs/CHANGELOG.md`, `PlexServiceCore/CHANGELOG.md`

## Out of Scope

- Reordering an already user-moved Plex pin  
- Multi-library single grid for all movie sections  
- Full Discover Home parity beyond watchlist + section hubs  

## Progress

| Step | Status |
|------|--------|
| 1 Milestone doc | done |
| 2 Sidebar under Home | in progress |
| 3 PMS hubs API | open |
| 4 Discover watchlist | open |
| 5 Rewrite rows | open |
| 6 Strings / changelog | open |

## TV Context

Lazy, paged API calls only; no bulk caches; one hub/section failure must not blank the whole Plex tab.
