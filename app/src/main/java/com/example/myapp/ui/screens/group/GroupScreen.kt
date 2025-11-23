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
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R

// UI용 데이터 모델
data class MateUser(
    val id: Int,
    val name: String,
    val spentAmount: Int,
    val momPercent: Int,
    var isFollowing: Boolean
)

@Composable
fun GroupScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0: 그룹, 1: 팔로잉
    var selectedUser by remember { mutableStateOf<MateUser?>(null) }

    // ★ 하드코딩 데이터 (서버 연결 없이 바로 보여줌)
    val groupList = remember { mutableStateListOf(
        MateUser(1, "김철수", 100000, 26, false),
        MateUser(2, "이영희", 154000, 12, true),
        MateUser(3, "박민수", 89000, -5, false),
        MateUser(4, "최수진", 210000, 30, false),
        MateUser(5, "정재훈", 45000, 0, true)
    )}

    val followingList = remember { mutableStateListOf(
        MateUser(2, "이영희", 154000, 12, true),
        MateUser(5, "정재훈", 45000, 0, true),
        MateUser(14, "아이유", 550000, 5, true) // 그룹 외 팔로잉 예시
    )}

    // 로컬 팔로우 토글 함수 (서버 통신 X)
    fun toggleFollow(user: MateUser) {
        val isNowFollowing = !user.isFollowing

        // 1. 그룹 리스트 업데이트
        val groupIndex = groupList.indexOfFirst { it.id == user.id }
        if (groupIndex != -1) {
            groupList[groupIndex] = groupList[groupIndex].copy(isFollowing = isNowFollowing)
        }

        // 2. 팔로잉 리스트 업데이트
        if (isNowFollowing) {
            // 팔로우: 리스트에 없다면 추가
            if (followingList.none { it.id == user.id }) {
                followingList.add(user.copy(isFollowing = true))
            }
        } else {
            // 언팔로우: 리스트에서 제거
            val followIndex = followingList.indexOfFirst { it.id == user.id }
            if (followIndex != -1) {
                followingList.removeAt(followIndex)
            }
        }

        // 3. 팝업 상태 업데이트
        if (selectedUser?.id == user.id) {
            selectedUser = selectedUser!!.copy(isFollowing = isNowFollowing)
        }
    }

    val displayList = if (selectedTab == 0) groupList else followingList

    // 팝업
    if (selectedUser != null) {
        UserDetailPopup(
            user = selectedUser!!,
            onDismiss = { selectedUser = null },
            onFollowToggle = { toggleFollow(selectedUser!!) }
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

        // 로딩 없이 바로 리스트 표시
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayList) { user ->
                MateItemCard(
                    user = user,
                    onItemClick = { selectedUser = user },
                    onFollowClick = { toggleFollow(user) }
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
    val mainBlue = Color(0xFF002CCE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onItemClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = mainBlue,
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
            OutlinedButton(
                onClick = onFollowClick,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = if (user.isFollowing) BorderStroke(1.dp, mainBlue) else null,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (user.isFollowing) Color.White else mainBlue,
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