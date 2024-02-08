package top.jiecs.screener.ui.displaymode

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import top.jiecs.screener.ui.displaymode.DisplayModeContent.DisplayMode
import top.jiecs.screener.databinding.FragmentDisplayModeBinding
import top.jiecs.screener.databinding.FragmentDisplayModeItemBinding

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
    }

    inner class ViewHolder(binding: FragmentDisplayModeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val displayMode: TextView = binding.displayModeText
    }

}