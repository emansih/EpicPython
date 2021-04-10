package xyz.hisname.epicpython.ui.addFood

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentAddFoodBinding
import xyz.hisname.epicpython.model.AttachmentData
import xyz.hisname.epicpython.ui.AttachmentRecyclerAdapter
import xyz.hisname.epicpython.ui.ProgressBar
import xyz.hisname.epicpython.util.DateTimeUtil
import xyz.hisname.epicpython.util.getImprovedViewModel
import java.io.File
import java.util.*

class AddFoodFragment: Fragment() {


    private var fragmentAddFoodBinding: FragmentAddFoodBinding? = null
    private val binding get() = fragmentAddFoodBinding!!
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private val attachmentItemAdapter by lazy { arrayListOf<AttachmentData>() }
    private val attachmentDataAdapter by lazy { arrayListOf<Uri>() }

    private val addFoodModel by lazy { getImprovedViewModel(AddFoodViewModel::class.java) }
    private lateinit var fileUri: Uri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddFoodBinding = FragmentAddFoodBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setIcon()
        setWidget()
        addFoodModel.isDataSaved.observe(viewLifecycleOwner){ saved ->
            if(saved){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(fileUri)
                attachmentItemAdapter.add(AttachmentData(fileUri))
                binding.attachmentInformation.adapter?.notifyDataSetChanged()
            }
        }
    }


    private fun setIcon(){
        binding.dateExpiredEditText.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        }, null, null, null)
        binding.addFoodFab.setImageDrawable(IconicsDrawable(requireContext())
            .icon(GoogleMaterial.Icon.gmd_add))
        binding.amountEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_chart_pie
            colorRes = R.color.md_green_500
            sizeDp = 24
        }, null, null, null)
    }

    private fun setWidget(){
        binding.dateExpiredEditText.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                binding.dateExpiredEditText.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        binding.addAttachmentButton.setOnClickListener {
            attachmentDialog()
        }
        binding.addFoodFab.setOnClickListener {
            ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f)
            addFoodModel.storeData(binding.descriptionEdittext.text.toString(),
                binding.amountEdittext.text.toString(), binding.dateExpiredEditText.text.toString(),
                binding.dietarySpinner.selectedItem.toString(),
                binding.noteEdittext.text.toString(), attachmentDataAdapter)
        }
        binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentItemAdapter)
    }

    private fun attachmentDialog(){
        val createTempDir = File(requireContext().getExternalFilesDir(null).toString() +
                File.separator + "temp")
        if (!createTempDir.exists()) {
            createTempDir.mkdir()
        }
        val randomId = UUID.randomUUID().toString().substring(0, 7)
        val fileToOpen = File(requireContext().getExternalFilesDir(null).toString() +
                File.separator + "temp" + File.separator + "${randomId}.png")
        if (fileToOpen.exists()) {
            fileToOpen.delete()
        }
        fileUri = FileProvider.getUriForFile(requireContext(),
            requireContext().packageName + ".provider", fileToOpen)
        takePicture.launch(fileUri)
    }
}