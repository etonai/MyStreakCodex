# Development Cycle 2026-010

**Status:** Implemented
**Start Date:** 2026-05-16
**Target Completion:** TBD
**Focus:** Remove success-level selection when editing Routine activities

## Overview

This development cycle fixes a bug in the activity edit flow. Routine activities do not have Minimum, Medium, or High success levels, but editing a Routine activity currently sends the user to the normal success-level selection screen.

Routine activity creation already skips success-level selection. Editing a Routine activity should follow the same product rule.

## Bug Definition

### Current Behavior
- User edits a Routine activity from Dashboard, Calendar, or Timeline.
- The app navigates to `SelectLevelFragment`.
- The user is asked to choose a success level.
- This is incorrect because Routine activities are simply done/not done plus optional notes.

### Expected Behavior
- User edits a Routine activity.
- The app should not show Minimum, Medium, or High success choices.
- The flow should preserve the Routine activity's done/completed state.
- The user should still be able to edit notes and date/time where supported.

## Current Work Items

### Phase 1: Trace Routine Edit Entry Points
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** High
**Description:** Identify all places that launch the activity edit flow and determine how each should branch for Routine activities.

**Technical Requirements:**
- Review edit entry points in Dashboard, Calendar, and Timeline.
- Confirm each entry point has access to `pieceOrTechnique.taskKind`.
- Confirm how Routine creation skips success-level selection.
- Identify the safest shared helper or navigation branch to avoid duplicated logic.

**Acceptance Criteria:**
- [x] All edit entry points are identified.
- [x] Routine-vs-standard branching location is chosen.
- [x] Existing standard Task edit flow remains unchanged.

### Phase 2: Route Routine Edits Around Select Level
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** High
**Description:** Change Routine activity edit navigation so it bypasses `SelectLevelFragment`.

**Technical Requirements:**
- When editing a Routine activity, set edit state as today.
- Navigate directly to notes or summary with a fixed done/high-equivalent level.
- Keep `performanceType = "routine"` for Routine activities.
- Preserve existing activity notes during edit.
- Avoid showing success-level controls for Routine activities.

**Acceptance Criteria:**
- [x] Editing a Routine activity does not show success-level options.
- [x] Editing a Routine activity preserves the done/completed state.
- [x] Existing Routine notes are shown for editing.
- [x] Saving the edit updates the original Routine activity.

### Phase 3: Summary and Display Consistency
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** Medium
**Description:** Ensure the Routine edit summary and activity display use Routine language.

**Technical Requirements:**
- Summary should show `Activity: Done` for Routine activities.
- Dashboard and Calendar activity rows should continue to display Routine activities as done, not as success levels.
- Note detail dialogs should not show a success level label for Routine activities if possible.

**Acceptance Criteria:**
- [x] Routine edit summary does not mention Minimum, Medium, or High.
- [x] Routine activity detail/display remains consistent.
- [x] Standard activity display remains unchanged.

### Phase 4: Regression Verification
**Status:** Complete
**Date Added:** 2026-05-16
**Priority:** High
**Description:** Verify Routine and standard edit paths.

**Technical Requirements:**
- Create or use an existing Routine activity.
- Edit it from Dashboard.
- Edit it from Calendar.
- Edit it from Timeline, if Timeline remains reachable.
- Confirm no success-level screen appears for Routine edits.
- Confirm notes/date edits save to the same activity.
- Confirm standard Task activities still show the success-level edit screen.
- Run debug build.

**Acceptance Criteria:**
- [x] Routine edits bypass success-level selection from every edit entry point.
- [x] Standard Task edits still use success-level selection.
- [x] Routine edit saves update the intended existing activity.
- [x] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Add a small shared edit-navigation helper or consistent branch in each edit entry point.
2. For Routine activities, navigate directly to notes or summary with level `3` and `performanceType = "routine"`.
3. Ensure edit state is still set before navigation.
4. Verify Summary recognizes Routine task kind and displays `Activity: Done`.
5. Build and update this document.

## Implementation Notes

- Routine creation currently routes from task selection directly to notes with a fixed level and `performanceType = "routine"`.
- Dashboard, Calendar, and Timeline currently navigate all edits to `selectLevelFragment`.
- `SummaryFragment` already has Routine-aware display logic for `Activity: Done`.
- This fix should not alter persistence schema.
- Added a shared edit navigation helper that routes Routine activity edits directly to Notes with level `3` and `performanceType = "routine"`.
- Notes now initializes edit mode from `EditActivityStorage`, so Routine edit notes prepopulate even though `SelectLevelFragment` is skipped.
- Standard Task activity edits still route through `SelectLevelFragment`.
- `gradlew.bat assembleDebug` succeeds.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Routine edits lose existing notes | Prepopulate notes from edit state as the notes screen already does for standard edits. |
| Routine edit accidentally becomes a new activity | Preserve DC 9 edit-state safeguards and verify update path. |
| Standard edits regress | Keep standard task navigation through `SelectLevelFragment`. |
| Duplicate navigation logic drifts | Prefer a helper if the edit branches are repeated in multiple fragments. |

## Handoff Checklist

- [x] Confirm whether Routine edit should go to Notes first or Summary first.
- [ ] Confirm whether Routine detail dialogs should remove the `Done` line or keep it.
