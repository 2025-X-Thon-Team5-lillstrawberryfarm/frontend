package com.example.myapp.ui.screens.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.NumberFormat
import java.util.Locale
import com.example.myapp.R

@Composable
fun UserDetailPopup(
    user: MateUser,
    onDismiss: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "20만원 그룹",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 프로필 카드 섹션
                ProfileCardSection(user, onFollowToggle)

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "9월",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "지출 내역",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "총 금액",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatMoney(user.spentAmount + 120000)}원",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                CategoryRow("식비", 29.6, 96000, Color(0xFFFF8A80))
                CategoryRow("쇼핑", 45.2, 145000, Color(0xFF80D8FF))
                CategoryRow("교통", 15.0, 48000, Color(0xFFA5D6A7))
                CategoryRow("기타", 10.2, 32000, Color(0xFFFFCC80))
            }
        }
    }
}

@Composable
fun ProfileCardSection(user: MateUser, onFollowToggle: () -> Unit) {
    val formattedSpent = formatMoney(user.spentAmount)
    val momText = if (user.momPercent >= 0) "+${user.momPercent}%" else "${user.momPercent}%"

    val mainBlue = Color(0xFF002CCE)
    // ★ 요청하신 새로운 배경색 (연한 파랑)
    val cardBackgroundColor = Color(0xFF6D8EEC)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBackgroundColor) // ★ 배경색 변경 (6D8EEC)
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.quart_icon),
            contentDescription = "Quote Icon",
            modifier = Modifier.size(24.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = mainBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "이번 달 사용한 금액", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = "${formattedSpent}원", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedButton(
                onClick = onFollowToggle,
                shape = RoundedCornerShape(8.dp),
                border = if (user.isFollowing) null else BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (user.isFollowing) Color.White else Color.Transparent,
                    contentColor = if (user.isFollowing) mainBlue else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = if (user.isFollowing) "팔로잉" else "팔로우", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "지난 달 대비", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = momText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CategoryRow(name: String, percent: Double, amount: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "$percent%", fontSize = 14.sp, color = Color.Gray)
        }
        Text(
            text = "${formatMoney(amount)}원",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatMoney(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}