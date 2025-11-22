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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 댓글 데이터 모델 ---
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
    val postTitle = "게시글 $postId 의 제목입니다"
    val postContent = "여기는 게시글의 상세 내용이 들어갑니다.\n사용자가 작성한 긴 글을 모두 볼 수 있습니다.\n\n줄바꿈도 잘 적용되는지 확인해봅니다."
    val postAuthor = "작성자$postId"
    val postDate = "09.22 14:30"

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(12) }

    val comments = remember { mutableStateListOf(
        Comment(1, "댓글러1", "좋은 정보 감사합니다!", "09.22 14:35"),
        Comment(2, "댓글러2", "저도 도전해봐야겠네요.", "09.22 15:00")
    ) }

    var commentInput by remember { mutableStateOf("") }

    // ★ 앱 테마 파란색 정의
    val mainBlue = Color(0xFF002CCE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- 상단 바 (수정됨) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(mainBlue) // ★ 배경색: 파란색
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onBackClick) {
                // ★ 아이콘 색상: 흰색
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
            }
            Text(
                text = "게시글 상세",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White, // ★ 텍스트 색상: 흰색
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

                    // 좋아요 버튼
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
                // ★ 전송 아이콘 색상 수정: 입력값이 있으면 파란색(mainBlue)
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