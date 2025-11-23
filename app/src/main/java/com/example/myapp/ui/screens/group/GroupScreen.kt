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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R
import com.example.myapp.ui.data.api.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

// 데이터 모델 (안전한 초기화)
data class MateUser(
    val id: Int,
    val name: String,
    val spentAmount: Int,
    val momPercent: Int,
    var isFollowing: Boolean
)

@Composable
fun GroupScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedUser by remember { mutableStateOf<MateUser?>(null) }

    var groupList by remember { mutableStateOf<List<MateUser>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<MateUser>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // ★ 에러 메시지 상태 추가

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null // 에러 초기화
            try {
                RetrofitClient.initToken(context)

                // 병렬 호출
                val groupDeferred = async { RetrofitClient.api.getGroupMembers() }
                val followingDeferred = async { RetrofitClient.api.getFollowings() }

                val groupRes = groupDeferred.await()
                val followingRes = followingDeferred.await()

                // 1. 내 팔로잉 ID 목록 추출 (안전 처리)
                val myFollowingIds = if (followingRes.isSuccessful && followingRes.body() != null) {
                    followingRes.body()!!.mapNotNull { it.userId }.toSet() // mapNotNull 사용
                } else {
                    emptySet()
                }

                // 2. 그룹 멤버 리스트 구성 (초강력 안전 처리)
                val mappedGroupUsers = if (groupRes.isSuccessful && groupRes.body() != null) {
                    groupRes.body()!!.mapNotNull { member ->
                        // 필수 데이터가 없으면 해당 유저는 목록에서 제외 (앱 종료 방지)
                        if (member.nick == null) return@mapNotNull null

                        MateUser(
                            id = member.userId,
                            name = member.nick,
                            spentAmount = member.spendAmount ?: 0,
                            momPercent = member.growthRate?.toInt() ?: 0,
                            isFollowing = member.userId in myFollowingIds
                        )
                    }
                } else {
                    emptyList()
                }

                groupList = mappedGroupUsers

                // 3. 팔로잉 리스트 구성
                val followingInGroup = mappedGroupUsers.filter { it.isFollowing }

                val otherFollowings = if (followingRes.isSuccessful && followingRes.body() != null) {
                    followingRes.body()!!
                        .filter { it.userId !in mappedGroupUsers.map { u -> u.id } }
                        .mapNotNull { socialUser ->
                            if (socialUser.nick == null) return@mapNotNull null
                            MateUser(
                                id = socialUser.userId,
                                name = socialUser.nick,
                                spentAmount = 0,
                                momPercent = 0,
                                isFollowing = true
                            )
                        }
                } else {
                    emptyList()
                }

                followingList = (followingInGroup + otherFollowings).sortedBy { it.name }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "데이터를 불러오는 중 오류가 발생했습니다.\n(${e.message})" // 화면에 에러 표시
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    fun toggleFollow(user: MateUser) {
        coroutineScope.launch {
            try {
                RetrofitClient.api.followUser(mapOf("targetId" to user.id))
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val displayList = if (selectedTab == 0) groupList else followingList

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
        // ★ 상단바 (이미지가 없어도 앱이 죽지 않도록 예외처리 내장된 CommonTopBar 사용 권장)
        // 만약 여기서 또 죽으면 R.drawable.fin_text를 확인하거나 CommonTopBar 내부를 텍스트로 바꿔야 함
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
        } else if (errorMessage != null) {
            // ★ 에러 발생 시 앱 종료 대신 에러 메시지 표시
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) "그룹 멤버가 없습니다." else "팔로잉 중인 멤버가 없습니다.",
                        color = Color.Gray
                    )
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