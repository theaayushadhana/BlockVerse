package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.GameViewModel

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val title: String,
    val isUser: Boolean = false
)

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("GLOBAL", "COUNTRY", "WEEKLY", "ALL TIME")

    val userScore = maxOf(uiState.highScore, uiState.score)

    val sampleLeaderboard = remember(userScore) {
        listOf(
            LeaderboardEntry(1, "NeonMaster99", 58420, "GRID GOD"),
            LeaderboardEntry(2, "CyberValkyrie", 47910, "BLOCKVERSE LEGEND"),
            LeaderboardEntry(3, "QuantumPulse", 39850, "COMBO DEMON"),
            LeaderboardEntry(4, "HyperMatrix", 28400, "PUZZLE BEAST"),
            LeaderboardEntry(5, "AeroBlade", 22150, "LINE DESTROYER"),
            LeaderboardEntry(6, "YOU (${uiState.profile.title})", userScore, uiState.profile.title, isUser = true),
            LeaderboardEntry(7, "ZenMaster", 15200, "GRID SURGEON"),
            LeaderboardEntry(8, "PixelNova", 11980, "BLOCK ROOKIE")
        ).sortedByDescending { it.score }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0F19), Color(0xFF131A2E))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "LEADERBOARDS",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Global High Score Rankings",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF111827),
                contentColor = Color(0xFFFFD700),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            // List of rankings
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleLeaderboard.size) { idx ->
                    val entry = sampleLeaderboard[idx]
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFE0E0E0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White.copy(alpha = 0.6f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUser) Color(0xFF00F0FF).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (entry.isUser) Color(0xFF00F0FF) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${entry.rank}",
                    color = rankColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(36.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = entry.name,
                        color = if (entry.isUser) Color(0xFF00F0FF) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.title,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "${entry.score}",
                color = if (entry.isUser) Color(0xFFFFD700) else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
