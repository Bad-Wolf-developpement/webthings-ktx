package studio.badwolfdev.webthings_ktx.api

import retrofit2.Call
import retrofit2.http.GET
import studio.badwolfdev.webthings_ktx.thing_object.Thing

/**
 * interface for Retrofit Api
 *
 * @author Arist0v
 * @author Bad Wolf Developpement
 */
interface ApiInterface {

    @GET("/things")
    fun getAllThings(): Call<List<Thing>>
}
