# Development Cycle 2026-006

**Status:** ✅ Completed
**Start Date:** 2026-05-13
**Completion Date:** 2026-05-13
**Focus:** Show Activity note detail on row tap in Dashboard and Calendar

## Overview

When a user taps an activity row in the Dashboard or Calendar view, the app should display the note associated with that activity. Currently, activity rows respond only to the Edit and Delete buttons; the row itself has no tap handler. Notes are already displayed inline as a secondary line when non-empty, but there is no dedicated detail view.

This cycle adds a row-level tap gesture to both surfaces that opens a focused note detail dialog. The dialog shows the task name, date/time, success level, and note text for the selected activity.

## Design Decisions

These decisions should be confirmed before or during Phase 1 implementation:

| Decision | Options | Recommended |
|---|---|---|
| What to show when no notes exist | (A) Show dialog with "No notes" message; (B) Tap does nothing | (A) — consistent behavior regardless of note presence |
| Inline notes after this change | (A) Keep inline notes; (B) Remove inline notes, rely on tap | (A) — inline notes aid quick scanning; tap provides full context |
| Dialog type | (A) AlertDialog; (B) BottomSheetDialog | (A) — simpler, sufficient for short-to-medium notes |

## Current Work Items

### Phase 1: Note Detail Dialog — Dashboard
**Status:** ✅ COMPLETED
**Date Added:** 2026-05-13
**Priority:** High
**Description:** Add a tap listener to each activity row in the Dashboard that opens a note detail dialog.

**Technical Requirements:**
- Add `setOnClickListener` on the row root view (`itemBinding.root`) inside `DashboardFragment.bindActivityRow()`.
- The click listener must not interfere with the existing Edit and Delete button listeners.
- Show an `AlertDialog` (or similar) containing:
  - Task name
  - Date and time of the activity (formatted consistently with the rest of the UI)
  - Success level description (using the existing `successLevelDescription` helper)
  - Note text, or a "No notes" placeholder if the note is empty
- The dialog should have a single dismiss button (e.g., "Close").

**Acceptance Criteria:**
- [x] Tapping an activity row in the Dashboard opens a note detail dialog.
- [x] The dialog shows the correct task name, date, success level, and note.
- [x] Tapping an activity with no notes shows the dialog with a "No notes" message.
- [x] Edit and Delete buttons still work normally after this change.
- [x] `gradlew.bat assembleDebug` succeeds.

**Files:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/DashboardFragment.kt`

### Phase 2: Note Detail Dialog — Calendar
**Status:** ✅ COMPLETED
**Date Added:** 2026-05-13
**Priority:** High
**Description:** Add the same tap-to-detail behavior to activity rows in the Calendar view.

**Technical Requirements:**
- Add `setOnClickListener` on `itemBinding.root` inside `CalendarFragment.bindActivityRow()`.
- Same dialog content and behavior as Phase 1.
- The click listener must not interfere with the existing Edit and Delete button listeners.

**Acceptance Criteria:**
- [x] Tapping an activity row in the Calendar opens a note detail dialog.
- [x] The dialog shows the correct task name, date, success level, and note.
- [x] Tapping an activity with no notes shows the dialog with a "No notes" message.
- [x] Edit and Delete buttons still work normally after this change.
- [x] `gradlew.bat assembleDebug` succeeds.

**Files:**
- `app/src/main/java/com/pseddev/playstreak/ui/progress/CalendarFragment.kt`

### Phase 3: Verification
**Status:** ✅ COMPLETED
**Date Added:** 2026-05-13
**Priority:** High
**Description:** Verify the tap behavior end-to-end in both surfaces, including edge cases.

**Acceptance Criteria:**
- [x] Activity with a short note displays correctly.
- [x] Activity with a long multi-line note displays without clipping.
- [x] Activity with no note shows the "No notes" placeholder.
- [x] Dialog dismisses cleanly with no navigation side effects.
- [x] Both Dashboard and Calendar surfaces behave consistently.
- [x] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Add `setOnClickListener` on `itemBinding.root` in `DashboardFragment.bindActivityRow()` and show an `AlertDialog` with the note detail.
2. Build and manually verify the Dashboard tap flow.
3. Add the same listener in `CalendarFragment.bindActivityRow()`.
4. Build and manually verify the Calendar tap flow.
5. Test edge cases: no notes, long notes, rapid taps.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Row tap conflicts with Edit/Delete button taps | Edit/Delete buttons consume their own touch events; the row listener fires only when neither button is hit. Standard Android touch dispatch handles this correctly. |
| Long notes overflow the AlertDialog | AlertDialog scrolls by default on Android when content exceeds screen height; verify on a small-screen device. |
| Dialog shown on a fragment that is already paused | Guard with `isAdded` / `!isDetached` before calling `AlertDialog.Builder.show()`. |

## Cycle Notes

- Both `DashboardFragment` and `CalendarFragment` already share the `ActivityWithPiece` data structure and the `successLevelDescription` helper, so no new data fetching is needed.
- The dialog does not need to be a separate `DialogFragment` unless reuse across multiple surfaces becomes necessary. An inline `AlertDialog.Builder` call in each fragment is sufficient for this cycle.
- The `item_dashboard_activity.xml` root `LinearLayout` does not need layout changes; `setOnClickListener` on a `ViewGroup` works without requiring `android:clickable="true"` in XML when set programmatically.
