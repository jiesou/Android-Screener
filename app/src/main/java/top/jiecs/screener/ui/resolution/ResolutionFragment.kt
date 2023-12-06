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
import android.os.Build
import android.view.Display
import android.util.DisplayMetrics

class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null
   
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var display: Display

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val resolutionViewModel =
            ViewModelProvider(this).get(ResolutionViewModel::class.java)

        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        display = requireActivity().windowManager.defaultDisplay
        resolutionViewModel.fetchScreenResolution(display)
        
        val textWidth = binding.textWidth.editText!!
        val textHeight = binding.textHeight.editText!!
        resolutionViewModel.resolutionPair.observe(viewLifecycleOwner) {
            textWidth.setText(it.first.toString())
            textHeight.setText(it.second.toString())
        }
        
        val textView = binding.textResolution
        resolutionViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        
        binding.btApply.setOnClickListener {
            val width = textWidth.text.toString().toInt()
            val height = textHeight.text.toString().toInt()
            
            //val displayManager: DisplayManager = context.getSystemService(Context.DISPLAY_SERVICE)
            //val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        
            //val size = android.util.Size(width, height)
            //val densityDpi = 560

            //displayManager.changeDisplayAttributes(display, size, densityDpi)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}
