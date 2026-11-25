package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycrushpizza.`in`.R
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Redesigned Bright & Vibrant Domino's-Style Visiting Splash Animation for MY CRUSH PIZZA – HAJIPUR:
 * - Warm Crimson-to-Amber Sunburst Studio Backdrop with rotating golden light beams & sparkles
 * - 3-Dish Floating Food Showcase Trio (Center Signature Cheese Pizza + Left Burger + Right Kulhad Pizza)
 * - Clean White & Gold Floating Brand Card with 30-Mins Express Delivery Scooter Pill & Progress Indicator
 */
@Composable
fun MyCrushSplashAnimationOverlay(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val centerScale = remember { Animatable(0.45f) }
    val sideSpread = remember { Animatable(0f) }
    val cardSlideY = remember { Animatable(40f) }
    val cardAlpha = remember { Animatable(0f) }
    val rayPhase = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            // 1. Center Pizza Hero spring pop-in
            launch {
                centerScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            // 2. Left & Right satellite food medallions fan out smoothly
            launch {
                delay(120L)
                sideSpread.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            // 3. White Brand Card slides up & fades in
            launch {
                delay(150L)
                cardAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
            }
            launch {
                delay(150L)
                cardSlideY.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
            }
            // 4. Sunburst rays & progress bar sweep
            launch {
                rayPhase.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1550, easing = LinearEasing)
                )
            }
        }
        overlayAlpha.animateTo(0f, tween(durationMillis = 200, easing = FastOutSlowInEasing))
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE31837),
                        Color(0xFFC81E32),
                        Color(0xFF991B1B)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
            .testTag("my_crush_splash_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Golden Sunburst Rays & Sparkling Bokeh Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.34f
            val maxRadius = size.maxDimension * 0.65f

            // Soft warm golden radial glow behind food trio
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x66FFD54F),
                        Color(0x28FFB300),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = 240.dp.toPx()
                ),
                radius = 240.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Rotating sunburst beams
            val rayCount = 12
            val baseRotation = rayPhase.value * 24f
            for (i in 0 until rayCount) {
                val startAngle = Math.toRadians((i * (360f / rayCount) + baseRotation).toDouble())
                val endAngle = Math.toRadians((i * (360f / rayCount) + 10f + baseRotation).toDouble())
                val beamPath = Path().apply {
                    moveTo(cx, cy)
                    lineTo(
                        cx + cos(startAngle).toFloat() * maxRadius,
                        cy + sin(startAngle).toFloat() * maxRadius
                    )
                    lineTo(
                        cx + cos(endAngle).toFloat() * maxRadius,
                        cy + sin(endAngle).toFloat() * maxRadius
                    )
                    close()
                }
                drawPath(
                    path = beamPath,
                    color = Color.White.copy(alpha = 0.06f)
                )
            }

            // Sparkling golden dots around the trio
            val sparkleAngles = listOf(20f, 65f, 115f, 160f, 205f, 255f, 300f, 340f)
            sparkleAngles.forEachIndexed { index, deg ->
                val rad = Math.toRadians((deg + rayPhase.value * 30f).toDouble())
                val dist = (135 + (index % 3) * 22).dp.toPx() * sideSpread.value.coerceAtLeast(0.5f)
                val sx = cx + cos(rad).toFloat() * dist
                val sy = cy + sin(rad).toFloat() * dist
                drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = 0.85f),
                    radius = (4 + (index % 3) * 2).dp.toPx(),
                    center = Offset(sx, sy)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(horizontal = 22.dp)
        ) {
            // Top Express Delivery Pill
            Surface(
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier.scale(centerScale.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeliveryDining,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "30-45 MINS HOT DELIVERY • HAJIPUR",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 3-Dish Bright Food Showcase Trio (Left Burger + Center Pizza + Right Kulhad Pizza)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Satellite Food Medallion (Tandoori Burger & Fries)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(3.dp, Color(0xFFFFD54F)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(106.dp)
                        .offset(
                            x = (-98 * sideSpread.value).dp,
                            y = (14 * sideSpread.value).dp
                        )
                        .graphicsLayer {
                            scaleX = 0.6f + 0.4f * sideSpread.value
                            scaleY = 0.6f + 0.4f * sideSpread.value
                            rotationZ = -10f * sideSpread.value
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_tandoori_paneer_burger_clean),
                        contentDescription = "Burgers & Sides",
                        contentScale = ContentScale.Crop,
                        colorFilter = BrightClearFoodColorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                // Right Satellite Food Medallion (Kulhad Pizza & Shakes)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(3.dp, Color(0xFFFFD54F)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(106.dp)
                        .offset(
                            x = (98 * sideSpread.value).dp,
                            y = (14 * sideSpread.value).dp
                        )
                        .graphicsLayer {
                            scaleX = 0.6f + 0.4f * sideSpread.value
                            scaleY = 0.6f + 0.4f * sideSpread.value
                            rotationZ = 10f * sideSpread.value
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_veg_kulhad_pizza_clean),
                        contentDescription = "Kulhad Pizza",
                        contentScale = ContentScale.Crop,
                        colorFilter = BrightClearFoodColorFilter,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                // Center Hero Showcase Medallion (Official My Crush Pizza Logo)
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .size(182.dp)
                        .graphicsLayer {
                            scaleX = centerScale.value
                            scaleY = centerScale.value
                        }
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Black,
                        border = BorderStroke(3.dp, Color(0xFFFFD54F)),
                        shadowElevation = 22.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
                            contentDescription = "My Crush Pizza Official Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                        )
                    }

                    // Golden Star Ribbon Pill at bottom of Center Pizza
                    Surface(
                        color = Color(0xFFFFB300),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 6.dp,
                        modifier = Modifier.offset(y = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFF4A0418),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "100% FRESH OVEN BAKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4A0418),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Clean White & Gold Brand Showcase Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha.value)
                    .offset(y = cardSlideY.value.dp)
                    .shadow(20.dp, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFFFD54F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
                                contentDescription = "My Crush Pizza Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "MY CRUSH PIZZA",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 27.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFFE31837),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CHAUHATTA RN COLLEGE ROAD • HAJIPUR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your Cravings, Our Passion ❤️",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("splash_tagline_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Crimson Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(rayPhase.value.coerceIn(0.15f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(CrushRed, Color(0xFFFF8F00))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
