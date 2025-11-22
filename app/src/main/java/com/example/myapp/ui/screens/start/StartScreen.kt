package com.example.myapp.ui.screens.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapp.R

@Composable
fun StartScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. 배경 이미지 (가장 뒤에 깔림)
        Image(
            painter = painterResource(id = R.drawable.background), // background.png 불러오기
            contentDescription = null, // 배경이므로 설명 생략 가능
            contentScale = ContentScale.Crop, // 화면을 꽉 채우도록 자름
            modifier = Modifier.fillMaxSize()
        )

        // 2. 중앙 로고들 (배경 위에 올라감)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 상단 로고 이미지
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(134.dp)
                    .height(114.dp)
            )

            Spacer(modifier = Modifier.height(9.dp))
        }
    }
}