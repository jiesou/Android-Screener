package top.jiecs.screener.ui.displaymode

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import top.jiecs.screener.R
import top.jiecs.screener.databinding.FragmentResolutionBinding

class DisplayModeSetDialogFragment : DialogFragment() {

    private var _binding: FragmentResolutionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.new_display_mode))
            .setView(R.layout.dialog_display_mode_set)
            .setPositiveButton(R.string.apply) { _, _ ->
                // Respond to positive button press
                newDisplayMode()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun newDisplayMode() {
        val displayMode = DisplayModeContent.DisplayMode(
            binding.resolutionEditor.textHeight.editText.toString().toFloat(),
            binding.resolutionEditor.textWidth.editText.toString().toFloat(),
            binding.resolutionEditor.textDpi.editText.toString().toFloat()
        )
        DisplayModeContent.DISPLAY_MODES.add(displayMode)
    }
}