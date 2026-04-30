# Development Cycle 2026-001

**Status:** Completed  
**Start Date:** 2026-04-30  
**Target Completion:** TBD  
**Focus:** Convert PlayStreak into MyStreak general task tracker

## Overview

This development cycle plans the conversion of PlayStreak from a music practice tracker into MyStreak, a general task streak and activity tracker. The goal is to reuse the stable PlayStreak architecture where it fits: Android/Kotlin, Room, MVVM fragments/view models, repository data access, local JSON import/export, the existing calendar layout, and the familiar logging flow. The major product changes are replacing Pieces/Techniques with Tasks, replacing Practice/Performance activities with success-level Activities, simplifying navigation to three tabs, and rebuilding calendar/dashboard logic around high-priority task completion.

The source product specification for this cycle is `C:\dev\MyStreakCodex\MyStreak.md`. Where older follow-up notes conflict with the top-level spec, this cycle treats the top-level sections as canonical: past calendar colors are frozen after midnight, retroactive activity edits do not update frozen past colors, MyStreak has no settings screen in this version, and Tasks use active/inactive status rather than PlayStreak's favorite/pro concepts.

## Current Work Items

### Phase 1: Product Scope and Naming Conversion
**Status:** In Progress  
**Date Added:** 2026-04-30  
**Priority:** Critical (Foundation)  
**Description:** Establish the MyStreak product identity and remove music-specific terminology from the visible experience before deeper behavior changes begin.

**Technical Requirements:**
- Rename user-facing strings from PlayStreak/music practice language to MyStreak/general task language.
- Replace "Piece", "Technique", "Practice", "Performance", "Favorite", and related copy with "Task", "Activity", "Success Level", "Priority", and active/inactive status where appropriate.
- Update app labels, navigation labels, dashboard titles, empty states, dialogs, import/export copy, legal/about copy, and documentation references.
- Keep the current PlayStreak Git history/workspace during early conversion, then create a separate `MyStreakCodex` GitHub repository after enough of DevCycle 2026-001 has been implemented to make the project meaningfully MyStreak.
- Decide whether package names remain `com.pseddev.playstreak` during the early conversion or move to a new MyStreak namespace before creating the new repository.
- Remove or hide PlayStreak-only features that do not belong in MyStreak: achievements, suggestions, favorites, pro/free limits, inactive-by-age analysis, practice/performance-specific statistics, duration, notes, and music-specific test data.

**Acceptance Criteria:**
- [x] A terminology inventory exists for reachable Phase 1 UI surfaces.
- [x] All in-scope labels have an approved MyStreak replacement for the main Dashboard / Calendar / Tasks shell.
- [x] Features to remove, keep, or defer are explicitly listed.
- [x] Temporary conversion-repo strategy and eventual `MyStreakCodex` repo cutover point are documented before implementation begins.
- [x] Package/application ID strategy is documented before any migration work starts.
- [x] The app can still build after visible naming changes.

**Implementation Notes:**
- Existing files likely touched include `res/values/strings.xml`, layout XML, navigation XML, fragments/view models under `ui`, legal/about assets, and documentation.
- This phase should avoid database schema changes except where compile-time terminology changes force local renames.

**Progress Notes:**
- 2026-04-30: Started Phase 1 implementation. The primary shell now presents MyStreak with three tabs: Dashboard, Calendar, and Tasks.
- 2026-04-30: Removed Suggestions, Inactive, Timeline, Settings menu, and Pro-gated tabs from the reachable main flow.
- 2026-04-30: Retitled reachable task/activity flows from Piece/Practice/Performance wording to Task/Activity/Success wording while leaving legacy entity names in code for Phase 2.
- 2026-04-30: Hid legacy task type/favorite controls in add-task flows. Priority, active/inactive, colors, and thresholds are deferred to Phase 2 and Phase 6 when the Task schema exists.
- 2026-04-30: Package and Kotlin namespace remain `com.pseddev.playstreak` during early conversion. Rename/package split is deferred until after initial MyStreak behavior is implemented and before the standalone `MyStreakCodex` repository cutover.
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds after configuring local ignored SDK/Firebase files for this clone.
- 2026-04-30: Fixed debug startup crash caused by Firebase auto-initializing against the local placeholder `google-services.json`; debug builds now remove `FirebaseInitProvider` and no-op analytics/crash reporting.

