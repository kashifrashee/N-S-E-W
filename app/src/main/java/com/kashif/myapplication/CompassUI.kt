package com.kashif.myapplication

import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.cos
import kotlin.math.sin

// --- 1. FONTS & COLORS SETUP ---

// --- NEW CODE (Local Font) ---
val MontserratFont = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

// Data class to hold theme colors
data class CompassColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val northColor: Color,
    val knobGradient: List<Color>,
    val dialShadow: Color
)

val DarkPalette = CompassColors(
    background = Color(0xFF1F2125),
    surface = Color(0xFF26292E),
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFF6E737C),
    accent = Color(0xFFFF8B3D),
    northColor = Color(0xFFFF3B30),
    knobGradient = listOf(Color(0xFF2E3238), Color(0xFF181A1D)),
    dialShadow = Color(0xFF0F1114)
)

val LightPalette = CompassColors(
    background = Color(0xFFF2F5F8),
    surface = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF2D3035),
    textSecondary = Color(0xFFA0A0A0),
    accent = Color(0xFFFF8B3D),
    northColor = Color(0xFFFF3B30),
    knobGradient = listOf(Color(0xFFFFFFFF), Color(0xFFE8EEF5)),
    dialShadow = Color(0xFFD1D9E6)
)

// --- 2. MAIN COMPOSABLE ---

@Composable
fun CompassApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val viewModel: CompassViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CompassViewModel(CompassManager(context)) as T
            }
        }
    )

    // Collect Data
    val realDegree by viewModel.currentDegree.collectAsState()
    val directionName by viewModel.directionText.collectAsState()
    val accuracy by viewModel.sensorAccuracy.collectAsState()

    // ⭐ YE ADD KARO - Previous degree track karne ke liye ⭐
    var previousTargetDegree by remember { mutableFloatStateOf(0f) }
    var accumulatedDegree by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(realDegree) {
        var delta = realDegree - previousTargetDegree

        // Wraparound fix
        if (delta > 180f) {
            delta -= 360f
        } else if (delta < -180f) {
            delta += 360f
        }

        accumulatedDegree += delta
        previousTargetDegree = realDegree
    }

    // ⭐ Ab accumulatedDegree use karo realDegree ki jagah ⭐
    val animatedDegree by animateFloatAsState(
        targetValue = accumulatedDegree,  // YE CHANGE KARO
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    // Theme Management
    var isDarkTheme by remember { mutableStateOf(true) }
    val colors = if (isDarkTheme) DarkPalette else LightPalette
    val bgColor by animateColorAsState(
        targetValue = colors.background,
        animationSpec = tween(500),
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            ImprovedHeaderRow(
                isDark = isDarkTheme,
                onToggle = { isDarkTheme = !isDarkTheme },
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration Warning (agar sensor accuracy low hai)
            if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
                accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                CalibrationWarning(colors = colors)
            }

            Spacer(modifier = Modifier.weight(1f))

            // MAIN COMPASS UI
            Box(contentAlignment = Alignment.Center) {
                // 1. Outer Glow Ring
                OuterGlowRing(colors = colors, modifier = Modifier.size(360.dp))

                // 2. Rotating Dial with Ticks & Numbers
                ImprovedCompassDial(
                    degree = animatedDegree,
                    colors = colors,
                    modifier = Modifier.size(340.dp)
                )

                // 3. Center Info Knob (Fixed)
                ImprovedCenterKnob(
                    degree = realDegree.toInt(),
                    direction = directionName,
                    colors = colors,
                    modifier = Modifier.size(200.dp)
                )

                // 4. Heading Indicator (Fixed at Top)
                ImprovedHeadingIndicator(
                    colors = colors,
                    modifier = Modifier.size(340.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "${realDegree.toInt()}°",
                    color = colors.accent,
                    fontSize = 16.sp,
                    fontFamily = MontserratFont,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hold device flat for accuracy",
                    color = colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = MontserratFont
                )
            }
        }
    }
}

// ==================== IMPROVED UI COMPONENTS ====================

