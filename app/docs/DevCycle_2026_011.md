# Development Cycle 2026-011

**Status:** Open
**Start Date:** 2026-05-17
**Target Completion:** TBD
**Focus:** Change Calendar activity selection behavior

## Overview

This development cycle changes how activities behave on the Calendar tab. Today, selecting an activity on the Calendar page opens an activity detail dialog. That detail behavior should remain available on the Dashboard, but Calendar activity selection should become a highlight/toggle interaction instead.

The Calendar page should let the user select one activity visually. Selecting an unhighlighted activity highlights it. Selecting the highlighted activity again unhighlights it.

## Product Definition

### Current Behavior
- Dashboard activity press shows activity detail.
- Calendar activity press also shows activity detail.
- Calendar activity rows do not have a persistent selected/highlighted state.

### New Calendar Behavior
- Dashboard activity detail behavior remains unchanged.
- Calendar activity press does not show the activity detail dialog.
- Calendar activity press toggles selection/highlighting.
- Only one Calendar activity/task can be highlighted at a time.
- Highlight state is transient UI state and is not persisted.
- Only the Calendar tab behavior changes in Phase 1.

## Current Work Items

### Phase 1: Calendar Activity Highlight Toggle
**Status:** Open
**Date Added:** 2026-05-17
**Priority:** High
**Description:** Replace Calendar activity detail-on-press with activity row highlight toggling.

**Technical Requirements:**
- Identify the Calendar selected-date activity list row binding.
- Remove or bypass the Calendar row click behavior that opens the activity detail dialog.
- Add selected/highlighted state tracking for Calendar activities.
- Track the highlighted item by task ID, not by display name.
- When a Calendar activity row is pressed:
  - if it is not currently highlighted, highlight it.
  - if it is already highlighted, unhighlight it.
  - if another activity is highlighted, replace the previous highlight with the newly selected activity.
- Ensure only Calendar activity rows use this behavior.
- Preserve Dashboard activity row press behavior that shows activity detail.
- Preserve Calendar edit/delete controls.
- When the selected date changes, recalculate row highlighting based on the highlighted task ID.

**Acceptance Criteria:**
- [ ] Pressing an activity on the Calendar tab no longer opens the activity detail dialog.
- [ ] Pressing an unhighlighted Calendar activity highlights it.
- [ ] Pressing the highlighted Calendar activity unhighlights it.
- [ ] Pressing a different Calendar activity moves the highlight to that activity.
- [ ] Only one Calendar activity/task is highlighted at a time.
- [ ] Calendar row highlighting is recalculated correctly when the selected date changes.
- [ ] Dashboard activity detail dialog still opens when pressing a Dashboard activity.
- [ ] Calendar edit/delete buttons still work.
- [ ] `gradlew.bat assembleDebug` succeeds.

### Phase 2: Highlighted Activity Calendar Overlay
**Status:** Open
**Date Added:** 2026-05-17
**Priority:** High
**Description:** When a Calendar activity is highlighted, change the calendar day coloring to show where that same activity was performed.

**Technical Requirements:**
- Detect when a Calendar activity row is highlighted.
- While an activity is highlighted, switch the calendar day coloring into highlighted-activity mode.
- In highlighted-activity mode:
  - days should no longer use the normal activity/priority color coding.
  - every day where the highlighted activity's task ID was performed should be light purple.
  - days where that task ID was not performed should use the neutral no-activity appearance, even if other activities occurred on those days.
- When the highlighted activity is unhighlighted, the Calendar exits highlighted-activity mode and returns to default mode.
- Default mode means the normal activity/priority calendar color coding is restored.
- The comparison should be based on the highlighted activity's task ID, not the specific activity instance or task display name.
- Preserve the selected date behavior and selected activity list.
- Proposed light purple color: `#D8B4FE`.
- Highlighted-activity mode should remain active across selected-date changes while the Calendar tab remains active.
- Highlighted-activity mode should clear when the Calendar tab view is destroyed or the user unhighlights the selected activity.

**Acceptance Criteria:**
- [ ] Highlighting an activity switches the calendar into highlighted-activity mode.
- [ ] Days where the highlighted activity's task ID was performed are light purple.
- [ ] Days where that task ID was not performed use the neutral no-activity appearance.
- [ ] Normal priority/activity color coding is hidden while highlighted-activity mode is active.
- [ ] Unhighlighting the activity returns the Calendar to default mode.
- [ ] Default mode restores the normal activity/priority calendar color coding.
- [ ] Highlighted-activity mode persists correctly when the selected date changes.
- [ ] Dashboard behavior is unchanged.
- [ ] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Inspect Calendar activity row rendering and current click listeners.
2. Add Calendar-only selected activity state.
3. Replace Calendar row detail click with highlight toggle.
4. Add highlighted-activity calendar color overlay.
5. Verify Dashboard detail click remains unchanged.
6. Build and update this document.