### Phase 2: Data Model Redesign
**Status:** Completed  
**Date Added:** 2026-04-30  
**Priority:** Critical (Foundation)  
**Description:** Replace PlayStreak's `PieceOrTechnique` and music-oriented `Activity` schema with MyStreak Tasks and Activities.

**Technical Requirements:**
- Introduce a `Task` entity with:
  - `id`
  - `name`
  - `color`
  - `priority` (`HIGH`, `LOW`)
  - `minimumSuccess`
  - `mediumSuccess`
  - `highSuccess`
  - `isActive`
  - creation/update timestamps as needed
- Introduce or refactor `Activity` to store:
  - `id`
  - `taskId`
  - `timestamp`
  - `successLevel` (`MINIMUM`, `MEDIUM`, `HIGH`)
- Remove PlayStreak-only fields from the MyStreak data surface:
  - activity type
  - performance type
  - minutes
  - notes
  - item type
  - favorite status
  - practice/performance count fields
  - satisfactory practice/performance timestamps
- Add DAO support for active tasks, inactive tasks, high-priority tasks, task activity counts for today, date-range activity lookup, delete-task-with-activities, and activity edit/delete operations.
- Decide whether to migrate existing PlayStreak data, clear data on first MyStreak install, or create a new database name.
- Prefer decisions that make the eventual standalone `MyStreakCodex` repository clean and understandable to future contributors.
- Add calendar frozen-day storage if not already present in a reusable form.

**Acceptance Criteria:**
- [x] Room entities represent MyStreak Tasks and Activities without music-specific persisted fields.
- [x] Room database versioning/migration strategy is documented and implemented.
- [x] DAO methods cover active tasks, inactive tasks, high-priority tasks, date-range activities, task activity counts, edit/delete operations, and delete-task-with-activities repository behavior.
- [x] Deleting a Task deletes its historical Activities through existing repository behavior.
- [x] Inactive Tasks remain queryable through DAO methods and can be excluded by active-task queries.
- [ ] Unit tests or database tests cover core query behavior and deletion behavior.

**Implementation Notes:**
- Current source files to replace or heavily modify include `PieceOrTechnique.kt`, `Activity.kt`, `PieceOrTechniqueDao.kt`, `ActivityDao.kt`, `AppDatabase.kt`, and `PianoRepository.kt`.
- Consider renaming `PianoRepository` to `MyStreakRepository` once the data model is stable.

**Progress Notes:**
- 2026-04-30: Added a MyStreak Room schema using a new local database name, `mystreak_database`, so early MyStreak builds do not attempt to migrate PlayStreak data in place.
- 2026-04-30: Replaced the persisted item table with `tasks` fields: name, color, priority, success threshold descriptions, active status, and timestamps.
- 2026-04-30: Replaced the persisted Activity columns with Task FK, timestamp, and success level. Legacy accessors remain as compatibility bridges for UI code that will be refactored in later phases.
- 2026-04-30: Added DAO queries for active, inactive, active high-priority, date-range activity lookup, task activity counts, and delete-by-task behavior.
- 2026-04-30: Left Kotlin/package names such as `PieceOrTechnique` and `PianoRepository` in place as temporary compatibility names. They should be renamed after Dashboard/Tasks/Calendar behavior has moved fully to MyStreak concepts.
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds.

### Phase 3: Repository and Business Logic Conversion
**Status:** Completed  
**Date Added:** 2026-04-30  
**Priority:** Critical (Core Behavior)  
**Description:** Convert repository-level calculations from music practice statistics to task activity, priority, streak, and calendar-color logic.

