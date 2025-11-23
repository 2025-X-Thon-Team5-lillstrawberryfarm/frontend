package com.example.myapp.ui.data.api

// --- [1, 2] 인증 ---
data class LoginRequest(val email: String, val pw: String)
data class LoginResponse(val accessToken: String, val userId: Int)

data class SignUpRequest(val email: String, val pw: String, val nickname: String)
data class SignUpResponse(val userId: Int, val message: String)

// --- [3] 프로필 ---
data class UserProfileResponse(
    val userId: Int,
    val nick: String?,
    val clusterName: String?,
    val counts: UserCounts?
)
data class UserCounts(val post: Int, val follower: Int, val following: Int)

// --- [4, 5] 뱅킹 ---
data class BankAuthUrlResponse(val authUrl: String, val state: String, val scope: String)
data class BankConnectRequest(val kftcAuthCode: String, val scope: String)
data class BankConnectResponse(val status: String, val bankName: String, val transactions: List<TransactionDetail>)

// --- [6, 7] 거래 내역 ---
data class TransactionListResponse(val content: List<TransactionDetail>?, val last: Boolean)
data class TransactionDetail(
    val id: Int, val date: String?, val time: String?, val fullDate: String?,
    val store: String?, val type: String?, val amt: Int?, val category: String?,
    val balance: Int?, val method: String?, val memo: String?, val isExcluded: Boolean?
)

data class AddTransactionRequest(val user_id: Int, val amount: Int, val content: String, val date: String? = null)
data class AddTransactionResponse(val status: String, val category: String, val content: String)

// --- [14, 15, 16, 17] 소셜 ---
data class OtherUserReportResponse(val nick: String, val fav: String, val pattern: Map<String, Int>)
data class GroupMember(val userId: Int, val nick: String?, val spendAmount: Int?, val growthRate: Double?, val isFollowing: Boolean)
data class SocialUserResponse(val userId: Int, val nick: String?, val img: String?)

// --- [18, 19, 20] 커뮤니티 (수정됨) ---

// 1. 목록 조회용 (writer가 String)
data class PostResponse(
    val id: Int,
    val title: String,
    val content: String?,
    val writer: String?, // 목록에서는 이름만 옴
    val view: Int,
    val like: Int,
    val comment: Int,
    val createdAt: String,
    val comments: List<CommentResponse>?
)

// 2. ★ [신규] 상세 조회용 (writer가 Object)
data class PostDetailResponse(
    val id: Int,
    val title: String,
    val content: String?,
    val writer: WriterInfo?, // 상세에서는 객체로 옴
    val view: Int,
    val like: Int,
    val comment: Int,
    val createdAt: String,
    val comments: List<CommentResponse>?
)

// 상세 조회 시 작성자 정보 객체
data class WriterInfo(
    val userId: Int,
    val nick: String
)

data class CommentResponse(val user: String, val text: String)
data class CreatePostRequest(val title: String, val content: String)
data class CreatePostResponse(val postId: Int, val createdAt: String)
data class CommentRequest(val content: String)

// --- [22, 23] AI ---
data class ChatRequest(val user_id: Int, val message: String, val target_budget: Int? = null)
data class ChatResponse(val reply: String)
data class ReportRequest(val user_id: Int)
data class ReportResponse(val status: String, val report_text: String)