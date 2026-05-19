package com.example.tapwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import android.media.SoundPool
import android.media.AudioAttributes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapWarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TapWarGame()
                }
            }
        }
    }
}

@Composable
private fun TapWarTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF111827),
        secondary = Color(0xFF3B82F6),
        tertiary = Color(0xFFEF4444),
        background = Color(0xFF08111F),
        surface = Color(0xFF0F172A),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
private fun TapWarGame() {
    var score by rememberSaveable { mutableIntStateOf(50) }
    var redWins by rememberSaveable { mutableIntStateOf(0) }
    var blueWins by rememberSaveable { mutableIntStateOf(0) }

    val isGameOver by remember { derivedStateOf { score <= 0 || score >= 100 } }
    val redWeight by remember(score) { derivedStateOf { score.toFloat().coerceAtLeast(0.001f) } }
    val blueWeight by remember(score) { derivedStateOf { (100 - score).toFloat().coerceAtLeast(0.001f) } }

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            if (score >= 100) redWins++
            else if (score <= 0) blueWins++
        }
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isGameOver) 1f else 0.72f,
        label = "overlayAlpha"
    )

    val winnerText = when {
        score >= 100 -> stringResource(R.string.red_wins)
        score <= 0 -> stringResource(R.string.blue_wins)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08111F))
    ) {
    val context = LocalContext.current
    // SoundPool for short low-latency tap sound
    val maxStreams = 4
    val soundPool = remember {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder().setMaxStreams(maxStreams).setAudioAttributes(attrs).build()
    }
    var clickSoundId by remember { mutableIntStateOf(0) }
        Column(modifier = Modifier.fillMaxSize()) {
    LaunchedEffect(Unit) {
        // load resource if available at R.raw.tap_click
        try {
            val resId = context.resources.getIdentifier("tap_click", "raw", context.packageName)
            if (resId != 0) clickSoundId = soundPool.load(context, resId, 1)
        } catch (_: Throwable) {
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try { soundPool.release() } catch (_: Throwable) {}
        }
    }
            GameZone(
                label = stringResource(R.string.blue_player),
                zoneWeight = blueWeight,
                enabled = !isGameOver,
                background = Brush.verticalGradient(
                    listOf(Color(0xFF58A6FF), Color(0xFF1D4ED8))
                ),
                textRotation = 180f,
                onTap = {
                    if (clickSoundId != 0) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
                    if (score > 0) {
                        val isCrit = (0..100).random() < 15
                        val power = if (isCrit) 15 else 5
                        score = (score - power).coerceAtLeast(0)
                    }
                }
            )

            GameZone(
                label = stringResource(R.string.red_player),
                zoneWeight = redWeight,
                enabled = !isGameOver,
                background = Brush.verticalGradient(
                    listOf(Color(0xFFF97316), Color(0xFFEF4444))
                ),
                textRotation = 0f,
                onTap = {
                    if (clickSoundId != 0) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
                    if (score < 100) {
                        val isCrit = (0..100).random() < 15
                        val power = if (isCrit) 15 else 5
                        score = (score + power).coerceAtMost(100)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = !isGameOver,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ScoreChip(score = score, alpha = overlayAlpha, redWins = redWins, blueWins = blueWins)
            }

            AnimatedVisibility(
                visible = isGameOver,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GameOverCard(
                    winnerText = winnerText.orEmpty(),
                    redWins = redWins,
                    blueWins = blueWins,
                    reset = { score = 50 }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.GameZone(
    label: String,
    zoneWeight: Float,
    enabled: Boolean,
    background: Brush,
    textRotation: Float,
    onTap: (offset: Offset?) -> Unit
) {
    // Localized ripple state so rapid taps don't trigger full recomposition
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var rippleOffset by remember { mutableIntStateOf(0) /* encoded offset x/y - not ideal for persistence */ }
    var rippleX by remember { mutableIntStateOf(0) }
    var rippleY by remember { mutableIntStateOf(0) }
    var rippleActive by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(zoneWeight)
            .background(background)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        // Immediate reaction on finger-down
                        onTap(offset)
                        // Haptic feedback for tactile satisfaction
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (_: Throwable) {}

                        // trigger a short ripple animation at the touch offset
                        rippleX = offset.x.toInt()
                        rippleY = offset.y.toInt()
                        rippleActive = 1
                        scope.launch {
                            // keep ripple active briefly
                            delay(120)
                            rippleActive = 0
                        }
                        tryAwaitRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Background ripple draw behind the label using a Canvas-like approach
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            if (rippleActive == 1) {
                val maxRadius = size.minDimension.coerceAtLeast(size.maxDimension) * 0.6f
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = maxRadius,
                    center = Offset(rippleX.toFloat(), rippleY.toFloat())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = maxRadius * 0.6f,
                    center = Offset(rippleX.toFloat(), rippleY.toFloat())
                )
            }
        }

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.18f),
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
            modifier = Modifier
                .rotate(textRotation)
                .alpha(if (enabled) 1f else 0.55f)
        )
    }
}

@Composable
private fun ScoreChip(score: Int, alpha: Float, redWins: Int, blueWins: Int) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF0B1220).copy(alpha = 0.58f * alpha),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "B: $blueWins   |   R: $redWins",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.score_label),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun GameOverCard(
    winnerText: String,
    redWins: Int,
    blueWins: Int,
    reset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.93f),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 36.dp, vertical = 34.dp)
    ) {
        Text(
            text = winnerText,
            color = Color(0xFF111827),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Total Wins  ->  Blue: $blueWins   Red: $redWins",
            color = Color(0xFF374151),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = reset,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.reset),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}