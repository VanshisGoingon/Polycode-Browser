package com.example.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynthesizer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RobotoMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class GameState {
    READY,
    PLAYING,
    GAME_OVER
}

data class PipeObstacle(
    var x: Float,
    val topHeight: Float,
    val gap: Float,
    var scored: Boolean = false
)

@Composable
fun PolyBirdGame(
    audioSynthesizer: AudioSynthesizer,
    modifier: Modifier = Modifier,
    onCloseGame: (() -> Unit)? = null
) {
    var gameState by remember { mutableStateOf(GameState.READY) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }

    var birdY by remember { mutableFloatStateOf(400f) }
    var birdVelocity by remember { mutableFloatStateOf(0f) }
    val birdRadius = 22f

    val pipes = remember { mutableStateListOf<PipeObstacle>() }
    var isMuted by remember { mutableStateOf(false) }

    // Visual background stars
    val stars = remember {
        List(25) {
            Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 1600f)
        }
    }

    fun restartGame() {
        birdY = 400f
        birdVelocity = 0f
        score = 0
        pipes.clear()
        gameState = GameState.PLAYING
    }

    fun jump() {
        if (gameState == GameState.READY) {
            restartGame()
            audioSynthesizer.playFlapSound()
            birdVelocity = -10.5f
        } else if (gameState == GameState.PLAYING) {
            audioSynthesizer.playFlapSound()
            birdVelocity = -10.5f
        }
    }

    // Main Game Loop (60 FPS tick)
    LaunchedEffect(gameState) {
        if (gameState != GameState.PLAYING) return@LaunchedEffect

        val gravity = 0.52f
        var spawnCounter = 0

        while (gameState == GameState.PLAYING) {
            delay(16) // ~60fps

            // Progressive difficulty calculations
            val currentSpeed = 4.2f + (score * 0.22f).coerceAtMost(8.5f)
            val currentGap = (240f - (score * 4f)).coerceAtLeast(155f)
            val spawnInterval = (95 - (score * 2)).coerceAtLeast(60)

            // Physics update
            birdVelocity += gravity
            birdY += birdVelocity

            // Pipe Spawning
            spawnCounter++
            if (spawnCounter >= spawnInterval) {
                spawnCounter = 0
                val minTop = 100f
                val maxTop = 550f
                val topHeight = minTop + Random.nextFloat() * (maxTop - minTop)
                pipes.add(PipeObstacle(x = 1100f, topHeight = topHeight, gap = currentGap))
            }

            // Move and check pipes
            val iterator = pipes.listIterator()
            while (iterator.hasNext()) {
                val pipe = iterator.next()
                pipe.x -= currentSpeed

                // Score point when passing bird (bird fixed at x = 200f)
                val birdX = 200f
                if (!pipe.scored && pipe.x + 60f < birdX) {
                    pipe.scored = true
                    score++
                    if (score > highScore) {
                        highScore = score
                    }
                    audioSynthesizer.playScoreSound()
                }

                // Remove off-screen pipes
                if (pipe.x < -100f) {
                    iterator.remove()
                }

                // Collision detection with pipe
                val pipeWidth = 64f
                val inPipeX = (birdX + birdRadius > pipe.x) && (birdX - birdRadius < pipe.x + pipeWidth)
                val inTopPipeY = birdY - birdRadius < pipe.topHeight
                val inBottomPipeY = birdY + birdRadius > (pipe.topHeight + pipe.gap)

                if (inPipeX && (inTopPipeY || inBottomPipeY)) {
                    audioSynthesizer.playCrashSound()
                    gameState = GameState.GAME_OVER
                    break
                }
            }

            // Floor and ceiling collisions
            if (birdY - birdRadius < 0f || birdY + birdRadius > 850f) {
                audioSynthesizer.playCrashSound()
                gameState = GameState.GAME_OVER
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                jump()
            }
    ) {
        // Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background cyber grid lines
            val gridStep = 40f
            for (gx in 0..(canvasWidth / gridStep).toInt()) {
                drawLine(
                    color = Color(0xFF141926),
                    start = Offset(gx * gridStep, 0f),
                    end = Offset(gx * gridStep, canvasHeight),
                    strokeWidth = 1f
                )
            }
            for (gy in 0..(canvasHeight / gridStep).toInt()) {
                drawLine(
                    color = Color(0xFF141926),
                    start = Offset(0f, gy * gridStep),
                    end = Offset(canvasWidth, gy * gridStep),
                    strokeWidth = 1f
                )
            }

            // Background ambient stars
            stars.forEach { star ->
                val x = (star.x) % canvasWidth
                val y = (star.y) % canvasHeight
                drawCircle(
                    color = Color(0x5500E5FF),
                    radius = 1.5f,
                    center = Offset(x, y)
                )
            }

            // Draw Pipes
            val pipeWidth = 64f
            pipes.forEach { pipe ->
                // Top Pipe
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF065F46), EmeraldGreen)
                    ),
                    topLeft = Offset(pipe.x, 0f),
                    size = Size(pipeWidth, pipe.topHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Top Pipe Lip
                drawRoundRect(
                    color = EmeraldGreen,
                    topLeft = Offset(pipe.x - 4f, pipe.topHeight - 16f),
                    size = Size(pipeWidth + 8f, 16f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Bottom Pipe
                val bottomY = pipe.topHeight + pipe.gap
                val bottomHeight = canvasHeight - bottomY
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(EmeraldGreen, Color(0xFF065F46))
                    ),
                    topLeft = Offset(pipe.x, bottomY),
                    size = Size(pipeWidth, bottomHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Bottom Pipe Lip
                drawRoundRect(
                    color = EmeraldGreen,
                    topLeft = Offset(pipe.x - 4f, bottomY),
                    size = Size(pipeWidth + 8f, 16f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Ground line
            drawLine(
                color = NeonCyan,
                start = Offset(0f, 850f),
                end = Offset(canvasWidth, 850f),
                strokeWidth = 3f
            )

            // Draw Cyber Bird
            val birdX = 200f
            val rotationAngle = (birdVelocity * 3.5f).coerceIn(-25f, 70f)

            rotate(degrees = rotationAngle, pivot = Offset(birdX, birdY)) {
                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(birdX, birdY),
                        radius = birdRadius * 1.8f
                    ),
                    radius = birdRadius * 1.8f,
                    center = Offset(birdX, birdY)
                )
                // Body
                drawCircle(
                    color = NeonCyan,
                    radius = birdRadius,
                    center = Offset(birdX, birdY)
                )
                // Wing
                val wingOffset = if (birdVelocity < 0) -4f else 4f
                drawRoundRect(
                    color = Color(0xFF007799),
                    topLeft = Offset(birdX - 16f, birdY + wingOffset),
                    size = Size(18f, 10f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                // Eye
                drawCircle(
                    color = DarkBackground,
                    radius = 4.5f,
                    center = Offset(birdX + 8f, birdY - 6f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2f,
                    center = Offset(birdX + 9f, birdY - 7f)
                )
                // Beak
                drawRoundRect(
                    color = WarmAmber,
                    topLeft = Offset(birdX + 16f, birdY - 3f),
                    size = Size(10f, 7f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }

        // Top HUD Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "POLY BIRD",
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeonCyan
                )
                Text(
                    text = "High Score: $highScore",
                    fontFamily = RobotoMonoFontFamily,
                    fontSize = 12.sp,
                    color = WarmAmber
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sound Toggle
                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        audioSynthesizer.isMuted = isMuted
                    },
                    modifier = Modifier.testTag("sound_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Sound",
                        tint = if (isMuted) TextMuted else NeonCyan
                    )
                }

                if (onCloseGame != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = onCloseGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurface,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("exit_game_button")
                    ) {
                        Text("Exit", fontSize = 12.sp)
                    }
                }
            }
        }

        // Big Centered Score during play
        if (gameState == GameState.PLAYING) {
            Text(
                text = "$score",
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 54.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            )
        }

        // Ready Banner
        if (gameState == GameState.READY) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮 POLY BIRD",
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developer Offline Mini-Game",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "• Tap screen to flap & gain altitude\n• Dodge neon circuit pipes\n• Speed & frequency increase as score rises",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { restartGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_game_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TAP OR CLICK TO FLAP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Game Over Banner
        if (gameState == GameState.GAME_OVER) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CRASHED!",
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SCORE", fontSize = 11.sp, color = TextMuted, fontFamily = RobotoMonoFontFamily)
                            Text("$score", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = RobotoMonoFontFamily)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BEST", fontSize = 11.sp, color = TextMuted, fontFamily = RobotoMonoFontFamily)
                            Text("$highScore", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = WarmAmber, fontFamily = RobotoMonoFontFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { restartGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("play_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRY AGAIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
