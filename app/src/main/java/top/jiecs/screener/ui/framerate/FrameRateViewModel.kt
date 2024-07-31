package top.jiecs.screener.ui.framerate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FrameRateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "frameRate feature is not implemented yet"
    }
    val text: LiveData<String> = _text
}