package top.jiecs.screener.ui.resolution

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import top.jiecs.screener.MainViewModel
import top.jiecs.screener.R
import top.jiecs.screener.databinding.DialogResolutionContentBinding
import top.jiecs.screener.units.ApiCaller
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class ResolutionDialogFragment : DialogFragment() {

    private var _binding: DialogResolutionContentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val resolutionViewModel: ResolutionViewModel by viewModels()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var apiCaller: ApiCaller

    // determine the most accurate original scale value required by the user
    // The real-time changing ScaleSlider value will be changed
    // when scaling cannot be performed at the original value
    // Apply resolution still according to real-time value
    private var stuckScaleValue = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogResolutionContentBinding.inflate(LayoutInflater.from(context))

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        apiCaller = ApiCaller()

        mainViewModel.shizukuPermissionGranted.observe(this) {
            if (it) {
                resolutionViewModel.fetchScreenResolution()
            }
        }

        resolutionViewModel.physicalResolutionMap.observe(this) {
            binding.textResolution.text = "Physical ${it?.get("height").toString()}x${
                it?.get("width").toString()
            }; DPI ${it?.get("dpi").toString()}"
        }
        val textHeight = binding.resolutionEditor.textHeight.editText!!
        val textWidth = binding.resolutionEditor.textWidth.editText!!
        val textDpi = binding.resolutionEditor.textDpi.editText!!
        resolutionViewModel.resolutionMap.observe(this) {
            textHeight.setText(it?.get("height")?.toInt()?.toString())
            textWidth.setText(it?.get("width")?.toInt()?.toString())
            textDpi.setText(it?.get("dpi")?.toInt()?.toString())
        }
        val chipGroup = binding.resolutionEditor.chipGroup
        resolutionViewModel.usersList.observe(this) {
            if (it.isEmpty()) return@observe
            // for each user, create a chip to chip group
            chipGroup.removeAllViews()
            for (user in it) {
                chipGroup.addView(Chip(chipGroup.context).apply {
                    text = "${user?.get("name")} (${user?.get("id")})"
                    isCheckable = true
                })
            }
            // set default checked chip
            val firstChip = chipGroup.getChildAt(0) as Chip
            firstChip.isChecked = true
        }

        binding.resolutionEditor.sliderScale.addOnChangeListener { _, value, fromUser ->
            value.toInt().let {
                if (fromUser) stuckScaleValue = it
                updateDpiEditorOrScaleSlider(scaleValue = it)
            }
        }
        textWidth.doAfterTextChanged { s: Editable? ->
            // auto calculate the other dimension when one dimension is changed
            if (s.isNullOrBlank()) return@doAfterTextChanged
            val physical =
                resolutionViewModel.physicalResolutionMap.value ?: return@doAfterTextChanged
            val aspectRatio = physical["height"]!! / physical["width"]!!

            when (s.hashCode()) {
                textHeight.text.hashCode() -> {
                    val equalRatioWidth = s.toString().toInt() / aspectRatio
                    textWidth.setText(equalRatioWidth.roundToInt().toString())
                }

                textWidth.text.hashCode() -> {
                    val equalRatioHeight = s.toString().toInt() * aspectRatio
                    textHeight.setText(equalRatioHeight.roundToInt().toString())
                }
            }
            updateDpiEditorOrScaleSlider(scaleValue = stuckScaleValue)
        }
        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            updateDpiEditorOrScaleSlider(scaleValue = null)
        }
        binding.btApply.setOnClickListener { v: View? ->
            apiCaller.applyResolution(
                textHeight.text.toString().toInt(),
                textWidth.text.toString().toInt(),
                textDpi.text.toString().toInt()
            )
            val navController = v?.findNavController()
            navController?.navigate(R.id.action_nav_resolution_to_nav_resolution_confirmation)
        }
        binding.btReset.setOnClickListener {
            apiCaller.resetResolution()
        }

        return dialog
    }

    private fun updateDpiEditorOrScaleSlider(scaleValue: Int?) {
        val scaledHeight =
            binding.resolutionEditor.textHeight.editText!!.text.toString().toFloatOrNull() ?: return
        val scaledWidth =
            binding.resolutionEditor.textWidth.editText!!.text.toString().toFloatOrNull() ?: return
        val physical = resolutionViewModel.physicalResolutionMap.value ?: return

        // Calculate the DPI that keeps the display size proportionally scaled
        // Get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
        // physical_adj_ratio = √(h²+w²/ph²+pw²)
        val physicalAdjRatio = sqrt(
            (scaledHeight.pow(2) + scaledWidth.pow(2)) /
                    (physical["height"]!!.pow(2) + physical["width"]!!.pow(2))
        )

        val baseDpi = physical["dpi"]!! * physicalAdjRatio
        if (scaleValue === null) {
            val scaledDpi =
                binding.resolutionEditor.textDpi.editText!!.text.toString().toFloatOrNull()
                    ?: baseDpi
            // scale_ratio = scaled_dpi / (physical_dpi * physical_adj_ratio)
            val scaleRatio = scaledDpi / baseDpi
            // 0.5 -> -50 ; 1 -> 0 ; 1.25 -> 25
            val scaledValue = (scaleRatio - 1) * 100
            // Round to two decimal places
            // scale_ratio = ((scale_ratio * 100).roundToInt()) / 100
            if (scaledValue < -50 || scaledValue > 50) {
                binding.resolutionEditor.textDpi.error = "over limit"
                return
            } else {
                binding.resolutionEditor.sliderScale.value =
                    ((scaledValue / 5).roundToInt() * 5).toFloat()
                binding.resolutionEditor.textDpi.error = null
            }
        } else {
            // -50 -> 0.5 ; 0 -> 1 ; 25 -> 1.25
            val scaleRatio = (scaleValue * 0.01 + 1).toFloat()
            // scaled_dpi = physical_dpi * physical_adj_ratio * scale_ratio
            val scaledDpi = (baseDpi * scaleRatio).roundToInt()
            binding.resolutionEditor.textDpi.editText!!.setText(scaledDpi.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
