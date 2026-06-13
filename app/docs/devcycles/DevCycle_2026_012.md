# Development Cycle 2026-012

**Status:** Complete
**Start Date:** 2026-05-18
**Target Completion:** 2026-05-18
**Focus:** Standardize routine task color

## Overview

This development cycle makes routine tasks visually distinct and consistent by giving all routines a fixed light gray color. Routine colors should no longer be treated as user-selected values. Standard tasks should continue to use their chosen colors.

## Product Definition

### Current Behavior
- Routine tasks can inherit or retain arbitrary task colors.
- Routine-related UI can show routine rows and routine activities with different colors.
- Imported or existing routine data may contain non-gray color values.

### New Behavior
- Every routine task uses light gray as its displayed color.
- New routine tasks are saved with light gray.
- Edited routine tasks remain light gray.
- Existing routine tasks display as light gray, even if their stored color is different.
- Imported routine tasks are normalized to light gray.
- Standard task colors are unchanged.

## Current Work Items

### Phase 1: Define Routine Color Standard
**Status:** Complete
**Date Added:** 2026-05-18
**Priority:** High
**Description:** Add a single app-level routine color definition and use it as the source of truth for routine display.

**Technical Requirements:**
- Define a routine color constant or resource.
- Proposed routine color: light gray `#D3D3D3`.
- Use the routine color whenever a task has `taskKind = ROUTINE`.
- Keep standard task color handling unchanged.

**Acceptance Criteria:**
- [x] Routine color is defined in one clear place.
- [x] Routine display code can use the routine color without duplicating the literal value.
- [x] Standard task color display remains unchanged.

### Phase 2: Normalize Routine Create and Edit Flows
**Status:** Complete
**Date Added:** 2026-05-18
**Priority:** High
**Description:** Ensure new and edited routine tasks are stored with the routine light gray color.

**Technical Requirements:**
- When creating a routine task, force the stored color to the routine light gray color.
- When editing a routine task, preserve or reset the stored color to routine light gray.
- Hide or disable routine color selection in routine create/edit UI.
- Leave standard task color selection unchanged.

**Acceptance Criteria:**
- [x] New routine tasks are saved with light gray.
- [x] Edited routine tasks remain light gray.
- [x] Routine create/edit UI does not invite the user to choose a routine color.
- [x] Standard task create/edit color selection still works.

### Phase 3: Normalize Existing and Imported Routine Data
**Status:** Complete
**Date Added:** 2026-05-18
**Priority:** Medium
**Description:** Make sure routines created before this change, or imported from JSON, behave consistently.

**Technical Requirements:**
- Display existing routine tasks as light gray regardless of stored color.
- Consider normalizing existing routine records during save/edit or data load if that fits the current data layer cleanly.
- Normalize imported routine tasks to light gray.
- Export routine tasks with light gray so exported data matches the product rule.

**Acceptance Criteria:**
- [x] Existing routine tasks display as light gray.
- [x] Imported routine tasks display as light gray.
- [x] Exported routine task data uses light gray.
- [x] No migration is required unless the implementation finds a clean, low-risk path.

### Phase 4: Verify Routine Color Surfaces
**Status:** Complete
**Date Added:** 2026-05-18
**Priority:** High
**Description:** Check all routine-related UI surfaces for consistent color behavior.

**Technical Requirements:**
- Verify the Routine tab.
- Verify Add Activity routine selections.
- Verify routine activity rows on Dashboard and Calendar.
- Verify Calendar behavior does not confuse routine light gray with normal day status colors.
- Build the app after changes.

**Acceptance Criteria:**
- [x] Routine tab rows show light gray.
- [x] Routine activities show light gray indicators.
- [x] Standard task activities still show their selected task colors.
- [x] Calendar day status coloring is not changed by this cycle except where routine activity row indicators are shown.
- [x] `gradlew.bat assembleDebug` succeeds.

## Proposed Implementation Sequence

1. Add a routine color constant or resource.
2. Route routine task display through the routine color rule.
3. Update routine create/edit persistence and hide routine color controls.
4. Normalize import/export behavior for routine task colors.
5. Verify routine surfaces and build.
6. Update this document with implementation notes and completion status.

## Implementation Notes

- This cycle should not change the meaning of routine completion. Routine tasks still have no minimum/medium/high success levels.
- This cycle should not affect standard task priority, success levels, or selected colors.
- A display-level guard is useful even if persistence is normalized, because older data may already contain routine records with arbitrary colors.
- If the implementation uses a color resource, keep the stored/exported color value aligned with the same hex value.
- Implemented `TaskColors` as the single Kotlin source for routine color behavior, with `ROUTINE_COLOR = "#D3D3D3"`.
- Added `task_routine_light_gray` as the matching Android color resource.
- Routine create and edit flows now force the stored color to light gray and hide routine color controls.
- Routine task/activity display now resolves routine colors through `TaskColors.displayColorFor`, so existing records with older colors display as light gray.
- JSON export writes routine tasks as light gray, and JSON import normalizes routine tasks to light gray.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Existing routine data keeps old colors | Apply the routine color rule at display time and normalize on save/import/export where practical. |
| Standard task colors are accidentally changed | Gate the new behavior strictly on `TaskKind.ROUTINE`. |
| Routine create/edit UI still shows color controls | Explicitly hide or disable color selection when editing or creating routines. |
| Calendar day colors are unintentionally altered | Limit this cycle to task/activity indicators, not calendar day status calculations. |

## Handoff Checklist

- [x] Confirm final light gray value. Implemented: `#D3D3D3`.
- [x] Confirm whether routine color should be persisted as light gray or only enforced at display time.
- [x] Confirm import/export normalization behavior.
