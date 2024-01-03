package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.chip.Chip
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentResolutionBinding

import top.jiecs.screener.R
import android.content.Context
import android.view.Display
import android.view.WindowManager
import android.content.DialogInterface
import android.text.TextWatcher
import android.text.Editable
import android.os.Handler
import android.os.Looper
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.roundToInt

import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.util.Log

class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null
   
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var resolutionViewModel: ResolutionViewModel
    
    private var userId = 0
    
    // determine the most accurate original scale value required by the user
    // The real-time changing ScaleSlider value will be changed
    // when scaling cannot be performed at the original value
    // Apply resolution still according to real-time value
    private var stuckScaleValue = 0

    companion object {
        lateinit var iWindowManager: Any
        lateinit var iUserManager: Any
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        resolutionViewModel =
            ViewModelProvider(this).get(ResolutionViewModel::class.java)

        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        iWindowManager = asInterface("android.view.IWindowManager", "window")
        iUserManager = asInterface("android.os.IUserManager", "user")
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
            Log.d("usersRecved", it.toString())
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
            if (fromUser) stuckScaleValue = value.toInt()
            updateDpiEditorOrScaleSlider(scale_value=value.toInt())
        }
        textWidth.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            val physical = resolutionViewModel.physicalResolutionMap.value ?: return@doAfterTextChanged
            val aspect_ratio = physical["height"]!! / physical["width"]!!

            when (s.hashCode()) {
                textHeight.text.hashCode() -> {
                    val equal_ratio_width = s.toString().toInt() / aspect_ratio
                    textWidth.setText(equal_ratio_width.roundToInt().toString())
                }
                textWidth.text.hashCode() -> {
                    val equal_ratio_height = s.toString().toInt() * aspect_ratio
                    textHeight.setText(equal_ratio_height.roundToInt().toString())
                }
            }
            updateDpiEditorOrScaleSlider(scale_value=stuckScaleValue)
        }
        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            updateDpiEditorOrScaleSlider(scale_value=null)
        }
        binding.btApply.setOnClickListener {
            applyResolution(textHeight.text.toString().toInt(),
              textWidth.text.toString().toInt(),
              textDpi.text.toString().toInt())
        }
        binding.btReset.setOnClickListener {
            resetResolution()
        }
        return root
    }
    
    private fun asInterface(className: String, serviceName: String): Any =
        ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName)).let {
            Class.forName("$className\$Stub").run {
                HiddenApiBypass.invoke(this, null, "asInterface", it)
            }
        }
    
    fun applyResolution(height: Int, width: Int, dpi: Int) {
        Log.d("screener", "apply")
        
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "setForcedDisplaySize", Display.DEFAULT_DISPLAY, width, height)
        
        // TODO: apply dpi for each user
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "setForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, dpi, userId)
        
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
          .setTitle(getString(R.string.success))
          .setMessage(getString(R.string.reset_hint))
          .setPositiveButton(getString(R.string.looks_fine), null)
          .setNegativeButton(getString(R.string.undo_changes)) { _, _ ->
               resetResolution()
          }
          .setCancelable(false)
          .show()
          
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        var countdown = 10
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
          override fun run() {
            if (isAdded) negativeButton.text = getString(R.string.undo_changes, "${countdown}s")

            if (countdown <= 0) {
              resetResolution()
              if (isAdded && dialog.isShowing) negativeButton.text =
                getString(R.string.undo_changes, getString(R.string.undone))
              return
            }
            countdown--
            handler.postDelayed(this, 1000)
          }
        }
        
        Handler(Looper.getMainLooper()).post(runnable)
        
        dialog.setOnDismissListener {
            handler.removeCallbacks(runnable)
        }
    }
    
    fun resetResolution() {
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "clearForcedDisplaySize", Display.DEFAULT_DISPLAY)
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "clearForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, userId)
    }
    
    fun updateDpiEditorOrScaleSlider(scale_value: Int?) {
        val scaled_height = binding.textHeight.editText!!.text.toString().toFloatOrNull() ?: return
        val scaled_width = binding.textWidth.editText!!.text.toString().toFloatOrNull() ?: return
        val physical = resolutionViewModel.physicalResolutionMap.value ?: return
        
        // Calculate the DPI that keeps the display size proportionally scaled
        // Get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
        // physical_adj_ratio = √(h²+w²/ph²+pw²)
        val physical_adj_ratio = sqrt((scaled_height.pow(2) + scaled_width.pow(2)) /
            (physical["height"]!!.pow(2) + physical["width"]!!.pow(2)))
        
        val base_dpi = physical["dpi"]!! * physical_adj_ratio
        if (scale_value === null) {
            val scaled_dpi = binding.textDpi.editText!!.text.toString().toFloatOrNull() ?: base_dpi
            // scale_ratio = scaled_dpi / (physical_dpi * physical_adj_ratio)
            val scale_ratio = scaled_dpi / base_dpi
            // 0.5 -> -50 ; 1 -> 0 ; 1.25 -> 25
            val scale_value = (scale_ratio - 1) * 100
            // Round to two decimal places
            // scale_ratio = ((scale_ratio * 100).roundToInt()) / 100
            if (scale_value < -50 || scale_value > 50) {
                binding.textDpi.error = "over limit"
                return
            } else {
                binding.sliderScale.value = ((scale_value / 5).roundToInt() * 5).toFloat()
                binding.textDpi.error = null
            }
        } else {
            // -50 -> 0.5 ; 0 -> 1 ; 25 -> 1.25
            val scale_ratio = (scale_value * 0.01 + 1).toFloat()
            // scaled_dpi = physical_dpi * physical_adj_ratio * scale_ratio
            val scaled_dpi = (base_dpi * scale_ratio).roundToInt()
            binding.textDpi.editText!!.setText(scaled_dpi.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}
