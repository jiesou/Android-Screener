package top.jiecs.screener.ui.displaymode

/**
 * Helper class for providing display modes for user interfaces.
 */
object DisplayModeContent {

    /**
     * An array of sample display mode items.
     */
    val DISPLAY_MODES: MutableList<DisplayMode> = mutableListOf()

    init {
        // Add some sample items.
        DISPLAY_MODES.add(DisplayMode(3200f, 1440f, 560f))
        DISPLAY_MODES.add(DisplayMode(2400f, 1080f, 480f))
        DISPLAY_MODES.add(DisplayMode(1600f, 720f, 280f))
    }

    /**
     * A item representing the display mode.
     */
    data class DisplayMode(
        val resolutionHeight: Float,
        val resolutionWidth: Float,
        val dpi: Float
    ) {
        override fun toString(): String = "${resolutionWidth}x${resolutionHeight} @ ${dpi}dpi"
    }
}