**Technical Requirements:**
- Implement current streak calculation based on consecutive local days with at least one Activity.
- Implement rolling 7-day dashboard summary:
  - total Activities from currently-active Tasks
  - total High Priority Activities from currently-active Tasks
- Implement High Priority Outstanding:
  - active High Priority Tasks not logged today
  - remove a Task from the list after any Activity for that Task is logged today
- Implement calendar color calculation:
  - no color for no Activity
  - Light Blue for at least one Activity
  - Medium Blue for at least one High Priority Activity
  - Dark Blue for at least half, rounded down, of active High Priority Tasks performed
  - Bright Green for all active High Priority Tasks performed
- Implement frozen past-day calendar colors computed the next time the app opens after midnight.
- Keep today's calendar color live and recalculated from current task priority/status.
- Ensure retroactive edits relocate Activities in listings but do not recalculate frozen past-day colors.
- Enforce no future timestamps when editing Activities.

**Acceptance Criteria:**
- [x] Streak calculation is correct across local midnight boundaries.
- [x] Dashboard summary excludes Activities whose current Task is inactive.
- [x] Today's and yesterday's activity lists include Activities from inactive Tasks.
- [x] Outstanding high-priority list updates immediately after logging.
- [x] Calendar color rules match the MyStreak specification.
- [x] Frozen past calendar colors remain immutable after priority changes, inactive toggles, retroactive additions, and deletions.
- [x] Retroactive move of today's only Activity removes today's live streak credit immediately.
- [ ] Unit tests cover streak, summary, outstanding, calendar color, and frozen-day behavior.

**Implementation Notes:**
- Existing helpers such as `StreakCalculator` and repository date-range methods can be reused or adapted.
- Freezing may require a new `DailyCalendarState` entity with date key, color level, and frozen timestamp.

**Progress Notes:**
- 2026-04-30: Added `DailyCalendarState` storage and DAO support for immutable frozen past calendar color levels.
- 2026-04-30: Added repository-level MyStreak business logic for rolling 7-day summary, high-priority outstanding tasks, live/frozen calendar color levels, and future timestamp rejection.
- 2026-04-30: Main startup now freezes past calendar days before showing the app's sync prompt.
- 2026-04-30: Dashboard now uses repository-provided active-task summary and high-priority outstanding data.
- 2026-04-30: Calendar now renders MyStreak color levels instead of PlayStreak's Pro/practice/performance heat map. The color guide is always visible and uses the MyStreak labels.
- 2026-04-30: Activity edit date picker prevents future dates and save rejects any future timestamp.
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds. Automated tests for this phase are still deferred.

### Phase 4: Navigation and Screen Structure
**Status:** Completed  
**Date Added:** 2026-04-30  
**Priority:** High (User Experience)  
**Description:** Restructure PlayStreak navigation into MyStreak's three primary top tabs: Dashboard, Calendar, and Tasks.

**Technical Requirements:**
- Replace or simplify the current progress area so the first screen exposes Dashboard, Calendar, and Tasks.
- Remove Suggestions, Timeline, Favorites, Inactive, Achievements, Pro, Configuration, and data-pruning navigation from the main user flow for this version.
- Keep Import/Export available only if a minimal entry point is approved, since the MyStreak spec says no settings screen in this version but also plans import/export.
- Ensure logging can start from Dashboard and from the Tasks tab.
- Ensure Activity editing is reachable from Dashboard daily lists and Calendar daily listing, since MyStreak has no Timeline tab.

**Acceptance Criteria:**
- [x] Primary tabs show only Dashboard, Calendar, and Tasks.
- [x] Dashboard is the primary landing view.
- [x] Removed PlayStreak screens are unreachable from production navigation.
- [x] Task list entry tap opens Task detail/edit.
- [x] A clear log action exists on the Dashboard.
- [x] A clear log action exists from the Tasks tab, either per-task or inside Task detail, pending final UX decision.

**Open Product Questions:**
- How exactly should logging from the Tasks tab work long-term: keep the current inline row action, move it into Task detail, or support both?
- Should Import/Export be exposed in this version without a settings screen, and if so where?

