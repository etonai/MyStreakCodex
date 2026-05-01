# Feature Tracking

This document tracks feature requests, their status, and implementation details for the PlayStreak 🎵 app.

## Status Legend
- 💡 **Requested** - Feature has been suggested and is under consideration
- 🔄 **In Progress** - Feature is currently being developed
- 🔍 **Verifying** - Feature implementation is complete and undergoing testing/verification
- ✅ **Implemented** - Feature has been completed, verified, and is available
- ❌ **Declined** - Feature request declined (e.g., not aligned with app goals, technical limitations)
- 🔮 **Future** - Feature planned for future release

## Feature Requests

### Feature #1: ✅ Pro Mode - Premium Feature Differentiation
**Status:** Implemented
**Date Requested:** 2025-07-21
**Date Implemented:** 2025-07-21
**Priority:** High
**Requested By:** Business Development

**Description:**
Implement a Pro Mode system that differentiates between Free and Pro users, with Pro users having access to enhanced features and capabilities. This will include a global setting mechanism to track user subscription status and conditionally enable premium features throughout the app.

**User Story:**
As a business, we want to offer premium features to Pro subscribers so that we can generate revenue while still providing value to free users, and as a Pro user, I want access to enhanced features that justify my subscription cost.

**Acceptance Criteria:**
- [x] Global `isProUser` setting/preference system
- [x] Pro user status detection and persistence across app sessions
- [ ] UI indicators showing Pro-only features (with upgrade prompts for free users)
- [ ] Graceful feature limiting for free users without breaking core functionality
- [ ] Pro user onboarding and feature discovery
- [x] Settings screen showing current subscription status
- [x] Feature gate system that can be easily extended for new Pro features
- [ ] Upgrade prompt system for free users attempting to access Pro features

**Technical Considerations:**
- Implement global user preference system (`SharedPreferences` or Room database)
- Create `ProUserManager` or similar class to handle Pro status checks
- Design feature gate annotations or utility methods for easy Pro feature implementation
- Update UI components to show Pro badges/indicators where appropriate
- Consider future integration with Google Play Billing for actual subscription management
- Ensure backwards compatibility and graceful degradation for free users
- Create consistent upgrade prompt UI patterns

**Priority Justification:**
High priority as this establishes the foundation for monetization strategy and must be implemented before rolling out premium features. This system will enable future revenue generation while maintaining a good free user experience.

**Implementation Details:**
- **ProUserManager**: Created singleton class managing Pro status via SharedPreferences
- **Storage**: Uses `playstreak_pro_prefs` SharedPreferences with `is_pro_user` boolean key
- **API**: Provides `isProUser()`, `setProUser(boolean)`, `toggleProStatus()`, and `resetToFreeUser()` methods
- **Testing**: Added Pro status toggle button in Settings for development/testing purposes
- **Future Migration**: Architecture supports easy integration with Google Play Billing later

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/utils/ProUserManager.kt` (new)
- `app/src/main/java/com/pseddev/mystreak/ui/main/MainFragment.kt`
- `app/src/main/res/layout/fragment_main.xml`

**Implementation Notes:**
- Start with simple boolean flag system that can be extended later
- Design UI patterns that clearly communicate value of Pro features
- Ensure all existing features remain available to free users initially
- Plan migration path for implementing actual subscription system later

---

### Feature #2: ✅ Pro Badge in Settings Title
**Status:** Implemented
**Date Requested:** 2025-07-21
**Date Implemented:** 2025-07-21
**Priority:** High
**Requested By:** Development Team

**Description:**
Display "Pro" after "PlayStreak" in the Settings page title when the user is a Pro subscriber. This serves as a simple visual indicator of Pro status and will be the first feature to test the Pro/Free user differentiation system.

**User Story:**
As a Pro user, I want to see visual confirmation of my Pro status in the Settings page so that I know my subscription is recognized by the app.

**Acceptance Criteria:**
- [x] Settings page title shows "PlayStreak" for free users
- [x] Settings page title shows "PlayStreak Pro" for Pro users
- [x] Title updates dynamically when Pro status changes
- [x] Consistent styling and formatting for both title variants

**Technical Considerations:**
- Integrate with the global `isProUser` setting system from Feature #1
- Update Settings fragment to check Pro status and modify title accordingly
- Ensure title updates if Pro status changes during the session
- Consider using string resources for easy localization

**Priority Justification:**
High priority as this is the first implementation to test the Pro Mode infrastructure. It's simple, low-risk, and provides immediate visual feedback to validate the Pro user detection system works correctly.

**Implementation Details:**
- **Title Logic**: Settings page title dynamically updates based on `ProUserManager.isProUser()`
- **Display**: Shows "PlayStreak" for free users, "PlayStreak Pro" for Pro users
- **Real-time Updates**: Title refreshes in `onResume()` to reflect Pro status changes
- **Testing Integration**: Works seamlessly with Pro status toggle button for testing

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/main/MainFragment.kt`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Serves as a proof-of-concept for the Pro user differentiation system
- Can be used to test Pro status toggling during development

---

### Feature #3: ✅ Limited Heat Map for Free Users
**Status:** Implemented
**Date Requested:** 2025-07-21
**Date Implemented:** 2025-07-21
**Priority:** High
**Requested By:** Product Team

**Description:**
Implement a limited heat map feature for the calendar that differentiates between Free and Pro users. Free users will see a simplified heat map where days with any activities show as light blue, while Pro users will see the full heat map with multiple color intensities based on activity levels.

**User Story:**
As a Free user, I want to see which days I practiced (light blue) so that I can track my practice consistency, and as a Pro user, I want to see detailed activity intensity levels so that I can better understand my practice patterns and volume.

**Acceptance Criteria:**
- [x] Free users see binary heat map: no color for days with no activities, light blue for days with any activities
- [x] Pro users see full heat map with multiple color intensities based on activity count/duration
- [x] Heat map updates immediately when Pro status changes
- [x] Consistent visual experience within each user tier
- [x] Clear visual differentiation between Free and Pro heat map experiences
- [x] Activity Level Color Guide hidden for free users, visible for pro users

**Technical Considerations:**
- Integrate with existing calendar heat map logic
- Use ProUserManager to determine which heat map to display
- Modify color calculation logic based on user tier
- Ensure heat map updates when toggling between Pro/Free modes
- Consider performance implications of heat map calculations

**Priority Justification:**
High priority as this provides a clear value proposition for Pro users while still giving Free users basic visual feedback about their practice consistency.

**Implementation Details:**
- **Free User Heat Map**: Shows light blue (`#B3D9FF`) for any day with activities, white for no activities
- **Pro User Heat Map**: Full heat map with intensity levels:
  - Practice days: Light blue → Medium blue → Dark blue (based on activity count)
  - Performance days: Light green → Medium green → Dark green (based on activity count)
- **Activity Level Color Guide**:
  - Hidden (`View.GONE`) for free users - no need to show complex legend for simple heat map
  - Visible (`View.VISIBLE`) for Pro users - shows detailed legend for 6 different color/intensity levels
- **Pro Status Detection**: Uses `ProUserManager.isProUser()` to determine which heat map to display
- **Dynamic Updates**: Calendar and color guide refresh in `onResume()` when Pro status changes
- **Preserved Logic**: All existing Pro heat map logic maintained for Pro users

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/CalendarFragment.kt`
- `app/src/main/res/layout/fragment_calendar.xml`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Provides tangible differentiation between Free and Pro user experiences
- Can serve as a model for other Pro/Free feature implementations

---

### Feature #4: ✅ Disable Import from CSV for Free Users
**Status:** Implemented
**Date Requested:** 2025-07-21
**Date Implemented:** 2025-07-21
**Priority:** High
**Requested By:** Product Team

**Description:**
Restrict CSV import functionality to Pro users only. Free users will see the Import/Export Data page but the import functionality will be disabled with an upgrade prompt. Export functionality will remain available to all users to ensure they can always access their data.

**User Story:**
As a business, we want to limit advanced data management features like CSV import to Pro users so that we can provide additional value for Pro subscriptions, while as a Free user, I can still export my data but need to upgrade to Pro to import external data.

**Acceptance Criteria:**
- [x] Free users can access Import/Export Data page
- [x] Free users can still export their data to CSV (data portability)
- [x] Free users cannot import CSV files - import functionality disabled
- [x] Import button/section shows upgrade prompt for Free users
- [x] Pro users retain full import and export functionality
- [x] Clear messaging about Pro-only import feature
- [x] Import functionality updates immediately when Pro status changes

**Technical Considerations:**
- Integrate with ProUserManager to check user status
- Disable/enable import UI elements based on Pro status
- Add upgrade prompt UI for import functionality
- Maintain export functionality for all users (data portability)
- Update import screens to check Pro status before allowing import
- Handle Pro status changes during import workflow

**Priority Justification:**
High priority as this provides a valuable Pro feature that encourages upgrades while maintaining ethical data portability (users can always export their data).

**Implementation Details:**
- **Import Button State**:
  - Free users: Button text shows "Import from CSV (Pro Only)" with 0.6 alpha transparency
  - Pro users: Button text shows "Import from CSV" with full opacity
- **Import Functionality**:
  - Free users: Clicking import button shows upgrade prompt dialog instead of file picker
  - Pro users: Full import functionality as before
- **Export Functionality**: Remains unchanged and available to all users (data portability)
- **Upgrade Prompt**: Professional dialog explaining Pro feature with "Learn More" button
- **Dynamic Updates**: Button state refreshes in `onResume()` when Pro status changes
- **Pro Status Detection**: Uses `ProUserManager.isProUser()` throughout import workflow

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/importexport/ImportExportFragment.kt`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Maintains user data portability by keeping export functionality free
- Creates clear incentive for Pro upgrade with advanced data management features

---

### Feature #5: ✅ Limit Free Users to 4 Favorites
**Status:** Implemented
**Date Requested:** 2025-07-21
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Product Team

**Description:**
Implement a simple restriction where Free users cannot add new favorites if they already have 4 or more favorites. Pro users have unlimited favorites. The restriction only applies to adding new favorites - removing favorites is always allowed for all users.

**User Story:**
As a business, we want to provide a simple incentive for Pro upgrades by limiting Free users to 4 favorites, while as a Pro user, I want unlimited favorites to organize my extensive repertoire without restrictions.

**Acceptance Criteria:**
- [x] Free users cannot add new favorites if they already have 4 or more favorites
- [x] Pro users have unlimited favorites (no restrictions)
- [x] When Free user with 4+ favorites tries to add another, show upgrade prompt instead of adding
- [x] Removing favorites is always allowed for all users regardless of Pro status
- [x] Existing Free users with >4 favorites can keep them all and remove any, but cannot add new ones
- [x] Clear messaging in upgrade prompt about Pro unlimited favorites
- [x] Simple favorite count check before allowing new additions
- [x] No changes to existing toggle behavior for removing favorites

**Technical Considerations:**
- Simple count check before allowing new favorite additions
- Minimal impact on existing data flow and suggestion systems
- Only affects the "add favorite" action, not remove
- Use straightforward favorite count from repository data
- Avoid complex UI state management or return value changes to toggle methods

**Priority Justification:**
High priority as this provides a clear but non-intrusive Pro upgrade incentive. The simplified approach minimizes technical complexity while still providing business value.

**Implementation Details:**
- **Favorite Limit Logic**: Added `canAddMoreFavorites()` method to ProUserManager with FREE_USER_FAVORITE_LIMIT constant (4)
- **Pro User Behavior**: Unlimited favorites, no restrictions or changes to existing workflow
- **Free User Logic**:
  - Can always remove favorites (no restrictions on unfavoriting)
  - Can add favorites only if current count is < 4
  - If attempting to add when already at 4+, show upgrade prompt instead
- **Upgrade Prompt**: Professional dialog explaining limit with "Learn More" button for future Pro upgrade flow
- **Return Values**: toggleFavorite() methods return boolean (true = success, false = limit reached)
- **UI Feedback**: Snackbar confirmation for successful toggles, upgrade prompt for limit reached

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/utils/ProUserManager.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/favorites/FavoritesViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/favorites/FavoritesFragment.kt`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Simplified implementation avoids complex data synchronization issues
- Only affects adding favorites, not removing them
- Maintains all existing suggestion functionality

---

### Feature #6: ✅ Hide Inactive Tab for Free Users
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Product Team

**Description:**
Hide the "Inactive" tab from Free users while keeping it visible for Pro users. This provides another Pro-only feature that encourages upgrades while simplifying the interface for Free users.

**User Story:**
As a business, we want to limit advanced features like the Inactive tab to Pro users so that we can provide additional value for Pro subscriptions, while as a Free user, I have a simpler interface focused on core functionality.

**Acceptance Criteria:**
- [x] Free users cannot see the "Inactive" tab
- [x] Pro users can see and use the "Inactive" tab normally
- [x] Tab visibility updates immediately when Pro status changes
- [x] No crashes or UI issues when tab is hidden/shown
- [x] Free users still have access to all other tabs and core functionality
- [x] Clear Pro-only feature differentiation

**Technical Considerations:**
- Identify where the Inactive tab is implemented
- Use ProUserManager to determine tab visibility
- Handle dynamic tab showing/hiding based on Pro status changes
- Ensure proper UI layout when tab is hidden
- Maintain existing functionality for Pro users

**Priority Justification:**
High priority as this provides a clear Pro feature differentiation that encourages upgrades while keeping the core app functionality available to Free users.

**Implementation Details:**
- **Tab Visibility Logic**: Inactive tab only shown when `proUserManager.isProUser()` returns true
- **Adapter Count**: ViewPager adapter returns 6 items for Pro users, 5 items for Free users
- **Dynamic Updates**: Tab setup refreshed in `onResume()` to handle Pro status changes
- **Free User Experience**:
  - Shows 5 tabs: Dashboard, Calendar, Suggestions, Pieces, Timeline
  - No access to Inactive tab or AbandonedFragment
- **Pro User Experience**:
  - Shows all 6 tabs including Inactive tab
  - Full access to all functionality unchanged
- **Error Handling**: Proper position validation prevents crashes when tab is hidden

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/ViewProgressFragment.kt`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Simple implementation that cleanly hides the tab without affecting other functionality
- Tab refreshes automatically when Pro status changes during app usage

---

### Feature #7: ✅ Limit Suggestions for Free Users
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Product Team

**Description:**
Limit Free users to only 2 favorite suggestions and 2 non-favorite suggestions in both the Dashboard and Suggestions tab. Pro users continue to see the full suggestion lists (up to 4 favorites and 4 non-favorites each).

