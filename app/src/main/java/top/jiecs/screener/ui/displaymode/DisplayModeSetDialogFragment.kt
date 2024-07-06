package top.jiecs.screener.ui.displaymode

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import top.jiecs.screener.R

class DisplayModeSetDialogFragment : DialogFragment() {
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.new_display_mode)
            .setView(R.layout.dialog_new_display_mode)
            .setPositiveButton(R.string.apply) { _, _ ->
//                viewModel.saveDisplayMode()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        return dialog
    }
}