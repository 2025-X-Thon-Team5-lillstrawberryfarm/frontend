package com.example.myapp // 패키지명 유지

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

// Import 확인
import com.example.myapp.ui.screens.start.StartScreen
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
    // 스플래시 화면 상태 관리
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500) // 1.5초 대기 (로고 감상 시간)
        showSplash = false
    }

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(durationMillis = 500),
        label = "SplashTransition"
    ) { isSplash ->
        if (isSplash) {
            StartScreen()
        } else {
            // ★ 스플래시가 끝나면 통합 네비게이션 화면으로 이동
            AppNavigation()
        }
    }
}