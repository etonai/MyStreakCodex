# Development Cycle 2026-013

**Status:** Open
**Start Date:** 2026-05-18
**Target Completion:** TBD
**Focus:** Minor UI wording cleanup

## Overview

This development cycle handles a small set of wording and detail-display tweaks across the main MyStreak tabs. The goal is to make routine/task labels read naturally and remove low-value text from repeated UI rows.

## Product Definition

### Current Behavior
- The fourth main tab is labeled "Routine".
- Task and routine rows show a "# today" count.
- Routine activity rows on Dashboard and Calendar show "Done" as their detail text.

### New Behavior
- The fourth main tab is labeled "Routines".
- Task and routine rows show total activity count in the format `Total Activities: #`.
- Routine activity rows on Dashboard and Calendar do not show "Done" or replacement detail text.

## Current Work Items

### Phase 1: Rename Routine Tab
**Status:** Open
**Date Added:** 2026-05-18
**Priority:** Low
**Description:** Change the main tab label from "Routine" to "Routines".

**Acceptance Criteria:**
- [ ] The tab label reads "Routines".
- [ ] Navigation behavior is unchanged.

### Phase 2: Replace Today Count on Task and Routine Rows
**Status:** Open
**Date Added:** 2026-05-18
**Priority:** Low
**Description:** Replace the per-row "# today" text with total activity count text.

**Technical Requirements:**
- Update rows in the Tasks tab.
- Update rows in the Routines tab.
- Use the format `Total Activities: #`.
- Remove or stop using the "# today" row text.

**Acceptance Criteria:**
- [ ] Task rows show `Total Activities: #`.
- [ ] Routine rows show `Total Activities: #`.
- [ ] Rows no longer show "# today".

### Phase 3: Remove Routine "Done" Detail Text
**Status:** Open
**Date Added:** 2026-05-18
**Priority:** Low
**Description:** Routine activity rows should not show the redundant detail text "Done".

**Technical Requirements:**
- Update Dashboard activity rows for routines.
- Update Calendar selected-date activity rows for routines.
- Do not replace "Done" with any other text.
- Standard task activity detail text should remain unchanged.

**Acceptance Criteria:**
- [ ] Routine activity rows on Dashboard do not show "Done".
- [ ] Routine activity rows on Calendar do not show "Done".
- [ ] Routine activity rows do not show replacement detail text.
- [ ] Standard task activity rows still show their success detail text.

### Phase 4: Verification
**Status:** Open
**Date Added:** 2026-05-18
**Priority:** Low
**Description:** Build and verify the small UI changes.

**Acceptance Criteria:**
- [ ] `gradlew.bat assembleDebug` succeeds.
- [ ] No unrelated behavior changes are introduced.

## Proposed Implementation Sequence

1. Update the tab label source for "Routine" to "Routines".
2. Update task/routine row binding to show total activity count instead of today's count.
3. Update routine activity row binding to hide the secondary detail text.
4. Build and mark this dev cycle complete when verified.

## Implementation Notes

- This cycle should not alter task counts, routine completion behavior, or calendar day color calculations.
- The Dashboard activity detail dialog can keep showing routine notes/date information; this cycle only targets the visible row detail text.
- If the activity row layout leaves vertical space when the routine detail is hidden, set the detail view visibility appropriately rather than replacing the text with an empty-looking placeholder.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Hiding routine detail text also hides standard task details | Gate the change on `TaskKind.ROUTINE`. |
| Row wording becomes duplicated | Reuse the existing activity count value and remove the today-count display. |
| Tab label is updated in one place but not another | Search for both "Routine" tab labels and navigation/title uses before implementation. |

## Handoff Checklist

- [ ] Confirm exact total activity wording: `Total Activities: #`.
- [ ] Confirm routine row secondary detail should be hidden, not blank text.
