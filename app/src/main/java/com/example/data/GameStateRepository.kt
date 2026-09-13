package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Achievement
import com.example.model.BlockSkinId
import com.example.model.BoardThemeId
import com.example.model.ChallengeLevel
import com.example.model.GameMode
import com.example.model.GameSettings
import com.example.model.GameStats
import com.example.model.PlayerProfile

/**
 * Robust local persistence repository for all player progression,
 * settings, unlocks, achievements, and statistics.
 */
class GameStateRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("blockverse_game_data", Context.MODE_PRIVATE)

    // --- Profile & Progression ---

    fun getPlayerProfile(): PlayerProfile {
        val level = prefs.getInt("profile_level", 1)
        val xp = prefs.getInt("profile_xp", 0)
        val coins = prefs.getInt("profile_coins", 350)
        val streak = prefs.getInt("profile_streak", 1)
        val title = prefs.getString("profile_title", "BLOCK ROOKIE") ?: "BLOCK ROOKIE"
        val avatarId = prefs.getString("profile_avatar", "cyber_cube") ?: "cyber_cube"
        val skinName = prefs.getString("profile_skin", BlockSkinId.NEON_GLOW.name) ?: BlockSkinId.NEON_GLOW.name
        val themeName = prefs.getString("profile_theme", BoardThemeId.CYBER_NEON.name) ?: BoardThemeId.CYBER_NEON.name

        val skin = try { BlockSkinId.valueOf(skinName) } catch (_: Exception) { BlockSkinId.NEON_GLOW }
        val theme = try { BoardThemeId.valueOf(themeName) } catch (_: Exception) { BoardThemeId.CYBER_NEON }

        val xpRequired = level * 1000
        return PlayerProfile(
            level = level,
            xp = xp,
            xpToNextLevel = xpRequired,
            coins = coins,
            streak = streak,
            title = title,
            avatarId = avatarId,
            activeSkin = skin,
            activeTheme = theme
        )
    }

    fun savePlayerProfile(profile: PlayerProfile) {
        prefs.edit()
            .putInt("profile_level", profile.level)
            .putInt("profile_xp", profile.xp)
            .putInt("profile_coins", profile.coins)
            .putInt("profile_streak", profile.streak)
            .putString("profile_title", profile.title)
            .putString("profile_avatar", profile.avatarId)
            .putString("profile_skin", profile.activeSkin.name)
            .putString("profile_theme", profile.activeTheme.name)
            .apply()
    }

    fun addXpAndCoins(xpGained: Int, coinsGained: Int): Pair<PlayerProfile, Boolean> {
        var profile = getPlayerProfile()
        var newXp = profile.xp + xpGained
        var newLevel = profile.level
        var newCoins = profile.coins + coinsGained
        var leveledUp = false

        while (newXp >= newLevel * 1000) {
            newXp -= newLevel * 1000
            newLevel++
            newCoins += 150 // Level-up bonus coins
            leveledUp = true
        }

        val updated = profile.copy(
            level = newLevel,
            xp = newXp,
            xpToNextLevel = newLevel * 1000,
            coins = newCoins
        )
        savePlayerProfile(updated)
        return Pair(updated, leveledUp)
    }

    // --- High Scores ---

    fun getHighScore(mode: GameMode): Int {
        return prefs.getInt("high_score_${mode.name}", 0)
    }

    fun saveHighScore(mode: GameMode, score: Int): Boolean {
        val current = getHighScore(mode)
        if (score > current) {
            prefs.edit().putInt("high_score_${mode.name}", score).apply()
            return true
        }
        return false
    }

    // --- Settings ---

    fun getSettings(): GameSettings {
        return GameSettings(
            soundEnabled = prefs.getBoolean("settings_sound", true),
            musicEnabled = prefs.getBoolean("settings_music", true),
            hapticsEnabled = prefs.getBoolean("settings_haptics", true),
            screenShakeEnabled = prefs.getBoolean("settings_shake", true),
            smartHintsEnabled = prefs.getBoolean("settings_hints", true),
            colorBlindMode = prefs.getBoolean("settings_colorblind", false),
            leftHandedMode = prefs.getBoolean("settings_left_handed", false),
            reducedMotion = prefs.getBoolean("settings_reduced_motion", false)
        )
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit()
            .putBoolean("settings_sound", settings.soundEnabled)
            .putBoolean("settings_music", settings.musicEnabled)
            .putBoolean("settings_haptics", settings.hapticsEnabled)
            .putBoolean("settings_shake", settings.screenShakeEnabled)
            .putBoolean("settings_hints", settings.smartHintsEnabled)
            .putBoolean("settings_colorblind", settings.colorBlindMode)
            .putBoolean("settings_left_handed", settings.leftHandedMode)
            .putBoolean("settings_reduced_motion", settings.reducedMotion)
            .apply()
    }

    // --- Statistics ---

    fun getStats(): GameStats {
        return GameStats(
            gamesPlayed = prefs.getInt("stats_games_played", 0),
            linesCleared = prefs.getInt("stats_lines_cleared", 0),
            bestScore = getHighScore(GameMode.CLASSIC),
            highestCombo = prefs.getInt("stats_highest_combo", 0),
            perfectClears = prefs.getInt("stats_perfect_clears", 0),
            totalBlocksPlaced = prefs.getInt("stats_blocks_placed", 0),
            totalTimePlayedSeconds = prefs.getLong("stats_time_played", 0L)
        )
    }

    fun updateStats(lines: Int, blocks: Int, combo: Int, isPerfectClear: Boolean, durationSec: Long) {
        val current = getStats()
        prefs.edit()
            .putInt("stats_games_played", current.gamesPlayed + 1)
            .putInt("stats_lines_cleared", current.linesCleared + lines)
            .putInt("stats_highest_combo", maxOf(current.highestCombo, combo))
            .putInt("stats_perfect_clears", current.perfectClears + if (isPerfectClear) 1 else 0)
            .putInt("stats_blocks_placed", current.totalBlocksPlaced + blocks)
            .putLong("stats_time_played", current.totalTimePlayedSeconds + durationSec)
            .apply()
    }

    // --- Challenges (Levels 1 to 30) ---

    fun getChallengeLevels(): List<ChallengeLevel> {
        val list = mutableListOf<ChallengeLevel>()
        for (i in 1..30) {
            val isFirst = i == 1
            val isUnlocked = prefs.getBoolean("challenge_unlocked_$i", isFirst)
            val stars = prefs.getInt("challenge_stars_$i", 0)

            val linesReq = 5 + (i * 2)
            val scoreReq = 800 * i
            val comboReq = 1 + (i / 4)

            val desc = when {
                i % 5 == 0 -> "Clear $linesReq lines with at least $comboReq combos!"
                i % 3 == 0 -> "Reach $scoreReq points in this puzzle!"
                else -> "Clear $linesReq lines to win!"
            }

            list.add(
                ChallengeLevel(
                    levelNumber = i,
                    title = "Sector #$i",
                    targetDescription = desc,
                    targetLines = linesReq,
                    targetScore = scoreReq,
                    targetCombos = comboReq,
                    maxMoves = 20 + i,
                    isUnlocked = isUnlocked,
                    stars = stars
                )
            )
        }
        return list
    }

    fun saveChallengeStars(levelNumber: Int, stars: Int) {
        val currentStars = prefs.getInt("challenge_stars_$levelNumber", 0)
        if (stars > currentStars) {
            prefs.edit().putInt("challenge_stars_$levelNumber", stars).apply()
        }
        // Unlock next level if completed with at least 1 star
        if (stars > 0 && levelNumber < 30) {
            prefs.edit().putBoolean("challenge_unlocked_${levelNumber + 1}", true).apply()
        }
    }

    // --- Achievements ---

    fun getAchievements(): List<Achievement> {
        val stats = getStats()
        return listOf(
            Achievement("first_clear", "First Spark", "Clear your first line", "stars", minOf(stats.linesCleared, 1), 1, stats.linesCleared >= 1, 50),
            Achievement("combo_master", "Combo Master", "Reach a 5x Combo streak", "bolt", minOf(stats.highestCombo, 5), 5, stats.highestCombo >= 5, 100),
            Achievement("line_machine", "Line Machine", "Clear 100 total lines", "grid", minOf(stats.linesCleared, 100), 100, stats.linesCleared >= 100, 200),
            Achievement("perfect_clear", "Grid Master", "Perform a Perfect Clear", "diamond", minOf(stats.perfectClears, 1), 1, stats.perfectClears >= 1, 500),
            Achievement("block_legend", "Block Legend", "Score over 10,000 points", "trophy", minOf(stats.bestScore, 10000), 10000, stats.bestScore >= 10000, 300),
            Achievement("speed_demon", "Speed Demon", "Play 10 games of Time Attack", "timer", minOf(prefs.getInt("time_attack_games", 0), 10), 10, prefs.getInt("time_attack_games", 0) >= 10, 150),
            Achievement("builder_god", "Massive Architect", "Place 500 blocks onto the grid", "box", minOf(stats.totalBlocksPlaced, 500), 500, stats.totalBlocksPlaced >= 500, 250),
            Achievement("chain_reaction", "Chain Reaction", "Activate 10 special blocks", "nuclear", minOf(prefs.getInt("special_blocks_used", 0), 10), 10, prefs.getInt("special_blocks_used", 0) >= 10, 200),
            Achievement("cosmetic_stylist", "Cosmetic Stylist", "Unlock and equip a custom theme", "palette", 1, 1, prefs.getString("profile_theme", BoardThemeId.CYBER_NEON.name) != BoardThemeId.CYBER_NEON.name, 100),
            Achievement("streak_champion", "Daily Champion", "Achieve a 7-day play streak", "fire", minOf(getPlayerProfile().streak, 7), 7, getPlayerProfile().streak >= 7, 350)
        )
    }

    fun recordSpecialBlockUse() {
        val current = prefs.getInt("special_blocks_used", 0)
        prefs.edit().putInt("special_blocks_used", current + 1).apply()
    }

    fun recordTimeAttackGame() {
        val current = prefs.getInt("time_attack_games", 0)
        prefs.edit().putInt("time_attack_games", current + 1).apply()
    }

    // --- Daily Rewards ---

    fun getDailyRewardStreakDay(): Int {
        return prefs.getInt("daily_reward_streak_day", 1)
    }

    fun isDailyRewardClaimedToday(): Boolean {
        val lastClaimTime = prefs.getLong("daily_reward_last_claim_ts", 0L)
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        return (now - lastClaimTime) < oneDayMs
    }

    fun claimDailyReward(): Int {
        val day = getDailyRewardStreakDay()
        val rewardCoins = when (day) {
            1 -> 100
            2 -> 200
            3 -> 300
            4 -> 400
            5 -> 500
            6 -> 750
            else -> 1200
        }
        val nextDay = if (day >= 7) 1 else day + 1
        prefs.edit()
            .putLong("daily_reward_last_claim_ts", System.currentTimeMillis())
            .putInt("daily_reward_streak_day", nextDay)
            .apply()

        addXpAndCoins(rewardCoins / 2, rewardCoins)
        return rewardCoins
    }

    fun isFirstLaunch(): Boolean {
        val isFirst = prefs.getBoolean("is_first_launch", true)
        if (isFirst) {
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirst
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}
