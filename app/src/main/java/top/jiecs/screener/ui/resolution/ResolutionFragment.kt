package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentResolutionBinding
import top.jiecs.screener.units.ApiCaller
import top.jiecs.screener.R

import android.text.Editable

import androidx.navigation.findNavController
import androidx.core.widget.doAfterTextChanged
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.roundToInt

import android.util.Log

class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null
   
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var resolutionViewModel: ResolutionViewModel
    
    private lateinit var apiCaller: ApiCaller
    
    // determine the most accurate original scale value required by the user
    // The real-time changing ScaleSlider value will be changed
    // when scaling cannot be performed at the original value
    // Apply resolution still according to real-time value
    private var stuckScaleValue = 0
    private var userId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resolutionViewModel =
            ViewModelProvider(this)[ResolutionViewModel::class.java]

        _binding = FragmentResolutionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiCaller = ApiCaller()
        resolutionViewModel.fetchScreenResolution()
        resolutionViewModel.fetchUsers()

        resolutionViewModel.physicalResolutionMap.observe(viewLifecycleOwner) {
            binding.textResolution.text = "Physical ${it["height"].toString()}x${it["width"].toString()}; DPI ${it["dpi"].toString()}"
        }
        val textHeight = binding.textHeight.editText!!
        val textWidth = binding.textWidth.editText!!
        val textDpi = binding.textDpi.editText!!
        resolutionViewModel.resolutionMap.observe(viewLifecycleOwner) {
            textHeight.setText(it["height"]?.toInt().toString())
            textWidth.setText(it["width"]?.toInt().toString())
            textDpi.setText(it["dpi"]?.toInt().toString())
        }
        val chipGroup = binding.chipGroup
        resolutionViewModel.usersList.observe(viewLifecycleOwner) {
            chipGroup.removeAllViews()
            for (user in it) {
                chipGroup.addView(Chip(chipGroup.context).apply {
                    text = "${user["name"]} (${user["id"]})"
                    isCheckable = true
                })
            }
            // set default checked chip
            val firstChip = chipGroup.getChildAt(0) as Chip
            firstChip.isChecked = true
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.d("index", checkedIds.toString())
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val index = checkedIds[0] - 1
            val users = resolutionViewModel.usersList.value!!
            if (index >= users.size) return@setOnCheckedStateChangeListener
            val currentUser = users[index]
            userId = currentUser["id"] as Int
        }
        binding.sliderScale.addOnChangeListener { _, value, fromUser ->
            value.toInt().let {
                if (fromUser) stuckScaleValue = it
                updateDpiEditorOrScaleSlider(scaleValue=it)
            }
        }
        textWidth.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            val physical = resolutionViewModel.physicalResolutionMap.value ?: return@doAfterTextChanged
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
            updateDpiEditorOrScaleSlider(scaleValue=stuckScaleValue)
        }
        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            updateDpiEditorOrScaleSlider(scaleValue=null)
        }
        binding.btApply.setOnClickListener { v: View? ->
            apiCaller.applyResolution(
                textHeight.text.toString().toInt(),
                textWidth.text.toString().toInt(),
                textDpi.text.toString().toInt(),
                userId
            )
            val navController = v?.findNavController()
            navController?.navigate(R.id.action_nav_resolution_to_nav_resolution_confirmation)
        }
        binding.btReset.setOnClickListener {
            apiCaller.resetResolution(userId)
        }
    }
    
    private fun updateDpiEditorOrScaleSlider(scaleValue: Int?) {
        val scaledHeight = binding.textHeight.editText!!.text.toString().toFloatOrNull() ?: return
        val scaledWidth = binding.textWidth.editText!!.text.toString().toFloatOrNull() ?: return
        val physical = resolutionViewModel.physicalResolutionMap.value ?: return
        
        // Calculate the DPI that keeps the display size proportionally scaled
        // Get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
        // physical_adj_ratio = √(h²+w²/ph²+pw²)
        val physicalAdjRatio = sqrt((scaledHeight.pow(2) + scaledWidth.pow(2)) /
            (physical["height"]!!.pow(2) + physical["width"]!!.pow(2)))
        
        val baseDpi = physical["dpi"]!! * physicalAdjRatio
        if (scaleValue === null) {
            val scaledDpi = binding.textDpi.editText!!.text.toString().toFloatOrNull() ?: baseDpi
            // scale_ratio = scaled_dpi / (physical_dpi * physical_adj_ratio)
            val scaleRatio = scaledDpi / baseDpi
            // 0.5 -> -50 ; 1 -> 0 ; 1.25 -> 25
            val scaledValue = (scaleRatio - 1) * 100
            // Round to two decimal places
            // scale_ratio = ((scale_ratio * 100).roundToInt()) / 100
            if (scaledValue < -50 || scaledValue > 50) {
                binding.textDpi.error = "over limit"
                return
            } else {
                binding.sliderScale.value = ((scaledValue / 5).roundToInt() * 5).toFloat()
                binding.textDpi.error = null
            }
        } else {
            // -50 -> 0.5 ; 0 -> 1 ; 25 -> 1.25
            val scaleRatio = (scaleValue * 0.01 + 1).toFloat()
            // scaled_dpi = physical_dpi * physical_adj_ratio * scale_ratio
            val scaledDpi = (baseDpi * scaleRatio).roundToInt()
            binding.textDpi.editText!!.setText(scaledDpi.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}
