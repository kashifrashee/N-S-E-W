# 🧭 Compass Pro - Jetpack Compose

A premium, high-precision Compass application built with **Modern Android Development** standards. This app features a stunning **Neumorphic UI**, smooth animations, and real-time sensor data processing using **Kotlin** and **Jetpack Compose**.

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-green)

## ✨ Features

- **🎯 Real-time Accuracy:** Uses Accelerometer & Magnetometer sensors for precise Azimuth calculation.
- **🎨 Neumorphic Design:** Custom `Canvas` drawing for a realistic, high-quality 3D Dial and Knob interface.
- **🌗 Theme Support:** Seamless toggle between **Dark Mode** (Charcoal) and **Light Mode** (Off-white).
- **🌊 Smooth Animations:** Implements `animateFloatAsState` to prevent needle jitter and provide a fluid user experience.
- **🏗 MVVM Architecture:** Clean separation of concerns using `ViewModel`, `StateFlow`, and `Coroutines`.
- **📐 Native Text Rendering:** Rotatable text on Canvas for degrees and cardinal directions.

## 📱 Screenshots

| Light Theme | Dark Theme |
|:---:|:---:|
| <img src="screenshots/light.png" alt="Light Mode" width="250"/> | <img src="screenshots/dark.png" alt="Dark Mode" width="250"/> |

*(Note: Add your screenshots in a folder named `screenshots`)*

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Sensors:** `SensorManager` (TYPE_ACCELEROMETER, TYPE_MAGNETIC_FIELD)
- **Concurrency:** Coroutines & Flow
- **Fonts:** Google Fonts (Montserrat)

## 🚀 Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/compass-pro-compose.git
    ```

2.  **Open in Android Studio:**
    *   Open the project and let Gradle sync.

3.  **Add Permissions:**
    Ensure your `AndroidManifest.xml` has:
    ```xml
    <uses-feature android:name="android.hardware.sensor.accelerometer" android:required="true" />
    <uses-feature android:name="android.hardware.sensor.compass" android:required="true" />
    ```

4.  **Run the App:**
    *   🔌 **Connect a Physical Device.** (Emulators usually do not support Magnetometer sensors effectively).
    *   Build and Run.

## 💡 How It Works

1.  **Sensor Fusion:** The app listens to the device's Gravity and Geomagnetic sensors.
2.  **Calculation:** `SensorManager.getRotationMatrix` computes the rotation matrix, which is converted to orientation angles (Azimuth).
3.  **Normalization:** The angle is normalized to 0-360 degrees.
4.  **UI Update:** The `ViewModel` exposes this degree via `StateFlow` to the UI, which animates the dial rotation.

## 🤝 Contributing

Contributions are welcome! Please fork the repository and create a pull request for any improvements, especially in:
- Sensor noise filtering (Kalman Filter).
- Layout optimizations for tablets.

## 📄 License

This project is licensed under the [MIT License](LICENSE).
