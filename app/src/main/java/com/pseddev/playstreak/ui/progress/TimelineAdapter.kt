package com.pseddev.mystreak.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.databinding.ItemTimelineActivityBinding
import java.text.SimpleDateFormat
import java.util.*

class TimelineAdapter(
    private val onDeleteClick: (ActivityWithPiece) -> Unit,
    private val onEditClick: (ActivityWithPiece) -> Unit
) : ListAdapter<ActivityWithPiece, TimelineAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimelineActivityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onEditClick)
    }

    class ViewHolder(private val binding: ItemTimelineActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

        fun bind(item: ActivityWithPiece, onDeleteClick: (ActivityWithPiece) -> Unit, onEditClick: (ActivityWithPiece) -> Unit) {
            val activity = item.activity
            val piece = item.pieceOrTechnique

            binding.dateText.text = dateFormat.format(Date(activity.timestamp))
            binding.timeText.text = timeFormat.format(Date(activity.timestamp))

            // Show technique emoji indicator for techniques
            val displayName = if (piece.type == com.pseddev.mystreak.data.entities.ItemType.TECHNIQUE) {
                "🎼 ${piece.name}"
            } else {
                piece.name
            }
            binding.pieceNameText.text = displayName

            val typeText = when (activity.activityType) {
                ActivityType.PRACTICE -> {
                    val level = when (activity.level) {
                        1 -> "Level 1 - Essentials"
                        2 -> "Level 2 - Incomplete"
                        3 -> "Level 3 - Complete with Issues"
                        4 -> "Level 4 - Complete and Satisfactory"
                        else -> "Level ${activity.level}"
                    }
                    "Practice - $level"
                }
                ActivityType.PERFORMANCE -> {
                    val level = when (activity.level) {
                        1 -> "Level 1 - Failed"
                        2 -> "Level 2 - Unsatisfactory"
                        3 -> "Level 3 - Satisfactory"
                        else -> "Level ${activity.level}"
                    }
                    val type = if (activity.performanceType == "online") "Online" else "Live"
                    "Performance - $type - $level"
                }
            }
            binding.activityTypeText.text = typeText

            if (activity.minutes > 0) {
                binding.durationText.text = "${activity.minutes} minutes"
            } else {
                binding.durationText.text = ""
            }

            if (activity.notes.isNotEmpty()) {
                binding.notesText.text = activity.notes
            } else {
                binding.notesText.text = ""
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(item)
            }

            binding.editButton.setOnClickListener {
                onEditClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActivityWithPiece>() {
        override fun areItemsTheSame(oldItem: ActivityWithPiece, newItem: ActivityWithPiece): Boolean {
            return oldItem.activity.id == newItem.activity.id
        }

        override fun areContentsTheSame(oldItem: ActivityWithPiece, newItem: ActivityWithPiece): Boolean {
            return oldItem == newItem
        }
    }
}