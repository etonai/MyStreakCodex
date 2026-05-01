# PlayStreak Product Specification (As Built)

**Version:** 1.1.0
**Date:** July 22, 2025
**Purpose:** Accurate specification of PlayStreak as currently implemented
**Author:** Claude Code Assistant (Opus model)

> **Note:** This document is a revision of the original `product-specification.md`, created to accurately describe PlayStreak as actually built. The original specification contained many unimplemented features and should be considered a vision document rather than an accurate product specification.

## Overview

PlayStreak is a personal music practice tracking application for Android that helps individual musicians maintain consistent practice habits through streak tracking, practice suggestions, and activity logging. The app offers both free and premium tiers with clearly differentiated features.

## Table of Contents

1. [Core Functionality](#core-functionality)
2. [User Interface Structure](#user-interface-structure)
3. [Feature Specifications](#feature-specifications)
4. [Data Model](#data-model)
5. [Free vs Pro Features](#free-vs-pro-features)
6. [Technical Implementation](#technical-implementation)

---

## Core Functionality

### What PlayStreak Does

1. **Track Practice Activities**
   - Log practice sessions with duration
   - Log performances without duration
   - Add notes to any activity
   - Select practice level (1-4) or performance level (1-3)

2. **Manage Repertoire**
   - Add pieces (musical compositions)
   - Add techniques (exercises, scales, etc.)
   - Mark items as favorites
   - View practice history per piece

3. **Visualize Progress**
   - Calculate and display current practice streak
   - Show calendar heat map of practice patterns
   - Display timeline of all activities
   - Generate practice suggestions

4. **Data Management**
   - Export all data to CSV format
   - Import data from CSV (Pro only)
   - Local data storage
   - Google Drive sync (implemented but currently disabled)

### What PlayStreak Does NOT Do

- Multi-user functionality
- Teacher-student connections
- Social features or sharing
- Analytics beyond basic counts
- Batch operations
- Import from multiple sources
- Audio/video recording
- Practice timers
- Notifications or reminders

---

## User Interface Structure

### Navigation

The app uses bottom navigation with 5-6 tabs:

1. **Dashboard** - Main screen with streak, summary, and suggestions
2. **Calendar** - Monthly view with practice indicators
3. **Suggestions** - List of pieces to practice
4. **Pieces** - All pieces and techniques
5. **Timeline** - Chronological activity history
6. **Inactive** - Pieces not recently practiced (Pro only)

Settings are accessed from the Dashboard tab.

### Screen Specifications

#### Dashboard
- Large streak counter (number of consecutive days)
- Week summary text
- Practice suggestions (1 favorite + 2 non-favorites for Free, 4+4 for Pro)
- Add Activity button
- Settings button

#### Calendar
- Month/Year header with Previous/Next buttons
- Grid of days with practice indicators
- Free: Binary (white = no practice, light blue = practice)
- Pro: Intensity heat map (6 colors based on activity count)
- Tap day to see activities
- Swipe to change months is disabled

#### Suggestions
- List of suggested pieces to practice
- Each shows piece name and reason (e.g., "Last practice 5 days ago")
- Favorite star indicator
- Pro: "+" button for quick add

#### Pieces
- Sortable list (Alphabetical, Date, Activity count)
- Each piece shows name, activity count, last practice date
- Favorite star (toggleable)
- Pro: "+" button for quick add
- Extended FAB: "Add Piece"
- Tap piece for detailed view

#### Timeline
- Reverse chronological list of all activities
- Shows date, piece name, type, level, duration
- Swipe to delete
- Tap to view/edit details

#### Inactive (Pro only)
- List of pieces not practiced in 30+ days
- Similar layout to Pieces tab

#### Settings
- App title ("PlayStreak" or "PlayStreak Pro")
- Add Activity button
- Add Piece button (shows count)
- Manage Favorites button (shows count)
- Import/Export Data button
- Version number
- Pro/Free toggle (debug builds only)

---

## Feature Specifications

### Adding Activities

**Process:**
1. Tap "Add Activity" from Dashboard or Settings
2. Choose Practice or Performance
3. Select existing piece or add new one
4. Select level:
   - Practice: 1-4 (Essentials, Incomplete, Complete with Review, Perfect Complete)
   - Performance: 1-3 (Failed, Unsatisfactory, Satisfactory)
5. For Practice: Enter duration in minutes
6. For Performance: Select type (Online/Live)
7. Optional: Add notes
8. Save

### Managing Pieces

**Add Piece:**
1. Tap "Add Piece" from Settings or Pieces tab
2. Enter name
3. Select type (Piece or Technique)
4. Save

**Edit Piece:**
- Available through piece detail view
- Can change name and type

**Favorite System:**
- Free users: Maximum 4 favorites
- Pro users: Unlimited favorites
- Toggle via star icon

### Practice Suggestions Algorithm

**Logic:**
- Favorites not practiced in 2+ days
- Non-favorites practiced 7-31 days ago
- Never shows pieces practiced recently
- Falls back to least recently practiced if no matches

**Display:**
- Free: 1 favorite + 2 non-favorites
- Pro: Up to 4 favorites + 4 non-favorites

### Data Import/Export

**Export (All users):**
- Exports all activities to CSV
- Includes: Date, Time, Piece, Type, Level, Duration, Notes
- File saved to device storage

**Import (Pro only):**
- Reads CSV files with practice data
- Basic field mapping
- Replaces all existing data
- Shows warning before import

### Google Drive Sync

**Status:** Implemented but disabled
- UI shows "Google Drive functionality coming soon"
- Code exists for authentication and sync
- Connect/Disconnect functionality ready
- Sync to Drive / Restore from Drive options

---

## Data Model

### Database Structure

**Pieces Table:**
```
- id (primary key)
- name (text)
- type (PIECE or TECHNIQUE)
- isFavorite (boolean)
```

**Activities Table:**
```
- id (primary key)
- pieceOrTechniqueId (foreign key)
- activityType (PRACTICE or PERFORMANCE)
- timestamp (datetime)
- level (integer)
- performanceType (text)
- minutes (integer, -1 for performances)
- notes (text)
```

### Data Constraints

- Piece names are normalized for consistency
- Activities linked to pieces via foreign key
- No orphaned activities allowed
- Timestamps stored in milliseconds

---

## Free vs Pro Features

### Free Tier Includes

**Core Features:**
- Add unlimited activities
- Add unlimited pieces
- Track practice streak
- View timeline
- Basic calendar (binary heat map)
- Export to CSV
- Up to 4 favorites
- 3 practice suggestions daily

**Limits:**
- 4 favorites maximum
- 1 favorite + 2 non-favorite suggestions
- Binary calendar heat map
- No CSV import
- No Inactive tab
- No quick-add buttons

### Pro Tier Adds

**Enhanced Features:**
- Unlimited favorites
- Advanced calendar heat map (6 intensity levels)
- Up to 8 suggestions (4+4)
- Inactive pieces tab
- Quick-add "+" buttons
- CSV import functionality

**Pro Identification:**
- "Pro" badge in Settings title
- BuildConfig.DEBUG shows toggle button
- SharedPreferences stores Pro status

### Feature Gating Implementation

```kotlin
ProUserManager.getInstance(context).isProUser()
```

Used to conditionally:
- Show/hide UI elements
- Enable/disable features
- Adjust limits
- Modify tab count

---

## Technical Implementation

### Architecture

**Pattern:** MVVM (Model-View-ViewModel)
- Fragments with ViewBinding
- ViewModels with ViewModelFactory
- Repository pattern for data access
- Room database for persistence

**Key Components:**
- `PianoRepository` - Single repository for all data
- `ProUserManager` - Singleton for Pro status
- `TextNormalizer` - Handles Unicode normalization
- `CsvHandler` - Import/export functionality

### Dependencies

**Core:**
- Kotlin 2.0.21
- AndroidX libraries
- Room 2.6.1
- Navigation Component 2.7.6
- Material Design Components

**Additional:**
- Google Drive API (disabled)
- OpenCSV for file handling

### Key Classes

**Data:**
- `Activity` - Practice/performance record
- `PieceOrTechnique` - Musical item
- `ActivityDao` / `PieceOrTechniqueDao` - Database access

**UI:**
- `ViewProgressFragment` - Main dashboard
- `CalendarFragment` - Calendar view
- `SuggestionsFragment` - Practice suggestions
- `AddActivityFragment` - Activity creation flow

**Utilities:**
- `StreakCalculator` - Calculates consecutive days
- `GoogleDriveHelper` - Drive sync (disabled)
- `DateUtils` - Date formatting helpers

### Build Configuration

- Min SDK: 24 (Android 7.0)
- Target SDK: 36
- Package: `com.pseddev.mystreak`
- Debug builds: Pro mode default
- Release builds: Free mode default

---

## Summary

PlayStreak is a focused, single-purpose application that does one thing well: help individual musicians track their practice habits. It avoids feature bloat while providing meaningful differentiation between free and premium tiers. The implementation is clean, maintainable, and ready for future enhancements when needed.

The app successfully balances simplicity for casual users with enough depth for serious musicians, all while maintaining a sustainable business model through its Pro tier.

---

*End of Product Specification (As Built)*