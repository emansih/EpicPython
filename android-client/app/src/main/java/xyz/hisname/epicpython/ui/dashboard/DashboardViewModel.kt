package xyz.hisname.epicpython.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardViewModel(application: Application): AndroidViewModel(application) {


    fun getNearbyDonors(latitude: Double, longitude: Double){
        val db = Firebase.firestore
        val center = GeoLocation(latitude, longitude)
        // 30km radius of user
        val radiusInM = (30 * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = ArrayList<Task<QuerySnapshot>>()
        bounds.forEach { b ->
            val query = db.collection("users").orderBy("geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(query.get())
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            val matchingDocs = ArrayList<DocumentSnapshot>()
            tasks.forEach { snapshot ->
                snapshot.result?.documents?.forEach {  documentSnapshot ->
                    val lat = documentSnapshot.getDouble("latitude") ?: 0.0
                    val lng = documentSnapshot.getDouble("longitude") ?: 0.0
                    val docLocation = GeoLocation(lat, lng)
                    val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                    if (distanceInM <= radiusInM) {
                        matchingDocs.add(documentSnapshot)
                    }
                }
            }
            val foodArray = ArrayList<DocumentSnapshot>()
            matchingDocs.forEach {  documentSnapshot ->
                val uid = documentSnapshot.get("uid")
                val query = db.collection("food")
                    .orderBy("timestamp")
                    .whereEqualTo("uid", uid)
                query.get().addOnSuccessListener {  querySnapshot ->
                    querySnapshot.documents.forEach { documentSnapshot ->
                        documentSnapshot.data?.forEach { t, u ->

                        }
                    }
                }

            }
        }

    }
}