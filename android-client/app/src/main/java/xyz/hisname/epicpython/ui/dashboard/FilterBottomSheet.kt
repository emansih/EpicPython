package xyz.hisname.epicpython.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.hisname.epicpython.databinding.FilterBottomSheetBinding
import xyz.hisname.epicpython.util.getViewModel

class FilterBottomSheet: BottomSheetDialogFragment() {

    private var filterBottomSheetBinding: FilterBottomSheetBinding? = null
    private val binding get() = filterBottomSheetBinding!!
    private val dashboardViewModel by lazy { getViewModel(DashboardViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        filterBottomSheetBinding = FilterBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitButton.setOnClickListener {
            when (binding.distanceSpinner.selectedItemPosition) {
                0 -> {
                    dashboardViewModel.distance.postValue(0.5.toInt())
                }
                1 -> {
                    dashboardViewModel.distance.postValue(1)
                }
                2 -> {
                    dashboardViewModel.distance.postValue(2)
                }
                3 -> {
                    dashboardViewModel.distance.postValue(5)
                }
                4 -> {
                    dashboardViewModel.distance.postValue(10)
                }
                5 -> {
                    dashboardViewModel.distance.postValue(25)
                }
                6 -> {
                    dashboardViewModel.distance.postValue(30)
                }
            }

            dashboardViewModel.dietary.postValue(binding.dietarySpinner.selectedItem.toString())
            dashboardViewModel.isSubmitted.postValue(true)
        }
    }
}