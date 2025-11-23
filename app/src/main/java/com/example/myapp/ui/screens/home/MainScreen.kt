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
import androidx.compose.material.icons.filled.*
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
import com.example.myapp.R
import com.example.myapp.ui.data.api.RetrofitClient
import com.example.myapp.ui.data.api.TransactionDetail
import com.example.myapp.ui.data.api.UserProfileResponse
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

// --- 데이터 모델 ---
data class CategoryExpense(
    val name: String,
    val amount: Int,
    val icon: ImageVector,
    val color: Color
)

data class DailySummary(
    val day: Int,
    val income: Int,
    val expense: Int
)

private var hasShownPopupInSession = false

@Composable
fun MainScreen(
    onNavigateToGroup: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var currentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }

    var userProfile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var transactionList by remember { mutableStateOf<List<TransactionDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // type도 String?일 수 있으므로 안전하게 비교
    val totalExpense = transactionList.filter { it.type == "WITHDRAW" }.sumOf { it.amt }

    val dailyMap = remember(transactionList) {
        transactionList.groupBy {
            // ★ 수정: date가 null일 경우 안전하게 처리 (?: "0.0")
            (it.date ?: "0.0").split(".").last().toIntOrNull() ?: 0
        }.mapValues { (_, list) ->
            DailySummary(
                day = 0,
                income = list.filter { it.type == "DEPOSIT" }.sumOf { it.amt },
                expense = list.filter { it.type == "WITHDRAW" }.sumOf { it.amt }
            )
        }
    }

    val categoryStats = remember(transactionList) {
        transactionList.filter { it.type == "WITHDRAW" }
            // ★ 수정: category가 null일 경우 "기타"로 처리
            .groupBy { getCategoryDisplayName(it.category ?: "기타") }
            .map { (koreanName, list) ->
                CategoryExpense(
                    name = koreanName,
                    amount = list.sumOf { it.amt },
                    icon = getCategoryIcon(koreanName),
                    color = getCategoryColor(koreanName)
                )
            }.sortedByDescending { it.amount }
    }

    LaunchedEffect(currentYear, currentMonth) {
        isLoading = true
        RetrofitClient.initToken(context)
        try {
            if (userProfile == null) {
                val profileRes = RetrofitClient.api.getProfile()
                if (profileRes.isSuccessful) userProfile = profileRes.body()
            }

            val dateStr = String.format("%04d%02d", currentYear, currentMonth)
            val transRes = RetrofitClient.api.getTransactions(date = dateStr)
            if (transRes.isSuccessful) {
                transactionList = transRes.body()?.content ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    var showMonthlyPopup by remember { mutableStateOf(!hasShownPopupInSession) }
    var isExpanded by remember { mutableStateOf(false) }

    if (showMonthlyPopup) {
        MonthlyUpdatePopup(
            onDismiss = { showMonthlyPopup = false; hasShownPopupInSession = true },
            onConfirm = { showMonthlyPopup = false; hasShownPopupInSession = true; onNavigateToGroup() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Image(painter = painterResource(id = R.drawable.blue_logo), contentDescription = "App Logo", modifier = Modifier.height(24.dp))
        }

        TopSection(
            userName = userProfile?.nick ?: "사용자",
            totalExpense = totalExpense
        )

        Spacer(modifier = Modifier.height(24.dp))

        MonthSelector(
            year = currentYear,
            month = currentMonth,
            onPrevClick = {
                if (currentMonth == 1) { currentYear--; currentMonth = 12 } else { currentMonth-- }
            },
            onNextClick = {
                if (currentMonth == 12) { currentYear++; currentMonth = 1 } else { currentMonth++ }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        CalendarSection(
            year = currentYear,
            month = currentMonth,
            dailyData = dailyMap
        )

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("카테고리별 소비 보기", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "toggle",
                    tint = Color.Gray
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (categoryStats.isEmpty()) {
                    Text("소비 내역이 없습니다.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    categoryStats.forEach { item ->
                        CategoryRowItem(item = item, totalAmount = totalExpense)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

// --- 컴포넌트 구현 ---

@Composable
fun CalendarSection(year: Int, month: Int, dailyData: Map<Int, DailySummary>) {
    val calendar = Calendar.getInstance().apply {
        set(year, month - 1, 1)
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val blankDays = firstDayOfWeek - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
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

        val totalCells = blankDays + daysInMonth
        val rows = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val cellIndex = (row * 7) + col
                    val dayNumber = cellIndex - blankDays + 1

                    if (dayNumber in 1..daysInMonth) {
                        val summary = dailyData[dayNumber]
                        DayCell(
                            day = dayNumber,
                            income = summary?.income ?: 0,
                            expense = summary?.expense ?: 0,
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
        modifier = modifier.heightIn(min = 60.dp),
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

@Composable
fun MonthSelector(year: Int, month: Int, onPrevClick: () -> Unit, onNextClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFFFF))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevClick) {
            Text("◀", fontSize = 18.sp, color = Color.Gray)
        }
        Text(text = "$year. $month", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNextClick) {
            Text("▶", fontSize = 18.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TopSection(userName: String, totalExpense: Int) {
    val formattedTotal = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense)
    Column(modifier = Modifier.padding(top = 10.dp, start = 24.dp, end = 24.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = userName, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
fun CategoryRowItem(item: CategoryExpense, totalAmount: Int) {
    val percent = if (totalAmount > 0) (item.amount.toDouble() / totalAmount * 100).toInt() else 0
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

// 헬퍼 함수
fun getCategoryDisplayName(category: String): String {
    return when (category.uppercase()) {
        "FOOD" -> "식비"
        "TRANSPORT", "TRAFFIC" -> "교통"
        "SHOPPING" -> "쇼핑"
        "HEALTH", "MEDICAL" -> "의료/건강"
        "CULTURE" -> "문화/여가"
        "FIXED", "UTILITY" -> "공과금/고정비"
        "TRANSFER" -> "이체"
        "MART", "CONVENIENCE", "STORE" -> "편의점/마트"
        "ETC", "OTHERS" -> "기타"
        else -> category
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "식비" -> Icons.Default.Restaurant
        "교통" -> Icons.Default.DirectionsCar
        "쇼핑" -> Icons.Default.ShoppingCart
        "의료/건강" -> Icons.Default.LocalHospital
        "문화/여가" -> Icons.Default.Movie
        "공과금/고정비" -> Icons.Default.DateRange
        "이체" -> Icons.Default.Send
        "편의점/마트" -> Icons.Default.Store
        "기타" -> Icons.Default.MoreHoriz
        else -> Icons.Default.List
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "식비" -> Color(0xFFFF8A80)
        "교통" -> Color(0xFF80D8FF)
        "쇼핑" -> Color(0xFFA5D6A7)
        "의료/건강" -> Color(0xFFF48FB1)
        "문화/여가" -> Color(0xFFCE93D8)
        "공과금/고정비" -> Color(0xFFBCAAA4)
        "이체" -> Color(0xFF90CAF9)
        "편의점/마트" -> Color(0xFFFFCC80)
        "기타" -> Color(0xFFEEEEEE)
        else -> Color.Gray
    }
}

fun formatMoney(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}