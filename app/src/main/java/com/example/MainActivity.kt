package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdsManager
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.CollectionScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdsManager.initialize(this)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B0F19)
                ) {
                    BlockverseApp()
                }
            }
        }
    }
}

@Composable
fun BlockverseApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
        if (uiState.currentScreen == AppScreen.GAME) {
            if (!uiState.isGameOver) {
                viewModel.pauseGame()
            } else {
                viewModel.quitToHome()
            }
        } else {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    when (uiState.currentScreen) {
        AppScreen.HOME -> HomeScreen(viewModel = viewModel)
        AppScreen.GAME -> GameScreen(viewModel = viewModel)
        AppScreen.COLLECTION -> CollectionScreen(viewModel = viewModel)
        AppScreen.CHALLENGES -> ChallengesScreen(viewModel = viewModel)
        AppScreen.LEADERBOARD -> LeaderboardScreen(viewModel = viewModel)
        AppScreen.STATS -> StatsScreen(viewModel = viewModel)
        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
    }
}
