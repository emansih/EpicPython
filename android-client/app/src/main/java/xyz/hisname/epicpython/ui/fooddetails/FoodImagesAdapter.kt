package xyz.hisname.epicpython.ui.fooddetails

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.hisname.epicpython.databinding.FoodItemsBinding

class FoodImagesAdapter(private val images: List<Uri>): RecyclerView.Adapter<FoodImagesAdapter.FoodImageAdapter>() {


    private var foodItemBinding: FoodItemsBinding? = null
    private val binding get() = foodItemBinding!!
    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodImageAdapter {
        context = parent.context
        foodItemBinding = FoodItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodImageAdapter(binding)
    }

    override fun onBindViewHolder(holder: FoodImageAdapter, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class FoodImageAdapter(itemView: FoodItemsBinding): RecyclerView.ViewHolder(itemView.root){
        fun bind(uri: Uri){
            Glide.with(context)
                .load(uri)
                .override(1000, 1000)
                .into(binding.foodImage)
        }
    }
}