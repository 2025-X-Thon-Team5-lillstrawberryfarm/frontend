package com.example.myapp.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R

data class CommunityPost(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val group: String,
    val date: String
)

@Composable
fun CommunityScreen(
    onPostClick: (Int) -> Unit
) {
    val posts = remember { mutableStateListOf(
        CommunityPost(1, "이번 달 식비 줄이는 꿀팁 공유해요", "도시락 싸 다니니까 확실히 많이 줄어드네요. 다들 어떻게 하시나요?", "알뜰살뜰", "20만원 그룹", "09.22"),
        CommunityPost(2, "편의점 신상 드셔보신 분?", "이번에 나온 도시락 가성비 괜찮은가요? 점심으로 먹을까 고민중입니다.", "먹깨비", "30만원 그룹", "09.21"),
        CommunityPost(3, "무지출 챌린지 3일차 성공!", "오늘도 커피 유혹을 이겨냈습니다. 뿌듯하네요.", "짠테크", "10만원 그룹", "09.20"),
    ) }

    var showWriteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CommonTopBar(titleImageId = R.drawable.community_text)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(posts.reversed()) { post ->
                    PostItem(
                        post = post,
                        onClick = { onPostClick(post.id) }
                    )
                    HorizontalDivider(thickness = 1.dp, color = Color(0x4D000000))
                }
            }
        }

        FloatingActionButton(
            onClick = { showWriteDialog = true },
            containerColor = Color(0xFF002CCE),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "글쓰기")
        }
    }

    if (showWriteDialog) {
        WritePostDialog(
            onDismiss = { showWriteDialog = false },
            onConfirm = { title, content ->
                val newPost = CommunityPost(
                    id = posts.size + 1,
                    title = title,
                    content = content,
                    author = "나(사용자)",
                    group = "20만원 그룹",
                    date = SimpleDateFormat("MM.dd", Locale.US).format(Date())
                )
                posts.add(newPost)
                showWriteDialog = false
            }
        )
    }
}

@Composable
fun PostItem(post: CommunityPost, onClick: () -> Unit) {
    // ★ 파란색 정의
    val mainBlue = Color(0xFF002CCE)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = post.content, fontSize = 16.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // ★ 작성자 닉네임 색상 변경: mainBlue
            Text(text = post.author, fontSize = 14.sp, color = mainBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color(0xFF7D7D7D)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = post.group, fontSize = 14.sp, color = Color(0xFF7D7D7D))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = post.date, fontSize = 14.sp, color = Color(0xFF7D7D7D))
        }
    }
}

@Composable
fun WritePostDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("새 글 작성", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용을 입력하세요") }, modifier = Modifier.fillMaxWidth().height(150.dp), maxLines = 10)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (title.isNotBlank() && content.isNotBlank()) onConfirm(title, content) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("등록") }
                }
            }
        }
    }
}