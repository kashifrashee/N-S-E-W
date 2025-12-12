package com.kashif.myapplication

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CompassViewModel(private val compassManager: CompassManager) : ViewModel() {

    // Ye variable add karo - previous degree track karne ke liye
    private var previousDegree = 0f

    private val _currentDegree = MutableStateFlow(0f)
    val currentDegree: StateFlow<Float> = _currentDegree.asStateFlow()

    private val _directionText = MutableStateFlow("North")
    val directionText: StateFlow<String> = _directionText.asStateFlow()

    private val _sensorAccuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy.asStateFlow()

    init {
        viewModelScope.launch {
            compassManager.getOrientationFlow().collect { data ->
                _currentDegree.value = data.degree
                _directionText.value = getDirectionLabel(data.degree)
                _sensorAccuracy.value = data.accuracy
            }
        }
    }

    // ⭐ YE NAYA FUNCTION ADD KARO ⭐
    private fun getSmoothedDegree(newDegree: Float): Float {
        // Calculate shortest angular distance
        var delta = newDegree - previousDegree

        // Agar difference 180 se zyada hai, toh opposite direction se jao
        if (delta > 180f) {
            delta -= 360f
        } else if (delta < -180f) {
            delta += 360f
        }

        // Update previous degree (cumulative tracking)
        previousDegree += delta

        return previousDegree
    }

    private fun getDirectionLabel(degree: Float): String {
        val normalized = (degree % 360 + 360) % 360  // Ensure 0-360 range
        return when (normalized.roundToInt()) {
            in 338..360, in 0..22 -> "North"
            in 23..67 -> "North-East"
            in 68..112 -> "East"
            in 113..157 -> "South-East"
            in 158..202 -> "South"
            in 203..247 -> "South-West"
            in 248..292 -> "West"
            in 293..337 -> "North-West"
            else -> "North"
        }
    }
}