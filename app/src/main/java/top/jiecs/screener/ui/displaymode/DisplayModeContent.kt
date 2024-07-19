package top.jiecs.screener.ui.displaymode

import DataStoreManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import top.jiecs.screener.MyApplication
import top.jiecs.screener.ui.displaymode.DisplayModeContent.adapter

/**
 * Helper class for providing display modes for user interfaces.
 */
object DisplayModeContent {
    /**
     * An array of sample display mode items.
     */
    val DISPLAY_MODES: DisplayModeList = DisplayModeList()

    var adapter: DisplayModeRecyclerViewAdapter? = null

    init {
        GlobalScope.launch {
            val displayModesFromDataStore = DataStoreManager(MyApplication.context).readDisplayModes().toMutableList()
            // Since we are in a coroutine, we can't directly modify the DISPLAY_MODES here because it's not thread-safe.
            // Consider posting the result back to the main thread or using a thread-safe collection.
            DISPLAY_MODES.addAll(displayModesFromDataStore)
        }
        // Add some sample items.
//        DISPLAY_MODES.add(DisplayMode(3200f, 1440f, 560f))
//        DISPLAY_MODES.add(DisplayMode(2400f, 1080f, 480f))
//        DISPLAY_MODES.add(DisplayMode(1600f, 720f, 280f))
    }

    /**
     * A item representing the display mode.
     */
    @Serializable
    data class DisplayMode(
        val resolutionHeight: Float,
        val resolutionWidth: Float,
        val dpi: Float
    ) {
        override fun toString(): String = "${resolutionWidth}x${resolutionHeight} @ ${dpi}dpi"
    }
}

class DisplayModeList(private val list: MutableList<DisplayModeContent.DisplayMode> = mutableListOf()) :
    MutableList<DisplayModeContent.DisplayMode> by list {
    override fun add(element: DisplayModeContent.DisplayMode): Boolean {
        // override add function for custom logic
        val returnValue = list.add(element)
        if (returnValue) {
            DataStoreManager(MyApplication.context).saveDisplayModes(list)
            adapter?.notifyDataSetChanged()
        }
        return returnValue
    }
    override fun remove(element: DisplayModeContent.DisplayMode): Boolean {
        // override remove function for custom logic
        val returnValue = list.remove(element)
        if (returnValue) {
            DataStoreManager(MyApplication.context).removeDisplayMode(element)
            adapter?.notifyDataSetChanged()
        }
        return returnValue
    }
}

