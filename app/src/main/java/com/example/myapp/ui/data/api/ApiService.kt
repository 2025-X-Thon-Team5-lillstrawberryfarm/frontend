package com.example.myapp.ui.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // ... (기존 API들 유지)
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>
    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfileResponse>
    @GET("bank/auth-url")
    suspend fun getBankAuthUrl(): Response<BankAuthUrlResponse>
    @POST("bank/connect")
    suspend fun connectBank(@Body request: BankConnectRequest): Response<BankConnectResponse>
    @GET("transactions")
    suspend fun getTransactions(@Query("date") date: String, @Query("page") page: Int = 0): Response<TransactionListResponse>
    @GET("transactions/{id}")
    suspend fun getTransactionDetail(@Path("id") id: Int): Response<TransactionDetail>
    @GET("social/report/{userId}")
    suspend fun getOtherUserReport(@Path("userId") userId: Int): Response<OtherUserReportResponse>
    @POST("social/follow")
    suspend fun followUser(@Body targetId: Map<String, Int>): Response<Map<String, Any>>
    @GET("social/following")
    suspend fun getFollowings(): Response<List<SocialUserResponse>>
    @GET("social/followers")
    suspend fun getFollowers(): Response<List<SocialUserResponse>>
    @GET("social/group/members")
    suspend fun getGroupMembers(): Response<List<GroupMember>>

    // --- 커뮤니티 ---
    @GET("community/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("sort") sort: String = "latest"
    ): Response<List<PostResponse>>

    // ★ [수정] 상세 조회 반환 타입 변경
    @GET("community/posts/{id}")
    suspend fun getPostDetail(@Path("id") id: Int): Response<PostDetailResponse>

    @POST("community/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<CreatePostResponse>
    @POST("community/posts/{id}/comments")
    suspend fun addComment(@Path("id") postId: Int, @Body request: CommentRequest): Response<Unit>
    @POST("community/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Int): Response<Unit>

    // ★ 게시글 삭제
    @DELETE("community/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): Response<Unit>

    // --- AI ---
    @POST("chat")
    suspend fun chatWithAI(@Body request: ChatRequest): Response<ChatResponse>
    @POST("analysis/report")
    suspend fun generateReport(@Body request: ReportRequest): Response<ReportResponse>
}