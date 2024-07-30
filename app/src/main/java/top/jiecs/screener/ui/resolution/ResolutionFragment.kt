package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import top.jiecs.screener.MainViewModel
import top.jiecs.screener.R
import top.jiecs.screener.databinding.FragmentResolutionBinding
import top.jiecs.screener.units.ApiCaller
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    val binding get() = _binding!!

    private val resolutionViewModel: ResolutionViewModel by viewModels()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var apiCaller: ApiCaller

    // determine the most accurate original scale value required by the user
    // The real-time changing ScaleSlider value will be changed
    // when scaling cannot be performed at the original value
    // Apply resolution still according to real-time value
    private var stuckScaleValue = 0

    private val textHeight get() = binding.resolutionEditor.textHeight.editText!!
    private val textWidth get() = binding.resolutionEditor.textWidth.editText!!
    private val textDpi get() = binding.resolutionEditor.textDpi.editText!!

    private val scaledHeight get() = textHeight.text.toString().toFloatOrNull() ?: 0f
    private val scaledWidth get() = textWidth.text.toString().toFloatOrNull() ?: 0f
    private val scaledDpi get() = textDpi.text.toString().toFloatOrNull() ?: 0f
    private val scaleValue get() = binding.resolutionEditor.sliderScale.value.toInt()

    private val physical get() = resolutionViewModel.physicalResolutionMap.value

    // Calculate the DPI that keeps the display size proportionally scaled
    // Get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
    // physical_adj_ratio = √(h²+w²/ph²+pw²)
    private val physicalAdjRatio
        get() = physical?.let {
            sqrt(
                (scaledHeight.pow(2) + scaledWidth.pow(2)) /
                        (it["height"]?.pow(2)!! + it["width"]?.pow(2)!!)
            )
        } ?: 1f
    private val baseDpi get() = physical?.get("dpi")!! * physicalAdjRatio

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiCaller = ApiCaller()

        mainViewModel.shizukuPermissionGranted.observe(viewLifecycleOwner) {
            if (it) resolutionViewModel.fetchScreenResolution()
        }

        resolutionViewModel.physicalResolutionMap.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.textResolution.text =
                "Physical ${it["height"]}x${it["width"]}; DPI ${it["dpi"]}"
        }
        resolutionViewModel.resolutionMap.observe(viewLifecycleOwner) {
            textHeight.setText(it?.get("height")?.toInt()?.toString())
            textWidth.setText(it?.get("width")?.toInt()?.toString())
            textDpi.setText(it?.get("dpi")?.toInt()?.toString())
        }
        val chipGroup = binding.resolutionEditor.chipGroup
        resolutionViewModel.usersList.observe(viewLifecycleOwner) {
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
            if (physical == null) return@addOnChangeListener
            if (fromUser) stuckScaleValue = value.toInt()
            updateDpiEditor()
        }
        textWidth.doAfterTextChanged { editable: Editable? ->
            handleWidthTextChange(editable ?: return@doAfterTextChanged)
        }
        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            if (physical == null) return@doAfterTextChanged
            adjustSliderBasedOnDpi()
        }
        binding.btApply.setOnClickListener {
            if (mainViewModel.shizukuPermissionGranted.value != true) return@setOnClickListener
            if (!checkValidResolution(null, null)) return@setOnClickListener
            apiCaller.applyResolution(
                scaledHeight,
                scaledWidth,
                scaledDpi
            )
            val navController = findNavController()
            navController.navigate(R.id.nav_resolution_confirmation)
        }
        binding.btReset.setOnClickListener {
            if (mainViewModel.shizukuPermissionGranted.value != true) return@setOnClickListener
            apiCaller.resetResolution()
        }
    }

    private fun checkValidResolution(updatedScaleValue: Int?, updatedDpi: Int?): Boolean {
        val scale = updatedScaleValue ?: scaleValue
        val dpi = updatedDpi ?: scaledDpi.toInt()

        if (scale !in -50..50 ||
            dpi !in 280..840 ||
            scaledHeight !in 480f..4096f ||
            scaledWidth !in 480f..4096f
        ) {
            binding.resolutionEditor.textDpi.error = getString(R.string.invalid)
            return false
        }
        binding.resolutionEditor.textDpi.error = null
        return true
    }

    private fun handleWidthTextChange(editable: Editable) {
        // auto calculate the other dimension when one dimension is changed
        if (editable.isBlank()) return
        if (physical == null) return
        val aspectRatio = physical!!["height"]!! / physical!!["width"]!!
        try {
            val equalRatioHeight = editable.toString().toInt() * aspectRatio
            textHeight.setText(equalRatioHeight.roundToInt().toString())
        } catch (e: NumberFormatException) {
            return
        }
        updateDpiEditor()
        checkValidResolution(null, null)
    }

    private fun updateDpiEditor() {
        // -50 -> 0.5 ; 0 -> 1 ; 25 -> 1.25
        val scaleRatio = (scaleValue * 0.01 + 1).toFloat()
        val updatedDpi = (baseDpi * scaleRatio).roundToInt()
        binding.resolutionEditor.textDpi.editText!!.setText(updatedDpi.toString())
        checkValidResolution(updatedScaleValue = null, updatedDpi = updatedDpi)
    }

    private fun adjustSliderBasedOnDpi() {
        // scale_ratio = scaled_dpi / (physical_dpi * physical_adj_ratio)
        val scaleRatio = scaledDpi / baseDpi
        // 0.5 -> -50 ; 1 -> 0 ; 1.25 -> 25
        val updatedScaleValue = (scaleRatio - 1) * 100
        // scale_ratio = ((scale_ratio * 100).roundToInt()) / 100
        if (checkValidResolution(updatedScaleValue = updatedScaleValue.toInt(), updatedDpi = null)) {
            binding.resolutionEditor.sliderScale.value = ((updatedScaleValue / 5).roundToInt() * 5).toFloat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}