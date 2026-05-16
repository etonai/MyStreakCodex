package com.pseddev.mystreak.ui.progress

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ItemType

/**
 * Temporary storage for activity data during edit mode and calendar pre-population.
 * This allows passing activity data between Timeline and Add Activity fragments,
 * and pre-populating date from Calendar tab.
 */
object EditActivityStorage {
    private var editActivity: Activity? = null
    private var pieceName: String? = null
    private var itemType: ItemType? = null
    private var prePopulatedDate: Long? = null

    fun setEditActivity(activity: Activity, pieceName: String = "", itemType: ItemType = ItemType.PIECE) {
        editActivity = activity
        this.pieceName = pieceName
        this.itemType = itemType
    }

    fun getEditActivity(): Activity? {
        return editActivity
    }

    fun getPieceName(): String? {
        return pieceName
    }

    fun getItemType(): ItemType? {
        return itemType
    }

    fun clearEditActivity() {
        editActivity = null
        pieceName = null
        itemType = null
        prePopulatedDate = null
    }

    fun clearEditState() {
        editActivity = null
        pieceName = null
        itemType = null
    }

    fun isEditMode(): Boolean {
        return editActivity != null
    }

    fun setPrePopulatedDate(timestamp: Long) {
        prePopulatedDate = timestamp
    }

    fun getPrePopulatedDate(): Long? {
        return prePopulatedDate
    }

    fun clearPrePopulatedDate() {
        prePopulatedDate = null
    }
}
