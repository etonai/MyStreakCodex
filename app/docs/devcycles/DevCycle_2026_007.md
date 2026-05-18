# Development Cycle 2026-007

**Status:** Implemented
**Start Date:** 2026-05-14
**Target Completion:** TBD
**Focus:** Add Routine tasks as a separate lightweight task type

## Overview

This development cycle introduces Routine tasks as a second kind of trackable work in MyStreak. Routine tasks are simpler than the existing success-level Tasks: they do not use Minimum, Medium, or High success descriptions. Logging a Routine only records that it was done, with optional Activity notes if notes are available for the logging flow.

Routine tasks should have their own tab alongside Dashboard, Calendar, and Tasks. They should not appear in the Tasks tab. During Activity entry, Routine tasks should appear at the bottom of the task selection list, below High Priority and Low Priority.

## Product Definition

### Existing Tasks
- Use priority: High or Low.
- Use Minimum, Medium, and High success descriptions.
- Appear in the Tasks tab.
- Appear in Activity entry under High Priority and Low Priority.
- Contribute to current Dashboard, Calendar, and priority-completion behavior.

### Routine Tasks
- Do not use Minimum, Medium, or High success descriptions.
- Logging records only that the routine was done.
- May still support optional Activity notes.
- Appear in a dedicated Routine tab.
- Do not appear in the Tasks tab.
- Appear at the bottom of Activity entry task selection, below High Priority and Low Priority.

## Current Work Items

### Phase 1: Routine Task Data Model
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (Foundation)
**Description:** Extend the data model so MyStreak can distinguish normal success-level Tasks from Routine tasks.

**Technical Requirements:**
- Add a Task category/type field, likely something like `TaskKind.STANDARD` and `TaskKind.ROUTINE`.
- Default existing Tasks to the standard success-level type.
- Add Room migration for the new field.
- Add DAO/repository queries for:
  - standard active Tasks
  - Routine active Tasks
  - all Routine Tasks for the Routine tab
- Ensure existing Tasks remain valid after upgrade.

**Acceptance Criteria:**
- [x] Existing Tasks migrate to the standard type.
- [x] Routine tasks can be persisted distinctly from standard Tasks.
- [x] Repository/DAO APIs can query standard and Routine tasks separately.
- [x] `gradlew.bat assembleDebug` succeeds.

### Phase 2: Routine Task Creation and Editing
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (User Workflow)
**Description:** Allow users to create and edit Routine tasks without success-level fields.

**Technical Requirements:**
- Decide whether Routine creation is:
  - a separate action in the Routine tab, or
  - a task type choice in the existing Add Task screen.
- Routine task forms should not require Minimum, Medium, or High success descriptions.
- Routine task edit UI should hide success-level descriptions.
- Preserve name, color, active/inactive state, and any other shared fields.

**Acceptance Criteria:**
- [x] Users can create a Routine task.
- [x] Users can edit a Routine task.
- [x] Routine task forms do not show or require success-level fields.
- [x] Existing standard Task creation/editing remains unchanged.

### Phase 3: Routine Tab
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (Navigation)
**Description:** Add a dedicated Routine tab alongside Dashboard, Calendar, and Tasks.

**Technical Requirements:**
- Add a `Routine` tab to the main tab layout.
- Show Routine tasks in the Routine tab.
- Exclude Routine tasks from the existing Tasks tab.
- Reuse appropriate task row UI where possible, but avoid showing success-level details for Routine tasks.
- Decide whether Routine tab sorting should match Tasks sorting or use a simpler default.

**Acceptance Criteria:**
- [x] The main app shell includes Dashboard, Calendar, Tasks, and Routine tabs.
- [x] Routine tasks appear in Routine.
- [x] Routine tasks do not appear in Tasks.
- [x] Standard Tasks do not appear in Routine unless explicitly designed later.

### Phase 4: Activity Entry Selection
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (Logging Workflow)
**Description:** Include Routine tasks at the bottom of the Activity entry selection list.

**Technical Requirements:**
- Keep the existing selection ordering:
  - High Priority
  - Low Priority
  - Routine
- Exclude Routine tasks from High Priority and Low Priority sections.
- Add a Routine section when at least one active Routine exists.
- Selecting a Routine should skip the success-level screen.
- Routine Activity logging should record a done/completed activity, likely using a neutral or dedicated success value.

**Acceptance Criteria:**
- [x] Activity entry lists Routine tasks below High Priority and Low Priority.
- [x] Routine tasks do not duplicate into the standard sections.
- [x] Selecting a Routine logs or proceeds through a Routine-specific flow without Minimum/Medium/High choices.
- [x] Optional Activity notes still work for Routine activities if notes remain part of the logging flow.

### Phase 5: Dashboard and Calendar Behavior
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** Medium (Product Semantics)
**Description:** Decide and implement how Routine activities appear in Dashboard and Calendar.

**Technical Requirements:**
- Decide whether Routine activities count toward:
  - daily activity counts
  - current streak
  - rolling week summary
  - calendar color levels
  - high-priority completion logic
