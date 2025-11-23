package com.example.myapp.ui.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // [1] 회원가입
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    // [2] 로그인
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // [3] 내 프로필 조회
    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    // [4] KFTC 토큰 발급 URL 조회 (수집)
    @GET("bank/auth-url")
    suspend fun getBankAuthUrl(): Response<BankAuthUrlResponse>

    // [5] 계좌 연동 및 내역 반환 (수집)
    @POST("bank/connect")
    suspend fun connectBank(@Body request: BankConnectRequest): Response<BankConnectResponse>

    // [6] 거래 내역 리스트 (분석)
    @GET("transactions")
    suspend fun getTransactions(
        @Query("date") date: String,
        @Query("page") page: Int = 0
    ): Response<TransactionListResponse>

    // [7] 내역 상세 (분석)
    @GET("transactions/{id}")
    suspend fun getTransactionDetail(@Path("id") id: Int): Response<TransactionDetail>

    // [14] 타인 리포트 보기 (소셜)
    @GET("social/report/{userId}")
    suspend fun getOtherUserReport(@Path("userId") userId: Int): Response<OtherUserReportResponse>

    // [15] 팔로우 하기
    @POST("social/follow")
    suspend fun followUser(@Body targetId: Map<String, Int>): Response<Map<String, Any>>

    // [16] 팔로잉 목록
    @GET("social/following")
    suspend fun getFollowings(): Response<List<SocialUserResponse>>

    // 팔로워 목록 (문서엔 없으나 기능 구현됨)
    @GET("social/followers")
    suspend fun getFollowers(): Response<List<SocialUserResponse>>

    // [17] 같은 그룹 유저 목록
    @GET("social/group/members")
    suspend fun getGroupMembers(): Response<List<GroupMember>>

    // [18] 커뮤니티 글 목록
    @GET("community/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("sort") sort: String = "latest"
    ): Response<List<PostResponse>>

    // [19] 글 상세 조회
    @GET("community/posts/{id}")
    suspend fun getPostDetail(@Path("id") id: Int): Response<PostResponse>

    // [20] 글 작성
    @POST("community/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<CreatePostResponse>

    // 댓글 작성 (커뮤니티)
    @POST("community/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: Int,
        @Body request: CommentRequest
    ): Response<Unit>

    // 글 좋아요 (커뮤니티)
    @POST("community/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Int): Response<Unit>

    // 글 삭제 (커뮤니티)
    @DELETE("community/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): Response<Unit>

    // ★ [21] 소비 내역 추가 (AI 카테고리 분류) -> 명세서대로 /api/ 포함
    @POST("api/transaction")
    suspend fun addTransaction(@Body request: AddTransactionRequest): Response<AddTransactionResponse>

    // ★ [22] 월간 소비 리포트 (AI) -> 명세서대로 /api/ 포함
    @POST("api/analysis/report")
    suspend fun generateReport(@Body request: ReportRequest): Response<ReportResponse>

    // ★ [23] AI 금융 비서 (챗봇) -> 명세서대로 /api/ 포함
    @POST("api/chat")
    suspend fun chatWithAI(@Body request: ChatRequest): Response<ChatResponse>
}