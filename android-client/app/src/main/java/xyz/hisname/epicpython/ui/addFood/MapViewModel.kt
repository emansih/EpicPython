package xyz.hisname.epicpython.ui.addFood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.epicpython.data.NominatimClient
import xyz.hisname.epicpython.data.SearchService

class MapViewModel: ViewModel() {


    val latitude =  MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()


    fun getLocationFromQuery(query: String): LiveData<List<String>> {
        val locationList = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO){
            locationList.postValue(getQuery(query))
        }
        return locationList
    }

    private suspend fun getQuery(location: String): List<String> {
        val client = NominatimClient.getClient()
        val locationResult = arrayListOf<String>()
        try {
            client.create(SearchService::class.java).searchLocation(location).forEach { search ->
                locationResult.add(search.display_name)
            }
        } catch (exception: Exception) { }
        return locationResult
    }

    override fun onCleared() {
        super.onCleared()
        NominatimClient.destroyClient()
    }
}