package com.example.myapp.ui.screens.community

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapp.ui.components.CommonTopBar
import com.example.myapp.R
import com.example.myapp.ui.data.api.CreatePostRequest
import com.example.myapp.ui.data.api.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// UI용 데이터 모델 (API 응답과 매핑)
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
    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showWriteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadPosts() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.getPosts()
                if (response.isSuccessful && response.body() != null) {
                    posts = response.body()!!.map { apiPost ->
                        CommunityPost(
                            id = apiPost.id,
                            title = apiPost.title,
                            // ★ 수정: null일 경우 빈 문자열 처리
                            content = apiPost.content ?: "",
                            // ★ 수정: writer가 null일 경우 "익명" 처리
                            author = apiPost.writer ?: "익명",
                            group = "핀메이트",
                            date = apiPost.createdAt.take(10)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        RetrofitClient.initToken(context)
        loadPosts()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CommonTopBar(titleImageId = R.drawable.community_text)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF002CCE))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            onClick = { onPostClick(post.id) }
                        )
                        HorizontalDivider(thickness = 1.dp, color = Color(0x4D000000))
                    }
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
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.api.createPost(CreatePostRequest(title, content))

                        if (response.isSuccessful) {
                            Toast.makeText(context, "글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            showWriteDialog = false
                            loadPosts()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "알 수 없는 오류"
                            Toast.makeText(context, "작성 실패(${response.code()}): $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
fun PostItem(post: CommunityPost, onClick: () -> Unit) {
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
                    Button(onClick = { if (title.isNotBlank() && content.isNotBlank()) onConfirm(title, content) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002CCE))) { Text("등록") }
                }
            }
        }
    }
}