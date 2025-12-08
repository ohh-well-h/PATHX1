package com.example.pathx01.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Animate the gradient position instead of colors for smoother effect
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = colors,
                    radius = 1000f,
                    center = androidx.compose.ui.geometry.Offset(
                        x = 500f + (animationProgress * 200f),
                        y = 500f + (animationProgress * 200f)
                    )
                )
            )
    )
}

@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 6
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    repeat(particleCount) { index ->
        val delay = index * 500
        val offsetX by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 300f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000 + delay, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetX$index"
        )
        
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 200f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000 + delay, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY$index"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + delay, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha$index"
        )
        
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    this.alpha = alpha
                }
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
    }
}
