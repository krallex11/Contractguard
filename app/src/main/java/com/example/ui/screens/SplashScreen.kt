package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenBorder
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    var statusText by remember { mutableStateOf("Güvenli Kasa Başlatılıyor...") }

    val infiniteTransition = rememberInfiniteTransition(label = "PulseEffect")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    LaunchedEffect(Unit) {
        // Step 1: 0 -> 40% (0.8s)
        statusText = "AES-256 Kripto Modülü Yükleniyor..."
        progress.animateTo(0.4f, animationSpec = tween(1000, easing = LinearEasing))

        // Step 2: 40% -> 80% (1.2s)
        statusText = "SHA-256 Dijital İmza Doğrulanıyor..."
        progress.animateTo(0.85f, animationSpec = tween(1200, easing = LinearEasing))

        // Step 3: 80% -> 100% (1.0s)
        statusText = "Sözleşme Kasası Hazır"
        progress.animateTo(1f, animationSpec = tween(800, easing = LinearEasing))

        delay(350)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SleekDarkBackground,
                        Color(0xFF0D1527),
                        SleekDarkBackground
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(pulseScale)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SleekLimeGreenPrimary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Main Shield Logo with Glow Border
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                SleekDarkSurface,
                                Color(0xFF162544)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SleekLimeGreenPrimary,
                                SleekLimeGreenBorder.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "ContractGuard Shield Logo",
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name
            Text(
                text = "ContractGuard",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SleekTextWhite,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AES-256 E-İMZA & SÖZLEŞME KASASI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekLimeGreenPrimary,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Sleek Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E293B))
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxSize(),
                    color = SleekLimeGreenPrimary,
                    trackColor = Color(0xFF1E293B),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Status Text
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SleekTextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Security Stamp
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(alpha.value),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SleekLimeGreenPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "End-to-End Cryptographic Seal",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SleekTextMuted.copy(alpha = 0.8f)
            )
        }
    }
}
