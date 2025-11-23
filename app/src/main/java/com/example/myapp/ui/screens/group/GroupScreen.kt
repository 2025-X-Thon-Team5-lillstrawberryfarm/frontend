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
import com.example.myapp.ui.data.api.RetrofitClient // API 클라이언트
import kotlinx.coroutines.launch

// UI용 데이터 모델 (API 응답을 이 형태로 변환해서 사용)
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

    // ★ 서버 데이터 상태 관리
    var groupList by remember { mutableStateOf<List<MateUser>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<MateUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    // ★ 화면 진입 시 API 데이터 로딩
    LaunchedEffect(Unit) {
        try {
            // 그룹 멤버 조회 API 호출
            val response = RetrofitClient.api.getGroupMembers()
            if (response.isSuccessful && response.body() != null) {
                val apiMembers = response.body()!!

                // API 모델 -> UI 모델 변환
                val mappedUsers = apiMembers.map { member ->
                    MateUser(
                        id = member.userId,
                        name = member.nick,
                        spentAmount = member.spendAmount,
                        momPercent = member.growthRate.toInt(),
                        isFollowing = member.isFollowing
                    )
                }

                groupList = mappedUsers
                // 팔로잉 목록은 그룹 목록 중 팔로우한 사람만 필터링 (임시 로직)
                // 실제로는 팔로잉 목록 API (/social/following)를 따로 호출하는 것이 정확함
                followingList = mappedUsers.filter { it.isFollowing }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // 팔로우 토글 함수 (서버 통신)
    fun toggleFollow(user: MateUser) {
        coroutineScope.launch {
            try {
                // 1. UI 선반영 (Optimistic Update) - 반응 속도를 위해
                val updatedFollowingState = !user.isFollowing

                // 리스트 업데이트 (불변성 유지하며 교체)
                groupList = groupList.map {
                    if (it.id == user.id) it.copy(isFollowing = updatedFollowingState) else it
                }
                followingList = groupList.filter { it.isFollowing }

                // 팝업 유저 상태도 업데이트
                if (selectedUser?.id == user.id) {
                    selectedUser = selectedUser!!.copy(isFollowing = updatedFollowingState)
                }

                // 2. 서버 요청 (비동기)
                // API 문서: POST /social/follow { "targetId": 55 }
                RetrofitClient.api.followUser(mapOf("targetId" to user.id))

            } catch (e: Exception) {
                e.printStackTrace()
                // 실패 시 롤백 로직이 필요할 수 있음
            }
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

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002CCE))
            }
        } else {
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
        // 프로필 아이콘
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