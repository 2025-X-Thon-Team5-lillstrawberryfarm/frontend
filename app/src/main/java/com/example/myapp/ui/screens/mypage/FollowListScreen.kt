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

    var followList by remember { mutableStateOf<List<MateUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadData() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = if (selectedTab == 0) {
                    RetrofitClient.api.getFollowers()
                } else {
                    RetrofitClient.api.getFollowings()
                }

                if (response.isSuccessful && response.body() != null) {
                    followList = response.body()!!.map { socialUser ->
                        MateUser(
                            id = socialUser.userId,
                            // ★ 수정: 닉네임이 null이면 "알 수 없음"으로 대체
                            name = socialUser.nick ?: "알 수 없음",
                            spentAmount = 0,
                            momPercent = 0,
                            isFollowing = (selectedTab == 1)
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

    LaunchedEffect(selectedTab) {
        loadData()
    }

    if (selectedUser != null) {
        UserDetailPopup(
            user = selectedUser!!,
            onDismiss = { selectedUser = null },
            onFollowToggle = {
                coroutineScope.launch {
                    try {
                        RetrofitClient.api.followUser(mapOf("targetId" to selectedUser!!.id))
                        selectedUser = selectedUser!!.copy(isFollowing = !selectedUser!!.isFollowing)
                        loadData()
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
            // ★ 타이틀 수정: 탭에 따라 다르게 표시
            Text(
                text = if (selectedTab == 0) "팔로워" else "팔로잉",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TabItem(
                text = "팔로워",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabItem(
                text = "팔로잉",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

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

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002CCE))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                val filteredList = if (searchText.isBlank()) followList else followList.filter { it.name.contains(searchText) }

                items(filteredList) { user ->
                    FollowUserItem(
                        user = user,
                        isFollowerTab = selectedTab == 0,
                        onItemClick = { selectedUser = user },
                        onButtonClick = {
                            coroutineScope.launch {
                                try {
                                    RetrofitClient.api.followUser(mapOf("targetId" to user.id))
                                    loadData()
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