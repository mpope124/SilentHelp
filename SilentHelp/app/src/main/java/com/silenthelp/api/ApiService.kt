package com.silenthelp.api
//added by Michael
import com.silenthelp.models.Incident
import retrofit2.Call
import retrofit2.http.*
interface ApiService {

    @GET("incidents")
    fun getIncidents(): Call<List<Incident>>

    @POST("incidents")
    fun createIncident(@Body incident: Incident): Call<Incident>

    @PUT("incidents/{id}")
    fun updateIncident(@Path("id") id: String, @Body incident: Incident): Call<Incident>

    @DELETE("incidents/{id}")
    fun deleteIncident(@Path("id") id: String): Call<Void>

}