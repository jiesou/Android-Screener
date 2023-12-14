package top.jiecs.screener.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import android.view.Display
import android.util.DisplayMetrics
import android.view.WindowManager

class ResolutionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Physical Resolution Override"
    }
    val text: LiveData<String> = _text
    
    private val _resolutionMap = MutableLiveData<Map<String, Int>>()
    val resolutionMap: LiveData<Map<String, Int>> = _resolutionMap

    fun fetchScreenResolution(windowManager: WindowManager) { 
        val metrics = windowManager.currentWindowMetrics.bounds
        
        // TODO: get now Physical and Override size
        _resolutionMap.value = mapOf(
          "height" to metrics.height(),
          "width" to metrics.width(),
          "dpi" to 520)
    }
}