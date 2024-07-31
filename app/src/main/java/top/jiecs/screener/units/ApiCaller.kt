package top.jiecs.screener.units

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.Display
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Field

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

    @SuppressLint("PrivateApi")
    fun fetchUsers(): List<Map<String, Any>> {
        try {
            val users = HiddenApiBypass.invoke(
                iUserManager::class.java,
                iUserManager,
                "getUsers",
                true,
                true,
                true
            ) as List<*>
            val userInfoFields =
                HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo")) as List<Field>

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

    fun applyResolution(height: Float, width: Float, dpi: Float) {
        val resolution = mapOf(
            "height" to height.toInt(),
            "width" to width.toInt(),
            "dpi" to dpi.toInt()
        )
        HiddenApiBypass.invoke(
            iWindowManager::class.java, iWindowManager,
            "setForcedDisplaySize", Display.DEFAULT_DISPLAY, resolution["width"], resolution["height"]
        )
        HiddenApiBypass.invoke(
            iWindowManager::class.java, iWindowManager,
            "setForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, resolution["dpi"], 0
        )
    }

    fun resetResolution() {
        HiddenApiBypass.invoke(
            iWindowManager::class.java, iWindowManager,
            "clearForcedDisplaySize", Display.DEFAULT_DISPLAY
        )
        HiddenApiBypass.invoke(
            iWindowManager::class.java, iWindowManager,
            "clearForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, 0
        )
    }
}
