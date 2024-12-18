package com.oleggio.topchat

import com.oleggio.view.LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.oleggio.view.ChatScreen
import com.oleggio.view.ViewImageFullscreen
import com.oleggio.view.MessageScreen
import com.oleggio.topchat.ui.theme.ChadTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChadTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "chats") {
                            composable("messages") { MessageScreen(navController) }
                            composable("chats") { ChatScreen(navController) }
                            composable("login") { LoginScreen(navController) }
                            composable(
                                route = "viewImage/{imageUrl}",
                                arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                                ViewImageFullscreen(imageUrl) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
