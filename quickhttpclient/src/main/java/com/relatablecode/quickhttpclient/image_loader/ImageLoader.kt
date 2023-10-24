package com.relatablecode.quickhttpclient.image_loader

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.relatablecode.quickhttpclient.network_interactors.RawHttpClient
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import com.relatablecode.quickhttpclient.request_helpers.RequestMethod
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * An image loader utility for loading images from a given URL into an ImageView.
 *
 * Supports setting custom placeholder and error images.
 */
object ImageLoader {

    private var errorResource: Int? = null
    private var placeholderResource: Int? = null

    /**
     * Sets a custom error image that will be displayed in case the image loading fails.
     *
     * @param _errorDrawable Resource ID of the error image drawable.
     * @return This ImageLoader instance for chaining.
     */
    fun withErrorImage(_errorDrawable: Int): ImageLoader {
        errorResource = _errorDrawable
        return this
    }

    /**
     * Sets a custom placeholder image that will be displayed until the image from URL is loaded.
     *
     * @param _placeHolder Resource ID of the placeholder image drawable.
     * @return This ImageLoader instance for chaining.
     */
    fun withPlaceHolder(_placeHolder: Int): ImageLoader {
        placeholderResource = _placeHolder
        return this
    }

    /**
     * Loads an image from a given URL into the provided ImageView.
     * If a placeholder has been set, it will be displayed until the image is loaded.
     * If the image loading fails, an error image will be displayed if it has been set.
     *
     * @param url URL of the image to load.
     * @param imageView ImageView into which the image will be loaded.
     */
    fun load(url: String, imageView: ImageView) {

        placeholderResource?.let {
            imageView.setImageResource(it)
        }

        launchIoScope {
            RawHttpClient.makeNetworkRequest(
                NetworkRequest(
                    endpoint = url,
                    requestMethod = RequestMethod.GET
                )
            ) { inputStream ->
                runCatching {
                    BitmapFactory.decodeStream(inputStream)
                }.fold(
                    onSuccess = { bitmap ->
                        launchIoScope {
                            withContext(Dispatchers.Main) {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    },
                    onFailure = {
                        launchIoScope {
                            withContext(Dispatchers.Main) {
                                errorResource?.let {
                                    imageView.setImageResource(it)
                                }
                            }
                        }
                    }
                )
            }.onFailure {
                errorResource?.let {
                    imageView.setImageResource(it)
                }
            }
        }
    }
}

internal object CoroutineScopes {

    private val ioJob  = SupervisorJob()
    val ioScope = CoroutineScope(Dispatchers.IO + ioJob )

    fun getCoroutineScope(): CoroutineScope {
        return ioScope
    }

    fun cancelAllCoroutines() {
        ioJob.cancel()
    }

}

internal fun launchIoScope(
    context: CoroutineContext = EmptyCoroutineContext,
    onError: ((Throwable) -> Unit)? = null,
    action: suspend CoroutineScope.() -> Unit
) {
    CoroutineScopes.ioScope.launch(context + CoroutineExceptionHandler { _, exception ->
        onError?.invoke(exception)
    }) {
        action()
    }
}
