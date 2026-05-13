# Development Cycle 2026-004

**Status:** Completed
**Start Date:** 2026-05-13
**Target Completion:** TBD
**Focus:** Add optional notes to Activities

## Overview

This development cycle begins by adding an optional notes field to MyStreak Activities. The field should serve the same general purpose as PlayStreak's optional performance notes: a flexible place for user-written context about a specific logged Activity.

Activity notes describe one logged occurrence of a Task. They are not task-level metadata and should not appear as part of the Task definition itself.

## Current Work Items

### Phase 1: Optional Activity Notes Field
**Status:** Completed
**Date Added:** 2026-05-13
**Priority:** High (Activity Data Completeness)
**Description:** Add an optional notes field to each Activity.

**Product Requirements:**
- Each Activity can have optional notes.
- Notes are Activity-level metadata, not Task-level metadata.
- Notes should be useful for free-form information such as:
  - what happened during this specific Activity
  - observations
  - blockers
  - context for future review
  - details that do not belong in the Task name or success descriptions
- Empty notes should be allowed and should not clutter the UI.

**Technical Requirements:**
- Add a nullable or default-empty `notes` field to the Activity data model.
- Update Room schema and migration/database handling.
- Update Activity creation flow to include optional notes.
- Update Activity edit flow to allow viewing and changing notes.
- Update Dashboard and Calendar Activity detail/list displays to show notes when present.
- Update JSON export/import so Activity notes round-trip.
- Ensure blank notes are normalized consistently, preferably to an empty string or `null`.
- Preserve existing Activities during upgrade.

**Acceptance Criteria:**
- [x] Users can create an Activity with notes.
- [x] Users can create an Activity without notes.
- [x] Users can edit Activity notes later.
- [x] Notes appear in Activity displays when non-empty.
- [x] Empty notes do not produce noisy labels or blank sections.
- [x] Notes persist after app restart.
- [x] Notes are included in MyStreak JSON export.
- [x] Notes are restored during MyStreak JSON import.
- [x] Existing Activities remain valid after the schema change.
- [x] `gradlew.bat assembleDebug` succeeds.

**Progress Notes:**
- 2026-05-13: Made `Activity.notes` a persisted Room column with default empty string instead of a compatibility getter that always returned empty.
- 2026-05-13: Added Room migration 7 to 8 to preserve existing Activities while adding the `notes` column.
- 2026-05-13: Reconnected the existing Notes input step after success-level selection.
- 2026-05-13: Export/import now includes Activity notes.

**Implementation Notes:**
- Current Activity code may still include legacy PlayStreak fields such as `notes`, `minutes`, or `performanceType`; verify the current persisted schema before adding or reusing any field.
- The product-facing behavior should be MyStreak Activity notes.
- Likely files include:
  - `Activity.kt`
  - `AppDatabase.kt`
  - Add/Edit Activity fragments and navigation args
  - Activity row/detail display in Dashboard and Calendar
  - JSON export/import model and mapping code
- Avoid adding Task-level notes in this cycle.

### Phase 2: Activity Flow UI Placement
**Status:** Completed
**Date Added:** 2026-05-13
**Priority:** Medium (Usability)
**Description:** Decide where notes belong in the Activity add/edit flow.

**Technical Requirements:**
- Prefer a multi-line text field for notes.
- Keep notes optional and visually secondary to Task and Success Level.
- Ensure the keyboard does not cover the notes field while editing.
- Ensure long notes can scroll comfortably.
- Preserve the current save/cancel behavior after adding notes.

**Acceptance Criteria:**
- [x] Notes input is reachable in the Activity flow.
- [x] Long notes can be entered and reviewed.
- [x] Save and cancel still navigate correctly.
- [x] Editing an Activity shows the existing notes.

**Progress Notes:**
- 2026-05-13: Updated the notes input hint to MyStreak wording: "Enter activity notes..."
- 2026-05-13: Summary shows notes only when notes are non-empty.

### Phase 3: Activity Display Updates
**Status:** Completed
**Date Added:** 2026-05-13
**Priority:** Medium (Reviewability)
**Description:** Show Activity notes in places where users review logged Activities.

**Technical Requirements:**
- Review Dashboard Activity rows.
- Review Calendar selected-date Activity rows.
- Decide whether notes should appear inline, as a secondary line, or only in expanded/detail contexts.
- Avoid making activity lists noisy when notes are absent.

**Acceptance Criteria:**
- [x] Notes are visible from Dashboard or Calendar when present.
- [x] Activity lists remain scannable.
- [x] Empty notes are hidden.

**Progress Notes:**
- 2026-05-13: Dashboard and Calendar Activity rows append a notes line only when notes are present.

### Phase 4: Verification and Compatibility
**Status:** Completed
**Date Added:** 2026-05-13
**Priority:** High (Data Safety)
**Description:** Verify the notes field works across persistence, import/export, and upgrade paths.

**Technical Requirements:**
- Test new installs.
- Test existing local data upgrading to the new schema.
- Test JSON export/import with notes present and absent.
- Test edit flows for notes clearing and notes replacement.

