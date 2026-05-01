package com.pseddev.mystreak.ui.progress

import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.utils.ProUserManager
import java.util.*

/**
 * Centralized service for generating practice and performance suggestions.
 * This consolidates the suggestion algorithms used by both DashboardViewModel and SuggestionsViewModel
 * to ensure consistency and eliminate code duplication.
 */
class SuggestionsService(
    private val proUserManager: ProUserManager
) {

    private val now = System.currentTimeMillis()
    private val fourteenHoursAgo = now - (14 * 60 * 60 * 1000L)
    private val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)
    private val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    private val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
    private val twentyEightDaysAgo = now - (28 * 24 * 60 * 60 * 1000L)

    // Calculate start of today (midnight) to exclude pieces practiced today
    private val startOfToday = run {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    /**
     * Generate all suggestions (practice + performance) for the given pieces and activities.
     * This is the main entry point used by both ViewModels.
     */
    fun generateAllSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        val practiceSuggestions = generatePracticeSuggestions(pieces, activities)
        val performanceSuggestions = generatePerformanceSuggestions(pieces, activities)

        if (BuildConfig.DEBUG) {
            // DEBUG: Log practice suggestions before combining
            android.util.Log.d("Phase4Debug74", "=== PRACTICE SUGGESTIONS BEFORE COMBINING ===")
            practiceSuggestions.forEachIndexed { index, suggestion ->
                android.util.Log.d("Phase4Debug75", "Practice[$index] ${suggestion.piece.name} | favorite=${suggestion.piece.isFavorite} | reason=${suggestion.suggestionReason}")
            }

            // DEBUG: Log performance suggestions before combining
            android.util.Log.d("Phase4Debug76", "=== PERFORMANCE SUGGESTIONS BEFORE COMBINING ===")
            performanceSuggestions.forEachIndexed { index, suggestion ->
                android.util.Log.d("Phase4Debug77", "Performance[$index] ${suggestion.piece.name} | favorite=${suggestion.piece.isFavorite} | reason=${suggestion.suggestionReason}")
            }
        }

        // Combine practice and performance suggestions while respecting favorite limits
        val allSuggestions = practiceSuggestions + performanceSuggestions

        // Apply favorite limits across all suggestion types
        val finalFavoriteLimit = if (proUserManager.isProUser())
            ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS
            else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
        val finalNonFavoriteLimit = if (proUserManager.isProUser())
            ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
            else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
        val favoriteSuggestionsInAll = allSuggestions.filter { it.piece.isFavorite }
        val nonFavoriteSuggestionsInAll = allSuggestions.filter { !it.piece.isFavorite }

        if (BuildConfig.DEBUG) {
            // DEBUG: Log the limits and counts in generateAllSuggestions
            android.util.Log.d("Phase4Debug55", "=== generateAllSuggestions() LIMITING LOGIC ===")
            android.util.Log.d("Phase4Debug56", "finalFavoriteLimit: $finalFavoriteLimit")
            android.util.Log.d("Phase4Debug57", "finalNonFavoriteLimit: $finalNonFavoriteLimit")
            android.util.Log.d("Phase4Debug58", "favoriteSuggestionsInAll.size: ${favoriteSuggestionsInAll.size}")
            android.util.Log.d("Phase4Debug59", "nonFavoriteSuggestionsInAll.size: ${nonFavoriteSuggestionsInAll.size}")

            // DEBUG: Log all favorite suggestions to check ordering
            android.util.Log.d("Phase4Debug70", "=== ALL FAVORITE SUGGESTIONS IN ORDER ===")
            favoriteSuggestionsInAll.forEachIndexed { index, suggestion ->
                android.util.Log.d("Phase4Debug71", "Fav[$index] ${suggestion.piece.name} | type=${suggestion.suggestionType} | reason=${suggestion.suggestionReason}")
            }

            // DEBUG: Log all non-favorite suggestions to check ordering
            android.util.Log.d("Phase4Debug72", "=== ALL NON-FAVORITE SUGGESTIONS IN ORDER ===")
            nonFavoriteSuggestionsInAll.forEachIndexed { index, suggestion ->
                android.util.Log.d("Phase4Debug73", "NonFav[$index] ${suggestion.piece.name} | type=${suggestion.suggestionType} | reason=${suggestion.suggestionReason}")
            }
        }

        // Limit favorites to user limit and preserve order
        val limitedFavorites = favoriteSuggestionsInAll.take(finalFavoriteLimit)
        val limitedNonFavorites = nonFavoriteSuggestionsInAll.take(finalNonFavoriteLimit)

        return limitedFavorites + limitedNonFavorites
    }

    /**
     * Generate performance suggestions for Pro users only.
     * Returns empty list for non-Pro users.
     */
    fun generatePerformanceSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        if (BuildConfig.DEBUG) {
            // DEBUG: Log start of performance suggestions generation
            android.util.Log.d("Phase4Debug60", "=== generatePerformanceSuggestions() START ===")
            android.util.Log.d("Phase4Debug61", "Pro user: ${proUserManager.isProUser()}")
        }

        if (!proUserManager.isProUser()) {
            if (BuildConfig.DEBUG) {
                android.util.Log.d("Phase4Debug62", "Not pro user, returning empty list")
            }
            return emptyList()
        }

        if (BuildConfig.DEBUG) {
            android.util.Log.d("Phase4Debug63", "Total pieces: ${pieces.filter { it.type == ItemType.PIECE }.size}")
        }

        val pieceActivities = activities.groupBy { it.pieceOrTechniqueId }
        val performanceActivities = activities.filter { it.activityType == ActivityType.PERFORMANCE }
        val piecePerformanceActivities = performanceActivities.groupBy { it.pieceOrTechniqueId }

        val firstTierSuggestions = mutableListOf<SuggestionItem>()
        val secondTierSuggestions = mutableListOf<SuggestionItem>()

        pieces.filter { it.type == ItemType.PIECE }.forEach { piece ->
            val allPieceActivities = pieceActivities[piece.id] ?: emptyList()
            val piecePerformances = piecePerformanceActivities[piece.id] ?: emptyList()

            // Get practice activities in last 28 days
            val recentPractices = allPieceActivities.filter {
                it.activityType == ActivityType.PRACTICE && it.timestamp >= twentyEightDaysAgo
            }

            // Check if practiced at least 3 times in last 28 days AND has at least one Level 4 practice
            val hasLevel4Practice = recentPractices.any { it.level == 4 }
            if (recentPractices.size >= 3 && hasLevel4Practice) {
                // Use piece fields for last activity dates instead of expensive database queries
                val lastPerformanceDate = piece.lastPerformanceDate
                val lastPracticeDate = piece.lastPracticeDate

                val daysSinceLastPerformance = if (lastPerformanceDate != null) {
                    ((now - lastPerformanceDate) / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    Int.MAX_VALUE
                }

                val daysSinceLastPractice = if (lastPracticeDate != null) {
                    ((now - lastPracticeDate) / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    Int.MAX_VALUE
                }

                // First tier: Practiced ≥3 times in 28 days BUT not performed in 28 days
                if (lastPerformanceDate == null || lastPerformanceDate < twentyEightDaysAgo) {
                    val favoritePrefix = if (piece.isFavorite) "⭐ " else ""
                    firstTierSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastPracticeDate, // Use practice date for sorting
                            daysSinceLastActivity = daysSinceLastPractice,
                            suggestionReason = if (lastPerformanceDate == null) {
                                "${favoritePrefix}${recentPractices.size} practices, never performed"
                            } else {
                                val hoursElapsed = ((now - lastPerformanceDate) / (60 * 60 * 1000)).toInt()
                                if (hoursElapsed < 48) {
                                    val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                    "${favoritePrefix}${recentPractices.size} practices, last performance $hoursElapsed $hourText ago"
                                } else {
                                    val dayText = if (daysSinceLastPerformance == 1) "day" else "days"
                                    "${favoritePrefix}${recentPractices.size} practices, last performance $daysSinceLastPerformance $dayText ago"
                                }
                            },
                            suggestionType = SuggestionType.PERFORMANCE
                        )
                    )
                } else if (lastPerformanceDate!! < startOfToday) {
                    // Second tier: Practiced ≥3 times in 28 days AND performed in 28 days BUT not today
                    val favoritePrefix = if (piece.isFavorite) "⭐ " else ""
                    secondTierSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastPerformanceDate, // Use performance date for sorting
                            daysSinceLastActivity = daysSinceLastPerformance,
                            suggestionReason = {
                                val hoursElapsed = ((now - lastPerformanceDate) / (60 * 60 * 1000)).toInt()
                                if (hoursElapsed < 48) {
                                    val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                    "${favoritePrefix}${recentPractices.size} practices, last performance $hoursElapsed $hourText ago"
                                } else {
                                    val dayText = if (daysSinceLastPerformance == 1) "day" else "days"
                                    "${favoritePrefix}${recentPractices.size} practices, last performance $daysSinceLastPerformance $dayText ago"
                                }
                            }(),
                            suggestionType = SuggestionType.PERFORMANCE
                        )
                    )
                }
            }
        }

        // Sort first tier by most recent practice (lowest daysSinceLastPractice)
        val sortedFirstTier = firstTierSuggestions.sortedWith(
            compareBy<SuggestionItem> { it.daysSinceLastActivity } // Most recent practice first
            .thenBy { it.piece.name.lowercase() }
        )

        // Sort second tier by least recent performance (highest daysSinceLastPerformance)
        // For ties, prioritize pieces with more practices
        val sortedSecondTier = secondTierSuggestions.sortedWith(
            compareByDescending<SuggestionItem> { it.daysSinceLastActivity } // Least recent performance first
            .thenByDescending { suggestion ->
                // Count practices for tiebreaker
                val allPieceActivities = pieceActivities[suggestion.piece.id] ?: emptyList()
                allPieceActivities.filter {
                    it.activityType == ActivityType.PRACTICE && it.timestamp >= twentyEightDaysAgo
                }.size
            }
            .thenBy { it.piece.name.lowercase() }
        )

        if (BuildConfig.DEBUG) {
            // DEBUG: Log tier results before limiting
            android.util.Log.d("Phase4Debug64", "=== PERFORMANCE SUGGESTIONS TIERS ===")
            android.util.Log.d("Phase4Debug65", "First tier suggestions: ${sortedFirstTier.size}")
            android.util.Log.d("Phase4Debug66", "Second tier suggestions: ${sortedSecondTier.size}")
            android.util.Log.d("Phase4Debug67", "Combined before limit: ${sortedFirstTier.size + sortedSecondTier.size}")
            android.util.Log.d("Phase4Debug68", "Performance limit: ${ProUserManager.PRO_USER_PERFORMANCE_SUGGESTIONS}")
        }

        val result = (sortedFirstTier + sortedSecondTier).take(ProUserManager.PRO_USER_PERFORMANCE_SUGGESTIONS)

        if (BuildConfig.DEBUG) {
            android.util.Log.d("Phase4Debug69", "Final performance suggestions: ${result.size}")
        }

        // Combine tiers and take Pro user performance suggestions limit
        return result
    }

    /**
     * Generate practice suggestions based on favorites and non-favorites logic.
     */
    fun generatePracticeSuggestions(
        pieces: List<PieceOrTechnique>,
        activities: List<Activity>
    ): List<SuggestionItem> {
        val favoriteSuggestions = mutableListOf<SuggestionItem>()
        val nonFavoriteSuggestions = mutableListOf<SuggestionItem>()

        if (BuildConfig.DEBUG) {
            // DEBUG: Log start of practice suggestions generation
            android.util.Log.d("Phase4Debug41", "=== generatePracticeSuggestions() START ===")
            android.util.Log.d("Phase4Debug42", "Pro user: ${proUserManager.isProUser()}")
            android.util.Log.d("Phase4Debug43", "Total pieces: ${pieces.filter { it.type == ItemType.PIECE }.size}")
        }

        pieces.filter { it.type == ItemType.PIECE }.forEach { piece ->
            // Use piece fields instead of expensive database queries
            val lastPracticeDate = piece.lastPracticeDate
            val lastPerformanceDate = piece.lastPerformanceDate

            // Determine most recent activity and its type
            val (lastActivityDate, isLastActivityPerformance) = when {
                lastPracticeDate != null && lastPerformanceDate != null -> {
                    if (lastPracticeDate > lastPerformanceDate) {
                        lastPracticeDate to false
                    } else {
                        lastPerformanceDate to true
                    }
                }
                lastPracticeDate != null -> lastPracticeDate to false
                lastPerformanceDate != null -> lastPerformanceDate to true
                else -> null to false
            }

            val daysSince = if (lastActivityDate != null) {
                ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
            } else {
                Int.MAX_VALUE
            }

            if (piece.isFavorite) {
                // Simplified favorites: not practiced or performed in the last 14 hours
                if (lastActivityDate == null || lastActivityDate < fourteenHoursAgo) {
                    favoriteSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val hoursElapsed = ((now - lastActivityDate) / (60 * 60 * 1000)).toInt()
                                if (hoursElapsed < 48) {
                                    val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                    "Favorite piece - $activityTypeText $hoursElapsed $hourText ago"
                                } else {
                                    val dayText = if (daysSince == 1) "day" else "days"
                                    "Favorite piece - $activityTypeText $daysSince $dayText ago"
                                }
                            }
                        )
                    )
                }
            } else {
                // Non-favorites that haven't been practiced in 7+ days but have been practiced in last 31 days
                if (lastActivityDate != null && lastActivityDate < sevenDaysAgo && lastActivityDate >= thirtyOneDaysAgo) {
                    android.util.Log.d("Phase4Debug44", "Adding non-favorite: ${piece.name} | days=$daysSince | count before add: ${nonFavoriteSuggestions.size}")
                    nonFavoriteSuggestions.add(
                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val hoursElapsed = ((now - lastActivityDate!!) / (60 * 60 * 1000)).toInt()
                                if (hoursElapsed < 48) {
                                    val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                    "$activityTypeText $hoursElapsed $hourText ago"
                                } else {
                                    val dayText = if (daysSince == 1) "day" else "days"
                                    "$activityTypeText $daysSince $dayText ago"
                                }
                            }()
                        )
                    )
                    android.util.Log.d("Phase4Debug45", "Added non-favorite: ${piece.name} | count after add: ${nonFavoriteSuggestions.size}")
                }
            }
        }

        // Determine favorite limit based on Pro status
        val favoriteLimit = if (proUserManager.isProUser())
            ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS
            else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS

        if (BuildConfig.DEBUG) {
            // DEBUG: Log all favorite suggestions before finalFavoriteSuggestions assignment
            android.util.Log.d("Phase4Debug78", "=== FAVORITE SUGGESTIONS BEFORE FALLBACK LOGIC ===")
            android.util.Log.d("Phase4Debug79", "favoriteLimit: $favoriteLimit, favoriteSuggestions.size: ${favoriteSuggestions.size}")
            favoriteSuggestions.forEachIndexed { index, suggestion ->
                android.util.Log.d("Phase4Debug80", "FavBeforeFallback[$index] ${suggestion.piece.name} | days=${suggestion.daysSinceLastActivity} | reason=${suggestion.suggestionReason}")
            }
        }

        // Sort favorite suggestions by last activity (least recent first)
        val sortedFavoriteSuggestions = favoriteSuggestions.sortedWith(
            compareBy<SuggestionItem> { it.lastActivityDate ?: 0L }
        )

        // Always aim for up to limit favorites total
        val finalFavoriteSuggestions = if (sortedFavoriteSuggestions.size < favoriteLimit) {
            val neededCount = favoriteLimit - sortedFavoriteSuggestions.size
            val usedPieceIds = sortedFavoriteSuggestions.map { it.piece.id }.toSet()

            // Get fallback favorites (excluding already used pieces and pieces practiced today)
            val allFavorites = pieces.filter { it.type == ItemType.PIECE && it.isFavorite && it.id !in usedPieceIds }
            val fallbackFavorites = if (allFavorites.isNotEmpty()) {
                val favoritesWithActivity = allFavorites.mapNotNull { piece ->
                    // Use piece fields instead of expensive database queries
                    val lastPracticeDate = piece.lastPracticeDate
                    val lastPerformanceDate = piece.lastPerformanceDate

                    // Determine most recent activity and its type
                    val (lastActivityDate, isLastActivityPerformance) = when {
                        lastPracticeDate != null && lastPerformanceDate != null -> {
                            if (lastPracticeDate > lastPerformanceDate) {
                                lastPracticeDate to false
                            } else {
                                lastPerformanceDate to true
                            }
                        }
                        lastPracticeDate != null -> lastPracticeDate to false
                        lastPerformanceDate != null -> lastPerformanceDate to true
                        else -> null to false
                    }

                    // Exclude pieces practiced or performed in the last 14 hours (consistent with main favorites logic)
                    if (lastActivityDate != null && lastActivityDate >= fourteenHoursAgo) {
                        return@mapNotNull null
                    }

                    val daysSince = if (lastActivityDate != null) {
                        ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                    } else {
                        Int.MAX_VALUE
                    }

                    SuggestionItem(
                        piece = piece,
                        lastActivityDate = lastActivityDate,
                        daysSinceLastActivity = daysSince,
                        suggestionReason = if (lastActivityDate == null) "Favorite piece - Never practiced" else {
                            val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                            val hoursElapsed = ((now - lastActivityDate) / (60 * 60 * 1000)).toInt()
                            if (hoursElapsed < 48) {
                                val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                "Favorite piece - $activityTypeText $hoursElapsed $hourText ago"
                            } else {
                                val dayText = if (daysSince == 1) "day" else "days"
                                "Favorite piece - $activityTypeText $daysSince $dayText ago"
                            }
                        }
                    )
                }
                // Get the least recently practiced favorites (highest daysSince)
                if (favoritesWithActivity.isNotEmpty()) {
                    val maxDays = favoritesWithActivity.maxOf { it.daysSinceLastActivity }
                    val candidates = favoritesWithActivity.filter { it.daysSinceLastActivity == maxDays }
                    // Sort by time (earliest first for tie-breaking), then alphabetically
                    candidates.sortedWith(compareBy<SuggestionItem> { it.lastActivityDate ?: 0L }
                        .thenBy { it.piece.name.lowercase() })
                        .take(neededCount)
                } else emptyList()
            } else emptyList()

            sortedFavoriteSuggestions + fallbackFavorites
        } else sortedFavoriteSuggestions

        // Determine non-favorite limit based on Pro status
        val nonFavoriteLimit = if (proUserManager.isProUser())
            ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
            else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS

        // DEBUG: Log the counts and limits
        android.util.Log.d("Phase4Debug46", "=== AFTER MAIN COLLECTION ===")
        android.util.Log.d("Phase4Debug47", "Collected favorites: ${favoriteSuggestions.size}")
        android.util.Log.d("Phase4Debug48", "Collected non-favorites: ${nonFavoriteSuggestions.size}")
        android.util.Log.d("Phase4Debug49", "nonFavoriteLimit: $nonFavoriteLimit")
        android.util.Log.d("Phase4Debug50", "Need fallback? ${nonFavoriteSuggestions.size < nonFavoriteLimit}")

        // Sort non-favorite suggestions by last activity (least recent first)
        val sortedNonFavoriteSuggestions = nonFavoriteSuggestions.sortedWith(
            compareBy<SuggestionItem> { it.lastActivityDate ?: 0L }
        )

        // Always aim for up to limit non-favorites total
        val finalNonFavoriteSuggestions = if (sortedNonFavoriteSuggestions.size < nonFavoriteLimit) {
            val neededCount = nonFavoriteLimit - sortedNonFavoriteSuggestions.size
            val usedPieceIds = sortedNonFavoriteSuggestions.map { it.piece.id }.toSet()

            // Get fallback non-favorites (excluding already used pieces)
            val allNonFavorites = pieces.filter { it.type == ItemType.PIECE && !it.isFavorite && it.id !in usedPieceIds }
            val fallbackNonFavorites = if (allNonFavorites.isNotEmpty()) {
                val nonFavoritesWithActivity = allNonFavorites.mapNotNull { piece ->
                    // Use piece fields instead of expensive database queries
                    val lastPracticeDate = piece.lastPracticeDate
                    val lastPerformanceDate = piece.lastPerformanceDate

                    // Determine most recent activity and its type
                    val (lastActivityDate, isLastActivityPerformance) = when {
                        lastPracticeDate != null && lastPerformanceDate != null -> {
                            if (lastPracticeDate > lastPerformanceDate) {
                                lastPracticeDate to false
                            } else {
                                lastPerformanceDate to true
                            }
                        }
                        lastPracticeDate != null -> lastPracticeDate to false
                        lastPerformanceDate != null -> lastPerformanceDate to true
                        else -> null to false
                    }

                    // Only include abandoned/inactive pieces (31+ days or never practiced)
                    if (lastActivityDate == null || lastActivityDate < thirtyOneDaysAgo) {
                        val daysSince = if (lastActivityDate != null) {
                            ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                        } else {
                            Int.MAX_VALUE
                        }

                        SuggestionItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince,
                            suggestionReason = if (lastActivityDate == null) "Never practiced" else {
                                val activityTypeText = if (isLastActivityPerformance) "Last performance" else "Last practice"
                                val hoursElapsed = ((now - lastActivityDate) / (60 * 60 * 1000)).toInt()
                                if (hoursElapsed < 48) {
                                    val hourText = if (hoursElapsed == 1) "hour" else "hours"
                                    "$activityTypeText $hoursElapsed $hourText ago"
                                } else {
                                    val dayText = if (daysSince == 1) "day" else "days"
                                    "$activityTypeText $daysSince $dayText ago"
                                }
                            }
                        )
                    } else null
                }
                // Get the most recently practiced abandoned pieces (lowest daysSince among abandoned)
                if (nonFavoritesWithActivity.isNotEmpty()) {
                    // Sort abandoned pieces: 1) most recent among abandoned (lowest daysSince), 2) latest timestamp, 3) alphabetical
                    val sortedCandidates = nonFavoritesWithActivity.sortedWith(
                        compareBy<SuggestionItem> { it.daysSinceLastActivity } // Most recent among abandoned (lowest days)
                        .thenByDescending { it.lastActivityDate ?: 0L } // Latest timestamp for tie-breaking
                        .thenBy { it.piece.name.lowercase() } // Alphabetical
                    )
                    sortedCandidates.take(neededCount)
                } else emptyList()
            } else emptyList()

            sortedNonFavoriteSuggestions + fallbackNonFavorites
        } else sortedNonFavoriteSuggestions

        // DEBUG: Log final results
        android.util.Log.d("Phase4Debug51", "=== FINAL PRACTICE SUGGESTIONS RESULT ===")
        android.util.Log.d("Phase4Debug52", "Final favorites: ${finalFavoriteSuggestions.size}")
        android.util.Log.d("Phase4Debug53", "Final non-favorites: ${finalNonFavoriteSuggestions.size}")
        android.util.Log.d("Phase4Debug54", "Total practice suggestions: ${finalFavoriteSuggestions.size + finalNonFavoriteSuggestions.size}")

        // PRACTICE SUGGESTIONS: Combine favorites first, then non-favorites
        return (finalFavoriteSuggestions + finalNonFavoriteSuggestions).map {
            it.copy(suggestionType = SuggestionType.PRACTICE)
        }
    }
}