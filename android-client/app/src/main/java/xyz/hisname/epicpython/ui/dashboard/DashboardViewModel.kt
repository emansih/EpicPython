package xyz.hisname.epicpython.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class DashboardViewModel(application: Application): AndroidViewModel(application) {

    val isSubmitted = MutableLiveData<Boolean>()
    val distance = MutableLiveData<Int>()
    val dietary = MutableLiveData<String>()

}