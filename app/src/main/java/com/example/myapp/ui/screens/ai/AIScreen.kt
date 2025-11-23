package com.example.myapp.ui.screens.ai

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

@Composable
fun AIScreen() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isReportExpanded by remember { mutableStateOf(false) }

    // ★ 하드코딩된 리포트 내용
    val reportText = """
        [10월 소비 패턴 분석]
        
        지난달은 '식비' 지출이 가장 많았습니다. (전체 45%)
        배달 음식 이용 횟수가 전월 대비 3회 증가했어요.
        
        💡 AI의 제안:
        이번 달은 외식 횟수를 주 1회로 줄이고, 
        '문화/여가' 예산을 10% 늘려보시는 건 어떨까요?
        목표 달성까지 15만원 남았습니다!
    """.trimIndent()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val mainBlue = Color(0xFF002CCE)

    // ★ 초기화 및 시나리오 시작
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            // 1. 첫 인사 및 질문 (하드코딩)
            messages.add(ChatMessage("안녕하세요! 당신의 금융 AI 메이트입니다.\n이번 달 목표 저축액이나 소득 목표는 얼마인가요?", false, getCurrentTime()))
        }
    }

    // 메시지 추가 시 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        CommonTopBar(titleImageId = R.drawable.ai_text)

        // AI 리포트 토글
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECECEC))
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isReportExpanded = !isReportExpanded }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Report Icon",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(18.dp).offset(y = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("월간 소비 분석 리포트", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isReportExpanded) "접기" else "보기", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isReportExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Report",
                        tint = Color.Gray
                    )
                }
            }

            if (isReportExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    // ★ 하드코딩된 리포트 텍스트 표시
                    Text(
                        text = reportText,
                        fontSize = 15.sp,
                        color = Color.Black,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        // 채팅 리스트
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }

        // 하단 입력창
        ChatInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSendClick = {
                if (inputText.isNotBlank()) {
                    // 1. 사용자 메시지 추가
                    messages.add(ChatMessage(inputText, true, getCurrentTime()))
                    inputText = ""

                    // 2. AI 답변 시뮬레이션 (하드코딩)
                    coroutineScope.launch {
                        delay(1000) // 1초 딜레이 (생각하는 척)

                        // ★ 무조건 응원 메시지 전송
                        val aiReply = "목표를 확인했습니다. \n저와 함께 소비 습관을 관리해서 꼭 달성해봐요! 화이팅! 💪"
                        messages.add(ChatMessage(aiReply, false, getCurrentTime()))
                    }
                }
            },
            mainColor = mainBlue
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = message.timestamp,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        )

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (message.isUser) Color(0xFF002CCE) else Color(0xFFE1E1E1))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = if (message.isUser) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    mainColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
            .border(1.dp, mainColor, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("메시지를 입력하세요...", color = Color.LightGray, fontSize = 16.sp)
                }
                innerTextField()
            }
        )

        IconButton(onClick = onSendClick) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "전송",
                tint = if (value.isNotBlank()) mainColor else Color.Gray
            )
        }
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.US)
    return sdf.format(Date())
}