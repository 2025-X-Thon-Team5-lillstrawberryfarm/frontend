package com.example.myapp.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.data.MockData
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R

data class MyPost(
    val id: Int,
    val title: String,
    val content: String,
    val date: String
)

@Composable
fun MyPageScreen(
    onPostClick: (Int) -> Unit,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    val userName = MockData.myName
    val followerCount = MockData.followers.size
    val followingCount = MockData.followings.size

    val myPosts = remember { mutableStateListOf(
        MyPost(1, "글제목 입니다.", "내용 미리보기 입니다\n두줄까지 보이게 하면 좋지 않을...", "2024.09.22"),
        MyPost(2, "무지출 챌린지 성공 후기", "일주일 동안 커피값 아껴서 3만원 모았습니다.", "2024.09.20"),
        MyPost(3, "편의점 도시락 추천", "이번 신상 진짜 맛있네요. 가격도 저렴하고...", "2024.09.18")
    ) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        CommonTopBar(titleImageId = R.drawable.my_text)

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                ProfileSection(
                    name = userName,
                    followers = followerCount,
                    followings = followingCount,
                    onFollowerClick = onFollowerClick,
                    onFollowingClick = onFollowingClick
                )
                HorizontalDivider(thickness = 1.dp, color = Color.Black.copy(alpha = 0.1f))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("내가 쓴 글", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            items(myPosts) { post ->
                MyPostItem(
                    post = post,
                    onClick = { onPostClick(post.id) },
                    onDeleteClick = { myPosts.remove(post) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileSection(
    name: String,
    followers: Int,
    followings: Int,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    val mainBlue = Color(0xFF002CCE)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = mainBlue
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onFollowerClick() }
            ) {
                Text(text = "$followers", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "팔로워", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onFollowingClick() }
            ) {
                Text(text = "$followings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "팔로잉", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MyPostItem(
    post: MyPost,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val mainBlue = Color(0xFF002CCE)
    // ★ 요청하신 새로운 배경색 (연한 파랑)
    val itemBackgroundColor = Color(0xFF6D8EEC)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)) // 전체 둥글게
            .background(itemBackgroundColor) // ★ 배경색 변경 (6D8EEC)
            .clickable { onClick() }
            .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                // ★ 제목 색상 변경: 흰색
                Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clickable { onDeleteClick() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ★ 삭제 아이콘 및 텍스트 색상 변경: 기존 파란색 (002CCE)
                    Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(14.dp), tint = mainBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "삭제하기", fontSize = 12.sp, color = mainBlue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // ★ 내용 텍스트 색상 변경: 흰색
            Text(text = post.content, fontSize = 16.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}