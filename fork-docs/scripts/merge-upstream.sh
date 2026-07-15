#!/usr/bin/env bash
# merge-upstream.sh — Fetch and merge upstream (yuliskov) into this SmartTube fork.
# Stops on conflicts for AI-assisted resolution via the upstream-merge Cursor skill.
#
# Usage:
#   ./fork-docs/scripts/merge-upstream.sh [--fetch-only] [--all|--repo NAME] [--continue] [--force-dirty]
#
# Safety: no push, no force, no git config changes.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
HINT_DOC="${ROOT_DIR}/fork-docs/architecture/UPSTREAM_MERGE.md"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

FETCH_ONLY=false
SINGLE_REPO=""
CONTINUE=false
FORCE_DIRTY=false
REPOS_ORDER=(sharedmodules mediaservicecore smarttube)

usage() {
    sed -n '2,12p' "$0" | sed 's/^# \?//'
    echo ""
    echo "Options:"
    echo "  --fetch-only     Fetch upstream and print divergence report only"
    echo "  --all            Merge all repos in order (default)"
    echo "  --repo NAME      Merge single repo: smarttube | mediaservicecore | sharedmodules"
    echo "  --continue       Continue merge sequence after conflict resolution"
    echo "  --force-dirty    Allow non-clean working tree"
    echo "  -h, --help       Show this help"
}

log_info()  { echo -e "${BLUE}[info]${NC} $*"; }
log_ok()    { echo -e "${GREEN}[ok]${NC} $*"; }
log_warn()  { echo -e "${YELLOW}[warn]${NC} $*"; }
log_error() { echo -e "${RED}[error]${NC} $*" >&2; }

while [[ $# -gt 0 ]]; do
    case "$1" in
        --fetch-only)   FETCH_ONLY=true; shift ;;
        --all)          shift ;;
        --repo)         SINGLE_REPO="${2:-}"; shift 2 ;;
        --continue)     CONTINUE=true; shift ;;
        --force-dirty)  FORCE_DIRTY=true; shift ;;
        -h|--help)      usage; exit 0 ;;
        *)              log_error "Unknown option: $1"; usage; exit 1 ;;
    esac
done

repo_path() {
    case "$1" in
        smarttube)         echo "${ROOT_DIR}" ;;
        mediaservicecore)  echo "${ROOT_DIR}/MediaServiceCore" ;;
        sharedmodules)     echo "${ROOT_DIR}/SharedModules" ;;
        *)                 log_error "Unknown repo: $1"; exit 1 ;;
    esac
}

repo_branch() {
    case "$1" in
        smarttube)         echo "main" ;;
        *)                 echo "master" ;;
    esac
}

check_clean_tree() {
    local dir="$1" name="$2"
    if ! git -C "$dir" diff --quiet 2>/dev/null || ! git -C "$dir" diff --cached --quiet 2>/dev/null; then
        if [[ "$FORCE_DIRTY" == true ]]; then
            log_warn "${name}: working tree not clean (--force-dirty)"
        else
            log_error "${name}: working tree not clean. Commit or stash, or use --force-dirty."
            git -C "$dir" status -sb
            exit 1
        fi
    fi
}

ensure_branch() {
    local dir="$1" branch="$2" name="$3" current
    current="$(git -C "$dir" symbolic-ref -q --short HEAD 2>/dev/null || true)"
    if [[ -z "$current" ]]; then
        log_warn "${name}: detached HEAD — checking out ${branch}"
        git -C "$dir" checkout "$branch"
    elif [[ "$current" != "$branch" ]]; then
        log_warn "${name}: on branch ${current}, expected ${branch}"
    fi
}

fetch_upstream() {
    local dir="$1" name="$2"
    log_info "${name}: fetching upstream..."
    if git -C "$dir" remote get-url upstream &>/dev/null; then
        git -C "$dir" fetch upstream --prune
    else
        log_warn "${name}: no upstream remote — fetching origin"
        git -C "$dir" fetch origin --prune
    fi
    git -C "$dir" remote get-url origin &>/dev/null && git -C "$dir" fetch origin --prune 2>/dev/null || true
}

