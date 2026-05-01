# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MyStreak is an Android application written in Kotlin using the modern Android development stack. The project uses Gradle with Kotlin DSL for build configuration and follows standard Android project structure.

## Development Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
# Unit tests (local JVM)
./gradlew test

# Instrumented tests (requires Android device/emulator)
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Installing the App
```bash
# Debug build
./gradlew installDebug

# Release build
./gradlew installRelease
```

### Cleaning the Project
```bash
./gradlew clean
```

### Linting
```bash
./gradlew lint
```

## Project Structure

- **Package**: `com.pseddev.mystreak`
- **Namespace**: `com.pseddev.mystreak`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36

### Key Directories
- `app/src/main/java/com/pseddev/mystreak/` - Main application source code
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented Android tests
- `app/src/main/res/` - Android resources (layouts, strings, drawables, etc.)

## Build Configuration

The project uses:
- **Gradle Version Catalog** (`gradle/libs.versions.toml`) for dependency management
- **Kotlin DSL** for build scripts
- **Android Gradle Plugin** 8.11.1
- **Kotlin** 2.0.21
- **Java 11** compatibility

### Key Dependencies
- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- JUnit for unit testing
- Espresso for UI testing

## Development Notes

- MyStreak tracks task activity, daily streaks, and calendar-based completion state.
- The package/application ID migration is separate from product naming and is not yet complete.
- The project follows modern Android development practices with Kotlin and AndroidX libraries.

## Development Cycle Rules

**CRITICAL**: Follow all rules and processes defined in `app/docs/DevCycles_overview.md`, including:
- **NEVER immediately implement** when adding a new phase to a DevCycle
- Add new phases to documentation first with "Open" status
- Wait for explicit approval or further direction before beginning implementation
- Allow time for phase planning, refinement, and clarification
- Follow all cycle management processes and status progression rules

## Git Workflow Rules

**IMPORTANT**: Do not commit any changes to git until the user has:
1. Confirmed that the implementation works correctly
2. Explicitly given permission to commit the changes

Wait for user confirmation before using `git commit` commands.

## Feature, Bug Fix, and Ticket Workflow

### Status Progression Rules

**For Features** (documented in `app/docs/features.md`):
1. **💡 Requested** → **🔄 In Progress**: Mark when starting work
2. **🔄 In Progress** → **🔍 Verifying**: Mark when code changes are complete
3. **🔍 Verifying** → **✅ Implemented**: Mark ONLY after user verification

**For Bugs** (documented in `app/docs/bugs.md`):
1. **🐛 Open** → **🔄 In Progress**: Mark when starting work
2. **🔄 In Progress** → **🔍 Verifying**: Mark when fix is complete
3. **🔍 Verifying** → **✅ Fixed**: Mark ONLY after user verification

**For Tickets** (documented in `app/docs/tickets.md`):
1. **🎫 Open** → **🔄 In Progress**: Mark when starting work
2. **🔄 In Progress** → **🔍 In Verification**: Mark when implementation is complete
3. **🔍 In Verification** → **✅ Completed**: Mark ONLY after user verification

**For DevCycle Phases** (documented in `app/docs/DevCycle_*.md`):
1. **🎫 Open** → **🔄 In Progress**: Mark when starting work
2. **🔄 In Progress** → **🔍 IN VERIFICATION**: Mark when implementation is complete
3. **🔍 IN VERIFICATION** → **✅ COMPLETED**: Mark ONLY after user verification

### Critical Rules

🚨 **NEVER** mark any item as complete/implemented/fixed/COMPLETED until the user has:
- Tested the implementation
- Confirmed it works as expected
- Explicitly approved the work

🛑 **STOP AND ASK YOURSELF BEFORE MARKING COMPLETE:**
- "Has the user tested this?"
- "Did the user explicitly say it works?"
- "Did I wait for their approval?"

If any answer is NO, use verification status instead.

⚠️ **CRITICAL REMINDER FOR DEVCYCLE PHASES:**
DevCycle phases should NEVER be marked as "✅ COMPLETED" just because code implementation is finished. Code implementation completion only means the phase moves to "🔍 IN VERIFICATION" status. Only mark as "✅ COMPLETED" after:
1. User has built/tested the implementation
2. User confirms functionality works as expected
3. User explicitly approves the work

**Remember:** Implementation ≠ Completion. Completion = Implementation + User Verification + User Approval.

### Correct Procedure
1. Complete the code changes
2. Update documentation status to verification status ("🔍 Verifying", "🔍 Verifying", or "🔍 In Verification")
3. Mark relevant acceptance criteria as completed
4. **WAIT** for user verification
5. Only mark as complete after user confirmation

This prevents premature marking of incomplete or unverified work across all documentation systems.

### Verification Request Template

When implementation is complete, use this template:

```
## 🔍 READY FOR VERIFICATION

**What I implemented:**
- [Brief description of changes]

**Files modified:**
- [List of files]

**What you need to verify:**
1. [Specific test steps]
2. [Expected behavior]
3. [Confirmation needed]

**Status:** 🔍 IN VERIFICATION (awaiting your approval)
```

Only mark as complete after user says "approved", "works", "looks good", etc.
