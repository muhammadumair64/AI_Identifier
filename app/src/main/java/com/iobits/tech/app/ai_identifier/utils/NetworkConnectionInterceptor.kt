package com.iobits.tech.app.ai_identifier.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.iobits.tech.app.ai_identifier.MyApplication
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {

    private val applicationContext = MyApplication.mInstance

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable())

            throw NoInternetException("Make sure you have an active data connection")
        println("position 1")
        return chain.proceed(chain.request())
    }

    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }
        return result
    }

}

class NoInternetException(message: String) : IOException(message)