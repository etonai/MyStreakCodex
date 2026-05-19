package com.pseddev.mystreak.ui.progress

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.databinding.ItemPieceStatsBinding
import com.pseddev.mystreak.utils.ProUserManager
import com.pseddev.mystreak.utils.TaskColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PiecesAdapter(
    private val onPieceClick: (PieceWithStats) -> Unit,
    private val onFavoriteToggle: (PieceWithStats) -> Unit,
    private val onAddActivityClick: (PieceWithStats) -> Unit,
    private val onEditClick: (PieceWithStats) -> Unit,
    private val onDeleteClick: (PieceWithStats) -> Unit,
    private val proUserManager: ProUserManager
) : ListAdapter<PieceWithStats, PiecesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPieceStatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onPieceClick, onAddActivityClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPieceStatsBinding,
        private val onPieceClick: (PieceWithStats) -> Unit,
        private val onAddActivityClick: (PieceWithStats) -> Unit,
        private val onEditClick: (PieceWithStats) -> Unit,
        private val onDeleteClick: (PieceWithStats) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

        fun bind(item: PieceWithStats) {
            binding.pieceNameText.text = item.piece.name
            binding.root.alpha = if (item.piece.isActive) 1.0f else 0.55f
            binding.activityCountText.text = "Total Activities: ${item.activityCount}"
            binding.todayCountText.visibility = View.GONE
            binding.priorityText.text = if (item.piece.taskKind == TaskKind.ROUTINE) {
                "Routine"
            } else if (item.piece.priority == TaskPriority.HIGH) {
                "High priority"
            } else {
                "Low priority"
            }
            binding.statusText.text = if (item.piece.isActive) "Active" else "Inactive"

            val swatch = binding.taskColorSwatch.background.mutate() as? GradientDrawable
            swatch?.setColor(parseColor(TaskColors.displayColorFor(item.piece)))
            binding.taskColorSwatch.background = swatch

            if (item.lastActivityDate != null) {
                binding.lastActivityText.text = "Last: ${dateFormat.format(Date(item.lastActivityDate))}"
            } else {
                binding.lastActivityText.text = "No activities yet"
            }

            binding.favoriteIcon.visibility = View.GONE
            binding.addActivityIcon.visibility = if (item.piece.isActive) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onPieceClick(item)
            }

            binding.addActivityIcon.setOnClickListener {
                onAddActivityClick(item)
            }

            binding.editIcon.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteIcon.setOnClickListener {
                onDeleteClick(item)
            }
        }

        private fun parseColor(color: String): Int {
            return try {
                Color.parseColor(color)
            } catch (_: IllegalArgumentException) {
                Color.parseColor("#66B2FF")
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PieceWithStats>() {
        override fun areItemsTheSame(oldItem: PieceWithStats, newItem: PieceWithStats): Boolean {
            return oldItem.piece.id == newItem.piece.id
        }

        override fun areContentsTheSame(oldItem: PieceWithStats, newItem: PieceWithStats): Boolean {
            return oldItem == newItem
        }
    }
}