@Composable
fun ImprovedHeaderRow(isDark: Boolean, onToggle: () -> Unit, colors: CompassColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Handle Back */ }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = colors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "N-S-E-W",
            color = colors.textPrimary,
            fontSize = 22.sp,
            fontFamily = MontserratFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        IconButton(onClick = onToggle) {
            Icon(
                painter = painterResource(
                    id = if (isDark) R.drawable.contrast
                    else R.drawable.moon
                ),
                contentDescription = "Theme Toggle",
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CalibrationWarning(colors: CompassColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.accent.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Blinking dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(colors.accent, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Move phone in figure-8 to calibrate",
                color = colors.accent,
                fontSize = 13.sp,
                fontFamily = MontserratFont,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OuterGlowRing(colors: CompassColors, modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accent.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width / 2
            )
        )
    }
}

@Composable
fun ImprovedCompassDial(degree: Float, colors: CompassColors, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // YE IMPORTANT HAI: Canvas ko ULTA rotate karo taake North fixed rahe
        rotate(-degree, pivot = center) {

            // Outer circle border
            drawCircle(
                color = colors.textSecondary.copy(alpha = 0.1f),
                radius = radius - 2.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw 360 Ticks
            for (i in 0 until 360 step 2) {
                val isCardinal = i % 90 == 0  // N, E, S, W
                val isMajor = i % 30 == 0      // 30, 60, 120, etc
                val isMinor = i % 10 == 0      // 10, 20, 40, etc

                // Angle calculation (0° = North = Top)
                val angleRad = Math.toRadians(i.toDouble() - 90)

                // Tick properties
                val tickLength = when {
                    isCardinal -> 28.dp.toPx()
                    isMajor -> 20.dp.toPx()
                    isMinor -> 14.dp.toPx()
                    else -> 7.dp.toPx()
                }

                val strokeWidth = when {
                    isCardinal -> 3.5.dp.toPx()
                    isMajor -> 2.5.dp.toPx()
                    else -> 1.5.dp.toPx()
                }

                val tickColor = when {
                    i == 0 -> colors.northColor  // North = Red
                    isCardinal -> colors.textPrimary
                    isMajor -> colors.textSecondary
                    else -> colors.textSecondary.copy(alpha = 0.3f)
                }

                // Draw tick line
                val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // Draw text labels
                if (isMajor) {
                    val textRadius = radius - 50.dp.toPx()
                    val textX = center.x + textRadius * cos(angleRad).toFloat()
                    val textY = center.y + textRadius * sin(angleRad).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            textSize = if (isCardinal) 42f else 30f
                            color = if (i == 0) colors.northColor.toArgb()
                            else colors.textSecondary.toArgb()
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create(
                                Typeface.DEFAULT,
                                if (isCardinal) Typeface.BOLD else Typeface.NORMAL
                            )
                        }

                        val label = when(i) {
                            0 -> "N"
                            90 -> "E"
                            180 -> "S"
                            270 -> "W"
                            else -> i.toString()
                        }

                        // Text ko upright rakhne ke liye rotate karo
                        save()
                        translate(textX, textY)
                        rotate(degree + i.toFloat())  // YE LINE IMPORTANT HAI
                        drawText(label, 0f, paint.textSize / 3, paint)
                        restore()
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovedCenterKnob(
    degree: Int,
    direction: String,
    colors: CompassColors,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = colors.dialShadow.copy(alpha = 0.4f),
                ambientColor = colors.dialShadow.copy(alpha = 0.2f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = colors.knobGradient,
                    center = Offset(0.3f, 0.3f)
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.textSecondary.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner detail ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .border(
                    width = 3.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.background.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$degree°",
                color = colors.textPrimary,
                fontSize = 52.sp,
                fontFamily = MontserratFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = direction,
                color = colors.accent,
                fontSize = 22.sp,
                fontFamily = MontserratFont,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ImprovedHeadingIndicator(colors: CompassColors, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Main indicator line
        drawLine(
            color = colors.accent,
            start = Offset(center.x, center.y - radius - 12.dp.toPx()),
            end = Offset(center.x, center.y - radius + 30.dp.toPx()),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Triangle at top
        val path = Path().apply {
            val triangleSize = 12.dp.toPx()
            val topY = center.y - radius - 5.dp.toPx()
            moveTo(center.x, topY - triangleSize)
            lineTo(center.x - triangleSize / 2, topY)
            lineTo(center.x + triangleSize / 2, topY)
            close()
        }
        drawPath(path, color = colors.accent)

        // Glow effect
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accent.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                radius = 20.dp.toPx()
            ),
            radius = 15.dp.toPx(),
            center = Offset(center.x, center.y - radius + 10.dp.toPx())
        )
    }
}