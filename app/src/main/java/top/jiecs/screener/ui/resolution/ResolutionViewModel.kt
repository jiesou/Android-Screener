package top.jiecs.screener.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import android.view.Display
import android.util.DisplayMetrics

class ResolutionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is resolution Fragment"
    }
    val text: LiveData<String> = _text
    
    private val _resolutionPair = MutableLiveData<Pair<Int, Int>>()
    val resolutionPair: LiveData<Pair<Int, Int>> = _resolutionPair

    fun fetchScreenResolution(display: Display) { 
        val metrics = DisplayMetrics()

        display.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        _resolutionPair.value = Pair(width, height)
    }
}