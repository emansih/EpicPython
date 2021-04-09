package xyz.hisname.epicpython.ui.addFood

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import xyz.hisname.epicpython.BuildConfig
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentMapBinding
import xyz.hisname.epicpython.model.LocationSearchModel
import xyz.hisname.epicpython.util.getViewModel
import xyz.hisname.epicpython.util.toastInfo
import java.io.File

class MapFragment: Fragment() {

    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mapsViewModel by lazy { getViewModel(MapViewModel::class.java) }
    private val longitudeBundle by lazy { arguments?.getString("longitude") }
    private val latitudeBundle by lazy { arguments?.getString("latitude") }
    private lateinit var startMarker: Marker
    private lateinit var cloneLocationList: List<LocationSearchModel>
    private lateinit var gpsPermission: ActivityResultLauncher<String>
    private var fragmentMapBinding: FragmentMapBinding? = null
    private val binding get() = fragmentMapBinding!!
    private val mapController by lazy { binding.maps.controller }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentMapBinding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
            if(success) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    toastInfo("Waiting for location...")
                    locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                    locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        startMarker = Marker(binding.maps)
        if(!latitudeBundle.isNullOrEmpty() && !longitudeBundle.isNullOrEmpty()){
            setMap(GeoPoint(latitudeBundle?.toDouble() ?: 37.276675,
                longitudeBundle?.toDouble() ?: -115.798936))
        } else {
            isGpsEnabled()
        }
        setMapClick()
        setFab()
        searchLocation()
        binding.okButton.setOnClickListener {

            mapsViewModel.latitude.postValue(startMarker.position.latitude)
            mapsViewModel.longitude.postValue(startMarker.position.longitude)
            parentFragmentManager.popBackStack()
        }
        binding.cancelButton.setOnClickListener {
            mapsViewModel.latitude.postValue(0.0)
            mapsViewModel.longitude.postValue(0.0)
            parentFragmentManager.popBackStack()
        }
    }

    private fun setMap(location: GeoPoint){
        startMarker.position = location
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.maps.setMultiTouchControls(true)
        binding.maps.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.maps.overlays.add(startMarker)
        binding.maps.setTileSource(TileSourceFactory.MAPNIK)
        startMarker.icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_map_marker
            colorRes = R.color.md_red_700
            sizeDp = 16
        }
        mapController.animateTo(location)
        mapController.setZoom(18.0)
    }

    private fun searchLocation(){
        binding.mapSearch.setOnKeyListener { v, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                location(binding.mapSearch.text.toString())
            }
            false
        }
        binding.mapSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                if(editable.isNotBlank()) {
                    location(editable.toString())
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {}

        })
        binding.mapSearch.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            setMap(GeoPoint(cloneLocationList[i].lat, cloneLocationList[i].lon))
        }
    }

    private fun location(query: String){
        mapsViewModel.getLocationFromQuery(query).observe(viewLifecycleOwner){ data ->
            if(data.isNotEmpty()){
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, data)
                binding.mapSearch.setAdapter(adapter)
            }
        }
    }

    private fun setMapClick(){
        val mapReceiver = object : MapEventsReceiver {
            override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                setMap(geoPoint)
                return true
            }

            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                setMap(geoPoint)
                return true
            }
        }
        binding.maps.overlays.add(MapEventsOverlay(mapReceiver))
    }

    private fun setFab(){
        binding.fabMap.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_my_location
            colorRes = R.color.md_black_1000
            sizeDp = 16
        })
        binding.fabMap.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)){
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Grant access to location data?")
                        .setMessage("Choosing coordinates data is simple when location data permission is granted. " +
                                "Otherwise you may have to manually search for your location")
                        .setPositiveButton("OK"){_,_ ->
                            gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        .setNegativeButton("No"){ _,_ ->
                            toastInfo("Alright...")
                        }
                        .show()
                } else {
                    gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            setMap(GeoPoint(location.latitude, location.longitude))
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun isGpsEnabled(){
        if(!locationService.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder(requireActivity())
                .setMessage("For a better experience turn on device's location")
                .setPositiveButton("Sure"){_, _ ->
                    requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No"){ _, _ ->
                    toastInfo("Alright...Using Network data instead.")
                }
                .show()
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                toastInfo("Acquiring current location...")
                locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            } else {
                setMap(GeoPoint(37.276675, -115.798936))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.maps.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.maps.onPause()
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationService.removeUpdates(locationListener)
        }
    }
}