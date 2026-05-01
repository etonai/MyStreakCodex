# Ticket #10: Toggle Verbose Calendar Mode

**Status:** 🎫 Open
**Date Created:** 2025-07-26
**Priority:** Medium
**Type:** Feature Enhancement

## Problem Statement

Currently, the app has two separate views for viewing practice data:
1. **Calendar View**: Shows practice days with basic indicators but limited detail
2. **Timeline View**: Shows detailed list of all activities with full information and editing capabilities

Users must switch between tabs to get different perspectives on their practice data. The Calendar view provides excellent visual context for practice patterns, but lacks the detailed information and editing capabilities available in the Timeline.

## Proposed Solution

Add a toggle option for "Verbose Calendar Mode" that transforms the calendar into a comprehensive view combining the benefits of both current views:

- **Visual Context**: Maintains calendar's visual representation of practice patterns
- **Detailed Information**: Shows full activity details similar to Timeline view
- **Interactive Functionality**: Allows editing and deleting activities directly from calendar
- **Unified Experience**: Potentially eliminates the need for a separate Timeline tab

## User Experience Design

### Default Calendar Mode (Current)
- Shows practice days with simple indicators (dots, colors, streak numbers)
- Minimal information per day
- Read-only view

### Verbose Calendar Mode (New)
- **Day Selection**: Tap on any day to expand detailed view
- **Activity Details**: Shows all activities for that day with:
  - Piece names and types
  - Activity types (Practice/Performance)
  - Duration and difficulty levels
  - Notes and timestamps
- **Edit Functionality**: Long-press or edit button to modify activities
- **Delete Functionality**: Swipe or delete button to remove activities
- **Add Activities**: Quick-add button for selected day

### Toggle Implementation
- **Settings Integration**: Toggle option in app settings
- **In-View Toggle**: Quick toggle button in calendar toolbar
- **Persistence**: Remember user preference between sessions

## Technical Implementation

### UI/UX Components

**Calendar Day Enhancement:**
```kotlin
// Enhanced day view for verbose mode
data class VerboseCalendarDay(
    val date: LocalDate,
    val activities: List<Activity>,
    val isExpanded: Boolean = false,
    val totalDuration: Int,
    val practiceCount: Int,
    val performanceCount: Int
)
```

**New UI Elements:**
- Expandable day cells with detailed activity lists
- Edit/delete action buttons for each activity
- Quick-add floating action button for selected day
- Toggle switch in calendar toolbar

### Key Implementation Areas

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/CalendarFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/CalendarViewModel.kt`
- `app/src/main/res/layout/fragment_calendar.xml`
- `app/src/main/res/layout/calendar_day_verbose.xml` (new)
- `app/src/main/java/com/pseddev/mystreak/ui/progress/CalendarDayAdapter.kt` (new)
- `app/src/main/java/com/pseddev/mystreak/utils/PreferencesManager.kt`

**Integration Points:**
- Activity editing logic from Timeline (reuse existing components)
- Activity deletion with confirmation dialogs
- Quick-add functionality integration
- Settings preference storage

### Implementation Phases

**Phase 1: Basic Verbose Mode (2-3 days)**
- [ ] Add toggle functionality in calendar toolbar
- [ ] Implement expandable day cells with activity details
- [ ] Create verbose day layout with activity list
- [ ] Add preference storage for toggle state

**Phase 2: Interactive Features (2-3 days)**
- [ ] Integrate edit functionality for activities
- [ ] Add delete functionality with confirmation
- [ ] Implement quick-add for selected days
- [ ] Add activity modification callbacks

**Phase 3: UI Polish & Optimization (1-2 days)**
- [ ] Smooth expand/collapse animations
- [ ] Optimize performance for large activity datasets
- [ ] Improve visual design and accessibility
- [ ] Add loading states and error handling

**Phase 4: Testing & Timeline Evaluation (1-2 days)**
- [ ] Comprehensive testing of verbose mode functionality
- [ ] User experience testing and feedback
- [ ] Evaluate whether Timeline tab is still needed
- [ ] Performance testing with large datasets

## Expected Benefits

### User Experience
- **Unified View**: Single view combining visual calendar context with detailed information
- **Reduced Navigation**: Less tab switching between Calendar and Timeline
- **Contextual Editing**: Edit activities directly in calendar context
- **Flexible Viewing**: Users can choose information density level

### Technical Benefits
- **Code Reuse**: Leverage existing Timeline editing components
- **Simplified Navigation**: Potentially eliminate Timeline tab
- **Better Data Utilization**: Make full use of calendar view space
- **Improved Accessibility**: Single comprehensive view for all users

## Timeline Tab Evaluation

### Potential Timeline Replacement
If Verbose Calendar Mode provides equivalent functionality:
- **Timeline Benefits**: Chronological list view, filtering capabilities
- **Calendar Benefits**: Visual context, date-based navigation, pattern recognition
- **User Preference**: Some users may prefer list view vs. calendar view

### Recommendation
- Implement Verbose Calendar Mode first
- Gather user feedback on functionality completeness
- Consider making Timeline tab optional or removable
- Maintain Timeline as backup until user preference is established

## Success Criteria

1. **Functionality Parity**: Verbose mode provides all Timeline editing capabilities
2. **Performance**: Calendar loads and responds smoothly even in verbose mode
3. **User Adoption**: Users prefer verbose mode over separate Timeline view
4. **Data Integrity**: All edit/delete operations maintain data consistency
5. **Accessibility**: Verbose mode works well for users with different needs

## Open Questions

**Q1: Timeline Tab Retention**
Should we keep the Timeline tab as an option, or completely replace it with Verbose Calendar Mode?

*Considerations:*
- Some users may prefer chronological list view
- Timeline may be better for searching across long date ranges
- Calendar provides better visual context for practice patterns

**Q2: Default Mode**
Should Verbose Calendar Mode be the default for new users, or should users opt-in?

*Considerations:*
- New users may benefit from more information immediately
- Verbose mode may be overwhelming for casual users
- Power users likely prefer detailed information

**Q3: Performance with Large Datasets**
How should we handle performance when users have hundreds of activities per month?

*Considerations:*
- Pagination or lazy loading for activity details
- Limit visible activities per day in verbose mode
- Provide filtering options within calendar view

## Future Enhancements

Once core implementation is complete, this foundation enables:
- **Advanced Filtering**: Filter calendar by piece, activity type, or difficulty
- **Bulk Operations**: Select multiple days for bulk editing or analysis
- **Calendar Export**: Export specific date ranges with full activity details
- **Practice Goals Integration**: Show goal progress directly in calendar view
- **Collaborative Features**: Share specific calendar days or date ranges