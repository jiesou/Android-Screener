package top.jiecs.screener.ui.resolution
import android.os.Bundle
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import top.jiecs.screener.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import top.jiecs.screener.ui.resolution.ResolutionFragment

class ConfirmationDialogFragment : DialogFragment() {
    
    private lateinit var countdownJob: Job
    
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
    
    override fun onStart() {
        super.onStart()
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            val dialog = dialog as AlertDialog
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            for (countdown in 10 downTo 0) {
                negativeButton?.text = getString(R.string.undo_changes, "${countdown}s")
                delay(1000)
            }
            if (isAdded && dialog.isShowing) negativeButton?.text =
                getString(R.string.undo_changes, getString(R.string.undone))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownJob.cancel()
    }
}