package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import top.jiecs.screener.R
import top.jiecs.screener.databinding.FragmentResolutionBinding


class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val resolutionViewModel =
            ViewModelProvider(this)[ResolutionViewModel::class.java]

        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // nav to ResolutionDialogFragment
        val navController = findNavController()
        navController.navigate(R.id.action_nav_resolution_to_nav_resolution_dialog)

    }

}
