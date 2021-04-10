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
import androidx.preference.PreferenceManager
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import xyz.hisname.epicpython.BuildConfig
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentDonorLocationBinding
import xyz.hisname.epicpython.model.FoodModel
import xyz.hisname.epicpython.util.toastInfo
import java.io.File

class DonorLocationFragment: Fragment() {

    private var donorLocationBinding: FragmentDonorLocationBinding? = null
    private val binding get() = donorLocationBinding!!
    private lateinit var gpsPermission: ActivityResultLauncher<String>
    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        donorLocationBinding = FragmentDonorLocationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
            if(success) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    toastInfo("Getting locations of donors near you...")
                    locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                    locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            getNearbyDonors(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun getNearbyDonors(latitude: Double, longitude: Double) {
        val db = Firebase.firestore
        val center = GeoLocation(latitude, longitude)
        // 30km
        val radiusInM = (30 * 1000).toDouble()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks = ArrayList<Task<QuerySnapshot>>()
        bounds.forEach { b ->
            val query = db.collection("users")
                .whereIn("userType", arrayListOf("restaurant", "private_donor"))
                .orderBy("geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(query.get())
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            val coords = arrayListOf<Pair<Double, Double>>()
            tasks.forEach { snapshot ->
                snapshot.result?.documents?.forEach {  documentSnapshot ->
                    val lat = documentSnapshot.getDouble("latitude") ?: 0.0
                    val lng = documentSnapshot.getDouble("longitude") ?: 0.0
                    val docLocation = GeoLocation(lat, lng)
                    val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                    if (distanceInM <= radiusInM) {
                        coords.add(Pair(lat, lng))
                    }
                }
            }
            if(coords.isNotEmpty()){
                showMapLocation(coords)
            }
        }
    }

    private fun showMapLocation(arrayOfCoords: ArrayList<Pair<Double, Double>>){
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        val startMarker = Marker(binding.maps)
        arrayOfCoords.forEach { coords ->
            val location = GeoPoint(coords.first, coords.second)
            startMarker.position = location
            binding.maps.overlays.add(startMarker)
        }
        val location = GeoPoint(arrayOfCoords[0].first, arrayOfCoords[0].second)
        binding.maps.controller.animateTo(location)
        binding.maps.setMultiTouchControls(false)
        binding.maps.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.maps.setTileSource(TileSourceFactory.MAPNIK)
        startMarker.icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_map_marker
            colorRes = R.color.md_red_700
            sizeDp = 16
        }
        binding.maps.controller.setZoom(18.0)
    }

}