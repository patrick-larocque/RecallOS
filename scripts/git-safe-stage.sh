#!/usr/bin/env bash
set -euo pipefail

# Safe staging helper:
# - stages only allowlisted paths
# - aborts if unexpected local changes exist
# - reports dirty sibling worktrees and overlap risk

if [[ $# -lt 1 ]]; then
  echo "Usage: scripts/git-safe-stage.sh <path1> [path2 ...]"
  exit 64
fi

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

allow_file="$(mktemp)"
unexpected_file="$(mktemp)"
tracked_dirty_file="$(mktemp)"
other_worktrees_file="$(mktemp)"
cleanup() {
  rm -f "$allow_file" "$unexpected_file" "$tracked_dirty_file" "$other_worktrees_file"
}
trap cleanup EXIT

for p in "$@"; do
  printf "%s\n" "$p" >> "$allow_file"
done

# Current worktree dirty files (tracked + untracked), path-only output.
git status --porcelain=v1 -uall | sed -E 's#^.. ##' > "$tracked_dirty_file"

# Compute unexpected paths: any dirty path not exactly in allowlist and not under allowlisted dirs.
while IFS= read -r dirty_path; do
  [[ -z "$dirty_path" ]] && continue
  is_allowed=0
  while IFS= read -r allow_path; do
    [[ -z "$allow_path" ]] && continue
    if [[ "$dirty_path" == "$allow_path" ]]; then
      is_allowed=1
      break
    fi
    if [[ "$allow_path" == */ ]]; then
      if [[ "$dirty_path" == "$allow_path"* ]]; then
        is_allowed=1
        break
      fi
    fi
  done < "$allow_file"

  if [[ "$is_allowed" -eq 0 ]]; then
    printf "%s\n" "$dirty_path" >> "$unexpected_file"
  fi
done < "$tracked_dirty_file"

if [[ -s "$unexpected_file" ]]; then
  echo "ABORT: unexpected dirty files detected in current worktree:"
  sed 's#^#  - #' "$unexpected_file"
  echo
  echo "No files were staged."
  exit 2
fi

# Check sibling worktrees for dirty files and potential overlaps.
current_path="$(pwd)"
while IFS= read -r wt_path; do
  [[ -z "$wt_path" ]] && continue
  if [[ "$wt_path" == "$current_path" ]]; then
    continue
  fi
  if [[ ! -d "$wt_path" ]]; then
    continue
  fi

  wt_dirty="$(git -C "$wt_path" status --porcelain=v1 -uall | sed -E 's#^.. ##' || true)"
  if [[ -n "$wt_dirty" ]]; then
    echo "WORKTREE_DIRTY: $wt_path" >> "$other_worktrees_file"
    while IFS= read -r f; do
      [[ -z "$f" ]] && continue
      echo "  $f" >> "$other_worktrees_file"
    done <<< "$wt_dirty"
  fi
done < <(git worktree list --porcelain | awk '/^worktree / {print substr($0,10)}')

if [[ -s "$other_worktrees_file" ]]; then
  echo "NOTICE: dirty sibling worktrees found (review for overlap risk):"
  cat "$other_worktrees_file"
  echo
  echo "Continuing with staging only allowlisted files in this worktree."
fi

# Stage only allowlisted files.
git add -- "$@"

echo "Staged files:"
git diff --cached --name-only | sed 's#^#  - #'

echo
echo "Dry-run commit summary:"
git commit --dry-run --short
