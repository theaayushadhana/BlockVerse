package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioManager
import com.example.data.GameStateRepository
import com.example.engine.GameEngine
import com.example.haptics.HapticsManager
import com.example.model.Achievement
import com.example.model.BlockSkinId
import com.example.model.BoardCell
import com.example.model.BoardEvent
import com.example.model.BoardThemeId
import com.example.model.ChallengeLevel
import com.example.model.GameMode
import com.example.model.GamePiece
import com.example.model.GameSettings
import com.example.model.GameStats
import com.example.model.PlayerProfile
import com.example.ui.vfx.VfxManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.floor

enum class AppScreen {
    HOME,
    GAME,
    COLLECTION,
    CHALLENGES,
    LEADERBOARD,
    STATS,
    SETTINGS
}

data class DragState(
    val isDragging: Boolean = false,
    val pieceIndex: Int = -1,
    val touchOffset: Offset = Offset.Zero,
    val gridRow: Int = -1,
    val gridCol: Int = -1,
    val isValid: Boolean = false
)

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val gameMode: GameMode = GameMode.CLASSIC,
    val grid: Array<Array<BoardCell>> = Array(10) { Array(10) { BoardCell() } },
    val availablePieces: List<GamePiece?> = listOf(null, null, null),
    val score: Int = 0,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val combo: Int = 0,
    val linesClearedTotal: Int = 0,
    val blocksPlacedTotal: Int = 0,
    val overdriveMeter: Float = 0f,
    val isOverdriveActive: Boolean = false,
    val activeEvent: BoardEvent? = null,
    val timeAttackSeconds: Int = 60,
    val duelAiScore: Int = 0,
    val nearMissRows: Set<Int> = emptySet(),
    val nearMissCols: Set<Int> = emptySet(),
    val smartHint: Triple<Int, Int, Int>? = null, // pieceIndex, r, c
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val showPerfectClearBanner: Boolean = false,
    val showShareDialog: Boolean = false,
    val showDailyRewardDialog: Boolean = false,
    val showLevelUpDialog: Boolean = false,
    val showTutorial: Boolean = false,
    val earnedCoins: Int = 0,
    val earnedXp: Int = 0,
    val profile: PlayerProfile = PlayerProfile(),
    val settings: GameSettings = GameSettings(),
    val stats: GameStats = GameStats(),
    val challenges: List<ChallengeLevel> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val dailyRewardClaimed: Boolean = false,
    val dailyRewardStreakDay: Int = 1
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameStateRepository(application)
    val audioManager = AudioManager()
    val hapticsManager = HapticsManager(application)
    val vfxManager = VfxManager()

    private var engine: GameEngine = GameEngine(GameMode.CLASSIC, repository.getHighScore(GameMode.CLASSIC))

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var timerJob: Job? = null
    private var idleHintJob: Job? = null
    private var gameStartTime: Long = 0L

    init {
        loadInitialData()
        startGameLoop()
    }

    private fun loadInitialData() {
        val profile = repository.getPlayerProfile()
        val settings = repository.getSettings()
        val stats = repository.getStats()
        val challenges = repository.getChallengeLevels()
        val achievements = repository.getAchievements()
        val dailyClaimed = repository.isDailyRewardClaimedToday()
        val streakDay = repository.getDailyRewardStreakDay()
        val firstLaunch = repository.isFirstLaunch()

        audioManager.setSfxEnabled(settings.soundEnabled)
        audioManager.setMusicEnabled(settings.musicEnabled)
        hapticsManager.setHapticsEnabled(settings.hapticsEnabled)

        _uiState.value = _uiState.value.copy(
            profile = profile,
            settings = settings,
            stats = stats,
            challenges = challenges,
            achievements = achievements,
            dailyRewardClaimed = dailyClaimed,
            dailyRewardStreakDay = streakDay,
            showTutorial = firstLaunch
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                vfxManager.update(0.016f)
                delay(16)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        audioManager.playClick()
        hapticsManager.vibrateLight()
        if (screen != AppScreen.GAME && _uiState.value.currentScreen == AppScreen.GAME) {
            timerJob?.cancel()
        }
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun startNewGame(mode: GameMode) {
        audioManager.playClick()
        hapticsManager.vibrateMedium()
        gameStartTime = System.currentTimeMillis()
        engine = GameEngine(mode, repository.getHighScore(mode))

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.GAME,
            gameMode = mode,
            grid = copyGrid(engine.grid),
            availablePieces = engine.availablePieces.toList(),
            score = 0,
            highScore = engine.highScore,
            isNewHighScore = false,
            combo = 0,
            linesClearedTotal = 0,
            blocksPlacedTotal = 0,
            overdriveMeter = 0f,
            isOverdriveActive = false,
            activeEvent = null,
            timeAttackSeconds = 60,
            duelAiScore = 0,
            isGameOver = false,
            isPaused = false,
            showPerfectClearBanner = false,
            showShareDialog = false,
            smartHint = null,
            nearMissRows = emptySet(),
            nearMissCols = emptySet()
        )

        startModeTimer(mode)
        resetIdleHintTimer()
    }

    private fun startModeTimer(mode: GameMode) {
        timerJob?.cancel()
        if (mode == GameMode.TIME_ATTACK) {
            timerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    if (!_uiState.value.isPaused && !_uiState.value.isGameOver) {
                        val ended = engine.decrementTimeAttack()
                        _uiState.value = _uiState.value.copy(
                            timeAttackSeconds = engine.timeAttackSeconds
                        )
                        if (ended) {
                            handleGameOver()
                            break
                        }
                    }
                }
            }
        }
    }

    private fun resetIdleHintTimer() {
        idleHintJob?.cancel()
        _uiState.value = _uiState.value.copy(smartHint = null)

        if (!_uiState.value.settings.smartHintsEnabled) return

        idleHintJob = viewModelScope.launch {
            delay(5000) // 5 seconds of idle
            if (!_uiState.value.isGameOver && !_uiState.value.isPaused) {
                val hint = engine.findSmartHint()
                _uiState.value = _uiState.value.copy(smartHint = hint)
            }
        }
    }

    fun onDragStart(pieceIndex: Int, initialOffset: Offset) {
        val piece = engine.availablePieces.getOrNull(pieceIndex) ?: return
        audioManager.playPickup()
        hapticsManager.vibrateLight()
        idleHintJob?.cancel()

        _dragState.value = DragState(
            isDragging = true,
            pieceIndex = pieceIndex,
            touchOffset = initialOffset,
            gridRow = -1,
            gridCol = -1,
            isValid = false
        )
    }

    fun onDragMove(touchOffset: Offset, boardOrigin: Offset, boardSize: Float) {
        val currentDrag = _dragState.value
        if (!currentDrag.isDragging) return

        val piece = engine.availablePieces.getOrNull(currentDrag.pieceIndex) ?: return
        val cellSize = boardSize / 10f

        // Finger offset: lift the piece up by 1.8 cell heights so user can see under thumb
        val pieceLiftY = cellSize * 1.8f
        val effectiveX = touchOffset.x - (piece.cols * cellSize / 2f)
        val effectiveY = touchOffset.y - (piece.rows * cellSize / 2f) - pieceLiftY

        val relativeX = effectiveX - boardOrigin.x
        val relativeY = effectiveY - boardOrigin.y

        val gridCol = floor((relativeX + cellSize / 2f) / cellSize).toInt()
        val gridRow = floor((relativeY + cellSize / 2f) / cellSize).toInt()

        val isValid = if (gridRow in 0..(10 - piece.rows) && gridCol in 0..(10 - piece.cols)) {
            engine.canPlacePiece(piece, gridRow, gridCol)
        } else {
            false
        }

        _dragState.value = currentDrag.copy(
            touchOffset = touchOffset,
            gridRow = gridRow,
            gridCol = gridCol,
            isValid = isValid
        )
    }

    fun onDragEnd(boardOrigin: Offset, boardSize: Float) {
        val currentDrag = _dragState.value
        if (!currentDrag.isDragging) return

        val pieceIndex = currentDrag.pieceIndex
        val piece = engine.availablePieces.getOrNull(pieceIndex)

        if (piece != null && currentDrag.isValid && currentDrag.gridRow >= 0 && currentDrag.gridCol >= 0) {
            // Valid drop! Place piece!
            val result = engine.placePiece(pieceIndex, currentDrag.gridRow, currentDrag.gridCol)
            if (result != null) {
                audioManager.playPlace()
                hapticsManager.vibrateMedium()

                // VFX & Audio for line clears
                if (result.linesCleared > 0) {
                    audioManager.playLineClear(result.linesCleared)
                    hapticsManager.vibrateMedium()

                    if (_uiState.value.settings.screenShakeEnabled) {
                        vfxManager.triggerShake(0.35f + result.linesCleared * 0.15f)
                    }
                    vfxManager.triggerFlash(Color(0xFF00F0FF), 0.35f)

                    val cellSize = boardSize / 10f
                    val color = com.example.ui.components.BlockRenderer.getBlockBaseColor(piece.colorIndex)
                    vfxManager.spawnLineClearParticles(result.clearedCells, cellSize, boardOrigin.x, boardOrigin.y, color)

                    // Combo popup
                    if (result.combo > 1) {
                        audioManager.playCombo(result.combo)
                        hapticsManager.vibrateCombo()
                        vfxManager.spawnFloatingText("COMBO x${result.combo}!", boardOrigin.x + boardSize / 2f, boardOrigin.y + boardSize / 2f, Color(0xFFFFD700), 1.6f)
                    } else {
                        vfxManager.spawnFloatingText("+${result.pointsAwarded}", boardOrigin.x + boardSize / 2f, boardOrigin.y + boardSize * 0.45f, Color.White, 1.3f)
                    }

                    // Special blocks audio
                    for (sp in result.activatedSpecials) {
                        repository.recordSpecialBlockUse()
                        if (sp == com.example.model.SpecialBlockType.BOMB) {
                            audioManager.playBomb()
                            vfxManager.triggerShake(0.65f)
                        } else if (sp == com.example.model.SpecialBlockType.LASER_ROW || sp == com.example.model.SpecialBlockType.LASER_COL || sp == com.example.model.SpecialBlockType.CROSS) {
                            audioManager.playLaser()
                        }
                    }

                    // Perfect clear
                    if (result.isPerfectClear) {
                        audioManager.playPerfectClear()
                        hapticsManager.vibrateCelebration()
                        vfxManager.spawnConfetti(boardSize * 1.5f, boardSize * 2f, 150)
                        vfxManager.triggerShake(0.85f)
                        _uiState.value = _uiState.value.copy(showPerfectClearBanner = true)
                    }
                }

                // Near miss detection
                val (nearRows, nearCols) = engine.calculateNearMisses()

                val isNewRecord = engine.score > repository.getHighScore(_uiState.value.gameMode)

                _uiState.value = _uiState.value.copy(
                    grid = copyGrid(engine.grid),
                    availablePieces = engine.availablePieces.toList(),
                    score = engine.score,
                    highScore = maxOf(engine.score, engine.highScore),
                    isNewHighScore = isNewRecord,
                    combo = engine.combo,
                    linesClearedTotal = engine.linesClearedTotal,
                    blocksPlacedTotal = engine.blocksPlacedTotal,
                    overdriveMeter = engine.overdriveMeter,
                    isOverdriveActive = engine.isOverdriveActive,
                    activeEvent = engine.activeBoardEvent,
                    duelAiScore = engine.duelAiScore,
                    nearMissRows = nearRows,
                    nearMissCols = nearCols,
                    smartHint = null
                )

                if (result.isGameOver) {
                    handleGameOver()
                } else {
                    resetIdleHintTimer()
                }
            }
        }

        _dragState.value = DragState()
    }

    fun onDragCancel() {
        _dragState.value = DragState()
        resetIdleHintTimer()
    }

    fun activateOverdrive() {
        if (engine.activateOverdrive()) {
            audioManager.playOverdrive()
            hapticsManager.vibrateCelebration()
            vfxManager.triggerFlash(Color(0xFFFFF700), 0.5f)
            vfxManager.spawnFloatingText("⚡ OVERDRIVE ACTIVATED! 2X SCORE!", 300f, 600f, Color(0xFFFFD700), 1.5f)
            _uiState.value = _uiState.value.copy(
                overdriveMeter = 0f,
                isOverdriveActive = true
            )
        }
    }

    fun requestHint() {
        val hint = engine.findSmartHint()
        if (hint != null) {
            audioManager.playClick()
            hapticsManager.vibrateLight()
            _uiState.value = _uiState.value.copy(smartHint = hint)
        }
    }

    private fun handleGameOver() {
        timerJob?.cancel()
        idleHintJob?.cancel()
        audioManager.playGameOver()
        hapticsManager.vibrateHeavy()

        val isNewRecord = repository.saveHighScore(_uiState.value.gameMode, engine.score)
        val durationSec = (System.currentTimeMillis() - gameStartTime) / 1000L

        // XP and coins reward based on score
        val xpGain = (engine.score / 15).coerceAtLeast(50)
        val coinsGain = (engine.score / 60).coerceAtLeast(10)
        val (updatedProfile, leveledUp) = repository.addXpAndCoins(xpGain, coinsGain)

        repository.updateStats(
            lines = engine.linesClearedTotal,
            blocks = engine.blocksPlacedTotal,
            combo = engine.highestCombo,
            isPerfectClear = _uiState.value.showPerfectClearBanner,
            durationSec = durationSec
        )

        if (_uiState.value.gameMode == GameMode.TIME_ATTACK) {
            repository.recordTimeAttackGame()
        }

        _uiState.value = _uiState.value.copy(
            isGameOver = true,
            isNewHighScore = isNewRecord,
            earnedCoins = coinsGain,
            earnedXp = xpGain,
            profile = updatedProfile,
            showLevelUpDialog = leveledUp,
            stats = repository.getStats(),
            achievements = repository.getAchievements()
        )
    }

    fun pauseGame() {
        audioManager.playClick()
        _uiState.value = _uiState.value.copy(isPaused = true)
    }

    fun resumeGame() {
        audioManager.playClick()
        _uiState.value = _uiState.value.copy(isPaused = false)
        resetIdleHintTimer()
    }

    fun restartGame() {
        startNewGame(_uiState.value.gameMode)
    }

    fun quitToHome() {
        timerJob?.cancel()
        idleHintJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.HOME,
            isPaused = false,
            isGameOver = false
        )
    }

    fun toggleShareDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showShareDialog = show)
    }

    fun toggleDailyRewardDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDailyRewardDialog = show)
    }

    fun claimDailyReward() {
        val reward = repository.claimDailyReward()
        audioManager.playReward()
        hapticsManager.vibrateCelebration()
        _uiState.value = _uiState.value.copy(
            dailyRewardClaimed = true,
            profile = repository.getPlayerProfile(),
            showDailyRewardDialog = false
        )
    }

    fun equipSkin(skin: BlockSkinId) {
        audioManager.playClick()
        val updated = _uiState.value.profile.copy(activeSkin = skin)
        repository.savePlayerProfile(updated)
        _uiState.value = _uiState.value.copy(profile = updated)
    }

    fun equipTheme(theme: BoardThemeId) {
        audioManager.playClick()
        val updated = _uiState.value.profile.copy(activeTheme = theme)
        repository.savePlayerProfile(updated)
        _uiState.value = _uiState.value.copy(profile = updated)
    }

    fun equipAvatar(avatarId: String) {
        audioManager.playClick()
        val updated = _uiState.value.profile.copy(avatarId = avatarId)
        repository.savePlayerProfile(updated)
        _uiState.value = _uiState.value.copy(profile = updated)
    }

    fun equipTitle(title: String) {
        audioManager.playClick()
        val updated = _uiState.value.profile.copy(title = title)
        repository.savePlayerProfile(updated)
        _uiState.value = _uiState.value.copy(profile = updated)
    }

    fun updateSettings(newSettings: GameSettings) {
        repository.saveSettings(newSettings)
        audioManager.setSfxEnabled(newSettings.soundEnabled)
        audioManager.setMusicEnabled(newSettings.musicEnabled)
        hapticsManager.setHapticsEnabled(newSettings.hapticsEnabled)
        _uiState.value = _uiState.value.copy(settings = newSettings)
    }

    fun resetProgress() {
        repository.resetAllProgress()
        loadInitialData()
    }

    fun closeTutorial() {
        _uiState.value = _uiState.value.copy(showTutorial = false)
    }

    fun dismissLevelUp() {
        _uiState.value = _uiState.value.copy(showLevelUpDialog = false)
    }

    private fun copyGrid(source: Array<Array<BoardCell>>): Array<Array<BoardCell>> {
        return Array(10) { r ->
            Array(10) { c -> source[r][c].copy() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
        gameLoopJob?.cancel()
        timerJob?.cancel()
        idleHintJob?.cancel()
    }
}
