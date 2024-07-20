package top.jiecs.screener.ui.displaymode

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import top.jiecs.screener.R
import top.jiecs.screener.databinding.DialogDisplayModeSetBinding
import top.jiecs.screener.ui.resolution.ResolutionFragment

class DisplayModeSetDialogFragment : DialogFragment() {

    private var _binding: DialogDisplayModeSetBinding? = null
    private val binding get() = _binding!!

    // tks: https://stackoverflow.com/a/70644415/20267613
    private val displayModeFragment: Fragment?
        get() = requireParentFragment().childFragmentManager.fragments[0]
    private val displayModeViewModel: DisplayModeViewModel by viewModels({ displayModeFragment!! })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDisplayModeSetBinding.inflate(LayoutInflater.from(context))

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.new_display_mode))
            .setView(binding.root)
            .setPositiveButton(R.string.apply) { _, _ ->
                // Respond to positive button press
                newDisplayMode()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun newDisplayMode() {
        val resolutionFragment = binding.resolutionFragment.getFragment<ResolutionFragment>()

        val displayMode = DisplayModeViewModel.DisplayMode(
            resolutionFragment.binding.resolutionEditor.textHeight.editText?.text.toString()
                .toFloat(),
            resolutionFragment.binding.resolutionEditor.textWidth.editText?.text.toString()
                .toFloat(),
            resolutionFragment.binding.resolutionEditor.textDpi.editText?.text.toString().toFloat()
        )
        displayModeViewModel.list.value?.add(displayMode)
        lifecycleScope.launch {
            DataStoreManager().save(displayModeViewModel.list.value!!)
        }
    }
}