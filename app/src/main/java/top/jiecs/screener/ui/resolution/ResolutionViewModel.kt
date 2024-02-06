package top.jiecs.screener.ui.resolution

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import android.view.Display
import android.util.Log
import java.lang.reflect.Field
import android.graphics.Point

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import top.jiecs.screener.units.ApiCaller
import org.lsposed.hiddenapibypass.HiddenApiBypass

class ResolutionViewModel : ViewModel() {

    val physicalResolutionMap: MutableLiveData<Map<String, Float>?> by lazy {
        MutableLiveData<Map<String, Float>?>()
    }
    val resolutionMap: MutableLiveData<Map<String, Float>?> by lazy {
        MutableLiveData<Map<String, Float>?>()
    }
    fun fetchScreenResolution() {
        val resolution = ApiCaller().fetchScreenResolution()
        physicalResolutionMap.value = resolution["physical"]
        resolutionMap.value = resolution["override"]
    }
    
    val usersList: MutableLiveData<List<Map<String, Any>?>> by lazy {
        MutableLiveData<List<Map<String, Any>?>>()
    }
    fun fetchUsers() {
        usersList.value = ApiCaller().fetchUsers()
    }
    
    val confirmCountdown: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val confirmCountdownJob: MutableLiveData<Job> by lazy {
        MutableLiveData<Job>()
    }
    fun startConfirmCountdownTo(callback: () -> Unit) {
        confirmCountdownJob.value = CoroutineScope(Dispatchers.Default).launch {
            for (countdown in 3 downTo 0) {
                confirmCountdown.value = countdown
                delay(1000)
            }
            callback()
        }
    }
    
}