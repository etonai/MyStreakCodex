package com.pseddev.mystreak.ui.sync

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.MyStreakApplication
import com.pseddev.mystreak.R

class SyncDialogFragment : DialogFragment() {

    private val viewModel: SyncDialogViewModel by viewModels {
        SyncDialogViewModelFactory(
            (requireActivity().application as MyStreakApplication).repository,
            requireContext()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Check if we should show the dialog
            if (!viewModel.shouldShowSyncDialog()) {
                dismiss()
                return Dialog(requireContext())
            }

            builder.setTitle("Sync with Google Drive")
                .setMessage("Would you like to sync your data with Google Drive?")
                .setPositiveButton("Sync Now") { _, _ ->
                    viewModel.performStartupSync()
                    observeSyncResult()
                }
                .setNegativeButton("Later") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Settings") { _, _ ->
                    // Navigate to import/export settings
                    dismiss()
                    findNavController().navigate(R.id.syncFragment)
                }
                .setCancelable(false)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun observeSyncResult() {
        viewModel.syncResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    dismiss()
                } else {
                    showErrorDialog(result.message)
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Sync Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }
            .show()
    }
}