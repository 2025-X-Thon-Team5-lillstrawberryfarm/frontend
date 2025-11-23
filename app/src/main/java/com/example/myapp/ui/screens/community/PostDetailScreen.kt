package com.example.myapp.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 댓글 데이터 모델 (UI용) ---
data class Comment(
    val id: Int,
    val author: String,
    val content: String,
    val date: String
)

@Composable
fun PostDetailScreen(
    postId: Int,
    onBackClick: () -> Unit
) {
    // 상태 변수들
    var postTitle by remember { mutableStateOf("로딩 중...") }
    var postContent by remember { mutableStateOf("") }
    var postAuthor by remember { mutableStateOf("") }
    var postDate by remember { mutableStateOf("") }

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }

    // 댓글 리스트
    var comments = remember { mutableStateListOf<Comment>() }

    var commentInput by remember { mutableStateOf("") }
    val mainBlue = Color(0xFF002CCE)
    val coroutineScope = rememberCoroutineScope()

    // ★ 서버에서 상세 정보 로딩
    LaunchedEffect(postId) {
        try {
            val response = RetrofitClient.api.getPostDetail(postId)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                postTitle = data.title
                postContent = data.content ?: ""
                postAuthor = data.writer
                postDate = data.createdAt.take(10)
                likeCount = data.like
                // 댓글 매핑 (API 응답에는 날짜/ID가 없으므로 임시값 처리)
                if (data.comments != null) {
                    data.comments.forEachIndexed { index, commentRes ->
                        comments.add(Comment(index, commentRes.user, commentRes.text, "최근"))
                    }
                }
            }
        } catch (e: Exception) {
            postTitle = "글을 불러오지 못했습니다."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- 상단 바 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(mainBlue)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
            }
            Text(
                text = "게시글 상세",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- 메인 컨텐츠 ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = postTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = postAuthor, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "|", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = postDate, fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = postContent,
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // 좋아요 버튼 (기능은 UI 상에서만 동작하도록 둠)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLiked = !isLiked
                                if (isLiked) likeCount++ else likeCount--
                            }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "좋아요",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "좋아요 $likeCount", color = Color.Black)
                    }
                }
                Divider(thickness = 8.dp, color = Color(0xFFF5F5F5))
            }

            item {
                Text(
                    text = "댓글 ${comments.size}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
                )
            }

            items(comments) { comment ->
                CommentItem(comment)
                Divider(color = Color(0xFFF5F5F5))
            }
        }

        // --- 하단 댓글 입력창 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (commentInput.isEmpty()) Text("댓글을 입력하세요...", color = Color.Gray)
                    innerTextField()
                }
            )
            IconButton(
                onClick = {
                    if (commentInput.isNotBlank()) {
                        // 댓글 작성 API가 있다면 여기서 호출
                        // 현재는 로컬 리스트에만 추가
                        comments.add(
                            Comment(
                                id = comments.size + 1,
                                author = "나",
                                content = commentInput,
                                date = SimpleDateFormat("MM.dd HH:mm", Locale.US).format(Date())
                            )
                        )
                        commentInput = ""
                    }
                }
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "등록",
                    tint = if (commentInput.isNotBlank()) mainBlue else Color.Gray
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = comment.author, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = comment.date, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = comment.content, fontSize = 15.sp)
    }
}