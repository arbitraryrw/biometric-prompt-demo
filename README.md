# Biometric Prompt Demo
Nikola Cucakovic, November 2019

## Overview
The purpose of this project is to showcase some of the basic features availble in the [BiometricManager](https://developer.android.com/reference/androidx/biometric/BiometricManager) and [BiometricPrompt](https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt) classes introduced in Android 10. These biometric handlers aim to:

1. Simplify the complexity of integrating biometric authentication  
2. Superseed exsisting methods of integrating biometric authentication, such as the [FingerprintManager](https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager) for fingerprint biometrics
3. Create a single API for developers to interface with any biometric type, whether it is facial recognition or fingerprint. Notably, this will probably be expanded to include other biometric types in the future such as iris etc.
4. Abstract biometric idiosyncrasies from developers

This code is purely for demonstration purposes and is not complete.

## Useful Links / Further Reading
- [Android - Biometric Security Overview](https://source.android.com/security/biometric)
- [Android - Measuring Biometric Security](https://source.android.com/security/biometric/measure#metrics)
- [Biometric Souce](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/hardware/biometrics)
- [BiometricManager](https://developer.android.com/reference/androidx/biometric/BiometricManager)
- [BiometricPrompt](https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt)
- [Android Biometric Guide](https://developer.android.com/training/sign-in/biometric-auth)
