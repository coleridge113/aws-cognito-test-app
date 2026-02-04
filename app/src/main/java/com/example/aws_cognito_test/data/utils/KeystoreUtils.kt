package com.example.aws_cognito_test.data.utils

import java.security.KeyStore
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.PrivateKey
import android.util.Base64
import android.util.Log

object KeyStoreUtils {
    fun savePrivateKeyToKeystore(alias: String, privateKeyPem: String, certPem: String) {
        Log.d("IotManager", privateKeyPem)
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        // 1. Identify if it is PKCS#1 (RSA PRIVATE KEY) or PKCS#8 (PRIVATE KEY)
        val isPkcs1 = privateKeyPem.contains("BEGIN RSA PRIVATE KEY")

        val cleanKey = privateKeyPem
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)

        val privateKey = if (isPkcs1) {
            val keySpec = PKCS8EncodedKeySpec(keyBytes) 
            KeyFactory.getInstance("RSA").generatePrivate(keySpec)
        } else {
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
        }

        // 2. Clean and Generate the Certificate
        val cf = java.security.cert.CertificateFactory.getInstance("X.509")
        val certStream = certPem.byteInputStream()
        val certificate = cf.generateCertificate(certStream)

        // 3. Create the Chain (Android requires an array of Certificate)
        val chain = arrayOf(certificate)

        // 4. Save to KeyStore WITH the chain
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }

        // Note: The third parameter is the password (null), 
        // and the fourth is the chain (cannot be null)
        keyStore.setKeyEntry(alias, privateKey, null, chain)

        Log.d("KeyStoreHelper", "Key and Cert successfully linked in KeyStore")
    }

    fun getPrivateKeyFromKeystore(alias: String): PrivateKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.getKey(alias, null) as? java.security.PrivateKey
    }
}
