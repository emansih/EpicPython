package xyz.hisname.epicpython.ui.addFood

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.Instant

class AddFoodViewModel(application: Application): AndroidViewModel(application) {

    val isDataSaved = MutableLiveData<Boolean>()
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(getApplication()) }


    fun storeData(description: String, amount: String, dateExpire: String, dietary: String,
                  additionalNotes: String, attachment: ArrayList<Uri>){
        val db = Firebase.firestore
        val userId = sharedPref.getString("userId", "") ?: ""
        val timestamp = Instant.now().epochSecond

        val userAttribute = hashMapOf(
            "description" to description,
            "amount" to amount,
            "dateExpire" to dateExpire,
            "additionalNotes" to additionalNotes,
            "uid" to  userId,
            "timestamp" to timestamp,
            "dietary" to dietary
        )

        db.collection("food")
            .add(userAttribute)
            .addOnSuccessListener { documentReference ->
                if(attachment.isEmpty()){
                    isDataSaved.postValue(true)
                } else {
                    val storage = Firebase.storage
                    val storageRef = storage.reference
                    attachment.forEach { uri ->
                        val attachmentRef = storageRef.child("attachments/" + documentReference.id + "/" + uri.lastPathSegment)
                        attachmentRef.putFile(uri)
                    }
                    isDataSaved.postValue(true)
                }
            }
    }
}