package top.jiecs.screener.ui.framerate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.jiecs.screener.databinding.FragmentFrameRateBinding
import top.jiecs.screener.units.ApiCaller

class FrameRateFragment : Fragment() {

    private var _binding: FragmentFrameRateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var apiCaller: ApiCaller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val frameRateViewModel =
            ViewModelProvider(this)[FrameRateViewModel::class.java]

        _binding = FragmentFrameRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiCaller = ApiCaller()
        val btApply: Button = binding.btApply
        btApply.setOnClickListener{
            apiCaller.applyFrameRate(60)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}