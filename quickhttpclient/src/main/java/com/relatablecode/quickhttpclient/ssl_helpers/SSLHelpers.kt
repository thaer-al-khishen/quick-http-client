package com.relatablecode.quickhttpclient.ssl_helpers

import com.relatablecode.quickhttpclient.logging_utils.Logger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.Date
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Creates an SSL socket factory using the given trust managers.
 * @param trustManagers Array of X509TrustManager to use for creating the SSL socket factory.
 * @return SSLSocketFactory? The created SSL socket factory or null if an error occurs.
 */
fun createSSLSocketFactory(trustManagers: Array<X509TrustManager>): SSLSocketFactory? {
    return runCatching {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, null)
        sslContext.socketFactory
    }.onFailure {
        Logger.e("SSL_ERROR", "Failed to create SSL Socket Factory: ${it.message}")
    }.getOrNull()
}

/**
 * Configures the [HttpsURLConnection] with SSL pinning using either public keys or certificate fingerprints.
 * @param publicKeys List of public keys for pinning.
 * @param certificates List of certificate fingerprints for pinning.
 */
fun HttpsURLConnection.setupSSL(publicKeys: List<String>, certificates: List<String>) {
    if (publicKeys.isNotEmpty()) {
        createSSLSocketFactory(createPublicKeyPinningTrustManager(publicKeys.toTypedArray()))?.let {
            this.sslSocketFactory = it
        }
    } else if (certificates.isNotEmpty()) {
        createSSLSocketFactory(createCertificatePinningTrustManager(certificates.toTypedArray()))?.let {
            this.sslSocketFactory = it
        }
    }
}

/**
 * Creates a trust manager for public key pinning.
 * @param publicKeys Array of public keys to pin against.
 * @return Array<X509TrustManager> Array containing the created trust manager.
 */
fun createPublicKeyPinningTrustManager(publicKeys: Array<String>): Array<X509TrustManager> {
    return arrayOf(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // No client verification for this example
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            runCatching {
                requireNotNull(chain) { "Certificate chain is empty" }

                var isPublicKeyPinned = false
                chain@ for (certificate in chain) {
                    check(!certificate.notAfter.before(Date())) { "Certificate has expired" }

                    val serverPublicKey = certificate.publicKey
                    val serverPublicKeyEncoded = serverPublicKey.encoded

                    // Convert the encoded public key to PEM format
                    val pemFormatPublicKey = StringBuilder()
                    pemFormatPublicKey.append(android.util.Base64.encodeToString(serverPublicKeyEncoded, android.util.Base64.DEFAULT))
                    val serverPEMPublicKey = pemFormatPublicKey.toString().replace("\n", "")

                    for (pinnedKey in publicKeys) {
                        if (pinnedKey == serverPEMPublicKey) {
                            isPublicKeyPinned = true
                            break@chain
                        }
                    }
                }

                check(isPublicKeyPinned) { "Server public key does not match any pinned public key" }
            }.onFailure {
                Logger.e("SSL_ERROR", it.message.orEmpty())
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })
}

/**
 * Creates a trust manager for certificate fingerprint pinning.
 * @param fingerprints Array of certificate fingerprints to pin against.
 * @return Array<X509TrustManager> Array containing the created trust manager.
 */
fun createCertificatePinningTrustManager(fingerprints: Array<String>): Array<X509TrustManager> {
    return arrayOf(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // No client verification for this example
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            runCatching {
                requireNotNull(chain) { "Certificate chain is empty" }

                var isFingerprintPinned = false
                for (certificate in chain) {
                    check(!certificate.notAfter.before(Date())) { "Certificate has expired" }
                    check(!certificate.notBefore.after(Date())) { "Certificate is not yet valid" }

                    val certificateFingerprint = MessageDigest.getInstance("SHA-256")
                        .digest(certificate.encoded)
                        .joinToString(":") { "%02X".format(it) }

                    if (fingerprints.contains(certificateFingerprint)) {
                        isFingerprintPinned = true
                        break
                    }
                }

                check(isFingerprintPinned) { "Server certificate fingerprint does not match any pinned fingerprint" }
            }.onFailure {
                Logger.e("SSL_ERROR", it.message.orEmpty())
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

}
