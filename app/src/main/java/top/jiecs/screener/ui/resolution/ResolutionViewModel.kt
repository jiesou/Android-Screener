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

    val physicalResolutionMap: MutableLiveData<Map<String, Float>> by lazy {
        MutableLiveData<Map<String, Float>>()
    }
    val resolutionMap: MutableLiveData<Map<String, Float>> by lazy {
        MutableLiveData<Map<String, Float>>()
    }
    fun fetchScreenResolution() {
        val windowManager = ApiCaller.iWindowManager

        val physicalSize = Point()
        HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getInitialDisplaySize", Display.DEFAULT_DISPLAY, physicalSize)
        val physicalDpi = HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getInitialDisplayDensity", Display.DEFAULT_DISPLAY) as Int
        physicalResolutionMap.value = mapOf(
          "height" to physicalSize.y.toFloat(),
          "width" to physicalSize.x.toFloat(),
          "dpi" to physicalDpi.toFloat())
        
        val overrideSize = Point()
        HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getBaseDisplaySize", Display.DEFAULT_DISPLAY, overrideSize)
        val overrideDpi = HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getBaseDisplayDensity", Display.DEFAULT_DISPLAY) as Int
        resolutionMap.value = mapOf(
          "height" to overrideSize.y.toFloat(),
          "width" to overrideSize.x.toFloat(),
          "dpi" to overrideDpi.toFloat())
    }
    
    val usersList: MutableLiveData<List<Map<String, Any>>> by lazy {
        MutableLiveData<List<Map<String, Any>>>()
    }
    fun fetchUsers() {
        val userManager = ApiCaller.iUserManager
        try {
            val users = HiddenApiBypass.invoke(userManager::class.java, userManager, "getUsers", true, true, true) as List<*>
            val userInfoFields = HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo")) as List<Field> ?: return
          
            val idField = userInfoFields.first { it.name == "id" }
            val nameField = userInfoFields.first { it.name == "name" }
            
            val mappedUsers = users.map { userInfo ->
                mapOf(
                    "id" to idField.get(userInfo)!!,
                    "name" to nameField.get(userInfo)!!
                )
            }
       
            usersList.value = mappedUsers
        } catch (e: Exception) {
            Log.e("fetchUsers", e.message, e)
        }
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