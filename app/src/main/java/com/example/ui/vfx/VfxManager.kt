package com.example.ui.vfx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.model.FloatingText
import com.example.model.GameParticle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance VFX and Juice manager.
 * Handles particles, screen shake, floating score texts, and celebratory confetti.
 */
class VfxManager {

    val particles = mutableListOf<GameParticle>()
    val floatingTexts = mutableListOf<FloatingText>()

    var shakeOffsetX: Float = 0f
        private set
    var shakeOffsetY: Float = 0f
        private set
    private var shakeTrauma: Float = 0f

    var flashAlpha: Float = 0f
        private set
    var flashColor: Color = Color.White
        private set

    fun triggerShake(intensity: Float) {
        shakeTrauma = (shakeTrauma + intensity).coerceIn(0f, 1f)
    }

    fun triggerFlash(color: Color = Color.White, alpha: Float = 0.45f) {
        flashColor = color
        flashAlpha = alpha
    }

    fun spawnLineClearParticles(clearedCells: List<Pair<Int, Int>>, cellSize: Float, boardOriginX: Float, boardOriginY: Float, color: Color) {
        for ((r, c) in clearedCells) {
            val cx = boardOriginX + (c + 0.5f) * cellSize
            val cy = boardOriginY + (r + 0.5f) * cellSize

            val particleCount = 8
            for (i in 0 until particleCount) {
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 12f + 4f
                val pColor = if (Random.nextBoolean()) color else Color(0xFFFFFFFF)

                particles.add(
                    GameParticle(
                        x = cx,
                        y = cy,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = pColor,
                        size = Random.nextFloat() * 7f + 4f,
                        alpha = 1f,
                        maxLife = 0.5f,
                        currentLife = 0.5f,
                        isConfetti = false,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 15f
                    )
                )
            }
        }
    }

    fun spawnConfetti(boardWidth: Float, boardHeight: Float, count: Int = 120) {
        val colors = listOf(
            Color(0xFFFFD700), Color(0xFF00F0FF), Color(0xFFFF1493),
            Color(0xFF00FF7F), Color(0xFF9D00FF), Color(0xFFFF4500)
        )
        for (i in 0 until count) {
            particles.add(
                GameParticle(
                    x = Random.nextFloat() * boardWidth,
                    y = Random.nextFloat() * (boardHeight * 0.3f),
                    vx = (Random.nextFloat() - 0.5f) * 8f,
                    vy = Random.nextFloat() * 6f + 4f,
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f,
                    alpha = 1f,
                    maxLife = 2.0f,
                    currentLife = 2.0f,
                    isConfetti = true,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 20f
                )
            )
        }
    }

    fun spawnFloatingText(text: String, x: Float, y: Float, color: Color = Color(0xFF00F0FF), scale: Float = 1.2f) {
        floatingTexts.add(
            FloatingText(
                id = System.nanoTime(),
                text = text,
                x = x,
                y = y,
                color = color,
                scale = scale,
                alpha = 1f,
                dy = -3.5f
            )
        )
    }

    fun update(dt: Float = 0.016f) {
        // Update particles
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.rotation += p.vRot

            if (p.isConfetti) {
                p.vy += 0.15f // gentle gravity
            } else {
                p.vx *= 0.94f // air drag
                p.vy *= 0.94f
            }

            p.currentLife -= dt
            p.alpha = (p.currentLife / p.maxLife).coerceIn(0f, 1f)

            if (p.currentLife <= 0f) {
                iterator.remove()
            }
        }

        // Update floating texts
        val textIterator = floatingTexts.iterator()
        while (textIterator.hasNext()) {
            val ft = textIterator.next()
            ft.y += ft.dy
            ft.dy *= 0.92f
            ft.alpha -= dt * 1.5f
            if (ft.alpha <= 0f) {
                textIterator.remove()
            }
        }

        // Screen shake trauma decay
        if (shakeTrauma > 0f) {
            shakeTrauma = (shakeTrauma - dt * 2.5f).coerceAtLeast(0f)
            val maxOffset = shakeTrauma * shakeTrauma * 24f
            shakeOffsetX = (Random.nextFloat() * 2f - 1f) * maxOffset
            shakeOffsetY = (Random.nextFloat() * 2f - 1f) * maxOffset
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }

        // Flash decay
        if (flashAlpha > 0f) {
            flashAlpha = (flashAlpha - dt * 2.5f).coerceAtLeast(0f)
        }
    }

    fun drawParticles(drawScope: DrawScope) {
        for (p in particles) {
            if (p.alpha <= 0f) continue
            val c = p.color.copy(alpha = p.alpha)
            if (p.isConfetti) {
                drawScope.drawCircle(
                    color = c,
                    radius = p.size / 2f,
                    center = Offset(p.x, p.y)
                )
            } else {
                // Spark / diamond shard
                drawScope.drawCircle(
                    color = c,
                    radius = p.size / 2f,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}
