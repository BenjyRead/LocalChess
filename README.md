# LocalChess
Simple, intuitive Android chess app for playing chess locally/over Bluetooth. Also includes stockfish integration for playing against the computer.

<img src="https://github.com/user-attachments/assets/980512a8-3271-4854-89e7-8e8d8f61af15" width="150" />

<img src="https://github.com/user-attachments/assets/b8ad1f1c-95f8-4bab-8956-6fc44fa6803c" width="150" />




# Installation

Currently in the process of publishing to the Google Play Store, and F-Droid.

# Building from source

Dependencies:
```
ADB
Kotlin
Cmake
Java 11
```

`/gradlew installDebug` to build and install the app on a connected device (make sure device is visible on `adb devices`).

A `local.properties` file may be needed in the root directory of the project, indicating the path to the Android SDK, e.g.:
```
sdk.dir=/path/to/your/android/sdk
```

