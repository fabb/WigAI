# Git Workflow (BMAD Cycles)

This repo keeps `main` protected and deploys on every merge to `main`. Day-to-day work lands in a cycle integration branch and is promoted to `main` at cycle completion via PR.

## Branch Types

1. `main` (protected)
   - Purpose: Production-ready code only.
   - Merges: Only from `develop/cycle-<n>` at cycle completion (PR required).

2. `develop/cycle-<n>` (protected; current cycle integration)
   - Purpose: Integration branch for the current cycle’s work.
   - Created from: `main` at cycle start.
   - Merges: Accepts PRs from `analysis/*`, `planning/*`, `solutioning/*`, `implementation/*`, and `docs/*`.
   - Promoted: PR `develop/cycle-<n>` → `main` when the cycle is complete.
   - Lifespan: Duration of the cycle; delete after merging to `main`.

3. `analysis/cycle-<n>-<topic>`
   - Purpose: BMAD analysis/discovery work that clarifies the problem and constraints (investigation notes, spikes, repros).
   - Examples:
     - `analysis/cycle-2-midi-routing-spike`
     - `analysis/cycle-2-api-capabilities-audit`
   - Created from: `develop/cycle-<n>`
   - Merge to: `develop/cycle-<n>` via PR; delete after merge.

4. `planning/cycle-<n>-<topic>`
   - Purpose: BMAD planning artifacts (PRD, epics/stories, acceptance criteria as docs).
   - Examples:
     - `planning/cycle-2-prd`
     - `planning/cycle-2-stories`
   - Created from: `develop/cycle-<n>`
   - Merge to: `develop/cycle-<n>` via PR; delete after merge.

5. `solutioning/cycle-<n>-<topic>`
   - Purpose: BMAD solutioning work that turns requirements into a buildable plan (architecture decisions, diagrams, prototypes).
   - Examples:
     - `solutioning/cycle-2-architecture`
     - `solutioning/cycle-2-mcp-transport-design`
   - Created from: `develop/cycle-<n>`
   - Merge to: `develop/cycle-<n>` via PR; delete after merge.

6. `implementation/<id>-<short-name>` (single story / small slice)
   - Purpose: Implementation of one story-sized change (code + tests).
   - Examples:
     - `implementation/123-audio-input-routing`
     - `implementation/BMM-42-mcp-transport-control`
   - Created from: `develop/cycle-<n>`
   - Merge to: `develop/cycle-<n>` via PR; delete after merge.

7. `docs/<short-name>` (optional)
   - Purpose: Doc-only changes not tied to a specific story.
   - Created from: `develop/cycle-<n>`
   - Merge to: `develop/cycle-<n>` via PR; delete after merge.

8. `hotfix/<id>-<short-name>`
   - Purpose: Urgent production fixes.
   - Created from: `main`
   - Merge to: `main` via PR (deploys), then cherry-pick into active `develop/cycle-<n>` if the cycle is still in progress.

## PR Targeting Rules

1. All `analysis/*`, `planning/*`, `solutioning/*`, `implementation/*`, and `docs/*` PRs target `develop/cycle-<n>`.
2. Only the cycle completion PR targets `main` (`develop/cycle-<n>` → `main`).
3. If a fix must ship immediately, use `hotfix/*` into `main`, then backport to the active cycle branch.

## Merge Strategy

1. `analysis/*` / `planning/*` / `solutioning/*` / `implementation/*` / `docs/*` → `develop/cycle-<n>`: Squash merge recommended.
2. `develop/cycle-<n>` → `main`: Squash merge or a merge commit are both acceptable.
   - If you want one clean commit per cycle, use squash.
   - If you want the PR boundary to show the cycle explicitly, use a merge commit.

## Protection Rules (Recommended)

1. `main`
   - Require PR approval
   - Require status checks to pass
   - Require branches to be up to date before merging
   - No force pushes
   - No deletions

2. `develop/cycle-<n>`
   - Same as `main` (prevents the cycle branch from becoming a bypass)

## Naming and Cycle Artifacts

If you keep cycle artifacts under `docs/sprint-artifacts/archive/cycle-<n>-<YYYY-MM-DD>/`, consider using the same `<n>` and date in your cycle branch name (e.g., `develop/cycle-2`) and record the cycle start date in the planning PR description or `docs/sprint-artifacts/sprint-status.yaml`.

## Workflow Example: Starting Cycle 2

```bash
# 1. Start new cycle from main
git checkout main
git pull
git checkout -b develop/cycle-2

# 2. Push cycle branch
git push -u origin develop/cycle-2

# 3. (Optional) Analysis / discovery
git checkout -b analysis/cycle-2-midi-routing-spike
# ... investigate, take notes, maybe build a tiny spike ...
git add docs/
git commit -m "docs: add cycle 2 analysis notes"
git push -u origin analysis/cycle-2-midi-routing-spike
# Create PR: analysis/cycle-2-midi-routing-spike → develop/cycle-2

# 4. Planning: PRD and stories
git checkout develop/cycle-2
git pull
git checkout -b planning/cycle-2-prd
# ... work on PRD ...
git add docs/prd/
git commit -m "docs: add cycle 2 PRD"
git push -u origin planning/cycle-2-prd
# Create PR: planning/cycle-2-prd → develop/cycle-2

# 5. After PRD approved and merged, plan stories (as docs)
git checkout develop/cycle-2
git pull
git checkout -b planning/cycle-2-stories
# ... write/update stories and acceptance criteria ...
git add docs/stories/
git commit -m "docs: add cycle 2 stories"
git push -u origin planning/cycle-2-stories
# Create PR: planning/cycle-2-stories → develop/cycle-2

# 6. Solutioning: architecture/design (if needed)
git checkout develop/cycle-2
git pull
git checkout -b solutioning/cycle-2-architecture
# ... architecture notes/diagrams/ADRs ...
git add docs/
git commit -m "docs: add cycle 2 architecture"
git push -u origin solutioning/cycle-2-architecture
# Create PR: solutioning/cycle-2-architecture → develop/cycle-2

# 7. Implement a story-sized slice
git checkout develop/cycle-2
git pull
git checkout -b implementation/BMM-42-transport-control
# ... implement ...
git add -A
git commit -m "feat: add transport control"
git push -u origin implementation/BMM-42-transport-control
# Create PR: implementation/BMM-42-transport-control → develop/cycle-2

# 8. When all work in cycle 2 is done
git checkout develop/cycle-2
git pull
# Create PR: develop/cycle-2 → main
# Title: "Cycle 2 Complete: <high-level summary>"
```
