package top.jiecs.screener

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val shizukuPermissionGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}