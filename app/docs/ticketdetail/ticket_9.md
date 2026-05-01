# Ticket #9: Independently Store Piece Information

**Status:** 🎫 Open
**Date Created:** 2025-07-26
**Priority:** High
**Type:** Feature Enhancement

## Problem Statement

Currently, piece information is derived by querying the activities database every time we need piece statistics. This approach requires expensive database queries to calculate:
- Total practice count per piece
- Total performance count per piece
- Last practice date per piece
- Last performance date per piece
- Practice frequency and patterns

This results in frequent, complex queries that impact performance, especially as the activities database grows.

## Proposed Solution

Create a separate piece information storage system that maintains aggregated statistics independently from the activities table. This will:

1. **Store piece metadata directly** instead of calculating from activities
2. **Update statistics incrementally** when activities are added/modified
3. **Reduce database query overhead** for piece-related operations
4. **Improve app performance** especially for suggestions and piece lists

## Technical Implementation

### Database Schema Changes

**New/Enhanced Piece Entity Fields:**
```kotlin
@Entity(tableName = "pieces")
data class Piece(
    @PrimaryKey val id: String,
    val name: String,
    val type: ItemType,
    val dateCreated: Long,

    // New independent statistics fields
    val practiceCount: Int = 0,
    val performanceCount: Int = 0,
    val lastPracticeDate: Long? = null,
    val lastPerformanceDate: Long? = null,
    val totalPracticeDuration: Int = 0, // in minutes
    val averagePracticeLevel: Float? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

### Migration Strategy

**Phase 1: Data Migration**
1. Create new piece statistics table/fields
2. Run one-time migration to populate statistics from existing activities
3. Validate data integrity between old queries and new stored data

**Phase 2: Update Logic**
1. Modify activity insertion/update logic to update piece statistics
2. Ensure piece statistics are updated whenever:
   - New activity is added
   - Existing activity is modified
   - Activity is deleted
   - Piece is deleted

**Phase 3: Query Optimization**
1. Replace activity-based queries with direct piece statistics queries
2. Update ViewModels to use new efficient queries
3. Maintain backward compatibility during transition

### Implementation Areas

**Files to Modify:**
- `app/src/main/java/com/pseddev/mystreak/data/entities/Piece.kt`
- `app/src/main/java/com/pseddev/mystreak/data/dao/PieceDao.kt`
- `app/src/main/java/com/pseddev/mystreak/data/dao/ActivityDao.kt`
- `app/src/main/java/com/pseddev/mystreak/data/AppDatabase.kt` (migration)
- `app/src/main/java/com/pseddev/mystreak/data/repositories/ActivityRepository.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/progress/DashboardViewModel.kt`
- `app/src/main/java/com/pseddev/mystreak/ui/pieces/PiecesViewModel.kt`

**Key Operations to Optimize:**
- Piece list display with last activity dates
- Practice suggestions based on piece history
- Dashboard statistics and recommendations
- Timeline filtering by piece statistics

## Expected Benefits

### Performance Improvements
- **Reduced query complexity**: Simple SELECT operations instead of complex JOINs and aggregations
- **Faster piece list loading**: Direct statistics retrieval instead of calculating per piece
- **Improved suggestions performance**: Pre-calculated last activity dates
- **Scalable architecture**: Performance remains consistent as activities database grows

### Data Consistency
- **Single source of truth**: Piece statistics maintained independently
- **Real-time updates**: Statistics updated immediately when activities change
- **Reduced calculation errors**: No risk of inconsistent aggregation logic

## Implementation Phases

### Phase 1: Database Schema (1-2 days)
- [ ] Design new piece statistics schema
- [ ] Create database migration
- [ ] Implement data population from existing activities
- [ ] Add validation to ensure data integrity

### Phase 2: Update Logic (2-3 days)
- [ ] Modify activity insertion to update piece statistics
- [ ] Handle activity updates and deletions
- [ ] Add piece deletion cleanup
- [ ] Implement rollback/repair mechanisms for data consistency

### Phase 3: Query Optimization (2-3 days)
- [ ] Replace activity-based queries in ViewModels
- [ ] Update Repository classes to use new queries
- [ ] Optimize piece list and suggestions performance
- [ ] Performance testing and validation

### Phase 4: Testing & Validation (1-2 days)
- [ ] Unit tests for statistics update logic
- [ ] Integration tests for migration process
- [ ] Performance benchmarking before/after
- [ ] User acceptance testing

## Success Criteria

1. **Performance**: Piece-related queries execute in <50ms (down from current 200ms+)
2. **Accuracy**: Statistics match previous activity-based calculations with 100% consistency
3. **Reliability**: Statistics remain accurate through all activity CRUD operations
4. **Maintainability**: Clean separation between transactional activities and aggregated statistics

## Risks and Mitigation

**Risk**: Data inconsistency between activities and piece statistics
**Mitigation**: Implement validation queries and repair mechanisms

**Risk**: Complex migration process
**Mitigation**: Phased rollout with rollback capability and extensive testing

**Risk**: Increased storage requirements
**Mitigation**: Statistics data is minimal compared to benefits, monitored during implementation

## Open Questions

**Q1: Favorites Storage Strategy**
Should we store favorite piece information separately (dedicated favorites table) or keep favorite status as a field within the pieces table?

*Considerations:*
- Separate table: More normalized, easier to query favorites independently
- Piece field: Simpler schema, fewer joins, but piece table becomes larger

**Q2: Import/Export Strategy**
Should we import/export piece statistics information separately from activities, or bundle them together?

*Considerations:*
- Separate export: Clean data separation, user choice of what to backup
- Bundled export: Simpler user experience, ensures data consistency
- Hybrid approach: Main export includes everything, option for separate exports

**Q3: Export Format for Statistics**
Should we export piece statistics data as JSON or CSV format?

*Considerations:*
- JSON: Better for complex nested data, preserves data types, more flexible
- CSV: User-friendly for spreadsheet analysis, consistent with current export format
- Both formats: Maximum compatibility but more implementation complexity

## Implementation Notes

**Recent Activity Storage Enhancement**
We should probably store the last 3 performances and last 3 practices for each piece. This can help with suggestions by providing more granular recent activity data instead of just the single last activity timestamp.

*Benefits:*
- More sophisticated suggestion algorithms based on recent activity patterns
- Better detection of pieces that need attention (no recent activity vs. sporadic activity)
- Improved practice recommendations based on frequency and recency trends

## Future Enhancements

Once core implementation is complete, this foundation enables:
- Advanced practice analytics and trends
- Predictive practice suggestions
- Performance tracking over time
- Custom piece organization and filtering
- **Activity Database Trimming**: This feature allows us to trim old activities while still keeping the most pertinent information (the count of performances and practices for that piece), enabling long-term data management without losing essential statistics