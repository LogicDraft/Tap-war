package com.example.tapwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TapWarGame()
                }
            }
        }
    }
}

@Composable
fun TapWarGame() {
    // Single state variable for the score, starting at 50
    var score by remember { mutableIntStateOf(50) }
    var gameStarted by remember { mutableStateOf(false) }
    var redWins by remember { mutableIntStateOf(0) }
    var blueWins by remember { mutableIntStateOf(0) }
    
    val gameOver = score <= 0 || score >= 100
    val matchWinner = when {
        redWins >= 2 -> "Red"
        blueWins >= 2 -> "Blue"
        else -> null
    }
    val winner = when {
        score >= 100 -> "Red"
        score <= 0 -> "Blue"
        else -> null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Blue Zone (Top) - Player 2
            // Weight cannot be exactly 0 in Compose, so we coerce to at least 0.001f
            val blueWeight = (100 - score).toFloat().coerceAtLeast(0.001f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(blueWeight)
                    .background(Color(0xFF3B82F6))
                    .pointerInput(gameOver, matchWinner, gameStarted) {
                        if (!gameOver && matchWinner == null) {
                            // High-frequency zero-latency touch handling
                            awaitPointerEventScope {
                                while (true) {
                                    awaitFirstDown(requireUnconsumed = false)
                                    if (!gameStarted) {
                                        gameStarted = true
                                    } else {
                                        val newScore = (score - 1).coerceAtLeast(0)
                                        score = newScore
                                        if (newScore == 0) blueWins++
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!gameOver && gameStarted) {
                    Text(
                        text = "TAP!",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.2f),
                        modifier = Modifier.rotate(180f), // Rotated for top player
                        letterSpacing = 8.sp
                    )
                }
            }

            // Red Zone (Bottom) - Player 1
            val redWeight = score.toFloat().coerceAtLeast(0.001f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(redWeight)
                    .background(Color(0xFFEF4444))
                    .pointerInput(gameOver, matchWinner, gameStarted) {
                        if (!gameOver && matchWinner == null) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitFirstDown(requireUnconsumed = false)
                                    if (!gameStarted) {
                                        gameStarted = true
                                    } else {
                                        val newScore = (score + 1).coerceAtMost(100)
                                        score = newScore
                                        if (newScore == 100) redWins++
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!gameOver && gameStarted) {
                    Text(
                        text = "TAP!",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black.copy(alpha = 0.2f),
                        letterSpacing = 8.sp
                    )
                }
            }
        }

        // Overlay UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            if (!gameStarted && matchWinner == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.98f), 
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    Text(
                        text = "TAP TO START",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(bottom = 16.dp),
                        letterSpacing = 2.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Red Wins: $redWins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text(text = "  |  ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = "Blue Wins: $blueWins", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                }
            } else if (matchWinner != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.98f), 
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    Text(
                        text = "${matchWinner.uppercase()} WINS MATCH!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (matchWinner == "Red") Color(0xFFEF4444) else Color(0xFF3B82F6),
                        modifier = Modifier.padding(bottom = 32.dp),
                        letterSpacing = 1.sp
                    )
                    Button(
                        onClick = { 
                            redWins = 0
                            blueWins = 0
                            score = 50
                            gameStarted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = "NEW MATCH",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else if (gameOver) {
                // Round Win Screen Banner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.98f), 
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    Text(
                        text = "${winner?.uppercase()} WINS ROUND!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = if (winner == "Red") Color(0xFFEF4444) else Color(0xFF3B82F6),
                        modifier = Modifier.padding(bottom = 32.dp),
                        letterSpacing = 1.sp
                    )
                    Button(
                        onClick = { 
                            score = 50 
                            gameStarted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = "NEXT ROUND",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                // Score Pill
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f), 
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score.toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}
