package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Types of special blocks that can appear on the board or in pieces.
 */
enum class SpecialBlockType(val displayName: String, val description: String) {
    NONE("Normal", "Standard block"),
    BOMB("Bomb", "Clears a 3x3 area on line clear"),
    LASER_ROW("Laser H", "Clears entire horizontal row"),
    LASER_COL("Laser V", "Clears entire vertical column"),
    CROSS("Cross Laser", "Clears both row and column"),
    RAINBOW("Rainbow", "Adapts to complete any line"),
    MULTIPLIER("2x Multiplier", "Doubles score of the clear"),
    TIME("Time Boost", "Adds +15 seconds in Time Attack"),
    FREEZE("Freeze", "Temporarily stabilizes board")
}

/**
 * Visual styling theme for the board grid.
 */
enum class BoardThemeId(
    val displayName: String,
    val bgStart: Long,
    val bgEnd: Long,
    val gridBorder: Long,
    val emptyCell: Long,
    val glowColor: Long
) {
    CYBER_NEON("Cyber Neon", 0xFF0B0F19, 0xFF141B2D, 0xFF1E293B, 0xFF111827, 0xFF00F0FF),
    DEEP_SPACE("Deep Space", 0xFF050510, 0xFF0F0B24, 0xFF1A1438, 0xFF0D091F, 0xFF8A2BE2),
    SUNSET_HORIZON("Sunset", 0xFF180A1A, 0xFF2A1020, 0xFF3D1A2E, 0xFF200E1D, 0xFFFF416C),
    DARK_MATTER("Dark Matter", 0xFF080808, 0xFF121212, 0xFF222222, 0xFF161616, 0xFF708090),
    EMERALD_FOREST("Emerald", 0xFF06140D, 0xFF0C2418, 0xFF143825, 0xFF0A1D13, 0xFF00E676)
}

/**
 * Visual block skin style.
 */
enum class BlockSkinId(val displayName: String, val rarity: String) {
    NEON_GLOW("Neon Glow", "COMMON"),
    CYBER_DIAMOND("Cyber Diamond", "RARE"),
    CANDY_JEWEL("Candy Jewel", "EPIC"),
    RETRO_PIXEL("Retro Pixel", "RARE"),
    OBSIDIAN_GOLD("Obsidian Gold", "LEGENDARY")
}

/**
 * Active board events that occasionally trigger during gameplay.
 */
enum class BoardEvent(val title: String, val subtitle: String, val durationMoves: Int) {
    NEON_SURGE("⚡ NEON SURGE", "Score multiplier +50%!", 6),
    COLOR_RUSH("🌈 COLOR RUSH", "Extra rainbow blocks spawning!", 5),
    DOUBLE_SCORE("🔥 DOUBLE SCORE", "All line clears award 2X points!", 4),
    BLOCK_FRENZY("💎 BLOCK FRENZY", "Overdrive charges 2x faster!", 6),
    GOLDEN_GRID("✨ GOLDEN GRID", "Bonus 100 coins for multi-clears!", 5),
    GRAVITY_SHIFT("🌌 GRAVITY SHIFT", "Special blocks trigger automatically!", 4)
}

/**
 * Game modes available in Blockverse.
 */
enum class GameMode(val displayName: String, val description: String) {
    CLASSIC("Classic", "Endless block puzzle. Achieve the highest score!"),
    TIME_ATTACK("Time Attack", "60-second speed challenge. Clear lines to gain bonus time!"),
    RELAX("Relax", "No pressure, peaceful gameplay with gentle chimes."),
    DAILY_PUZZLE("Daily Puzzle", "Unique seeded puzzle for today. Earn up to 3 stars!"),
    CHALLENGE("Challenges", "30 handcrafted levels with specific goals."),
    ZEN("Zen Mode", "Ultra-minimal soothing audio-visual flow."),
    DUEL("Duel (VS AI)", "Real-time score race against an AI challenger!")
}

/**
 * A single cell in a 10x10 board.
 */
data class BoardCell(
    val filled: Boolean = false,
    val colorIndex: Int = 0,
    val specialType: SpecialBlockType = SpecialBlockType.NONE,
    val isClearing: Boolean = false,
    val clearProgress: Float = 0f,
    val isGhost: Boolean = false,
    val isNearMiss: Boolean = false
)

/**
 * A piece composed of a grid of cells.
 */
data class GamePiece(
    val id: String,
    val shape: Array<IntArray>,
    val colorIndex: Int,
    val specialType: SpecialBlockType = SpecialBlockType.NONE
) {
    val rows: Int get() = shape.size
    val cols: Int get() = if (shape.isNotEmpty()) shape[0].size else 0
    val totalBlocks: Int get() = shape.sumOf { row -> row.count { it == 1 } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GamePiece
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Player profile information and progression.
 */
data class PlayerProfile(
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 1000,
    val coins: Int = 250,
    val streak: Int = 1,
    val title: String = "BLOCK ROOKIE",
    val avatarId: String = "avatar_cyber_cube",
    val activeSkin: BlockSkinId = BlockSkinId.NEON_GLOW,
    val activeTheme: BoardThemeId = BoardThemeId.CYBER_NEON
)

/**
 * Challenge level definition.
 */
data class ChallengeLevel(
    val levelNumber: Int,
    val title: String,
    val targetDescription: String,
    val targetLines: Int = 0,
    val targetScore: Int = 0,
    val targetCombos: Int = 0,
    val maxMoves: Int = 0,
    val timeLimitSec: Int = 0,
    val isUnlocked: Boolean = false,
    val stars: Int = 0
)

/**
 * Particle entity for canvas rendering.
 */
data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1f,
    val maxLife: Float = 1f,
    var currentLife: Float = 1f,
    val isConfetti: Boolean = false,
    var rotation: Float = 0f,
    var vRot: Float = 0f
)

/**
 * Floating popup score text.
 */
data class FloatingText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var scale: Float = 1f,
    var alpha: Float = 1f,
    var dy: Float = -2f
)

/**
 * In-game achievement.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val progress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val rewardCoins: Int
)

/**
 * Game settings preferences.
 */
data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val smartHintsEnabled: Boolean = true,
    val colorBlindMode: Boolean = false,
    val leftHandedMode: Boolean = false,
    val reducedMotion: Boolean = false
)

/**
 * Overall game statistics.
 */
data class GameStats(
    val gamesPlayed: Int = 0,
    val linesCleared: Int = 0,
    val bestScore: Int = 0,
    val highestCombo: Int = 0,
    val perfectClears: Int = 0,
    val totalBlocksPlaced: Int = 0,
    val totalTimePlayedSeconds: Long = 0L
)
