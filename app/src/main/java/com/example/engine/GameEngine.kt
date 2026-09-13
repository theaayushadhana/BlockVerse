package com.example.engine

import com.example.model.BoardCell
import com.example.model.BoardEvent
import com.example.model.GameMode
import com.example.model.GamePiece
import com.example.model.SpecialBlockType
import kotlin.random.Random

data class PlacementResult(
    val linesCleared: Int,
    val pointsAwarded: Int,
    val clearedRows: List<Int>,
    val clearedCols: List<Int>,
    val clearedCells: List<Pair<Int, Int>>,
    val activatedSpecials: List<SpecialBlockType>,
    val isPerfectClear: Boolean,
    val isGameOver: Boolean,
    val combo: Int
)

/**
 * High-performance, fully isolated Game Engine for 10x10 block placement.
 */
class GameEngine(
    val mode: GameMode,
    private val initialHighScore: Int = 0
) {
    val BOARD_SIZE = 10

    var grid: Array<Array<BoardCell>> = Array(BOARD_SIZE) {
        Array(BOARD_SIZE) { BoardCell() }
    }
        private set

    var availablePieces: MutableList<GamePiece?> = mutableListOf(null, null, null)
        private set

    var score: Int = 0
        private set

    var highScore: Int = initialHighScore
        private set

    var combo: Int = 0
        private set

    var highestCombo: Int = 0
        private set

    var linesClearedTotal: Int = 0
        private set

    var blocksPlacedTotal: Int = 0
        private set

    var isGameOver: Boolean = false
        private set

    var overdriveMeter: Float = 0f // 0f to 100f
        private set

    var isOverdriveActive: Boolean = false
        private set

    var overdriveMovesRemaining: Int = 0
        private set

    var activeBoardEvent: BoardEvent? = null
        private set

    var eventMovesRemaining: Int = 0
        private set

    var timeAttackSeconds: Int = 60
        private set

    var duelAiScore: Int = 0
        private set

    var movesCount: Int = 0
        private set

    init {
        resetGame()
    }

    fun resetGame() {
        grid = Array(BOARD_SIZE) { Array(BOARD_SIZE) { BoardCell() } }
        score = 0
        combo = 0
        highestCombo = 0
        linesClearedTotal = 0
        blocksPlacedTotal = 0
        isGameOver = false
        overdriveMeter = 0f
        isOverdriveActive = false
        overdriveMovesRemaining = 0
        activeBoardEvent = null
        eventMovesRemaining = 0
        timeAttackSeconds = 60
        duelAiScore = 0
        movesCount = 0

        replenishPieces()
    }

    private fun replenishPieces() {
        val trio = PieceFactory.generatePieceTrio(allowSpecial = mode != GameMode.ZEN)
        availablePieces[0] = trio[0]
        availablePieces[1] = trio[1]
        availablePieces[2] = trio[2]
        checkGameOverCondition()
    }

    fun canPlacePiece(piece: GamePiece, startR: Int, startC: Int): Boolean {
        if (startR < 0 || startC < 0) return false
        if (startR + piece.rows > BOARD_SIZE || startC + piece.cols > BOARD_SIZE) return false

        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shape[r][c] == 1) {
                    val boardR = startR + r
                    val boardC = startC + c
                    if (grid[boardR][boardC].filled) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Finds rows and columns that have 9 out of 10 cells filled (near-miss feedback).
     */
    fun calculateNearMisses(): Pair<Set<Int>, Set<Int>> {
        val rows = mutableSetOf<Int>()
        val cols = mutableSetOf<Int>()
        for (r in 0 until BOARD_SIZE) {
            val count = (0 until BOARD_SIZE).count { c -> grid[r][c].filled }
            if (count == 9) rows.add(r)
        }
        for (c in 0 until BOARD_SIZE) {
            val count = (0 until BOARD_SIZE).count { r -> grid[r][c].filled }
            if (count == 9) cols.add(c)
        }
        return Pair(rows, cols)
    }

    /**
     * Smart hint generator: finds an optimal position for any of the available pieces.
     */
    fun findSmartHint(): Triple<Int, Int, Int>? {
        for (pieceIndex in availablePieces.indices) {
            val piece = availablePieces[pieceIndex] ?: continue
            for (r in 0..(BOARD_SIZE - piece.rows)) {
                for (c in 0..(BOARD_SIZE - piece.cols)) {
                    if (canPlacePiece(piece, r, c)) {
                        // Check if this placement clears at least one line
                        var clearsLine = false
                        // Check rows
                        for (pr in 0 until piece.rows) {
                            val br = r + pr
                            val willFill = (0 until BOARD_SIZE).count { bc ->
                                grid[br][bc].filled || (bc >= c && bc < c + piece.cols && piece.shape[pr][bc - c] == 1)
                            }
                            if (willFill == BOARD_SIZE) {
                                clearsLine = true
                                break
                            }
                        }
                        if (clearsLine) {
                            return Triple(pieceIndex, r, c)
                        }
                    }
                }
            }
        }

        // Fallback to any valid placement
        for (pieceIndex in availablePieces.indices) {
            val piece = availablePieces[pieceIndex] ?: continue
            for (r in 0..(BOARD_SIZE - piece.rows)) {
                for (c in 0..(BOARD_SIZE - piece.cols)) {
                    if (canPlacePiece(piece, r, c)) {
                        return Triple(pieceIndex, r, c)
                    }
                }
            }
        }
        return null
    }

    fun activateOverdrive(): Boolean {
        if (overdriveMeter >= 100f && !isOverdriveActive) {
            isOverdriveActive = true
            overdriveMovesRemaining = 8
            overdriveMeter = 0f
            return true
        }
        return false
    }

    fun placePiece(pieceIndex: Int, startR: Int, startC: Int): PlacementResult? {
        val piece = availablePieces.getOrNull(pieceIndex) ?: return null
        if (!canPlacePiece(piece, startR, startC)) return null

        movesCount++
        blocksPlacedTotal += piece.totalBlocks

        // 1. Place the piece onto the board
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shape[r][c] == 1) {
                    val br = startR + r
                    val bc = startC + c
                    grid[br][bc] = BoardCell(
                        filled = true,
                        colorIndex = piece.colorIndex,
                        specialType = piece.specialType
                    )
                }
            }
        }

        // Remove piece from rack
        availablePieces[pieceIndex] = null

        // 2. Detect full horizontal and vertical lines
        val fullRows = mutableListOf<Int>()
        for (r in 0 until BOARD_SIZE) {
            if ((0 until BOARD_SIZE).all { c -> grid[r][c].filled }) {
                fullRows.add(r)
            }
        }

        val fullCols = mutableListOf<Int>()
        for (c in 0 until BOARD_SIZE) {
            if ((0 until BOARD_SIZE).all { r -> grid[r][c].filled }) {
                fullCols.add(c)
            }
        }

        val clearedCellsSet = mutableSetOf<Pair<Int, Int>>()
        val activatedSpecials = mutableListOf<SpecialBlockType>()

        // If piece is a Bomb, trigger immediate 3x3 blast centered on piece
        if (piece.specialType == SpecialBlockType.BOMB) {
            activatedSpecials.add(SpecialBlockType.BOMB)
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val nr = startR + dr
                    val nc = startC + dc
                    if (nr in 0 until BOARD_SIZE && nc in 0 until BOARD_SIZE) {
                        clearedCellsSet.add(Pair(nr, nc))
                    }
                }
            }
        }

        // Collect cells from rows and columns
        for (r in fullRows) {
            for (c in 0 until BOARD_SIZE) {
                clearedCellsSet.add(Pair(r, c))
            }
        }
        for (c in fullCols) {
            for (r in 0 until BOARD_SIZE) {
                clearedCellsSet.add(Pair(r, c))
            }
        }

        // Check for special block activations among cleared cells
        val pendingSpecials = clearedCellsSet.mapNotNull { (r, c) ->
            val special = grid[r][c].specialType
            if (special != SpecialBlockType.NONE) Pair(Pair(r, c), special) else null
        }

        for ((pos, special) in pendingSpecials) {
            activatedSpecials.add(special)
            when (special) {
                SpecialBlockType.BOMB -> {
                    // 3x3 blast
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = pos.first + dr
                            val nc = pos.second + dc
                            if (nr in 0 until BOARD_SIZE && nc in 0 until BOARD_SIZE) {
                                clearedCellsSet.add(Pair(nr, nc))
                            }
                        }
                    }
                }
                SpecialBlockType.LASER_ROW -> {
                    for (c in 0 until BOARD_SIZE) clearedCellsSet.add(Pair(pos.first, c))
                }
                SpecialBlockType.LASER_COL -> {
                    for (r in 0 until BOARD_SIZE) clearedCellsSet.add(Pair(r, pos.second))
                }
                SpecialBlockType.CROSS -> {
                    for (c in 0 until BOARD_SIZE) clearedCellsSet.add(Pair(pos.first, c))
                    for (r in 0 until BOARD_SIZE) clearedCellsSet.add(Pair(r, pos.second))
                }
                SpecialBlockType.TIME -> {
                    timeAttackSeconds += 15
                }
                else -> {}
            }
        }

        val linesCleared = fullRows.size + fullCols.size
        var pointsGained = piece.totalBlocks * 10

        if (linesCleared > 0 || clearedCellsSet.isNotEmpty()) {
            if (linesCleared > 0) {
                combo++
                highestCombo = maxOf(highestCombo, combo)
                linesClearedTotal += linesCleared
            }

            // Multi-line clear exponential formula
            val linePoints = when (linesCleared) {
                1 -> 100
                2 -> 300
                3 -> 600
                4 -> 1000
                5 -> 1500
                else -> linesCleared * 350
            }

            // Combo multiplier
            var multiplier = combo.toFloat()
            if (isOverdriveActive) multiplier *= 2.0f
            if (activeBoardEvent == BoardEvent.DOUBLE_SCORE) multiplier *= 2.0f
            if (activeBoardEvent == BoardEvent.NEON_SURGE) multiplier *= 1.5f
            if (activatedSpecials.contains(SpecialBlockType.MULTIPLIER)) multiplier *= 2.0f

            pointsGained += (linePoints * multiplier).toInt()

            // Overdrive charging
            val meterGain = (linesCleared * 18f) + (combo * 5f)
            overdriveMeter = (overdriveMeter + (if (activeBoardEvent == BoardEvent.BLOCK_FRENZY) meterGain * 2f else meterGain)).coerceIn(0f, 100f)

            // Time attack bonus seconds
            if (mode == GameMode.TIME_ATTACK) {
                timeAttackSeconds += linesCleared * 3
            }

            // Clear the cells
            for ((r, c) in clearedCellsSet) {
                grid[r][c] = BoardCell(filled = false)
            }
        } else {
            // Unsuccessful clear: reset combo (except in Relax/Zen where it's forgiven)
            if (mode != GameMode.RELAX && mode != GameMode.ZEN) {
                combo = 0
            }
        }

        // Perfect Clear Check
        var isPerfectClear = false
        if (linesCleared > 0) {
            val allEmpty = (0 until BOARD_SIZE).all { r ->
                (0 until BOARD_SIZE).all { c -> !grid[r][c].filled }
            }
            if (allEmpty) {
                isPerfectClear = true
                pointsGained += 5000
                overdriveMeter = 100f
            }
        }

        score += pointsGained
        if (score > highScore) {
            highScore = score
        }

        // Update Overdrive duration
        if (isOverdriveActive) {
            overdriveMovesRemaining--
            if (overdriveMovesRemaining <= 0) {
                isOverdriveActive = false
            }
        }

        // Update Board Event duration
        if (activeBoardEvent != null) {
            eventMovesRemaining--
            if (eventMovesRemaining <= 0) {
                activeBoardEvent = null
            }
        } else if (movesCount % 12 == 0 && Random.nextFloat() < 0.45f) {
            // Trigger new dynamic board event
            val events = BoardEvent.values()
            val chosen = events[Random.nextInt(events.size)]
            activeBoardEvent = chosen
            eventMovesRemaining = chosen.durationMoves
        }

        // Update Duel AI score
        if (mode == GameMode.DUEL) {
            val aiPoints = (40..160).random() + (if (Random.nextFloat() < 0.35f) 300 else 0)
            duelAiScore += aiPoints
        }

        // Replenish pieces if all 3 used
        if (availablePieces.all { it == null }) {
            replenishPieces()
        } else {
            checkGameOverCondition()
        }

        return PlacementResult(
            linesCleared = linesCleared,
            pointsAwarded = pointsGained,
            clearedRows = fullRows,
            clearedCols = fullCols,
            clearedCells = clearedCellsSet.toList(),
            activatedSpecials = activatedSpecials,
            isPerfectClear = isPerfectClear,
            isGameOver = isGameOver,
            combo = combo
        )
    }

    fun decrementTimeAttack(): Boolean {
        if (mode == GameMode.TIME_ATTACK && !isGameOver) {
            timeAttackSeconds--
            if (timeAttackSeconds <= 0) {
                timeAttackSeconds = 0
                isGameOver = true
                return true
            }
        }
        return false
    }

    private fun checkGameOverCondition() {
        // Can ANY remaining piece be placed ANYWHERE on the board?
        var hasValidMove = false
        for (piece in availablePieces) {
            if (piece == null) continue
            for (r in 0..(BOARD_SIZE - piece.rows)) {
                for (c in 0..(BOARD_SIZE - piece.cols)) {
                    if (canPlacePiece(piece, r, c)) {
                        hasValidMove = true
                        break
                    }
                }
                if (hasValidMove) break
            }
            if (hasValidMove) break
        }

        if (!hasValidMove && availablePieces.any { it != null }) {
            isGameOver = true
        }
    }
}
