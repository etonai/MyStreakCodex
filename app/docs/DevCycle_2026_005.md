# Development Cycle 2026-005

**Status:** Open
**Start Date:** TBD
**Target Completion:** TBD
**Focus:** Expand Task color palette from 6 to 12 colors

## Overview

Tasks currently offer 6 color choices. This cycle doubles the palette to 12 by adding 6 new colors that fill the visible gaps in the existing hue range. The 6 existing colors are preserved without change so that Tasks created before this cycle continue to display correctly.

## Current Colors (unchanged)

| Name   | Hex       |
|--------|-----------|
| Blue   | `#66B2FF` |
| Green  | `#46C07A` |
| Yellow | `#F4C542` |
| Red    | `#E86B6B` |
| Purple | `#9B7EDE` |
| Teal   | `#3FB8B8` |

## New Colors to Add

| Name   | Hex       | Rationale                              |
|--------|-----------|----------------------------------------|
| Orange | `#F4884A` | Fills gap between Red and Yellow       |
| Pink   | `#E05CB0` | Fills gap between Red and Purple       |
| Lime   | `#8BC34A` | Fills gap between Green and Yellow     |
| Indigo | `#5C6BC0` | Fills gap between Blue and Purple      |
| Amber  | `#FFA726` | Warm orange-yellow, distinct from Yellow |
| Cyan   | `#26C6DA` | Fills gap between Blue and Teal        |

## Current Work Items

### Phase 1: Add New Colors to Add Task Flow
**Status:** 🔍 In Verification
**Date Added:** 2026-05-13
**Priority:** High
**Description:** Add the 6 new color options to the Add Task screen.

**Technical Requirements:**
- Add 6 new `RadioButton` entries to `fragment_add_piece.xml` inside the existing `RadioGroup`.
- Add the corresponding hex mappings in `AddPieceFragment.kt` color selection logic.
- Existing 6 colors and their radio button IDs must remain unchanged.

**Acceptance Criteria:**
- [ ] All 12 colors appear as selectable options when adding a new Task.
- [ ] Selecting each new color and saving correctly stores the hex value.
- [ ] The default selection (Blue) is unchanged.
- [ ] Existing Tasks created with the original 6 colors display correctly.
- [ ] `gradlew.bat assembleDebug` succeeds.

**Files:**
- `app/src/main/res/layout/fragment_add_piece.xml`
- `app/src/main/java/com/pseddev/playstreak/ui/pieces/AddPieceFragment.kt`

### Phase 2: Add New Colors to Edit Task Flow
**Status:** 🔍 In Verification
**Date Added:** 2026-05-13
**Priority:** High
**Description:** Add the 6 new color options to the Edit Task dialog.

**Technical Requirements:**
- Add 6 new `RadioButton` entries to `dialog_edit_piece.xml` inside the existing `RadioGroup`.
- Add the hex-to-radio-ID mappings in `EditPieceDialogFragment.kt` (the block that pre-selects the current color when the dialog opens).
- Add the radio-ID-to-hex mappings in `EditPieceDialogFragment.kt` (the block that reads the selected color on save).
- Existing colors must pre-select correctly for Tasks that already use them.

**Acceptance Criteria:**
- [ ] All 12 colors appear as selectable options when editing an existing Task.
- [ ] A Task currently using one of the original 6 colors pre-selects correctly in the dialog.
- [ ] Changing to a new color and saving correctly stores the hex value.
- [ ] `gradlew.bat assembleDebug` succeeds.

**Files:**
- `app/src/main/res/layout/dialog_edit_piece.xml`
- `app/src/main/java/com/pseddev/playstreak/ui/progress/EditPieceDialogFragment.kt`

### Phase 3: UI Layout Review
**Status:** Open
**Date Added:** 2026-05-13
**Priority:** Medium
**Description:** Review whether a vertical list of 12 radio buttons is comfortable on phone-sized screens, and adjust the layout if needed.

**Technical Requirements:**
- Verify the color picker section is fully visible and scrollable on the Add Task screen and the Edit Task dialog.
- If the list is too tall, consider splitting into two columns or another compact layout.
- The selected color must remain visually clear regardless of layout choice.

**Acceptance Criteria:**
- [ ] Color picker is usable on a phone-sized screen without excessive scrolling.
- [ ] No color options are cut off or hidden by the keyboard or dialog bounds.
- [ ] The layout looks reasonable in both light and dark mode.

**Files:**
- `app/src/main/res/layout/fragment_add_piece.xml`
- `app/src/main/res/layout/dialog_edit_piece.xml`

## Proposed Implementation Sequence

1. Add the 6 new `RadioButton` entries to `fragment_add_piece.xml`.
2. Add the corresponding color mappings to `AddPieceFragment.kt`.
3. Add the 6 new `RadioButton` entries to `dialog_edit_piece.xml`.
4. Add the bidirectional color mappings to `EditPieceDialogFragment.kt`.
5. Build and verify both flows on device/emulator.
6. Review layout on a phone screen and adjust if needed.

## Risks and Mitigations

| Risk | Mitigation |
|---|---|
| 12 radio buttons too tall in Edit dialog | Dialog is inside a ScrollView; add scrolling if needed or switch to two-column layout. |
| New hex values accidentally reused by existing color names | Hex values above are all visually distinct from the existing 6; verify on device. |
| Tasks with unknown colors fall back incorrectly | The `else -> "#66B2FF"` fallback in both fragments is already present; no change needed. |

## Cycle Notes

- No database migration is required. Colors are stored as plain hex strings; the schema does not change.
- No export/import changes are required. The export format already accepts any valid hex string.
- The 6 new color names (Orange, Pink, Lime, Indigo, Amber, Cyan) should match the `android:text` labels on the new radio buttons.
