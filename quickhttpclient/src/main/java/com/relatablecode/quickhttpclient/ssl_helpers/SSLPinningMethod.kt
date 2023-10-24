package com.relatablecode.quickhttpclient.ssl_helpers

/**
 * Represents the methods for SSL pinning.
 */
sealed class SSLPinningMethod {
    /**
     * Represents pinning using public keys.
     * @property publicKeys List of public keys to pin against.
     */
    data class PublicKeyPinning(val publicKeys: List<String>): SSLPinningMethod()

    /**
     * Represents pinning using certificate fingerprints.
     * @property certificates List of certificate fingerprints to pin against.
     */
    data class CertificatesPinning(val certificates: List<String>): SSLPinningMethod()
}
