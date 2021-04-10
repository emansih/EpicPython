package xyz.hisname.epicpython.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OnboardingViewModel(application: Application): AndroidViewModel(application) {

    val isDataSaved = MutableLiveData<Boolean>()

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }


    fun saveUser(name: String, latitude: Double, longitude: Double, userType: String,
                 email: String, phoneNumber: String){
        val db = Firebase.firestore
        val user = if(userType.contentEquals("Restaurant")){
            "restaurant"
        } else if(userType.contentEquals("Food Donor")){
            "private_donor"
        } else {
            "inNeedOfFood"
        }

        val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))

        val userAttribute = hashMapOf(
            "name" to name,
            "latitude" to latitude,
            "longitude" to longitude,
            "geohash" to hash,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "userType" to user,
            "uid" to  sharedPref.getString("userId", "")
         )


        db.collection("users")
            .add(userAttribute)
            .addOnSuccessListener { documentReference ->
                isDataSaved.postValue(true)
            }
    }
}