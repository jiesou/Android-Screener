package top.jiecs.screener.ui.resolution

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.jiecs.screener.R
import top.jiecs.screener.units.ApiCaller

class ConfirmationDialogFragment : DialogFragment() {

    private lateinit var apiCaller: ApiCaller
    private lateinit var dialog: AlertDialog
    private lateinit var negativeButton: Button

    private val confirmationDialogViewModel: ConfirmationDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        apiCaller = ApiCaller()
        confirmationDialogViewModel.confirmCountdown.observe(this) {
            if (!::negativeButton.isInitialized) return@observe
            if (it == 0) {
                confirmationDialogViewModel.confirmCountdownJob?.cancel()
                negativeButton.performClick()
            } else {
                negativeButton.text = getString(R.string.undo_changes, "${it}s")
            }
        }

        isCancelable = false
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes), null)
            .create()
        dialog.setOnShowListener { onDialogShow() }
        return dialog
    }

    private fun onDialogShow() {
        negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

        negativeButton.setOnClickListener {
            negativeButton.text = getString(R.string.undo_changes, getString(R.string.undone))
            apiCaller.resetResolution()
        }

        if (confirmationDialogViewModel.confirmCountdownJob == null) {
            startConfirmCountdown()
        } else {
            confirmationDialogViewModel.confirmCountdown.postValue(0)
        }
    }

    private fun startConfirmCountdown() {
        confirmationDialogViewModel.confirmCountdownJob =
            CoroutineScope(Dispatchers.Main).launch {
                for (countdown in 3 downTo 0) {
                    confirmationDialogViewModel.confirmCountdown.postValue(countdown)
                    delay(1000)
                }
            }
    }
}