## Implementation Notes

- This cycle intentionally keeps Dashboard behavior unchanged.
- Activity detail dialogs should remain available from Dashboard activity rows.
- Phase 1 should not introduce new persistence; highlighted activity can be transient UI state unless Phase 2 changes that requirement.
- Phase 2 also uses transient UI state. Highlighting an activity changes the calendar display while highlighted, but does not alter stored calendar state.
- Light purple should be visually distinct from the existing Calendar color scale.
- Calendar activity rows have edit/delete controls; row selection must not interfere with those button listeners.
- The highlighted activity should be represented internally by task ID so renamed tasks still behave correctly.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Dashboard detail behavior accidentally changes | Keep changes scoped to `CalendarFragment` and Calendar row binding only. |
| Edit/delete row controls conflict with row selection | Keep button click listeners independent from row click listener. |
| Highlight state behaves incorrectly when selected date changes | Recalculate visible row highlighting from the highlighted task ID when the selected date/activity list changes. |
| Highlight styling is too subtle | Use an existing selected/activated visual state or a clear background/swatch treatment. |
| Highlight overlay is confused with stored calendar colors | Treat it as a display-only mode and do not write highlighted colors to frozen calendar state. |
| Tab switches leave stale highlight state | Treat highlight as view-scoped transient state and clear it when the Calendar view is destroyed. |

## Handoff Checklist

- [x] Confirm whether only one Calendar activity can be highlighted at a time.
- [x] Confirm whether highlight should clear when the user selects a different calendar date.
- [x] Define Phase 2.
- [ ] Confirm exact light purple color value. Proposed: `#D8B4FE`.

## Claude Review

### Open Questions

1. **Single vs. multi-select (Handoff item):** The Handoff Checklist leaves this open. The Proposed Implementation Sequence and Phase 2 both read as if only one activity is ever highlighted. Confirm before Phase 1 implementation so the state model is correct from the start.

2. **Highlight cleared on date change (Handoff item):** The Risks table says to "clear or recalculate" on date change, but the Acceptance Criteria do not include a test step for this. Once confirmed, add it as an AC item in Phase 1 so it is explicitly verified.

3. **Light purple color value (Handoff item):** Phase 2 cannot be finalized without this. Existing calendar colors (priority/activity scale) should be checked so the chosen purple is visually distinct from all of them.

4. **Edit/delete controls on Calendar rows:** Phase 1 requires preserving them "if present." It is worth confirming now whether they exist, to avoid a last-minute discovery during implementation.

5. **Tab-switch behavior:** Both phases treat highlight as transient UI state. Should the highlight be cleared when the user navigates away from the Calendar tab and returns, or should it survive tab switches within the same session?

### Comments

6. **"Task" definition in Phase 2:** The requirement says the overlay comparison is based on "the highlighted activity's task, not only the specific activity instance." Clarifying whether "task" means the task record's ID or its display name would prevent an ambiguous implementation.

7. **Phase 2 — appearance of non-matching days:** The spec says non-matching days use "the normal no-highlight/no-activity appearance." Confirming whether this means they look identical to days with zero activities (blank/neutral) versus retaining any indicator would remove ambiguity.

8. **Risk not reflected in AC:** The risk "Clear or recalculate highlight state when the selected date/activity list changes" is only in the Risks table. If this behavior is required, it should appear in the Phase 1 Acceptance Criteria for explicit test coverage.

## Codex Response

- I agree with the Claude Review that the state model should be explicit before implementation. This document now defines a single highlighted task at a time.
- I recommend storing highlight state as a task ID rather than an activity ID or display name. That matches Phase 2's goal: show every day where the same task was performed.
- I recommend recalculating visible row highlight state when the selected date changes instead of clearing the highlight. This lets the user select a task/activity and browse the month while the purple overlay remains meaningful.
- Non-matching days in highlighted-activity mode should use the neutral no-activity appearance, even if other activities occurred on those days, because the overlay is answering one question: "When did I do this highlighted task?"
- `#D8B4FE` is a reasonable starting light purple. It should still be checked against the app's existing calendar colors during implementation.
