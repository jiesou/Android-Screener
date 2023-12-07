package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentResolutionBinding

import android.content.Context
import android.view.Display
import android.view.WindowManager

import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null
   
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var windowManager: WindowManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val resolutionViewModel =
            ViewModelProvider(this).get(ResolutionViewModel::class.java)

        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        windowManager = requireActivity().windowManager
        resolutionViewModel.fetchScreenResolution(windowManager)
        
        val textHeight = binding.textHeight.editText!!
        val textWidth = binding.textWidth.editText!!
        val textDpi = binding.textDpi.editText!!
        resolutionViewModel.resolutionMap.observe(viewLifecycleOwner) {
            textHeight.setText(it["height"].toString())
            textWidth.setText(it["width"].toString())
            textDpi.setText(it["dpi"].toString())
        }
        
        val textView = binding.textResolution
        resolutionViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        
        binding.btApply.setOnClickListener {
            applyResolution(textHeight.text.toString().toInt(),
              textWidth.text.toString().toInt(),
              textDpi.text.toString().toInt())
        }
        return root
    }
    
    fun applyResolution(height: Int, width: Int, dpi: Int) {
        //val displayManager: DisplayManager = context.getSystemService(Context.DISPLAY_SERVICE)
        //val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        //val size = android.util.Size(width, height)
        //val densityDpi = 560

        //displayManager.changeDisplayAttributes(display, size, densityDpi)
        
        // reset display if no action in 10s
        MaterialAlertDialogBuilder(requireContext())
          .setTitle(R.string.success)
          .setMessage(getString(R.string.reset_hint))
          .setPositiveButton(getString(R.string.looks_fine), null)
          .setNegativeButton(getString(R.string.undo_changes)) { _, _ ->
              // val size = android.util.Size(width, height)
              // val densityDpi = 560
              // displayManager.changeDisplayAttributes(display, size, densityDpi)
          }
          .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}
