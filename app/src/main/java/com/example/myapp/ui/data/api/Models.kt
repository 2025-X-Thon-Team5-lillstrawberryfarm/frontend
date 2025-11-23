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

// --- [4, 5] 뱅킹 (Bank) ---
data class BankAuthUrlResponse(val authUrl: String, val state: String, val scope: String)
data class BankConnectRequest(val kftcAuthCode: String, val scope: String)
data class BankConnectResponse(val status: String, val bankName: String, val transactions: List<TransactionDetail>)

// --- [6, 7, 21] 거래 내역 (Transactions) ---
data class TransactionListResponse(
    val content: List<TransactionDetail>,
    val last: Boolean
)

data class TransactionDetail(
    val id: Int,
    val date: String?, // 목록용 "01.20"
    val time: String?, // 목록용 "14:30"
    val fullDate: String?, // 상세용 "2025.01.20 14:30"
    val store: String,
    val type: String?, // "WITHDRAW"
    val amt: Int, // 금액 (목록/상세 공통)
    val category: String?,
    val balance: Int?, // 상세용
    val method: String?, // 상세용
    val memo: String?, // 상세용
    val isExcluded: Boolean? // 상세용
)

// [21] 소비 내역 수동 추가
data class AddTransactionRequest(
    val user_id: Int,
    val amount: Int,
    val content: String,
    val date: String? = null // 없으면 현재 시간
)
data class AddTransactionResponse(val status: String, val category: String, val content: String)

// --- [14, 15, 16, 17] 소셜/그룹 (Social) ---
data class GroupMember(
    val userId: Int,
    val nick: String?,
    val spendAmount: Int?,
    val growthRate: Double?,
    val isFollowing: Boolean
)

data class SocialUserResponse(
    val userId: Int,
    val nick: String?,
    val img: String?
)

data class OtherUserReportResponse(
    val nick: String,
    val fav: String,
    val pattern: Map<String, Int> // {"식비": 40, "쇼핑": 30}
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

data class CommentRequest(val content: String)

// --- [22, 23] AI 기능 (Chat & Report) ---
data class ChatRequest(val user_id: Int, val message: String, val target_budget: Int? = null)
data class ChatResponse(val reply: String)

data class ReportRequest(val user_id: Int)
data class ReportResponse(val status: String, val report_text: String)