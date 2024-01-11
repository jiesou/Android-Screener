package top.jiecs.screener.ui.resolution
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface

import kotlinx.coroutines.Job

import top.jiecs.screener.ui.resolution.ResolutionFragment
import top.jiecs.screener.units.ApiCaller
import android.util.Log

class ConfirmationDialogFragment : DialogFragment() {
    
    private lateinit var resolutionViewModel: ResolutionViewModel
    private lateinit var apiCaller: ApiCaller
    private lateinit var dialog: AlertDialog
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes), null)
            .create()
         apiCaller = ApiCaller()
        resolutionViewModel =
            ViewModelProvider(requireParentFragment()).get(ResolutionViewModel::class.java)
       
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        //val navBackStackEntry = requireParentFragment().findNavController().getBackStackEntry(R.id.nav_resolution_confirmation) !!
        resolutionViewModel.confirmCountdown.observe(this) {
            Log.d("countdown", it.toString())
            negativeButton.text = getString(R.string.undo_changes, "${it}s")
        }
        resolutionViewModel.startConfirmCountdownTo {
            if (isAdded && dialog.isShowing) {
                negativeButton.performClick()
            }
        }
        
        negativeButton.setOnClickListener {
            (resolutionViewModel.confirmCountdownJob as Job).cancel()
            apiCaller?.resetResolution(0)
            negativeButton.text =
                getString(R.string.undo_changes, getString(R.string.undone))
        }
        
        return dialog
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
    }

    override fun onDestroy() {
        super.onDestroy()
        //countdownJob.cancel()
    }
}