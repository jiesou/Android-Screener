package top.jiecs.screener.units

import android.graphics.Point
import java.lang.reflect.Field
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

    fun fetchUsers(): List<Map<String, Any>> {
        try {
            val users = HiddenApiBypass.invoke(iUserManager::class.java, iUserManager, "getUsers", true, true, true) as List<*>
            val userInfoFields = HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo")) as List<Field>

            val idField = userInfoFields.first { it.name == "id" }
            val nameField = userInfoFields.first { it.name == "name" }

            return users.map { userInfo ->
                mapOf(
                    "id" to idField.get(userInfo)!!,
                    "name" to nameField.get(userInfo)!!
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun fetchScreenResolution(): Map<String, Map<String, Float>> {
        try {
            val physicalSize = Point()
            HiddenApiBypass.invoke(
                iWindowManager::class.java,
                iWindowManager,
                "getInitialDisplaySize",
                Display.DEFAULT_DISPLAY,
                physicalSize
            )
            val physicalDpi = HiddenApiBypass.invoke(
                iWindowManager::class.java,
                iWindowManager,
                "getInitialDisplayDensity",
                Display.DEFAULT_DISPLAY
            ) as Int

            val overrideSize = Point()
            HiddenApiBypass.invoke(
                iWindowManager::class.java,
                iWindowManager,
                "getBaseDisplaySize",
                Display.DEFAULT_DISPLAY,
                overrideSize
            )
            val overrideDpi = HiddenApiBypass.invoke(
                iWindowManager::class.java,
                iWindowManager,
                "getBaseDisplayDensity",
                Display.DEFAULT_DISPLAY
            ) as Int

            return mapOf(
                "physical" to mapOf(
                    "height" to physicalSize.y.toFloat(),
                    "width" to physicalSize.x.toFloat(),
                    "dpi" to physicalDpi.toFloat()
                ),
                "override" to mapOf(
                    "height" to overrideSize.y.toFloat(),
                    "width" to overrideSize.x.toFloat(),
                    "dpi" to overrideDpi.toFloat()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyMap()
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
