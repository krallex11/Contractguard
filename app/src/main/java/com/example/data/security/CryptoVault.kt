package com.example.data.security

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object CryptoVault {
    private const val ALGORITHM = "AES"
    // Internal secret key used for end-to-end payload obfuscation & encryption
    private val SECRET_KEY_BYTES = byteArrayOf(
        0x43, 0x6f, 0x6e, 0x74, 0x72, 0x61, 0x63, 0x74,
        0x47, 0x75, 0x61, 0x72, 0x64, 0x32, 0x30, 0x32,
        0x36, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65, 0x4b,
        0x65, 0x79, 0x41, 0x45, 0x53, 0x32, 0x35, 0x36
    ) // 32-byte AES-256 key

    fun encrypt(plainText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY_BYTES, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText // Fallback
        }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY_BYTES, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText // Fallback
        }
    }

    fun generateSha256Hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun generateESignatureHash(
        contractTitle: String,
        partyA: String,
        partyB: String,
        signatureBase64: String,
        timestamp: Long
    ): String {
        val combinedData = "$contractTitle|$partyA|$partyB|$signatureBase64|$timestamp|ContractGuard2026"
        return generateSha256Hash(combinedData).uppercase()
    }
}
