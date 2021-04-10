package xyz.hisname.epicpython.ui.onboarding

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import xyz.hisname.epicpython.R
import xyz.hisname.epicpython.databinding.FragmentOnboardingBinding
import xyz.hisname.epicpython.ui.MainActivity
import xyz.hisname.epicpython.ui.addFood.MapFragment
import xyz.hisname.epicpython.ui.addFood.MapViewModel
import xyz.hisname.epicpython.util.getImprovedViewModel
import xyz.hisname.epicpython.util.getViewModel
import xyz.hisname.epicpython.util.zipLiveData
import java.util.*

class OnboardingFragment: Fragment() {


    private var fragmentOnboardingBinding: FragmentOnboardingBinding? = null
    private val binding get() = fragmentOnboardingBinding!!
    private val onboardingViewModel by lazy { getImprovedViewModel(OnboardingViewModel::class.java) }
    private val mapsViewModel by lazy { getViewModel(MapViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentOnboardingBinding = FragmentOnboardingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = "Welcome"
        val user = FirebaseAuth.getInstance().currentUser
        if(user.email.isNullOrBlank()){
            binding.emailLayout.isVisible = true
        }
        if(user.phoneNumber.isNullOrBlank()){
            binding.phoneLayout.isVisible = true
        }
        binding.addressEditText.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, MapFragment())
                addToBackStack(null)
            }
        }

        zipLiveData(mapsViewModel.latitude, mapsViewModel.longitude).observe(viewLifecycleOwner){ data ->
            val geoCoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geoCoder.getFromLocation(data.first, data.second, 1)
            val address = addresses[0].getAddressLine(0)
            if(!address.isNullOrBlank()){
                binding.addressEditText.setText(address)
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 2){
                    binding.addressEditText.isGone = true
                    binding.phoneLayout.isGone = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.submitButton.setOnClickListener {
            onboardingViewModel.saveUser(binding.nameEditText.text.toString(),
                mapsViewModel.latitude.value ?: 0.0, mapsViewModel.longitude.value ?: 0.0,
                binding.spinner.selectedItem.toString(), binding.emailEditText.text.toString(),
                binding.phoneEditText.text.toString())
        }

        onboardingViewModel.isDataSaved.observe(viewLifecycleOwner){ isSaved ->
            if(isSaved){
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
        }
    }



}