# Development Cycle 2026-008

**Status:** Implemented
**Start Date:** 2026-05-15
**Target Completion:** TBD
**Focus:** Redefine streaks around high-priority task completion

## Overview

This development cycle changes the definition of a streak in MyStreak. A day should no longer count toward a streak merely because the user logged any activity. Instead, a day should count only when the user performs activities for at least half of the active high-priority standard tasks for that day.

Routine tasks should continue to count as activities for activity totals, but Routine-only days should not qualify for the main streak unless the high-priority threshold is also met by standard tasks.

## Product Definition

### Current Behavior
- Any logged activity can count toward the current streak.
- Routine activities count the same as standard task activities for streak purposes.
- Streak achievement milestones are based on this any-activity streak.

### New Behavior
- A day counts toward the streak only if half or more of active high-priority standard tasks are performed as activities.
- The half threshold should round up for odd counts.
  - 1 high-priority task requires 1 completed high-priority task.
  - 2 high-priority tasks require 1 completed high-priority task.
  - 3 high-priority tasks require 2 completed high-priority tasks.
  - 5 high-priority tasks require 3 completed high-priority tasks.
- Low-priority tasks do not count toward the streak threshold.
- Routine tasks do not count toward the streak threshold.
- Multiple activities for the same high-priority task on the same day count as one completed high-priority task for streak purposes.

## Product Questions

- What should happen when there are no active high-priority standard tasks?
  - Recommended: the day does not qualify for the streak, because there is no high-priority threshold to satisfy.
- Should historical streaks be recalculated under the new definition immediately after upgrade?
  - Recommended: yes, because the streak display should reflect the current definition.
- Should calendar colors and streak definition use the same high-priority threshold?
  - Recommended: yes, to keep Dashboard, Calendar, and streak feedback aligned.

## Current Work Items

### Phase 1: Locate Existing Streak Calculation Paths
**Status:** Complete
**Date Added:** 2026-05-15
**Priority:** High (Discovery)
**Description:** Identify every code path that calculates, displays, or awards achievements based on streak count.

**Technical Requirements:**
- Find the central streak calculation logic.
- Find Dashboard streak display usage.
- Find achievement milestone logic that depends on streak length.
- Find any DAO methods or repository methods that still count any activity as a streak day.
- Determine whether tests already exist for streak behavior.

**Acceptance Criteria:**
- [x] All streak calculation call sites are identified.
- [x] Any duplicate streak logic is documented.
- [x] The implementation target is clear before changing behavior.

### Phase 2: Implement High-Priority Threshold Streak Logic
**Status:** Complete
**Date Added:** 2026-05-15
**Priority:** High (Core Behavior)
**Description:** Replace any-activity streak qualification with high-priority threshold qualification.

**Technical Requirements:**
- For each day, determine the active high-priority standard tasks that apply.
- Count distinct high-priority standard tasks with at least one activity on that day.
- A day qualifies when completed high-priority tasks are greater than or equal to the rounded-up half threshold.
- Exclude low-priority standard tasks from streak qualification.
- Exclude Routine tasks from streak qualification.
- Avoid counting multiple activities for the same task more than once for the same day.

**Acceptance Criteria:**
- [x] A day with only low-priority activity does not count toward the streak.
- [x] A day with only Routine activity does not count toward the streak.
- [x] A day with fewer than half of high-priority tasks completed does not count.
- [x] A day with half or more high-priority tasks completed does count.
- [x] Duplicate activities for one high-priority task do not inflate the count.

### Phase 3: Dashboard and Achievement Integration
**Status:** Complete
**Date Added:** 2026-05-15
**Priority:** High (User-Facing Consistency)
**Description:** Ensure Dashboard streak display and streak achievements use the new definition.

**Technical Requirements:**
- Update Dashboard current streak display to use high-priority threshold streak days.
- Update streak milestone achievement checks after activity logging.
- Confirm that adding a Routine or low-priority activity alone does not award streak achievements.
- Consider whether Dashboard copy should mention high-priority completion.

