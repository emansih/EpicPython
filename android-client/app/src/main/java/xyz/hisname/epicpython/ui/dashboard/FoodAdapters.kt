package xyz.hisname.epicpython.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.hisname.epicpython.databinding.DashboardItemsBinding
import xyz.hisname.epicpython.model.FoodModel

class FoodAdapters(private val foodData: List<FoodModel>): RecyclerView.Adapter<FoodAdapters.FoodHolder>() {

    private var dashboardItems: DashboardItemsBinding? = null
    private val binding get() = dashboardItems!!
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        context = parent.context
        dashboardItems = DashboardItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodHolder(binding)
    }


    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.bind(foodData[position])
    }

    override fun getItemCount() = foodData.size

    inner class FoodHolder(itemView: DashboardItemsBinding): RecyclerView.ViewHolder(itemView.root) {
        fun bind(foodModel: FoodModel) {
            println(foodModel)
            if(foodModel.imageUrl.isEmpty()){
                binding.userImage.isGone = true
            } else {
                Glide.with(context)
                    .load(foodModel.imageUrl)
                    .into(binding.userImage)
            }
            binding.descriptionText.text = foodModel.description
            binding.timestamp.text = foodModel.timestamp
            binding.donorName.text = foodModel.name
        }
    }
}