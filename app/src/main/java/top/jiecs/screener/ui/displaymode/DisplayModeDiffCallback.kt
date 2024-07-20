import androidx.recyclerview.widget.DiffUtil
import top.jiecs.screener.ui.displaymode.DisplayModeViewModel

class DisplayModeDiffCallback(
    private val oldList: List<DisplayModeViewModel.DisplayMode>,
    private val newList: List<DisplayModeViewModel.DisplayMode>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].hashCode() == newList[newItemPosition].hashCode()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
