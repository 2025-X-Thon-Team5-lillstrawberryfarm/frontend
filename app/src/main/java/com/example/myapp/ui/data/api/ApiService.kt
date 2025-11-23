package com.example.myapp.ui.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // [2] 로그인
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // [1] 회원가입
    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    // [3] 내 프로필 조회
    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    // [6] 거래 내역 조회
    @GET("transactions")
    suspend fun getTransactions(
        @Query("date") date: String,
        @Query("page") page: Int = 0
    ): Response<TransactionListResponse>

    // [17] 그룹 멤버 목록
    @GET("social/group/members")
    suspend fun getGroupMembers(): Response<List<GroupMember>>

    // [15] 팔로우 하기
    @POST("social/follow")
    suspend fun followUser(@Body targetId: Map<String, Int>): Response<Map<String, Any>>

    // ★ [추가] [16] 팔로잉 목록
    @GET("social/following")
    suspend fun getFollowings(): Response<List<SocialUserResponse>>

    // ★ [추가] 팔로워 목록 (API 문서 누락분, 관례상 경로 추정)
    @GET("social/followers")
    suspend fun getFollowers(): Response<List<SocialUserResponse>>

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

    // [23] AI 챗봇
    @POST("api/chat")
    suspend fun chatWithAI(@Body request: ChatRequest): Response<ChatResponse>

    // [22] 월간 소비 리포트 생성
    @POST("api/analysis/report")
    suspend fun generateReport(@Body request: ReportRequest): Response<ReportResponse>
}