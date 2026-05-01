package com.pseddev.mystreak.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.mystreak.R
import com.pseddev.mystreak.data.entities.Achievement
import java.text.SimpleDateFormat
import java.util.*

class AchievementsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories = listOf<AchievementCategory>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    companion object {
        private const val VIEW_TYPE_CATEGORY_HEADER = 0
        private const val VIEW_TYPE_ACHIEVEMENT_ITEM = 1
    }

    private data class AdapterItem(
        val type: Int,
        val category: String? = null,
        val achievement: Achievement? = null
    )

    private var items = listOf<AdapterItem>()

    fun submitCategories(newCategories: List<AchievementCategory>) {
        categories = newCategories

        // Flatten categories into adapter items
        val newItems = mutableListOf<AdapterItem>()
        for (category in categories) {
            // Add category header
            newItems.add(AdapterItem(VIEW_TYPE_CATEGORY_HEADER, category = category.categoryName))

            // Add achievement items
            for (achievement in category.achievements) {
                newItems.add(AdapterItem(VIEW_TYPE_ACHIEVEMENT_ITEM, achievement = achievement))
            }
        }

        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_achievement_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            VIEW_TYPE_ACHIEVEMENT_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_achievement, parent, false)
                AchievementViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is CategoryHeaderViewHolder -> {
                holder.bind(item.category!!)
            }
            is AchievementViewHolder -> {
                holder.bind(item.achievement!!)
            }
        }
    }

    inner class CategoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: String) {
            categoryName.text = category
        }
    }

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val achievementIcon: TextView = itemView.findViewById(R.id.achievementIcon)
        private val achievementTitle: TextView = itemView.findViewById(R.id.achievementTitle)
        private val achievementDescription: TextView = itemView.findViewById(R.id.achievementDescription)
        private val achievementStatus: TextView = itemView.findViewById(R.id.achievementStatus)

        fun bind(achievement: Achievement) {
            // Show actual icon for unlocked achievements, question mark emoji for locked ones
            achievementIcon.text = if (achievement.isUnlocked) {
                achievement.iconEmoji
            } else {
                "❓"
            }
            achievementTitle.text = achievement.title
            achievementDescription.text = achievement.description

            if (achievement.isUnlocked) {
                achievementStatus.text = if (achievement.unlockedAt != null) {
                    "Unlocked ${dateFormat.format(Date(achievement.unlockedAt))}"
                } else {
                    "Unlocked"
                }
                achievementStatus.setTextColor(
                    itemView.context.getColor(android.R.color.holo_green_dark)
                )

                // Make unlocked achievements more prominent
                achievementIcon.alpha = 1.0f
                achievementTitle.alpha = 1.0f
                achievementDescription.alpha = 1.0f
            } else {
                achievementStatus.text = "Locked"
                achievementStatus.setTextColor(
                    itemView.context.getColor(android.R.color.darker_gray)
                )

                // Make locked achievements less prominent
                achievementIcon.alpha = 0.4f
                achievementTitle.alpha = 0.6f
                achievementDescription.alpha = 0.6f
            }
        }
    }
}