package com.example.myapp.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapp.ui.data.api.RetrofitClient
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthlyUpdatePopup(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var lastMonthTotal by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dateStr = String.format("%04d%02d", year, month)

        try {
            val response = RetrofitClient.api.getTransactions(date = dateStr)
            if (response.isSuccessful && response.body() != null) {
                // ★ 수정: content가 null일 경우 빈 리스트로 처리
                val list = response.body()?.content ?: emptyList()

                // ★ 수정: amt가 null일 경우 0으로 처리 (sumOf 에러 해결)
                lastMonthTotal = list.filter { it.type == "WITHDRAW" }.sumOf { it.amt ?: 0 }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }

        for (i in 0 until 4) {
            delay(2500)
            step++
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if(step == 4) 0.dp else 16.dp, bottomEnd = if(step == 4) 0.dp else 16.dp))
                    .background(Color(0xFF0024CC))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(2000)) togetherWith
                                fadeOut(animationSpec = tween(2000))
                    },
                    label = "PopupContent"
                ) { currentStep ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (currentStep) {
                            0 -> Step0_Updated()
                            1 -> Step1_Amount(lastMonthTotal)
                            2 -> Step2_Detail1(lastMonthTotal)
                            3 -> Step3_Detail2(lastMonthTotal)
                            4 -> Step3_Detail2(lastMonthTotal)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = step == 4,
                enter = expandVertically(animationSpec = tween(1000)) + fadeIn(animationSpec = tween(1000))
            ) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0024CC)
                    ),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "새로운 핀메이트 보러가기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Step0_Updated() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "이번 달의 핀메이트가\n새롭게 갱신되었어요.",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )
    }
}

@Composable
fun Step1_Amount(amount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("지난 달 소비 총 금액", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${formatPopupMoney(amount)}원", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun Step2_Detail1(amount: Int) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("지난 달 소비 총 금액", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(0.dp))
        Text("${formatPopupMoney(amount)}원", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "당신의 그룹에서 지난 달 어떤 소비를 했는지\n카테고리별로 확인할 수 있어요.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun Step3_Detail2(amount: Int) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text("지난 달 소비 총 금액", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(0.dp))
        Text("${formatPopupMoney(amount)}원", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "당신의 그룹에서 지난 달 어떤 소비를 했는지\n카테고리별로 확인할 수 있어요.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "이번 달에는 어떤 소비를 하는지 실시간으로\n확인하며 소비 습관을 잘 가꿔보세요.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 18.sp
        )
    }
}

fun formatPopupMoney(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}