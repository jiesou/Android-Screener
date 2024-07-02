package top.jiecs.screener.ui.resolution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import top.jiecs.screener.R
import top.jiecs.screener.databinding.FragmentResolutionBinding


class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val resolutionViewModel: ResolutionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (resolutionViewModel.isResolutionDialogShown) {
            return
        }
        // nav to ResolutionDialogFragment
        val navController = findNavController()
        navController.navigate(R.id.nav_resolution_dialog)
        resolutionViewModel.isResolutionDialogShown = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
