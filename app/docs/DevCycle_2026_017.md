# Development Cycle 2026-017

**Status:** COMPLETE  
**Start Date:** 2026-06-30  
**Target Completion:** 2026-06-30  
**Focus:** Fix new-activity time defaulting to 12:00 AM instead of current time

## Overview

When a user taps a date on the Calendar tab and then logs a new activity, `EditActivityStorage.getPrePopulatedDate()` returns a timestamp at midnight (start of day). That midnight value becomes `currentTimestamp` in `SummaryFragment`, so the time displayed — and ultimately saved — is 12:00 AM instead of the actual current time. This cycle fixes the bug by merging the calendar-selected date with the current wall-clock time.

## Bug Description

### Current Behavior
- User taps a date on the Calendar tab, then adds an activity.
- The Summary screen shows the time as 12:00 AM.
- If the user does not tap Edit Time, the activity is saved with a 12:00 AM timestamp.

### Root Cause
`EditActivityStorage.getPrePopulatedDate()` stores only a date (midnight epoch for that day). In `SummaryFragment.onViewCreated()`, that midnight timestamp is used directly as `currentTimestamp` with no adjustment for the time-of-day component.

**Relevant code (`SummaryFragment.kt`, lines 59–65):**
```kotlin
currentTimestamp = if (editActivity != null) {
    editActivity.timestamp
} else {
    com.pseddev.mystreak.ui.progress.EditActivityStorage.getPrePopulatedDate()
        ?: System.currentTimeMillis()   // ← fallback is fine; pre-populated value is the problem
}
com.pseddev.mystreak.ui.progress.EditActivityStorage.clearPrePopulatedDate()
```

When `getPrePopulatedDate()` returns a midnight timestamp, the time portion is 00:00:00.

### Expected Behavior
- When the user arrives from the Calendar tab, `currentTimestamp` uses the calendar-selected **date** but the **current wall-clock time** for the time portion.
- When the user arrives from the normal add-activity flow (no pre-populated date), `currentTimestamp` is `System.currentTimeMillis()` — no change.

## Current Work Items

### Phase 1: Combine Pre-Populated Date with Current Time
**Status:** ✅ COMPLETED  
**Date Added:** 2026-06-30  
**Priority:** High  
**Description:** After reading `EditActivityStorage.getPrePopulatedDate()`, extract the year/month/day from the returned timestamp and combine it with the current hour/minute/second from `System.currentTimeMillis()`. This preserves the user's calendar date selection while defaulting the time to now.

**File to change:** `app/src/main/java/com/pseddev/playstreak/ui/addactivity/SummaryFragment.kt`

**Proposed fix:**
```kotlin
currentTimestamp = if (editActivity != null) {
    editActivity.timestamp
} else {
    val prePopulated = com.pseddev.mystreak.ui.progress.EditActivityStorage.getPrePopulatedDate()
    if (prePopulated != null) {
        // Use the calendar date but current time-of-day
        val dateCal = Calendar.getInstance().apply { timeInMillis = prePopulated }
        val nowCal = Calendar.getInstance()
        dateCal.set(Calendar.HOUR_OF_DAY, nowCal.get(Calendar.HOUR_OF_DAY))
        dateCal.set(Calendar.MINUTE,      nowCal.get(Calendar.MINUTE))
        dateCal.set(Calendar.SECOND,      nowCal.get(Calendar.SECOND))
        dateCal.set(Calendar.MILLISECOND, nowCal.get(Calendar.MILLISECOND))
        dateCal.timeInMillis
    } else {
        System.currentTimeMillis()
    }
}
com.pseddev.mystreak.ui.progress.EditActivityStorage.clearPrePopulatedDate()
```

**Acceptance Criteria:**
- [x] When the user arrives from the Calendar tab, the Summary screen shows the calendar-selected date with the current time (not 12:00 AM).
- [x] When the user arrives from the normal add-activity flow, the Summary screen shows the current date and time.
- [x] The combined timestamp does not exceed `System.currentTimeMillis()` (i.e., no future-time issue for same-day adds).
- [x] Editing an existing activity is unaffected (no regression).

### Phase 2: Build and Verification
**Status:** ✅ COMPLETED  
**Date Added:** 2026-06-30  
**Priority:** Medium  
**Description:** Build the app and manually verify the fix in both the Calendar-initiated and normal add-activity flows.

**Acceptance Criteria:**
- [x] `gradlew.bat assembleDebug` succeeds with no new errors or warnings.
- [x] Calendar tab → tap a past date → add activity: Summary screen shows the tapped date with the current time.
- [x] Normal add flow (not from Calendar): Summary screen shows the current date and time.
- [x] Editing an existing activity: no regression — date/time shown is the activity's saved timestamp.
- [x] Future date/time selection is still blocked by the existing toast.

## Proposed Implementation Sequence

1. In `SummaryFragment.onViewCreated()`, replace the direct use of `getPrePopulatedDate()` with the combined date+current-time logic described in Phase 1.
2. Build and verify manually per Phase 2 acceptance criteria.

## Cycle Notes

- The fix is intentionally minimal: one `else` branch in `SummaryFragment.onViewCreated()`. No layout, ViewModel, or repository changes are needed.
- The existing "future timestamp" guard (`currentTimestamp > System.currentTimeMillis()`) in the save handler will naturally catch any edge case where the combined timestamp lands in the future (e.g., user taps today's date and the second ticks over), so no additional guard is needed.
- DC 016 introduced the pre-populated date seeding and the Edit Time button in add mode. This cycle is a follow-on fix to that work.

## Cycle Completion Summary

**Completion Date:** 2026-06-30  
**Git Commit Status:** Pending commit

**Accomplishments:**
- Fixed bug where adding a new activity from the Calendar tab defaulted the time to 12:00 AM instead of the current time.

**Metrics:**
- Files modified: 1 (`SummaryFragment.kt`)
- Bugs fixed: 1

**Notes:**
- Root cause was that `EditActivityStorage.getPrePopulatedDate()` stores a midnight timestamp; the fix merges the calendar date with the current wall-clock time at the point of `currentTimestamp` initialisation.
