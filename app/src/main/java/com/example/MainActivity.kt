package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AuthRepositoryImpl
import com.example.data.CalculatorDatabase
import com.example.data.HistoryRepository
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room database components
        val database = CalculatorDatabase.getDatabase(applicationContext)
        val historyRepository = HistoryRepository(database.historyDao())
        val authRepository = AuthRepositoryImpl(database.userDao(), applicationContext)

        // Initialize ViewModels using custom factories
        val calculatorViewModel: CalculatorViewModel by viewModels {
            CalculatorViewModel.Factory(historyRepository)
        }
        val authViewModel: AuthViewModel by viewModels {
            AuthViewModel.Factory(authRepository)
        }

        setContent {
            MyApplicationTheme {
                // Use Surface to provide the background color. 
                // We remove the outer Scaffold to prevent double-padding/double-insets,
                // as each screen (Login, Register, Calculator) has its own Scaffold.
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val sessionUser by authViewModel.sessionUser.collectAsState()
                    var startRoute by remember { mutableStateOf<String?>(null) }

                    // Determine initial route on app launch
                    LaunchedEffect(sessionUser) {
                        if (startRoute == null) {
                            startRoute = if (sessionUser != null) "calculator" else "login"
                        }
                    }

                    if (startRoute == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = startRoute!!,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onNavigateToRegister = {
                                        navController.navigate("register")
                                    },
                                    onLoginSuccess = {
                                        navController.navigate("calculator") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("register") {
                                RegisterScreen(
                                    viewModel = authViewModel,
                                    onNavigateToLogin = {
                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("calculator") {
                                val currentSessionUser by authViewModel.sessionUser.collectAsState()
                                if (currentSessionUser == null) {
                                    LaunchedEffect(Unit) {
                                        navController.navigate("login") {
                                            popUpTo("calculator") { inclusive = true }
                                        }
                                    }
                                } else {
                                    CalculatorScreen(
                                        viewModel = calculatorViewModel,
                                        onLogout = {
                                            authViewModel.logout()
                                            navController.navigate("login") {
                                                popUpTo("calculator") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
