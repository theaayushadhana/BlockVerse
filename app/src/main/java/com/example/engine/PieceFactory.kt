package com.example.engine

import com.example.model.GamePiece
import com.example.model.SpecialBlockType
import java.util.UUID
import kotlin.random.Random

/**
 * Factory for creating balanced, exciting block shapes with fair distribution
 * and progressive special block injection.
 */
object PieceFactory {

    // Palette of vibrant gem colors (Indices 0 to 7)
    // 0: Cyan, 1: Violet, 2: Amber, 3: Emerald, 4: Ruby, 5: Magenta, 6: Electric Blue, 7: Gold

    private val SHAPES = listOf(
        // 1x1 single
        arrayOf(intArrayOf(1)),
        // 1x2 and 2x1
        arrayOf(intArrayOf(1, 1)),
        arrayOf(intArrayOf(1), intArrayOf(1)),
        // 1x3 and 3x1
        arrayOf(intArrayOf(1, 1, 1)),
        arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1)),
        // 1x4 and 4x1
        arrayOf(intArrayOf(1, 1, 1, 1)),
        arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
        // 1x5 and 5x1
        arrayOf(intArrayOf(1, 1, 1, 1, 1)),
        arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)),
        // 2x2 square
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        ),
        // 3x3 square
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 1, 1),
            intArrayOf(1, 1, 1)
        ),
        // L-shapes
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        ),
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        ),
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0)
        ),
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1)
        ),
        // T-shapes
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        ),
        arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1)
        ),
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        ),
        // Corner 2x2
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 0)
        ),
        arrayOf(
            intArrayOf(1, 1),
            intArrayOf(0, 1)
        ),
        arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1)
        ),
        arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 1)
        ),
        // Corner 3x3
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0)
        ),
        arrayOf(
            intArrayOf(1, 1, 1),
            intArrayOf(0, 0, 1),
            intArrayOf(0, 0, 1)
        ),
        // Plus / Cross
        arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 1),
            intArrayOf(0, 1, 0)
        ),
        // S and Z shapes
        arrayOf(
            intArrayOf(0, 1, 1),
            intArrayOf(1, 1, 0)
        ),
        arrayOf(
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 1)
        )
    )

    /**
     * Generates a single balanced game piece.
     */
    fun createRandomPiece(allowSpecial: Boolean = true, forcedSpecial: SpecialBlockType? = null): GamePiece {
        val shape = SHAPES[Random.nextInt(SHAPES.size)]
        val colorIndex = Random.nextInt(8)

        val specialType = when {
            forcedSpecial != null -> forcedSpecial
            allowSpecial && Random.nextFloat() < 0.12f -> {
                // Progressive special block roll
                when (Random.nextInt(7)) {
                    0 -> SpecialBlockType.BOMB
                    1 -> SpecialBlockType.LASER_ROW
                    2 -> SpecialBlockType.LASER_COL
                    3 -> SpecialBlockType.CROSS
                    4 -> SpecialBlockType.RAINBOW
                    5 -> SpecialBlockType.MULTIPLIER
                    else -> SpecialBlockType.TIME
                }
            }
            else -> SpecialBlockType.NONE
        }

        return GamePiece(
            id = UUID.randomUUID().toString(),
            shape = shape,
            colorIndex = colorIndex,
            specialType = specialType
        )
    }

    /**
     * Generates a batch of 3 pieces with guaranteed fairness:
     * - At least one small or medium piece (1x1, 1x2, 2x2, or 1x3)
     * - Never 3 giant 3x3 squares at once.
     */
    fun generatePieceTrio(allowSpecial: Boolean = true): List<GamePiece> {
        val pieces = mutableListOf<GamePiece>()

        // First piece: always friendly small/medium
        val smallShapes = SHAPES.filter { matrix ->
            val count = matrix.sumOf { row -> row.count { it == 1 } }
            count in 1..4
        }
        val firstShape = smallShapes[Random.nextInt(smallShapes.size)]
        pieces.add(
            GamePiece(
                id = UUID.randomUUID().toString(),
                shape = firstShape,
                colorIndex = Random.nextInt(8),
                specialType = if (allowSpecial && Random.nextFloat() < 0.15f) SpecialBlockType.BOMB else SpecialBlockType.NONE
            )
        )

        // Second piece
        pieces.add(createRandomPiece(allowSpecial))

        // Third piece
        pieces.add(createRandomPiece(allowSpecial))

        return pieces
    }
}
