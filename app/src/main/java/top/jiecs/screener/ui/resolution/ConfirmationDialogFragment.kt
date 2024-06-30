package top.jiecs.screener.ui.resolution
import android.os.Bundle
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import top.jiecs.screener.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface

import kotlinx.coroutines.Job

import top.jiecs.screener.units.ApiCaller
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConfirmationDialogFragment : DialogFragment() {

    private lateinit var apiCaller: ApiCaller
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes), null)
            .create()
        apiCaller = ApiCaller()

        dialog.setOnShowListener {
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            confirmCountdown.observe(this) {
                negativeButton.text = getString(R.string.undo_changes, "${it}s")
            }
            startConfirmCountdownTo {
                if (isAdded && dialog.isShowing) {
                    negativeButton.performClick()
                }
            }

            negativeButton.setOnClickListener {
                confirmCountdownJob.value?.cancel()
                apiCaller.resetResolution()
                negativeButton.text =
                    getString(R.string.undo_changes, getString(R.string.undone))
            }
        }
        return dialog
    }

    private val confirmCountdown: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    private val confirmCountdownJob: MutableLiveData<Job> by lazy {
        MutableLiveData<Job>()
    }
    private fun startConfirmCountdownTo(callback: () -> Unit) {
        confirmCountdownJob.value = CoroutineScope(Dispatchers.Main).launch {
            for (countdown in 3 downTo 0) {
                confirmCountdown.postValue(countdown)
                delay(1000)
            }
            callback()
        }
    }
}