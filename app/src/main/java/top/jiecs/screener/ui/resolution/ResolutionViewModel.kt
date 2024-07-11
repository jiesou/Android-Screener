package top.jiecs.screener.ui.resolution

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.jiecs.screener.units.ApiCaller

class ResolutionViewModel : ViewModel() {

    val physicalResolutionMap: MutableLiveData<Map<String, Float>?> by lazy {
        MutableLiveData<Map<String, Float>?>()
    }
    val resolutionMap: MutableLiveData<Map<String, Float>?> by lazy {
        MutableLiveData<Map<String, Float>?>()
    }

    fun fetchScreenResolution() {
        val resolution = ApiCaller().fetchScreenResolution()
        physicalResolutionMap.value = resolution["physical"]
        resolutionMap.value = resolution["override"]
    }

    val usersList: MutableLiveData<List<Map<String, Any>?>> by lazy {
        MutableLiveData<List<Map<String, Any>?>>()
    }

    fun fetchUsers() {
        usersList.value = ApiCaller().fetchUsers()
    }
}