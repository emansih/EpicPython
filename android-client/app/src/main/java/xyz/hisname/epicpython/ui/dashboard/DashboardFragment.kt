package xyz.hisname.epicpython.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentDashboardBinding
import xyz.hisname.epicpython.model.FoodModel
import xyz.hisname.epicpython.ui.addFood.AddFoodFragment
import xyz.hisname.epicpython.util.getImprovedViewModel
import xyz.hisname.epicpython.util.toastInfo

class DashboardFragment: Fragment() {

    private var fragmentDashboardBinding: FragmentDashboardBinding? = null
    private val binding get() = fragmentDashboardBinding!!
    private lateinit var gpsPermission: ActivityResultLauncher<String>
    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val dashboardViewModel by lazy { getImprovedViewModel(DashboardViewModel::class.java) }
    private val foodList = arrayListOf<FoodModel>()
    private val foodAdapter by lazy { FoodAdapters(foodList) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentDashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
            if(success) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    toastInfo("Getting donors near you...")
                    locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                    locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                }
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            getNearbyDonors(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = foodAdapter


        binding.fabAction.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddFoodFragment())
                addToBackStack(null)
            }
        }
        gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    // This code is disgusting
    private fun getNearbyDonors(latitude: Double, longitude: Double){
        val db = Firebase.firestore
        val center = GeoLocation(latitude, longitude)
        // 30km radius of user
        val radiusInM = (30 * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = ArrayList<Task<QuerySnapshot>>()
        bounds.forEach { b ->
            val query = db.collection("users")
                .whereEqualTo("userType", "restaurant")
                .orderBy("geohash")
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
            matchingDocs.forEach {  documentSnapshot ->
                val uid = documentSnapshot.get("uid")
                val query = db.collection("food")
                    .orderBy("timestamp")
                    .whereEqualTo("uid", uid)
                query.addSnapshotListener { value, error ->
                    if(value != null){
                        value.documents.forEach {  documentSnapshot ->
                            db.collection("users")
                                .whereEqualTo("uid",
                                    documentSnapshot.get("uid").toString()).get().addOnCompleteListener {  queryTask ->
                                    val storage = Firebase.storage
                                    val storageRef = storage.reference
                                    foodList.clear()
                                    val userAttribute = queryTask.result?.documents?.get(0)
                                    try {
                                        val attachmentRef = storageRef.child("attachments/" + documentSnapshot.id)
                                        attachmentRef.listAll().addOnSuccessListener {  listResult ->
                                            if(listResult.items.isEmpty()){
                                                foodList.add(
                                                    FoodModel(
                                                        documentSnapshot.get("uid").toString(),
                                                        documentSnapshot.get("timestamp").toString(),
                                                        documentSnapshot.get("description").toString(),
                                                        userAttribute?.get("name").toString(),
                                                        "", userAttribute?.get("latitude").toString(),
                                                        userAttribute?.get("longitude").toString(),
                                                        latitude, longitude
                                                    )
                                                )
                                            } else {
                                                listResult.items[0].downloadUrl.addOnSuccessListener { uri ->
                                                    foodList.add(
                                                        FoodModel(
                                                            documentSnapshot.get("uid").toString(),
                                                            documentSnapshot.get("timestamp").toString(),
                                                            documentSnapshot.get("description").toString(),
                                                            queryTask.result?.documents?.get(0)?.get("name").toString(),
                                                            uri.toString(), userAttribute?.get("latitude").toString(),
                                                            userAttribute?.get("longitude").toString(),
                                                            latitude, longitude
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } catch (exception: Exception){
                                        foodList.add(
                                            FoodModel(
                                                documentSnapshot.get("uid").toString(),
                                                documentSnapshot.get("timestamp").toString(),
                                                documentSnapshot.get("description").toString(),
                                                userAttribute?.get("name").toString(),
                                                "", userAttribute?.get("latitude").toString(),
                                                userAttribute?.get("longitude").toString(),
                                                latitude, longitude
                                            )
                                        )
                                    }
                                }
                        }
                        foodAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }



}