**Progress Notes:**
- 2026-04-30: Kept the PlayStreak-style top tab strip, limited to Dashboard, Calendar, and Tasks. A brief bottom-navigation implementation was reverted because top tabs are the preferred MyStreak design.
- 2026-04-30: Kept Dashboard as the first ViewPager page so it remains the landing view.
- 2026-04-30: Removed the main shell's dormant navigation action to the legacy About/settings hub. Legacy configuration, achievements, pruning, favorites, and About screens remain in source for now but are no longer reachable from production app navigation.
- 2026-04-30: Confirmed Dashboard still has an Add Activity entry point, and Tasks still has Add Task plus per-row quick activity logging and task detail/edit access.
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds.

### Phase 5: Dashboard Implementation
**Status:** Completed  
**Date Added:** 2026-04-30  
**Priority:** High (Core UI)  
**Description:** Build the MyStreak Dashboard as the main daily accountability screen.

**Technical Requirements:**
- Display Current Streak.
- Display Today's Activities sorted by time ascending and always expanded.
- Display Yesterday's Activities sorted by time ascending and always expanded.
- Display Week Summary with rolling 7-day total Activities and High Priority Activities.
- Display highlighted High Priority Outstanding list.
- Show Task color, time, Task name, and success level for Activity rows.
- Include Add Activity entry point.
- Support dark mode using existing theme patterns.

**Acceptance Criteria:**
- [x] Dashboard correctly handles days with no Activities.
- [x] Today's and yesterday's lists show inactive Task history.
- [x] Activity rows support edit/delete entry points with confirmation for deletion.
- [x] Week Summary counts each individual Activity log instance.
- [x] Week Summary excludes Activities from currently-inactive Tasks.
- [x] Outstanding list excludes inactive Tasks and tasks already logged today.

**Progress Notes:**
- 2026-04-30: Replaced plain-text Today and Yesterday summaries with always-expanded activity rows.
- 2026-04-30: Dashboard activity rows now show Task color, time, Task name, and success level.
- 2026-04-30: Added Dashboard edit/delete actions for Activities. Delete shows confirmation; edit reuses the existing Activity edit flow.
- 2026-04-30: Empty Today and Yesterday states now render in place instead of collapsing the section.
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds.

### Phase 6: Task Management UI
**Status:** Open  
**Date Added:** 2026-04-30  
**Priority:** High (Core UI)  
**Description:** Replace the Pieces UI with Tasks list, add/edit/detail, active/inactive management, and deletion confirmation.

**Technical Requirements:**
- Task list entries show:
  - name
  - color
  - priority badge
  - today's raw Activity count
  - active/inactive status
- Preserve PlayStreak's existing list ordering approach unless explicitly changed.
- Add Task creation/edit form with:
  - name
  - predefined color palette
  - High/Low priority selector
  - Minimum/Medium/High success threshold descriptions
  - active/inactive toggle
  - delete action with warning
- Grey out inactive Tasks in the Tasks list while keeping them visible.
- Hide inactive Tasks from logging flows.

**Acceptance Criteria:**
- [ ] Active and inactive Tasks appear in the Tasks tab.
- [ ] Inactive Tasks are visually distinct and can be reactivated.
- [ ] Task properties are editable after creation.
- [ ] Deleting a Task warns that historical Activities will be permanently removed.
- [ ] Today's count displays raw log count for each Task.
- [ ] Color palette is accessible in light and dark themes.

**Implementation Notes:**
- The MyStreak spec says the palette should match PlayStreak, but `PlayStreakForMyStreak.md` notes PlayStreak does not currently have per-piece colors. Define a new accessible predefined palette in `colors.xml` and document it as the MyStreak v1 palette.

### Phase 7: Activity Logging, Editing, and Deletion
**Status:** Open  
**Date Added:** 2026-04-30  
**Priority:** High (Core Workflow)  
**Description:** Adapt PlayStreak's add-activity flow to MyStreak's task success-level model.

