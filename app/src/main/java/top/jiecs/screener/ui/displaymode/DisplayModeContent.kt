package top.jiecs.screener.ui.displaymode

import DataStoreManager
import kotlinx.serialization.Serializable
import top.jiecs.screener.MyApplication

/**
 * Helper class for providing display modes for user interfaces.
 */
object DisplayModeContent {
    /**
     * An array of sample display mode items.
     */
    val DISPLAY_MODES: DisplayModeList = DisplayModeList()

    init {
        // Add some sample items.
        DISPLAY_MODES.add(DisplayMode(3200f, 1440f, 560f))
        DISPLAY_MODES.add(DisplayMode(2400f, 1080f, 480f))
        DISPLAY_MODES.add(DisplayMode(1600f, 720f, 280f))
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
            // save to data store
            DataStoreManager(MyApplication.context).saveDisplayModes(list)
        }
        return returnValue
    }
}

