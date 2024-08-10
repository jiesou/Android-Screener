package top.jiecs.screener.services

import android.content.ContentResolver
import android.os.IBinder
import top.jiecs.screener.IUserService
import kotlin.system.exitProcess
import android.os.Process
import android.provider.Settings
import rikka.shizuku.Shizuku
import top.jiecs.screener.MyApplication

class UserService : IUserService.Stub() {

    /* refer to https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/provider/Settings.java;l=5058 */
    private val settingPeakRefreshRate = "peak_refresh_rate"

    var contentResolver: android.content.ContentResolver? = null

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    override fun setContentResolver(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    override fun applyPeakRefreshRate(frameRate: Int) : Boolean {
        Settings.System.putFloat(contentResolver, settingPeakRefreshRate, frameRate.toFloat())
        return true
    }
}