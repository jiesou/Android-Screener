package top.jiecs.screener.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import android.os.Build
import android.view.Display
import android.util.DisplayMetrics
import android.view.WindowManager
//import android.content.pm.UserInfo
import android.util.Log
import java.lang.reflect.Field
import android.graphics.Point

import top.jiecs.screener.units.ApiCaller
import org.lsposed.hiddenapibypass.HiddenApiBypass
import top.jiecs.screener.ui.resolution.ResolutionFragment

class ResolutionViewModel : ViewModel() {

    val physicalResolutionMap: MutableLiveData<Map<String, Float>> by lazy {
        MutableLiveData<Map<String, Float>>()
    }
    val resolutionMap: MutableLiveData<Map<String, Float>> by lazy {
        MutableLiveData<Map<String, Float>>()
    }
    fun fetchScreenResolution() { 
        val apiCaller = ResolutionFragment.apiCaller as ApiCaller
        val windowManager = ApiCaller.iWindowManager

        val physical_size = Point()
        HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getInitialDisplaySize", Display.DEFAULT_DISPLAY, physical_size)
        val physical_dpi = HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getInitialDisplayDensity", Display.DEFAULT_DISPLAY) as Int
        physicalResolutionMap.value = mapOf(
          "height" to physical_size.y.toFloat(),
          "width" to physical_size.x.toFloat(),
          "dpi" to physical_dpi.toFloat())
        
        val override_size = Point()
        HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getBaseDisplaySize", Display.DEFAULT_DISPLAY, override_size)
        val override_dpi = HiddenApiBypass.invoke(windowManager::class.java, windowManager, "getBaseDisplayDensity", Display.DEFAULT_DISPLAY) as Int
        resolutionMap.value = mapOf(
          "height" to override_size.y.toFloat(),
          "width" to override_size.x.toFloat(),
          "dpi" to override_dpi.toFloat())
    }
    
    val usersList: MutableLiveData<List<Map<String, Any>>> by lazy {
        MutableLiveData<List<Map<String, Any>>>()
    }
    fun fetchUsers() {
        val userManager = ApiCaller.iUserManager
        try {
            val users = HiddenApiBypass.invoke(userManager::class.java, userManager, "getUsers", true, true, true) as List<*>
            val userInfoFields = HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo")) as List<Field>

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
    
}