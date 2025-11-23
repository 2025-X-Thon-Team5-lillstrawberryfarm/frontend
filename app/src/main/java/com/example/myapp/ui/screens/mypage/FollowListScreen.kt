package com.example.myapp.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.myapp.ui.data.api.RetrofitClient
import com.example.myapp.ui.screens.group.MateUser
import com.example.myapp.ui.screens.group.UserDetailPopup
import kotlinx.coroutines.launch

@Composable
fun FollowListScreen(
    initialTab: Int = 0,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var selectedUser by remember { mutableStateOf<MateUser?>(null) }
    var searchText by remember { mutableStateOf("") }

    // ★ 서버 데이터 상태
    var followList by remember { mutableStateOf<List<MateUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 데이터 로딩 함수
    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = if (selectedTab == 0) {
                    // 0: 팔로워 목록 조회 (가정된 엔드포인트)
                    RetrofitClient.api.getFollowers()
                } else {
                    // 1: 팔로잉 목록 조회
                    RetrofitClient.api.getFollowings()
                }

                if (response.isSuccessful && response.body() != null) {
                    // API 응답(SocialUserResponse) -> UI 모델(MateUser) 변환
                    // 목록 API에는 소비 금액 정보가 없으므로 0으로 설정
                    followList = response.body()!!.map { socialUser ->
                        MateUser(
                            id = socialUser.userId,
                            name = socialUser.nick,
                            spentAmount = 0, // 정보 없음
                            momPercent = 0,  // 정보 없음
                            isFollowing = (selectedTab == 1) // 팔로잉 탭이면 true, 팔로워면 false(맞팔 여부 알 수 없음)
                        )
                    }
                } else {
                    followList = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 탭 변경 시 데이터 리로드
    LaunchedEffect(selectedTab) {
        loadData()
    }

    // 팝업
    if (selectedUser != null) {
        UserDetailPopup(
            user = selectedUser!!,
            onDismiss = { selectedUser = null },
            onFollowToggle = {
                // 팝업에서 팔로우 토글 시 서버 요청
                coroutineScope.launch {
                    try {
                        RetrofitClient.api.followUser(mapOf("targetId" to selectedUser!!.id))
                        selectedUser = selectedUser!!.copy(isFollowing = !selectedUser!!.isFollowing)
                        loadData() // 목록 갱신
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            // 사용자 이름은 프로필 API에서 받아오거나, 일단 "마이페이지"로 표시
            Text(
                text = "친구 목록",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 탭 버튼
        Row(modifier = Modifier.fillMaxWidth()) {
            TabItem(
                text = if (selectedTab == 0) "팔로워" else "팔로워", // 로딩 전이라 숫자 표시 어려움, 텍스트 간소화
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabItem(
                text = if (selectedTab == 1) "팔로잉" else "팔로잉",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        // 검색창
        Box(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "검색", tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchText.isEmpty()) Text("검색", fontSize = 14.sp, color = Color.Gray)
                        innerTextField()
                    }
                )
            }
        }

        // 리스트
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002CCE))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                // 검색어 필터링
                val filteredList = if (searchText.isBlank()) followList else followList.filter { it.name.contains(searchText) }

                items(filteredList) { user ->
                    FollowUserItem(
                        user = user,
                        isFollowerTab = selectedTab == 0,
                        onItemClick = { selectedUser = user },
                        onButtonClick = {
                            coroutineScope.launch {
                                try {
                                    // 팔로우/언팔로우 요청
                                    RetrofitClient.api.followUser(mapOf("targetId" to user.id))
                                    loadData() // 성공 시 목록 새로고침
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable { onClick() }.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSelected) {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Black))
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
        }
    }
}

@Composable
fun FollowUserItem(
    user: MateUser,
    isFollowerTab: Boolean,
    onItemClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    val mainBlue = Color(0xFF002CCE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = mainBlue)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "핀 메이트 회원", fontSize = 14.sp, color = Color.Gray)
        }

        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isFollowerTab) Color.White else Color(0xFFEFEFEF),
                contentColor = if (!isFollowerTab) mainBlue else Color.Black
            ),
            border = if (!isFollowerTab) androidx.compose.foundation.BorderStroke(1.dp, mainBlue) else null,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = if (isFollowerTab) "삭제" else "팔로잉",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}