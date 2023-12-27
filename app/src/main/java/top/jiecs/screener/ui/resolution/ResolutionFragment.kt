package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.chip.Chip
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentResolutionBinding

import top.jiecs.screener.R
import android.content.Context
import android.view.Display
import android.view.WindowManager
import android.content.DialogInterface
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
    
    private var userId = 0

    companion object {
        lateinit var iWindowManager: Any
        lateinit var iUserManager: Any
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val resolutionViewModel =
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
            textHeight.setText(it["height"].toString())
            textWidth.setText(it["width"].toString())
            textDpi.setText(it["dpi"].toString())
        }
        val chipGroup = binding.chipGroup
        resolutionViewModel.usersList.observe(viewLifecycleOwner) {
            Log.d("usersRecved", it.toString())
            chipGroup.removeAllViews()
            for (user in it) {
                chipGroup.addView(Chip(chipGroup.context).apply {
                    text = "${user["name"]} (${user["id"]})"
                    isCheckable = true
                    //isCheckedIconVisible = true
                })
            }
            // set default
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
        binding.silderScale.addOnChangeListener { _, value, _ ->
            // 0 -> 1; 25 -> 1.25
            val scale_ratio = value * 0.01 + 1
            val base_height = textHeight.text.toString().toFloat()
            val base_width = textWidth.text.toString().toFloat()
            val physical = resolutionViewModel.physicalResolutionMap.value ?: return@addOnChangeListener
            
            // calculate the DPI that keeps the display size proportionally scaled
            // get the ratio of virtual to physical resolution diagonal (pythagorean theorem)
            // base_physical_ratio = √(h²+w²/ph²+pw²)
            val base_physical_ratio = sqrt((base_height.pow(2) + base_width.pow(2)) /
                (physical["height"]!!.pow(2) + physical["width"]!!.pow(2)))
            
            // scaled_dpi = pdpi * base_physical_ratio * scale_ratio
            val scaled_dpi = (physical["dpi"]!! * base_physical_ratio * scale_ratio).roundToInt()
            
            Log.d("scaled_dpi", scaled_dpi.toString())
            
            binding.textDpi.editText!!.setText(scaled_dpi.toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}
