package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameMode
import com.example.ui.AppScreen
import com.example.ui.GameViewModel

@Composable
fun HomeScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val theme = uiState.profile.activeTheme

    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroPulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(theme.bgStart),
                        Color(0xFF0F172A),
                        Color(theme.bgEnd)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP HEADER: PROFILE & CURRENCY ---
            HomeTopBar(
                viewModel = viewModel,
                uiState = uiState
            )

            // --- SCROLLABLE CONTENT ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Title & Subtitle
                    Text(
                        text = "BLOCKVERSE",
                        color = Color(theme.glowColor),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        modifier = Modifier.testTag("app_title")
                    )
                    Text(
                        text = "AAA 10x10 PUZZLE EVOLUTION",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Large glowing PLAY CLASSIC button
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(20.dp, RoundedCornerShape(22.dp), ambientColor = Color(theme.glowColor), spotColor = Color(theme.glowColor))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(theme.glowColor), Color(0xFF3A86FF), Color(0xFF9D00FF))
                                )
                            )
                            .clickable { viewModel.startNewGame(GameMode.CLASSIC) }
                            .padding(horizontal = 32.dp, vertical = 18.dp)
                            .testTag("play_classic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PLAY CLASSIC",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Endless High Score Mode",
                                    color = Color.Black.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Quick Daily Reward Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDailyRewardDialog(true) }
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = "Reward",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "DAILY REWARD STREAK",
                                        color = Color(0xFFFFD700),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (uiState.dailyRewardClaimed) "Day ${uiState.dailyRewardStreakDay} Claimed! Next tomorrow" else "Day ${uiState.dailyRewardStreakDay} Ready to Claim!",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.toggleDailyRewardDialog(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.dailyRewardClaimed) Color.White.copy(alpha = 0.2f) else Color(0xFFFFD700)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (uiState.dailyRewardClaimed) "VIEW" else "CLAIM",
                                    color = if (uiState.dailyRewardClaimed) Color.White else Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "GAME MODES",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Modes Grid (2 columns)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = "TIME ATTACK",
                            subtitle = "60s Speed Rush",
                            icon = Icons.Default.Timer,
                            accentColor = Color(0xFFFF0054),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startNewGame(GameMode.TIME_ATTACK) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ModeCard(
                            title = "DAILY PUZZLE",
                            subtitle = "Day #142 Challenge",
                            icon = Icons.Default.Star,
                            accentColor = Color(0xFFFFBE0B),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startNewGame(GameMode.DAILY_PUZZLE) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = "CHALLENGES",
                            subtitle = "30 Sectors",
                            icon = Icons.Default.EmojiEvents,
                            accentColor = Color(0xFF06D6A0),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo(AppScreen.CHALLENGES) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ModeCard(
                            title = "ZEN & RELAX",
                            subtitle = "Calm Grid Flow",
                            icon = Icons.Default.Palette,
                            accentColor = Color(0xFF00F0FF),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.startNewGame(GameMode.ZEN) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ModeCard(
                        title = "DUEL (VS AI)",
                        subtitle = "Real-time AI Score Battle",
                        icon = Icons.Default.Whatshot,
                        accentColor = Color(0xFF9D00FF),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.startNewGame(GameMode.DUEL) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- BOTTOM NAVIGATION BAR ---
            HomeBottomNav(
                currentScreen = uiState.currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }

        // Daily Reward Dialog Modal
        if (uiState.showDailyRewardDialog) {
            DailyRewardsDialog(
                uiState = uiState,
                onClaim = { viewModel.claimDailyReward() },
                onDismiss = { viewModel.toggleDailyRewardDialog(false) }
            )
        }

        // First Launch Tutorial Modal
        if (uiState.showTutorial) {
            TutorialDialog(onClose = { viewModel.closeTutorial() })
        }
    }
}

@Composable
fun HomeTopBar(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Avatar & Level
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.COLLECTION) }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00F0FF))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LV.${uiState.profile.level}",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = uiState.profile.title,
                    color = Color(0xFF00F0FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                // XP Progress Bar
                LinearProgressIndicator(
                    progress = { (uiState.profile.xp.toFloat() / uiState.profile.xpToNextLevel).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(90.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF00F0FF),
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }

        // Coins & Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Coins Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "🪙", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${uiState.profile.coins}",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Streak Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF416C),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${uiState.profile.streak}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings Button
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun HomeBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0D1322),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.HOME,
            onClick = { onNavigate(AppScreen.HOME) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
            label = { Text("Play", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.COLLECTION,
            onClick = { onNavigate(AppScreen.COLLECTION) },
            icon = { Icon(Icons.Default.Palette, contentDescription = "Vault") },
            label = { Text("Vault", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.CHALLENGES,
            onClick = { onNavigate(AppScreen.CHALLENGES) },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Levels") },
            label = { Text("Levels", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.LEADERBOARD,
            onClick = { onNavigate(AppScreen.LEADERBOARD) },
            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Ranks") },
            label = { Text("Ranks", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.STATS,
            onClick = { onNavigate(AppScreen.STATS) },
            icon = { Icon(Icons.Default.QueryStats, contentDescription = "Stats") },
            label = { Text("Stats", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                unselectedTextColor = Color.White.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
fun DailyRewardsDialog(
    uiState: com.example.ui.GameUiState,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentDay = uiState.dailyRewardStreakDay

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1626)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "7-DAY REWARD STREAK",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Log in daily to claim epic rewards!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Days 1 to 7 Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (d in 1..4) {
                        RewardDayBox(
                            day = d,
                            coins = d * 100,
                            isCurrent = d == currentDay && !uiState.dailyRewardClaimed,
                            isClaimed = d < currentDay || (d == currentDay && uiState.dailyRewardClaimed),
                            modifier = Modifier.weight(1f).padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (d in 5..7) {
                        RewardDayBox(
                            day = d,
                            coins = if (d == 7) 1200 else d * 120,
                            isCurrent = d == currentDay && !uiState.dailyRewardClaimed,
                            isClaimed = d < currentDay || (d == currentDay && uiState.dailyRewardClaimed),
                            modifier = Modifier.weight(1f).padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!uiState.dailyRewardClaimed) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth().testTag("claim_daily_reward_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("CLAIM TODAY'S REWARD", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text(
                        text = "Come back tomorrow for your next reward!",
                        color = Color(0xFF06D6A0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CLOSE", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun RewardDayBox(
    day: Int,
    coins: Int,
    isCurrent: Boolean,
    isClaimed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isCurrent -> Color(0xFFFFD700).copy(alpha = 0.3f)
                    isClaimed -> Color.White.copy(alpha = 0.05f)
                    else -> Color.White.copy(alpha = 0.08f)
                }
            )
            .border(
                1.5.dp,
                if (isCurrent) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DAY $day", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (day == 7) "👑" else "🪙", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isClaimed) "CLAIMED" else "+$coins",
                color = if (isClaimed) Color(0xFF06D6A0) else Color(0xFFFFD700),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TutorialDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = Color(0xFF00F0FF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                TutorialStepRow("1", "DRAG BLOCKS", "Pick up blocks from the rack and position them anywhere on the 10x10 board.")
                TutorialStepRow("2", "FILL FULL LINES", "Complete full horizontal or vertical lines to clear them and score big points.")
                TutorialStepRow("3", "BUILD COMBOS", "Clear lines consecutively to multiply your points (x2, x3, x4+).")
                TutorialStepRow("4", "UNLEASH OVERDRIVE", "Fill your energy meter to trigger OVERDRIVE for double score and massive VFX!")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    modifier = Modifier.fillMaxWidth().testTag("close_tutorial_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("LET'S PLAY!", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun TutorialStepRow(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFF00F0FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}
