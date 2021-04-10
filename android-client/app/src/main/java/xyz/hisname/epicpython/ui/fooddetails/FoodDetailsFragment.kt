package xyz.hisname.epicpython.ui.fooddetails

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import xyz.hisname.epicpython.BuildConfig
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentFoodDetailsBinding
import java.io.File

class FoodDetailsFragment: Fragment() {

    private var fragmentFoodDetailsBinding: FragmentFoodDetailsBinding? = null
    private val binding get() = fragmentFoodDetailsBinding!!
    private val imagesList = arrayListOf<Uri>()
    private val foodAdapter by lazy { FoodImagesAdapter(imagesList) }
    private val databaseId: String by lazy { arguments?.getString("databaseId") as String  }
    private val userId: String by lazy { arguments?.getString("userId") as String  }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentFoodDetailsBinding = FragmentFoodDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.imageRecyclerView.layoutManager = layoutManager
        binding.imageRecyclerView.adapter = foodAdapter
        setImages(databaseId)
        val db = Firebase.firestore
        db.collection("users")
            .whereEqualTo("uid", userId).get().addOnCompleteListener { queryTask ->
                val userAttribute = queryTask.result?.documents?.get(0)
                binding.userName.text = userAttribute?.get("name").toString()
                binding.phoneNumber.text = userAttribute?.get("phoneNumber").toString()
                showMapLocation(userAttribute?.get("latitude").toString().toDouble(),
                    userAttribute?.get("longitude").toString().toDouble())
            }
        db.collection("food").document(databaseId).get().addOnCompleteListener {  querySnapshot ->
            val food = querySnapshot.result
            val amount = food?.get("amount").toString()
            val description = food?.get("description").toString()
            val expiryDate = food?.get("dateExpire").toString()
            val notes = food?.get("additionalNotes").toString()
            binding.food.text = amount + "x " + description
            binding.expiryDate.text = "Expiry Date: " + expiryDate
            binding.notes.text = notes
        }
    }


    private fun setImages(documentId: String){
        val storage = Firebase.storage
        val storageRef = storage.reference
        try {
            val attachmentRef = storageRef.child("attachments/$documentId")
            attachmentRef.listAll().addOnSuccessListener { listResult ->
                listResult.items.forEach { ref ->
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        imagesList.add(uri)
                        foodAdapter.notifyDataSetChanged()
                    }
                }
            }
        } catch (exception: Exception){
            binding.imageRecyclerView.isGone = true
        }
    }

    private fun showMapLocation(latitude: Double, longitude: Double){
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        val startMarker = Marker(binding.maps)
        val location = GeoPoint(latitude, longitude)
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
        binding.maps.controller.animateTo(location)
        binding.maps.controller.setZoom(18.0)
    }
}