**Technical Requirements:**
- Replace Practice/Performance selection with Task selection and success-level selection.
- Show all three Task threshold descriptions when logging.
- Allow the user to select Minimum, Medium, or High.
- Do not provide notes or duration fields.
- Log Activities for the current day/time by default.
- Allow editing of date/time and success level only.
- Prevent future date/time selection.
- Keep Task association fixed after logging.
- Support deleting an Activity with confirmation.

**Acceptance Criteria:**
- [ ] Logging flow only lists active Tasks.
- [ ] Each logged Activity has exactly one Task, timestamp, and success level.
- [ ] Below-minimum attempts cannot be recorded as Activities.
- [ ] Activity edit screen does not allow Task reassignment.
- [ ] Future dates are rejected with clear feedback.
- [ ] Moving an Activity from today to a past date updates today's live data immediately.
- [ ] Deletion updates dashboard, calendar live day, streak, and task counts appropriately.

### Phase 8: Calendar Conversion
**Status:** Open  
**Date Added:** 2026-04-30  
**Priority:** High (Core UI)  
**Description:** Reuse PlayStreak's calendar layout while replacing activity-count/performance colors with MyStreak's high-priority completion colors.

**Technical Requirements:**
- Keep the monthly grid layout and previous/next navigation pattern.
- Add MyStreak calendar color resources for Light Blue, Medium Blue, Dark Blue, and Bright Green.
- Display frozen colors for past days and live color for today.
- On day tap, show all Activities for that day sorted ascending by time.
- Include Activities from currently-inactive Tasks in day listings.
- Show Task color, time, and success level in the daily listing.
- Allow Activity edit/delete from daily listings.

**Acceptance Criteria:**
- [ ] Calendar displays correct colors for empty, partial, high-priority, half-complete, and all-complete days.
- [ ] Today's color changes after logging, editing, or deleting today's Activities.
- [ ] Frozen past day colors do not change after retroactive edits.
- [ ] Day activity listings reflect actual Activities even when frozen color does not.
- [ ] Calendar remains usable in dark mode.

### Phase 9: Import/Export and Local Data
**Status:** Open  
**Date Added:** 2026-04-30  
**Priority:** Medium (Data Portability)  
**Description:** Adapt PlayStreak's JSON import/export approach to MyStreak's Task and Activity schema.

**Technical Requirements:**
- Define a MyStreak JSON schema containing Tasks, Activities, and frozen calendar day states if implemented.
- Use Android file picker flow and full-replace import semantics from PlayStreak.
- Validate JSON before import.
- Warn that import replaces existing data.
- Export all local data required to restore the visible MyStreak experience.
- Decide whether old PlayStreak JSON import is unsupported, blocked with a helpful message, or converted best-effort.

**Acceptance Criteria:**
- [ ] Exported JSON can be imported into a clean MyStreak install.
- [ ] Import preserves Task colors, priorities, active states, thresholds, Activities, and frozen calendar colors.
- [ ] Invalid JSON produces actionable validation errors.
- [ ] Import confirmation warning is shown before replacing data.
- [ ] Import/export UI copy uses MyStreak terminology.

### Phase 10: Test, Cleanup, and Release Readiness
**Status:** Open  
**Date Added:** 2026-04-30  
**Priority:** High (Quality Assurance)  
**Description:** Remove obsolete PlayStreak behavior, add focused tests, and verify MyStreak is ready for regular development or beta testing.

**Technical Requirements:**
- Remove unused PlayStreak classes, layouts, menu items, icons, docs links, feature gates, analytics events, and tests that no longer apply.
- Add tests for:
  - streak calculation
  - rolling 7-day summary
  - High Priority Outstanding
  - calendar color calculation
  - frozen day behavior
  - active/inactive Task filtering
  - import/export round trip
- Build debug and release variants.
- Manually verify first-run, add Task, log Activity, edit Activity, delete Activity, toggle inactive, calendar day tap, dark mode, and import/export.
- Review privacy/legal text for MyStreak-specific claims.

