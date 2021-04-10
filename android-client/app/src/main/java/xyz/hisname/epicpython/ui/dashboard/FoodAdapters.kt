package xyz.hisname.epicpython.ui.dashboard

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.hisname.epicpython.databinding.DashboardItemsBinding
import xyz.hisname.epicpython.model.FoodModel
import java.text.DecimalFormat
import java.time.*
import java.time.temporal.ChronoUnit

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
            if(foodModel.imageUrl.isEmpty()){
                binding.userImage.isGone = true
            } else {
                Glide.with(context)
                    .load(foodModel.imageUrl)
                    .centerCrop()
                    .into(binding.userImage)
            }

            val databaseTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(foodModel.timestamp.toLong()), ZoneId.systemDefault())

            val currentTime = LocalDateTime.now()
            val minutes = databaseTime.until(currentTime, ChronoUnit.MINUTES)
            val hours = databaseTime.until(currentTime, ChronoUnit.HOURS)
            val days = databaseTime.until(currentTime, ChronoUnit.DAYS)
            var dateTimeToShow = ""
            if(minutes <= 5){
                // Less than 5 mins
                dateTimeToShow = "Just now"
            } else if(minutes < 60){
                // Less than 60 mins (1 hour)
                dateTimeToShow = minutes.toString() + "m"
            } else {
                if(hours < 24){
                    // Less than 24 hours (1 day)
                    dateTimeToShow = hours.toString() + "h"
                } else {
                    if(days < 2){
                        // Less than 2 days
                        dateTimeToShow = days.toString() + " days ago"
                    } else {
                        dateTimeToShow = databaseTime.dayOfMonth.toString() + " "  +
                                databaseTime.month.toString() + " " + databaseTime.year.toString()
                    }
                }
            }
            val userLocation = Location("pointA")
            userLocation.latitude = foodModel.userLatitude
            userLocation.longitude = foodModel.userLongitude

            val donorLocation = Location("pointB")
            donorLocation.latitude = foodModel.latitude.toDouble()
            donorLocation.longitude = foodModel.longitude.toDouble()
            val distance = userLocation.distanceTo(donorLocation)
            val df = DecimalFormat()
            df.maximumFractionDigits = 0
            val distanceBetween = if (distance >= 1000){
                val distanceInKm = df.format(distance.div(1000))
                "$distanceInKm km"
            } else {
                val distanceInM = df.format(distance)
                "$distanceInM m"
            }


            binding.distanceBetweenLocation.text = distanceBetween
            binding.descriptionText.text = foodModel.description
            binding.timestamp.text = " • " + dateTimeToShow
            binding.donorName.text = foodModel.name
        }
    }
}