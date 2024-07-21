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
    private val textWidth get() =  binding.resolutionEditor.textWidth.editText!!
    private val textDpi get() = binding.resolutionEditor.textDpi.editText!!

    private val scaledHeight get() = textHeight.text.toString().toFloatOrNull() ?: 0f
    private val scaledWidth get() = textWidth.text.toString().toFloatOrNull() ?: 0f
    private val scaledDpi get() = textDpi.text.toString().toFloatOrNull() ?: 0f

    private val physical get() = resolutionViewModel.physicalResolutionMap.value

    // Calculate the DPI that keeps the display size proportionally scaled
    // Get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
    // physical_adj_ratio = √(h²+w²/ph²+pw²)
    private val physicalAdjRatio get() = physical?.let {
        sqrt((scaledHeight.pow(2) + scaledWidth.pow(2)) /
                ( it["height"]?.pow(2)!! + it["width"]?.pow(2)!! )
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
            binding.textResolution.text = "Physical ${it["height"]}x${it["width"]}; DPI ${it["dpi"]}"
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
            updateDpiEditor(scaleValue = stuckScaleValue)
        }
        textWidth.doAfterTextChanged { editable: Editable? ->
            // auto calculate the other dimension when one dimension is changed
            if (editable.isNullOrBlank()) return@doAfterTextChanged
            if (physical == null) return@doAfterTextChanged
            val aspectRatio = physical!!["height"]!! / physical!!["width"]!!

            when (editable.hashCode()) {
                textHeight.text.hashCode() -> {
                    val equalRatioWidth = editable.toString().toInt() / aspectRatio
                    textWidth.setText(equalRatioWidth.roundToInt().toString())
                }

                textWidth.text.hashCode() -> {
                    val equalRatioHeight = editable.toString().toInt() * aspectRatio
                    textHeight.setText(equalRatioHeight.roundToInt().toString())
                }
            }
            updateDpiEditor(scaleValue = stuckScaleValue)
        }
        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            if (physical == null) return@doAfterTextChanged
            adjustSliderBasedOnDpi()
        }
        binding.btApply.setOnClickListener { v: View? ->
            if (mainViewModel.shizukuPermissionGranted.value != true) return@setOnClickListener
            apiCaller.applyResolution(
                scaledHeight.toInt(),
                scaledWidth.toInt(),
                scaledDpi.toInt()
            )
            val navController = findNavController()
            navController.navigate(R.id.nav_resolution_confirmation)
        }
        binding.btReset.setOnClickListener {
            if (mainViewModel.shizukuPermissionGranted.value != true) return@setOnClickListener
            apiCaller.resetResolution()
        }
    }

    private fun checkValidResolution(scaledValue: Int, dpi: Int): Boolean {
        if (scaledValue < -50 || scaledValue > 50) {
            binding.resolutionEditor.textDpi.error = getString(R.string.invalid)
            return false
        } else if (dpi < 280) {
            binding.resolutionEditor.textDpi.error = getString(R.string.invalid)
            return false
        } else {
            binding.resolutionEditor.textDpi.error = null
            return true
        }
    }

    private fun updateDpiEditor(scaleValue: Int) {
        val scaleRatio = (scaleValue * 0.01 + 1).toFloat()
        val scaledDpi = (baseDpi * scaleRatio).roundToInt()

        if (checkValidResolution(scaleValue, scaledDpi)) {
            binding.resolutionEditor.textDpi.editText!!.setText(scaledDpi.toString())
        }
    }

    private fun adjustSliderBasedOnDpi() {
        val scaledDpi =
            binding.resolutionEditor.textDpi.editText!!.text.toString().toFloatOrNull() ?: baseDpi
        // scale_ratio = scaled_dpi / (physical_dpi * physical_adj_ratio)
        val scaleRatio = scaledDpi / baseDpi
        // 0.5 -> -50 ; 1 -> 0 ; 1.25 -> 25
        val scaledValue = (scaleRatio - 1) * 100
        // scale_ratio = ((scale_ratio * 100).roundToInt()) / 100
        if (checkValidResolution(scaledValue.roundToInt(), scaledDpi.roundToInt())) {
            binding.resolutionEditor.sliderScale.value =
                ((scaledValue / 5).roundToInt() * 5).toFloat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}