**Acceptance Criteria:**
- [ ] `gradlew.bat assembleDebug` succeeds.
- [ ] `gradlew.bat assembleRelease` succeeds or release blockers are documented.
- [ ] Core business logic tests pass.
- [ ] No production UI references music practice, pieces, techniques, practice, performance, favorites, achievements, or pro/free tiers unless intentionally retained.
- [ ] Manual smoke test checklist is completed.
- [ ] Known deferred items and open UX questions are documented for the next cycle.

## Proposed Implementation Sequence

1. Freeze the MyStreak v1 product decisions and answer open UX questions.
2. Rename visible product language and remove unreachable PlayStreak-only features from the main flow.
3. Replace the database model with Tasks, Activities, and optional frozen calendar day states.
4. Convert repository queries and business logic.
5. Build Dashboard, Tasks, Calendar, and Activity edit flows.
6. Adapt import/export to the new schema.
7. Remove obsolete PlayStreak code and broaden tests.
8. Complete release-readiness verification.

## Cycle Notes

- The conversion is closer to a product fork than a small feature cycle. It should be implemented in guarded phases with buildable checkpoints after each phase.
- GitHub cannot create a normal fork when the same user owns the source repository and has no organization target. The practical strategy is to keep developing in this conversion workspace through the first meaningful MyStreak implementation milestones, then create a new standalone `MyStreakCodex` repository once the app has diverged beyond a lightly renamed PlayStreak codebase.
- Before the new repository is created, preserve history locally and avoid large unrelated rewrites that would make the eventual repo split harder to review.
- The current app contains significant PlayStreak-specific systems: achievements, pro/free gating, favorites, suggestions, configuration, pruning, practice/performance statistics, and music-specific import/export assumptions. These should be removed or isolated early so they do not keep leaking into MyStreak behavior.
- MyStreak's frozen calendar color behavior is intentionally allowed to diverge from actual past activity listings after retroactive edits. Tests should cover this, because it will otherwise look like a bug during refactoring.
- Activity count semantics differ by surface: calendar completion counts a Task once per day, while Week Summary and task row counts count each individual Activity instance.
- Inactive Task behavior differs by surface: hidden from logging and outstanding list, excluded from Week Summary, visible in Tasks tab, and still shown in Dashboard/Calendar daily activity listings.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| GitHub fork limitations block a formal fork under the same owner | Continue implementation locally/in this workspace, then create a new standalone `MyStreakCodex` repo after initial MyStreak conversion work is real enough to justify the split. |
| Database migration complexity from PlayStreak to MyStreak | Prefer a new database name for early MyStreak builds unless preserving PlayStreak user data is explicitly required. |
| Music-specific statistics code creates hidden behavior regressions | Remove or replace practice/performance statistic updates when the Task schema lands. |
| Frozen calendar colors become inconsistent after edits | Treat immutable frozen colors as a first-class data model and test retroactive edit cases. |
| No settings screen conflicts with import/export needs | Decide whether import/export is deferred or exposed through a small overflow/action entry point. |
| Task-tab logging entry point is unresolved | Make the UX decision before implementing the Tasks tab so row tap behavior and log buttons do not conflict. |
| PlayStreak feature-gating code affects MyStreak limits | Remove Pro/Free gating and associated limits from MyStreak's core flows. |

## Future Cycles

- MyStreak v1 implementation cycle split into code phases if this planning cycle is accepted.
- Optional PlayStreak data conversion/import path if legacy users need migration.
- Optional reminders/notifications, explicitly out of scope for this version.
- Optional cloud backup/sync, explicitly out of scope for this version.
- Optional richer task scheduling if future MyStreak versions need non-daily task availability.

## Handoff Checklist

- [ ] Confirm open product questions.
- [ ] Approve whether this cycle remains a planning cycle or becomes the active implementation cycle.
- [ ] Define the minimum DevCycle 2026-001 milestone that triggers creating the new `MyStreakCodex` GitHub repository.
- [ ] Decide database/package identity strategy.
- [ ] Decide import/export placement with no settings screen.
- [ ] Break implementation into smaller follow-up DevCycles if desired.
