package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Pos : Screen("pos", "Kasir", Icons.Default.PointOfSale)
    object Queue : Screen("queue", "Antrian", Icons.Default.People)
    object Reports : Screen("reports", "Laporan", Icons.Default.Assessment)
    object More : Screen("more", "Lainnya", Icons.Default.MoreHoriz)

    // Sub-screens
    object TvDisplay : Screen("tv_display", "Layar TV Antrian")
    object ManageData : Screen("manage_data", "Manajemen Data")
    object Salary : Screen("salary", "Gaji & Komisi Kapster")
    object Settings : Screen("settings", "Pengaturan & Backup")

    companion object {
        val bottomNavItems = listOf(Home, Pos, Queue, Reports, More)
    }
}
