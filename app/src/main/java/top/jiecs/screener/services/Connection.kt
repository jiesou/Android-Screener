package top.jiecs.screener.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import top.jiecs.screener.IUserService

class Connection : ServiceConnection {

    var iUserService: IUserService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        iUserService = IUserService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        iUserService = null
    }
}