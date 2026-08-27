package com.vivocloud.reporting_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vivocloud.reporting_app.ui.theme.ReportingappTheme

enum class AuthState {
    LOGIN,
    SIGN_UP,
    AUTHENTICATED
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var authState by remember { mutableStateOf(AuthState.LOGIN) }
            ReportingappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (authState) {
                        AuthState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { authState = AuthState.AUTHENTICATED },
                                onNavigateToSignUp = { authState = AuthState.SIGN_UP }
                            )
                        }
                        AuthState.SIGN_UP -> {
                            SignUpScreen(
                                onSignUpSuccess = { authState = AuthState.AUTHENTICATED },
                                onNavigateToLogin = { authState = AuthState.LOGIN }
                            )
                        }
                        AuthState.AUTHENTICATED -> {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.camera_title)) },
                    label = { Text(stringResource(R.string.camera_title)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = stringResource(R.string.gallery_title)) },
                    label = { Text(stringResource(R.string.gallery_title)) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> CameraScreen()
                1 -> GalleryScreen()
            }
        }
    }
}