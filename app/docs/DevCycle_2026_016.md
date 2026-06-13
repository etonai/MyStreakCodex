# Development Cycle 2026-016

**Status:** In Progress
**Start Date:** 2026-06-13
**Target Completion:** TBD
**Focus:** Allow date and time editing when adding a new activity

## Overview

DC 015 confirmed that Edit Date and Edit Time buttons work correctly in edit mode. This cycle extends the same pattern to the add-activity flow: when a user is on the Summary screen after choosing to log a new activity, they can tap Edit Date or Edit Time to back-date or adjust the time before saving. The change also ensures the Calendar-tab pre-populated date (set in `EditActivityStorage`) is used to seed `currentTimestamp` so that tapping a past date in the Calendar and then logging an activity correctly pre-fills that date in the pickers.

## Product Definition

### Current Behavior
- When adding a new activity, the Summary screen shows the current date/time but provides no way to change it.
- The Edit Date and Edit Time buttons exist in `fragment_summary.xml` but are hidden (`View.GONE`) in add mode.
- `currentTimestamp` in add mode is always initialised to `System.currentTimeMillis()`, even when the user arrived from the Calendar tab with a pre-populated date.

### New Behavior
- When adding a new activity, the Summary screen shows Edit Date and Edit Time buttons, matching the experience in edit mode.
- `currentTimestamp` is seeded from `EditActivityStorage.getPrePopulatedDate()` if a Calendar date was pre-selected, otherwise from `System.currentTimeMillis()`.
- Tapping Edit Date opens a date picker pre-populated with `currentTimestamp`; the user can pick any date up to today.
- Tapping Edit Time opens a time picker pre-populated with `currentTimestamp` in 12-hour format.
- Saving uses `currentTimestamp` (potentially modified by the pickers) as the activity's timestamp — same path that already works in edit mode.
- Selecting a future date/time is blocked by the existing "Activities cannot be dated in the future" toast.

## Current Work Items

### Phase 1: Show Edit Date and Edit Time Buttons in Add Mode
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** High
**Description:** In `SummaryFragment.setupSummary()`, the `else` branch (add mode) currently hides `buttonEditDate` and `buttonEditTime`. Change both to `View.VISIBLE` so they appear in add mode.

**File to change:** `app/src/main/java/com/pseddev/playstreak/ui/addactivity/SummaryFragment.kt`

**Relevant code (current `else` branch in `setupSummary()`):**
```kotlin
} else {
    binding.textTitle.text = "Add Activity"
    binding.textDate.text = "Date: ${dateFormat.format(Date(currentTimestamp))}"
    binding.buttonSave.text = "Save"
    binding.buttonEditDate.visibility = View.GONE   // ← change to View.VISIBLE
    binding.buttonEditTime.visibility = View.GONE   // ← change to View.VISIBLE
}
```

**Acceptance Criteria:**
- [ ] Edit Date and Edit Time buttons are visible on the Summary screen when adding a new activity.
- [ ] Edit Date and Edit Time buttons remain visible on the Summary screen when editing an existing activity (no regression to DC 015).
- [ ] Tapping Edit Date in add mode opens the date picker.
- [ ] Tapping Edit Time in add mode opens the time picker.

### Phase 2: Seed currentTimestamp from Pre-Populated Calendar Date
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** Medium
**Description:** In `SummaryFragment.onViewCreated()`, the `else` branch that initialises `currentTimestamp` in add mode uses `System.currentTimeMillis()` unconditionally. Change it to read `EditActivityStorage.getPrePopulatedDate()` first, falling back to `System.currentTimeMillis()`. Clear the pre-populated date after reading it so it is not accidentally reused.

**File to change:** `app/src/main/java/com/pseddev/playstreak/ui/addactivity/SummaryFragment.kt`

