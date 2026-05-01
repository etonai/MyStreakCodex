# Bug Tracking

This document tracks known bugs, their status, and resolutions for the Piano Track Opus app.

## Status Legend
- 🐛 **Open** - Bug is confirmed and needs fixing
- 🔄 **In Progress** - Bug is being worked on
- 🔍 **Verifying** - Fix implemented, awaiting user verification
- ✅ **Fixed** - Bug has been resolved and verified
- ❌ **Closed** - Bug closed without fix (e.g., not reproducible, won't fix)

## Known Bugs

### Bug #1: 🔄 Calendar Month Swiping Should Be Disabled
**Status:** In Progress
**Date Reported:** 2025-07-20
**Severity:** Medium

**Description:**
Users can swipe left/right on the calendar to change months, but this should be disabled. Month navigation should only be possible through the Previous/Next buttons in the header.

**Steps to Reproduce:**
1. Open the Calendar view
2. Swipe left or right on the calendar grid
3. Calendar changes months

**Expected Behavior:**
Swiping should not change months. Only the Previous/Next buttons should control month navigation.

**Actual Behavior:**
Calendar responds to swipe gestures and changes months.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Disabled calendar month swiping while preserving day selection functionality:
- **Touch Event Handling**: Added custom `OnTouchListener` to intercept horizontal swipe gestures
- **Swipe Detection**: Tracks horizontal movement from initial touch position
- **Threshold**: Blocks horizontal swipes greater than 30 pixels to prevent accidental month changes
- **Day Selection Preserved**: Allows small movements and vertical touches for normal day selection
- **Button Navigation**: Previous/Next month buttons remain fully functional

**Technical Implementation:**
- **Method**: `disableCalendarSwiping()` in CalendarFragment
- **Logic**: Intercepts `ACTION_MOVE` events and consumes horizontal swipes beyond threshold
- **Touch Flow**: `ACTION_DOWN` stores initial X position, `ACTION_MOVE` checks horizontal distance
- **Selective Blocking**: Only blocks significant horizontal movements (>30px) while allowing day taps

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

**Previous Attempts:**
- Attempted fix: `android:nestedScrollingEnabled="false"` in XML layout
- Attempted fix: Touch event interception - **REVERTED** (blocked day selection)
- Current status: Day selection restored, but month swiping may still work
- **Update 2025-07-20**: Confirmed that when a day is selected in the calendar, users can still swipe left/right to change months. This swiping should be completely disabled - only Previous/Next buttons should control month navigation.
- **Implementation Attempt**: Added custom touch handler with 30px threshold - **PARTIAL SUCCESS** (reduces but doesn't completely eliminate month swiping. User reports occasional successful swipes still occur inconsistently)
- **Next Steps**: User will test further to identify repeatable reproduction case for the remaining swipe behavior

---

### Bug #2: ❌ Calendar Has Too Much Spacing Between Numbers
**Status:** Closed
**Date Reported:** 2025-07-20
**Date Closed:** 2025-07-20
**Severity:** Low

**Description:**
The calendar view has excessive spacing between the day numbers, both vertically (between rows) and horizontally (between columns). This makes the calendar appear sparse and wastes screen space.

**Steps to Reproduce:**
1. Open the Calendar view
2. Observe the spacing between day numbers

**Expected Behavior:**
Calendar should have compact spacing between day numbers for better visual density and screen space utilization.

**Actual Behavior:**
Calendar has large gaps between day numbers making it appear spread out.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Closure Reason:**
Closed without implementing - current calendar spacing is acceptable for readability and touch targets. Previous attempts to reduce spacing caused readability issues. The current layout provides good balance between visual density and usability.

---

### Bug #3: ✅ Pieces Sort Line Too Long - Needs Text Optimization
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The pieces sort line text is too long and should be shortened. Specifically "activities" should be changed to "activity" and "Last Date" should be changed to "Date" to reduce line length.

**Steps to Reproduce:**
1. Open the Pieces view
2. Observe the sort line text length

**Expected Behavior:**
Sort line should use shorter text: "activity" instead of "activities" and "Date" instead of "Last Date".

**Actual Behavior:**
Sort line uses longer text that may cause wrapping or display issues.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Applied text changes to reduce line length:
- "activities" → "activity"
- "Last Date" → "Date"

**Files Modified:**
- `app/src/main/res/layout/fragment_pieces.xml`

---

### Bug #4: ✅ Missing Buffer Space at Bottom of Tabs
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
The Home, Calendar, and Pieces tabs need buffer space (empty whitespace) at the bottom to allow users to scroll past the last content item. This prevents floating action buttons or other UI elements from covering text or content near the bottom of the screen.

**Steps to Reproduce:**
1. Open any of the Home, Calendar, or Pieces tabs
2. Scroll to the bottom of the content
3. Observe that there's no extra space to scroll past the last item

**Expected Behavior:**
User should be able to scroll down into empty whitespace below the last content item to ensure all content is visible and not covered by UI elements.

**Actual Behavior:**
Content ends abruptly at the last item with no buffer space, potentially causing UI elements to cover important content.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Added buffer space to all three main tabs:
- **Home tab (Dashboard)**: Added 100dp buffer View at bottom of ScrollView
- **Calendar tab**: Added 100dp buffer View at bottom of ScrollView
- **Pieces tab**: Added 108dp bottom padding to RecyclerView

This allows users to scroll past the last content item to ensure floating action buttons don't cover important content.

**Files Modified:**
- `app/src/main/res/layout/fragment_dashboard.xml`
- `app/src/main/res/layout/fragment_calendar.xml`
- `app/src/main/res/layout/fragment_pieces.xml`

---

### Bug #6: ✅ Activity Sort Should Default to Descending
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
When sorting activities, the default sort order should be descending (newest first) rather than ascending. This provides a better user experience as users typically want to see their most recent activities at the top.

**Steps to Reproduce:**
1. Open any view that displays activities with sorting
2. Observe the default sort order

**Expected Behavior:**
Activities should be sorted in descending order by default (newest/highest values first).

**Actual Behavior:**
Activities are sorted in ascending order by default (oldest/lowest values first).

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Implemented smart defaults for sorting based on sort type:
- **PiecesViewModel**: Date and Activity Count sorting now default to descending (newest/highest first)
- **AbandonedViewModel**: Changed default to descending (most recently abandoned first)
- **Alphabetical sorting**: Remains ascending (A-Z) as expected

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/AbandonedViewModel.kt`

---

### Bug #7: ✅ Calendar Day Circles Too Small
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The circles around the day numbers in the calendar view are too small, making them harder to see and interact with. The circles should be larger for better visibility and touch targets.

**Steps to Reproduce:**
1. Open the Calendar view
2. Observe the size of the circles around the day numbers

**Expected Behavior:**
Calendar day circles should be larger for better visibility and easier touch interaction.

**Actual Behavior:**
Calendar day circles are too small, making them harder to see and tap accurately.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Increased calendar day circle size for better visibility and touch interaction:
- **Size Change**: Increased from 32dp x 32dp to 40dp x 40dp (25% larger)
- **Container Adjustment**: Increased calendar day container height from 48dp to 52dp to accommodate larger circles
- **Touch Target**: Larger circles provide better touch targets for easier interaction
- **Visual Impact**: More prominent day numbers for improved readability
- **Layout**: Maintains proper spacing and prevents overlap between adjacent days

**Files Modified:**
- `app/src/main/res/layout/calendar_day_layout.xml`

---

### Bug #8: ✅ Home Page Activities Missing Level Information
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
The activities listed on the Home page do not include the activity level information, unlike the Calendar page which shows level details (e.g., "Practice (3)", "Performance (2)"). This inconsistency makes the Home page less informative.

**Steps to Reproduce:**
1. Open the Home page and view the activity list
2. Open the Calendar page and select a date with activities
3. Compare the activity information displayed

**Expected Behavior:**
Home page activities should include level information in the same format as the Calendar page (e.g., "Practice (3)", "Performance (2)").

**Actual Behavior:**
Home page activities only show activity type without level information.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Added level information to both today and yesterday activity lists in DashboardFragment. Activity format now matches Calendar page: "• time - piece - type (level) minutes".

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardFragment.kt`

---

### Bug #9: ✅ Change Home Tab Label to Dashboard
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The first tab is currently labeled "Home" but should be changed to "Dashboard" to better reflect its purpose as a dashboard view with statistics and recent activity overview.

**Steps to Reproduce:**
1. Open the app
2. Observe the bottom navigation tab labels

**Expected Behavior:**
The first tab should be labeled "Dashboard" instead of "Home".

**Actual Behavior:**
The first tab is labeled "Home".

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Changed tab label from "Home" to "Dashboard" in ViewProgressFragment to better reflect the tab's purpose as a dashboard with statistics and activity overview.

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/ViewProgressFragment.kt`

---

### Bug #10: ✅ Add Suggestions Section to Dashboard Tab
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
The Dashboard tab should include a suggestions section to help users decide what to practice next. It should display the oldest favorite suggestion and the oldest non-favorite suggestion. When there are ties (multiple pieces with the same "oldest" date), all tied suggestions should be listed.

**Steps to Reproduce:**
1. Open the Dashboard tab
2. Look for practice suggestions

**Expected Behavior:**
Dashboard should display:
- A "Suggestions" section
- The oldest favorite piece(s) that need practice
- The oldest non-favorite piece(s) that need practice
- If multiple pieces tie for "oldest", show all tied pieces

**Actual Behavior:**
Dashboard has no suggestions section to guide practice decisions.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Added suggestions section to Dashboard tab that displays practice recommendations. The section shows the oldest favorite and non-favorite pieces that need practice, with support for multiple tied suggestions when pieces have the same "oldest" practice date.

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardViewModel.kt`

---

### Bug #11: ✅ Add Activity Dialog Navigation Issue from Dashboard/Calendar
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
When adding an activity from the Dashboard or Calendar screens, the Save and Cancel buttons in the add activity dialog do not return the user to the originating screen. The Save button does successfully save the activity, but the navigation flow is broken. The add activity button from Settings works correctly.

**Steps to Reproduce:**
1. Open Dashboard or Calendar tab
2. Tap the add activity button/option
3. Fill out the activity dialog
4. Tap Save or Cancel
5. Observe that you are not returned to the Dashboard/Calendar screen

**Expected Behavior:**
After tapping Save or Cancel in the add activity dialog, the user should return to the screen where they originally pressed the add activity button (Dashboard or Calendar).

**Actual Behavior:**
Save and Cancel buttons don't navigate back to the originating screen, leaving the user in an unexpected location.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Fixed add activity navigation to correctly return to the originating screen:
- **Root Cause**: SummaryFragment was hard-coded to always navigate back to mainFragment (Settings)
- **Solution**: Changed navigation to use `popBackStack(addActivityFragment, true)` which returns to the screen that launched the add activity flow
- **Behavior**: Now works correctly for all entry points:
  - From **Dashboard/Calendar**: Returns to progressFragment (Dashboard/Calendar tabs)
  - From **Settings**: Returns to mainFragment (Settings screen)
- **Fixed Actions**: Both Save and Cancel buttons now navigate back to the correct originating screen

**Technical Details:**
- **Before**: `popBackStack(mainFragment, false)` - always went to Settings
- **After**: `popBackStack(addActivityFragment, true)` - pops the entire add activity flow and returns to launching screen
- **Navigation Flow**: originating screen → addActivityFragment → ... → summaryFragment → back to originating screen

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/SummaryFragment.kt`

---

### Bug #12: ✅ Missing Delete Activity Feature in Timeline Tab
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
The Timeline tab should allow users to delete activities directly from the timeline view. This would provide a convenient way to remove incorrect or unwanted activity entries without having to navigate to other screens.

**Steps to Reproduce:**
1. Open the Timeline tab
2. View activities in the timeline
3. Look for delete/remove options for individual activities

**Expected Behavior:**
Users should be able to delete activities from the timeline view (e.g., through long press, swipe gesture, or context menu) which would remove the activity from the database.

**Actual Behavior:**
Timeline tab does not provide any way to delete activities from the timeline view.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Added delete functionality to Timeline tab with the following features:
- **Delete Button**: Added delete icon button to each timeline activity item in the top-right header area
- **Confirmation Dialog**: Shows confirmation dialog with activity details (piece name and date/time) before deletion
- **Database Integration**: Implemented complete delete flow from UI → ViewModel → Repository → DAO → Database
- **Real-time Updates**: Timeline automatically updates after deletion through LiveData observers
- **User Experience**: Clean delete icon with proper touch targets and Material Design styling

**Technical Implementation:**
- **Database Layer**: Added `@Delete suspend fun delete(activity: Activity)` to ActivityDao
- **Repository Layer**: Added `suspend fun deleteActivity(activity: Activity)` to PianoRepository
- **ViewModel Layer**: Added `deleteActivity(activityWithPiece: ActivityWithPiece)` to TimelineViewModel
- **UI Layer**: Added delete button to timeline item layout with click handler in TimelineAdapter
- **Confirmation Flow**: AlertDialog shows piece name and formatted date/time before confirming deletion

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/data/daos/ActivityDao.kt`
- `app/src/main/java/com/pseddev/practicetracker/data/repository/PianoRepository.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineAdapter.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineFragment.kt`
- `app/src/main/res/layout/item_timeline_activity.xml`
- `app/src/main/res/drawable/ic_delete.xml` (new)

---

### Bug #13: ✅ Add Last Export Tracking to Import/Export Data Page
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The Import/Export Data page should track and display when the last export was performed. This information should be shown at the bottom of the page, under the existing "Last sync" information, to help users keep track of their export history.

**Steps to Reproduce:**
1. Open Settings
2. Navigate to Import/Export Data page
3. Look for last export information

**Expected Behavior:**
The Import/Export Data page should display "Last export: [date/time]" information below the existing "Last sync" section to track when exports were last performed.

**Actual Behavior:**
The page only shows "Last sync" information but does not track or display when the last export was done.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Added last export tracking functionality:
- **ViewModel**: Added SharedPreferences to track last export timestamp and `getLastExportTime()` method
- **Export Process**: Updates timestamp when CSV export completes successfully
- **UI**: Added "Last export" TextView below "Last sync" information
- **Format**: Matches existing sync time format (MMM dd, yyyy HH:mm) or shows "Never"
- **Persistence**: Export time persists across app restarts using SharedPreferences

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/importexport/ImportExportViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/importexport/ImportExportFragment.kt`
- `app/src/main/res/layout/fragment_import_export.xml`

---

### Bug #14: ✅ Duplicate Titles on Settings, Manage Favorites, and Import/Export Data Pages
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The Settings, Manage Favorites, and Import/Export Data pages display duplicate titles - one in the action bar/toolbar with a back arrow, and another title shown on the page content itself. This creates visual redundancy since these pages are now navigated to from other screens rather than being independent pages.

**Steps to Reproduce:**
1. Navigate to Settings page
2. Observe both the toolbar title and page content title
3. Navigate to Manage Favorites page
4. Observe both the toolbar title and page content title
5. Navigate to Import/Export Data page
6. Observe both the toolbar title and page content title

**Expected Behavior:**
Each page should show the title only once - either in the toolbar/action bar OR on the page content, but not both.

**Actual Behavior:**
Pages show duplicate titles: one in the toolbar with back arrow navigation and another as a heading on the page content.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Resolution:**
Removed duplicate title TextViews from all three pages and adjusted layout constraints and margins:
- **Settings page**: Removed "Settings" title TextView and updated button positioning
- **Manage Favorites page**: Removed "Manage Favorites" header TextView
- **Import/Export Data page**: Removed "Import/Export Data" title TextView and updated card positioning

This eliminates visual redundancy while maintaining clean navigation with toolbar titles and back arrows.

**Files Modified:**
- `app/src/main/res/layout/fragment_main.xml`
- `app/src/main/res/layout/fragment_favorites.xml`
- `app/src/main/res/layout/fragment_import_export.xml`

---

### Bug #15: ❌ Calendar Day Selection Circle Too Small
**Status:** Closed
**Date Reported:** 2025-07-20
**Date Closed:** 2025-07-20
**Severity:** Low

**Description:**
When selecting a day in the calendar, a small purple circle appears around the selected day. The circle is too small and should be increased by 3x for better visibility and user feedback.

**Steps to Reproduce:**
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the purple selection circle around the day

**Expected Behavior:**
The purple selection circle should be 3x larger than its current size for better visibility and clear indication of the selected day.

**Actual Behavior:**
The purple selection circle is too small and barely visible, making it difficult to see which day is currently selected.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Closure Reason:**
Closed as duplicate/superseded by Bug #18. The selection ring visibility has been addressed through the color change to light bright purple in Bug #18, which provides better visibility than just increasing the stroke width. The current 9dp stroke width (implemented in previous fixes) combined with the new light bright purple color provides optimal visibility.

---

### Bug #16: ✅ Calendar Activity Section Missing Date in Title
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
On the Calendar page, when displaying activities for a selected day, the section title shows information like "1 activities (Practice)" or "No activities on this date" but does not include the actual date. The title should include the selected date for better user context and clarity.

**Steps to Reproduce:**
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the activity section title below the calendar

**Expected Behavior:**
The title should include the selected date and use proper grammar, for example:
- "January 15: 1 activity (Practice)"
- "January 15: 3 activities (Mixed)"
- "January 15: No activities on this date"
- Or similar format that clearly shows the selected date with correct singular/plural grammar

**Actual Behavior:**
The title only shows activity information without the date:
- "1 activities (Practice)"
- "No activities on this date"

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Enhanced the calendar activity section title with date information and proper grammar:
- **Date Format**: Smart formatting - "Today", "Yesterday", or "Month Day" (e.g., "January 15")
- **Grammar Fix**: Proper singular/plural - "1 activity" vs "2 activities"
- **Title Examples**:
  - "Today: 1 activity (Practice)"
  - "Yesterday: 3 activities (Mixed)"
  - "January 15: No activities on this date"
- **Location**: CalendarFragment.kt `updateSelectedDateView()` function
- **Logic**: Added date formatting and conditional grammar based on activity count

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

### Bug #17: ✅ Calendar Month Navigation Issues with Selected Date
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
When switching between months in the calendar while having a day selected, multiple issues occur: the activity title shows the previously selected date from the old month, the activities displayed appear to be from the first day of the new month, and no day appears visually selected in the new month's calendar view.

**Steps to Reproduce:**
1. Open the Calendar tab
2. Select a day (e.g., July 14)
3. Navigate to a different month using Previous/Next month buttons (e.g., move to June)
4. Observe the activity section title and calendar selection state

**Expected Behavior:**
When navigating to a new month:
- Either clear the selection and show no activities, OR
- Select the same day number in the new month if it exists (e.g., June 14), OR
- Select the first day of the new month and update the title accordingly
- The calendar should visually show which day is selected
- The activity title should match the actually selected date

**Actual Behavior:**
- Activity title still shows the old month's date (e.g., "July 14: ...")
- Activities displayed appear to be from a different date (e.g., June 1)
- No day appears visually selected in the calendar
- Inconsistency between displayed date and actual data

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Fixed calendar month navigation to maintain consistent date selection:
- **Smart Day Selection**: When switching months, attempts to select the same day number in the new month (e.g., July 14 → June 14)
- **Edge Case Handling**: If the day doesn't exist in the new month (e.g., Feb 30), selects the last day of the month
- **State Synchronization**: Updates both `selectedDate` variable and ViewModel selection to keep them in sync
- **Visual Update**: Calls `notifyCalendarChanged()` to refresh the calendar display and show the new selection
- **Fallback**: If no previous selection exists, defaults to the first day of the month

**Logic Flow**:
1. User switches month (Previous/Next buttons)
2. System attempts to maintain same day number in new month
3. Updates `selectedDate` variable and ViewModel state
4. Refreshes calendar view to show visual selection
5. Activity title and data now match the correctly selected date

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

### Bug #5: ✅ Duplicate Piece Entries from CSV Import
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** High

**Description:**
When importing CSV data, duplicate entries for "Somewhere That's Green" appeared - one with 20 activities and one with 2 activities.

**Root Cause:**
CSV contained different Unicode apostrophe characters:
- Regular apostrophe (') U+0027
- Curly apostrophe (') U+2019

**Resolution:**
- Created `TextNormalizer` utility class
- Implemented comprehensive Unicode normalization for all text input
- Applied normalization to import, export, and manual entry workflows
- Used Unicode escape sequences for reliable character replacement

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/utils/TextNormalizer.kt` (new)
- `app/src/main/java/com/pseddev/practicetracker/utils/CsvHandler.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/AddActivityViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/pieces/AddPieceViewModel.kt`

---

### Bug #18: ✅ Calendar Selection Ring Color Too Dark
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
The calendar selection ring around the selected date should be changed to a light bright shade of purple for better visual appeal and consistency with the app's design theme.

**Steps to Reproduce:**
1. Open the Calendar tab
2. Tap on any day in the calendar
3. Observe the color of the selection ring around the selected day

**Expected Behavior:**
The selection ring should be a light bright shade of purple that provides good visibility while being visually appealing.

**Actual Behavior:**
The current selection ring color may not match the desired light bright purple shade.

**Environment:**
- App Version: 1.0.8
- All Android devices

**Implementation Details:**
Updated calendar selection ring to use a light bright purple color:
- **Color**: Changed from dark purple (`#FF6200EE`) to light bright purple (`#FFCC99FF`)
- **Visibility**: Light purple provides better contrast against calendar backgrounds
- **Consistency**: Maintains 9dp stroke width from previous Bug #15 fix
- **User Experience**: More visually appealing and easier to see selection state

**Technical Implementation:**
- **Color Resource**: Added `calendar_selection_ring` color (#FFCC99FF) to colors.xml
- **Calendar Selection**: Updated CalendarFragment to use new color resource instead of purple_500
- **Stroke Application**: Maintains existing 9dp stroke width with new light bright purple color

**Files Modified:**
- `app/src/main/res/values/colors.xml`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/CalendarFragment.kt`

---

### Bug #19: ✅ Dashboard Suggestions Time Display Lacks Context
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Low

**Description:**
In the Suggestions section on the Dashboard tab, the time display for pieces shows relative time like "16 days ago" without specifying whether this refers to the last practice or last performance. The text should be more descriptive to provide better context for both the Suggestions section and any other areas on the Dashboard that display similar time information.

**Steps to Reproduce:**
1. Open the Dashboard tab
2. Scroll to the Suggestions section
3. Observe the time display for suggested pieces
4. Check other Dashboard areas that may display similar time information

**Expected Behavior:**
The time display should specify the activity type throughout the Dashboard, such as:
- "Last practice 16 days ago"
- "Last performance 16 days ago"

**Actual Behavior:**
The time display only shows relative time like "16 days ago" without specifying whether it was practice or performance.

**Environment:**
- App Version: 1.0.7.5
- All Android devices

**Implementation Details:**
Updated Dashboard suggestions to show activity type context in time displays:
- **Activity Type Detection**: Enhanced suggestions logic to include activity type from `lastActivity.activityType`
- **Descriptive Text**: Changed from generic "16 days ago" to specific "Last practice 16 days ago" or "Last performance 16 days ago"
- **Consistent Format**: Applied to both favorite and non-favorite suggestions throughout Dashboard
- **User Context**: Helps users make informed decisions about which pieces to practice next

**Technical Implementation:**
- **Dashboard**: DashboardViewModel.kt suggestions generation logic updated (lines 117-123 and 135-141)
- **Suggestions Tab**: SuggestionsViewModel.kt suggestions generation logic updated (lines 53-59 and 71-77)
- **Logic**: Added activity type detection using `when (lastActivity!!.activityType)` clause in both ViewModels
- **Text Format**: "Last practice X days ago" or "Last performance X days ago"
- **Scope**: Applied to all suggestion time displays across Dashboard and dedicated Suggestions tab

**Root Cause:** The app has two separate suggestion implementations:
1. **Dashboard suggestions** (condensed view) - was already fixed in initial implementation
2. **Dedicated Suggestions tab** (full view) - still showed generic time text until this fix

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/SuggestionsViewModel.kt`

---

### Bug #20: ✅ Missing Favorite Toggle in Pieces Tab
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-20
**Severity:** Medium

**Description:**
The Pieces tab should allow users to directly toggle whether a piece is marked as a favorite without having to navigate to the separate "Manage Favorites" screen. This would provide a more convenient way to manage favorites while viewing the pieces list.

**Steps to Reproduce:**
1. Open the Pieces tab
2. View the list of pieces/techniques
3. Look for a way to mark/unmark pieces as favorites

**Expected Behavior:**
Each piece in the Pieces tab should have a favorite toggle (such as a star icon) that allows users to:
- Mark non-favorite pieces as favorites by tapping the star
- Unmark favorite pieces by tapping the filled star
- See visual indication of current favorite status

**Actual Behavior:**
The Pieces tab only displays piece information without any way to manage favorite status. Users must navigate to Settings > Manage Favorites to change favorite status.

**Environment:**
- App Version: 1.0.7.5
- All Android devices

**Additional Information:**
- Could use star icon (filled for favorites, outline for non-favorites)
- Should update the favorites list immediately when toggled
- May need to refresh Dashboard suggestions when favorites change
- Consider adding this to the same location as the sorting options

**Implementation Details:**
Added direct favorite toggle functionality to Pieces tab:
- **Interactive Star Icon**: Made favorite icon clickable with proper touch target (32dp x 32dp)
- **Visual States**: Shows filled star for favorites, outline star for non-favorites
- **Immediate Toggle**: Tap star icon to instantly toggle favorite status
- **Database Update**: Updates PieceOrTechnique entity immediately via ViewModel
- **No Navigation Required**: Eliminates need to navigate to Settings > Manage Favorites

**Technical Implementation:**
- **Layout**: Updated item_piece_stats.xml to make star icon always visible and clickable
- **Adapter**: Enhanced PiecesAdapter with onFavoriteToggle callback and proper icon state management
- **ViewModel**: Added toggleFavorite() method to PiecesViewModel using viewModelScope.launch
- **Fragment**: Wired favorite toggle callback in PiecesFragment to connect UI to ViewModel
- **Icon States**: ic_star_filled for favorites, ic_star_outline for non-favorites

**User Experience Impact:**
- Eliminates multiple navigation steps previously required to manage favorites
- Provides immediate visual feedback for favorite status changes
- Aligns with modern mobile app patterns for favorite management
- Significantly improves usability and convenience

**Files Modified:**
- `app/src/main/res/layout/item_piece_stats.xml`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/PiecesAdapter.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/PiecesFragment.kt`

---

### Bug #21: ✅ Rebrand App from "Music Practice Tracker" to "PlayStreak"
**Status:** Fixed
**Date Reported:** 2025-07-20
**Date Fixed:** 2025-07-21
**Severity:** Medium

**Description:**
The app needs to be rebranded from "Music Practice Tracker" to "PlayStreak" throughout the application. This includes updating all user-facing text, titles, and branding elements. Where appropriate, the full branding should be "PlayStreak 🎵" to include the musical note emoji.

**Steps to Reproduce:**
1. Review all user-facing text in the app
2. Look for instances of "Music Practice Tracker"
3. Check Dashboard for "Current Streak" text
4. Review app titles, headers, and branding elements

**Expected Behavior:**
All instances should be updated to the new branding:
- "Music Practice Tracker" → "PlayStreak" or "PlayStreak 🎵" where appropriate
- Dashboard "Current Streak" → "Current PlayStreak 🎵"
- App titles and headers should reflect new branding
- Maintain consistency throughout the user interface

**Actual Behavior:**
The app currently uses "Music Practice Tracker" branding throughout.

**Environment:**
- App Version: 1.0.7.6
- All Android devices

**Rebranding Requirements:**
- **App Name**: Change to "PlayStreak"
- **Full Branding**: Use "PlayStreak 🎵" where appropriate for visual appeal
- **Dashboard Streak**: Change "Current Streak" to "Current PlayStreak 🎵"
- **Consistency**: Ensure uniform branding across all user-facing elements
- **Scope**: Update titles, headers, welcome messages, and any other branding text

**Areas to Update:**
- App navigation titles
- Dashboard headers and streak display
- Settings screen titles
- Any welcome or introductory text
- Fragment labels and titles
- README and documentation (if user-facing)

**Implementation Details:**
Completed comprehensive rebranding from "Music Practice Tracker" to "PlayStreak":
- **App Name**: Updated strings.xml app_name to "PlayStreak 🎵"
- **Navigation**: Changed main navigation label from "Music Practice Tracker" to "PlayStreak"
- **Dashboard**: Updated "Current Streak" to "Current PlayStreak 🎵" in fragment_dashboard.xml
- **Google Drive**: Changed APPLICATION_NAME in GoogleDriveHelper.kt to "PlayStreak"
- **Project**: Updated settings.gradle.kts rootProject.name to "PlayStreak"
- **Documentation**: Updated README.md title to "PlayStreak 🎵" and version to 1.0.7.6
- **License**: Updated LICENSE copyright from "Music Practice Tracker" to "PlayStreak"

**Files Modified:**
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/navigation/nav_graph.xml`
- `app/src/main/res/layout/fragment_dashboard.xml`
- `app/src/main/java/com/pseddev/practicetracker/utils/GoogleDriveHelper.kt`
- `settings.gradle.kts`
- `README.md`
- `LICENSE`

---

### Bug #22: ✅ Add Edit Functionality for Timeline Entries
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-21
**Severity:** Medium

**Description:**
Users should be able to edit existing entries in the timeline system, specifically allowing them to modify the time and date of practice sessions and performances. This would provide flexibility to correct data entry errors or adjust timing information after activities have been logged.

**Steps to Reproduce:**
1. Open the Timeline tab
2. View existing activity entries
3. Look for edit options on timeline entries

**Expected Behavior:**
Users should be able to:
- Tap on timeline entries to edit them
- Modify the date and time of the activity
- Save changes and see them reflected in the timeline
- Cancel edits without losing original data
- Have edited entries update across all views (Dashboard, Calendar, etc.)

**Actual Behavior:**
Timeline entries can only be deleted, not edited. Users cannot modify the date, time, or other details of existing activities.

**Environment:**
- App Version: 1.0.7.6
- All Android devices

**Additional Information:**
- Edit functionality should allow modification of date, time, and potentially other fields like notes or level
- Changes should propagate to all views that display the activity data
- Consider using the existing add activity flow as a template for the edit interface
- Should maintain data integrity and validation (e.g., can't set future dates)
- Edit action could be triggered by long press or dedicated edit button alongside the existing delete button

**Implementation Details:**
Completed comprehensive edit functionality for timeline entries:
- **Edit Button**: Added edit icon button next to delete button in timeline items
- **Navigation Flow**: Edit button navigates to add activity flow with pre-populated data
- **Data Storage**: Created EditActivityStorage for passing activity data between fragments
- **Database Support**: Added updateActivity method to repository and DAO layers
- **Edit Mode Detection**: Modified AddActivityViewModel to track edit mode state
- **Pre-population**: All add activity fragments now pre-populate fields in edit mode:
  - AddActivityFragment: Skips activity type selection and navigates directly to piece selection
  - SelectPieceFragment: Automatically navigates to level selection with existing piece
  - SelectLevelFragment: Pre-selects level and performance type from existing activity
  - TimeInputFragment: Pre-fills minutes field with existing duration
  - NotesInputFragment: Pre-fills notes field with existing notes
  - SummaryFragment: Shows original timestamp, updates button text to "Update", uses updateActivity instead of saveActivity
- **Timestamp Preservation**: Edit mode preserves original activity timestamp
- **Timeline Refresh**: Updated activities automatically refresh in timeline through LiveData
- **Date/Time Editing**: Added dedicated "Edit Date" and "Edit Time" buttons in edit mode
- **Date/Time Pickers**: Native Android DatePickerDialog and TimePickerDialog for intuitive editing
- **Real-time Updates**: Date display updates immediately when date/time is changed
- **Smart Back Navigation**: In edit mode, back button skips intermediate screens and returns directly to Timeline

**Technical Implementation:**
- **Database Layer**: Added `suspend fun update(activity: Activity)` to ActivityDao (already existed)
- **Repository Layer**: Added `suspend fun updateActivity(activity: Activity)` to PianoRepository
- **ViewModel Layer**: Added edit mode support to AddActivityViewModel with `setEditMode()` and `updateActivity()` methods
- **UI Layer**: Enhanced timeline item layout with edit button and modified all add activity fragments for edit mode
- **Data Flow**: EditActivityStorage → AddActivityViewModel → Repository → Database → LiveData → Timeline refresh
- **Date/Time Management**: Custom timestamp tracking with Calendar-based date/time manipulation
- **Enhanced ViewModel**: Added overloaded saveActivity method with custom timestamp support
- **Back Navigation**: OnBackPressedCallback implementation in all edit mode fragments to bypass skipped screens

**Files Modified:**
- `app/src/main/res/layout/item_timeline_activity.xml`
- `app/src/main/res/drawable/ic_edit.xml` (new)
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineAdapter.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/TimelineFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/progress/EditActivityStorage.kt` (new)
- `app/src/main/java/com/pseddev/practicetracker/data/repository/PianoRepository.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/AddActivityViewModel.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/AddActivityFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/SelectPieceFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/SelectLevelFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/TimeInputFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/NotesInputFragment.kt`
- `app/src/main/java/com/pseddev/practicetracker/ui/addactivity/SummaryFragment.kt`

---

### Bug #23: ✅ Update Package Structure and Code References to PlayStreak
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-21
**Severity:** Medium

**Description:**
The app package structure and internal code references should be updated to use "playstreak" instead of "practicetracker" to align with the new branding. This includes updating the main package name from `com.pseddev.practicetracker` to `com.pseddev.mystreak` and reviewing other places in the code where "playstreak" would make more sense than "practicetracker".

**Steps to Reproduce:**
1. Review current package structure in the codebase
2. Look for references to "practicetracker" in package names, class names, and variable names
3. Identify areas where "playstreak" would be more appropriate

**Expected Behavior:**
All package names, class names, and relevant code references should use "playstreak" terminology to maintain consistency with the new app branding.

**Actual Behavior:**
The codebase currently uses "practicetracker" throughout the package structure and various code references.

**Environment:**
- App Version: 1.0.7.7
- All Android devices

**Areas to Review and Update:**
- **Package Structure**: `com.pseddev.practicetracker` → `com.pseddev.mystreak`
- **Application Class**: `PianoTrackerApplication` → `PlayStreakApplication`
- **Export File Names**: `piano_tracker_export_*.csv` → `play_streak_export_*.csv`
- **SharedPreferences**: `piano_tracker_export_prefs` → `play_streak_export_prefs`
- **Class Names**: Classes that reference "tracker" or "piano"
- **Variable Names**: Variables that could better reflect "playstreak" terminology
- **String Resources**: Internal string references (not user-facing)
- **Gradle Configuration**: Application ID and related configurations

**Implementation Considerations:**
- This is a significant refactoring that will affect many files
- Need to ensure all imports are updated correctly
- Update build configuration (applicationId, etc.)
- Review impact on existing user data and settings

**Priority Notes:**
- Should be done before major release to avoid migration issues
- Consider doing this as part of a version bump
- May want to coordinate with other structural changes

**Implementation Details:**
Completed comprehensive package refactoring and code restructuring:
- **Package Structure**: Successfully moved all 53 Kotlin files from `com.pseddev.practicetracker` to `com.pseddev.mystreak`
- **Application Class**: Renamed `PianoTrackerApplication` to `PlayStreakApplication` with all references updated
- **Export File Names**: Updated from `piano_tracker_export_*.csv` to `play_streak_export_*.csv`
- **SharedPreferences**: Updated from `piano_tracker_export_prefs` to `play_streak_export_prefs`
- **Build Configuration**: Updated applicationId and namespace to `com.pseddev.mystreak`
- **Import Statements**: Updated all internal imports across the entire codebase
- **Configuration Files**: Updated AndroidManifest.xml and navigation graph with new class paths
- **Directory Structure**: Created new package hierarchy and removed old structure completely
- **Verification**: Ensured no old package references remain anywhere in the codebase

**Technical Implementation:**
- **Files Moved**: 53 Kotlin source files across 7 package directories
- **Package Declarations**: Updated all `package com.pseddev.practicetracker.*` statements
- **Import Updates**: Updated all internal imports and R class references
- **Class Renaming**: `PianoTrackerApplication` → `PlayStreakApplication`
- **Configuration Updates**: AndroidManifest.xml, nav_graph.xml, build.gradle.kts
- **Cleanup**: Removed old directory structure and cleaned build cache

**Files Modified:**
- All 53 source files in the new `com.pseddev.mystreak` package
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/navigation/nav_graph.xml`
- `app/build.gradle.kts`

**Additional Information:**
- This change aligns with the recent app rebranding from "Music Practice Tracker" to "PlayStreak"
- Provides complete consistency between user-facing branding and internal code structure
- Improves code maintainability and developer understanding
- Users will need to export data before upgrading and reimport after due to package change

---

### Bug #24: ✅ Rename Database from "piano_tracker_database" to "playstreak_database"
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-21
**Severity:** Low

**Description:**
As part of the PlayStreak rebranding effort, the database name should be updated from "piano_tracker_database" to "playstreak_database" to align with the new app branding. This change maintains consistency between the user-facing brand and internal technical naming.

**Steps to Reproduce:**
1. Review the AppDatabase.kt file
2. Locate the Room database builder configuration
3. Observe the current database name

**Expected Behavior:**
The database should be named "playstreak_database" to match the PlayStreak branding.

**Actual Behavior:**
The database is currently named "piano_tracker_database" which reflects the old branding.

**Environment:**
- App Version: 1.0.7.8
- All Android devices

**Implementation Details:**
Updated database name in Room configuration:
- **File**: AppDatabase.kt
- **Change**: `"piano_tracker_database"` → `"playstreak_database"`
- **Location**: Room.databaseBuilder() call in getDatabase() method
- **Impact**: New installations will use the new database name
- **Migration Strategy**: Small user base can export to CSV and reimport after upgrade (no complex migration needed)

**User Impact:**
- **New Users**: Will automatically use the new database name
- **Existing Users**: Will need to export data to CSV before upgrading and reimport after upgrade
- **Data Preservation**: Export/import workflow preserves all user data

**Files Modified:**
- `app/src/main/java/com/pseddev/practicetracker/data/AppDatabase.kt`

---

### Bug #25: ❌ Incorrect Activity Count Sorting in Pieces Tab
**Status:** Closed
**Date Reported:** 2025-07-21
**Date Closed:** 2025-07-21
**Severity:** Medium

**Description:**
When sorting pieces by activity count in the Pieces tab, the sort order is incorrect. Pieces with fewer activities appear between pieces with more activities, despite using descending sort order.

**Steps to Reproduce:**
1. Import CSV data with multiple activities for various pieces
2. Open the Pieces tab
3. Sort by "Activity" (activity count)
4. Observe the sort order

**Expected Behavior:**
Pieces should be sorted by activity count in descending order (highest count first). For example:
- Gimmie Gimmie (20 activities)
- Don't Cry Out Loud (15 activities)
- Goodbye Yellow Brick Road (14 activities)

**Actual Behavior:**
Goodbye Yellow Brick Road (14 activities) appears between Gimmie Gimmie (20 activities) and Don't Cry Out Loud (15 activities), which violates the descending sort order.

**Environment:**
- App Version: 1.0.7.8
- All Android devices

**Technical Analysis:**
- **Sorting Logic**: PiecesViewModel.kt uses `sortedBy { it.activityCount }` with direction reversal for descending order
- **Data Source**: Activity counts calculated from `activities.filter { it.pieceOrTechniqueId == piece.id }.size`
- **Suspected Issue**: Possible inconsistency in activity count calculation or comparison logic
- **CSV Import**: Issue appears after importing historical data from piano_tracker_export_2025-07-20_233021.csv

**Closure Reason:**
Closed as user error - reporter was mistaken about the sorting behavior. The activity count sorting is working correctly as designed.

**Additional Information:**
This issue appeared after importing the 07-20 export data, suggesting it may be related to how imported activities are being counted or filtered in the sorting logic.

---

### Bug #26: ✅ Suggestions Should Fallback to Least Recent Favorite When No "Oldest" Favorites Found
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-21
**Severity:** Medium

**Description:**
The suggestions algorithm in both the Dashboard suggestions section and the dedicated Suggestions tab currently only suggests pieces that meet the "oldest" criteria. If no favorites qualify as "oldest" based on the current algorithm, no favorite suggestions are shown. Similarly, if no non-favorites qualify as "oldest", no non-favorite suggestions are shown. The system should implement fallback logic to ensure users always get recommendations when pieces exist in each category.

**Steps to Reproduce:**
1. Have pieces (both favorites and non-favorites) that don't meet the current "oldest" algorithm criteria
2. Open the Dashboard tab and view the Suggestions section
3. Open the dedicated Suggestions tab
4. Observe that no suggestions are displayed for categories where pieces don't meet "oldest" criteria

**Expected Behavior:**
When pieces don't meet the "oldest" criteria, the suggestions should implement fallback logic:
- **Favorites**: If no favorites meet "oldest" criteria, show the least recently practiced/performed favorite piece
- **Non-Favorites**: If no non-favorites meet "oldest" criteria, show the most recently practiced/performed inactive (non-favorite) piece
- Ensure users always get suggestions when pieces exist in each category
- Maintain consistent behavior between Dashboard suggestions and dedicated Suggestions tab

**Actual Behavior:**
When pieces don't meet the "oldest" criteria, no suggestions are shown for those categories, leaving users without guidance on what to practice when the algorithm finds no "oldest" pieces.

**Environment:**
- App Version: 1.0.7.8
- All Android devices

**Technical Analysis:**
- **Affected Components**:
  - Dashboard suggestions in DashboardViewModel.kt
  - Dedicated Suggestions tab in SuggestionsViewModel.kt
- **Current Algorithm**: Only shows favorites that are determined to be "oldest" by the existing logic
- **Missing Fallback**: No fallback mechanism when pieces don't qualify as "oldest"
- **Expected Logic**:
  - If oldest favorites list is empty, find the favorite with the earliest lastActivityDate
  - If oldest non-favorites list is empty, find the non-favorite with the latest lastActivityDate

**Resolution:**
Implemented comprehensive fallback logic in both suggestion systems:
- **Favorites fallback**: When no favorites meet the "oldest" criteria (2+ days), system fallbacks to suggest the least recently practiced favorite piece
- **Non-favorites fallback**: When no non-favorites meet the "oldest" criteria (7+ days but within 31 days), system fallbacks to suggest the most recently practiced non-favorite piece
- **Consistent behavior**: Applied same logic to both Dashboard suggestions and dedicated Suggestions tab
- **Preserved existing logic**: Normal suggestion criteria still apply when pieces meet the "oldest" thresholds

**Technical Implementation:**
- **DashboardViewModel**: Enhanced suggestions flow with fallback logic for both categories
- **SuggestionsViewModel**: Refactored to separate favorites and non-favorites processing with fallback support
- **Fallback criteria**:
  - Favorites: Finds piece with highest daysSinceLastActivity (least recent)
  - Non-favorites: Finds piece with lowest daysSinceLastActivity (most recent, among practiced pieces)
- **User experience**: Ensures users always get suggestions when pieces exist in each category

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

---

### Bug #27: ✅ Increase Suggestion Fallback Limit from 2 to 4 Pieces per Category
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-22
**Severity:** Low

**Description:**
The suggestion fallback logic currently limits results to a maximum of 2 favorite pieces and 2 non-favorite pieces. This limit should be increased to 4 pieces per category to provide users with more options when the regular suggestion algorithm doesn't find pieces meeting the "oldest" criteria.

**Steps to Reproduce:**
1. Have a scenario where the regular suggestion algorithm finds no "oldest" pieces
2. Observe fallback suggestions in Dashboard or Suggestions tab
3. Count the number of suggestions shown per category

**Expected Behavior:**
Fallback suggestions should show up to:
- **4 favorite pieces** (instead of current 2)
- **4 non-favorite pieces** (instead of current 2)

**Actual Behavior:**
Fallback suggestions currently limit to:
- **2 favorite pieces**
- **2 non-favorite pieces**

**Environment:**
- App Version: 1.0.7.8
- All Android devices

**Technical Details:**
- **Current Implementation**: Uses `.take(2)` in both DashboardViewModel and SuggestionsViewModel
- **Required Change**: Update `.take(2)` to `.take(4)` in fallback logic for both categories
- **Files to Modify**:
  - `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
  - `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

**Resolution:**
Successfully increased fallback suggestion limits from 2 to 4 pieces per category:
- **Favorites fallback**: Now suggests up to 4 pieces (was 2)
- **Non-favorites fallback**: Now suggests up to 4 pieces (was 2)
- **Implementation**: Updated `.take(2)` to `.take(4)` in both DashboardViewModel and SuggestionsViewModel
- **Preserved logic**: Maintained all existing sorting and tie-breaking algorithms

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

---

### Bug #28: ✅ Always Provide Up to 4 Suggestions Per Category Using Combined Regular and Fallback Logic
**Status:** Fixed
**Date Reported:** 2025-07-21
**Date Fixed:** 2025-07-21
**Severity:** Medium

**Description:**
The suggestion system should always aim to provide up to 4 favorite pieces and 4 non-favorite pieces by combining regular suggestions with fallback suggestions when needed. Currently, if the regular algorithm finds fewer than 4 pieces per category, the system doesn't supplement with fallback suggestions to reach the target of 4 per category.

**Steps to Reproduce:**
1. Have a scenario where regular suggestions find fewer than 4 favorites or 4 non-favorites
2. Observe total suggestion count in Dashboard or Suggestions tab
3. Note that fallback logic only triggers when regular algorithm finds zero pieces

**Expected Behavior:**
The system should always try to provide up to 4 suggestions per category:
- If regular algorithm finds 2 favorites, supplement with 2 fallback favorites to reach 4 total
- If regular algorithm finds 1 non-favorite, supplement with 3 fallback non-favorites to reach 4 total
- If not enough pieces exist to reach 4, show all available pieces up to the maximum possible

**Actual Behavior:**
Fallback logic only activates when regular algorithm finds zero pieces in a category. If regular algorithm finds 1-3 pieces, no supplementation occurs, resulting in fewer than 4 suggestions per category.

**Environment:**
- App Version: 1.0.7.8
- All Android devices

**Technical Analysis:**
- **Current Logic**: Fallback only triggers when regular suggestions are empty (`if (favoriteSuggestions.isEmpty())`)
- **Required Logic**: Always combine regular and fallback to reach target of 4 per category
- **Implementation**: Modify suggestion logic to supplement regular results with fallback results

**Implementation Requirements:**
- Calculate how many additional suggestions are needed per category (4 - regular count)
- Apply fallback logic to find additional pieces (excluding already selected pieces)
- Combine regular and supplemental suggestions while maintaining sort order
- Handle edge cases where not enough total pieces exist to reach 4 per category

**Resolution:**
Implemented intelligent combined regular+fallback suggestion system:
- **Smart Supplementation**: Always aims for 4 suggestions per category by supplementing regular suggestions with fallback when needed
- **Duplicate Prevention**: Tracks used piece IDs to avoid suggesting the same piece twice
- **Dynamic Calculation**: Calculates exactly how many fallback suggestions are needed (4 - regular count)
- **Consistent Behavior**: Applied same logic to both Dashboard and Suggestions tab
- **Examples**:
  - Regular finds 2 favorites → adds 2 fallback favorites = 4 total
  - Regular finds 0 non-favorites → adds 4 fallback non-favorites = 4 total
  - Regular finds 4+ pieces → uses regular suggestions only

**Technical Implementation:**
- **Variable Logic**: `neededCount = 4 - regularSuggestions.size`
- **Filter Logic**: `pieces.filter { it.id !in usedPieceIds }` prevents duplicates
- **Combination**: `regularSuggestions + fallbackSuggestions`
- **Preserved Features**: All existing sorting, tie-breaking, and grammar fixes maintained

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

---

### Bug #29: ✅ Incorrect Sort Order for Non-Favorites in Suggestions Tab
**Status:** Fixed
**Date Reported:** 2025-07-22
**Date Fixed:** 2025-07-22
**Severity:** Medium

**Description:**
The sort order for non-favorite pieces in the Suggestions tab is incorrect. When comparing the same non-favorite suggestions between the Dashboard tab and the Suggestions tab, they appear in different orders. The Dashboard tab shows the correct sort order, but the Suggestions tab shows them in the wrong order.

**Steps to Reproduce:**
1. Open the Dashboard tab and observe the order of non-favorite suggestions
2. Switch to the Suggestions tab
3. Compare the order of the same non-favorite pieces between both tabs

**Expected Behavior:**
Non-favorite pieces should appear in the same sort order in both the Dashboard tab and Suggestions tab. The Dashboard tab appears to have the correct sort order.

**Actual Behavior:**
Non-favorite pieces appear in different sort orders between Dashboard and Suggestions tabs, with the Suggestions tab showing an incorrect order.

**Environment:**
- App Version: Current development version
- All Android devices

**Additional Information:**
- Favorites appear to sort correctly in both tabs
- Only non-favorites have this sort order discrepancy
- The Dashboard implementation appears to be correct and should be the reference

**Root Cause:**
The issue was caused by an additional sorting operation in `SuggestionsViewModel.kt` that was applied AFTER combining favorites and non-favorites. This additional sort disrupted the carefully established order within each category.

- **Dashboard (correct)**: Simple concatenation `finalFavorites + finalNonFavorites`
- **Suggestions (incorrect)**: Additional sort by `daysSinceLastActivity` across ALL suggestions, disrupting category-specific ordering

**Resolution:**
Removed the problematic additional sorting operation in `SuggestionsViewModel.kt` and replaced with simple concatenation to match `DashboardViewModel.kt` approach. Non-favorite pieces now appear in the same order in both Dashboard and Suggestions tabs.

**Technical Fix:**
- **Issue**: Lines 199-200 in SuggestionsViewModel applied `sortedWith(compareBy { !it.piece.isFavorite }.thenByDescending { it.daysSinceLastActivity })` which re-sorted all suggestions by days since last activity
- **Fix**: Replaced with simple concatenation `finalFavoriteSuggestions + finalNonFavoriteSuggestions`
- **Result**: Both tabs now use identical suggestion ordering logic

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

**Verification:**
Confirmed that non-favorite pieces appear in the same sort order in both Dashboard and Suggestions tabs.

---

### Bug #30: ✅ Fix "No pieces recorded yet" Text in Pieces Tab
**Status:** Implemented
**Date Reported:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Low
**Requested By:** User Interface Team

**Description:**
The empty state message in the Pieces tab currently shows "No pieces recorded yet" which is grammatically awkward and doesn't match the app's terminology. This should be changed to "No pieces added yet" for better clarity and consistency with the app's language around adding pieces.

**Steps to Reproduce:**
1. Open the Pieces tab when no pieces have been added to the app
2. Observe the empty state message text

**Expected Behavior:**
The empty state message should read "No pieces added yet" to match the app's terminology used elsewhere (e.g., "Add Piece" buttons).

**Actual Behavior:**
The empty state message shows "No pieces recorded yet" which sounds awkward and doesn't align with the app's standard terminology.

**Environment:**
- App Version: Current development version
- All Android devices

**Technical Considerations:**
- Locate empty state text in Pieces tab layout or string resources
- Update text resource or hardcoded string to new wording
- Verify change doesn't affect other parts of the app using same string resource
- Test empty state display to ensure proper formatting

**Priority Justification:**
Low priority cosmetic fix that improves user experience through clearer, more grammatically correct messaging that aligns with the app's terminology standards.

**Resolution:**
Updated the empty state text in the Pieces tab from "No pieces recorded yet" to "No pieces added yet" to improve grammar and align with the app's consistent terminology around adding pieces.

**Implementation Details:**
- **Text Change**: Updated android:text attribute from "No pieces recorded yet" to "No pieces added yet"
- **Location**: Found in fragment_pieces.xml layout file, line 101
- **Consistency**: Now matches terminology used in "Add Piece" buttons and other UI elements
- **User Experience**: Provides clearer, more grammatically correct messaging for empty state

**Files Modified:**
- `app/src/main/res/layout/fragment_pieces.xml`

---

### Bug #31: 🔄 Calendar Heat Map Doesn't Auto-Update When Scrolling to Imported Historical Data
**Status:** In Progress
**Date Reported:** 2025-07-23
**Priority:** Medium
**Requested By:** User Testing

**Description:**
After importing the PlayTest_2024.csv document, the calendar heat map doesn't automatically update to show activity data when scrolling back to early 2024. The heat map appears empty for historical dates that should have imported activity data, requiring manual refresh or navigation to see the data properly.

**Steps to Reproduce:**
1. Import the PlayTest_2024.csv file using the CSV import functionality
2. Navigate to the Calendar tab
3. Scroll back to early 2024 (January, February, March 2024)
4. Observe the heat map display for dates that should contain imported activities

**Expected Behavior:**
The calendar heat map should automatically load and display activity data for historical dates when scrolling, showing appropriate heat map colors for days with imported activities from 2024.

**Actual Behavior:**
The heat map appears empty/blank for historical dates in early 2024, even though the imported data contains activities for those dates. The heat map may require manual refresh, app restart, or other navigation to properly display the imported historical data.

**Environment:**
- App Version: 1.0.8.3-beta
- Test Data: PlayTest_2024.csv
- Calendar tab heat map functionality
- Historical data scrolling behavior

**Technical Analysis:**
- Issue likely related to calendar data loading/refresh logic after import
- Heat map may not be invalidating cached data for historical date ranges
- Calendar view might need to refresh activity data when scrolling to new date ranges
- Import process may not be triggering proper calendar data refresh

**Potential Causes:**
- Calendar fragment not observing activity data changes after import
- Heat map calculation not refreshing for dates outside current visible range
- Calendar view caching issue preventing display of newly imported historical data
- LiveData/Observer pattern not properly updating calendar when data changes

**Priority Justification:**
Medium priority as this affects user experience after data import, particularly for users importing historical practice data. While not preventing core functionality, it creates confusion about whether import was successful and reduces confidence in data integrity.

**Investigation Needed:**
- Verify that imported 2024 activities are properly stored in database
- Check if calendar heat map refreshes when navigating away and back to Calendar tab
- Test if manual refresh actions (pull-to-refresh, tab switching) resolve the display
- Examine calendar data loading logic and LiveData observers for historical date ranges

---

### Bug #32: ✅ Performance Suggestions Second Tier Should Exclude Pieces Performed Today
**Status:** Fixed
**Date Reported:** 2025-07-23
**Severity:** Medium
**Reporter:** User Experience Team

**Description:**
The Performance Suggestions algorithm's second tier was suggesting pieces that were performed earlier the same day. This creates awkward situations where users might be suggested to perform a piece they already performed that morning or earlier in the day.

**Steps to Reproduce:**
1. Perform a piece (that meets the algorithm requirements) earlier today
2. Open Dashboard or Suggestions tab
3. Check Performance Suggestions
4. The piece performed today appears in the suggestions

**Expected Behavior:**
Pieces performed today should not be suggested for performance until the next day, even if they meet all other algorithm requirements.

**Actual Behavior:**
Pieces performed earlier the same day were being suggested in the second tier for continued performance rotation.

**Algorithm Details:**
**Before Fix:**
- **Second Tier**: Practiced ≥3 times in 28 days AND performed in last 28 days
- **Issue**: Could suggest pieces performed hours ago on the same day

**After Fix:**
- **Second Tier**: Practiced ≥3 times in 28 days AND performed in last 28 days BUT NOT today
- **Logic**: Added `lastPerformanceDate!! < startOfToday` condition to exclude same-day performances

**Environment:**
- App Version: 1.0.8+
- Feature: Performance Suggestions (Feature #32)
- Affects: Pro users only

**Technical Implementation:**
- **Files Modified**:
  - `DashboardViewModel.kt` - Updated second tier condition
  - `SuggestionsViewModel.kt` - Updated second tier condition
- **Logic Change**: Added `startOfToday` timestamp calculation (midnight boundary)
- **Condition Updated**: Changed `} else {` to `} else if (lastPerformanceDate!! < startOfToday) {`
- **Impact**: More practical suggestions that avoid same-day redundancy

**Solution:**
Added `startOfToday` calculation in both ViewModels:
```kotlin
val startOfToday = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
```

Updated second tier condition to exclude pieces performed today:
```kotlin
} else if (lastPerformanceDate!! < startOfToday) {
    // Second tier: Practiced ≥3 times in 28 days AND performed in 28 days BUT not today
```

**Test Cases:**
- ✅ Piece performed yesterday → Still eligible for second tier
- ✅ Piece performed 5 days ago → Still eligible for second tier
- ✅ Piece performed 20 days ago → Still eligible for second tier
- ❌ Piece performed this morning → Excluded from both tiers until tomorrow
- ✅ Piece performed 30 days ago → First tier (not performed in 28 days)

---

## Bug Report Template

When reporting new bugs, please use this template:

```markdown
### 🐛 [Bug Title]
**Status:** Open
**Date Reported:** YYYY-MM-DD
**Severity:** [Critical/High/Medium/Low]
**Reporter:** [Name/Username]

**Description:**
[Clear description of the bug]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Environment:**
- App Version: [e.g., 1.0.8]
- Android Version: [e.g., Android 13]
- Device: [e.g., Pixel 7]

**Additional Information:**
[Any other relevant details, screenshots, logs, etc.]
```

---

## Fixed Bugs Archive

This section contains bugs that have been resolved and can serve as reference for similar issues in the future.

### Text Normalization Issues
**Common Cause:** Unicode character variations (apostrophes, quotes, whitespace)
**Solution Pattern:** Implement comprehensive text normalization using Unicode escape sequences
**Prevention:** Always normalize user input at entry points (manual input, import, etc.)