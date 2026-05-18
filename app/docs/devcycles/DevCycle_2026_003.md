# Development Cycle 2026-003

**Status:** Completed
**Start Date:** 2026-05-07
**Target Completion:** TBD
**Focus:** Add priority-first alphabetical sorting to the Tasks tab

## Overview

This development cycle adds a new Tasks tab sort order for users who want high-priority tasks grouped first while still keeping each priority group easy to scan alphabetically. The new order should sort primarily by priority, with High Priority tasks before Low Priority tasks, and secondarily by task name from A to Z within each priority group.

This is a narrow Tasks tab usability improvement. It should not change task priority semantics, Dashboard high-priority calculations, activity logging, import/export schema, or calendar color behavior.

## Current Work Items

### Phase 1: Current Sort Inventory
**Status:** Completed
**Date Added:** 2026-05-07
**Priority:** Medium (Implementation Clarity)
**Description:** Identify the existing Tasks tab sort controls, enum values, default direction behavior, and visible chip labels before adding the new option.

**Technical Requirements:**
- Review `PiecesFragment` sorting controls and chip handling.
- Review `PiecesViewModel` `SortType`, `SortDirection`, and sorting implementation.
- Confirm current sort options:
  - Alphabetical
  - Last Activity Date
  - Activity Count
- Confirm how sort direction currently applies to each sort type.

**Acceptance Criteria:**
- [x] Existing sort behavior is documented before changes.
- [x] The desired behavior for the new sort option is unambiguous.

**Progress Notes:**
- 2026-05-07: Current Tasks tab sort options are Name, Date, and Activity. Name defaults A-Z; Date and Activity default descending.
- 2026-05-07: Priority sort defaults to High Priority A-Z, then Low Priority A-Z. The direction toggle reverses only the priority grouping; names remain A-Z within each group.

### Phase 2: Add Priority Sort Option
**Status:** Completed
**Date Added:** 2026-05-07
**Priority:** High (Feature Implementation)
**Description:** Add a new Tasks tab sort option that orders tasks by priority first and name second.

**Technical Requirements:**
- Add a new `SortType` value, likely `PRIORITY`.
- Add a new chip/control to the Tasks tab sort controls with user-facing copy such as `Priority`.
- Sort rules:
  - High Priority tasks appear before Low Priority tasks.
  - Tasks within the same priority are sorted alphabetically by normalized/lowercase name.
  - Alphabetical tie-breaks should be deterministic.
- Decide how the existing sort direction toggle applies:
  - Preferred behavior: priority sort always means High-to-Low priority, A-to-Z within each group.
  - If the existing direction toggle remains active, document whether descending should reverse both priority and name order or be disabled/ignored for this mode.

**Acceptance Criteria:**
- [x] Users can select the new Priority sort option from the Tasks tab.
- [x] High Priority tasks appear before Low Priority tasks.
- [x] Tasks are alphabetical within High and Low priority groups.
- [x] Sorting remains stable when task names differ only by case.
- [x] Existing sort options continue to behave as before.

**Progress Notes:**
- 2026-05-07: Added `SortType.PRIORITY` and a Priority chip to the Tasks tab sort controls.
- 2026-05-07: Priority sorting uses priority rank, lowercase task name, then task ID as a deterministic tie-breaker.
- 2026-05-07: Rewrote Priority sorting as an explicit comparator so priority grouping is always primary. When High has priority, all High Priority tasks are listed before any Low Priority tasks.
- 2026-05-07: Replaced the comparator with explicit High and Low sorted lists that are concatenated according to arrow direction, guaranteeing no interleaving between priority groups.
- 2026-05-07: Made sort type selection idempotent so re-selecting the current Priority chip cannot reset the arrow direction back to the default.

### Phase 3: UI and State Behavior
**Status:** Completed
**Date Added:** 2026-05-07
**Priority:** Medium (User Experience)
**Description:** Ensure the new sort option fits the current Tasks tab UI and does not create confusing direction-toggle behavior.

**Technical Requirements:**
- Add the new chip to the existing sort chip group in the Tasks layout.
- Ensure chip text fits on phone-width screens.
- Choose and implement default direction behavior for Priority sort.
- Update the sort direction button state if Priority sort has fixed direction.
- Preserve scroll-to-top behavior after sort changes.

**Acceptance Criteria:**
- [x] The sort controls remain readable on narrow phone screens.
- [x] The direction button behavior is clear and consistent.
- [x] Changing to Priority sort updates the list immediately.
- [x] Returning to other sort modes preserves their expected default direction.

**Progress Notes:**
- 2026-05-07: Updated Priority sort direction behavior: ascending shows High Priority first by default, descending shows Low Priority first, and the secondary alphabetical sort always remains ascending.
- 2026-05-07: Selecting Priority updates the list through the existing sort `StateFlow` path and preserves the existing scroll-to-top behavior.

### Phase 4: Verification
**Status:** Completed
**Date Added:** 2026-05-07
**Priority:** High (Regression Safety)
**Description:** Verify priority sort behavior with representative task data.

**Technical Requirements:**
- Test with at least:
  - multiple High Priority tasks
  - multiple Low Priority tasks
  - mixed-case task names
  - inactive tasks if they are visible in the Tasks tab
  - tasks with no activities
  - tasks with activity history
- Run the debug build after implementation.

**Acceptance Criteria:**
- [x] Priority sort produces High A-Z, then Low A-Z.
- [x] Alphabetical sort still produces A-Z or Z-A based on direction.
- [x] Last Activity Date sort still orders by activity date.
- [x] Activity Count sort still orders by count.
- [x] `gradlew.bat assembleDebug` succeeds.

**Progress Notes:**
- 2026-05-07: Verified `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Inspect `fragment_pieces.xml`, `PiecesFragment`, and `PiecesViewModel`.
2. Add the new sort enum value and sorting branch.
3. Add the Priority chip and map it in the fragment.
4. Decide and implement direction-toggle behavior for Priority sort.
5. Build and manually verify with mixed-priority task data.

## Cycle Notes

- This cycle uses the current internal class names (`PiecesFragment`, `PiecesViewModel`) because the Tasks tab still contains some legacy PlayStreak naming in code.
- The product-facing feature should use MyStreak terminology: `Tasks`, `Priority`, `High Priority`, and `Low Priority`.
- This cycle should not rename classes or broaden into a PlayStreak-to-MyStreak naming cleanup.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Direction toggle makes Priority sort confusing | Use the arrow to change only priority group order; keep names A-Z inside each group. |
| Sort chip row becomes crowded on small screens | Keep label short, such as `Priority`, and verify phone-width layout. |
| Existing sort behavior regresses | Keep changes scoped to the new `SortType` branch and run manual checks for all existing sort options. |
| Inactive task placement is ambiguous | Preserve existing Tasks tab inclusion rules; priority sort only changes ordering of visible tasks. |

## Handoff Checklist

- [x] Confirm preferred behavior for direction toggle in Priority sort.
- [x] Implement the new sort option.
- [x] Verify with mixed High/Low Priority sample tasks.
- [x] Run `gradlew.bat assembleDebug`.

## Cycle Completion Summary

**Completion Date:** 2026-05-07
**Git Commit Status:** Changes pending commit

**Accomplishments:**
- Added Priority as a Tasks tab sort option.
- Implemented priority sorting with High Priority first by default and alphabetical ascending as the fixed secondary sort.
- Kept the sort direction toggle active for Priority sort, matching the other sort modes.

**Verification:**
- `gradlew.bat assembleDebug` succeeds.
