package com.example.myapp.ui.screens.mypage

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R
import com.example.myapp.ui.data.api.RetrofitClient
import kotlinx.coroutines.launch

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
    var userName by remember { mutableStateOf("로딩중...") }
    var followerCount by remember { mutableIntStateOf(0) }
    var followingCount by remember { mutableIntStateOf(0) }
    var myPosts by remember { mutableStateOf<List<MyPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        RetrofitClient.initToken(context)
        try {
            // 프로필 정보
            val profileRes = RetrofitClient.api.getProfile()
            if (profileRes.isSuccessful && profileRes.body() != null) {
                val profile = profileRes.body()!!
                userName = profile.nick
                followerCount = profile.counts.follower
                followingCount = profile.counts.following
            }

            // 내 글 목록
            val postsRes = RetrofitClient.api.getPosts()
            if (postsRes.isSuccessful && postsRes.body() != null) {
                val allPosts = postsRes.body()!!
                myPosts = allPosts.filter { it.writer == userName }.map { apiPost ->
                    MyPost(
                        id = apiPost.id,
                        title = apiPost.title,
                        content = apiPost.content ?: "",
                        date = apiPost.createdAt.take(10)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            userName = "오류 발생"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        CommonTopBar(titleImageId = R.drawable.my_text)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002CCE))
            }
        } else {
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

                if (myPosts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("작성한 글이 없습니다.", color = Color.Gray)
                        }
                    }
                } else {
                    items(myPosts) { post ->
                        MyPostItem(
                            post = post,
                            onClick = { onPostClick(post.id) },
                            onDeleteClick = {
                                // ★ 글 삭제 API 호출
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.api.deletePost(post.id)
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                            // 로컬 리스트에서 제거하여 UI 즉시 반영
                                            myPosts = myPosts.filter { it.id != post.id }
                                        } else {
                                            Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
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
    val itemBackgroundColor = Color(0xFF6D8EEC)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(itemBackgroundColor)
            .clickable { onClick() }
            .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clickable { onDeleteClick() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(14.dp), tint = mainBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "삭제하기", fontSize = 12.sp, color = mainBlue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content, fontSize = 16.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}