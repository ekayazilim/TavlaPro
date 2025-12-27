package com.toplu.tavlauygulamasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.toplu.tavlauygulamasi.ui.board.BoardScreen
import com.toplu.tavlauygulamasi.ui.board.GameViewModel
import com.toplu.tavlauygulamasi.ui.menu.MainMenuScreen
import com.toplu.tavlauygulamasi.ui.history.HistoryScreen
import com.toplu.tavlauygulamasi.ui.history.HistoryViewModel
import com.toplu.tavlauygulamasi.ui.settings.SettingsScreen
import com.toplu.tavlauygulamasi.ui.theme.TavlaUygulamasıTheme

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val matchDao = (application as TavlaApplication).database.matchDao()
                return GameViewModel(application, matchDao) as T
            }
        }
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val matchDao = (application as TavlaApplication).database.matchDao()
                return HistoryViewModel(matchDao) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TavlaUygulamasıTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        MainMenuScreen(
                            onPlayLocal = { 
                                gameViewModel.setAiMode(true)
                                gameViewModel.resetGame()
                                navController.navigate("game") 
                            },
                            onPlayOnline = { 
                                // Here we use it as Local 2-Player for now
                                gameViewModel.setAiMode(false)
                                gameViewModel.resetGame()
                                navController.navigate("game")
                            },
                            onProfileClick = { navController.navigate("history") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("game") {
                        BoardScreen(
                            viewModel = gameViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = historyViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        val difficulty by gameViewModel.aiDifficulty.collectAsState()
                        val currentTheme by gameViewModel.currentTheme.collectAsState()
                        SettingsScreen(
                            currentDifficulty = difficulty,
                            onDifficultyChange = { gameViewModel.setAiDifficulty(it) },
                            currentTheme = currentTheme,
                            onThemeChange = { gameViewModel.setTheme(it) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}