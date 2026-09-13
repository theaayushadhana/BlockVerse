package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.BlockSkinId
import com.example.model.SpecialBlockType

/**
 * AAA-quality block rendering with 3D bevels, inner specular highlights,
 * jewel glow, and distinctive special block glyphs.
 */
object BlockRenderer {

    // Palette of 8 luminous jewel block colors
    val PALETTE_COLORS = listOf(
        Color(0xFF00F0FF), // 0: Neon Cyan
        Color(0xFF9D00FF), // 1: Cyber Violet
        Color(0xFFFFB703), // 2: Radiant Amber
        Color(0xFF06D6A0), // 3: Emerald
        Color(0xFFFF0054), // 4: Ruby Rose
        Color(0xFFFF007F), // 5: Electric Magenta
        Color(0xFF3A86FF), // 6: Deep Azure
        Color(0xFFFFBE0B)  // 7: Celestial Gold
    )

    fun getBlockBaseColor(colorIndex: Int): Color {
        return PALETTE_COLORS[colorIndex.coerceIn(0, PALETTE_COLORS.size - 1)]
    }

    /**
     * Draws a single beveled, shaded 3D-ish block cell inside a given rectangle.
     */
    fun drawBlockCell(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Float,
        colorIndex: Int,
        specialType: SpecialBlockType = SpecialBlockType.NONE,
        skin: BlockSkinId = BlockSkinId.NEON_GLOW,
        isGhost: Boolean = false,
        alpha: Float = 1f,
        isOverdrive: Boolean = false
    ) {
        val baseColor = getBlockBaseColor(colorIndex)
        val inset = size * 0.05f
        val blockSize = size - (inset * 2f)
        val cellTopLeft = Offset(topLeft.x + inset, topLeft.y + inset)
        val cornerRadius = CornerRadius(size * 0.18f, size * 0.18f)

        if (isGhost) {
            // Ghost placement preview
            drawScope.drawRoundRect(
                color = baseColor.copy(alpha = 0.35f * alpha),
                topLeft = cellTopLeft,
                size = Size(blockSize, blockSize),
                cornerRadius = cornerRadius
            )
            drawScope.drawRoundRect(
                color = baseColor.copy(alpha = 0.8f * alpha),
                topLeft = cellTopLeft,
                size = Size(blockSize, blockSize),
                cornerRadius = cornerRadius,
                style = Stroke(width = size * 0.08f)
            )
            return
        }

        // Overdrive enhanced color
        val renderColor = if (isOverdrive) Color(0xFFFFF700) else baseColor

        // 1. Dark bottom-right shadow bevel
        val shadowColor = renderColor.copy(
            red = (renderColor.red * 0.35f).coerceIn(0f, 1f),
            green = (renderColor.green * 0.35f).coerceIn(0f, 1f),
            blue = (renderColor.blue * 0.35f).coerceIn(0f, 1f),
            alpha = alpha
        )
        drawScope.drawRoundRect(
            color = shadowColor,
            topLeft = Offset(cellTopLeft.x, cellTopLeft.y + size * 0.06f),
            size = Size(blockSize, blockSize),
            cornerRadius = cornerRadius
        )

        // 2. Main body gradient
        val lighterColor = renderColor.copy(
            red = (renderColor.red * 1.35f).coerceIn(0f, 1f),
            green = (renderColor.green * 1.35f).coerceIn(0f, 1f),
            blue = (renderColor.blue * 1.35f).coerceIn(0f, 1f),
            alpha = alpha
        )
        val darkerColor = renderColor.copy(
            red = (renderColor.red * 0.75f).coerceIn(0f, 1f),
            green = (renderColor.green * 0.75f).coerceIn(0f, 1f),
            blue = (renderColor.blue * 0.75f).coerceIn(0f, 1f),
            alpha = alpha
        )

        val gradientBrush = Brush.linearGradient(
            colors = listOf(lighterColor, darkerColor),
            start = cellTopLeft,
            end = Offset(cellTopLeft.x + blockSize, cellTopLeft.y + blockSize)
        )

        drawScope.drawRoundRect(
            brush = gradientBrush,
            topLeft = cellTopLeft,
            size = Size(blockSize, blockSize),
            cornerRadius = cornerRadius
        )

        // 3. Top-Left inner specular shine (glossy glass/jewel effect)
        val highlightPath = Path().apply {
            moveTo(cellTopLeft.x + blockSize * 0.15f, cellTopLeft.y + blockSize * 0.15f)
            lineTo(cellTopLeft.x + blockSize * 0.85f, cellTopLeft.y + blockSize * 0.15f)
            lineTo(cellTopLeft.x + blockSize * 0.15f, cellTopLeft.y + blockSize * 0.85f)
            close()
        }
        drawScope.drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = 0.22f * alpha)
        )

        // 4. Subtle inner border highlight
        drawScope.drawRoundRect(
            color = Color.White.copy(alpha = 0.45f * alpha),
            topLeft = cellTopLeft,
            size = Size(blockSize, blockSize),
            cornerRadius = cornerRadius,
            style = Stroke(width = size * 0.04f)
        )

        // 5. Special block iconography
        if (specialType != SpecialBlockType.NONE) {
            drawSpecialIcon(drawScope, cellTopLeft, blockSize, specialType, alpha)
        }
    }

    private fun drawSpecialIcon(
        drawScope: DrawScope,
        topLeft: Offset,
        size: Float,
        specialType: SpecialBlockType,
        alpha: Float
    ) {
        val center = Offset(topLeft.x + size / 2f, topLeft.y + size / 2f)
        val iconRadius = size * 0.28f

        when (specialType) {
            SpecialBlockType.BOMB -> {
                // Bomb core + spark
                drawScope.drawCircle(
                    color = Color.Black.copy(alpha = 0.85f * alpha),
                    radius = iconRadius,
                    center = center
                )
                drawScope.drawCircle(
                    color = Color(0xFFFF3333).copy(alpha = alpha),
                    radius = iconRadius * 0.6f,
                    center = center
                )
                // Spark top
                drawScope.drawLine(
                    color = Color(0xFFFFD700).copy(alpha = alpha),
                    start = Offset(center.x + iconRadius * 0.5f, center.y - iconRadius * 0.5f),
                    end = Offset(center.x + iconRadius * 1.1f, center.y - iconRadius * 1.1f),
                    strokeWidth = size * 0.08f
                )
            }
            SpecialBlockType.LASER_ROW -> {
                // Horizontal beam indicator
                drawScope.drawLine(
                    color = Color.White.copy(alpha = 0.95f * alpha),
                    start = Offset(center.x - iconRadius * 1.1f, center.y),
                    end = Offset(center.x + iconRadius * 1.1f, center.y),
                    strokeWidth = size * 0.12f
                )
            }
            SpecialBlockType.LASER_COL -> {
                // Vertical beam indicator
                drawScope.drawLine(
                    color = Color.White.copy(alpha = 0.95f * alpha),
                    start = Offset(center.x, center.y - iconRadius * 1.1f),
                    end = Offset(center.x, center.y + iconRadius * 1.1f),
                    strokeWidth = size * 0.12f
                )
            }
            SpecialBlockType.CROSS -> {
                // Cross beams
                drawScope.drawLine(
                    color = Color(0xFF00FFFF).copy(alpha = alpha),
                    start = Offset(center.x - iconRadius, center.y),
                    end = Offset(center.x + iconRadius, center.y),
                    strokeWidth = size * 0.1f
                )
                drawScope.drawLine(
                    color = Color(0xFF00FFFF).copy(alpha = alpha),
                    start = Offset(center.x, center.y - iconRadius),
                    end = Offset(center.x, center.y + iconRadius),
                    strokeWidth = size * 0.1f
                )
            }
            SpecialBlockType.RAINBOW -> {
                // Concentric rainbow rings
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.9f * alpha),
                    radius = iconRadius,
                    center = center,
                    style = Stroke(width = size * 0.08f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = alpha),
                    radius = iconRadius * 0.5f,
                    center = center
                )
            }
            SpecialBlockType.MULTIPLIER -> {
                // 2x diamond badge
                drawScope.drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = alpha),
                    radius = iconRadius * 0.8f,
                    center = center
                )
            }
            SpecialBlockType.TIME -> {
                // Hourglass / clock circle
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.95f * alpha),
                    radius = iconRadius,
                    center = center,
                    style = Stroke(width = size * 0.08f)
                )
                drawScope.drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = center,
                    end = Offset(center.x, center.y - iconRadius * 0.7f),
                    strokeWidth = size * 0.06f
                )
                drawScope.drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = center,
                    end = Offset(center.x + iconRadius * 0.6f, center.y),
                    strokeWidth = size * 0.06f
                )
            }
            else -> {}
        }
    }
}
