package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PizzaOrder
import com.example.data.model.formatInr
import com.example.ui.theme.CrushRed
import com.example.util.OrderSuccessSoundPlayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Celebratory "Order Placed Successfully" Overlay:
 * - Animated green circle popping in with spring bounce + soft radiating emerald ripples
 * - Crisp white checkmark tick drawn smoothly inside the green circle via PathMeasure
 * - Bold "Order Placed Successfully" title right beneath the green circle
 * - Plays OrderSuccessSoundPlayer chime once automatically when opened
 * - Offers 1-tap Track Order, WhatsApp Order, and Call Store actions
 */
@Composable
fun OrderPlacedSuccessDialog(
    order: PizzaOrder,
    storePhone: String,
    onTrackOrderClick: () -> Unit,
    onWhatsAppOrderClick: () -> Unit,
    onCallStoreClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val circleScale = remember(order.id) { Animatable(0f) }
    val tickProgress = remember(order.id) { Animatable(0f) }
    val burstProgress = remember(order.id) { Animatable(0f) }
    var showContent by remember(order.id) { mutableStateOf(false) }

    val pulseTransition = rememberInfiniteTransition(label = "order_success_ripple")
    val rippleFraction by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_fraction"
    )

    LaunchedEffect(order.id) {
        OrderSuccessSoundPlayer.playOrderPlacedSound(context)
        circleScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        burstProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
        )
        tickProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
        delay(80L)
        showContent = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB30F172A))
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .testTag("order_placed_whatsapp_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_placed_success_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                border = BorderStroke(1.dp, Color(0xFFD1FAE5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Animated Green Circle + Radiating Ripple + Smoothly Drawn White Tick
                    Box(
                        modifier = Modifier
                            .size(156.dp)
                            .testTag("order_placed_green_tick_animation"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val maxRippleRadius = size.minDimension * 0.49f
                            val baseCircleRadius = size.minDimension * 0.30f

                            // Outer pulsing emerald ring
                            val rippleRadius = baseCircleRadius + (maxRippleRadius - baseCircleRadius) * rippleFraction
                            drawCircle(
                                color = Color(0xFF22C55E).copy(alpha = (1f - rippleFraction) * 0.28f),
                                radius = rippleRadius,
                                center = center
                            )

                            // Soft halo ring
                            drawCircle(
                                color = Color(0xFFDCFCE7),
                                radius = baseCircleRadius * 1.18f * circleScale.value,
                                center = center
                            )

                            // Festive burst dots popping around the green circle
                            if (burstProgress.value > 0.02f) {
                                val dotColors = listOf(
                                    Color(0xFF22C55E),
                                    Color(0xFFF59E0B),
                                    Color(0xFFE31837),
                                    Color(0xFF10B981),
                                    Color(0xFF3B82F6),
                                    Color(0xFFF97316)
                                )
                                val dotCount = 10
                                for (i in 0 until dotCount) {
                                    val angleRad = (i * (360.0 / dotCount)) * PI / 180.0
                                    val dist = baseCircleRadius * (1.20f + 0.36f * burstProgress.value)
                                    val dx = center.x + (cos(angleRad) * dist).toFloat()
                                    val dy = center.y + (sin(angleRad) * dist).toFloat()
                                    val dotRadius = (4.5.dp.toPx()) * (1f - 0.35f * burstProgress.value)
                                    drawCircle(
                                        color = dotColors[i % dotColors.size].copy(alpha = (1f - 0.2f * burstProgress.value)),
                                        radius = dotRadius,
                                        center = Offset(dx, dy)
                                    )
                                }
                            }
                        }

                        // Vibrant Bouncy Green Circle with Animated Stroke-Drawn Checkmark
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(circleScale.value)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF22C55E),
                                            Color(0xFF16A34A),
                                            Color(0xFF15803D)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(56.dp)) {
                                val fullPath = Path().apply {
                                    moveTo(size.width * 0.16f, size.height * 0.52f)
                                    lineTo(size.width * 0.41f, size.height * 0.77f)
                                    lineTo(size.width * 0.85f, size.height * 0.27f)
                                }
                                val measure = PathMeasure()
                                measure.setPath(fullPath, false)
                                val animatedSegment = Path()
                                measure.getSegment(
                                    startDistance = 0f,
                                    stopDistance = measure.length * tickProgress.value,
                                    destination = animatedSegment,
                                    startWithMoveTo = true
                                )
                                drawPath(
                                    path = animatedSegment,
                                    color = Color.White,
                                    style = Stroke(
                                        width = 6.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Bold "Order Placed Successfully" Title
                    Text(
                        text = "Order Placed Successfully",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF15803D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("order_placed_success_title")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Order Number + Delivery ETA Pill Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "#${order.orderNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "30-45 mins",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 5 }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Summary Card (Bill, Items, Delivery Address)
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total Payable (${order.paymentMethod})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = formatInr(order.totalInr),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    if (order.bonusUsedInr > 0) {
                                        Text(
                                            text = "Bonus Cash Applied: -${formatInr(order.bonusUsedInr)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    HorizontalDivider(color = Color(0xFFE2E8F0))

                                    Text(
                                        text = order.itemsSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (order.deliveryAddress.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.LocationOn,
                                                contentDescription = null,
                                                tint = CrushRed,
                                                modifier = Modifier
                                                    .padding(top = 1.dp)
                                                    .size(15.dp)
                                            )
                                            Text(
                                                text = order.deliveryAddress,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // Primary CTA: Track My Order
                            Button(
                                onClick = onTrackOrderClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("order_success_track_btn")
                            ) {
                                Text(
                                    text = "Track My Order Live →",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Secondary Action Row: WhatsApp & Call Store
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onWhatsAppOrderClick,
                                    border = BorderStroke(1.2.dp, Color(0xFF16A34A)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF15803D)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("dialog_whatsapp_order_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "WhatsApp",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }

                                OutlinedButton(
                                    onClick = onCallStoreClick,
                                    border = BorderStroke(1.2.dp, CrushRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrushRed),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("dialog_call_store_btn")
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Call Store",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }

                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("order_success_continue_btn")
                            ) {
                                Text(
                                    text = "Continue Browsing ($storePhone)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
