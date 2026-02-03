package com.example.aws_cognito_test.data.utils

import java.security.KeyStore
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import android.util.Base64

fun savePrivateKeyToKeystore(alias: String, privateKeyPem: String) {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    val cleanKey = privateKeyPem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("\n", "")

    val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyBytes))

    keyStore.setKeyEntry(alias, privateKey, null, null)
}
