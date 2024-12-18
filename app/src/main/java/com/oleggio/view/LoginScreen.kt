package com.oleggio.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oleggio.topchat.viewmodel.LoginViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = hiltViewModel()) {
    var isLoading by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val error by loginViewModel.error.collectAsState()
    val token by loginViewModel.token.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (!token.isNullOrEmpty()) {
        navController.navigate("chats")
    }

    Scaffold (
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
        content = { padding ->
            LaunchedEffect(error) {
                if (error != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = error ?: "An uknown error occured",
                            actionLabel = "Ok"
                        )
                        loginViewModel.clearError()
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = padding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Login", modifier = Modifier.padding(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Password", modifier = Modifier.padding(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        loginViewModel.login(name, password)
                        isLoading = false
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(text = "Login")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}