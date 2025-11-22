package com.example.myapp.ui.screens.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.data.MockData
import com.example.myapp.ui.screens.group.MateUser
import com.example.myapp.ui.screens.group.UserDetailPopup

@Composable
fun FollowListScreen(
    initialTab: Int = 0,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var selectedUser by remember { mutableStateOf<MateUser?>(null) }

    val followers = MockData.followers
    val followings = MockData.followings
    val displayList = if (selectedTab == 0) followers else followings

    if (selectedUser != null) {
        UserDetailPopup(
            user = selectedUser!!,
            onDismiss = { selectedUser = null },
            onFollowToggle = {
                selectedUser!!.isFollowing = !selectedUser!!.isFollowing
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
            Text(
                text = MockData.myName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TabItem("팔로워 ${followers.size}명", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
            TabItem("팔로잉 ${followings.size}명", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
        }

        Box(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("검색", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp))
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(displayList) { user ->
                FollowUserItem(
                    user = user,
                    isFollowerTab = selectedTab == 0,
                    onItemClick = { selectedUser = user },
                    onButtonClick = {
                        if (selectedTab == 0) followers.remove(user) else followings.remove(user)
                    }
                )
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
            // ★ 프로필 아이콘: 파란색
            Icon(Icons.Default.Person, contentDescription = null, tint = mainBlue)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "핀 메이트 회원", fontSize = 14.sp, color = Color.Gray)
        }

        // ★ 버튼 스타일
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                // "팔로잉" 버튼일 때: 흰색 배경 + 파란 테두리 (인스타 느낌)
                // "삭제" 버튼일 때: 회색 배경
                containerColor = if (!isFollowerTab) Color.White else Color(0xFFEFEFEF),
                contentColor = if (!isFollowerTab) mainBlue else Color.Black
            ),
            border = if (!isFollowerTab) BorderStroke(1.dp, mainBlue) else null,
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