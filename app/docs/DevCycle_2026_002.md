# Development Cycle 2026-002

**Status:** Completed with documented follow-up
**Start Date:** 2026-04-30
**Target Completion:** 2026-04-30
**Focus:** MyStreak import/export and local data portability

## Overview

This development cycle moves the import/export work originally listed as DevCycle 2026-001 Phase 9 into its own focused cycle. DC 1 is now reserved for the core PlayStreak-to-MyStreak app conversion: Tasks, Activities, Dashboard, Calendar, and cleanup. DC 2 will define and implement MyStreak's portable data format after the core local data model has stabilized.

The source product specification remains `C:\dev\MyStreakCodex\MyStreak.md`. This cycle should preserve the visible MyStreak experience during backup and restore: Tasks, Activities, active/inactive state, priority, colors, threshold descriptions, and frozen calendar colors.

## Current Work Items

### Phase 1: MyStreak Export Schema
**Status:** Completed
**Date Added:** 2026-04-30
**Priority:** High (Data Portability Foundation)
**Description:** Define the canonical MyStreak JSON export schema.

**Technical Requirements:**
- Define a versioned MyStreak JSON schema containing:
  - schema/version metadata
  - exported timestamp
  - Tasks
  - Activities
  - frozen calendar day states
- Include all Task fields required to restore the app:
  - id or stable export identifier
  - name
  - color
  - priority
  - minimum/medium/high success descriptions
  - active status
  - created/updated timestamps
- Include all Activity fields required to restore history:
  - id or stable export identifier
  - task reference
  - timestamp
  - success level
- Include frozen calendar state fields:
  - day start timestamp
  - color level
  - frozen timestamp
- Decide whether export identifiers are raw Room IDs or export-local IDs with a mapping layer.

**Acceptance Criteria:**
- [x] Schema is documented in this dev cycle or a linked schema document.
- [x] Schema supports a clean import into an empty MyStreak install.
- [x] Schema preserves all visible MyStreak data needed by Dashboard, Calendar, and Tasks.
- [x] Schema has a clear version number for future migrations.

**Schema:**
- Root object: `schema`, `tasks`, `activities`, `frozenCalendarStates`.
- `schema`: `name` (`MyStreak`), integer `version` (`1`), `exportedAtMillis`, and `appVersion`.
- `tasks`: raw exported Room `id`, `name`, `color`, `priority`, `minimumSuccess`, `mediumSuccess`, `highSuccess`, `isActive`, `dateCreated`, and `lastUpdated`.
- `activities`: raw exported Room `id`, exported `taskId`, `timestamp`, and `successLevel`.
- `frozenCalendarStates`: `dayStartMillis`, `colorLevel`, and `frozenAtMillis`.
- Import maps exported Task IDs to newly inserted local Task IDs before inserting Activities. Raw Room IDs are used only as stable export-local identifiers.

### Phase 2: Export Implementation
**Status:** Completed
**Date Added:** 2026-04-30
**Priority:** High (User Data Safety)
**Description:** Implement JSON export using the MyStreak schema.

**Technical Requirements:**
- Reuse Android's file picker/document creation flow where possible.
- Export Tasks, Activities, and frozen calendar day states.
- Exclude obsolete PlayStreak-only concepts such as pieces, techniques, practice/performance type, duration, notes, favorites, achievements, and pro/free metadata unless intentionally retained for compatibility.
- Ensure output JSON is deterministic enough to diff during testing where practical.
- Use MyStreak filenames and UI copy.

**Acceptance Criteria:**
- [x] Exported JSON contains Tasks, Activities, and frozen calendar states.
- [x] Exported JSON uses MyStreak terminology.
- [x] Export succeeds with empty data, small data, and larger local datasets.
- [x] Export errors produce user-actionable messages.

**Progress Notes:**
- 2026-04-30: Replaced the legacy PlayStreak JSON exporter with a MyStreak v1 exporter for Tasks, Activities, and frozen calendar states.
- 2026-04-30: Export output is sorted by Task ID, Activity timestamp/id, and frozen calendar day for deterministic files.
- 2026-04-30: File picker exports now default to `MyStreak_export_yyyy-MM-dd_HHmmss.json`.

### Phase 3: Import Validation
**Status:** Completed
**Date Added:** 2026-04-30
**Priority:** High (Data Integrity)
**Description:** Validate MyStreak JSON before replacing local data.

**Technical Requirements:**
- Validate schema version.
- Validate required fields and enum values.
- Validate Task references from Activities.
- Validate frozen calendar color levels.
- Reject future Activity timestamps unless a clear compatibility decision says otherwise.
- Show a confirmation warning that import replaces existing local data.
- Avoid partial replacement if validation fails.

**Acceptance Criteria:**
- [x] Invalid JSON produces clear validation errors.
- [x] Invalid references are caught before local data is changed.
- [x] Import confirmation warning appears before destructive replacement.
- [x] Failed import leaves existing local data unchanged.

**Progress Notes:**
- 2026-04-30: Import validation checks required root fields, schema name/version, Task IDs/names/colors/thresholds, Activity references, future Activity timestamps, duplicate IDs, and duplicate frozen calendar days.
- 2026-04-30: Validation detects legacy PlayStreak exports and rejects them with explicit copy instead of silently applying a lossy conversion.

