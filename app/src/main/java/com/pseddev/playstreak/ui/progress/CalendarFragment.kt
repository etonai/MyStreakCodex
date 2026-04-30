package com.pseddev.playstreak.ui.progress

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.R
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.databinding.FragmentCalendarBinding
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.PreferencesManager
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*
import android.content.res.Configuration

class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(
            (requireActivity().application as PlayStreakApplication).repository
        )
    }
    
    private var selectedDate: LocalDate? = null
    private var monthlyActivities: Map<LocalDate, List<ActivityWithPiece>> = emptyMap()
    private var currentDisplayMonth: YearMonth = YearMonth.now()
    private lateinit var proUserManager: ProUserManager
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        proUserManager = ProUserManager.getInstance(requireContext())
        preferencesManager = PreferencesManager.getInstance(requireContext())
        
        setupClickListeners()
        setupCalendar()
        updateMonthDisplay()
        updateColorGuideVisibility()
        
        viewModel.selectedDateActivities.observe(viewLifecycleOwner) { activities ->
            updateSelectedDateView(activities)
        }
        
        viewModel.monthlyActivitySummary.observe(viewLifecycleOwner) { summary ->
            updateMonthlySummary(summary)
            updateMonthlyActivities(summary)
        }
        
        // Set initial date to today
        val today = LocalDate.now()
        selectedDate = today
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModel.selectDate(todayMillis)
    }
    
    private fun setupCalendar() {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.bind(data)
            }
        }
        
        // Set up calendar to show current month
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        binding.calendarView.setup(firstMonth, lastMonth, com.kizitonwose.calendar.core.firstDayOfWeekFromLocale())
        binding.calendarView.scrollToMonth(currentMonth)
        
        // Disable month swiping by intercepting horizontal scroll gestures
        disableCalendarSwiping()
    }
    
    private fun disableCalendarSwiping() {
        var initialX = 0f
        
        binding.calendarView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Store the initial touch position for swipe detection
                    initialX = event.x
                    false // Allow the touch to proceed for day selection
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = kotlin.math.abs(event.x - initialX)
                    
                    // Block horizontal swipes greater than 30 pixels to prevent month changes
                    // Allow smaller movements for day selection and natural touch behavior
                    if (deltaX > 30) {
                        true // Consume horizontal swipes to prevent month navigation
                    } else {
                        false // Allow day selection and small movements
                    }
                }
                else -> false // Allow all other touch events (UP, CANCEL, etc.)
            }
        }
    }
    
    private fun updateMonthlyActivities(summary: MonthlyActivitySummary) {
        // Convert activities map to LocalDate keys
        monthlyActivities = summary.dailyActivities.mapKeys { (dateMillis, _) ->
            val instant = java.time.Instant.ofEpochMilli(dateMillis)
            instant.atZone(ZoneId.systemDefault()).toLocalDate()
        }
        
        // Refresh calendar to update colors
        binding.calendarView.notifyCalendarChanged()
    }
    
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        
        
        fun bind(day: CalendarDay) {
            textView.text = day.date.dayOfMonth.toString()
            
            if (day.position == DayPosition.MonthDate) {
                textView.visibility = View.VISIBLE
                
                // Get activities for this date
                val activities = monthlyActivities[day.date] ?: emptyList()
                val color = getCalendarColorForActivities(activities)
                
                // Set background color
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_day_background)?.mutate() as? GradientDrawable
                drawable?.setColor(color)
                textView.background = drawable
                
                // Set text color for visibility
                textView.setTextColor(getTextColorForBackground(color))
                
                // Handle selection
                textView.setOnClickListener {
                    selectedDate = day.date
                    val millis = day.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.selectDate(millis)
                    binding.calendarView.notifyCalendarChanged()
                }
                
                // Highlight selected date
                if (day.date == selectedDate) {
                    textView.alpha = 1.0f
                    
                    // Handle selection styling based on dark mode and activity presence
                    if (isDarkMode() && activities.isEmpty()) {
                        // Dark mode with no activities: use medium gray background for visibility
                        val selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_day_background)?.mutate() as? GradientDrawable
                        val mediumGrayColor = ContextCompat.getColor(requireContext(), R.color.calendar_dark_mode_selection)
                        selectedDrawable?.setColor(mediumGrayColor)
                        selectedDrawable?.setStroke(9, ContextCompat.getColor(requireContext(), R.color.calendar_selection_ring))
                        textView.background = selectedDrawable
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    } else {
                        // Light mode or has activities: use original logic
                        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                        
                        // Add border to selected date
                        val selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_day_background)?.mutate() as? GradientDrawable
                        selectedDrawable?.setColor(color)
                        selectedDrawable?.setStroke(9, ContextCompat.getColor(requireContext(), R.color.calendar_selection_ring))
                        textView.background = selectedDrawable
                    }
                } else {
                    textView.alpha = 0.8f
                }
            } else {
                textView.visibility = View.INVISIBLE
            }
        }
    }
    
    private fun populateActivityList(activities: List<ActivityWithPiece>) {
        binding.linearLayoutSelectedDateActivities.removeAllViews()
        
        for (activityWithPiece in activities) {
            val itemBinding = com.pseddev.playstreak.databinding.ItemTimelineActivityBinding.inflate(
                layoutInflater, 
                binding.linearLayoutSelectedDateActivities, 
                false
            )
            
            // Bind the activity data using the same logic as TimelineAdapter
            bindActivityView(itemBinding, activityWithPiece)
            
            binding.linearLayoutSelectedDateActivities.addView(itemBinding.root)
        }
    }
    
    private fun bindActivityView(itemBinding: com.pseddev.playstreak.databinding.ItemTimelineActivityBinding, item: ActivityWithPiece) {
        val activity = item.activity
        val piece = item.pieceOrTechnique
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        
        itemBinding.dateText.text = dateFormat.format(Date(activity.timestamp))
        itemBinding.timeText.text = timeFormat.format(Date(activity.timestamp))
        
        // Show technique emoji indicator for techniques
        val displayName = if (piece.type == com.pseddev.playstreak.data.entities.ItemType.TECHNIQUE) {
            "🎼 ${piece.name}"
        } else {
            piece.name
        }
        itemBinding.pieceNameText.text = piece.name
        
        val typeText = when (activity.level) {
            1 -> "Success - Minimum"
            2 -> "Success - Medium"
            3, 4 -> "Success - High"
            else -> "Success - Level ${activity.level}"
        }
        itemBinding.activityTypeText.text = typeText
        
        if (activity.minutes > 0) {
            itemBinding.durationText.text = "${activity.minutes} minutes"
        } else {
            itemBinding.durationText.text = ""
        }
        
        if (activity.notes.isNotEmpty()) {
            itemBinding.notesText.text = activity.notes
        } else {
            itemBinding.notesText.text = ""
        }
        
        itemBinding.addActivityButton.visibility = View.GONE
        
        itemBinding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(item)
        }
        
        itemBinding.editButton.setOnClickListener {
            editActivity(item)
        }
    }
    
    private fun updateSelectedDateView(activities: List<ActivityWithPiece>) {
        // Update color indicator with larger, more prominent display
        val color = getCalendarColorForActivities(activities)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.circle_indicator)?.mutate()
        drawable?.setTint(color)
        binding.activityColorIndicator.background = drawable
        
        // Format the selected date
        val dateText = selectedDate?.let { date ->
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                    "$monthName ${date.dayOfMonth}"
                }
            }
        } ?: "Selected Date"
        
        // Check if detail mode is enabled
        val isDetailModeEnabled = preferencesManager.isCalendarDetailModeEnabled()
        
        if (activities.isEmpty()) {
            binding.selectedDateText.text = "$dateText: No activities on this date"
            binding.activityColorIndicator.visibility = View.GONE
            
            // Hide both displays when no activities
            binding.selectedDateActivities.text = ""
            binding.selectedDateActivities.visibility = View.VISIBLE
            binding.linearLayoutSelectedDateActivities.visibility = View.GONE
            binding.linearLayoutSelectedDateActivities.removeAllViews()
        } else {
            val activityTypeText = getActivityTypeDescription(activities)
            val activityWord = if (activities.size == 1) "activity" else "activities"
            binding.selectedDateText.text = "$dateText: ${activities.size} $activityWord ($activityTypeText)"
            binding.activityColorIndicator.visibility = View.VISIBLE
            
            if (isDetailModeEnabled) {
                // Detail mode: Show LinearLayout with Timeline-style cards
                binding.selectedDateActivities.visibility = View.GONE
                binding.linearLayoutSelectedDateActivities.visibility = View.VISIBLE
                populateActivityList(activities)
            } else {
                // Regular mode: Show simple text summary
                binding.selectedDateActivities.visibility = View.VISIBLE
                binding.linearLayoutSelectedDateActivities.visibility = View.GONE
                binding.linearLayoutSelectedDateActivities.removeAllViews()
                
                val summary = activities.joinToString("\n") { item ->
                    val time = android.text.format.DateFormat.format("h:mm a", item.activity.timestamp)
                    val level = "(${item.activity.level})"
                    val minutes = if (item.activity.minutes > 0) " - ${item.activity.minutes} min" else ""
                    "$time - ${item.pieceOrTechnique.name} - Success $level$minutes"
                }
                binding.selectedDateActivities.text = summary
            }
        }
        
        // Scroll to top of the details section when date changes
        binding.scrollViewCalendarDetails.post {
            binding.scrollViewCalendarDetails.smoothScrollTo(0, 0)
        }
    }
    
    private fun updateMonthlySummary(summary: MonthlyActivitySummary) {
        binding.monthlyActiveDaysText.text = summary.activeDays.toString()
        binding.monthlyActivitiesText.text = summary.totalActivities.toString()
    }
    
    private fun getActivityTypeDescription(activities: List<ActivityWithPiece>): String {
        val hasPerformance = activities.any { it.activity.activityType == ActivityType.PERFORMANCE }
        val practiceCount = activities.count { it.activity.activityType == ActivityType.PRACTICE }
        val performanceCount = activities.count { it.activity.activityType == ActivityType.PERFORMANCE }
        
        return when {
            hasPerformance && practiceCount > 0 -> "Mixed success"
            hasPerformance -> "Priority placeholder"
            else -> "Activity"
        }
    }
    
    private fun getTextColorForBackground(backgroundColor: Int): Int {
        // Calculate if we need light or dark text based on background color
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        
        return if (luminance > 0.5) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }
    
    private fun isDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    private fun getCalendarColorForActivities(activities: List<ActivityWithPiece>): Int {
        if (activities.isEmpty()) {
            return ContextCompat.getColor(requireContext(), R.color.calendar_no_activity)
        }
        
        // Free users get simplified heat map: only light blue for any activities
        if (!proUserManager.isProUser()) {
            return ContextCompat.getColor(requireContext(), R.color.calendar_practice_light)
        }
        
        // Pro users get full heat map with multiple colors and intensities
        val activityCount = activities.size
        val hasPerformance = activities.any { it.activity.activityType == ActivityType.PERFORMANCE }
        
        return if (hasPerformance) {
            // Performance days use green colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_performance_light)
            }
        } else {
            // Practice-only days use blue colors
            when {
                activityCount >= 11 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_dark)
                activityCount >= 5 -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_medium)
                else -> ContextCompat.getColor(requireContext(), R.color.calendar_practice_light)
            }
        }
    }
    
    private fun updateColorGuideVisibility() {
        val isProUser = proUserManager.isProUser()
        binding.colorGuideTitle.visibility = if (isProUser) View.VISIBLE else View.GONE
        binding.colorGuideContainer.visibility = if (isProUser) View.VISIBLE else View.GONE
    }
    
    private fun setupClickListeners() {
        binding.buttonAddActivity.setOnClickListener {
            // Pre-populate with selected calendar date
            selectedDate?.let { date ->
                val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                EditActivityStorage.setPrePopulatedDate(timestamp)
            }
            findNavController().navigate(R.id.action_viewProgressFragment_to_addActivityFragment)
        }
        
        binding.buttonPreviousMonth.setOnClickListener {
            currentDisplayMonth = currentDisplayMonth.minusMonths(1)
            binding.calendarView.scrollToMonth(currentDisplayMonth)
            updateMonthDisplay()
            updateMonthData()
        }
        
        binding.buttonNextMonth.setOnClickListener {
            currentDisplayMonth = currentDisplayMonth.plusMonths(1)
            binding.calendarView.scrollToMonth(currentDisplayMonth)
            updateMonthDisplay()
            updateMonthData()
        }
    }
    
    private fun updateMonthDisplay() {
        val monthName = currentDisplayMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        binding.monthYearText.text = "$monthName ${currentDisplayMonth.year}"
    }
    
    private fun updateMonthData() {
        // When switching months, try to select the same day number if it exists in the new month
        val dayToSelect = selectedDate?.let { currentSelected ->
            val dayOfMonth = currentSelected.dayOfMonth
            val newMonth = currentDisplayMonth
            
            // Check if the same day number exists in the new month
            if (dayOfMonth <= newMonth.lengthOfMonth()) {
                newMonth.atDay(dayOfMonth)
            } else {
                // If the day doesn't exist (e.g., Feb 30), select the last day of the month
                newMonth.atEndOfMonth()
            }
        } ?: currentDisplayMonth.atDay(1) // Default to first day if no previous selection
        
        // Update the selected date and view model
        selectedDate = dayToSelect
        val millis = dayToSelect.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModel.selectDate(millis)
        
        // Refresh the calendar view to show the new selection
        binding.calendarView.notifyCalendarChanged()
    }
    
    private fun editActivity(activityWithPiece: ActivityWithPiece) {
        // Store the activity data for edit mode
        EditActivityStorage.setEditActivity(
            activityWithPiece.activity, 
            activityWithPiece.pieceOrTechnique.name,
            activityWithPiece.pieceOrTechnique.type
        )
        
        // Navigate directly to select level fragment for editing
        findNavController().navigate(
            R.id.action_viewProgressFragment_to_selectLevelFragment,
            bundleOf(
                "activityType" to activityWithPiece.activity.activityType,
                "pieceId" to activityWithPiece.activity.pieceOrTechniqueId,
                "pieceName" to activityWithPiece.pieceOrTechnique.name,
                "itemType" to activityWithPiece.pieceOrTechnique.type
            )
        )
    }
    
    private fun showDeleteConfirmationDialog(activityWithPiece: ActivityWithPiece) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val dateString = dateFormat.format(Date(activityWithPiece.activity.timestamp))
        val pieceName = activityWithPiece.pieceOrTechnique.name
        
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?\n\n$pieceName\n$dateString")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(activityWithPiece)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh calendar and color guide in case Pro status changed while away from this fragment
        // Also refresh in case detail mode preference changed
        binding.calendarView.notifyCalendarChanged()
        updateColorGuideVisibility()
        
        // Refresh selected date view in case detail mode preference changed
        viewModel.selectedDateActivities.value?.let { activities ->
            updateSelectedDateView(activities)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
