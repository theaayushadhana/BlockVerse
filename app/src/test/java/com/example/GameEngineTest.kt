package com.example

import com.example.engine.GameEngine
import com.example.model.GameMode
import com.example.model.GamePiece
import com.example.model.SpecialBlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        engine = GameEngine(mode = GameMode.CLASSIC)
        engine.resetGame()
    }

    @Test
    fun testInitialState() {
        assertEquals(0, engine.score)
        assertEquals(0, engine.combo)
        assertFalse(engine.isGameOver)
        assertEquals(3, engine.availablePieces.size)
        // Check grid is 10x10 and empty
        assertEquals(10, engine.grid.size)
        assertEquals(10, engine.grid[0].size)
        for (r in 0 until 10) {
            for (c in 0 until 10) {
                assertFalse(engine.grid[r][c].filled)
            }
        }
    }

    @Test
    fun testCanPlacePiece() {
        val singleDot = GamePiece(
            id = "test_dot",
            shape = arrayOf(intArrayOf(1)),
            colorIndex = 0
        )

        // Can place inside board
        assertTrue(engine.canPlacePiece(singleDot, 0, 0))
        assertTrue(engine.canPlacePiece(singleDot, 9, 9))

        // Cannot place outside bounds
        assertFalse(engine.canPlacePiece(singleDot, -1, 0))
        assertFalse(engine.canPlacePiece(singleDot, 10, 0))
        assertFalse(engine.canPlacePiece(singleDot, 0, 10))
    }

    @Test
    fun testLineClearAndScoring() {
        // Fill row 0 completely except for column 9
        val dot = GamePiece(id = "dot", shape = arrayOf(intArrayOf(1)), colorIndex = 0)
        for (c in 0 until 9) {
            engine.availablePieces[0] = dot
            engine.placePiece(0, 0, c)
        }
        assertEquals(0, engine.linesClearedTotal)

        // Place the 10th block in row 0
        engine.availablePieces[0] = dot
        val result = engine.placePiece(0, 0, 9)

        // Verify line clear
        assertNotNull(result)
        assertEquals(1, result!!.linesCleared)
        assertEquals(1, engine.linesClearedTotal)
        assertTrue(result.pointsAwarded > 0)
        // Verify row 0 is cleared
        for (c in 0 until 10) {
            assertFalse(engine.grid[0][c].filled)
        }
    }

    @Test
    fun testBombSpecialBlock() {
        val dot = GamePiece(id = "d", shape = arrayOf(intArrayOf(1)), colorIndex = 0)
        engine.availablePieces[0] = dot
        engine.placePiece(0, 4, 4)
        engine.availablePieces[0] = dot
        engine.placePiece(0, 4, 5)

        // Place a bomb block at 5, 5
        val bombPiece = GamePiece(
            id = "bomb",
            shape = arrayOf(intArrayOf(1)),
            colorIndex = 0,
            specialType = SpecialBlockType.BOMB
        )
        engine.availablePieces[0] = bombPiece
        engine.placePiece(0, 5, 5)

        // Surrounding cells should have been blown up
        assertFalse(engine.grid[4][4].filled)
        assertFalse(engine.grid[4][5].filled)
        assertFalse(engine.grid[5][5].filled)
    }

    @Test
    fun testFindSmartHint() {
        val hint = engine.findSmartHint()
        assertNotNull(hint)
        val (pieceIdx, row, col) = hint!!
        assertTrue(pieceIdx in 0..2)
        assertTrue(row in 0..9)
        assertTrue(col in 0..9)
    }

    @Test
    fun testReviveMechanic() {
        val dot = GamePiece(id = "d", shape = arrayOf(intArrayOf(1)), colorIndex = 0)
        // Fill center cells (rows 3..6, cols 3..6)
        for (r in 3..6) {
            for (c in 3..6) {
                engine.availablePieces[0] = dot
                engine.placePiece(0, r, c)
            }
        }
        // Call revive
        val cleared = engine.revive()
        assertTrue(cleared.isNotEmpty())
        assertFalse(engine.isGameOver)
        assertEquals(3, engine.availablePieces.filterNotNull().size)
        // Verify center cells cleared
        for (r in 3..6) {
            for (c in 3..6) {
                assertFalse(engine.grid[r][c].filled)
            }
        }
    }
}
