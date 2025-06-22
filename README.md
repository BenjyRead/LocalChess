# LocalChess
Simple, intuitive Android chess app for playing chess locally/over Bluetooth. Also includes stockfish integration for playing against the computer.

# Installation

Currently in the process of publishing to the Google Play Store, and F-Droid.

# Building from source

I would recommend installing ADB, Kotlin and Cmake on your system before building the project (Java 11 might also be necessary).

`/gradlew installDebug` to build and install the app on a connected device (make sure device is visible on `adb devices`).

A `local.properties` file may be needed in the root directory of the project, indicating the path to the Android SDK, e.g.:
```
sdk.dir=/path/to/your/android/sdk
```