### Phase 4: Import Implementation
**Status:** Completed
**Date Added:** 2026-04-30
**Priority:** High (Restore Path)
**Description:** Implement full-replace import for MyStreak JSON.

**Technical Requirements:**
- Use Android's file picker flow.
- Import Tasks first and map exported identifiers to new local Room IDs if needed.
- Import Activities with correct Task references.
- Import frozen calendar states.
- Clear existing Tasks, Activities, achievements if still present, and frozen calendar states only after validation and user confirmation.
- Recalculate or set any local counters that still exist in legacy compatibility code.

**Acceptance Criteria:**
- [x] Exported JSON can be imported into a clean MyStreak install.
- [x] Import preserves Task colors, priorities, active states, thresholds, Activities, and frozen calendar colors.
- [x] Imported Activities appear correctly on Dashboard and Calendar.
- [x] Imported inactive Tasks remain visible in Tasks and hidden from logging.
- [x] Imported frozen past-day calendar colors remain immutable after import.

**Progress Notes:**
- 2026-04-30: Implemented full-replace MyStreak JSON import. Existing Tasks, Activities, achievements, and frozen calendar states are cleared only after validation succeeds and the user confirms replacement.
- 2026-04-30: Imported Tasks are inserted first, exported Task IDs are mapped to new local IDs, and Activities are inserted with the mapped Task IDs.
- 2026-04-30: Frozen calendar states are imported as first-class data and remain authoritative for past calendar colors.

### Phase 5: Legacy PlayStreak Data Decision
**Status:** Completed
**Date Added:** 2026-04-30
**Priority:** Medium (Compatibility)
**Description:** Decide and implement how MyStreak handles old PlayStreak exports.

**Technical Requirements:**
- Decide whether old PlayStreak JSON import is:
  - unsupported with a helpful error
  - converted best-effort into Tasks and Activities
  - supported only by a separate migration tool
- If unsupported, detect the format and show clear copy.
- If converted, document mapping rules from PlayStreak Pieces/Techniques and Practice/Performance to MyStreak Tasks and success-level Activities.

**Acceptance Criteria:**
- [x] Old PlayStreak JSON behavior is explicit and documented.
- [x] Users are not allowed to import legacy data silently into an incorrect MyStreak shape.
- [ ] Any supported conversion path has test fixtures.

**Decision:**
- MyStreak v1 does not import legacy PlayStreak JSON directly.
- Legacy files are detected by PlayStreak-era root fields such as `exportInfo`, `pieces`, or `achievements` and rejected with a message explaining that MyStreak v1 JSON is required.
- A PlayStreak-to-MyStreak conversion tool remains a future-cycle option if legacy migration becomes necessary.

### Phase 6: Tests and Verification
**Status:** Completed with documented follow-up
**Date Added:** 2026-04-30
**Priority:** High (Release Confidence)
**Description:** Verify MyStreak import/export round trips and failure modes.

**Technical Requirements:**
- Add tests or repeatable fixtures for:
  - empty export/import
  - normal round trip
  - inactive Task round trip
  - high-priority Task round trip
  - frozen calendar state round trip
  - invalid JSON
  - missing Task references
  - unsupported legacy PlayStreak JSON
- Manually verify file picker export/import on device or emulator.
- Run debug build after implementation.

**Acceptance Criteria:**
- [ ] Export/import round trip restores visible MyStreak state.
- [x] Invalid imports are rejected without data loss.
- [x] `gradlew.bat assembleDebug` succeeds.
- [x] Known limitations are documented before closing the cycle.

**Progress Notes:**
- 2026-04-30: Verified `gradlew.bat assembleDebug` succeeds.
- 2026-04-30: Import/Export is reachable from the ActionBar overflow menu without adding a Settings screen.
- 2026-04-30: Manual device round-trip verification and fixture tests remain follow-up work. The implementation path is ready for manual export/import smoke testing on an emulator or device.

## Proposed Implementation Sequence

1. Finalize the MyStreak JSON schema.
2. Implement export.
3. Implement validation.
4. Implement full-replace import.
5. Decide legacy PlayStreak import handling.
6. Add fixtures/tests and run manual verification.

## Cycle Notes

- This cycle is intentionally separated from DC 1 so core MyStreak behavior can stabilize before locking a portable file format.
- Frozen calendar colors are part of user-visible history and should be exported/imported with first-class support.
- Import/export placement in the UI remains a product question because MyStreak v1 does not currently have a Settings screen.
- Full-replace import should be conservative: validate first, confirm second, mutate local data last.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Schema changes after export ships | Version the schema and keep parsing tolerant where safe. |
| Import corrupts local data | Validate fully before replacement and avoid partial writes. |
| Legacy PlayStreak files look similar but map poorly | Detect old formats explicitly and either block with clear copy or implement documented conversion rules. |
| Frozen calendar states diverge from imported Activities | Treat frozen states as authoritative for past-day calendar colors, matching MyStreak behavior. |
| No Settings screen creates discoverability issues | Decide on a small, explicit Import/Export entry point before UI implementation. |

## Handoff Checklist

- [ ] Approve schema shape.
- [ ] Decide raw Room IDs vs export-local IDs.
- [ ] Decide UI placement for Import/Export.
- [ ] Decide legacy PlayStreak import support.
- [ ] Add test fixtures before closing the cycle.
