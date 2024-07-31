package top.jiecs.screener.ui.displaymode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Helper class for providing display modes for user interfaces.
 */
class DisplayModeViewModel : ViewModel() {
    val list: MutableLiveData<MutableList<DisplayMode>> by lazy {
        MutableLiveData<MutableList<DisplayMode>>()
    }

    init {
        viewModelScope.launch {
            DataStoreManager().read()
                .collect { newList: MutableList<DisplayMode> ->
                    list.postValue(newList)
                }
        }
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
        override fun toString(): String =
            "${resolutionWidth.toInt()}x${resolutionHeight.toInt()} @ ${dpi.toInt()}dpi"
    }

}


