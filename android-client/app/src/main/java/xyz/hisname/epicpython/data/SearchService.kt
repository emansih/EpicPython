package xyz.hisname.epicpython.data

import retrofit2.http.POST
import retrofit2.http.Query
import xyz.hisname.epicpython.model.LocationSearchModel

interface SearchService {

    @POST("/search")
    suspend fun searchLocation(@Query("q")location: String,
                               @Query("format")outputFormat: String = "jsonv2"): List<LocationSearchModel>
}