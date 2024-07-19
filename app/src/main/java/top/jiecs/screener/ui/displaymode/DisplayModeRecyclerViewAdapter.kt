package top.jiecs.screener.ui.displaymode

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.jiecs.screener.databinding.FragmentDisplayModeItemBinding
import top.jiecs.screener.ui.displaymode.DisplayModeContent.DISPLAY_MODES
import top.jiecs.screener.ui.displaymode.DisplayModeContent.DisplayMode

/**
 * [RecyclerView.Adapter] that can display a [DisplayMode].
 */
class DisplayModeRecyclerViewAdapter(
    private val values: List<DisplayMode>
) : RecyclerView.Adapter<DisplayModeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentDisplayModeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val displayMode = values[position]
        holder.displayMode.text = displayMode.toString()

        holder.editButton.setOnClickListener {

        }
        holder.removeButton.setOnClickListener {
            DISPLAY_MODES.remove(displayMode)
        }
    }

    inner class ViewHolder(binding: FragmentDisplayModeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val displayMode: TextView = binding.displayModeText
        val editButton: Button = binding.editButton
        val removeButton: Button = binding.removeButton
    }

}