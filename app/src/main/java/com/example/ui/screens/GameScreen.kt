package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ads.AdsManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BoardThemeId
import com.example.model.GameMode
import com.example.model.GamePiece
import com.example.ui.GameViewModel
import com.example.ui.components.BlockRenderer
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val dragState by viewModel.dragState.collectAsState()
    val theme = uiState.profile.activeTheme

    var boardPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var boardSizePx by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(theme.bgStart),
                        Color(theme.bgEnd)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP HUD BAR ---
            TopHudBar(
                viewModel = viewModel,
                uiState = uiState,
                theme = theme
            )

            // --- CENTER 10x10 BOARD ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val boardWidth = minOf(maxWidth, maxHeight * 0.95f)
                    val shakeX = viewModel.vfxManager.shakeOffsetX
                    val shakeY = viewModel.vfxManager.shakeOffsetY

                    Box(
                        modifier = Modifier
                            .size(boardWidth)
                            .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
                            .shadow(24.dp, RoundedCornerShape(18.dp), ambientColor = Color(theme.glowColor), spotColor = Color(theme.glowColor))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(theme.gridBorder))
                            .padding(4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(theme.emptyCell))
                            .onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInRoot()
                                boardPositionInRoot = Offset(bounds.left, bounds.top)
                                boardSizePx = bounds.width
                            }
                    ) {
                        // Interactive Game Board Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("game_board_canvas")
                        ) {
                            val cellSize = size.width / 10f

                            // 1. Draw Empty Grid Cells with subtle bevels
                            for (r in 0 until 10) {
                                for (c in 0 until 10) {
                                    val cellTopLeft = Offset(c * cellSize, r * cellSize)
                                    val inset = cellSize * 0.05f

                                    val isNearMissRow = uiState.nearMissRows.contains(r)
                                    val isNearMissCol = uiState.nearMissCols.contains(c)

                                    val cellBg = when {
                                        isNearMissRow || isNearMissCol -> Color(theme.glowColor).copy(alpha = 0.12f)
                                        else -> Color(0xFF0F172A).copy(alpha = 0.65f)
                                    }

                                    drawRoundRect(
                                        color = cellBg,
                                        topLeft = Offset(cellTopLeft.x + inset, cellTopLeft.y + inset),
                                        size = Size(cellSize - inset * 2, cellSize - inset * 2),
                                        cornerRadius = CornerRadius(cellSize * 0.14f, cellSize * 0.14f)
                                    )

                                    // Subtle inner grid border
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.05f),
                                        topLeft = Offset(cellTopLeft.x + inset, cellTopLeft.y + inset),
                                        size = Size(cellSize - inset * 2, cellSize - inset * 2),
                                        cornerRadius = CornerRadius(cellSize * 0.14f, cellSize * 0.14f),
                                        style = Stroke(width = 1f)
                                    )
                                }
                            }

                            // 2. Draw Smart Hint highlight if active
                            uiState.smartHint?.let { (hintPieceIdx, hr, hc) ->
                                val hintPiece = uiState.availablePieces.getOrNull(hintPieceIdx)
                                if (hintPiece != null) {
                                    for (pr in 0 until hintPiece.rows) {
                                        for (pc in 0 until hintPiece.cols) {
                                            if (hintPiece.shape[pr][pc] == 1) {
                                                val r = hr + pr
                                                val c = hc + pc
                                                if (r in 0 until 10 && c in 0 until 10) {
                                                    drawRoundRect(
                                                        color = Color(0xFFFFD700).copy(alpha = 0.35f),
                                                        topLeft = Offset(c * cellSize + cellSize * 0.05f, r * cellSize + cellSize * 0.05f),
                                                        size = Size(cellSize * 0.9f, cellSize * 0.9f),
                                                        cornerRadius = CornerRadius(cellSize * 0.15f, cellSize * 0.15f),
                                                        style = Stroke(width = 2.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Draw Placed Blocks
                            for (r in 0 until 10) {
                                for (c in 0 until 10) {
                                    val cell = uiState.grid[r][c]
                                    if (cell.filled) {
                                        BlockRenderer.drawBlockCell(
                                            drawScope = this,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = cellSize,
                                            colorIndex = cell.colorIndex,
                                            specialType = cell.specialType,
                                            skin = uiState.profile.activeSkin,
                                            isOverdrive = uiState.isOverdriveActive
                                        )
                                    }
                                }
                            }

                            // 4. Draw Ghost Piece Preview during drag
                            if (dragState.isDragging && dragState.isValid && dragState.gridRow >= 0 && dragState.gridCol >= 0) {
                                val piece = uiState.availablePieces.getOrNull(dragState.pieceIndex)
                                if (piece != null) {
                                    for (pr in 0 until piece.rows) {
                                        for (pc in 0 until piece.cols) {
                                            if (piece.shape[pr][pc] == 1) {
                                                val gr = dragState.gridRow + pr
                                                val gc = dragState.gridCol + pc
                                                if (gr in 0 until 10 && gc in 0 until 10) {
                                                    BlockRenderer.drawBlockCell(
                                                        drawScope = this,
                                                        topLeft = Offset(gc * cellSize, gr * cellSize),
                                                        size = cellSize,
                                                        colorIndex = piece.colorIndex,
                                                        specialType = piece.specialType,
                                                        skin = uiState.profile.activeSkin,
                                                        isGhost = true,
                                                        alpha = 0.85f
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Draw Particles and Confetti
                            viewModel.vfxManager.drawParticles(this)
                        }

                        // Floating score text overlays
                        for (ft in viewModel.vfxManager.floatingTexts) {
                            Text(
                                text = ft.text,
                                color = ft.color.copy(alpha = ft.alpha),
                                fontSize = (22 * ft.scale).sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (ft.x - boardPositionInRoot.x).roundToInt(),
                                            (ft.y - boardPositionInRoot.y).roundToInt()
                                        )
                                    }
                            )
                        }
                    }
                }
            }

            // --- BOTTOM PIECE RACK & CONTROLS ---
            BottomPieceRack(
                viewModel = viewModel,
                uiState = uiState,
                boardPosition = boardPositionInRoot,
                boardSizePx = boardSizePx
            )
        }

        // --- DRAGGING PIECE FLOATING UNDER/ABOVE FINGER ---
        if (dragState.isDragging) {
            val draggingPiece = uiState.availablePieces.getOrNull(dragState.pieceIndex)
            if (draggingPiece != null && boardSizePx > 0f) {
                val cellSize = boardSizePx / 10f
                val liftY = cellSize * 1.8f
                val pieceWidthPx = draggingPiece.cols * cellSize
                val pieceHeightPx = draggingPiece.rows * cellSize
                val posX = dragState.touchOffset.x - (pieceWidthPx / 2f)
                val posY = dragState.touchOffset.y - (pieceHeightPx / 2f) - liftY

                Box(
                    modifier = Modifier
                        .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                        .size((draggingPiece.cols * 36).dp, (draggingPiece.rows * 36).dp)
                        .scale(1.08f)
                        .shadow(16.dp, RoundedCornerShape(8.dp), ambientColor = Color(theme.glowColor))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cSize = size.width / draggingPiece.cols
                        for (r in 0 until draggingPiece.rows) {
                            for (c in 0 until draggingPiece.cols) {
                                if (draggingPiece.shape[r][c] == 1) {
                                    BlockRenderer.drawBlockCell(
                                        drawScope = this,
                                        topLeft = Offset(c * cSize, r * cSize),
                                        size = cSize,
                                        colorIndex = draggingPiece.colorIndex,
                                        specialType = draggingPiece.specialType,
                                        skin = uiState.profile.activeSkin,
                                        isOverdrive = uiState.isOverdriveActive
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PAUSE DIALOG ---
        if (uiState.isPaused) {
            PauseDialog(
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // --- SECOND CHANCE (REWARDED AD REVIVE) DIALOG ---
        if (uiState.isGameOver && uiState.showSecondChanceDialog) {
            SecondChanceReviveDialog(
                viewModel = viewModel,
                uiState = uiState
            )
        } else if (uiState.isGameOver) {
            // --- GAME OVER SUMMARY (WITH 2X COIN REWARDED AD) ---
            GameOverDialog(
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // --- VICTORY DIALOG (WITH INTERSTITIAL AD ON ADVANCE) ---
        if (uiState.isGameWon) {
            VictoryDialog(
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // --- SHARE CARD DIALOG ---
        if (uiState.showShareDialog) {
            ShareCardDialog(
                uiState = uiState,
                onDismiss = { viewModel.toggleShareDialog(false) }
            )
        }
    }
}

@Composable
fun TopHudBar(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState,
    theme: BoardThemeId
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause Button
            IconButton(
                onClick = { viewModel.pauseGame() },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }

            // Mode Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = uiState.gameMode.displayName.uppercase(),
                    color = Color(theme.glowColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Best Score Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${uiState.highScore}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big Current Score & Combo Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCORE",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.score}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Combo Badge
            if (uiState.combo > 1) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF416C)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFFFF416C))
                        .scale(1.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Combo",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COMBO x${uiState.combo}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Time Attack Timer or Duel Score
            if (uiState.gameMode == GameMode.TIME_ATTACK) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (uiState.timeAttackSeconds <= 15) Color(0xFFFF2A2A) else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.timeAttackSeconds}s",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (uiState.gameMode == GameMode.DUEL) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("AI RIVAL", color = Color(0xFFFF3366), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${uiState.duelAiScore}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Event Banner
        uiState.activeEvent?.let { event ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF00F0FF).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = event.title, color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(text = event.subtitle, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Overdrive Progress Bar
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(uiState.overdriveMeter / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFFFFD700), Color(0xFFFF007F))
                            )
                        )
                )
            }

            if (uiState.overdriveMeter >= 100f && !uiState.isOverdriveActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.activateOverdrive() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("activate_overdrive_button")
                ) {
                    Text("OVERDRIVE!", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            } else if (uiState.isOverdriveActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔥 2X ACTIVE", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun BottomPieceRack(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState,
    boardPosition: Offset,
    boardSizePx: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3 Piece Slots
        for (i in 0..2) {
            val piece = uiState.availablePieces.getOrNull(i)
            PieceSlotCard(
                piece = piece,
                index = i,
                viewModel = viewModel,
                boardPosition = boardPosition,
                boardSizePx = boardSizePx,
                activeSkin = uiState.profile.activeSkin,
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .padding(horizontal = 4.dp)
            )
        }

        // Smart Hint Button
        IconButton(
            onClick = { viewModel.requestHint() },
            modifier = Modifier
                .size(42.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .testTag("request_hint_button")
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Hint",
                tint = if (uiState.smartHint != null) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PieceSlotCard(
    piece: GamePiece?,
    index: Int,
    viewModel: GameViewModel,
    boardPosition: Offset,
    boardSizePx: Float,
    activeSkin: com.example.model.BlockSkinId,
    modifier: Modifier = Modifier
) {
    var pieceCardOffsetInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInRoot()
                pieceCardOffsetInRoot = Offset(bounds.left, bounds.top)
            }
            .pointerInput(piece) {
                if (piece != null) {
                    detectDragGestures(
                        onDragStart = { localOffset ->
                            val startOffsetInRoot = Offset(
                                pieceCardOffsetInRoot.x + localOffset.x,
                                pieceCardOffsetInRoot.y + localOffset.y
                            )
                            viewModel.onDragStart(index, startOffsetInRoot)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val currentPosInRoot = Offset(
                                pieceCardOffsetInRoot.x + change.position.x,
                                pieceCardOffsetInRoot.y + change.position.y
                            )
                            viewModel.onDragMove(currentPosInRoot, boardPosition, boardSizePx)
                        },
                        onDragEnd = {
                            viewModel.onDragEnd(boardPosition, boardSizePx)
                        },
                        onDragCancel = {
                            viewModel.onDragCancel()
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            // Draw piece preview scaled to fit slot
            Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .testTag("piece_slot_$index")
            ) {
                val maxDim = maxOf(piece.rows, piece.cols)
                val cellSize = (size.width / maxDim) * 0.85f
                val totalW = piece.cols * cellSize
                val totalH = piece.rows * cellSize
                val startX = (size.width - totalW) / 2f
                val startY = (size.height - totalH) / 2f

                for (r in 0 until piece.rows) {
                    for (c in 0 until piece.cols) {
                        if (piece.shape[r][c] == 1) {
                            BlockRenderer.drawBlockCell(
                                drawScope = this,
                                topLeft = Offset(startX + c * cellSize, startY + r * cellSize),
                                size = cellSize,
                                colorIndex = piece.colorIndex,
                                specialType = piece.specialType,
                                skin = activeSkin
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState
) {
    Dialog(onDismissRequest = { viewModel.resumeGame() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B2D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Score: ${uiState.score}",
                    color = Color(0xFF00F0FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.resumeGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    modifier = Modifier.fillMaxWidth().testTag("resume_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.restartGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth().testTag("restart_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RESTART", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.quitToHome() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().testTag("quit_home_button")
                ) {
                    Text("MAIN MENU", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFF416C).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isNewHighScore) {
                    Text(
                        text = "🔥 NEW HIGH SCORE! 🔥",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = "GAME OVER",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${uiState.score}",
                    color = Color(0xFF00F0FF),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LINES", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("${uiState.linesClearedTotal}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MAX COMBO", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("x${uiState.combo}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COINS", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("+${uiState.earnedCoins}", color = Color(0xFF06D6A0), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rewarded Ad 2X Coin & XP Multiplier
                if (!uiState.rewardsDoubled) {
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                AdsManager.showRewarded(
                                    activity = activity,
                                    onUserEarnedReward = { viewModel.doubleCoinsReward() },
                                    onAdClosed = {}
                                )
                            } else {
                                viewModel.doubleCoinsReward()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rewarded_ad_double_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Watch Ad", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("2X REWARDS (WATCH AD)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Text("✅ 2X BONUS COINS APPLIED!", color = Color(0xFF06D6A0), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (uiState.isNewHighScore && activity != null) {
                            AdsManager.showInterstitial(activity) { viewModel.restartGame() }
                        } else {
                            viewModel.restartGame()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    modifier = Modifier.fillMaxWidth().testTag("game_over_retry_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.toggleShareDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f).testTag("game_over_share_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SHARE", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (uiState.isNewHighScore && activity != null) {
                                AdsManager.showInterstitial(activity) { viewModel.quitToHome() }
                            } else {
                                viewModel.quitToHome()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f).testTag("game_over_home_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Rewarded Ad Revive Modal:
 * Triggered on loss/game over. Gives player a Second Chance by clearing center blocks
 * and refilling pieces so they continue their high score run.
 */
@Composable
fun SecondChanceReviveDialog(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF00F0FF).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF00F0FF).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Second Chance",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SECOND CHANCE!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Out of moves! Watch a short video ad to clear center blocks and keep your score of ${uiState.score} going!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score Display
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SAVED SCORE: ", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text("${uiState.score}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Watch Rewarded Ad to Revive
                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            AdsManager.showRewarded(
                                activity = activity,
                                onUserEarnedReward = { viewModel.reviveGame() },
                                onAdClosed = {}
                            )
                        } else {
                            viewModel.reviveGame()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revive_watch_ad_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Watch Ad", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REVIVE (WATCH AD)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.dismissSecondChance() },
                    modifier = Modifier.testTag("revive_skip_button")
                ) {
                    Text("No Thanks, End Game", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Victory Modal:
 * Triggered on winning a puzzle sector or defeating AI.
 * Shows Interstitial Ad upon advancing or continuing.
 */
@Composable
fun VictoryDialog(
    viewModel: GameViewModel,
    uiState: com.example.ui.GameUiState
) {
    val context = LocalContext.current
    val level = uiState.wonChallengeLevel

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (level != null) "SECTOR #${level.levelNumber} CLEARED!" else "VICTORY! YOU WON!",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3 Stars Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (s <= uiState.wonStars) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(34.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COINS", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("+${uiState.earnedCoins}", color = Color(0xFF06D6A0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("XP", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("+${uiState.earnedXp}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Next Level Button (Triggers Interstitial Ad on Victory!)
                if (level != null && level.levelNumber < 30) {
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                AdsManager.showInterstitial(activity) {
                                    viewModel.onVictoryProceed(nextLevel = true)
                                }
                            } else {
                                viewModel.onVictoryProceed(nextLevel = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("victory_next_level_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("NEXT SECTOR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Continue Button (Also respects Interstitial Ad)
                OutlinedButton(
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            AdsManager.showInterstitial(activity) {
                                viewModel.onVictoryProceed(nextLevel = false)
                            }
                        } else {
                            viewModel.onVictoryProceed(nextLevel = false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("victory_continue_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("CONTINUE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShareCardDialog(
    uiState: com.example.ui.GameUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡ BLOCKVERSE ⚡",
                    color = Color(0xFF00F0FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "CAN YOU BEAT MY SCORE?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // High score badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF141B2D), Color(0xFF221133))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.score}",
                            color = Color(0xFFFFD700),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mode: ${uiState.gameMode.displayName}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Combo x${uiState.combo} • Lines: ${uiState.linesClearedTotal}",
                            color = Color(0xFF00F0FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val shareText = "I scored ${uiState.score} with a Combo x${uiState.combo} in Blockverse! Can you beat my score?"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share your Blockverse score")
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    modifier = Modifier.fillMaxWidth().testTag("share_intent_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHARE VIA APPS", color = Color.Black, fontWeight = FontWeight.Black)
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
