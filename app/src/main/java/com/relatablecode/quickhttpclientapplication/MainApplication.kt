package com.relatablecode.quickhttpclientapplication

import android.app.Application
import android.util.Log
import com.relatablecode.quickhttpclient.interceptors.RequestInterceptor
import com.relatablecode.quickhttpclient.interceptors.RequestModifier
import com.relatablecode.quickhttpclient.quickhttpclient.QuickHttpClient
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        QuickHttpClient
            .initialize()
//            .configureSSLPinning(
//                SSLPinningMethod.PublicKeyPinning(
//                    publicKeys = listOf(
//                        "M8HztCzM3elS5P4hhyBNf6lHkmjAHKhpGPWE=",
//                        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAExkUB47dbJuZjGTqGGF8NuIuZCCY+rbvpT0nm0Sy9rxtfgtGR/2itGkXIEtqQWToSXd5rrWfSzI+WfPXwRJ776w=="
//                    )
//                )
//            )
//            .configureSSLPinning(SSLPinningMethod.CertificatesPinning(
//                certificates = listOf(
//                    "YOUR_DUMMY_CERTIFICATE_FINGERPRINT_2",
//                    "2B:F6:51:9C:22:3B:02:21:39:66:1D:11:4A:61:5D:3C:5E:EA:DC:70:F9:C9:CA:9D:A1:7E:47:CB:2E:49:9B:41"
//                )
//            ))
//            .configureHostNameVerification("jsonplaceholder.typicode.com")
            .addRequestInterceptor(object : RequestInterceptor {
                override fun intercept(networkRequest: NetworkRequest?): RequestModifier {
                    networkRequest?.let {
                        Log.d("ThaerInterceptor", "${networkRequest.toString()}!")
                    }
                    return RequestModifier().apply {}
                }

            })
            .setLoggingCondition(true)
    }

}