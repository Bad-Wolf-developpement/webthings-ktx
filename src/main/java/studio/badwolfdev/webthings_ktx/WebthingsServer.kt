package studio.badwolfdev.webthings_ktx

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.HttpUrl
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import studio.badwolfdev.webthings_ktx.Const.RETURN_OK
import studio.badwolfdev.webthings_ktx.Const.WRONG_SERVER_ADDRESS
import studio.badwolfdev.webthings_ktx.api.ApiService
import java.net.InetAddress
import java.net.URI
import java.net.URL

private const val TAG = "WebthingsServer"
/**
 * Interface representing a Webthings Server Object
 *
 * @author Arist0v
 * @author Bad Wolf Developpement
 */
interface WebthingsServer {
    //TODO add logic for fallbackServer
    //TODO create an activity to get the token from a web browser(copy/paste at first and other way later)

    /**
     * Application context to access the ressources
     */
    val ctx: Context

    /**
     * domain of the gateway
     */
    val gatewayDomain: String

    /**
     * Token generated from the webthings UI
     */
    val gatewayToken: String

    /**
     * Is the gateway reachable with HTTPS
     * Default: true
     */
    val ssl: Boolean
        get() = true

    /**
     * Fallback domain for the gateway(ie: if no internet access you could fallback to local ip or domain)
     * Default: gateway.local
     */
    val fallbackDomain: String
        get() = "gateway.local"

    /**
     * Port to communicate with the gateway
     * Default: 443
     */
    val gatewayPort: Int
        get() = 443

    /**
     * gateway URI build from provided information
     */
    val gatewayUri: URI
        get() {
           return if (ssl){
                if (gatewayPort == 443){
                    URI("https://$gatewayDomain")
                }else{
                    URI("https://$gatewayDomain:$gatewayPort")
                }
            }else{
                if (gatewayPort == 80){
                    URI("http://$gatewayDomain")
                }else{
                    URI("http://$gatewayDomain:$gatewayPort")
                }
            }
        }

    /**
     * fallback URI build from provided information
     */
    val fallbackUri: URI
        get() {
            return if (ssl){
                if (gatewayPort == 443){
                    URI("https://$fallbackDomain")
                }else{
                    URI("https://$fallbackDomain:$gatewayPort")
                }
            }else{
                if (gatewayPort == 80){
                    URI("http://$fallbackDomain")
                }else{
                    URI("http://$fallbackDomain:$gatewayPort")
                }
            }
        }

    /**
     * which uri to use
     *
     * test if main uri is accessible if not test fallback uri
     *
     * @return URI or null
     */
    val uriToUse: URI?
        get() {
            return if (isGatewayReachable(gatewayUri)){
                gatewayUri
            }else if (isGatewayReachable(fallbackUri)){
                fallbackUri
            }else{
                null
            }
        }

    val things: List<Thing>
        get() = emptyList()

    val webthingsApi: ApiService
        get() = ApiService(
            gatewayUri.toString(),//TODO should i move this to the getThings or similar for fallback check
            gatewayToken
        )

    /**
     * Method to get the list of things from the gateway
     *
     * The function will populate a list of [Thing] object
     */
    fun getThings(){
        //TODO choose the uri to use before calling the action
        //TODO url shouldn't have protocol in front
        webthingsApi.getThingsList { response ->
            if (response == null){
                Log.d("TAG", "API CALL FAILED IN getThings")
            }else{
                when (response.code()){
                    200 -> {
                        Log.d("TAG", "API CALL SUCCESS, 200")
                        Log.d("TAG", "response: ${response.body()}")
                    }
                    else -> {
                        Log.d("TAG", "UNHANDLED ERROR: ${response}")
                    }
                }
            }
        }
    }

    /**
     * Method to test if the url is valid
     *
     * @return [WRONG_SERVER_ADDRESS] if url is invalid or [RETURN_OK]
     */
    private fun validateUrl(url: String): Int {
       //TODO instead of return throws exception
        return if (!Patterns.WEB_URL.matcher(url).matches())
            WRONG_SERVER_ADDRESS
        else if (HttpUrl.parse(url) == null) {
            WRONG_SERVER_ADDRESS
        } else {
            RETURN_OK
        }
    }

    /**
     * method to test if the gateway is reachable
     *
     * @return Boolean true if uri is reachable
     */
    private fun isGatewayReachable(
        uri: URI,
        timeout: Int = 1000): Boolean {
        Log.d(TAG, "Looking if gateway is reachable on : $uri")

        val status = runBlocking(Dispatchers.IO) {

            Log.d(TAG, "testing access to domain: ${uri.host}")
            InetAddress.getByName(uri.host).isReachable(timeout)
        }
        return status
    }
}




