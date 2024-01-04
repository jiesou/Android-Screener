package top.jiecs.screener.ui.resolution
import android.os.Bundle
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import top.jiecs.screener.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import top.jiecs.screener.ui.resolution.ResolutionFragment

class ConfirmationDialogFragment : DialogFragment() {
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes)) { _, _ ->
                 (getTargetFragment() as ResolutionFragment).resetResolution()
            }
            .setCancelable(false)
            .create()
    }
}