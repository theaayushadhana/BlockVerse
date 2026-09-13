package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockSkinId
import com.example.model.BoardThemeId
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.components.BlockRenderer

@Composable
fun CollectionScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SKINS", "THEMES", "TITLES")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0F19), Color(0xFF141B2D))
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
                        .testTag("collection_back_button")
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
                        text = "THE VAULT",
                        color = Color(0xFF00F0FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Customize your visual identity",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF111827),
                contentColor = Color(0xFF00F0FF),
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
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Content per tab
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Block Skins
                        items(BlockSkinId.values().size) { idx ->
                            val skin = BlockSkinId.values()[idx]
                            val isEquipped = uiState.profile.activeSkin == skin
                            SkinCard(
                                skin = skin,
                                isEquipped = isEquipped,
                                onEquip = { viewModel.equipSkin(skin) }
                            )
                        }
                    }
                    1 -> {
                        // Board Themes
                        items(BoardThemeId.values().size) { idx ->
                            val theme = BoardThemeId.values()[idx]
                            val isEquipped = uiState.profile.activeTheme == theme
                            ThemeCard(
                                theme = theme,
                                isEquipped = isEquipped,
                                onEquip = { viewModel.equipTheme(theme) }
                            )
                        }
                    }
                    2 -> {
                        // Player Titles
                        val titles = listOf(
                            "BLOCK ROOKIE" to 1,
                            "GRID SURGEON" to 3,
                            "LINE DESTROYER" to 5,
                            "COMBO DEMON" to 10,
                            "PUZZLE BEAST" to 15,
                            "GRID GOD" to 25,
                            "BLOCKVERSE LEGEND" to 50
                        )
                        items(titles.size) { idx ->
                            val (title, reqLevel) = titles[idx]
                            val isUnlocked = uiState.profile.level >= reqLevel
                            val isEquipped = uiState.profile.title == title
                            TitleCard(
                                title = title,
                                reqLevel = reqLevel,
                                isUnlocked = isUnlocked,
                                isEquipped = isEquipped,
                                onEquip = { viewModel.equipTitle(title) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkinCard(
    skin: BlockSkinId,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isEquipped) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini 2x2 preview of blocks
                Canvas(modifier = Modifier.size(44.dp)) {
                    val cSize = size.width / 2f
                    BlockRenderer.drawBlockCell(this, Offset(0f, 0f), cSize, 0, skin = skin)
                    BlockRenderer.drawBlockCell(this, Offset(cSize, 0f), cSize, 1, skin = skin)
                    BlockRenderer.drawBlockCell(this, Offset(0f, cSize), cSize, 2, skin = skin)
                    BlockRenderer.drawBlockCell(this, Offset(cSize, cSize), cSize, 3, skin = skin)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = skin.displayName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = skin.rarity,
                        color = when (skin.rarity) {
                            "LEGENDARY" -> Color(0xFFFFD700)
                            "EPIC" -> Color(0xFFFF007F)
                            "RARE" -> Color(0xFF00F0FF)
                            else -> Color(0xFF06D6A0)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Button(
                onClick = onEquip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFF06D6A0) else Color(0xFF00F0FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isEquipped) {
                    Icon(Icons.Default.Check, contentDescription = "Equipped", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EQUIPPED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else {
                    Text("EQUIP", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: BoardThemeId,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isEquipped) Color(theme.glowColor) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color palette circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(theme.bgStart), Color(theme.glowColor))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = theme.displayName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Board Atmosphere",
                        color = Color(theme.glowColor),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onEquip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFF06D6A0) else Color(0xFF00F0FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEquipped) "EQUIPPED" else "EQUIP",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun TitleCard(
    title: String,
    reqLevel: Int,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEquipped) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isUnlocked) "Unlocked" else "Unlocks at Level $reqLevel",
                    color = if (isUnlocked) Color(0xFF06D6A0) else Color(0xFFFF416C),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onEquip,
                enabled = isUnlocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFFFFD700) else Color(0xFF00F0FF),
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEquipped) "EQUIPPED" else if (isUnlocked) "EQUIP" else "LOCKED",
                    color = if (isUnlocked) Color.Black else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}
