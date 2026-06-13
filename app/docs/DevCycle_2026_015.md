# Development Cycle 2026-015

**Status:** In Progress
**Start Date:** 2026-06-13
**Target Completion:** TBD
**Focus:** Allow date and time editing when editing an existing activity

## Overview

When a user edits an existing activity, they should be able to change the date and time of that activity in addition to the other editable fields. The `SummaryFragment` layout and partial code for Edit Date / Edit Time buttons already exist, but the buttons are gated on `currentEditActivity()` returning a non-null value — which requires both `EditActivityStorage` and `viewModel.editActivity` to be populated. The current edit navigation path sets `EditActivityStorage` but does not appear to call `viewModel.setEditMode()`, so the edit buttons may never become visible. This cycle investigates, fixes, and verifies the full end-to-end feature.

## Product Definition

### Current Behavior
- When a user edits an existing activity they can change the task, success level, and notes.
- The Summary screen has Edit Date and Edit Time buttons in its layout, but they are hidden (`visibility="gone"`) and may remain hidden in the edit flow due to a wiring gap.
- Users cannot reliably change the date or time of an existing activity.

### New Behavior
- When a user edits an existing activity, the Summary screen shows Edit Date and Edit Time buttons.
- Tapping Edit Date opens a date picker pre-populated with the activity's current date; the user can pick any date up to and including today.
- Tapping Edit Time opens a time picker pre-populated with the activity's current time.
- Selecting a future date/time is blocked with a user-visible error message.
- Saving the edit persists the updated timestamp along with the other edited fields.

## Current Work Items

### Phase 1: Investigate and Fix Edit Mode Wiring
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** High
**Description:** Determine whether `viewModel.setEditMode()` is called during the edit navigation path and fix the wiring so that `currentEditActivity()` in `SummaryFragment` returns the correct activity.

**Key files to examine:**
- `ui/progress/ActivityEditNavigator.kt` — launches the edit flow
- `ui/addactivity/SelectLevelFragment.kt` — first stop for task edits
- `ui/addactivity/NotesInputFragment.kt` — first stop for routine edits
- `ui/addactivity/AddActivityViewModel.kt` — owns `setEditMode()` / `editActivity`
- `ui/progress/EditActivityStorage.kt` — singleton storage used alongside the ViewModel

**Acceptance Criteria:**
- [ ] Root cause of why `currentEditActivity()` may return null is identified and documented.
- [ ] `viewModel.setEditMode()` is called (or a suitable equivalent approach is used) so that `currentEditActivity()` returns the correct activity throughout the edit flow.
- [ ] No regression to the existing edit flow for task, success level, or notes fields.

### Phase 2: Show Edit Date and Edit Time Buttons in Edit Mode
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** High
**Description:** Confirm or fix that `buttonEditDate` and `buttonEditTime` become visible on the Summary screen when the user is in edit mode.

**Acceptance Criteria:**
- [ ] Opening an activity for editing and reaching the Summary screen shows the Edit Date and Edit Time buttons.
- [ ] The buttons remain hidden when adding a new activity (non-edit mode).
- [ ] The current activity timestamp is used to pre-populate both pickers.

### Phase 3: Validate Date and Time Picker Behavior
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** Medium
**Description:** Confirm that the existing `showDatePicker()` and `showTimePicker()` methods in `SummaryFragment` work correctly once the buttons are visible.

**Acceptance Criteria:**
- [ ] Edit Date picker opens with the activity's current date selected.
- [ ] The date picker prevents selecting a future date (maxDate = System.currentTimeMillis()).
- [ ] Edit Time picker opens with the activity's current time selected and uses 12-hour format.
- [ ] Selecting a new date updates `textDate` to reflect the change.
- [ ] Selecting a new time updates `textDate` to reflect the change.
- [ ] Picking a time that would result in a future timestamp is clamped or blocked with the existing "Activities cannot be dated in the future" toast.

### Phase 4: Persist Updated Timestamp on Save
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** High
**Description:** Confirm that when the user taps Update after changing the date or time, the new timestamp is passed through to `viewModel.updateActivity()` and stored in the database.

**Acceptance Criteria:**
- [ ] `updateActivity()` is called with the modified `currentTimestamp` from `SummaryFragment`.
- [ ] After saving, the activity appears on the Timeline with the updated date and time.
- [ ] After saving, the Calendar reflects the activity on the new date (not the old date).
- [ ] Streak calculations are not broken by the date change.

### Phase 5: Build and Verification
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** Medium
**Description:** Build the app and perform end-to-end manual verification of the feature.

**Acceptance Criteria:**
- [ ] `gradlew.bat assembleDebug` succeeds with no new errors or warnings.
- [ ] Editing an activity and changing its date saves correctly.
- [ ] Editing an activity and changing its time saves correctly.
- [ ] Editing an activity without touching date/time still saves correctly (no regression).
- [ ] Adding a new activity is unaffected (no Edit Date / Edit Time buttons visible).

## Proposed Implementation Sequence

1. Read `SelectLevelFragment.kt` and `NotesInputFragment.kt` to find where edit mode should be activated in the ViewModel.
2. Add a call to `viewModel.setEditMode(editActivity)` at the earliest fragment in the edit flow (or determine an alternative approach if the ViewModel scope does not persist across fragments).
3. Confirm that `SummaryFragment.currentEditActivity()` now returns the correct activity and that the edit buttons appear.
4. Smoke-test the date picker and time picker using the existing methods.
5. Verify the timestamp is saved correctly and reflected in Timeline and Calendar.
6. Build and update this document.

## Implementation Notes

- The `SummaryFragment` layout (`fragment_summary.xml`) already has `buttonEditDate` and `buttonEditTime` defined; no new layout work is expected.
- The `showDatePicker()`, `showTimePicker()`, and `updateDateDisplay()` methods in `SummaryFragment.kt` already exist; they only need to be reachable (i.e., buttons must be visible).
- `currentEditActivity()` requires both `EditActivityStorage.getEditActivity()` AND `viewModel.editActivity.value` to be non-null with matching IDs. If the ViewModel is scoped to the Activity and `setEditMode()` is never called during the edit nav path, the buttons will never appear.
- If the ViewModel loses state across fragment transitions in some configurations, an alternative approach (e.g., always using `EditActivityStorage` as the single source of truth) may be cleaner.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| `viewModel.editActivity` is always null during the edit path | Confirm with logging or breakpoints; if so, call `setEditMode()` from the entry fragment of the edit flow. |
| Changing the date moves the activity to a different day, breaking streak state | Verify streak recalculation after a date-change edit; rely on existing repository logic. |
| Date picker's maxDate does not prevent a same-day future time | Existing "Activities cannot be dated in the future" toast in `buttonSave` click handler covers this. |
| Routine edit path (`notesInputFragment`) is different from task edit path | Test both paths independently. |

## Handoff Checklist

- [ ] Confirm whether `viewModel.setEditMode()` is called anywhere during the edit navigation path.
- [ ] Confirm the edit flow works for both task edits (SelectLevel → Notes → Summary) and routine edits (Notes → Summary).
- [ ] Confirm Timeline and Calendar both update correctly after a date-change edit.
