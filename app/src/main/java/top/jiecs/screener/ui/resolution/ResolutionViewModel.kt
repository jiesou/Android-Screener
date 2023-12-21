package top.jiecs.screener.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import android.os.Build
import android.view.Display
import android.util.DisplayMetrics
import android.view.WindowManager
// import android.content.pm.UserInfo
import android.util.Log
import java.lang.reflect.Method
import java.lang.reflect.Field

import org.lsposed.hiddenapibypass.HiddenApiBypass
import top.jiecs.screener.ui.resolution.ResolutionFragment

class ResolutionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Physical Resolution Override"
    }
    val text: LiveData<String> = _text
    
    
    val resolutionMap: MutableLiveData<Map<String, Int>> by lazy {
        MutableLiveData<Map<String, Int>>()
    }
    fun fetchScreenResolution(windowManager: WindowManager) { 
        val metrics = windowManager.currentWindowMetrics.bounds
        
        // TODO: get now Physical and Override size
        resolutionMap.value = mapOf(
          "height" to metrics.height(),
          "width" to metrics.width(),
          "dpi" to 520)
    }
    
    val usersList: MutableLiveData<List<Map<String, Any>>> by lazy {
        MutableLiveData<List<Map<String, Any>>>()
    }
    fun fetchUsers() { 
        val userManager = ResolutionFragment.iUserManager
        
        val users = HiddenApiBypass.invoke(userManager::class.java, userManager, "getUsers", true, true, true) as List<*>
        Log.d("users", users.toString())
        
        val userInfoFields = HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo"))
        //val idField = userInfoFields.stream().filter { e: Field -> e.getName() == "id" }.findFirst().get()
        //val nameField = userInfoFields.stream().filter { e: Field -> e.getName() == "name" }.findFirst().get()
        
       // for (userInfo in users) {
         // _usersList.postValue(mapOf(
          //  "id" to idField.get(userInfo),
         //   "name" to nameField.get(userInfo)
         //  // "id" to allInstanceFields.id.get(user),
        //   // "name" to allInstanceFields.name.get(user)
       //   ))
       // }
        
       // TODO
       
        usersList.setValue(
          listOf(mapOf(
             "id" to 0,
             "name" to "jiesou"
          ), mapOf(
             "id" to 10,
             "name" to "second user")
          )
        )
    }
    
}