- Recommended starting point:
  - Routine activities count as activities for activity totals and streaks.
  - Routine activities do not affect high-priority completion.
  - Routine activities produce the lowest non-empty calendar color if no stronger standard Task activity exists.
- Display Routine activity rows without success-level description.

**Acceptance Criteria:**
- [x] Routine activities appear in Dashboard when logged today/yesterday.
- [x] Routine activities appear on the Calendar selected-date activity list.
- [x] Calendar and Dashboard behavior is documented and implemented consistently.
- [x] High-priority outstanding logic ignores Routine tasks.

### Phase 6: Import/Export and Compatibility
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (Data Portability)
**Description:** Update MyStreak JSON import/export for Routine tasks and Routine activities.

**Technical Requirements:**
- Add task type/kind to exported Tasks.
- Ensure old exports without task kind import as standard Tasks.
- Bump schema version if needed.
- Export/import Routine tasks and Routine activities correctly.
- Validate enum values during import.

**Acceptance Criteria:**
- [x] Routine tasks export and import correctly.
- [x] Existing v1/v2 MyStreak exports import as standard Tasks.
- [x] Invalid task kind values are rejected with clear errors.
- [x] Import/export round trip preserves Routine tab contents.

### Phase 7: Verification
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** High (Regression Safety)
**Description:** Verify Routine behavior across creation, logging, display, and portability.

**Technical Requirements:**
- Test upgrade from existing local data.
- Test creating standard and Routine tasks.
- Test editing standard and Routine tasks.
- Test logging standard and Routine activities.
- Test Dashboard, Calendar, Tasks, and Routine tabs.
- Test JSON export/import with mixed standard and Routine tasks.
- Run debug build.

**Acceptance Criteria:**
- [x] Existing data remains usable.
- [x] Standard Task behavior is unchanged.
- [x] Routine task behavior matches this document.
- [x] `gradlew.bat assembleDebug` succeeds.

### Phase 8: Rename Activity Entry Low-Priority Section
**Status:** Complete
**Date Added:** 2026-05-14
**Priority:** Low (Label Clarity)
**Description:** Rename the second Activity entry task section from `All Tasks:` to `Low Priority:`.

**Technical Requirements:**
- In the Add Activity task picker, keep the ordering:
  - High Priority
  - Low Priority
  - Routine
- The second section should contain only active standard low-priority tasks.
- Update the displayed section header from `All Tasks:` to `Low Priority:`.
- Ensure Routine tasks remain in the Routine section only.

**Acceptance Criteria:**
- [x] The Add Activity task picker shows `Low Priority:` instead of `All Tasks:`.
- [x] High-priority tasks appear only under `High Priority:`.
- [x] Low-priority standard tasks appear only under `Low Priority:`.
- [x] Routine tasks appear only under `Routine:`.

## Proposed Implementation Sequence

1. Add Task kind/type to the data model and migrate existing data.
2. Split DAO/repository queries into standard Task and Routine Task paths.
3. Update Tasks tab to exclude Routine tasks.
4. Add Routine tab and Routine task list.
5. Update add/edit flows for Routine task creation.
6. Update Activity entry selection to include Routine at the bottom.
7. Add Routine logging path that skips success-level selection.
8. Update Dashboard, Calendar, and import/export behavior.
9. Rename the Activity entry second task section from `All Tasks:` to `Low Priority:`.
10. Build and manually verify mixed standard/Routine data.

## Cycle Notes

- Routine tasks are not simply Low Priority tasks. They are a separate task type with a simpler completion model.
- Routine tasks should not appear in the Tasks tab.
- The existing Activity notes work should remain reusable for Routine activity logging.
- Routine activities count toward activity totals and streaks, but do not count toward high-priority completion.
- Routine-only days receive the lowest non-empty calendar color through existing "any activity" behavior.

## Implementation Notes

- Added `TaskKind.STANDARD` and `TaskKind.ROUTINE` with Room migration 8 -> 9.
- Added a Routine tab that reuses the task list with a Routine filter.
- Routine add/edit screens hide priority and Minimum/Medium/High success fields.
- Activity entry now lists active Routine tasks after High Priority and All Tasks, and selecting one skips success-level selection.
- JSON export schema is now version 3 and includes `taskKind`; v1/v2 imports default missing task kind to Standard.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Routine tasks blur with normal Tasks | Use an explicit task type and separate tab/filtering. |
| Logging flow becomes confusing | Skip success-level selection for Routine tasks and use clear section headers. |
| Dashboard/calendar semantics become inconsistent | Decide count/streak/color behavior before coding those surfaces. |
| Import/export breaks older files | Default missing task kind to standard and support previous schema versions. |
| UI tab row becomes crowded | Verify Dashboard/Calendar/Tasks/Routine on phone-width screens. |

## Handoff Checklist

- [ ] Confirm Routine activities count toward streaks.
- [ ] Confirm Routine activities count toward calendar color.
- [ ] Confirm Routine creation entry point.
- [ ] Confirm whether Routine tasks use color and active/inactive state.
- [ ] Implement Phase 1 only after product semantics are agreed.
