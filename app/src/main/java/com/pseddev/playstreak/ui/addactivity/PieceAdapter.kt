package com.pseddev.mystreak.ui.addactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.databinding.ItemHeaderBinding
import com.pseddev.mystreak.databinding.ItemPieceBinding

sealed class PieceAdapterItem {
    data class Header(val title: String) : PieceAdapterItem()
    data class Item(val piece: PieceOrTechnique) : PieceAdapterItem()
}

class PieceAdapter(
    private val onItemClick: (PieceOrTechnique) -> Unit
) : ListAdapter<PieceAdapterItem, RecyclerView.ViewHolder>(PieceDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PieceAdapterItem.Header -> VIEW_TYPE_HEADER
            is PieceAdapterItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = ItemPieceBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                PieceViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PieceAdapterItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is PieceAdapterItem.Item -> (holder as PieceViewHolder).bind(item.piece)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.textHeader.text = title
        }
    }

    class PieceViewHolder(
        private val binding: ItemPieceBinding,
        private val onItemClick: (PieceOrTechnique) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(piece: PieceOrTechnique) {
            val icon = if (piece.type == ItemType.PIECE) "🎵" else "⚙️"
            val favoriteIcon = if (piece.isFavorite) " ⭐" else ""
            binding.textPieceName.text = "$icon ${piece.name}$favoriteIcon"
            binding.root.setOnClickListener { onItemClick(piece) }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}

class PieceDiffCallback : DiffUtil.ItemCallback<PieceAdapterItem>() {
    override fun areItemsTheSame(oldItem: PieceAdapterItem, newItem: PieceAdapterItem): Boolean {
        return when {
            oldItem is PieceAdapterItem.Header && newItem is PieceAdapterItem.Header ->
                oldItem.title == newItem.title
            oldItem is PieceAdapterItem.Item && newItem is PieceAdapterItem.Item ->
                oldItem.piece.id == newItem.piece.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: PieceAdapterItem, newItem: PieceAdapterItem): Boolean {
        return oldItem == newItem
    }
}