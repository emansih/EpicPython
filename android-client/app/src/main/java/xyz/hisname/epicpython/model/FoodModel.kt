package xyz.hisname.epicpython.model

data class FoodModel(
    val uid: String,
    val timestamp: String,
    val description: String,
    val name: String,
    val imageUrl: String,
    val latitude: String,
    val longitude: String,
    val userLatitude: Double,
    val userLongitude: Double
)