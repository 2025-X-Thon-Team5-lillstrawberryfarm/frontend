package com.example.myapp.ui.screens.ai

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapp.ui.components.CommonTopBar // ★ 공통 헤더 Import
import com.example.myapp.R

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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val mainBlue = Color(0xFF002CCE)

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(ChatMessage("안녕하세요! 저는 당신의 금융 AI 메이트입니다. 무엇을 도와드릴까요?", false, getCurrentTime()))
        }
    }

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
        // ★ 헤더 교체: AI 메이트 텍스트 이미지
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
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "월간 소비 분석 리포트",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isReportExpanded) "접기" else "보기",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
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
                    Text(
                        text = "당신은 소비구간 (XX만원~YY만원) 구간에 속해있습니다.\n" +
                                "지난 달 당신의 소비습관을 분석해본다면 ...\n" +
                                "현재 소비습관을 지난달과 비교해본다면...\n\n" +
                                "당신이 설정한 목표치에 가깝게 제안하고자 하는 바는 ...\n\n" +
                                "블라블라블라블라블라\n\n" +
                                "뭐시기뭐시기\n\n" +
                                "아몰랑아몰랑\n",
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
                    messages.add(ChatMessage(inputText, true, getCurrentTime()))
                    val userQuestion = inputText
                    inputText = ""

                    coroutineScope.launch {
                        delay(1000)
                        val aiResponse = getMockAiResponse(userQuestion)
                        messages.add(ChatMessage(aiResponse, false, getCurrentTime()))
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
                .background(
                    if (message.isUser) Color(0xFF002CCE) else Color(0xFFE1E1E1)
                )
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

fun getMockAiResponse(question: String): String {
    return when {
        question.contains("안녕") -> "안녕하세요! 오늘 기분은 어떠신가요?"
        question.contains("소비") || question.contains("돈") -> "이번 달 소비 내역을 분석해 드릴까요?"
        question.contains("추천") -> "사용자님의 패턴에 맞는 예금 상품이 있습니다."
        else -> "죄송합니다. 아직 배우는 중이라 잘 모르겠어요. 다시 말씀해 주시겠어요?"
    }
}