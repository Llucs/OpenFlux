package com.openflux.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openflux.app.ui.screens.ChatScreen
import com.openflux.app.ui.screens.FileExplorerScreen
import com.openflux.app.ui.screens.SettingsScreen
import com.openflux.app.ui.screens.TerminalScreen
import com.openflux.app.ui.theme.OpenFluxTheme
import com.openflux.app.ui.viewmodel.ChatViewModel
import com.openflux.app.ui.viewmodel.SettingsViewModel
import com.openflux.app.ui.viewmodel.TerminalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OpenFluxTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val chatViewModel: ChatViewModel = viewModel()
                    val settingsViewModel: SettingsViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "chat"
                    ) {
                        composable("chat") {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onOpenTerminal = { navController.navigate("terminal") },
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenFiles = { navController.navigate("files") }
                            )
                        }
                        composable("terminal") {
                            val terminalViewModel: TerminalViewModel = viewModel()
                            TerminalScreen(
                                viewModel = terminalViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("files") {
                            FileExplorerScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
