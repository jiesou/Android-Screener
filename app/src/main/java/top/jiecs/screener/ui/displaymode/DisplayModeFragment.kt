package top.jiecs.screener.ui.displaymode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import top.jiecs.screener.R

/**
 * A fragment for configuring the display modes
 */
class DisplayModeFragment : Fragment() {
    private val viewModel: DisplayModeViewModel by viewModels()
    private lateinit var mAdapter: DisplayModeRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display_mode, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            mAdapter = DisplayModeRecyclerViewAdapter(viewModel, lifecycleScope)

            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
            }

            viewModel.list.observe(viewLifecycleOwner) {
                mAdapter.updateList(it)
            }
        }

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.display_mode_action_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.new_display_mode -> {
                        findNavController().navigate(R.id.nav_display_mode_set)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return view
    }
}