package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.mycrushpizza.`in`.R
import com.example.ui.components.BrightClearFoodColorFilter
import com.example.ui.theme.CrushRed
import com.example.ui.theme.CrushWhite
import com.example.ui.theme.CrushYellow
import com.example.util.GoogleSignInHelper

/**
 * Redesigned Bright Domino's-Style Sign-In Screen for MY CRUSH PIZZA – HAJIPUR:
 * - Top Vibrant Crimson-Gold Showcase Header with Top-Right "Skip ➔" button and Bright 3-Dish Food Collage
 * - Bottom Clean Snow-White Card with 3 Highlight Pills, Crimson "Continue with Google" CTA, and "Explore Menu as Guest" button
 */
@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onSkipToGuestMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Crimson-Gold Festive Backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE31837),
                            Color(0xFFC81E32),
                            Color(0xFF9F1239)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0x26FFD54F),
                    radius = 120.dp.toPx(),
                    center = Offset(size.width * 0.18f, size.height * 0.35f)
                )
                drawCircle(
                    color = Color(0x22FFB300),
                    radius = 140.dp.toPx(),
                    center = Offset(size.width * 0.85f, size.height * 0.28f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 540.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Pinned Mobile-Responsive Top Bar: Brand Identity on Left & Crisp "Skip ➔" Pill on Top-Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, CrushYellow),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
                            contentDescription = "My Crush Pizza Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MY CRUSH PIZZA",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = CrushYellow,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "RN College Chautha Road, Hajipur",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = Color(0xFFFFF3E0),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Mobile-Friendly Top-Right "Skip" Button (Always visible, never wraps or clips)
                Surface(
                    onClick = onSkipToGuestMenu,
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("skip_guest_btn"),
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, CrushYellow),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFFE31837),
                            maxLines = 1,
                            softWrap = false
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Skip",
                            tint = Color(0xFFE31837),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 2. Bright 3-Photo Food Showcase Collage in the Top Hero Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left Food Card (Peppy Paneer Pizza)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(2.5.dp, Color.White),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .size(width = 122.dp, height = 144.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = 6.dp, y = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_peppy_paneer_golden_1794920700880),
                            contentDescription = "Peppy Paneer Pizza",
                            contentScale = ContentScale.Crop,
                            colorFilter = BrightClearFoodColorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Right Food Card (Tandoori Burger & Sides)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(2.5.dp, Color.White),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .size(width = 122.dp, height = 144.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = (-6).dp, y = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_tandoori_paneer_burger_clean),
                            contentDescription = "Burgers & Sides",
                            contentScale = ContentScale.Crop,
                            colorFilter = BrightClearFoodColorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Center Main Pizza Showcase Card
                    Box(
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier.size(width = 178.dp, height = 178.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black,
                            border = BorderStroke(3.dp, Color(0xFFFFD54F)),
                            shadowElevation = 18.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_my_crush_pizza_logo),
                                contentDescription = "My Crush Pizza Official Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Surface(
                            color = Color(0xFF059669),
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
                                    imageVector = Icons.Filled.DeliveryDining,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "30-45 MINS HOT DELIVERY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Clean Snow-White Sign-In & Welcome Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(16.dp, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFFF1F2),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3))
                        ) {
                            Text(
                                text = "Your Cravings, Our Passion ❤️",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE31837),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }

                        Text(
                            text = "Hot Pizzas, Kulhad Pizza & Combos!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 22.sp,
                                lineHeight = 28.sp
                            ),
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Sign in with Google for 1-tap ordering, live GPS delivery updates, and instant Wallet Balance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )

                        // 3 Bright Benefit Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrightBenefitBadge(
                                icon = Icons.Filled.Timer,
                                title = "30 Mins",
                                subtitle = "Fast Delivery",
                                tint = Color(0xFFE31837),
                                bg = Color(0xFFFFF1F2),
                                modifier = Modifier.weight(1f)
                            )
                            BrightBenefitBadge(
                                icon = Icons.Filled.DeliveryDining,
                                title = "Free Delivery",
                                subtitle = "Above ₹299",
                                tint = Color(0xFF059669),
                                bg = Color(0xFFECFDF5),
                                modifier = Modifier.weight(1f)
                            )
                            BrightBenefitBadge(
                                icon = Icons.Filled.AccountBalanceWallet,
                                title = "My Wallet",
                                subtitle = "Easy Balance",
                                tint = Color(0xFFD97706),
                                bg = Color(0xFFFFFBEB),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (errorMessage != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFF1F2),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE31837))
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9F1239),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Primary Domino's Crimson "Continue with Google" Button
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                GoogleSignInHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onSignInSuccess()
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_sign_in_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE31837),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in with Google...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Surface(
                                    color = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFFE31837),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BrightBenefitBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
