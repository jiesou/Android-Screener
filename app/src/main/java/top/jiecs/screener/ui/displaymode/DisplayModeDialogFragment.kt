package top.jiecs.screener.ui.displaymode

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import top.jiecs.screener.R
import top.jiecs.screener.units.ApiCaller

class DisplayModeDialogFragment : DialogFragment() {
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.display_modes))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes)) { _: DialogInterface, _: Int ->
                val apiCaller = ApiCaller()
                apiCaller.resetResolution(0)
            }
            .create()
        return dialog
    }
}