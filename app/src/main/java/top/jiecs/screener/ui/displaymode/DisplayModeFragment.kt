package top.jiecs.screener.ui.displaymode

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.jiecs.screener.R

/**
 * A fragment for configuring the display modes
 */
class DisplayModeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display_mode, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = DisplayModeRecyclerViewAdapter(DisplayModeContent.DISPLAY_MODES)
            }
        }
        return view
    }
}