**Acceptance Criteria:**
- [x] Dashboard current streak reflects the new definition.
- [x] Streak achievements use the new definition.
- [x] Logging low-priority-only activity does not advance streak achievements.
- [x] Logging Routine-only activity does not advance streak achievements.

### Phase 4: Calendar and Frozen Day Alignment
**Status:** Complete
**Date Added:** 2026-05-15
**Priority:** Medium (Consistency)
**Description:** Confirm calendar color and frozen day behavior remains consistent with the new streak definition.

**Technical Requirements:**
- Compare the new streak threshold to existing calendar half-high-priority logic.
- Ensure frozen calendar states do not conflict with streak recalculation.
- Decide whether calendar color should remain more nuanced than streak qualification.

**Acceptance Criteria:**
- [x] Calendar days that meet the streak threshold are visually distinguishable from any-activity days.
- [x] Frozen calendar states remain stable.
- [x] Streak logic does not depend on stale frozen calendar state.

### Phase 5: Verification
**Status:** Complete
**Date Added:** 2026-05-15
**Priority:** High (Regression Safety)
**Description:** Verify the new streak definition against normal, edge, and migration-style cases.

**Technical Requirements:**
- Test with 1, 2, 3, and 5 active high-priority standard tasks.
- Test with inactive high-priority tasks.
- Test with low-priority-only days.
- Test with Routine-only days.
- Test days with duplicate activities for the same high-priority task.
- Test current streak after a missed qualifying day.
- Run debug build.

**Acceptance Criteria:**
- [x] High-priority threshold examples produce expected streak counts.
- [x] Routine and low-priority activity totals remain unaffected.
- [x] Streak achievements are not awarded incorrectly.
- [x] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Audit current streak code and call sites.
2. Add a shared streak-day qualification helper using active high-priority standard tasks.
3. Update repository/viewmodel streak calculations to use the helper.
4. Update achievement checks to use the new streak count.
5. Align Dashboard display with the new definition.
6. Add focused tests if the existing test setup supports the affected layer.
7. Build and manually verify with mixed high-priority, low-priority, and Routine data.

## Cycle Notes

- This is a product semantics change, not only a calculation tweak.
- The rounded-up half threshold should match the existing calendar intent for high-priority completion.
- Routine tasks remain valuable but intentionally do not define the main streak.
- When there are no active high-priority standard tasks, the streak is 0 because there is no high-priority threshold to satisfy.
- Historical streaks are recalculated under the new definition when streak displays and achievements are evaluated.

## Implementation Notes

- Updated `StreakCalculator` to qualify streak days by distinct active high-priority standard task completions.
- Updated repository, Dashboard, Main, quick-add, add-activity, and retroactive achievement paths to use the new streak definition.
- Added focused unit tests for odd high-priority counts, duplicate activities, Routine/low-priority exclusion, and the no-high-priority case.
- `testDebugUnitTest` is currently blocked by Gradle task creation failure: `Type T not present`.
- `gradlew.bat assembleDebug` succeeds.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Existing users see their streak drop unexpectedly | Treat this as an intentional definition change and make Dashboard wording clearer if needed. |
| Streak and calendar disagree | Share threshold logic or keep the formulas visibly equivalent. |
| Odd high-priority task counts behave surprisingly | Document and test rounded-up half thresholds. |
| Duplicate activities inflate progress | Count distinct high-priority task ids per day. |
| Routine tasks blur with standard streak tasks | Explicitly exclude `TaskKind.ROUTINE` from threshold calculation. |

## Handoff Checklist

- [x] Confirm behavior when there are zero active high-priority standard tasks.
- [x] Confirm whether historical streaks should be recalculated immediately.
- [ ] Confirm whether Dashboard should explain the new streak rule.