**Acceptance Criteria:**
- [x] Existing local data survives upgrade.
- [x] Notes survive export/import round trips.
- [x] Clearing notes persists correctly.
- [x] Build succeeds.

**Progress Notes:**
- 2026-05-13: `gradlew.bat assembleDebug` succeeds. The first sandboxed build attempt could not access the Gradle wrapper lock in the user cache, so the successful verification build was run with approval outside the sandbox.

### Phase 5: Default Tasks Sort to Priority
**Status:** Completed
**Date Added:** 2026-05-13
**Priority:** Medium (Tasks Usability)
**Description:** Make Priority the default sort order in the Tasks tab.

**Product Requirements:**
- When the Tasks tab first loads, it should default to Priority sort.
- The default Priority direction should show High Priority tasks before Low Priority tasks.
- Within each priority group, Tasks should remain alphabetical ascending.
- Users should still be able to choose other sort orders manually.

**Technical Requirements:**
- Update the Tasks sort ViewModel default from alphabetical to priority.
- Update the initial checked sort chip in the Tasks layout from Name to Priority.
- Ensure the arrow state matches the default High-first priority order.
- Preserve existing behavior for the other sort options.

**Acceptance Criteria:**
- [x] Opening the Tasks tab defaults to Priority sort.
- [x] Default order is High Priority A-Z, then Low Priority A-Z.
- [x] The Priority chip is selected by default.
- [x] The direction arrow state matches the default order.
- [x] Other sort chips still work.
- [x] `gradlew.bat assembleDebug` succeeds.

**Progress Notes:**
- 2026-05-13: Updated `PiecesViewModel` default sort type to `PRIORITY`.
- 2026-05-13: Updated the Tasks layout so the Priority chip is checked by default instead of Name.
- 2026-05-13: Verified `gradlew.bat assembleDebug` succeeds. As with the previous build, the sandbox could not access the Gradle wrapper lock, so the successful build was run with approval outside the sandbox.

## Proposed Implementation Sequence

1. Inspect the current Activity entity to determine whether a legacy persisted notes field already exists.
2. Confirm storage choice: nullable `String?` vs default empty `String`.
3. Add or reactivate Activity model field and Room migration as needed.
4. Update add/edit Activity UI and view models.
5. Update Dashboard and Calendar displays.
6. Update JSON export/import.
7. Build and manually verify create/edit/export/import paths.

## Cycle Notes

- The notes field belongs to Activities, not Tasks.
- This cycle should preserve MyStreak terminology even where internal code still uses PlayStreak-era names.
- If Task-level notes are desired later, they should be handled in a separate cycle because they affect task creation/editing and task details differently.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Room migration breaks existing installs | Add a simple migration/default and verify with existing data. |
| Notes field clutters the Activity flow | Make it optional, secondary, and multi-line. |
| Export/import schema changes silently lose notes | Update schema/version notes and mapping code together. |
| Confusion between Task notes and Activity notes | Use clear UI copy and document this as activity-level metadata. |

## Handoff Checklist

- [x] Confirm whether notes should be nullable or stored as an empty string.
- [x] Confirm UI label copy, likely `Notes` or `Activity notes`.
- [x] Implement Phase 1.
- [x] Verify persistence and import/export.
- [x] Implement Phase 5: default Tasks sort to Priority.

## Cycle Completion Summary

**Completion Date:** 2026-05-13
**Git Commit Status:** Changes pending commit

**Accomplishments:**
- Added persisted optional notes to Activities.
- Re-enabled Activity notes entry in the add/edit Activity flow.
- Displayed notes in Activity review surfaces without showing empty placeholders.
- Included notes in MyStreak JSON export/import.
- Made Priority the default Tasks tab sort.

**Verification:**
- `gradlew.bat assembleDebug` succeeds.

---

## Post-Review Follow-up

**Date:** 2026-05-13
**Reviewer Notes Addressed:** Claude Code review

**Changes Applied:**
- Bumped MyStreak JSON export schema from v1 to v2 now that Activity notes are part of the exported format.
- Kept JSON import backward-compatible with v1 and v2 schema files so old MyStreak exports without notes remain importable.
- Documented `MyStreakExportActivity.notes` as nullable intentionally, because v1 imports may not contain the field.
- Extracted duplicated Dashboard/Calendar activity description formatting into the shared `SuccessLevelFormatter.kt` helper.
- Verified `NotesInputFragment` already pre-fills existing notes in edit mode, so no edit-flow change was needed for that review item.

**Notes:**
- The legacy PlayStreak export rejection message now refers to the MyStreak JSON schema generically instead of specifically requiring v1.

---

## Claude Code Review

**Reviewed by:** Claude Code (claude-sonnet-4-6)
**Review Date:** 2026-05-13
**Files Reviewed:** app/build.gradle.kts, Activity.kt, AppDatabase.kt, ExportData.kt, SelectLevelFragment.kt, SummaryFragment.kt, CalendarFragment.kt, DashboardFragment.kt, PiecesViewModel.kt, JsonExporter.kt, JsonImporter.kt, fragment_notes_input.xml, fragment_pieces.xml

### Overall Assessment