**Relevant code (current timestamp initialisation):**
```kotlin
currentTimestamp = if (editActivity != null) {
    editActivity.timestamp
} else {
    System.currentTimeMillis()   // ← replace with: EditActivityStorage.getPrePopulatedDate() ?: System.currentTimeMillis()
}
```

After reading, add: `EditActivityStorage.clearPrePopulatedDate()`

**Acceptance Criteria:**
- [ ] When the user arrives from the Calendar tab, `currentTimestamp` is initialised to the tapped calendar date, and the date picker opens to that date.
- [ ] When the user arrives from the normal add-activity flow (not Calendar), `currentTimestamp` is initialised to the current time.
- [ ] `EditActivityStorage.clearPrePopulatedDate()` is called after reading so the value is not reused on a subsequent add.

### Phase 3: Verify Timestamp Is Saved Correctly in Add Mode
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** High
**Description:** Confirm the existing save path in `SummaryFragment` already passes `currentTimestamp` to `viewModel.saveActivity(..., timestamp = currentTimestamp)`. No code change is expected here — this phase is a code-review confirmation.

**Acceptance Criteria:**
- [ ] The `buttonSave` click handler in add mode calls `viewModel.saveActivity(..., timestamp = currentTimestamp)` with the potentially modified `currentTimestamp`.
- [ ] No additional changes are needed to `AddActivityViewModel.saveActivity()`.

### Phase 4: Build and Verification
**Status:** 🎫 Open
**Date Added:** 2026-06-13
**Priority:** Medium
**Description:** Build the app and perform end-to-end manual verification of the feature for both the normal add flow and the Calendar-initiated add flow.

**Acceptance Criteria:**
- [ ] `gradlew.bat assembleDebug` succeeds with no new errors or warnings.
- [ ] Adding a new activity from the main flow: Edit Date and Edit Time buttons are visible; changing the date saves correctly.
- [ ] Adding a new activity from the Calendar tab: the date picker pre-selects the tapped calendar date; saving records the activity on that date.
- [ ] Editing an existing activity: no regression — Edit Date and Edit Time buttons still appear and function correctly.
- [ ] Future date/time selection is still blocked by the existing toast.

## Proposed Implementation Sequence

1. In `SummaryFragment.setupSummary()`, change `buttonEditDate` and `buttonEditTime` visibility from `View.GONE` to `View.VISIBLE` in the `else` (add mode) branch.
2. In `SummaryFragment.onViewCreated()`, replace `System.currentTimeMillis()` in the `else` branch with `EditActivityStorage.getPrePopulatedDate() ?: System.currentTimeMillis()`, then call `EditActivityStorage.clearPrePopulatedDate()`.
3. Confirm `buttonSave` already passes `currentTimestamp` in add mode (code review only).
4. Build and verify manually.

## Implementation Notes

- `setupDateTimeEditing()` in `SummaryFragment` already wires click listeners for both buttons unconditionally — no change needed there.
- `showDatePicker()` and `showTimePicker()` operate on `currentTimestamp` regardless of mode — no change needed.
- The no-arg `AddActivityViewModel.saveActivity()` overload also reads `EditActivityStorage.getPrePopulatedDate()`, but `SummaryFragment` does not call that overload — it calls the explicit-timestamp overload. Clearing the pre-populated date in `SummaryFragment` ensures the ViewModel overload would not pick up a stale value on any other code path.
- Changes are limited to `SummaryFragment.kt` only; no layout, ViewModel, or repository changes are needed.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Showing Edit Date/Time in add mode confuses users who expect to log for "now" | The date field already shows the current time; buttons simply allow changing it, consistent with edit mode. |
| `clearPrePopulatedDate()` called in SummaryFragment prevents ViewModel overload from using it on other paths | The ViewModel overload is not called by SummaryFragment, so no conflict. Clearing early is safe. |
| Calendar pre-populated date carries a timestamp at midnight or start-of-day, resulting in an unexpected time | Time part comes from `currentTimestamp` initialisation; user can adjust with Edit Time if needed. |
