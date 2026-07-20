package com.example

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val sessionUser by viewModel.sessionUser.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(sessionUser) {
        if (sessionUser != null) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Compact layout kicks in on short/landscape screens so everything
            // fits in one view with no scrolling required.
            val compact = maxHeight < 500.dp

            val iconSize: Dp = if (compact) 36.dp else 72.dp
            val fieldSpacing: Dp = if (compact) 8.dp else 16.dp
            val outerPadding: Dp = if (compact) 12.dp else 24.dp
            val buttonHeight: Dp = if (compact) 40.dp else 50.dp

            // Don't force an explicit .height() on OutlinedTextField — it has an
            // internal minimum height for correctly laying out/clipping the text
            // baseline, and forcing it smaller clips typed text so it renders
            // invisible even though the cursor still shows.Instead, shrink via
            // content padding + a smaller text style, which the field supports.
            val fieldTextStyle = if (compact) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 450.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(fieldSpacing)
                ) {
                    if (compact) {
                        // Landscape / short-height: icon + title side by side to save vertical space
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Precision Calc Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(iconSize)
                            )
                            Text(
                                text = "tejas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Precision Calc Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(iconSize)
                        )

                        Text(
                            text = "tejas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Authenticate securely to access your advanced workspace.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Username Icon")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        textStyle = fieldTextStyle,
                        singleLine = true,
                        enabled = authState !is AuthState.Loading
                    )

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        textStyle = fieldTextStyle,
                        singleLine = true,
                        enabled = authState !is AuthState.Loading
                    )

                    // Errors and Loading — only reserve space when actually shown,
                    // and skip in compact mode to keep everything on-screen.
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        val errorState = authState as? AuthState.Error
                        Text(
                            text = errorState?.message ?: "Error logging in",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(if (compact) 20.dp else 40.dp)
                                .testTag("login_progress_indicator")
                        )
                    }

                    // Log In Button + Register row combined into one row in compact mode
                    if (compact) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.login(username, password) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight)
                                    .testTag("login_button"),
                                enabled = username.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
                            ) {
                                Text(
                                    text = "Log In", fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = onNavigateToRegister,
                                modifier = Modifier.testTag("btn_navigate_to_register"),
                                enabled = authState !is AuthState.Loading
                            ) {
                                Text(
                                    text = "Register",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.login(username, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .testTag("login_button"),
                            enabled = username.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
                        ) {
                            Text(text = "Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Don't have an account?",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = onNavigateToRegister,
                                modifier = Modifier.testTag("btn_navigate_to_register"),
                                enabled = authState !is AuthState.Loading
                            ) {
                                Text(text = "Register", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}