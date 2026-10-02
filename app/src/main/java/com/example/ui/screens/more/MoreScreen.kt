package com.example.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarberTopAppBar
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintNavy
import com.example.ui.viewmodel.BarberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: BarberViewModel,
    onNavigateToManageData: () -> Unit,
    onNavigateToSalary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTvDisplay: () -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()

    Scaffold(
        topBar = {
            BarberTopAppBar(
                title = "Menu Lainnya",
                subtitle = "Manajemen data, komisi & pengaturan",
                logoUri = shopConfig.shopLogoUri
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "OPERASIONAL BARBERSHOP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    ),
                    color = PrimaryNavy,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Manajemen Data Master
            item {
                MenuCard(
                    title = "Manajemen Data Master",
                    description = "Kelola Layanan, Produk, Kapster, Pelanggan & Pengeluaran",
                    icon = Icons.Default.Storage,
                    iconBg = TintNavy,
                    iconColor = PrimaryNavy,
                    testTag = "menu_manage_data",
                    onClick = onNavigateToManageData
                )
            }

            // Gaji & Komisi Kapster
            if (shopConfig.enableKapster) {
                item {
                    MenuCard(
                        title = "Gaji & Komisi Kapster",
                        description = "Bagi hasil, kasbon, bonus, potongan & slip gaji",
                        icon = Icons.Default.Payments,
                        iconBg = TintAmber,
                        iconColor = AccentAmber,
                        testTag = "menu_salary",
                        onClick = onNavigateToSalary
                    )
                }
            }

            // Tampilan TV Antrian
            if (shopConfig.enableQueue) {
                item {
                    MenuCard(
                        title = "Layar TV Antrian (Fullscreen)",
                        description = "Tampilan antrian untuk monitor TV, HDMI & browser TV",
                        icon = Icons.Default.Tv,
                        iconBg = TintNavy,
                        iconColor = PrimaryNavy,
                        testTag = "menu_tv_display",
                        onClick = onNavigateToTvDisplay
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SISTEM & KONFIGURASI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    ),
                    color = PrimaryNavy,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Pengaturan & Backup
            item {
                MenuCard(
                    title = "Pengaturan & Backup",
                    description = "Profil toko, logo galeri, tema, TV server lokal & backup database",
                    icon = Icons.Default.Settings,
                    iconBg = Color(0xFFE2E8F0),
                    iconColor = Color(0xFF334155),
                    testTag = "menu_settings",
                    onClick = onNavigateToSettings
                )
            }

            // App Version Badge
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${shopConfig.shopName} POS v1.0",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Aplikasi Kasir & Antrian Barbershop 100% Offline (Room Database)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        cornerRadius = 24.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconBox(
                icon = icon,
                iconColor = iconColor,
                tintColor = iconBg,
                size = 46.dp,
                iconSize = 24.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
