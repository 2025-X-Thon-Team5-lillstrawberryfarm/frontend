package com.example.myapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapp.R

@Composable
fun CommonTopBar(
    titleImageId: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 상단바 내용 영역 (흰색 배경)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                // ★ 세로 패딩을 12.dp -> 20.dp로 늘려 높이를 키움
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 왼쪽: 텍스트 이미지
            Image(
                painter = painterResource(id = titleImageId),
                contentDescription = "Page Title",
                modifier = Modifier.height(24.dp) // 크기에 맞춰 22->24로 살짝 키움
            )

            // 2. 오른쪽: 파란색 로고
            Image(
                painter = painterResource(id = R.drawable.blue_logo),
                contentDescription = "App Logo",
                modifier = Modifier.height(26.dp) // 크기에 맞춰 24->26로 살짝 키움
            )
        }

        // 하단 경계선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFF002CCE).copy(alpha = 0.5f))
        )
    }
}