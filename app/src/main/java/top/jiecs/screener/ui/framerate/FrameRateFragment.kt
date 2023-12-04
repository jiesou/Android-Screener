package top.jiecs.screener.ui.framerate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentFrameRateBinding

class FrameRateFragment : Fragment() {

    private var _binding: FragmentFrameRateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val frameRateViewModel =
            ViewModelProvider(this).get(FrameRateViewModel::class.java)

        _binding = FragmentFrameRateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textFrameRate
        frameRateViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}