package top.jiecs.screener.units

import android.view.Display

import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class ApiCaller {
    companion object {
        lateinit var iWindowManager: Any
        lateinit var iUserManager: Any
    }
    
    init {
        iWindowManager = asInterface("android.view.IWindowManager", "window")
        iUserManager = asInterface("android.os.IUserManager", "user")
    }

    private fun asInterface(className: String, serviceName: String): Any =
        ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName)).let {
            Class.forName("$className\$Stub").run {
                HiddenApiBypass.invoke(this, null, "asInterface", it)
            }
        }
    
    fun applyResolution(height: Int, width: Int, dpi: Int, userId: Int) {
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "setForcedDisplaySize", Display.DEFAULT_DISPLAY, width, height)
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "setForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, dpi, userId)
    }
    
    fun resetResolution(userId: Int) {
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "clearForcedDisplaySize", Display.DEFAULT_DISPLAY)
        HiddenApiBypass.invoke(iWindowManager::class.java, iWindowManager,
          "clearForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, userId)
    }
}