divergence_report() {
    local dir="$1" name="$2" branch="$3" upstream_ref="upstream/master"
    git -C "$dir" rev-parse --verify "${upstream_ref}" &>/dev/null || upstream_ref="origin/master"
    git -C "$dir" rev-parse --verify "${upstream_ref}" &>/dev/null || { log_warn "${name}: no upstream ref"; return; }
    local behind ahead
    behind="$(git -C "$dir" rev-list --count HEAD.."${upstream_ref}" 2>/dev/null || echo "?")"
    ahead="$(git -C "$dir" rev-list --count "${upstream_ref}"..HEAD 2>/dev/null || echo "?")"
    echo ""
    echo "=== ${name} (${branch}) ==="
    echo "  Path: ${dir}"
    echo "  Behind upstream: ${behind} commit(s)"
    echo "  Ahead of upstream: ${ahead} commit(s)"
    echo "  Recent upstream commits not in HEAD:"
    git -C "$dir" log --oneline HEAD.."${upstream_ref}" 2>/dev/null | head -5 | sed 's/^/    /' || true
}

do_merge() {
    local dir="$1" name="$2" branch="$3" upstream_ref="upstream/master"
    git -C "$dir" rev-parse --verify "${upstream_ref}" &>/dev/null || upstream_ref="origin/master"
    log_info "${name}: merging ${upstream_ref} into ${branch}..."
    if git -C "$dir" merge "${upstream_ref}" --no-edit; then
        log_ok "${name}: merge successful"
        [[ "$name" == "MediaServiceCore" || "$name" == "SharedModules" ]] && \
            log_info "Remember to commit updated submodule pointer in SmartTube."
        return 0
    fi
    log_error "${name}: merge conflicts detected"
    echo "Conflicted files:"
    git -C "$dir" diff --name-only --diff-filter=U 2>/dev/null | sed 's/^/  /'
    echo ""
    echo "Resolve, then: cd ${dir} && git add -A && git commit --no-edit"
    echo "Continue: ${SCRIPT_DIR}/merge-upstream.sh --continue"
    echo "See: ${HINT_DOC}"
    return 1
}

process_repo() {
    local name="$1" dir branch display_name
    dir="$(repo_path "$name")"
    branch="$(repo_branch "$name")"
    case "$name" in
        smarttube) display_name="SmartTube" ;;
        mediaservicecore) display_name="MediaServiceCore" ;;
        sharedmodules) display_name="SharedModules" ;;
    esac
    [[ -d "${dir}/.git" || -f "${dir}/.git" ]] || { log_error "${display_name}: not a git repo"; exit 1; }
    check_clean_tree "$dir" "$display_name"
    ensure_branch "$dir" "$branch" "$display_name"
    fetch_upstream "$dir" "$display_name"
    divergence_report "$dir" "$display_name" "$branch"
    [[ "$FETCH_ONLY" == true ]] && return 0
    [[ -f "${dir}/.git/MERGE_HEAD" ]] && { log_warn "${display_name}: merge in progress"; return 1; }
    do_merge "$dir" "$display_name" "$branch"
}

cd "${ROOT_DIR}"
log_info "SmartTube fork upstream merge helper"
log_info "Root: ${ROOT_DIR}"
echo ""

if [[ -n "$SINGLE_REPO" ]]; then REPOS_TO_RUN=("$SINGLE_REPO"); else REPOS_TO_RUN=("${REPOS_ORDER[@]}"); fi

FAILED=false
for repo in "${REPOS_TO_RUN[@]}"; do
    if [[ "$CONTINUE" == true ]]; then
        dir="$(repo_path "$repo")"
        [[ -f "${dir}/.git/MERGE_HEAD" ]] && { log_warn "Unresolved merge in ${repo}"; FAILED=true; break; }
        upstream_ref="upstream/master"
        git -C "$dir" rev-parse --verify "${upstream_ref}" &>/dev/null || upstream_ref="origin/master"
        behind="$(git -C "$dir" rev-list --count HEAD.."${upstream_ref}" 2>/dev/null || echo "0")"
        [[ "$behind" == "0" ]] && { log_info "Skipping ${repo} (up to date)"; continue; }
    fi
    process_repo "$repo" || { FAILED=true; break; }
done

echo ""
[[ "$FETCH_ONLY" == true ]] && { log_ok "Fetch-only report complete."; exit 0; }
[[ "$FAILED" == true ]] && { log_error "Merge stopped."; exit 1; }
log_ok "All requested merges completed."
echo "Post-merge: commit submodule pointers, ./gradlew assembleStbetaDebug, update fork-docs/CHANGELOG.md"
exit 0
