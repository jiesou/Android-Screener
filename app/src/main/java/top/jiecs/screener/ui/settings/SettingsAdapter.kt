package top.jiecs.screener.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import top.jiecs.screener.R
import top.jiecs.screener.data.ResolutionConf

class SettingsAdapter(private val onClick: (ResolutionConf) -> Unit) :
    ListAdapter<ResolutionConf, SettingsAdapter.SettingsViewHolder>(ConfDiffCallback) {

    /* ViewHolder for Settings, takes in the inflated view and the onClick behavior. */
    class SettingsViewHolder(itemView: View, val onClick: (ResolutionConf) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val confTextView: TextView = itemView.findViewById(R.id.conf_text)
        private var currentConf: ResolutionConf? = null

        init {
            itemView.setOnClickListener {
                currentConf?.let {
                    onClick(it)
                }
            }
        }

        /* Bind ResolutionConfs name */
        fun bind(conf: ResolutionConf) {
            currentConf = conf

            confTextView.text = conf.text
            
        }
    }

    /* Creates and inflates view and return SettingsViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_resolution_conf_item, parent, false)
        return SettingsViewHolder(view, onClick)
    }

    /* Gets current conf and uses it to bind view. */
    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val conf = getItem(position)
        holder.bind(conf)

    }
}

object ConfDiffCallback : DiffUtil.ItemCallback<ResolutionConf>() {
    override fun areItemsTheSame(oldItem: ResolutionConf, newItem: ResolutionConf): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ResolutionConf, newItem: ResolutionConf): Boolean {
        return oldItem.id == newItem.id
    }
}
