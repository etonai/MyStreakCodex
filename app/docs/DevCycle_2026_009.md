# Development Cycle 2026-009

**Status:** Complete
**Start Date:** 2026-05-16
**Target Completion:** Immediate
**Focus:** Prevent abandoned activity edits from overwriting later new activities

## Overview

This development cycle addresses a critical data-loss bug in the activity add/edit flow. If the user begins editing an existing activity, backs out without saving, and later enters a new activity, the new activity can overwrite the previously edited activity instead of being inserted as a new activity.

The likely cause is stale edit state surviving after the user abandons the edit flow. `EditActivityStorage` and `AddActivityViewModel.editActivity` can remain populated, so `SummaryFragment` later treats a new activity flow as edit mode and calls `updateActivity(...)` instead of `saveActivity(...)`.

## Severity

**Critical**

This can silently replace user history. The fix should be prioritized before feature work.

## Product Definition

### Expected Behavior
- Starting an edit should put the activity flow into edit mode.
- Saving an edit should update only the selected existing activity.
- Canceling or backing out of an edit should clear edit mode.
- Starting a new activity should always clear any stale edit mode first.
- Saving a new activity should always insert a new activity.

### Bug Behavior
- User starts editing an existing activity.
- User backs out or cancels before saving.
- Edit state remains alive.
- User starts a new activity later.
- The new activity flow incorrectly updates the old activity.

## Current Work Items

### Phase 1: Reproduce and Trace Edit State
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** Critical
**Description:** Trace the lifecycle of edit mode across Dashboard, Calendar, Timeline, Add Activity, Select Level, Notes, and Summary screens.

**Technical Requirements:**
- Identify every `EditActivityStorage.setEditActivity(...)` call.
- Identify every `EditActivityStorage.clearEditActivity()` call.
- Identify every `AddActivityViewModel.setEditMode(...)` and `clearEditMode()` call.
- Reproduce the abandoned edit path:
  - edit existing activity
  - back/cancel before saving
  - start new activity
  - save
- Confirm whether stale state is in `EditActivityStorage`, `AddActivityViewModel`, or both.

**Acceptance Criteria:**
- [x] The stale-state path is documented.
- [x] The exact screen/action where edit state should be cleared is identified.
- [x] The overwrite path is reproducible before the fix.

### Phase 2: Clear Edit State on Abandoned Edit
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** Critical
**Description:** Ensure edit mode is cleared whenever the user leaves an edit flow without saving.

**Technical Requirements:**
- Clear edit state when the user presses Cancel in the Summary screen.
- Clear edit state when the user uses system Back from the edit flow.
- Clear edit state when the flow navigates back to Progress without saving.
- Ensure `viewModel.doneNavigating()` does not remain the only cleanup path, because that currently runs only after save/update completion.

**Acceptance Criteria:**
- [x] Canceling an edit clears `EditActivityStorage`.
- [x] Backing out of an edit clears `EditActivityStorage`.
- [x] Canceling or backing out clears `AddActivityViewModel.editActivity`.
- [x] Saving an edit still updates the intended activity.

### Phase 3: Clear Stale Edit State When Starting New Activity
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** Critical
**Description:** Add a defensive cleanup when beginning any new add-activity flow from Dashboard, Calendar, or other entry points.

**Technical Requirements:**
- Before navigating into a new activity flow, clear edit state.
- Preserve calendar pre-populated date behavior when starting from the Calendar tab.
- Avoid clearing edit state when intentionally navigating within an active edit flow.
- Review Dashboard and Calendar add buttons because they currently navigate directly to `selectPieceFragment`.

**Acceptance Criteria:**
- [x] Starting a new activity from Dashboard cannot inherit stale edit state.
- [x] Starting a new activity from Calendar cannot inherit stale edit state.
- [x] Calendar selected-date prepopulation still works.
- [x] Starting a new activity after abandoning an edit inserts a new row.

### Phase 4: Guard Summary Save Logic
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** High
**Description:** Make `SummaryFragment` less vulnerable to stale edit state by deriving edit mode from an explicit, current flow state.

**Technical Requirements:**
- Avoid treating any non-null global edit activity as proof that the current flow is editing.
- Consider passing an explicit `isEditMode` argument through navigation.
- At minimum, ensure `viewModel.editActivity` and `EditActivityStorage.isEditMode()` cannot disagree silently.
- Prefer insert over update when the current flow was launched as a new activity flow.

**Acceptance Criteria:**
- [x] Summary save cannot update an old activity during a new activity flow.
- [x] Edit flow still updates the selected activity.
- [x] Add flow always inserts.

### Phase 5: Regression Verification
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** Critical
**Description:** Verify the data-loss scenario and nearby edit/add paths.

**Technical Requirements:**
- Test edit existing activity, save, confirm only that activity changes.
- Test edit existing activity, cancel, then add new activity, confirm old activity is unchanged and new row is inserted.
- Test edit existing activity, system Back, then add new activity, confirm old activity is unchanged and new row is inserted.
- Test Calendar add activity with preselected date.
- Test Dashboard add activity.
- Run debug build.

**Acceptance Criteria:**
- [x] Abandoned edit no longer causes overwrite.
- [x] New activity creates a new activity after abandoned edit.
- [x] Existing activity remains unchanged after abandoned edit.
- [x] Calendar date prepopulation still works.
- [x] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Reproduce the bug and trace stale edit state.
2. Add explicit cleanup for cancel/back from edit flows.
3. Add defensive cleanup before new activity entry points.
4. Harden `SummaryFragment` save/update decision.
5. Verify abandoned edit followed by new add from Dashboard and Calendar.
6. Build and update this document.

## Implementation Notes

- `EditActivityStorage` is global process-level state. It must be treated carefully because it can outlive the screen that originally set it.
- `SummaryFragment` currently chooses update vs insert based on `viewModel.editActivity.value`.
- `DashboardFragment`, `CalendarFragment`, and `TimelineFragment` can start edit mode via `EditActivityStorage.setEditActivity(...)`.
- `DashboardFragment` and `CalendarFragment` can also start new activity flows directly, so they should defensively clear stale edit state before doing so.
- Added separate edit-only cleanup to preserve Calendar pre-populated dates while removing stale edit activity state.
- New add flows clear stale edit state at the task picker.
- Edit cancel/back paths now clear both `EditActivityStorage` and `AddActivityViewModel` edit state.
- `SummaryFragment` only updates an activity when storage and ViewModel agree on the same active edit activity.
- `gradlew.bat assembleDebug` succeeds.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Clearing edit state too aggressively breaks legitimate edits | Only clear on cancel/back/new-flow entry, not while moving between edit screens. |
| Calendar add loses selected date | Clear edit activity separately from pre-populated date, or set the pre-populated date after clearing. |
| Summary still sees stale ViewModel state | Clear both `EditActivityStorage` and `AddActivityViewModel` edit state. |
| Data loss persists silently | Add manual regression steps and consider automated coverage around save vs update selection. |

## Handoff Checklist

- [x] Confirm whether back from any edit step should behave exactly like Cancel.
- [x] Confirm whether abandoned edit should return to Dashboard, Calendar, or whichever screen launched it.
- [ ] Consider replacing global edit storage with explicit navigation args in a later cleanup cycle.
