# Development Cycle 2026-014

**Status:** Complete
**Start Date:** 2026-05-19
**Target Completion:** 2026-05-20
**Focus:** Routines tab sort cleanup

## Overview

This development cycle updates sorting controls on the Routines tab. Routines do not have meaningful priority, so the Priority sort option should not be shown there. The Routines tab should default to sorting by name.

## Product Definition

### Current Behavior
- The Routines tab shows the same sort options as Tasks, including Priority.
- Priority is the default sort, even though routines do not have a meaningful priority.

### New Behavior
- The Routines tab does not show the Priority sort option.
- The Routines tab defaults to Name sorting.
- The Tasks tab keeps the Priority sort option and its current default behavior.

## Current Work Items

### Phase 1: Hide Priority Sort on Routines Tab
**Status:** Complete
**Date Added:** 2026-05-19
**Priority:** Low
**Description:** Remove the Priority sort option from the Routines tab only.

**Acceptance Criteria:**
- [x] Priority sort is not visible on the Routines tab.
- [x] Priority sort remains visible on the Tasks tab.
- [x] Routines tab sort controls still lay out cleanly after Priority is removed.

### Phase 2: Default Routines to Name Sort
**Status:** Complete
**Date Added:** 2026-05-19
**Priority:** Low
**Description:** Make Name the default sort for routines.

**Technical Requirements:**
- Use Name/Alphabetical sort as the initial sort type for the Routines tab.
- Keep ascending alphabetical order as the initial direction.
- Preserve the existing Tasks tab default sort behavior.

**Acceptance Criteria:**
- [x] Opening the Routines tab defaults to Name sort.
- [x] Routines initially sort alphabetically ascending.
- [x] Opening the Tasks tab still defaults to Priority sort.

### Phase 3: Verification
**Status:** Complete
**Date Added:** 2026-05-19
**Priority:** Low
**Description:** Build and verify the sorting behavior.

**Acceptance Criteria:**
- [x] `gradlew.bat assembleDebug` succeeds.
- [x] No unrelated task/routine behavior changes are introduced.

## Proposed Implementation Sequence

1. Locate the shared Tasks/Routines sort controls.
2. Hide or remove the Priority chip when the active task kind is routine.
3. Set the Routines tab initial sort type to Name/Alphabetical.
4. Confirm the Tasks tab keeps Priority as its default.
5. Build and update this document.

## Implementation Notes

- This cycle should not change routine activity counts, routine completion behavior, or routine colors.
- If the sort state is shared through a ViewModel, initialize or adjust it based on `TaskKind.ROUTINE` when the tab filter is applied.
- The user-facing label may be "Name" while the internal sort enum may be `ALPHABETICAL`; use the existing naming pattern in code.
- Implemented the Routines tab by hiding the Priority chip when `taskKind == TaskKind.ROUTINE`.
- The routine filter now switches the shared sort state from Priority to Alphabetical ascending.
- Tasks keep the existing Priority chip and Priority default.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Removing Priority from Routines also removes it from Tasks | Gate the UI change on `TaskKind.ROUTINE`. |
| Routines still initialize with Priority internally | Set the routine default after the task-kind filter is known. |
| Sort direction carries an unexpected value into Routines | Initialize routine name sort with ascending direction. |

## Handoff Checklist

- [x] Confirm whether the visible sort label should read "Name" or keep the existing alphabetical wording.
- [x] Confirm Tasks tab Priority default remains unchanged.
