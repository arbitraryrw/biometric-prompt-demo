package com.example.biometricpromptdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

//Ref: https://developer.android.com/training/sign-in/biometric-auth#kotlin

class MainActivity : AppCompatActivity() {

    var counter = 0

    private val executor = MainThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authResultTextView = findViewById<TextView>(R.id.authResultTextView)
        val mainButton = findViewById<Button>(R.id.mainButton)

        authResultTextView.setText("Not Authenticated")

        mainButton.setOnClickListener {

            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    showBiometricPrompt()
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    println("No biometric features available on this device.")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    println("Biometric features are currently unavailable.")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    println("The user hasn't associated any biometrics with their account.")
            }
        }

    }

    fun showBiometricPrompt() {

        // For information on individual prompt options refer to:
        //https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.Builder.html#public-methods
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Example")
            .setDescription("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Abort biometric login")
            .setConfirmationRequired(true)
            .setDeviceCredentialAllowed(true)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
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
                    super.onAuthenticationSucceeded(result)
                    val authenticatedCryptoObject: BiometricPrompt.CryptoObject? =
                        result.getCryptoObject()
                    // User has verified the signature, cipher, or message
                    // authentication code (MAC) associated with the crypto object,
                    // so you can use it in your app's crypto-driven workflows.

                    notiftyAuthSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()

                    println("Biometric auth failed")
                }
            })

        // Displays the "log in" prompt.
        biometricPrompt.authenticate(promptInfo)
    }


    fun notiftyAuthSuccess(){
        val authResultTextView = findViewById<TextView>(R.id.authResultTextView)

        println("[+] Successful authentication, yahoo!")

        authResultTextView.setText(""+"Successfully Authenticated")
    }

    inner class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(runnable: Runnable) {
            handler.post(runnable)
        }
    }

}
