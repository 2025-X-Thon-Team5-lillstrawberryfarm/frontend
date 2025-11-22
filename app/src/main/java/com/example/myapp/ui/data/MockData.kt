package com.example.myapp.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.example.myapp.ui.screens.group.MateUser

object MockData {
    const val myName = "김사용자"

    // 1. 전체 그룹원 리스트 (고정된 데이터)
    val groupUsers = mutableStateListOf(
        MateUser(1, "김철수", 100000, 26, false), // 초기값 false여도 아래 로직으로 덮어씌워짐
        MateUser(2, "이영희", 154000, 12, false),
        MateUser(3, "박민수", 89000, -5, false),
        MateUser(4, "최수진", 210000, 30, false),
        MateUser(5, "정재훈", 45000, 0, false)
    )

    // 2. 팔로워 리스트 (나를 팔로우하는 사람들)
    val followers = mutableStateListOf(
        MateUser(10, "김철수", 120000, 10, false),
        MateUser(11, "박지성", 45000, -5, true),
        MateUser(12, "손흥민", 300000, 20, true),
        MateUser(13, "이강인", 15000, 0, false)
    )

    // 3. 팔로잉 리스트 (내가 팔로우하는 사람들)
    // ★ 이 리스트가 '내가 누구를 팔로우했는지'를 판단하는 기준입니다.
    val followings = mutableStateListOf(
        MateUser(11, "박지성", 45000, -5, true),
        MateUser(12, "손흥민", 300000, 20, true),
        MateUser(14, "아이유", 550000, 5, true)
    )

    // ★ 핵심 기능: 팔로우 토글 (추가/삭제)
    // 어느 화면에서든 이 함수를 호출하면 동기화가 해결됩니다.
    fun toggleFollow(user: MateUser) {
        val existingUser = followings.find { it.id == user.id }
        if (existingUser != null) {
            // 이미 팔로우 중이면 -> 삭제 (언팔로우)
            followings.remove(existingUser)
        } else {
            // 팔로우 중이 아니면 -> 추가 (팔로우)
            // (isFollowing = true로 강제 설정하여 추가)
            followings.add(user.copy(isFollowing = true))
        }
    }

    // ★ 확인 기능: 특정 유저를 내가 팔로우 중인지 확인
    fun isFollowing(userId: Int): Boolean {
        return followings.any { it.id == userId }
    }
}