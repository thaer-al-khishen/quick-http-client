package com.relatablecode.quickhttpclientapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.relatablecode.quickhttpclient.network_manager.NetworkManager
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import com.relatablecode.quickhttpclient.request_helpers.RequestMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.IO).launch {
            repeat(1) { iteration ->
                val startTime = System.currentTimeMillis()
                NetworkManager.makeNetworkCallWithFullUrl<List<JsonPlaceHolderPost>>(
                    NetworkRequest(
                        endpoint = "https://jsonplaceholder.typicode.com/posts",
                        requestMethod = RequestMethod.GET,
                        params = listOf(),
                        headers = mutableListOf(),
                        retryCount = 3
                    )
                ).fold(
                    onSuccess = {
                        val timeTaken = System.currentTimeMillis() - startTime
                        Log.d("ThaerResult", "AppSuccess! | Iteration: $iteration | Time Taken: $timeTaken ms")
                    },
                    onFailure = {
                        val timeTaken = System.currentTimeMillis() - startTime
                        Log.d("ThaerResult", "AppError! | Iteration: $iteration | Time Taken: $timeTaken ms")
                    }
                )
            }
        }

    }

}
