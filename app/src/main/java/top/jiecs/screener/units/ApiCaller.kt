package top.jiecs.screener.units

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.ParcelFileDescriptor
import android.os.UserHandle
import android.provider.Settings
import android.view.Display
import moe.shizuku.server.IShizukuService
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import top.jiecs.screener.MyApplication
import top.jiecs.screener.services.UserServiceProvider
import java.lang.reflect.Field
import java.lang.reflect.Method


class ApiCaller {
    companion object {
        lateinit var iUserManager: Any
        lateinit var iWindowManager: Any
    }

    init {
        iUserManager = asInterface("android.os.IUserManager", "user")
        iWindowManager = asInterface("android.view.IWindowManager", "window")
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

    fun fetchScreenResolution(): Map<String, Map<String, Float>> = runCatching {
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
    }.getOrElse {
        it.printStackTrace()
        emptyMap()
    }

    fun getWriteSecureSettingsPermission() {
        if (MyApplication.context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED) {
            return
        }
//        UserHandle.of(
//            translateUserId(
//                userId,
//                UserHandle.USER_NULL, "runGrantRevokePermission"
//            )
//        );
//        HiddenApiBypass.invoke(
//            UserHandle::class.java,
//            null,
//            "of",
//            translateUserId(userId, -10000, "runGrantRevokePermission")
//        )
//        HiddenApiBypass.invoke(
//            iPermissionManager::class.java,
//            iPermissionManager,
//            "grantRuntimePermission",
//            "android.permission.WRITE_SECURE_SETTINGS",
//            MyApplication.context.packageName,
//            0
//        )
    }

    fun applyResolution(height: Float, width: Float, dpi: Float) {
        val resolution = mapOf(
            "height" to height.toInt(),
            "width" to width.toInt(),
            "dpi" to dpi.toInt()
        )
        HiddenApiBypass.invoke(
            iWindowManager::class.java,
            iWindowManager,
            "setForcedDisplaySize",
            Display.DEFAULT_DISPLAY,
            resolution["width"],
            resolution["height"]
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

    fun execute(command: String) {
        IShizukuService.Stub.asInterface(Shizuku.getBinder())
    }

    fun applyFrameRate(frameRate: Int) {

        UserServiceProvider.run {
            applyPeakRefreshRate(frameRate)
        }

//        val contentResolver = MyApplication.context.contentResolver
//        val systemSettingsFields =
//            HiddenApiBypass.getInstanceFields(Settings.System::class.java) as List<Field>
//        val putFloatForUserMethod =
//            systemSettingsFields.first { it.name == "putFloatForUser" } as Method
//        putFloatForUserMethod.isAccessible = true
//        putFloatForUserMethod.invoke(
//            null, contentResolver, settingPeakRefreshRate, frameRate.toFloat(), 0
//        )
        // Settings.System.putFloat(contentResolver, settingPeakRefreshRate, frameRate.toFloat())

        /* refer to https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/java/com/android/server/display/mode/DisplayModeDirector.java;l=1043 */
        // don't work
//        HiddenApiBypass.invoke(
//            iSettings::class.java, null,
//            "putFloatForUser", contentResolver, settingPeakRefreshRate, 60f, 0)
    }
}
