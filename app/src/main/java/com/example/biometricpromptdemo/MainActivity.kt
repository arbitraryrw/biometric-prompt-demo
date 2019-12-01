package com.example.biometricpromptdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.nio.charset.Charset

//Crypto APIs
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

//Many of the code snippets are inspired from here:
//Ref: https://developer.android.com/training/sign-in/biometric-auth#kotlin

class MainActivity : AppCompatActivity() {

    object irrelevantVars{
        var counter = 0
        var flag = 0
    }

    val KEY_NAME = "insert_obfuscated_keyname"

    private lateinit var executor:Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricManager: BiometricManager

    val superSecretValue = "SuperSecretValueShhh"
    lateinit var encryptedSuperSecretValue: ByteArray
    lateinit var iv: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authResultTextView = findViewById<TextView>(R.id.authResultTextView)
        val encryptButton = findViewById<Button>(R.id.encryptButton)
        val decryptButton = findViewById<Button>(R.id.decryptButton)

        authResultTextView.setText("Not Authenticated")


        executor = ContextCompat.getMainExecutor(this)

        setupBiometricPromptAndInfo()

        biometricManager = BiometricManager.from(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            generateSecretKey(KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                //.setUserAuthenticationValidityDurationSeconds()
                .build())
        }
        else{
            generateSecretKey(KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .build())
        }

        encryptButton.setOnClickListener {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    performBiometricAuthenticationEncrypt()
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    println("No biometric features available on this device.")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    println("Biometric features are currently unavailable.")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    println("The user hasn't associated any biometrics with their account.")
            }
        }

        decryptButton.setOnClickListener {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    performBiometricAuthenticationDecrypt()
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    println("No biometric features available on this device.")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    println("Biometric features are currently unavailable.")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    println("The user hasn't associated any biometrics with their account.")
            }
        }

    }

    fun setupBiometricPromptAndInfo() {
        // For information on individual prompt options refer to:
        //https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.Builder.html#public-methods
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Nik.re Login")
            .setSubtitle("Biometric Auth")
            .setDescription("Login using any biometric the device supports")
            .setNegativeButtonText("Abort biometric login")
            .setConfirmationRequired(true)
            .setDeviceCredentialAllowed(false)
            .build()

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()

                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                        println("Error: negative button pressed.")
                    }

                    println("AUTH ERROR")
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {


                    if (irrelevantVars.flag == 0){

                        encryptedSuperSecretValue = result.cryptoObject?.cipher?.doFinal(
                            superSecretValue.toByteArray(Charset.defaultCharset())
                        )!!

                        println("[POST AUTH] Encrypted Data: " +
                                encryptedSuperSecretValue.toString(Charset.defaultCharset()))

                        notiftyUIAuthSuccess()
                    }


                    if (irrelevantVars.flag == 1) {

                        val decryptedData = result.cryptoObject?.cipher?.doFinal(
                            encryptedSuperSecretValue
                        )

                        println("[POST AUTH] Decrypted info: " +
                                decryptedData?.toString(Charset.defaultCharset()))

                        notiftyUIAuthSuccess()
                    }

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()

                    println("Biometric auth failed")
                }
            })
    }

    fun performBiometricAuthenticationEncrypt(){
        val cipher = getCipher()
        val secretKey = getSecretKey()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, SecureRandom())

        iv = cipher.iv

        biometricPrompt.authenticate(promptInfo,
            BiometricPrompt.CryptoObject(cipher)
        )

        irrelevantVars.flag = 0
    }

    fun performBiometricAuthenticationDecrypt(){
        val decryptionCipher = getCipher()
        val decryptionSecretKey = getSecretKey()

        decryptionCipher.init(Cipher.DECRYPT_MODE, decryptionSecretKey, IvParameterSpec(iv))

//        Testing purposes, checking if this will trigger an error when accessing key without
//              the user authenticating. Correctly throws the following exception:
//              android.security.KeyStoreException: Key user not authenticated
//        val decryptedData:ByteArray = decryptionCipher.doFinal(encryptedSuperSecretValue)
//        println("[PRE AUTH] Decrypted Data: " + decryptedData.toString(Charset.defaultCharset()))

        biometricPrompt.authenticate(promptInfo,
            BiometricPrompt.CryptoObject(decryptionCipher))

        irrelevantVars.flag = 1
    }

    fun notiftyUIAuthSuccess(){
        val authResultTextView = findViewById<TextView>(R.id.authResultTextView)

        println("[+] Successful authentication, yahoo!")

        irrelevantVars.counter++

        authResultTextView.setText("Successfully Authenticated:" + irrelevantVars.counter)
    }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }


}