**User Story:**
As a business, we want to limit the number of practice suggestions for Free users so that we can provide additional value for Pro subscriptions, while as a Pro user, I want access to comprehensive practice suggestions to optimize my practice routine.

**Acceptance Criteria:**
- [x] Free users see maximum 1 favorite suggestion (updated from 2 by Feature #8)
- [x] Free users see maximum 2 non-favorite suggestions (instead of up to 4)
- [x] Pro users continue to see full suggestion lists (up to 4 each)
- [x] Limitation applies to both Dashboard and Suggestions tab
- [x] Suggestion quality maintained - show the most relevant suggestions within limits
- [x] Suggestion limits update immediately when Pro status changes
- [x] No crashes or UI issues when suggestions are limited

**Technical Considerations:**
- Modify DashboardViewModel suggestions logic to limit results for Free users
- Modify SuggestionsViewModel to apply same limits for Free users
- Use ProUserManager to determine suggestion limits
- Maintain existing suggestion ranking/priority algorithms
- Apply limits after suggestions are calculated but before returning to UI
- Ensure consistent behavior across both Dashboard and Suggestions tab

**Priority Justification:**
High priority as this provides valuable Pro differentiation while maintaining core suggestion functionality for Free users. Practice suggestions are a key feature that serious practitioners will want unlimited access to.

**Implementation Details:**
- **Suggestion Limits**:
  - Free users: Maximum 1 favorite suggestion + 2 non-favorite suggestions (favorite limit updated by Feature #8)
  - Pro users: Up to 4 favorite suggestions + 4 non-favorite suggestions (unchanged)
- **Dynamic Limits**: Both ViewModels check `proUserManager.isProUser()` to determine limits
- **Consistent Implementation**: Same logic applied to both DashboardViewModel and SuggestionsViewModel
- **Suggestion Quality**: Existing ranking and fallback algorithms preserved within the reduced limits
- **Real-time Updates**: Limits apply immediately when Pro status changes since ViewModels react to Pro status
- **UI Impact**: Dashboard shows fewer suggestions for Free users (3 total vs up to 8), Suggestions tab shows fewer items

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsFragment.kt`

**Implementation Notes:**
- This feature depends on Feature #1 (Pro Mode infrastructure) being implemented first
- Maintains all existing suggestion algorithm logic and quality
- Provides clear value proposition for Pro upgrade
- No UI changes needed - suggestion limits work seamlessly with existing displays
- **Status Update**: Original specification called for 2 favorite suggestions, but Feature #8 further reduced this to 1 favorite suggestion. The non-favorite limit of 2 remains as originally specified.

---

### Feature #8: ✅ Single Favorite Suggestion for Free Users
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Product Team

**Description:**
Limit Free users to only 1 favorite suggestion (instead of 2) in both the Dashboard and Suggestions tab. Free users will always see their least recently practiced/performed favorite piece as the single suggestion. If they have no favorite pieces, no favorite suggestions are shown. Pro users continue to see up to 4 favorite suggestions.

**User Story:**
As a business, we want to further limit favorite suggestions for Free users to provide even stronger Pro upgrade incentive, while as a Free user, I still get the most important favorite suggestion (my most neglected favorite piece) to maintain practice value.

**Acceptance Criteria:**
- [x] Free users see exactly 1 favorite suggestion (instead of 2)
- [x] Free users with no favorites see 0 favorite suggestions
- [x] The single favorite suggestion is always the least recently practiced/performed favorite piece
- [x] Pro users continue to see up to 4 favorite suggestions (unchanged)
- [x] Limitation applies to both Dashboard and Suggestions tab
- [x] Non-favorite suggestions remain at 2 for Free users
- [x] Suggestion limits update immediately when Pro status changes

**Technical Considerations:**
- Modify DashboardViewModel and SuggestionsViewModel favorite limits from 2 to 1 for Free users
- Maintain existing "least recently practiced" algorithm for the single suggestion
- Ensure proper handling when user has no favorites (empty list)
- Keep non-favorite suggestion limits unchanged (2 for Free, 4 for Pro)

**Priority Justification:**
High priority as this creates a stronger Pro upgrade incentive while still providing valuable practice guidance to Free users with their most neglected favorite piece.

**Implementation Details:**
- **Favorite Limit Change**: Updated from 2 to 1 favorite suggestions for Free users in both ViewModels
- **Algorithm Preserved**: Existing "least recently practiced/performed" selection logic maintained
- **Pro Users Unchanged**: Continue to see up to 4 favorite suggestions
- **Non-favorite Limits**: Unchanged at 2 for Free users, 4 for Pro users
- **Empty Handling**: When Free users have no favorites, suggestion list naturally shows 0 favorite suggestions
- **Total Suggestions**: Free users now see maximum 3 suggestions total (1 favorite + 2 non-favorite)

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`

**Implementation Notes:**
- Simple change from `4 else 2` to `4 else 1` in favorite limit logic
- Leverages existing suggestion algorithms - no complex changes needed
- Provides stronger Pro upgrade incentive while maintaining core value for Free users
- Works seamlessly with existing UI - no interface changes required

---

### Feature #9: 🔄 Remove Pro Upgrade Prompts for Free-Only Release
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Product Team

**Description:**
Remove all Pro upgrade prompts and monetization messaging for the initial Free-only release of PlayStreak. Users will experience Free tier limitations without upgrade pressure, creating a clean user experience focused on core functionality.

**User Story:**
As a user downloading the Free version of PlayStreak, I want to understand the app's limitations without being pressured to upgrade, so that I can focus on using the core features and providing authentic feedback about the app's value.

**Acceptance Criteria:**
- [ ] Import CSV prompt changed from Pro upgrade to "Coming Soon" message
- [ ] Favorites limit prompt changed from Pro upgrade to simple limitation explanation
- [ ] No "Learn More" or "Upgrade" buttons in limitation messages
- [ ] All upgrade-related messaging removed from the app
- [ ] Users still experience authentic Free tier limitations (4 favorites, 1 favorite suggestion, etc.)
- [ ] Clean, professional messaging about current app capabilities
- [ ] Pro/Free logic remains intact for future Pro launch

**Technical Considerations:**
- Modify ImportExportFragment upgrade prompt to show "Coming Soon" message
- Update favorites limit prompts in PiecesFragment and FavoritesFragment
- Remove upgrade action buttons but keep informative dialogs
- Preserve underlying Pro/Free differentiation logic for future use
- Maintain current limitation behaviors without monetization pressure

**Priority Justification:**
High priority for initial app release strategy. Allows focus on core app quality and user feedback without monetization complexity, while preserving architecture for future Pro launch.

**Implementation Approach:**
- **Import CSV**: Change "Pro Feature" dialog to "Coming Soon - Import from CSV functionality will be available in a future update"
- **Favorites Limit**: Change "Pro Feature" dialog to "Favorites Limit - This app supports up to 4 favorite pieces to help you focus on your most important repertoire"
- **Preserve Logic**: Keep all existing Pro/Free differentiation code intact for easy future conversion back to upgrade prompts

---

### Feature #10: 💡 Google Drive Functionality Coming Soon Message
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** Product Team

**Description:**
Add "Google Drive functionality coming soon" messaging to the Import/Export Data page to inform users about planned cloud storage integration for data backup and sync capabilities.

**User Story:**
As a user of PlayStreak, I want to know that Google Drive integration is planned so that I can look forward to cloud backup and sync capabilities for my practice data in future updates.

**Acceptance Criteria:**
- [ ] Import/Export Data page shows "Google Drive functionality coming soon" message
- [ ] Message is clearly visible and professionally presented
- [ ] Message doesn't interfere with existing export functionality
- [ ] Gives users expectation of future cloud storage features
- [ ] Consistent with overall app messaging style

**Technical Considerations:**
- Add informational message/section to ImportExportFragment
- Place message prominently but not intrusively on the Import/Export screen
- Consider adding icon or visual element to make message more noticeable
- Ensure message fits well with existing UI layout

**Priority Justification:**
Medium priority feature that helps set user expectations about upcoming cloud functionality while building anticipation for future updates. Supports user retention by showing ongoing development.

**Implementation Approach:**
- Add a dedicated "Coming Soon" section or card to the Import/Export screen
- Include Google Drive icon if available
- Use encouraging messaging like "Google Drive sync and backup functionality coming in a future update!"
- Position prominently but maintain focus on current export capabilities

---

### Feature #11: 💡 Purge All Data Button Testing Mode
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** Development Team

**Description:**
Add "(Testing)" label to the Purge All Data button and implement functionality to hide/show this button for production vs testing builds using BuildConfig.DEBUG. The button should be visible during development and testing but hidden in production releases, with ability to reveal it when needed for testing purposes.

**User Story:**
As a developer, I want to be able to easily reset all app data during testing while ensuring production users don't accidentally access this destructive functionality, and as a tester, I want to be able to reveal the purge button when needed for test scenarios.

**Acceptance Criteria:**
- [ ] Purge All Data button shows "(Testing)" label in the text
- [ ] Button is hidden by default in production builds
- [ ] Button can be revealed through a testing mechanism (debug menu, build flag, etc.)
- [ ] Button remains fully functional when visible
- [ ] Clear visual indication that this is a testing/development feature
- [ ] No accidental access by regular users in production
- [ ] Easy to enable for QA testing scenarios

**Technical Considerations:**
- Add "(Testing)" text to the button label
- Implement build variant or debug flag to control button visibility
- Consider adding to existing debug/testing menu if available
- Ensure button functionality remains unchanged when visible
- Use BuildConfig.DEBUG or similar mechanism for production detection
- Alternative reveal methods: long press, secret gesture, or settings toggle

**Priority Justification:**
Medium priority development tool that improves testing workflow while protecting production users from accidental data loss. Important for QA processes and development efficiency.

**Implementation Approach:**
- **Button Label**: Change to "Purge All Data (Testing)"
- **Visibility Control**: Hide in production builds using `if (BuildConfig.DEBUG)` or similar
- **Testing Access**: Provide mechanism to reveal button when needed (debug menu, long press, etc.)
- **Safety**: Maintain existing confirmation dialogs when button is used
- **Documentation**: Clear instructions for testers on how to access the button

---

### Feature #12: 💡 Remove MIT License
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Legal/Business Team

**Description:**
Remove the MIT License from the PlayStreak app and replace it with a commercial "All Rights Reserved" license to prepare for commercial release. This includes removing MIT license files, replacing with commercial copyright, and updating any license references in the app or documentation.

**User Story:**
As a business preparing for commercial app release, I want to replace the open-source MIT License with a commercial "All Rights Reserved" license so that the app can be properly licensed as a commercial product with full ownership protection.

**Acceptance Criteria:**
- [ ] Remove MIT LICENSE file from project root
- [ ] Add new COPYRIGHT file with "All Rights Reserved" commercial license
- [ ] Remove MIT license headers from all source files
- [ ] Add copyright notice to app About/Settings screen
- [ ] Update documentation files to reference commercial licensing
- [ ] Ensure no remaining MIT License artifacts in the codebase
- [ ] Update any build scripts or configuration that reference the license
- [ ] Professional commercial licensing presentation

**Technical Considerations:**
- Search entire codebase for "MIT", "License", or license headers
- Remove or update LICENSE files in project root
- Check app's About/Settings screens for license display
- Review build.gradle files for license-related configurations
- Ensure third-party library licenses are handled separately
- Update README or other documentation that references licensing

**Priority Justification:**
High priority for commercial release preparation. MIT License removal is essential before app store publication to avoid legal complications and establish proper commercial licensing.

**Implementation Approach:**
- **License Replacement**: Delete MIT LICENSE file and add COPYRIGHT file with commercial license
- **Commercial License Text**: "Copyright (c) 2025 [Your Name/Company]. All rights reserved. This software and associated documentation files are proprietary and confidential. Unauthorized copying, distribution, or modification is strictly prohibited."
- **Source Headers**: Remove MIT headers from .kt, .java files (optional commercial headers)
- **UI Integration**: Add copyright notice to About/Settings screen
- **Documentation**: Update README and other docs to reference commercial licensing
- **Build Configuration**: Update any license-related build configurations

---

### Feature #13: 💡 Hide Switch to Pro/Free Button Using BuildConfig.DEBUG
**Status:** 🔍 Verifying
**Date Requested:** 2025-07-22
**Priority:** High
**Requested By:** Development Team

**Description:**
Hide the Switch to Pro/Free button in production builds using BuildConfig.DEBUG while keeping it visible in debug builds for testing. This ensures production users don't see testing functionality while maintaining developer/tester access to Pro status switching.

**User Story:**
As a production user, I should not see testing buttons that allow switching between Pro and Free modes, while as a developer or tester, I want access to the Pro/Free switch button in debug builds to test different user experiences.

**Acceptance Criteria:**
- [ ] Switch to Pro/Free button hidden in production/release builds
- [ ] Switch to Pro/Free button visible in debug builds
- [ ] Uses BuildConfig.DEBUG to control visibility
- [ ] Button functionality unchanged when visible
- [ ] No impact on other Pro/Free differentiation logic
- [ ] Clean production UI without testing artifacts
- [ ] Easy testing access in debug builds

**Technical Considerations:**
- Locate Switch to Pro/Free button in Settings or main interface
- Wrap button visibility with `if (BuildConfig.DEBUG)` condition
- Ensure button container/layout adjusts properly when hidden
- Maintain existing button functionality when visible
- Consider using `View.GONE` vs `View.INVISIBLE` for clean layout
- Test both debug and release build variants

**Priority Justification:**
High priority for production release cleanliness. Testing buttons should not be visible to production users as they create confusion and expose internal functionality not intended for end users.

**Implementation Approach:**
- **Visibility Control**: Use `button.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE`
- **Location**: Find Switch to Pro/Free button in Settings screen or similar
- **Layout**: Ensure parent layout handles hidden button gracefully
- **Testing**: Verify button works in debug builds and is hidden in release builds
- **Clean UI**: Production users see polished interface without testing elements

---

### Feature #14: 💡 Build Variant Default Pro Status
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Critical
**Requested By:** Business Team

**Description:**
Set different default Pro status based on build variant: Free mode default for release builds (production users), Pro mode default for debug builds (developers/testers). This ensures production users get the intended Free experience while developers can easily test Pro features.

**User Story:**
As a production user installing PlayStreak, I should automatically start with the Free experience, while as a developer or tester using debug builds, I should start with Pro access to easily test all features without manual switching.

**Acceptance Criteria:**
- [ ] Release builds: New installations default to Free mode (isProUser = false)
- [ ] Debug builds: New installations default to Pro mode (isProUser = true)
- [ ] ProUserManager initializes with different defaults based on BuildConfig.DEBUG
- [ ] Release builds: All Free tier limitations active from first app launch
- [ ] Debug builds: All Pro features accessible from first app launch
- [ ] Consistent experience within each build variant
- [ ] Fresh installs show appropriate UI based on build variant (release=5 tabs, debug=6 tabs)
- [ ] Existing users maintain their current Pro/Free status regardless of build variant

**Technical Considerations:**
- Update ProUserManager initialization to use BuildConfig.DEBUG for default value
- Modify SharedPreferences default behavior to return build-variant-specific defaults
- Verify that all Pro/Free checks handle both debug and release defaults correctly
- Test fresh installation on both debug and release builds
- Ensure existing user data is not affected by changes (only impacts new installs)
- Consider that debug users may need to manually switch to Free for testing

**Priority Justification:**
Critical priority for both production release and development efficiency. Production users must start with Free experience for proper evaluation and monetization, while developers need immediate Pro access for efficient feature testing.

**Implementation Approach:**
- **Default Logic**: Use `BuildConfig.DEBUG ? true : false` as default Pro status for new users
- **Location**: Update ProUserManager.isProUser() method to return build-variant-specific defaults
- **Existing Users**: Preserve current Pro/Free status in SharedPreferences
- **Testing**: Verify debug builds start Pro, release builds start Free
- **ProUserManager Default**: Ensure `isProUser()` returns `false` when no preference exists
- **SharedPreferences**: Use `getBoolean(KEY_IS_PRO_USER, false)` with explicit false default
- **Fresh Install Testing**: Verify new installations show Free tier behavior
- **User Migration**: Ensure existing users (if any) maintain current status
- **Consistency Check**: All Pro/Free logic should assume Free by default

---

### Feature #15: 💡 Add "Add Piece (#)" Button to Settings Page
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** UI/UX Team

**Description:**
Add an "Add Piece" button to the Settings page positioned right after the "Add Activity" button. The button should display the current count of pieces in parentheses (e.g., "Add Piece (12)") to provide users with quick access to piece management and visibility into their repertoire size.

**User Story:**
As a user, I want quick access to add new pieces from the Settings page and see at a glance how many pieces are in my repertoire, so that I can easily manage my practice library and understand the scope of my collection.

**Acceptance Criteria:**
- [ ] "Add Piece" button added to Settings page after "Add Activity" button
- [ ] Button shows current piece count in parentheses format: "Add Piece (12)"
- [ ] Piece count updates dynamically when pieces are added/removed
- [ ] Button navigates to Add Piece screen/functionality
- [ ] Consistent styling with existing Settings page buttons
- [ ] Count reflects actual number of pieces in user's library
- [ ] Real-time count updates when returning to Settings page

**Technical Considerations:**
- Add button to Settings page layout after Add Activity button
- Query repository to get current piece count
- Use LiveData or similar to observe piece count changes
- Navigate to appropriate Add Piece screen when clicked
- Ensure count updates when user returns from adding/removing pieces
- Consider performance impact of frequent count queries
- Handle zero pieces case gracefully ("Add Piece (0)")

**Priority Justification:**
Medium priority UI enhancement that improves user experience by providing quick access to piece management and useful repertoire size information. Helps users understand and manage their practice library more effectively.

**Implementation Approach:**
- **Button Placement**: Add after existing Add Activity button in Settings layout
- **Count Query**: Use repository to get piece count with `repository.getAllPiecesAndTechniques().count { it.type == ItemType.PIECE }`
- **Dynamic Updates**: Observe piece data changes with LiveData
- **Navigation**: Navigate to Add Piece screen (create if doesn't exist)
- **Styling**: Match existing Settings page button design
- **Performance**: Cache count or use efficient query for real-time updates

---

### Feature #16: 💡 Set Application Limits for Activities and Pieces
**Status:** Requested
**Date Requested:** 2025-07-22
**Priority:** High
**Requested By:** Engineering/QA Team

**Description:**
Investigate and establish reasonable limits for the number of activities that can be created per day and the total number of pieces the app can hold. Implement these limits with proper validation and user feedback to ensure stable app performance and prevent edge cases that could cause crashes or poor performance.

**User Story:**
As a user, I want the app to function reliably even with extensive use, and as a developer, I want to ensure the app has tested, guaranteed performance limits rather than unknown breaking points that could cause crashes or poor user experience.

**Acceptance Criteria:**
- [ ] Research and determine reasonable daily activity limits (e.g., 50-100 activities per day)
- [ ] Research and determine reasonable total piece limits (e.g., 500-1000 pieces)
- [ ] Test app performance at determined limits to ensure stability
- [ ] Implement validation to prevent exceeding limits
- [ ] Provide clear user feedback when approaching/hitting limits
- [ ] Graceful handling when limits are reached (no crashes)
- [ ] Document limits for user support, troubleshooting, and testing documentation
- [ ] Consider different limits for Free vs Pro users if applicable

**Technical Considerations:**
- Performance testing with large datasets (database queries, UI rendering, memory usage)
- Database performance with thousands of activities and hundreds of pieces
- UI responsiveness with large lists (RecyclerView performance, pagination)
- Memory usage analysis under heavy data loads
- Export/import performance with maximum data sets
- Search and filtering performance with large datasets
- Calendar rendering with many activities per day

**Priority Justification:**
High priority for app stability and user trust. Having undefined limits creates risk of crashes, poor performance, and bad user experience. Professional apps should have tested, documented limits to ensure reliability.

**Implementation Approach:**
- **Research Phase**: Performance testing with synthetic large datasets
- **Limit Determination**: Based on testing results and user research
- **Validation**: Add checks when creating activities/pieces
- **User Feedback**: Clear messages like "Daily activity limit reached (50/50)"
- **Graceful Degradation**: Disable add buttons/show alternative options when limits reached
- **Documentation**: Include limits in user guide, support materials, and testing documentation
- **Monitoring**: Track usage patterns to validate limit appropriateness

**Suggested Research Areas:**
- Database performance with 10,000+ activities and 1,000+ pieces
- Calendar view rendering with 50+ activities per day
- Suggestions algorithm performance with large datasets
- Export file size and generation time limits
- Memory usage patterns under maximum load

---

### Feature #17: 💡 Disable Import From CSV Button for Free Users
**Status:** ✅ Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Business Team

**Description:**
Disable the Import From CSV button functionality for Free users while keeping it enabled for Pro users. This creates a clear Pro/Free differentiation for advanced data management features while maintaining export functionality for all users to ensure data portability.

**User Story:**
As a Free user, I should not have access to CSV import functionality, while as a Pro user, I want full data import capabilities so that I can manage my practice data comprehensively.

**Acceptance Criteria:**
- [ ] Free users: Import From CSV button disabled/grayed out
- [ ] Pro users: Import From CSV button fully functional
- [ ] Button visual state reflects availability (enabled/disabled styling)
- [ ] Clicking disabled button shows appropriate message (not "Coming Soon")
- [ ] Export functionality remains available to all users
- [ ] Pro/Free status changes update button state immediately
- [ ] Consistent with other Pro/Free feature differentiations

**Technical Considerations:**
- Update ImportExportFragment to check Pro status for import button state
- Modify button click behavior based on Pro/Free status
- Ensure button styling reflects enabled/disabled state (alpha, colors)
- Replace "Coming Soon" message with Pro feature prompt for consistency
- Test button state changes when Pro status toggles
- Maintain export functionality for all users (data portability)

**Priority Justification:**
High priority for establishing clear Pro/Free feature boundaries. Import functionality is an advanced data management feature that should be restricted to Pro users while ensuring all users can export their data.

**Implementation Approach:**
- **Button State**: Use `button.isEnabled = proUserManager.isProUser()` for import button
- **Visual Styling**: Apply appropriate alpha/styling for disabled state
- **Click Behavior**: Show Pro upgrade prompt when disabled button is clicked
- **Dynamic Updates**: Update button state when Pro status changes in onResume()
- **User Experience**: Clear visual distinction between enabled/disabled states

---

### Feature #18: ✅ Add Activity from Suggestions or Pieces Tab
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** UX Team

**Description:**
Add a quick "Add Activity" feature directly from the Suggestions and Pieces tabs. Each piece listed should have a small "+" icon that allows users to quickly log an activity for that piece without navigating through the full Add Activity flow and without needing to select the piece name again.

**User Story:**
As a user viewing my suggestions or pieces list, I want to quickly add an activity for a specific piece by tapping a "+" icon, so that I can efficiently log practice sessions without navigating through multiple screens to select the piece name.

**Acceptance Criteria:**
- [x] Add "+" icon to each piece in Suggestions tab (Pro users only)
- [x] Add "+" icon to each piece in Pieces tab (Pro users only)
- [x] Clicking "+" opens simplified Add Activity dialog
- [x] Dialog pre-fills piece name (user cannot change it)
- [x] Dialog allows selection of Practice vs Performance
- [x] Dialog shows proper level options matching regular Add Activity flow
- [x] Performance activities show only 3 levels with performance-specific descriptions
- [x] Practice activities show 4 levels with practice-specific descriptions
- [x] Performance activities include performance type selection (Online/Live)
- [x] Dialog has Add Activity and Cancel buttons
- [x] Successful save closes dialog and returns to original tab
- [x] Activity will appear in timeline/dashboard after saving
- [x] Icon is visually clear but doesn't dominate the list item
- [x] Feature restricted to Pro users only

**Technical Considerations:**
- Add "+" button/icon to suggestion item layout and pieces item layout
- Create simplified Add Activity dialog fragment or modal
- Pre-populate piece ID and name in the dialog
- Use existing activity creation logic but skip piece selection step
- Handle navigation back to originating tab after save
- Ensure icon styling is consistent and accessible
- Consider icon placement (end of row, overlay, etc.)

**Priority Justification:**
Medium priority UX enhancement that significantly improves workflow efficiency. Reduces friction for the most common user action (logging practice) by eliminating navigation overhead when users already know which piece they want to practice.

**Implementation Details:**

**UI Components Created:**
- **QuickAddActivityDialogFragment.kt**: Simplified dialog for quick activity addition
- **QuickAddActivityViewModel.kt**: ViewModel to handle activity creation logic
- **dialog_quick_add_activity.xml**: Layout for the dialog with spinners for activity type and level
- **ic_add.xml**: Vector drawable for the "+" icon

**Layout Modifications:**
- **item_suggestion.xml**: Added clickable "+" icon next to existing favorite star
- **item_piece_stats.xml**: Added clickable "+" icon between piece name and favorite star

**Adapter Updates:**
- **SuggestionsAdapter.kt**: Added onAddActivityClick callback, updated ViewHolder to handle + icon clicks
- **PiecesAdapter.kt**: Added onAddActivityClick callback, updated ViewHolder to handle + icon clicks

**Fragment Integration:**
- **SuggestionsFragment.kt**: Shows QuickAddActivityDialogFragment when + icon clicked
- **PiecesFragment.kt**: Shows QuickAddActivityDialogFragment when + icon clicked

**Dialog Features:**
- Pre-filled piece name (read-only display)
- Activity type spinner (Practice/Performance)
- Dynamic level spinner that matches regular Add Activity flow:
  - **Practice**: Level 1-4 with specific descriptions (Essentials, Incomplete, Complete with Review, Perfect Complete)
  - **Performance**: Level 1-3 with specific descriptions (Failed, Unsatisfactory, Satisfactory)
- Performance type spinner (Online Performance/Live Performance) - only visible for Performance activities
- Default values: Practice type, 30 minutes for practice activities, "online" performance type
- Toast notifications for success/failure
- Automatic dialog dismissal on success
- **Pro User Restriction**: + icons only visible to Pro users

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsAdapter.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesAdapter.kt`
- `app/src/main/res/layout/item_suggestion.xml`
- `app/src/main/res/layout/item_piece_stats.xml`

**Files Created:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/QuickAddActivityDialogFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/QuickAddActivityViewModel.kt`
- `app/src/main/res/layout/dialog_quick_add_activity.xml`
- `app/src/main/res/drawable/ic_add.xml`

**Implementation Notes:**

**Version 2 Updates (Based on User Feedback):**
- **Pro User Restriction Added**: + icons are now only visible to Pro users, providing additional Pro feature differentiation
- **Level System Corrected**: Updated from generic 1-10 levels to match exact level descriptions from regular Add Activity flow:
  - Practice: 4 levels with meaningful descriptions (Essentials, Incomplete, Complete with Review, Perfect Complete)
  - Performance: 3 levels with performance-specific descriptions (Failed, Unsatisfactory, Satisfactory)
- **Performance Type Integration**: Added performance type selection (Online/Live) that appears only for Performance activities
- **Data Consistency**: Ensures QuickAddActivityDialog creates activities with identical structure to regular Add Activity flow

**Implementation Approach:**
- **UI Design**: Add small "+" icon (24dp) to right side of each list item
- **Pro Restriction**: Use ProUserManager to control + icon visibility
- **Dialog Creation**: Create simplified AddActivityDialog with pre-filled piece info and dynamic level options
- **Navigation Flow**: Dialog → Activity Type → Dynamic Level Options → Performance Type (if needed) → Save → Return to original tab
- **Icon Styling**: Use Material Design add icon with subtle styling
- **Level Logic**: Mirror SelectLevelFragment.setupLevelOptions() for consistency
- **Integration**: Leverage existing activity creation repository methods
- **User Experience**: Fast, one-tap access to activity logging from browsing context

---

### Feature #19: ✅ Add Activity from Inactive Tab
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Feedback

**Description:**
Add a quick "Add Activity" feature to the Inactive tab that works like the existing quick add functionality in Suggestions and Pieces tabs. Each piece listed in the Inactive tab should have a small "+" icon that allows users to quickly log an activity for that piece.

**User Story:**
As a Pro user viewing my inactive pieces, I want to quickly add an activity for a specific piece by tapping a "+" icon, so that I can efficiently resume practice on neglected pieces without navigating through multiple screens to select the piece name.

**Acceptance Criteria:**
- [ ] Add "+" icon to each piece in Inactive tab (Pro users only)
- [ ] Clicking "+" opens the same QuickAddActivityDialog used in Suggestions/Pieces tabs
- [ ] Dialog pre-fills piece name (user cannot change it)
- [ ] Dialog allows selection of Practice vs Performance
- [ ] Dialog shows proper level options matching regular Add Activity flow
- [ ] Performance activities show only 3 levels with performance-specific descriptions
- [ ] Practice activities show 4 levels with practice-specific descriptions
- [ ] Performance activities include performance type selection (Online/Live)
- [ ] Dialog has Add Activity and Cancel buttons
- [ ] Successful save closes dialog and returns to Inactive tab
- [ ] Activity will appear in timeline/dashboard after saving
- [ ] After logging activity, piece should eventually move out of Inactive tab
- [ ] Icon is visually clear but doesn't dominate the list item
- [ ] Feature restricted to Pro users only (consistent with Inactive tab access)

**Technical Considerations:**
- Add "+" button/icon to inactive piece item layout
- Reuse existing QuickAddActivityDialogFragment from Feature #18
- Add onAddActivityClick callback to InactiveAdapter
- Update InactiveFragment to handle dialog presentation
- Ensure icon styling matches Suggestions and Pieces tabs
- Consider that pieces may disappear from Inactive list after activity is added
- Test that Inactive tab refreshes properly after activity creation

**Priority Justification:**
Medium priority UX enhancement that extends the successful quick-add pattern from Feature #18 to the Inactive tab. Provides consistent user experience across all piece-viewing tabs and makes it easier to resume practice on neglected pieces, which directly supports the app's core goal of consistent practice habits.

**Implementation Approach:**
- **Reuse Components**: Leverage existing QuickAddActivityDialogFragment and ViewModel
- **UI Consistency**: Use same "+" icon and positioning as Suggestions/Pieces tabs
- **Adapter Pattern**: Follow same pattern as SuggestionsAdapter and PiecesAdapter
- **Fragment Integration**: Add dialog handling to InactiveFragment
- **Pro Restriction**: Maintain consistency with other Pro-only quick-add features
- **Data Flow**: Ensure Inactive list updates properly when pieces become active again

**Implementation Details:**

**UI Components Modified:**
- **item_abandoned.xml**: Added addActivityIcon ImageView with + icon, positioned between piece name and days badge
- Icon styled consistently with other tabs (24dp size, selectableItemBackgroundBorderless background)

**Adapter Updates:**
- **AbandonedAdapter.kt**: Added constructor parameters for onAddActivityClick callback and ProUserManager
- Updated ViewHolder to handle + icon clicks and Pro user visibility control
- Icon visibility controlled by `proUserManager.isProUser()` - visible for Pro, gone for Free users

**Fragment Integration:**
- **AbandonedFragment.kt**: Updated to create adapter with callback and ProUserManager instance
- Added `showQuickAddActivityDialog()` method that reuses existing QuickAddActivityDialogFragment
- Dialog receives abandonedItem.piece.id and abandonedItem.piece.name for pre-filling

**Dialog Reuse:**
- Leverages existing QuickAddActivityDialogFragment and QuickAddActivityViewModel from Feature #18
- No new dialog components needed - maintains UI consistency across all tabs

**Pro User Restriction:**
- + icons only visible when `ProUserManager.getInstance(context).isProUser()` returns true
- Consistent with Inactive tab being Pro-only feature
- Maintains feature differentiation between Free and Pro users

**Data Flow:**
- User clicks + icon → showQuickAddActivityDialog() → QuickAddActivityDialogFragment
- Dialog pre-fills piece information → User selects activity details → Activity saved
- After activity creation, piece may naturally move out of Inactive tab on next data refresh

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/AbandonedFragment.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/AbandonedAdapter.kt`
- `app/src/main/res/layout/item_abandoned.xml`

**Implementation Notes:**
- Feature extends successful quick-add pattern from Feature #18 to Inactive/Abandoned tab
- Reuses existing QuickAddActivityDialogFragment for consistency and maintainability
- Maintains Pro-only restriction consistent with Inactive tab visibility
- After adding activity, pieces naturally filter out of Inactive tab on next refresh (30+ day rule)
- UI styling matches Suggestions and Pieces tabs for consistent user experience

---

### Feature #20: ✅ Exclude Today's Practiced Favorites from Suggestions
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Feedback

**Description:**
Modify the favorite suggestions logic to exclude any favorite piece that has already been practiced or performed today. Currently, favorites are suggested if they haven't been practiced in 2+ days, but this should be enhanced to never suggest favorites that have been practiced today, regardless of the 2-day rule.

**User Story:**
As a user who has already practiced a favorite piece today, I don't want to see that piece in my suggestions list, so that my suggestions focus on pieces that still need attention today and don't show me redundant recommendations.

**Acceptance Criteria:**
- [ ] Favorite pieces practiced or performed today are excluded from suggestions
- [ ] Applies to both primary suggestions (2+ days rule) and fallback suggestions
- [ ] "Today" is defined as current calendar day (midnight to midnight)
- [ ] Both PRACTICE and PERFORMANCE activities count as "practiced today"
- [ ] Suggestion logic still follows existing 2+ days rule, but adds today exclusion
- [ ] Fallback logic (least recently practiced) also excludes today's activities
- [ ] No impact on non-favorite suggestion logic
- [ ] Pro and Free user limits remain unchanged
- [ ] Clear suggestion reasons still displayed for remaining suggestions

**Technical Considerations:**
- Update SuggestionsViewModel favorite suggestion logic
- Add today timestamp calculation (start of current day)
- Filter out pieces with activities >= today timestamp in both primary and fallback logic
- Ensure timezone handling is consistent with existing date calculations
- Update both DashboardViewModel and SuggestionsViewModel for consistency
- Consider edge case where user has practiced all favorites today (empty favorites list)
- Maintain existing sorting and limiting logic for remaining favorites

**Priority Justification:**
Medium priority UX enhancement that prevents redundant suggestions and helps users focus on pieces that actually need attention. Improves suggestion quality by avoiding pieces already practiced today, making the suggestion system more intelligent and user-friendly.

**Implementation Approach:**
- **Today Calculation**: Add `startOfToday` timestamp calculation using current date at 00:00:00
- **Primary Filter**: Update favorite selection to exclude pieces with `lastActivityDate >= startOfToday`
- **Fallback Filter**: Apply same filter to fallback favorites selection
- **Logic Flow**:
  1. Calculate `startOfToday = todayMidnight timestamp`
  2. For each favorite: if `lastActivityDate >= startOfToday` → exclude
  3. Apply existing 2+ days rule to remaining favorites
  4. Apply fallback logic to remaining favorites if needed
- **Consistency**: Update both SuggestionsViewModel and DashboardViewModel
- **Edge Cases**: Handle scenario where all favorites practiced today (may result in 0 favorite suggestions)

**Implementation Details:**

**Today Calculation:**
- Added `startOfToday` timestamp calculation using `Calendar.getInstance()` set to midnight (00:00:00)
- Consistent implementation across both ViewModels using same calendar logic

**Primary Suggestion Filter Enhancement:**
- **SuggestionsViewModel**: Updated favorite condition from `(lastActivityDate == null || lastActivityDate < twoDaysAgo)`
- **Enhanced to**: `(lastActivityDate == null || lastActivityDate < twoDaysAgo) && (lastActivityDate == null || lastActivityDate < startOfToday)`
- Same logic applied to DashboardViewModel for consistency

**Fallback Suggestion Filter Enhancement:**
- Added explicit exclusion in fallback favorites mapping: `if (lastActivityDate != null && lastActivityDate >= startOfToday) return@mapNotNull null`
- Applies to both SuggestionsViewModel and DashboardViewModel fallback logic
- Ensures pieces practiced today are excluded from all suggestion paths

**Logic Flow:**
1. Calculate `startOfToday` as current date at 00:00:00 timestamp
2. For each favorite piece: if practiced today (`lastActivityDate >= startOfToday`) → exclude from suggestions
3. Apply existing 2+ day rule to remaining (non-today) favorites
4. Apply same today exclusion to fallback favorites if primary suggestions insufficient
5. Maintain existing Pro/Free limits and sorting logic

**Edge Case Handling:**
- If all favorites practiced today: Results in 0 favorite suggestions (expected behavior)
- Never-practiced pieces (`lastActivityDate == null`): Still eligible for suggestions
- Maintains existing suggestion reasons and display logic

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`

**Implementation Notes:**
- Feature enhances suggestion intelligence by preventing redundant recommendations
- Maintains all existing 2+ day logic while adding today exclusion as prerequisite filter
- Both primary and fallback suggestion paths now exclude today's practiced favorites
- May result in fewer suggestions on active practice days, but significantly improves suggestion relevance
- Helps users focus practice time on pieces that haven't been touched today
- Consistent implementation across Dashboard and Suggestions tab ensures uniform user experience

---

### Feature #21: ✅ Enhanced Streak Emoji Progression
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Experience Team

**Description:**
Enhance the streak display emoji system to provide better visual progression and motivation for different milestone achievements. Currently, the fire emoji (🔥) appears at 6+ days. This should be changed to a more graduated system with multiple emoji stages that align with PlayStreak's musical theme.

**User Story:**
As a user building my practice streak, I want to see visual progress indicators that celebrate different milestone achievements, so that I feel motivated to continue practicing and can see my progress through meaningful emoji rewards that connect to PlayStreak's musical theme.

**Acceptance Criteria:**
- [ ] 0-2 days: No emoji (same as current)
- [ ] 3-4 days: Single music note emoji 🎵 (same emoji used elsewhere in PlayStreak)
- [ ] 5-6 days: Double music note emoji 🎵🎵 (progression within musical theme)
- [ ] 7-13 days: Single fire emoji 🔥 (upgraded from music notes)
- [ ] 14+ days: Triple fire emoji 🔥🔥🔥 (maximum achievement level)
- [ ] Emoji appears after the day count: "3 days 🎵", "5 days 🎵🎵", "7 days 🔥", "14 days 🔥🔥🔥"
- [ ] Proper spacing between day count and emoji
- [ ] No other display logic changes (singular/plural "day"/"days" remains the same)
- [ ] Emoji progression creates sense of achievement and motivation

**Technical Considerations:**
- Update DashboardFragment.kt streak display logic
- Modify the conditional emoji logic from single threshold (6+ days) to multiple thresholds
- Ensure music note emoji 🎵 matches the same emoji used elsewhere in the app
- Test emoji display across different Android versions and devices
- Consider emoji rendering consistency across different font systems
- Maintain existing streak calculation logic (only change display)

**Priority Justification:**
Medium priority UX enhancement that improves user motivation and engagement through better visual feedback. The graduated emoji system provides more frequent positive reinforcement and creates stronger psychological incentive to maintain practice streaks.

**Implementation Approach:**
- **Emoji Logic**: Replace single `if (streak >= 6)` condition with multiple conditions:
  ```kotlin
  val emojiSuffix = when {
      streak >= 14 -> " 🔥🔥🔥"
      streak >= 7 -> " 🔥"
      streak >= 5 -> " 🎵🎵"
      streak >= 3 -> " 🎵"
      else -> ""
  }
  binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}$emojiSuffix"
  ```
- **Thresholds**: 3+ days (🎵), 5+ days (🎵🎵), 7+ days (🔥), 14+ days (🔥🔥🔥)
- **Musical Theme**: Music note emoji connects to PlayStreak branding and musical context
- **Progression**: Creates clear achievement levels with appropriate reward escalation
- **Testing**: Verify emoji rendering on various Android devices and OS versions

**Implementation Details:**

**Code Changes:**
- **DashboardFragment.kt**: Replaced simple if/else emoji logic with comprehensive when expression
- **Old Logic**: Single threshold at 6+ days for fire emoji
- **New Logic**: Four-tier progression system with multiple emoji milestones

**Emoji Progression Implementation:**
```kotlin
val emojiSuffix = when {
    streak >= 14 -> " 🔥🔥🔥"  // Triple fire for elite 14+ day streaks
    streak >= 7 -> " 🔥"        // Single fire for solid 7+ day streaks
    streak >= 5 -> " 🎵🎵"      // Double music note for 5+ day momentum
    streak >= 3 -> " 🎵"        // Single music note for 3+ day achievement
    else -> ""                  // No emoji for 0-2 days
}
binding.currentStreakText.text = "$streak day${if (streak != 1) "s" else ""}$emojiSuffix"
```

**Display Examples:**
- **"0 days"** - No emoji, encouraging start
- **"3 days 🎵"** - First musical milestone reward
- **"5 days 🎵🎵"** - Building momentum with double music notes
- **"7 days 🔥"** - Transition to "hot streak" territory
- **"14 days 🔥🔥🔥"** - Ultimate elite status achievement

**User Experience Impact:**
- **More Frequent Rewards**: Users get first emoji at day 3 instead of day 6
- **Musical Branding**: Music note emoji (🎵) reinforces PlayStreak's musical identity
- **Progressive Motivation**: Four distinct achievement levels create psychological incentive
- **Elite Recognition**: Triple fire emoji provides spectacular reward for 14+ day dedication

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt`

**Implementation Notes:**
- Feature enhances existing streak display without changing underlying calculation logic
- Music note emoji (🎵) reinforces PlayStreak's musical identity and branding throughout early stages
- Four-tier system provides balanced progression: early achievement (3 days), building momentum (5 days), solid streak (7 days), impressive dedication (14+ days)
- Double music note progression creates satisfying intermediate reward before transitioning to fire emojis
- Triple fire emoji (🔥🔥🔥) provides spectacular ultimate visual reward for most dedicated users
- Maintains all existing text formatting and singular/plural day handling
- Creates stronger psychological motivation through more frequent positive reinforcement

---

### Feature #22: ✅ Add PieceType to CSV Export/Import
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Data Integrity Team

**Description:**
Add the PieceType field (PIECE or TECHNIQUE) to CSV export and import functionality to preserve complete piece information during data transfers. Currently, piece type information is lost during export/import cycles, requiring the system to guess whether items like "C Major Scale" should be classified as PIECE or TECHNIQUE during import.

**User Story:**
As a user who exports and imports my practice data, I want the piece type (PIECE or TECHNIQUE) to be preserved in CSV files, so that my data maintains complete integrity without losing the distinction between musical pieces and technical exercises during import.

**Acceptance Criteria:**
- [ ] CSV export includes PieceType column after Piece column
- [ ] PieceType values are exported as "PIECE" or "TECHNIQUE" (enum string values)
- [ ] CSV import validates and parses PieceType field correctly
- [ ] Import creates pieces with correct type classification
- [ ] Enhanced CSV header: `DateTime,Length,ActivityType,Piece,PieceType,Level,PerformanceType,Notes`
- [ ] Backward compatibility: Import still works with old CSV format (falls back to type detection)
- [ ] Export preserves all existing functionality while adding new field
- [ ] Import validation includes PieceType field validation with clear error messages
- [ ] Documentation updated to reflect new CSV format

**Technical Considerations:**
- Update CsvHandler.exportActivitiesToCsv to include piece.type field
- Update CSV header constants to include HEADER_PIECE_TYPE
- Modify import validation to handle both old and new CSV formats
- Update importActivitiesFromCsv to parse and validate PieceType field
- Ensure ImportedActivity data class includes pieceType field
- Add validation for PieceType enum values during import
- Maintain backward compatibility with existing CSV files
- Update error messages to include PieceType validation failures

**Priority Justification:**
High priority data integrity fix that prevents data loss during export/import cycles. Currently, users lose piece type classification when exporting and re-importing data, requiring manual re-classification or relying on imperfect keyword detection. This is a fundamental data preservation issue that affects the reliability of the backup/restore functionality.

**Implementation Approach:**
- **Export Enhancement**: Add piece.type.toString() to CSV row data
  ```kotlin
  val row = listOf(
      datetime,
      activity.minutes.toString(),
      activity.activityType.toString(),
      TextNormalizer.normalizePieceName(piece.name),
      piece.type.toString(), // New field
      activity.level.toString(),
      activity.performanceType,
      TextNormalizer.normalizeUserInput(activity.notes)
  )
  ```
- **Header Update**: Add HEADER_PIECE_TYPE constant and include in header array
- **Import Enhancement**: Parse PieceType field with validation
- **Backward Compatibility**: Check CSV column count to handle old vs new format
- **Data Class Update**: Add pieceType field to ImportedActivity
- **Validation**: Ensure PieceType is valid ItemType enum value

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/utils/CsvHandler.kt`

**Implementation Notes:**
- Feature addresses critical data integrity issue where piece type information is lost during export/import
- Maintains backward compatibility with existing CSV files through format detection
- Enhances the reliability of PlayStreak's backup and restore functionality
- Prevents users from having to manually re-classify pieces after import
- Eliminates reliance on imperfect keyword-based type detection during import

---

### Feature #23: ✅ Include Techniques in Pieces Tab with Emoji Indicator
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Experience Team

**Description:**
Modify the Pieces tab to display both pieces AND techniques in a unified view, with a visual emoji indicator to distinguish techniques from regular pieces. Currently, the Pieces tab only shows items of type PIECE, but users should be able to see and manage their techniques (scales, exercises, etc.) in the same interface.

**User Story:**
As a user managing my practice repertoire, I want to see both my pieces and techniques in the Pieces tab, so that I have a complete view of all my practice materials in one place, with clear visual distinction between pieces and technical exercises.

**Acceptance Criteria:**
- [ ] Pieces tab displays both PIECE and TECHNIQUE items in the same list
- [ ] Techniques show a visual emoji indicator next to their title
- [ ] Pieces continue to display without technique emoji (maintain current appearance)
- [ ] Technique emoji should be consistent with PlayStreak's visual theme
- [ ] Sort functionality works correctly with mixed piece/technique list
- [ ] Search/filter functionality works with both types
- [ ] All existing functionality (favorites, quick-add, navigation) works for both types
- [ ] Tab title remains "Pieces" but content includes techniques
- [ ] Techniques are visually distinguishable but integrated seamlessly
- [ ] Activity counts and last practice dates work for both types

**Technical Considerations:**
- Update PiecesViewModel to query both PIECE and TECHNIQUE types instead of filtering to PIECE only
- Modify piece list item layout or adapter to conditionally display technique emoji
- Determine appropriate emoji for techniques (🎼, 🎹, ⚙️, or other music-related emoji)
- Ensure sorting algorithms work correctly with mixed item types
- Update any hardcoded filters that assume only PIECE type in Pieces tab
- Consider whether techniques should be grouped separately or fully integrated
- Maintain backward compatibility with existing piece-focused functionality

**Priority Justification:**
Medium priority UX enhancement that provides users with a more complete and unified view of their practice materials. Currently, techniques are somewhat hidden or less accessible, but they are an important part of practice routines. This improves discoverability and management of technical exercises.

**Implementation Approach:**
- **ViewModel Update**: Change filter from `.filter { it.type == ItemType.PIECE }` to include both types
- **Visual Indicator**: Add conditional emoji display in adapter:
  ```kotlin
  // In PiecesAdapter ViewHolder
  val displayName = if (piece.type == ItemType.TECHNIQUE) {
      "🎼 ${piece.name}"  // Or chosen technique emoji
  } else {
      piece.name
  }
  binding.pieceNameText.text = displayName
  ```
- **Emoji Selection**: Choose from music-related options:
  - 🎼 (musical score - indicates written exercises)
  - 🎹 (piano - indicates piano techniques)
  - ⚙️ (gear - indicates technical exercises)
  - 🔧 (wrench - indicates "technique work")
- **Sorting Compatibility**: Ensure existing sort options work with mixed types
- **Testing**: Verify all piece-related functionality works with techniques

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/PiecesAdapter.kt`
- Possibly `app/src/main/res/layout/item_piece_stats.xml` if emoji needs special styling

**Implementation Notes:**
- Feature creates a more unified and complete practice materials management interface
- Maintains clear visual distinction between pieces and techniques while integrating them
- Improves discoverability of techniques that may currently be less accessible
- Preserves all existing functionality while expanding scope to include techniques
- Choice of emoji should align with PlayStreak's musical theme and be easily recognizable

---

### Feature #24: ✅ Add Technique Emoji to Timeline and Inactive Tab
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Experience Team

**Description:**
Add the technique emoji indicator (🎼) next to technique names in both the Timeline and Inactive tabs to maintain visual consistency across the app. This ensures that techniques are clearly distinguished from pieces throughout all interfaces, not just in the Pieces tab.

**User Story:**
As a user viewing my activity timeline or inactive items, I want to easily distinguish between pieces and techniques at a glance, so that I can quickly understand what type of practice material each activity involved without having to rely on memory or naming conventions.

**Acceptance Criteria:**
- [ ] Timeline tab shows technique emoji (🎼) next to technique names in activity entries
- [ ] Inactive tab shows technique emoji (🎼) next to technique names in the pieces list
- [ ] Regular pieces continue to display without emoji in both tabs
- [ ] Emoji placement is consistent with Pieces tab implementation (before the name)
- [ ] Timeline activity formatting maintains readability with emoji addition
- [ ] Inactive tab list formatting maintains readability with emoji addition
- [ ] All existing functionality (navigation, quick-add, sorting) works unchanged
- [ ] Emoji rendering is consistent across all tabs (Timeline, Inactive, Pieces)
- [ ] Performance impact is minimal (emoji added at display time, not stored)

**Technical Considerations:**
- Update TimelineAdapter to conditionally display emoji for techniques
- Update AbandonedAdapter (Inactive tab) to conditionally display emoji for techniques
- Ensure consistent emoji choice (🎼) across all implementations
- Modify piece name display logic in both adapters
- Consider caching piece type information to avoid repeated database queries
- Test emoji rendering consistency across different Android versions
- Verify that emoji doesn't interfere with existing text truncation or styling

**Priority Justification:**
Medium priority UX consistency enhancement that extends the visual distinction system from Feature #23 across the entire app. Provides uniform user experience and reduces cognitive load when scanning through practice history or inactive items.

**Implementation Approach:**
- **Timeline Adapter**: Update activity display to include emoji for techniques:
  ```kotlin
  // In TimelineAdapter ViewHolder
  val pieceName = if (pieceOrTechnique.type == ItemType.TECHNIQUE) {
      "🎼 ${pieceOrTechnique.name}"
  } else {
      pieceOrTechnique.name
  }
  // Use pieceName in activity display string
  ```
- **Inactive Adapter**: Update piece name display to include emoji for techniques:
  ```kotlin
  // In AbandonedAdapter ViewHolder
  val displayName = if (item.piece.type == ItemType.TECHNIQUE) {
      "🎼 ${item.piece.name}"
  } else {
      item.piece.name
  }
  binding.pieceNameText.text = displayName
  ```
- **Consistency**: Use same emoji (🎼) and placement pattern across all tabs
- **Performance**: Add emoji at display time, not stored in database

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/TimelineAdapter.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/AbandonedAdapter.kt`

**Implementation Notes:**
- Feature provides visual consistency across all tabs where pieces/techniques are displayed
- Maintains same emoji choice (🎼) and placement pattern established in Feature #23
- Enhances user ability to quickly scan and understand practice material types
- No data model changes required - purely a display enhancement
- Should be implemented after or alongside Feature #23 for consistency
- Improves overall app cohesion and user experience through consistent visual language

**Dependencies:**
- Should align with emoji choice made in Feature #23 (Pieces tab technique emoji)
- Consider implementing together with Feature #23 for consistent rollout

---

### Feature #25: ✅ Import/Export Button Label Updates and Free User Access
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Interface Team

**Description:**
Update the import/export UI to provide clearer button labeling and remove unnecessary feature gating for free users on the import functionality. The export button should be renamed to "Export Activities to CSV" and the import button to "Import Activities from CSV" for better clarity. Additionally, free users should have access to the same import button as Pro users, removing the current disabled/different import button behavior.

**User Story:**
As a user, I want clear and descriptive button labels so that I understand exactly what each action will do, and as a free user, I want access to import functionality without being shown disabled buttons that create confusion about available features.

**Acceptance Criteria:**
- [ ] Export button text changed from current label to "Export Activities to CSV"
- [ ] Import button text changed from current label to "Import Activities from CSV"
- [ ] Free users see the same enabled import button as Pro users
- [ ] Remove any code that shows disabled/different import buttons for free users
- [ ] Import functionality works identically for both free and Pro users
- [ ] Export functionality continues to work unchanged (only button label changes)
- [ ] UI layout and styling remain consistent with current design
- [ ] No breaking changes to existing import/export logic

**Technical Considerations:**
- Update button text resources in strings.xml or directly in layout/code
- Remove Pro user checks specifically around import button display/enablement
- Verify that import functionality doesn't have other Pro-gated features that would break
- Test both free and Pro user flows to ensure consistent behavior
- Consider if this aligns with overall monetization strategy

**Priority Justification:**
Medium priority UX improvement that removes user confusion and provides clearer interface labeling. Removing arbitrary feature gates on import improves user experience without significantly impacting Pro value proposition, as the core differentiation should focus on other premium features.

**Implementation Approach:**
- **UI Updates**: Update button text resources for clarity
- **Feature Gate Removal**: Remove Pro user checks around import button display
- **Testing**: Verify import works consistently for both user types

**Files to Modify:**
- Import/Export fragment/activity files (likely in `ui/importexport/`)
- String resources (`res/values/strings.xml`)
- Any ProUserManager checks related to import button display

---

### Feature #26: ✅ Update Default Export Filename Format
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Low
**Requested By:** User Interface Team

**Description:**
Update the default export filename format to better reflect the PlayStreak brand and clarify the export content type. The current filename uses underscores and a generic "export" label that doesn't specify what type of data is being exported. With future plans for multiple export types (pieces, favorites, etc.), the filename should be more descriptive and consistent with branding.

**User Story:**
As a user exporting my practice data, I want the default filename to clearly indicate what type of data is being exported and maintain consistent branding, so that I can easily organize and identify my exported files.

**Acceptance Criteria:**
- [ ] Default export filename uses "PlayStreak" as a single word (not "play_streak")
- [ ] Filename includes "activities" to specify the export type
- [ ] Filename format supports future differentiation from other export types
- [ ] Maintains timestamp for uniqueness
- [ ] Uses consistent naming conventions with underscores as separators

**Technical Considerations:**
- Update filename generation in ImportExportFragment
- Consider establishing naming convention pattern for future export types
- Ensure filename compatibility across different operating systems
- Maintain backward compatibility (existing functionality unchanged)

**Priority Justification:**
Low priority cosmetic improvement that enhances user experience through clearer file identification and consistent branding. Prepares filename structure for future multiple export types.

**Implementation Approach:**
- **Current Format**: `play_streak_export_YYYY-MM-DD_HHMMSS.csv`
- **New Format**: `PlayStreak_activities_export_YYYY-MM-DD_HHMMSS.csv`
- **Future Formats**: `PlayStreak_pieces_export_...`, `PlayStreak_favorites_export_...`, etc.

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/ui/importexport/ImportExportFragment.kt`

---

### Feature #27: ✅ Implement Piece/Technique Count Limits
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** High
**Requested By:** Free Release Team

**Description:**
Implement limits on the total number of pieces and techniques that users can add to the app. Free users should be limited to 50 total items (pieces + techniques combined), while Pro users can have up to 60 total items. This limit only applies to new additions - users who already have more items than the limit should be allowed to keep them but not add more.

**User Story:**
As a system administrator, I want to limit the number of pieces and techniques users can add based on their subscription tier, so that we can maintain app performance and create differentiation between Free and Pro users while ensuring existing users aren't penalized.

**Acceptance Criteria:**
- [ ] Free users limited to maximum 50 pieces + techniques combined
- [ ] Pro users limited to maximum 60 pieces + techniques combined
- [ ] Limit enforcement only applies to new piece/technique additions
- [ ] Users with existing items above the limit can keep them but cannot add more
- [ ] Clear error message when user attempts to exceed limit
- [ ] Error message should suggest Pro upgrade for Free users (when Pro is available)
- [ ] Add piece dialog should check limits before allowing submission
- [ ] Quick-add functionality should respect limits
- [ ] Import functionality should handle limits appropriately (see Investigation section)
- [ ] Limits are enforced consistently across all piece/technique creation flows

**Technical Considerations:**
- Update AddPieceViewModel to check current count before allowing new pieces
- Add ProUserManager method to get appropriate limit for user type
- Update PianoRepository to provide current piece/technique count
- Modify all piece creation flows (manual add, quick-add, import) to respect limits
- Consider database performance for count queries
- Handle edge cases where count changes between check and insertion
- Ensure thread safety for concurrent piece additions

**Priority Justification:**
High priority for Free release preparation. Essential for creating meaningful differentiation between Free and Pro tiers while ensuring good performance for all users. Prevents potential abuse and ensures sustainable resource usage.

**Implementation Approach:**
- **Constants**: Define `FREE_USER_PIECE_LIMIT = 50` and `PRO_USER_PIECE_LIMIT = 60`
- **Validation**: Check current count before allowing new piece creation
- **User Experience**: Clear, helpful error messages with upgrade path information
- **Performance**: Efficient count queries that don't impact app performance

**Implementation Details:**
- **Piece Limits**: Added constants FREE_USER_PIECE_LIMIT = 50 and PRO_USER_PIECE_LIMIT = 60
- **ProUserManager**: Enhanced with canAddMorePieces() and getPieceLimit() methods
- **AddPieceViewModel**: Added piece count validation before saving new pieces
- **AddPieceFragment**: Added limit reached dialog with clear messaging
- **Validation Logic**: Checks current piece/technique count against user type limits
- **User Experience**: Shows informative dialog when limit reached, allows keeping existing pieces
- **Manual Addition**: Fully implemented for Add Piece functionality

**Phase 1 Complete - Manual Piece Addition:**
- ✅ Piece limit constants and validation logic implemented
- ✅ User-friendly error dialog with clear messaging
- ✅ Existing pieces preserved (grandfathered) above limits
- ✅ Consistent Pro/Free user differentiation

**Phase 2 Pending - CSV Import Handling:**
**Investigation Required: Import Handling**
**Issue:** How should the system handle piece/technique limits when importing CSV data?

**Scenarios to Consider:**
1. Free user imports CSV with 75 pieces - exceeds 50 piece limit
2. User already has 45 pieces, imports CSV with 20 more pieces
3. Pro user imports CSV that would exceed 60 piece limit
4. User downgrades from Pro to Free after having 55 pieces

**Recommendation:**
Implement **Import Limit Enforcement with User Choice**:

1. **Pre-Import Validation:**
   - Count total unique pieces in CSV file
   - Compare with user's current count + import count vs limit
   - If would exceed limit, show warning dialog before import

2. **Import Options Dialog:**
   ```
   "Import Limit Warning

   This import contains 75 pieces, but you are limited to 50 pieces total.
   You currently have 30 pieces.

   Choose an option:
   • Import first 20 pieces (up to your limit)
   • Proceed anyway (existing pieces kept, new ones truncated)"
   ```

3. **Truncation Strategy:**
   - Import pieces in CSV order until limit reached
   - Log which pieces were skipped in import results
   - Allow user to see list of skipped pieces

4. **Alternative Approach:**
   - Allow import to proceed but mark excess pieces as "inactive"
   - User can choose which pieces to keep active within their limit
   - Provides more user control over which pieces are important

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/utils/ProUserManager.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/pieces/AddPieceViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/pieces/AddPieceFragment.kt`

**Files Pending (Import Functionality):**
- `app/src/main/java/com/pseddev/mystreak/data/repository/PianoRepository.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/importexport/ImportExportViewModel.kt`

**Implementation Notes:**
- Feature is critical for Free release and Pro differentiation
- Import handling requires careful UX design to avoid user frustration
- Consider phased implementation: basic limits first, import handling second
- Should integrate with existing ProUserManager architecture
- Error messages should be helpful and guide users toward solutions

---

### Feature #28: ✅ Update Practice Suggestions Algorithm Timing
**Status:** Implemented
**Date Requested:** 2025-07-22
**Date Implemented:** 2025-07-22
**Priority:** Medium
**Requested By:** User Experience Team

**Description:**
Update the user-facing text description of the practice suggestions algorithm to clarify the timing intervals. Change the explanation text from "favorites after 2+ days, others after 7-31 days" to "favorites after 1+ day, others after 7+ days" to better communicate the suggestion criteria to users.

**User Story:**
As a user who practices regularly, I want to see practice suggestions more frequently so that I can maintain consistent practice on my favorite pieces and don't forget about other pieces in my repertoire for too long.

**Acceptance Criteria:**
- [ ] Favorite pieces appear in suggestions after 1+ day without practice (down from 2+ days)
- [ ] Non-favorite pieces appear in suggestions after 7+ days without practice
- [ ] Today exclusion logic remains unchanged (pieces practiced today are never suggested)
- [ ] Suggestion priority remains: favorites first, then non-favorites by longest time since practice
- [ ] Pro/Free suggestion count limits remain unchanged
- [ ] Dashboard suggestions follow same timing rules as Suggestions tab
- [ ] Clear suggestion reasons display updated timing ("not practiced in 1+ day" vs "not practiced in 7+ days")
- [ ] Fallback logic (least recently practiced) uses same timing thresholds

**Technical Considerations:**
- Update SuggestionsViewModel favorite condition from 2+ days to 1+ day
- Update DashboardViewModel to match SuggestionsViewModel timing
- Modify suggestion reason text to reflect new timing
- Update any hardcoded day calculations (twoDaysAgo → oneDayAgo, etc.)
- Ensure consistent implementation across all suggestion logic paths
- Consider impact on suggestion frequency and user experience
- Test with various practice patterns to ensure appropriate suggestion behavior

**Priority Justification:**
Medium priority UX enhancement that makes the suggestion system more responsive and encourages more frequent practice. The shorter intervals align with daily practice habits and help users maintain momentum with their favorite pieces while ensuring other repertoire doesn't get forgotten.

**Implementation Approach:**
- **Favorites Logic**: Change from `lastActivityDate < twoDaysAgo` to `lastActivityDate < oneDayAgo`
- **Non-Favorites Logic**: Implement `lastActivityDate < sevenDaysAgo` threshold for non-favorite suggestions
- **Time Calculations**:
  ```kotlin
  val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
  val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
  ```
- **Suggestion Reasons**: Update display text to reflect new timing intervals
- **Consistency**: Ensure both SuggestionsViewModel and DashboardViewModel use identical logic

**Current vs New Behavior:**
- **Current**: Favorites suggested after 2+ days, others suggested less frequently
- **New**: Favorites suggested after 1+ day, others suggested after 7+ days
- **Impact**: More frequent suggestions overall, better coverage of user's repertoire

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- Any suggestion reason text resources or string formatting

**Implementation Notes:**
- Feature makes suggestions more responsive to daily practice patterns
- Should encourage more consistent practice by surfacing favorites more frequently
- 7-day threshold for non-favorites prevents repertoire from being completely forgotten
- Maintains existing suggestion prioritization and limits
- Should be tested with various practice schedules to ensure good user experience

---

### Feature #29: ✅ Set App Version to Beta Status
**Status:** Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** High
**Requested By:** Development Team

**Description:**
Update the app version number in build.gradle.kts to include a "-beta" suffix to clearly indicate the current development stage. The version should be changed from "1.0.8.3" to "1.0.8.3-beta" to properly reflect that this is a beta release.

**User Story:**
As a developer or beta tester, I want the app version to clearly indicate beta status so that everyone understands this is a pre-release version, and as a user, I want to know when I'm using beta software so I can set appropriate expectations.

**Acceptance Criteria:**
- [x] App version changed from "1.0.8.3" to "1.0.8.3-beta" in build.gradle.kts
- [x] Version naming strategy documented for future beta releases (1.0.8.4-beta, 1.0.8.5-beta, etc.)
- [x] Production release target set as 1.0.9 (without beta suffix)
- [x] Beta version is visible in app UI where version is displayed
- [x] Consistent beta labeling across development workflow

**Technical Considerations:**
- Update versionName in app/build.gradle.kts defaultConfig section
- Ensure version is properly displayed in About/Settings screens
- Plan version numbering strategy for future beta releases
- Prepare transition to stable release version (1.0.9)

**Priority Justification:**
High priority for proper beta release management and user expectation setting. Beta versioning prevents confusion about app maturity level and ensures appropriate feedback from testers.

**Implementation Details:**
- **Version Change**: Updated `versionName` from "1.0.8.3" to "1.0.8.3-beta" in build.gradle.kts
- **Future Strategy**: Next beta versions will be 1.0.8.4-beta, 1.0.8.5-beta, etc.
- **Production Target**: Stable release will be version 1.0.9 (removing beta suffix)
- **Documentation**: Updated free-release-readiness.md to reflect beta versioning approach

**Files Modified:**
- `app/build.gradle.kts`
- `app/docs/free-release-readiness.md`

**Implementation Notes:**
- Feature provides clear beta status indication for all stakeholders
- Maintains version continuity while adding development stage clarity
- Prepares path for smooth transition to stable release
- Aligns with standard software development versioning practices

---

### Feature #30: ✅ Add Activity Count to Settings Page Add Activity Button
**Status:** Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** Low
**Requested By:** User Interface Team

**Description:**
Add the current total number of activities to the Add Activity button in the Settings page, similar to how the Add Piece button shows the piece count. The button should display the format "Add Activity (#)" where # is the total number of activities in the user's database.

**User Story:**
As a user, I want to see at a glance how many activities I have logged in total when I'm on the Settings page, so that I can quickly understand the scope of my practice history and track my overall activity volume.

**Acceptance Criteria:**
- [x] Add Activity button in Settings page shows current activity count in parentheses
- [x] Button format: "Add Activity (123)" where 123 is the total activity count
- [x] Activity count updates dynamically when activities are added/removed
- [x] Count reflects actual total number of activities in user's database
- [x] Button functionality remains unchanged (still navigates to Add Activity screen)
- [x] Consistent styling with existing Add Piece button that shows piece count
- [x] Real-time count updates when returning to Settings page after adding activities
- [x] Performance is acceptable (count query doesn't slow down Settings page)

**Technical Considerations:**
- Add activity count query to Settings page/ViewModel
- Use LiveData or similar reactive pattern to observe activity count changes
- Query repository for total activity count: `repository.getAllActivities().count()`
- Update button text dynamically based on observed activity count
- Consider caching strategy if count queries impact performance
- Ensure count updates when user returns from adding/editing activities
- Handle zero activities case gracefully ("Add Activity (0)")

**Priority Justification:**
Low priority UI enhancement that provides useful information to users about their practice volume. Complements the existing Add Piece (#) button pattern and helps users understand their overall app usage statistics at a glance.

**Implementation Approach:**
- **Data Source**: Use repository.getAllActivities() to get activity count
- **UI Pattern**: Follow same pattern as existing Add Piece button with count display
- **Observer Pattern**: Use LiveData to reactively update count when activities change
- **Button Text**: Update from "Add Activity" to "Add Activity (#)" format
- **Performance**: Use efficient count query or cached count if needed
- **Consistency**: Match styling and behavior of Add Piece button

**Similar Features:**
This feature mirrors Feature #15 (Add Piece button with count) but for activities instead of pieces, providing users with a complete view of both their repertoire size and practice volume.

**Files to Modify:**
- Settings page/fragment (likely `app/src/main/java/com/pseddev/mystreak/ui/main/MainFragment.kt`)
- Settings ViewModel if exists, or add activity count to MainViewModel
- Settings layout file for button text updates

**Implementation Details:**
- **MainViewModel Enhancement**: Added `activitiesCount` LiveData using `repository.getAllActivities().map { activities -> activities.size }`
- **Observer Pattern**: Added observer in MainFragment that updates button text: `binding.buttonAddActivity.text = "Add Activity ($count)"`
- **Real-time Updates**: Uses reactive LiveData pattern to automatically update count when activities are added/removed
- **Performance**: Leverages existing repository.getAllActivities() flow with efficient mapping to count
- **UI Consistency**: Follows same pattern as existing Add Piece button implementation

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/main/MainViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/main/MainFragment.kt`

**Implementation Notes:**
- Feature provides symmetric information display with piece count button
- Helps users understand their practice activity volume at a glance
- Uses efficient database queries without performance impact
- Count display creates sense of accomplishment for active users
- May be useful for tracking progress toward activity limits in future features
- Seamlessly integrates with existing Settings page UI pattern

---

### Feature #31: ✅ Update Developer Attribution to PseudonymousEd
**Status:** Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** High
**Requested By:** Developer/Legal Team

**Description:**
Update all references to the developer's real name throughout the codebase, documentation, and configuration files to use "PseudonymousEd" instead. This includes code comments, copyright notices, author attributions, package names, documentation, and any other places where the developer name appears.

**User Story:**
As a developer preparing for public release, I want to use a pseudonymous identity "PseudonymousEd" instead of my real name in all project files, so that my personal identity is protected while maintaining proper attribution for the work.

**Acceptance Criteria:**
- [x] All code files updated to reference "PseudonymousEd" instead of real name
- [x] Documentation files updated with new attribution
- [x] Copyright notices updated to use "PseudonymousEd"
- [x] Package names or namespaces updated if they contain personal name references
- [x] Build configuration files updated (gradle, manifest, etc.)
- [x] README files and project descriptions updated
- [ ] Git commit author information considerations addressed
- [x] No remaining references to real name anywhere in the project
- [x] Consistent use of "PseudonymousEd" across all files and contexts
- [x] Legal/licensing files updated to reflect new attribution

**Technical Considerations:**
- Search entire codebase for real name references
- Update package declarations if they contain personal name
- Review AndroidManifest.xml for developer/author references
- Check gradle files for author or developer information
- Update any LICENSE or COPYRIGHT files
- Consider implications for app store listings and developer accounts
- Review documentation files (README, CHANGELOG, etc.)
- Check string resources for developer attribution
- Verify no hardcoded personal information remains

**Priority Justification:**
High priority for privacy and professional pseudonymous identity management. Essential for public release preparation to protect developer's personal identity while maintaining proper code attribution and ownership.

**Implementation Approach:**
- **Global Search**: Use IDE or command-line tools to find all instances of real name
- **Systematic Replacement**: Replace with "PseudonymousEd" maintaining context appropriateness
- **File Types to Check**:
  - Source code files (.kt, .java)
  - Documentation (.md, .txt)
  - Configuration files (build.gradle.kts, AndroidManifest.xml)
  - Legal files (LICENSE, COPYRIGHT)
  - Resource files (strings.xml)
- **Verification**: Final search to ensure no instances remain

**Files to Investigate:**
- All source code files in `app/src/`
- Documentation files in `app/docs/`
- Build configuration files (`build.gradle.kts`, `AndroidManifest.xml`)
- Legal/licensing files in project root
- Any README or project description files
- String resources in `app/src/main/res/values/`

**Implementation Details:**
- **UI Attribution**: Updated fragment_main.xml author text from real name to "PseudonymousEd"
- **Package Names**: Confirmed existing package "com.pseddev.mystreak" already uses pseudonymous identity
- **Documentation**: Verified and updated features.md to remove real name references
- **Copyright Files**: Confirmed COPYRIGHT file already uses pseudonymous "PlayStreak" attribution
- **README**: Verified README.md already uses pseudonymous references throughout
- **AndroidManifest**: Confirmed uses pseudonymous package name
- **Build Files**: Confirmed no real name references in build.gradle.kts

**Files Modified:**
- `app/src/main/res/layout/fragment_main.xml` - Updated author attribution text
- `app/docs/features.md` - Removed real name references from feature description

**Verification Results:**
- No remaining real name references found in source code
- All documentation uses pseudonymous identity
- Package names and namespaces are pseudonymous
- Legal/copyright files use business name attribution
- UI displays "PseudonymousEd" as developer attribution

**Implementation Notes:**
- Feature successfully protects developer privacy while maintaining code attribution
- Essential for public release and professional pseudonymous identity management
- Completed before any public code repository or app store publication
- All project materials now use consistent pseudonymous identity
- Ready for public release from privacy perspective

---

### Feature #32: ✅ Performance Suggestions for Pro Users
**Status:** Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** High
**Requested By:** User Experience Team

**Description:**
Add a new "Performance Suggestions" section to the Dashboard tab's suggestions area, positioned after the existing Practice Suggestions section. This Pro-only feature will help users identify pieces they should consider performing by analyzing their performance history and favorite status.

**User Story:**
As a Pro user preparing for performances, I want intelligent suggestions for pieces to perform based on my performance history and favorites, so that I can maintain a balanced performance repertoire and ensure I'm regularly performing my favorite pieces.

**Acceptance Criteria:**
- [x] Performance Suggestions section appears only for Pro users
- [x] Section is positioned after Practice Suggestions in Dashboard tab
- [x] Shows up to 5 performance suggestions using two-tier algorithm with quality requirement
- [x] Base requirement: Pieces practiced ≥3 times in 28 days with at least one Level 4 practice
- [x] First tier: Qualified pieces not performed in 28 days (sorted by most recent practice)
- [x] Second tier: All qualified pieces (sorted by least recent performance, ties broken by practice count)
- [x] Each suggestion shows piece name and relevant performance timing info
- [x] Section has clear "Performance Suggestions" heading
- [x] Suggestions are tappable and navigate to Add Activity for that piece
- [x] Section gracefully handles cases with insufficient data
- [x] Performance suggestions update dynamically when activities are added
- [x] Free users do not see this section at all
- [x] Performance suggestions also appear in dedicated Suggestions tab
- [x] Suggestions tab shows both Practice and Performance sections for Pro users

**Technical Considerations:**
- Query database for both practice and performance activities to analyze practice frequency and quality
- Two-tier algorithm based on practice frequency (≥3 times in 28 days) and quality (≥1 Level 4 practice)
- Quality requirement ensures only performance-ready pieces are suggested
- First tier prioritizes pieces ready for performance (practiced but not recently performed)
- Second tier includes pieces with recent performances for continued performance rotation
- Integrate with existing Pro user detection system
- Update Dashboard ViewModel and Fragment to handle new suggestions
- Consider performance impact of additional database queries

**Priority Justification:**
High priority Pro feature that adds significant value for performing musicians. Helps Pro users maintain active performance repertoire and ensures favorite pieces aren't neglected in performance rotation.

**Implementation Approach:**
- **Database Queries**: Query both practice and performance activities for comprehensive analysis
- **Pro User Check**: Use existing ProUserManager to show/hide section
- **UI Integration**: Add new section to Dashboard fragment after Practice Suggestions
- **Two-Tier Algorithm**:
  - **First Tier**: Pieces with ≥3 practices in 28 days but no performances in 28 days (sorted by most recent practice)
  - **Second Tier**: Pieces with ≥3 practices in 28 days (sorted by least recent performance)
  - **Combined**: Up to 5 suggestions total across both tiers
- **Navigation**: Make suggestions tappable to add performance activities

**Implementation Details:**
- **Two-Tier Algorithm**: Implemented sophisticated algorithm based on practice frequency and performance history
- **DashboardViewModel Enhancement**: Added `performanceSuggestions` LiveData using new two-tier algorithm
- **SuggestionsViewModel Enhancement**: Extended to include both practice and performance suggestions with SuggestionType enum
- **Pro User Logic**: Only shows Performance Suggestions section for Pro users using ProUserManager
- **Practice Frequency Analysis**: Filters pieces with ≥3 practice activities in last 28 days
- **Performance History Analysis**: Tracks last performance date to determine tier placement
- **First Tier Logic**: Prioritizes pieces practiced recently but not performed in 28 days (sorted by most recent practice)
- **Second Tier Logic**: Includes pieces with recent performances for rotation (sorted by least recent performance)
- **Dashboard Integration**: Added new MaterialCardView section positioned after Practice Suggestions
- **Suggestions Tab Integration**: Modified Suggestions tab to show separate sections for Practice and Performance suggestions
- **Dynamic Updates**: Uses reactive LiveData pattern to update suggestions when activities change
- **Section-Based UI**: Suggestions tab now has two RecyclerViews with clear section headers
- **Suggestion Reasons**: Shows practice count and performance history in user-friendly format

**Files Modified:**
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt` - Added performanceSuggestions LiveData
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt` - Added observer for performance suggestions
- `app/src/main/res/layout/fragment_dashboard.xml` - Added Performance Suggestions card section
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsViewModel.kt` - Enhanced with SuggestionType and performance logic
- `app/src/main/java/com/pseddev/mystreak/ui/progress/SuggestionsFragment.kt` - Updated to handle separate sections
- `app/src/main/res/layout/fragment_suggestions.xml` - Modified layout for section-based display

**Implementation Notes:**
- Feature successfully differentiates between practice and performance activities for targeted suggestions
- Pro-only feature enhances value proposition for paid users
- Helps performing musicians maintain active performance repertoire rotation
- Ensures favorite pieces aren't neglected in performance planning
- Provides balanced suggestions across different performance frequency patterns
- Integrates seamlessly with existing Dashboard suggestions architecture
- Available in both Dashboard tab (card format) and Suggestions tab (dedicated sections)
- Suggestions tab shows clear separation between Practice and Performance suggestions for Pro users
- Free users see only Practice suggestions, maintaining feature differentiation

---

### Feature #33: Filter Timeline by Performances (Pro Only) - ✅ COMPLETE
**Status:** ✅ Fully Implemented via Sub-Features
**Date Requested:** 2025-07-23
**Priority:** Medium
**Requested By:** Pro User Experience Team

**Description:**
Add a filter option to the Timeline tab that allows Pro users to view only Performance activities, hiding Practice activities from the timeline view. This will help Pro users focus on their performance history and track their performance frequency and patterns.

**User Story:**
As a Pro user tracking my performance activities, I want to filter the Timeline tab to show only performances, so that I can focus on my performance history without being distracted by practice activities and better analyze my performance patterns over time.

**Implementation Strategy:**
Due to previous app crashes when implementing this feature, it has been split into smaller, safer sub-features that can be implemented and tested incrementally.

---

### Feature #33A: Timeline Pro User Detection and Basic Filter UI
**Status:** ✅ Implemented
**Priority:** High (Foundation)
**Dependencies:** None

**Description:**
Add basic Pro user detection to Timeline and create minimal filter UI structure without any functional filtering logic.

**Acceptance Criteria:**
- [x] Add ProUserManager integration to TimelineViewModel
- [x] Add basic filter UI section that shows/hides based on Pro status
- [x] Filter UI appears only for Pro users
- [x] Filter UI shows placeholder text (no actual filtering)
- [x] Free users see no filter changes
- [x] No crashes when switching between tabs

**Technical Focus:**
- Safe ProUserManager integration in TimelineViewModel
- Basic UI visibility logic in TimelineFragment
- Minimal layout changes to timeline XML
- No filtering logic - just UI framework

**Risk Level:** Low (UI-only changes, no filtering logic)

---

### Feature #33B: Timeline Filter State Management (Safe Implementation)
**Status:** ✅ Implemented
**Priority:** High (Core Logic)
**Dependencies:** Feature #33A

**Description:**
Add filter state management to TimelineViewModel without connecting it to actual data filtering. Uses simple Switch control instead of ChipGroup to avoid crashes.

**Acceptance Criteria:**
- [x] Add filter state LiveData in TimelineViewModel
- [x] Add methods to toggle filter state
- [x] Filter state responds to UI interactions
- [x] Filter state is logged for debugging
- [x] No actual data filtering occurs yet
- [x] App remains stable when toggling filter

**Safe Implementation Approach:**
- Uses simple `Switch` control instead of Material ChipGroup
- Single toggle method instead of multiple state setters
- Comprehensive logging for debugging
- Pro user gating for all filter operations

**Technical Focus:**
- LiveData for filter state management
- Basic toggle methods in ViewModel
- UI interaction handling in Fragment
- Debug logging for state changes
- No data transformation yet

**Risk Level:** Low (State management only, no data filtering)

---

### Feature #33C: Timeline Data Filtering Logic
**Status:** ✅ Implemented
**Priority:** Medium (Filtering Implementation)
**Dependencies:** Feature #33B

**Description:**
Implement actual activity filtering based on filter state, showing only Performance activities when filter is active.

**Acceptance Criteria:**
- [x] Activities are filtered by ActivityType.PERFORMANCE when filter is active
- [x] "All Activities" shows all activities (current behavior)
- [x] "Performances Only" shows only performance activities
- [x] Empty state updates appropriately for filtered view
- [x] Filtering works with existing sorting and display logic

**Technical Focus:**
- Activity filtering in ViewModel data flow
- Proper LiveData transformation
- Empty state message updates
- Preserve existing timeline functionality

**Risk Level:** Medium (Data transformation logic)

---

### Feature #33D: Timeline Filter State Persistence
**Status:** ✅ Implemented
**Priority:** Low (Enhancement)
**Dependencies:** Feature #33C

**Description:**
Add SharedPreferences to persist filter state across app sessions and navigation.

**Acceptance Criteria:**
- [x] Filter state persists when navigating away and back
- [x] Filter state persists across app restarts
- [x] Different users can have different filter preferences
- [x] State persistence doesn't affect app performance

**Technical Focus:**
- SharedPreferences integration
- State restoration on ViewModel initialization
- User-specific preference keys
- Performance optimization

**Risk Level:** Low (Preference storage only)

**Files to Modify Across All Sub-Features:**
- **33A**: Timeline fragment layout, TimelineFragment.kt (minimal)
- **33B**: TimelineViewModel.kt (state management)
- **33C**: TimelineViewModel.kt (filtering logic)
- **33D**: TimelineViewModel.kt (SharedPreferences)

---

## Feature Request Template

When requesting new features, please use this template:

```markdown
### 💡 [Feature Title]
**Status:** Requested
**Date Requested:** YYYY-MM-DD
**Priority:** [Critical/High/Medium/Low/Future]
**Requested By:** [Name/Community/User Feedback]

**Description:**
[Clear description of the requested feature]

**User Story:**
As a [user type], I want [functionality] so that [benefit/value].

**Acceptance Criteria:**
- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

**Technical Considerations:**
[Any technical requirements, constraints, or implementation notes]

**Priority Justification:**
[Why this feature has the assigned priority level]
```

---

## Implementation Guidelines

### Feature Development Process
1. **Analysis** - Evaluate feasibility, user value, and technical requirements
2. **Design** - Create UI/UX mockups and technical architecture
3. **Implementation** - Develop feature following coding standards
4. **Testing** - Unit tests, integration tests, and user acceptance testing
5. **Documentation** - Update user guides and developer documentation
6. **Release** - Deploy with appropriate version numbering

---

### Feature #34: Improved Streak Emoji Differentiation
**Status:** ✅ Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** Low (Visual Enhancement)
**Requested By:** User Experience Team

**Description:**
Replace duplicate musical note emojis (🎵🎵) in streak display with a more distinct musical notes emoji (🎶) to create better visual differentiation between streak levels and avoid redundant emoji usage.

**User Story:**
As a user tracking my practice streak, I want visually distinct emojis for different streak levels so that I can easily distinguish my progress milestones and feel a sense of progression through the emoji changes.

**Acceptance Criteria:**
- [x] Replace duplicate 🎵🎵 emoji with single 🎶 emoji for 5-6 day streaks
- [x] Maintain existing emoji progression logic and thresholds
- [x] Ensure new emoji displays correctly across all Android devices
- [x] Preserve visual consistency with existing streak display

**Current Emoji Progression:**
- **1-2 days**: No emoji suffix
- **3-4 days**: 🎵 (single musical note)
- **5-6 days**: 🎶 (multiple musical notes) - *Updated from 🎵🎵*
- **7-13 days**: 🔥 (fire - "hot streak")
- **14+ days**: 🔥🔥🔥 (triple fire - "on fire")

**Technical Implementation:**
Updated `DashboardFragment.kt` streak emoji logic:
```kotlin
val emojiSuffix = when {
    streak >= 14 -> " 🔥🔥🔥"
    streak >= 7 -> " 🔥"
    streak >= 5 -> " 🎶"      // Changed from " 🎵🎵"
    streak >= 3 -> " 🎵"
    else -> ""
}
```

**Files Modified:**
- `/app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt` (line 87)

**Impact:**
- Improves visual progression and user experience
- Eliminates redundant emoji usage
- Creates clearer milestone distinction
- Maintains musical theme consistency

**Testing Notes:**
- Verify emoji displays correctly on various Android versions
- Test streak calculation and emoji assignment at different streak levels
- Confirm no regression in existing streak functionality

---

### Feature #35: Adjust Fire Emoji Streak Threshold
**Status:** ✅ Implemented
**Date Requested:** 2025-07-23
**Date Implemented:** 2025-07-23
**Priority:** Low (Visual Enhancement)
**Requested By:** User Experience Team

**Description:**
Adjust the streak emoji progression to make the fire emoji (🔥) appear at 8 days instead of 7 days, creating a more challenging and rewarding milestone for users to achieve the "hot streak" status.

**User Story:**
As a user building practice streaks, I want the fire emoji to appear at 8 days instead of 7 so that achieving a "hot streak" feels more significant and rewarding, encouraging me to maintain longer consistent practice habits.

**Acceptance Criteria:**
- [x] Fire emoji (🔥) appears at 8+ day streaks instead of 7+ days
- [x] Maintain existing emoji progression for other thresholds
- [x] Update any documentation referencing the 7-day fire threshold
- [x] Test streak calculation and emoji assignment at all levels

**Previous vs. New Emoji Progression:**

**Previous:**
- 1-2 days: No emoji suffix
- 3-4 days: 🎵 (single musical note)
- 5-6 days: 🎶 (multiple musical notes)
- 7-13 days: 🔥 (fire - "hot streak")
- 14+ days: 🔥🔥🔥 (triple fire - "on fire")

**New (Implemented):**
- 1-2 days: No emoji suffix
- 3-4 days: 🎵 (single musical note)
- 5-7 days: 🎶 (multiple musical notes) - *Extended range*
- 8-13 days: 🔥 (fire - "hot streak") - *Moved from 7 to 8*
- 14+ days: 🔥🔥🔥 (triple fire - "on fire")

**Technical Implementation:**
Update `DashboardFragment.kt` streak emoji logic:
```kotlin
val emojiSuffix = when {
    streak >= 14 -> " 🔥🔥🔥"
    streak >= 8 -> " 🔥"       // Changed from >= 7
    streak >= 5 -> " 🎶"
    streak >= 3 -> " 🎵"
    else -> ""
}
```

**Files to Modify:**
- `/app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt` (line 86)
- Update any documentation referencing 7-day fire threshold

**Impact:**
- Increases challenge and reward for achieving fire emoji status
- Makes 7-day streaks still feel rewarding with musical notes emoji
- Maintains progression logic while adjusting milestone significance
- Encourages longer practice consistency

**Dependencies:**
- None (independent change to existing emoji logic)

**Testing Notes:**
- Test emoji assignment at streak days 6, 7, 8, 9 to verify transition
- Verify existing streak calculation logic remains intact
- Test with various streak lengths to ensure no regression
- Update any user documentation or help text referencing emoji thresholds

---

### Feature #36: Firebase Crashlytics and Analytics Integration
**Status:** ✅ Implemented
**Date Requested:** 2025-07-23
**Date Completed:** 2025-07-23
**Priority:** High (Release Critical)
**Requested By:** Release Preparation Team

**Description:**
Integrate Firebase Crashlytics for crash reporting and Firebase Analytics for basic usage tracking to monitor app health, user engagement, and support successful free release launch. This is a critical component of the free release preparation identified in free-release-readiness.md.

**User Story:**
As a developer preparing for free release, I want comprehensive crash reporting and usage analytics so that I can monitor app stability, identify issues quickly, track user engagement patterns, and make data-driven decisions for app improvements.

**Acceptance Criteria:**
- [x] Firebase project created and configured for PlayStreak
- [x] Firebase Crashlytics SDK integrated and functional
- [x] Firebase Analytics SDK integrated and functional
- [x] Crash reporting working with test crash functionality
- [x] Basic usage events tracked (app opens, activities logged, streaks achieved)
- [x] Custom events for key user actions implemented
- [x] Privacy policy updated to reflect data collection
- [x] Analytics dashboard accessible and functional

**Implementation Steps:**

#### **Phase 1: Firebase Project Setup**
1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Create a project" or "Add project"
   - Project name: "PlayStreak" or "playstreak-android"
   - Enable Google Analytics for project (recommended)
   - Choose or create Google Analytics account
   - Accept Firebase terms and create project

2. **Add Android App to Project**
   - Click "Add app" → Android icon
   - **Android package name**: `com.pseddev.mystreak` (must match build.gradle)
   - **App nickname**: "PlayStreak Android"
   - **SHA-1 signing certificate**: Generate using `./gradlew signingReport`
   - Download `google-services.json` file

3. **Add Configuration File**
   - Place `google-services.json` in `app/` directory (same level as build.gradle.kts)
   - Add to `.gitignore` if contains sensitive data (optional for this project)

#### **Phase 2A: Firebase Analytics Setup (Follow Google's Docs)**
4. **Project-level build.gradle.kts Updates**
   - Add Google Services plugin to classpath:
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.4.3" apply false
       // Note: Don't add crashlytics plugin yet - Analytics first
   }
   ```

5. **App-level build.gradle.kts Updates**
   - Apply Firebase plugin:
   ```kotlin
   plugins {
       // existing plugins...
       id("com.google.gms.google-services")
       // Note: Don't add crashlytics plugin yet
   }
   ```

   - Add Firebase Analytics dependency:
   ```kotlin
   dependencies {
       // Firebase BOM (Bill of Materials) - check for latest version
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

       // Firebase Analytics only (start with this)
       implementation("com.google.firebase:firebase-analytics-ktx")

       // existing dependencies...
   }
   ```

6. **Test Analytics Integration**
   - Build and run the app: `./gradlew assembleDebug`
   - Verify no build errors
   - Check Firebase console for initial connection (may take 24-48 hours to show data)

#### **Phase 2B: Add Crashlytics (After Analytics Works)**
7. **Add Crashlytics Plugin to Project-level build.gradle.kts**
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.4.3" apply false
       id("com.google.firebase.crashlytics") version "2.9.9" apply false // Add this line
   }
   ```

8. **Add Crashlytics Plugin to App-level build.gradle.kts**
   ```kotlin
   plugins {
       // existing plugins...
       id("com.google.gms.google-services")
       id("com.google.firebase.crashlytics") // Add this line
   }
   ```

9. **Add Crashlytics Dependency**
   ```kotlin
   dependencies {
       // Firebase BOM (already added)
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

       // Firebase Analytics (already added)
       implementation("com.google.firebase:firebase-analytics-ktx")

       // Firebase Crashlytics (add this)
       implementation("com.google.firebase:firebase-crashlytics-ktx")

       // existing dependencies...
   }
   ```

#### **Phase 3: Analytics Implementation**
10. **Initialize Analytics**
   - Add to application initialization:
   ```kotlin
   import com.google.firebase.analytics.FirebaseAnalytics

   private lateinit var firebaseAnalytics: FirebaseAnalytics

   // In onCreate()
   firebaseAnalytics = FirebaseAnalytics.getInstance(this)
   ```

11. **Track Key Events**
    - **App Opens**: Automatic (no code needed)
    - **Activity Logged**:
    ```kotlin
    firebaseAnalytics.logEvent("activity_logged") {
        param("activity_type", activityType.name)
        param("piece_type", pieceType)
        param("has_duration", hasDuration)
    }
    ```

    - **Streak Achieved**:
    ```kotlin
    firebaseAnalytics.logEvent("streak_achieved") {
        param("streak_length", streakLength)
        param("emoji_level", emojiLevel)
    }
    ```

    - **Piece Added**:
    ```kotlin
    firebaseAnalytics.logEvent("piece_added") {
        param("piece_type", pieceType)
        param("total_pieces", totalPieceCount)
    }
    ```

    - **CSV Import/Export**:
    ```kotlin
    firebaseAnalytics.logEvent("csv_operation") {
        param("operation_type", "import" or "export")
        param("activity_count", activityCount)
        param("success", isSuccessful)
    }
    ```

#### **Phase 4: Crashlytics Implementation**
12. **Initialize Crashlytics**
   - Add to `PlayStreakApplication.kt` or `MainActivity.kt`:
   ```kotlin
   import com.google.firebase.crashlytics.FirebaseCrashlytics

   // In onCreate() or initialization
   FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
   ```

13. **Test Crash Reporting**
   - Add test crash button/method for development:
   ```kotlin
   // For testing only - remove before release
   FirebaseCrashlytics.getInstance().recordException(RuntimeException("Test crash"))
   // Or force crash:
   throw RuntimeException("Test crash")
   ```

14. **Custom Crash Logging**
   - Add custom keys and logs to crashes:
   ```kotlin
   FirebaseCrashlytics.getInstance().apply {
       setCustomKey("user_type", if (proUserManager.isProUser()) "pro" else "free")
       setCustomKey("piece_count", pieceCount)
       log("User performed action: $actionName")
   }
   ```

#### **Phase 5: Privacy and Compliance**
15. **Update Privacy Policy**
    - Add Firebase data collection disclosure
    - Mention crash reporting and usage analytics
    - Include Google Analytics data sharing (if enabled)
    - Link to Google's privacy policy for Firebase services

16. **User Consent Handling** (Optional for initial release)
    - Consider adding analytics opt-out setting
    - Implement consent management if required by target markets
    - Default to enabled for app stability monitoring

#### **Phase 6: Testing and Validation**
17. **Test Implementation**
    - Verify Crashlytics dashboard shows test crashes
    - Confirm Analytics events appear in Firebase console
    - Test custom parameters and user properties
    - Validate event tracking across key user flows

18. **Release Configuration**
    - Ensure crash reporting works in release builds
    - Verify ProGuard/R8 rules don't break Firebase
    - Test with release keystore and production configuration

**Technical Requirements:**
- Android SDK 19+ (already met - minSdk 24)
- Google Play Services available on device
- Internet connection for data transmission
- Proper ProGuard/R8 configuration for release builds

**Dependencies:**
- Google Services Plugin
- Firebase Crashlytics and Analytics SDKs
- Updated privacy policy before release

**Expected Outcomes:**
- Real-time crash reporting and alerting
- User engagement metrics and retention tracking
- Key performance indicators for app health
- Data-driven insights for feature improvements
- Improved user support through crash diagnostics

**Files to Modify:**
- `build.gradle.kts` (project and app level)
- `PlayStreakApplication.kt` or main activity
- Key activity classes for event tracking
- Privacy policy documentation
- ProGuard rules (if needed)

**Testing Notes:**
- Test on both debug and release builds
- Verify events appear in Firebase console within 24 hours
- Test crash reporting with actual crashes (not just exceptions)
- Validate custom parameters and user properties
- Ensure privacy policy accurately reflects data collection

**Post-Implementation:**
- Monitor Firebase console for crash reports and analytics
- Set up alerting for critical crashes or performance issues
- Regular review of user engagement metrics
- Use data for prioritizing future feature development

**Implementation Summary:**
Firebase Crashlytics and Analytics have been successfully integrated and are working end-to-end. The implementation includes:

**Completed Integration:**
- Firebase project configured with PlayStreak Android app
- Firebase BOM 33.6.0 integrated with Crashlytics (19.3.0) and Analytics (22.1.2) SDKs
- Google Services plugin (4.4.5) and Crashlytics plugin (3.0.2) properly configured
- google-services.json configuration file added to project

**Working Features:**
- Crash reporting: Test crashes successfully reported to Firebase console
- Analytics tracking: Key events implemented and verified working
  - App opens (automatic)
  - Activity logging with type, piece, and duration parameters
  - Streak achievements with length and emoji level tracking
  - Piece additions with type and count metrics
  - CSV operations with success/failure tracking
- Custom crash keys: User type (pro/free), piece count, and action logging
- Firebase console dashboard accessible and receiving data

**Technical Implementation:**
- Analytics initialized in PlayStreakApplication with proper error handling
- Event tracking integrated throughout ActivityRepository, DashboardFragment, and PieceRepository
- Crashlytics enabled with custom key collection and user context
- Build configuration supports both debug and release variants
- Privacy policy updated to reflect Firebase data collection practices

**Verification Status:**
All acceptance criteria have been met and the feature is fully operational. Analytics events are being received in the Firebase console and crash reporting has been verified through testing. The implementation is ready for production release.

---

### Feature #37: Reduce Debug Logging for Performance Suggestions
**Status:** ❌ Moved to Ticket #3
**Date Requested:** 2025-07-23
**Date Moved:** 2025-07-24
**Priority:** Low (Code Quality)
**Requested By:** Development Team

**Description:**
Remove or reduce excessive debug logging for performance suggestions in DashboardFragment that is cluttering the log output and potentially impacting performance. The current implementation logs detailed performance suggestion information on every dashboard update, creating verbose and repetitive log entries.

**User Story:**
As a developer debugging the app, I want cleaner log output without excessive performance suggestion debug messages so that I can focus on relevant debugging information and reduce log noise during development and testing.

**Current Issue:**
The DashboardFragment currently outputs verbose logging like:
```
D/DashboardFragment: Performance suggestions: 4
D/DashboardFragment: Performance Suggestion: Don't Cry Out Loud - Type: PERFORMANCE - Reason: ⭐ 12 practices, never performed
D/DashboardFragment: Performance Suggestion: On My Own - Type: PERFORMANCE - Reason: 8 practices, last performance 35 days ago
[... repeated multiple times per dashboard refresh]
```

**Acceptance Criteria:**
- [ ] Remove or significantly reduce debug logging for performance suggestions
- [ ] Keep essential error logging for debugging actual issues
- [ ] Maintain logging for critical suggestion calculation failures
- [ ] Ensure no performance impact from excessive logging
- [ ] Consider adding conditional debug logging (debug builds only)

**Implementation Options:**

**Option 1: Remove Debug Logging (Recommended)**
- Remove all debug log statements for performance suggestions
- Keep only error/warning logs for actual issues
- Clean up log output completely

**Option 2: Conditional Debug Logging**
- Wrap debug logs in BuildConfig.DEBUG checks
- Only log in debug builds, not release builds
- Reduce frequency (e.g., log only on significant changes)

**Option 3: Reduce Logging Verbosity**
- Log only summary information (count of suggestions)
- Remove individual suggestion detail logging
- Keep high-level information for debugging

**Files to Modify:**
- `/app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardFragment.kt`
- Any other files with performance suggestion logging

**Benefits:**
- Cleaner log output for development and debugging
- Reduced log noise when testing Firebase Analytics
- Better performance (less string formatting and I/O)
- More professional codebase ready for release

**Technical Implementation:**
```kotlin
// Current (verbose):
android.util.Log.d("DashboardFragment", "Performance suggestions: ${suggestions.size}")
suggestions.forEach { suggestion ->
    android.util.Log.d("DashboardFragment", "Performance Suggestion: ${suggestion.name} - Type: ${suggestion.type} - Reason: ${suggestion.reason}")
}

// Option 1 (remove):
// Remove all debug logging

// Option 2 (conditional):
if (BuildConfig.DEBUG) {
    android.util.Log.d("DashboardFragment", "Performance suggestions: ${suggestions.size}")
}

// Option 3 (summary only):
android.util.Log.d("DashboardFragment", "Loaded ${suggestions.size} performance suggestions")
```

**Priority Justification:**
Low priority as this is a code quality issue that doesn't affect functionality, but it improves development experience and prepares the codebase for release by removing debug noise.

---

### Priority Levels
- **Critical** - Essential for app functionality or user safety
- **High** - Significant user value and feasible with current resources
- **Medium** - Good user value but lower priority than high items
- **Low** - Nice-to-have features that enhance experience
- **Future** - Aspirational features requiring significant planning or resources

### Technical Standards
- Follow existing code architecture and patterns
- Maintain consistency with current UI/UX design
- Ensure accessibility compliance
- Include comprehensive testing
- Document all new APIs and components