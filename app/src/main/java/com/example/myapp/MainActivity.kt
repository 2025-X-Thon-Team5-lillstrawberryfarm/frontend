package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

import com.example.myapp.ui.screens.start.StartScreen
import com.example.myapp.ui.screens.login.LoginScreen
import com.example.myapp.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                MainFlow()
            }
        }
    }
}

@Composable
fun MainFlow() {
    // 1. 스플래시 상태
    var showSplash by remember { mutableStateOf(true) }

    // 2. 로그인 완료 여부 상태 (앱 켤 때마다 false로 초기화 -> 무조건 로그인 창 뜸)
    var isLoginCompleted by remember { mutableStateOf(false) }

    // 앱 시작 시 1.5초 카운트
    LaunchedEffect(Unit) {
        delay(1500)
        showSplash = false // 스플래시 종료
    }

    // 화면 전환 로직
    if (showSplash) {
        StartScreen()
    } else {
        // 스플래시 후 -> 로그인 안 했으면 LoginScreen, 했으면 AppNavigation
        Crossfade(targetState = isLoginCompleted, animationSpec = tween(500), label = "MainTransition") { completed ->
            if (completed) {
                AppNavigation()
            } else {
                LoginScreen(
                    onLoginSuccess = { isLoginCompleted = true } // 로그인 성공 시 메인으로 전환
                )
            }
        }
    }
}