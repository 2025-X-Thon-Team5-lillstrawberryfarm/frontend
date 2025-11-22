package com.example.myapp.ui.screens.home

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.example.myapp.R // ★ 리소스 사용을 위해 추가

// --- 데이터 모델 ---
data class CategoryExpense(
    val name: String,
    val amount: Int,
    val icon: ImageVector,
    val color: Color
)

data class DailyTransaction(
    val day: Int,
    val income: Int = 0,
    val expense: Int = 0
)

// ★ 전역 변수: 앱이 실행되어 있는 동안 팝업을 봤는지 체크
private var hasShownPopupInSession = false

@Composable
fun MainScreen(
    onNavigateToGroup: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val totalExpense = 1097951

    var showMonthlyPopup by remember { mutableStateOf(!hasShownPopupInSession) }

    val categoryList = listOf(
        CategoryExpense("식비", 450000, Icons.Default.Restaurant, Color(0xFFFF8A80)),
        CategoryExpense("교통", 120000, Icons.Default.DirectionsCar, Color(0xFF80D8FF)),
        CategoryExpense("쇼핑", 300000, Icons.Default.ShoppingCart, Color(0xFFA5D6A7)),
        CategoryExpense("카페", 50000, Icons.Default.LocalCafe, Color(0xFFFFCC80)),
    )

    val calendarData = listOf(
        DailyTransaction(1, expense = 15000),
        DailyTransaction(5, income = 2500000, expense = 50000),
        DailyTransaction(8, expense = 32000),
        DailyTransaction(12, expense = 12000),
        DailyTransaction(15, income = 50000),
        DailyTransaction(22, expense = 120000),
        DailyTransaction(25, expense = 8500),
    ).associateBy { it.day }

    var isExpanded by remember { mutableStateOf(false) }

    if (showMonthlyPopup) {
        MonthlyUpdatePopup(
            onDismiss = {
                showMonthlyPopup = false
                hasShownPopupInSession = true
            },
            onConfirm = {
                showMonthlyPopup = false
                hasShownPopupInSession = true
                onNavigateToGroup()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(scrollState)
    ) {
        // ★ [신규] 우측 상단 로고 배치
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, end = 24.dp), // 상단, 우측 여백
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.blue_logo),
                contentDescription = "App Logo",
                modifier = Modifier.height(24.dp) // 로고 크기
            )
        }

        // 1. 상단 정보 (로고가 생겼으므로 상단 패딩을 줄임)
        TopSection(totalExpense)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 월 선택기
        MonthSelector()

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 캘린더 영역
        CalendarSection(year = 2025, month = 9, data = calendarData)

        Spacer(modifier = Modifier.height(30.dp))

        // 4. 접이식 카테고리 리스트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "카테고리별 소비 보기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "toggle",
                    tint = Color.Gray
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                categoryList.forEach { item ->
                    CategoryRowItem(item = item, totalAmount = totalExpense)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun CalendarSection(year: Int, month: Int, data: Map<Int, DailyTransaction>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { index, day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    color = if (index == 0) Color.Red else if (index == 6) Color.Blue else Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val startDayOffset = 1
        val lastDay = 30
        val totalCells = startDayOffset + lastDay
        val rows = (totalCells / 7) + 1

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val dayCounter = (row * 7) + col - startDayOffset + 1

                    if (dayCounter in 1..lastDay) {
                        val dayData = data[dayCounter]
                        DayCell(
                            day = dayCounter,
                            income = dayData?.income ?: 0,
                            expense = dayData?.expense ?: 0,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(day: Int, income: Int, expense: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 60.dp)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$day", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        if (income > 0) {
            Text(text = "+${formatMoney(income)}", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        if (expense > 0) {
            Text(text = "-${formatMoney(expense)}", fontSize = 10.sp, color = Color.Blue, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

fun formatMoney(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}

@Composable
fun CategoryRowItem(item: CategoryExpense, totalAmount: Int) {
    val percent = (item.amount.toDouble() / totalAmount * 100).toInt()
    val formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(item.amount)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = item.icon, contentDescription = item.name, tint = item.color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$percent%", fontSize = 12.sp, color = Color.Gray)
        }
        Text(text = "${formattedAmount}원", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TopSection(totalExpense: Int) {
    val formattedTotal = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense)
    // ★ 수정: 상단 여백을 60dp -> 10dp로 축소 (위에 로고가 생겼으므로)
    Column(modifier = Modifier.padding(top = 10.dp, start = 24.dp, end = 24.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "김사용자", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = "님", fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(text = "이번 달 소비 금액", fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
            Text(text = formattedTotal, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "원", fontSize = 32.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun MonthSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFFFF))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("◀", fontSize = 18.sp, color = Color.Gray)
        Text(text = " 9월 ", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Text("▶", fontSize = 18.sp, color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}