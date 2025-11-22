package com.example.myapp.ui.screens.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.example.myapp.ui.data.MockData
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R

data class MateUser(
    val id: Int,
    val name: String,
    val spentAmount: Int,
    val momPercent: Int,
    var isFollowing: Boolean = false
)

@Composable
fun GroupScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedUser by remember { mutableStateOf<MateUser?>(null) }

    val groupList = MockData.groupUsers
    val followingList = MockData.followings
    val displayList = if (selectedTab == 0) groupList else followingList

    if (selectedUser != null) {
        val realTimeIsFollowing = MockData.isFollowing(selectedUser!!.id)
        val userWithState = selectedUser!!.copy(isFollowing = realTimeIsFollowing)

        UserDetailPopup(
            user = userWithState,
            onDismiss = { selectedUser = null },
            onFollowToggle = {
                MockData.toggleFollow(selectedUser!!)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        CommonTopBar(titleImageId = R.drawable.fin_text)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GroupTabButton("20만원 그룹", selectedTab == 0) { selectedTab = 0 }
            Spacer(modifier = Modifier.width(16.dp))
            GroupTabButton("팔로잉", selectedTab == 1) { selectedTab = 1 }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayList) { user ->
                val isActuallyFollowing = MockData.isFollowing(user.id)

                MateItemCard(
                    user = user.copy(isFollowing = isActuallyFollowing),
                    onItemClick = { selectedUser = user },
                    onFollowClick = {
                        MockData.toggleFollow(user)
                    }
                )
            }
        }
    }
}

@Composable
fun GroupTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFD9D9D9) else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.5f)
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = fontWeight)
    }
}

@Composable
fun MateItemCard(
    user: MateUser,
    onItemClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val formattedSpent = NumberFormat.getNumberInstance(Locale.KOREA).format(user.spentAmount)
    val momText = if (user.momPercent >= 0) "+${user.momPercent}%" else "${user.momPercent}%"
    val mainBlue = Color(0xFF002CCE) // ★ 파란색 정의

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onItemClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 아이콘
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = mainBlue, // ★ 아이콘 색상 변경: 파란색
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "이번 달 사용", fontSize = 12.sp, color = Color.Gray)
            Text(text = "${formattedSpent}원", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Column(horizontalAlignment = Alignment.End) {
            // ★ 팔로우 버튼 (흰 배경에 어울리도록 스타일 적용)
            OutlinedButton(
                onClick = onFollowClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                // 팔로잉: 파란 테두리 / 팔로우: 테두리 없음(채움)
                border = if (user.isFollowing) BorderStroke(1.dp, mainBlue) else null,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    // 팔로잉: 흰 배경 / 팔로우: 파란 배경
                    containerColor = if (user.isFollowing) Color.White else mainBlue,
                    // 팔로잉: 파란 글씨 / 팔로우: 흰 글씨
                    contentColor = if (user.isFollowing) mainBlue else Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.isFollowing) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(text = if (user.isFollowing) "팔로잉" else "팔로우", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "지난 달 대비", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(
                text = momText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (user.momPercent >= 0) Color.Red else Color.Blue
            )
        }
    }
}