package com.kashif.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class CompassData(
    val degree: Float,
    val accuracy: Int
)

class CompassManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    fun getOrientationFlow(): Flow<CompassData> = callbackFlow {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        // Low-pass filter for smooth data
                        val alpha = 0.8f
                        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                        hasGeomagnetic = true
                    }
                }

                if (hasGravity && hasGeomagnetic) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)

                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)

                        // Azimuth ko degrees mein convert karo
                        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                        // Normalize to 0-360
                        azimuth = (azimuth + 360) % 360

                        // Data send karo
                        trySend(CompassData(azimuth, currentAccuracy))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                currentAccuracy = accuracy
            }
        }

        // Sensors register karo
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            listener,
            magnetometer,
            SensorManager.SENSOR_DELAY_UI
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}