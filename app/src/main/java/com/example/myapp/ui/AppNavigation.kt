package com.example.myapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.myapp.ui.screens.home.MainScreen
import com.example.myapp.ui.screens.group.GroupScreen
import com.example.myapp.ui.screens.community.CommunityScreen
import com.example.myapp.ui.screens.ai.AIScreen
import com.example.myapp.ui.screens.mypage.MyPageScreen
import com.example.myapp.ui.screens.community.PostDetailScreen
import com.example.myapp.ui.screens.mypage.FollowListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "홈", Icons.Default.Home)
    object Group : Screen("group", "핀 메이트", Icons.Default.Group)
    object Community : Screen("community", "커뮤니티", Icons.Default.Forum)
    object AI : Screen("ai", "AI 메이트", Icons.Default.Chat)
    object MyPage : Screen("mypage", "마이", Icons.Default.AccountCircle)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Group,
        Screen.Community,
        Screen.AI,
        Screen.MyPage
    )

    val mainBlueColor = Color(0xFF002CCE)

    Scaffold(
        bottomBar = {
            // ★ 하단바 장식: 선 + 네비게이션 바
            Column {
                // 1. 상단 경계선: 파란색 50% 투명도, 2px
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(mainBlueColor.copy(alpha = 0.5f))
                )

                // 2. 네비게이션 바
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = mainBlueColor,
                                selectedIconColor = Color.White,
                                selectedTextColor = mainBlueColor,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                MainScreen(
                    onNavigateToGroup = {
                        navController.navigate(Screen.Group.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Group.route) { GroupScreen() }
            composable(Screen.Community.route) {
                CommunityScreen(onPostClick = { postId -> navController.navigate("post_detail/$postId") })
            }
            composable(Screen.AI.route) { AIScreen() }

            composable(Screen.MyPage.route) {
                MyPageScreen(
                    onPostClick = { postId -> navController.navigate("post_detail/$postId") },
                    onFollowerClick = { navController.navigate("follow_list/0") },
                    onFollowingClick = { navController.navigate("follow_list/1") }
                )
            }

            composable(
                route = "post_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                PostDetailScreen(postId = postId, onBackClick = { navController.popBackStack() })
            }

            composable(
                route = "follow_list/{initialTab}",
                arguments = listOf(navArgument("initialTab") { type = NavType.IntType })
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                FollowListScreen(
                    initialTab = initialTab,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}