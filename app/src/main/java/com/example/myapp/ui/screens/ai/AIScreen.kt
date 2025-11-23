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
import com.example.myapp.ui.data.api.ChatRequest
import com.example.myapp.ui.data.api.ReportRequest
import com.example.myapp.ui.data.api.RetrofitClient
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

    // ★ AI 리포트 텍스트 상태
    var reportText by remember { mutableStateOf("리포트를 생성 중입니다...") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val mainBlue = Color(0xFF002CCE)

    // 사용자 ID 가져오기
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val userId = sharedPref.getInt("user_id", 1) // 기본값 1

    // ★ 초기화 및 리포트 로딩
    LaunchedEffect(Unit) {
        // 1. 토큰 초기화 (안전장치)
        RetrofitClient.initToken(context)

        // 2. 웰컴 메시지
        if (messages.isEmpty()) {
            messages.add(ChatMessage("안녕하세요! 저는 당신의 금융 AI 메이트입니다. 무엇을 도와드릴까요?", false, getCurrentTime()))
        }

        // 3. 리포트 생성 API 호출
        try {
            val response = RetrofitClient.api.generateReport(ReportRequest(user_id = userId))
            if (response.isSuccessful && response.body() != null) {
                reportText = response.body()!!.report_text
            } else {
                reportText = "리포트 생성에 실패했습니다. 잠시 후 다시 시도해주세요."
            }
        } catch (e: Exception) {
            reportText = "네트워크 오류가 발생했습니다."
            e.printStackTrace()
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
                    // ★ 실제 서버에서 받은 리포트 텍스트 표시
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
                    val userMsg = inputText
                    messages.add(ChatMessage(userMsg, true, getCurrentTime()))
                    inputText = ""

                    // ★ 채팅 API 호출
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.api.chatWithAI(ChatRequest(user_id = userId, message = userMsg))
                            if (response.isSuccessful && response.body() != null) {
                                val aiReply = response.body()!!.reply
                                messages.add(ChatMessage(aiReply, false, getCurrentTime()))
                            } else {
                                messages.add(ChatMessage("죄송합니다. 오류가 발생했습니다.", false, getCurrentTime()))
                            }
                        } catch (e: Exception) {
                            messages.add(ChatMessage("네트워크 오류가 발생했습니다.", false, getCurrentTime()))
                            e.printStackTrace()
                        }
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