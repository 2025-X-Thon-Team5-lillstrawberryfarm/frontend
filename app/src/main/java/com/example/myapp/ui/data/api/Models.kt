package com.example.myapp.ui.data.api

// --- [1, 2] 인증 (Auth) ---
data class LoginRequest(val email: String, val pw: String)
data class LoginResponse(val accessToken: String, val userId: Int)

data class SignUpRequest(val email: String, val pw: String, val nickname: String)
data class SignUpResponse(val userId: Int, val message: String)

// --- [3] 프로필 (Profile) ---
data class UserProfileResponse(
    val userId: Int,
    val nick: String,
    val clusterName: String?,
    val counts: UserCounts
)
data class UserCounts(val post: Int, val follower: Int, val following: Int)

// --- [6, 7] 거래 내역 (Transactions) ---
data class TransactionListResponse(
    val content: List<TransactionDetail>,
    val last: Boolean
)

data class TransactionDetail(
    val id: Int,
    val date: String,
    val time: String,
    val fullDate: String?,
    val store: String,
    val type: String,
    val amt: Int,
    val category: String,
    val balance: Int?,
    val method: String?,
    val memo: String?,
    val isExcluded: Boolean?
)

// --- [17] 그룹 멤버 (Group) ---
data class GroupMember(
    val userId: Int,
    val nick: String,
    val spendAmount: Int,
    val growthRate: Double,
    val isFollowing: Boolean
)

// ★ [추가] [16] 소셜 유저 목록 (팔로워/팔로잉)
data class SocialUserResponse(
    val userId: Int,
    val nick: String,
    val img: String?
)

// --- [18, 19, 20] 커뮤니티 (Community) ---
data class PostResponse(
    val id: Int,
    val title: String,
    val content: String?,
    val writer: String,
    val view: Int,
    val like: Int,
    val comment: Int,
    val createdAt: String,
    val comments: List<CommentResponse>?
)

data class CommentResponse(val user: String, val text: String)

data class CreatePostRequest(val title: String, val content: String)
data class CreatePostResponse(val postId: Int, val createdAt: String)

// --- [22, 23] AI 기능 (Chat & Report) ---
data class ChatRequest(val user_id: Int, val message: String, val target_budget: Int? = null)
data class ChatResponse(val reply: String)

data class ReportRequest(val user_id: Int)
data class ReportResponse(val status: String, val report_text: String)