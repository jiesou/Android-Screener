package top.jiecs.screener.ui.resolution

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class ConfirmationDialogViewModel : ViewModel() {
    val confirmCountdown: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    var confirmCountdownJob: Job? = null


}