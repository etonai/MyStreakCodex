# MyStreak

MyStreak is an Android app for tracking general task activity, daily streaks, and high-priority task follow-through. It is organized around Tasks, Activities, success levels, active/inactive task status, and calendar-based accountability.

The app is built natively for Android using Kotlin, Room (SQLite), MVVM architecture, AndroidX Navigation, and Material Design components.

**Current Status:** Active MyStreak implementation.

**Primary Development Plan:** See [`app/docs/DevCycle_2026_001.md`](app/docs/DevCycle_2026_001.md).

**Import/Export Planning:** Import/export work has been split into [`app/docs/DevCycle_2026_002.md`](app/docs/DevCycle_2026_002.md).

## What MyStreak Tracks

MyStreak is organized around three core ideas:

- **Tasks:** General activities you want to keep doing.
- **Activities:** A timestamped log that you completed a Task at a Minimum, Medium, or High success level.
- **Streaks and calendar color:** Daily activity keeps your streak alive, while high-priority completion affects the calendar color for each day.

Tasks can be active or inactive. Inactive Tasks remain visible for history, but are hidden from logging and high-priority outstanding lists.

## Current Features

### Dashboard

- Current streak.
- Today's Activities, sorted by time.
- Yesterday's Activities, sorted by time.
- Rolling 7-day summary.
- High-priority Tasks still outstanding today.
- Add, edit, and delete Activity entry points.

### Calendar

- Monthly calendar view.
- MyStreak color rules:
  - no color for no Activity
  - light blue for any Activity
  - medium blue for at least one high-priority Activity
  - dark blue for at least half of active high-priority Tasks
  - green for all active high-priority Tasks
- Past-day colors are frozen after midnight and intentionally do not recalculate after retroactive edits.
- Day detail list shows actual Activities for the selected date.

### Tasks

- Active and inactive Tasks.
- Task name, color, priority, active status, total Activity count, today's Activity count, and last Activity date.
- Add/Edit Task fields:
  - name
  - predefined color
  - High/Low priority
  - Minimum/Medium/High success descriptions
  - active/inactive status
- Delete Task with warning that historical Activities will also be removed.

### Activity Logging

- Log an Activity by choosing an active Task and selecting Minimum, Medium, or High success.
- Task-specific threshold descriptions are shown during logging.
- Edit success level and date/time.
- Future timestamps are rejected.
- Task association stays fixed during Activity edits.

## Deferred Work

The following areas are intentionally still being converted or deferred:

- Import/export for MyStreak JSON is planned in `DevCycle_2026_002.md`.
- Package names still use the `playstreak` namespace until the application ID migration is planned.
- Some legacy screens/classes may remain in source while being removed from reachable MyStreak navigation.
- Automated tests for the new MyStreak business rules are still being expanded.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android |
| UI | Material Design Components, AndroidX Fragments |
| Architecture | MVVM, ViewModel, LiveData, Repository |
| Database | Room (SQLite) |
| Navigation | AndroidX Navigation Component |
| Calendar | Kizitonwose CalendarView |
| Build | Gradle |
| Testing | JUnit, Espresso |

## Project Structure

```text
app/src/main/java/com/pseddev/mystreak/
|-- data/
|   |-- entities/      # Room entities for Tasks, Activities, calendar state, etc.
|   |-- daos/          # Data Access Objects
|   `-- repository/    # Repository/business logic
|-- ui/
|   |-- addactivity/   # Activity logging and edit flow
|   |-- pieces/        # Add Task flow, still using some legacy names
|   |-- progress/      # Dashboard, Calendar, Tasks tab UI
|   `-- sync/          # Legacy sync/import-export related UI
`-- utils/             # Utility classes
```

## Getting Started

### Prerequisites

- Android Studio
- Android SDK configured locally
- JDK compatible with the Gradle wrapper

### Build

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
./gradlew assembleDebug
```

## Development Notes

- The current local database name is `mystreak_database`.
- Debug builds are configured to avoid Firebase startup crashes when using local placeholder Firebase configuration.
- MyStreak currently defaults to dark mode.
- The conversion history is documented in `app/docs/DevCycle_2026_001.md`.

## License

Copyright (c) 2025.

This software is proprietary and confidential. See the [COPYRIGHT](COPYRIGHT) file for full licensing terms.

## Acknowledgments

MyStreak development has been AI-assisted, with human review and product direction guiding the product.

Key libraries and tools include:

- Kizitonwose CalendarView
- Material Design Components
- AndroidX Libraries
- Room
- Gradle
