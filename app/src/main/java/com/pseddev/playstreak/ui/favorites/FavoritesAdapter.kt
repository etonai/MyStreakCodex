package com.pseddev.mystreak.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.mystreak.R
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.databinding.ItemFavoriteBinding

class FavoritesAdapter(
    private val onFavoriteToggle: (PieceOrTechnique) -> Unit
) : ListAdapter<PieceOrTechnique, FavoritesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onFavoriteToggle)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemFavoriteBinding,
        private val onFavoriteToggle: (PieceOrTechnique) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PieceOrTechnique) {
            binding.nameText.text = item.name

            // Set type icon and text
            when (item.type) {
                ItemType.PIECE -> {
                    binding.typeIcon.text = "🎵"
                    binding.typeText.text = "Piece"
                }
                ItemType.TECHNIQUE -> {
                    binding.typeIcon.text = "⚙️"
                    binding.typeText.text = "Technique"
                }
            }

            // Set favorite state
            updateFavoriteIcon(item.isFavorite)

            // Handle favorite toggle click
            binding.favoriteButton.setOnClickListener {
                onFavoriteToggle(item)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.favoriteButton.setImageResource(
                if (isFavorite) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PieceOrTechnique>() {
        override fun areItemsTheSame(oldItem: PieceOrTechnique, newItem: PieceOrTechnique): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PieceOrTechnique, newItem: PieceOrTechnique): Boolean {
            return oldItem == newItem
        }
    }
}