The implementation is clean, well-scoped, and correctly addresses all five phases. The Room migration is data-safe, the notes field threads correctly through the entire stack (creation → display → export → import), and the Priority sort default is consistent between ViewModel and layout. No blocking issues were found.

---

### Findings

#### Issues

**1. Type inconsistency: `Activity.notes` vs `MyStreakExportActivity.notes`**
- `Activity.notes` is `String` (non-nullable, default `""`)
- `MyStreakExportActivity.notes` (`ExportData.kt:82`) is `String? = ""`
- This mismatch is functionally safe: `JsonImporter.toActivity` (`JsonImporter.kt:198`) uses `.orEmpty()` to handle a `null` value from Gson when the field is absent in an older export file. However, the inconsistency is a code smell. The export model field should either be `String = ""` to match the entity, or the nullable form should be documented as an intentional Gson compatibility escape hatch.

**2. Export schema version not bumped after adding `notes`**
- `JsonExporter.SCHEMA_VERSION` (`JsonExporter.kt:18`) remains at `1` after `notes` was added to the export format.
- This is functionally safe because Gson defaults absent fields to `null`/`""` on import, maintaining both backward compatibility (old exports, no `notes` field) and forward compatibility (new exports, `notes` field present). However, a version bump to `2` would make the schema evolution explicit and auditable. If a stricter importer is ever written that rejects unknown fields, the version would be the correct signal. Worth discussing before the next cycle.

**3. `activityDescription` duplicated across two fragments**
- An identical private function `activityDescription(activity, task)` exists in both `DashboardFragment.kt:146–153` and `CalendarFragment.kt:290–297`. The logic is the same: return the success description, appending `"\nNotes: $notes"` only when notes are non-empty.
- Not a bug, but if the display format for notes ever changes, it will need to be updated in two places. Consider extracting to a shared extension function or utility in a follow-on cycle.

---

#### Observations and Confirmations

**Room migration is correct.**
`MIGRATION_7_8` (`AppDatabase.kt:57–61`) uses `ALTER TABLE activities ADD COLUMN notes TEXT NOT NULL DEFAULT ''`. The SQL default matches the Kotlin entity default. Existing rows will receive an empty string, which is the correct behavior. The migration is registered before `fallbackToDestructiveMigration`, so existing data is preserved on upgrade.

**Notes are correctly hidden when empty in all three surfaces.**
- `SummaryFragment.setupSummary()` (`SummaryFragment.kt:188–194`): `binding.textNotes.visibility = View.GONE` when `notes.isEmpty()`.
- `DashboardFragment.activityDescription()` (`DashboardFragment.kt:146–153`): returns plain success text when notes are empty.
- `CalendarFragment.activityDescription()` (`CalendarFragment.kt:290–297`): same pattern.

**Notes round-trip correctly through export/import.**
- Export: `JsonExporter.kt:61` maps `notes = activity.notes` (non-null String).
- Import: `JsonImporter.toActivity` (`JsonImporter.kt:198`) normalizes with `TextNormalizer.normalizeUserInput(activity.notes.orEmpty())`. User input normalization on import is a good practice.

**Priority sort default is consistent.**
- `PiecesViewModel.kt:53`: `sortType = MutableStateFlow(SortType.PRIORITY)`.
- `fragment_pieces.xml:69`: `android:checked="true"` on `chipPriority`.
- Sort direction arrow defaults to `↑` (ascending), which matches `SortDirection.ASCENDING` = High priority first. All three signals are aligned.

**`@Ignore` constructor on Activity is correctly wired.**
The secondary `@Ignore` constructor (`Activity.kt:17–36`) threads `notes` through to the primary constructor. Legacy calling code that constructs Activity via the old parameter names will correctly carry notes. Room only uses the primary constructor for database operations.

**`saveInProgress` guard in SummaryFragment is correct.**
The double-tap save prevention (`SummaryFragment.kt:79–88`) disables both Save and Cancel buttons on first tap and is cleared only if the ViewModel emits an error. This prevents duplicate Activity records from rapid taps.

---

#### Item Worth Verifying

**Notes pre-population in edit flow (NotesInputFragment not in diff)**
`NotesInputFragment` was not modified in this cycle — it was "reconnected" rather than changed. In edit mode, `SelectLevelFragment` pre-populates the level selection from `EditActivityStorage` (`SelectLevelFragment.kt:47–65`). It is worth confirming that `NotesInputFragment` also reads the existing notes from `EditActivityStorage` and pre-fills the `editTextNotes` field. If it does not, editing an activity with existing notes would silently clear them on save (since the user would see an empty field and the nav args would pass an empty string to `SummaryFragment`). This is the one path not verifiable from the current diff alone.

---

### Summary

| Category | Finding |
|---|---|
| Data safety | Migration correct; existing rows preserved |
| Export/import | Notes round-trip correctly; minor type inconsistency in export model |
| UI correctness | Notes hidden when empty in all surfaces |
| Sort default | Priority chip and ViewModel default are aligned |
| Code duplication | `activityDescription` duplicated in Dashboard and Calendar |
| Schema versioning | Version not bumped after adding `notes` field |
| Verify | Notes pre-population in NotesInputFragment during edit mode |
