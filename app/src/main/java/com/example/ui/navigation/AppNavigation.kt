package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.more.data.ManageDataScreen
import com.example.ui.screens.more.salary.SalaryScreen
import com.example.ui.screens.more.settings.SettingsScreen
import com.example.ui.screens.pos.PosScreen
import com.example.ui.screens.queue.QueueScreen
import com.example.ui.screens.queue.TvQueueDisplayScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintNavy
import com.example.ui.viewmodel.BarberViewModel

@Composable
fun AppNavigation(viewModel: BarberViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTvScreen = currentRoute == Screen.TvDisplay.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 8 } },
            exitTransition = { fadeOut(animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { it / 8 } }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPos = { navController.navigate(Screen.Pos.route) },
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                )
            }
            composable(Screen.Pos.route) {
                PosScreen(viewModel = viewModel)
            }
            composable(Screen.Queue.route) {
                QueueScreen(
                    viewModel = viewModel,
                    onNavigateToTvDisplay = { navController.navigate(Screen.TvDisplay.route) },
                    onNavigateToPosCheckout = { navController.navigate(Screen.Pos.route) }
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }
            composable(Screen.More.route) {
                MoreScreen(
                    viewModel = viewModel,
                    onNavigateToManageData = { navController.navigate(Screen.ManageData.route) },
                    onNavigateToSalary = { navController.navigate(Screen.Salary.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTvDisplay = { navController.navigate(Screen.TvDisplay.route) }
                )
            }

            // Subscreens
            composable(Screen.TvDisplay.route) {
                TvQueueDisplayScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ManageData.route) {
                ManageDataScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Salary.route) {
                SalaryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Navigation Bar (displayed on main tabs)
        val isMainScreen = currentRoute in Screen.bottomNavItems.map { it.route }
        if (isMainScreen) {
            val navShape = RoundedCornerShape(28.dp)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .shadow(12.dp, navShape, spotColor = PrimaryNavy.copy(alpha = 0.25f), ambientColor = PrimaryNavy.copy(alpha = 0.15f))
                    .clip(navShape)
                    .border(1.dp, Color(0x1A1E2A78), navShape),
                shape = navShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        it,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryNavy,
                                selectedTextColor = PrimaryNavy,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = TintNavy
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    }
}
