package top.jiecs.screener.services

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.SystemServiceHelper
import top.jiecs.screener.BuildConfig
import top.jiecs.screener.IUserService
import java.lang.reflect.Field
import kotlin.system.exitProcess


class UserService : IUserService.Stub() {

    /* refer to https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/provider/Settings.java;l=5058 */
    private val settingPeakRefreshRate = "peak_refresh_rate"

    private lateinit var iActivityManager: Any

    init {
        iActivityManager = asInterface("android.app.IActivityManager", "activity")
    }

    private fun asInterface(className: String, serviceName: String): Any =
        SystemServiceHelper.getSystemService(serviceName).let {
            Class.forName("$className\$Stub").run {
                HiddenApiBypass.invoke(this, null, "asInterface", it)
            }
        }

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    private fun getContentProviderHolder(packageName: String, userId: Int): Any {
        val name = "$packageName.shizuku"
        return HiddenApiBypass.invoke(
            iActivityManager.javaClass,
            iActivityManager,
            "getContentProviderExternal",
            name, userId, null, name
        )
    }

    @SuppressLint("PrivateApi")
    override fun applyPeakRefreshRate(frameRate: Int): Boolean {
        Log.d("UserService", "applyPeakRefreshRate: $frameRate")
        val cpHolder = getContentProviderHolder(BuildConfig.APPLICATION_ID, 0)

        val cpHolderFields =
            HiddenApiBypass.getInstanceFields(Class.forName("android.app.ContentProviderHolder")) as List<Field>
        val providerField = cpHolderFields.first { it.name == "provider" }
        val iContentProvider = providerField.get(cpHolder)

        val attributionSourceConstructor =
            Class.forName("android.content.AttributionSource").getConstructor(
                Int::class.java,
                String::class.java,
                String::class.java
            )
        val attributionSource = attributionSourceConstructor.newInstance(
            Process.myUid(),
            BuildConfig.APPLICATION_ID,
            null
        )
        val authority = HiddenApiBypass.invoke(
            cpHolder::class.java,
            cpHolder,
            "uri"
        ) as String
        val mCallSetCommand =
            "PUT_system" // refer to https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/provider/Settings.java;l=4018
        val extras = Bundle().apply {
            putString(Settings.NameValueTable.VALUE, frameRate.toFloat().toString())
            putInt("user", 0)
        }

        val result = HiddenApiBypass.invoke(
            iContentProvider::class.java,
            iContentProvider,
            "call",
            attributionSource,
            authority,
            mCallSetCommand,
            settingPeakRefreshRate,
            extras
        )

        Log.d("UserService", "applyPeakRefreshRate: $result")

//        Settings.System.putFloat(contentResolver, settingPeakRefreshRate, frameRate.toFloat())
        return true
    }
}
