package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarChartItem
import com.example.ui.components.BarberTopAppBar
import com.example.ui.components.Formatters
import com.example.ui.components.HeroGradientCard
import com.example.ui.components.IncomeBarChart
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.screens.queue.AddQueueDialog
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyGradientEnd
import com.example.ui.theme.PrimaryNavyLight
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintBlue
import com.example.ui.theme.TintGreen
import com.example.ui.theme.TintNavy
import com.example.ui.theme.TintRed
import com.example.ui.viewmodel.BarberViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BarberViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val activeQueueCount by viewModel.activeQueueCount.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val allKapsters by viewModel.allKapsters.collectAsState()

    var showAddQueueDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour in 4..10 -> "Selamat Pagi ✂"
        currentHour in 11..14 -> "Selamat Siang ✂"
        currentHour in 15..18 -> "Selamat Sore ✂"
        else -> "Selamat Malam ✂"
    }

    val startOfDayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayTransactions = allTransactions.filter { it.transaction.timestamp >= startOfDayMillis && !it.transaction.isVoided }
    val todayRevenue = todayTransactions.sumOf { it.transaction.totalAmount }
    val todayExpenses = allExpenses.filter { it.dateTimestamp >= startOfDayMillis }.sumOf { it.amount }
    val todayKapsterCommission = todayTransactions.flatMap { it.items }.sumOf { it.kapsterCommission }
    val todayNetProfit = todayRevenue - todayExpenses - todayKapsterCommission

    // Top service today
    val serviceCounts = mutableMapOf<String, Int>()
    todayTransactions.flatMap { it.items }.filter { it.itemType == "SERVICE" }.forEach {
        serviceCounts[it.itemName] = (serviceCounts[it.itemName] ?: 0) + it.quantity
    }
    val topService = serviceCounts.maxByOrNull { it.value }?.key ?: "Potong Rambut Dewasa"

    // Top kapster today
    val kapsterCounts = mutableMapOf<String, Int>()
    todayTransactions.filter { it.transaction.kapsterName != null }.forEach {
        val name = it.transaction.kapsterName!!
        kapsterCounts[name] = (kapsterCounts[name] ?: 0) + 1
    }
    val topKapster = kapsterCounts.maxByOrNull { it.value }?.key ?: (allKapsters.firstOrNull()?.name ?: "Zain")

    // 7-day revenue trend
    val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
    val chartItems = (6 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 86400000L - 1
        val dayTotal = allTransactions
            .filter { it.transaction.timestamp in dayStart..dayEnd && !it.transaction.isVoided }
            .sumOf { it.transaction.totalAmount }

        BarChartItem(
            label = dayFormat.format(Date(dayStart)),
            value = dayTotal,
            formattedValue = Formatters.formatRupiah(dayTotal)
        )
    }

    Scaffold(
        topBar = {
            BarberTopAppBar(
                title = shopConfig.shopName,
                subtitle = greeting,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card: Total Omset Hari Ini with smooth Navy-Blue Gradient
            item {
                HeroGradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Total Omset Hari Ini",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFFE2E8F0),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = Color(0x26FFFFFF)
                            ) {
                                Text(
                                    text = Formatters.formatDate(System.currentTimeMillis()),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = Formatters.formatRupiah(todayRevenue),
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "${todayTransactions.size} Transaksi Selesai",
                                color = Color(0xFFDBEAFE),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "•",
                                color = Color(0xFF93C5FD)
                            )
                            Text(
                                text = "Laba: ${Formatters.formatRupiah(todayNetProfit)}",
                                color = Color(0xFFFCD34D),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Quick Action Buttons (52dp, rounded-full, ripple)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToPos,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("home_quick_pos_button"),
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transaksi Baru", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (shopConfig.enableQueue) {
                        Button(
                            onClick = { showAddQueueDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("home_quick_queue_button"),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Antrian", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 2x2 Grid Statistik Kecil dengan Ikon Berlatar Tint Warna
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Transaksi Selesai",
                            value = "${todayTransactions.size} Trx",
                            icon = Icons.Default.CheckCircle,
                            iconColor = AccentGreen,
                            tintColor = TintGreen
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Estimasi Laba",
                            value = Formatters.formatRupiah(todayNetProfit),
                            icon = Icons.Default.AttachMoney,
                            iconColor = AccentAmber,
                            tintColor = TintAmber
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Pengeluaran Hari Ini",
                            value = Formatters.formatRupiah(todayExpenses),
                            icon = Icons.Default.TrendingUp,
                            iconColor = AccentRed,
                            tintColor = TintRed
                        )
                        if (shopConfig.enableQueue) {
                            StatTile(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToQueue() },
                                label = "Antrian Aktif",
                                value = "$activeQueueCount Orang",
                                icon = Icons.Default.Group,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy
                            )
                        } else {
                            StatTile(
                                modifier = Modifier.weight(1f),
                                label = "Komisi Kapster",
                                value = Formatters.formatRupiah(todayKapsterCommission),
                                icon = Icons.Default.People,
                                iconColor = AccentBlue,
                                tintColor = TintBlue
                            )
                        }
                    }
                }
            }

            // Grafik 7 Hari Penjualan (dengan state "belum ada data" rapi berikon)
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tren Pendapatan 7 Hari",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Performa omset seminggu terakhir",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            TextButton(onClick = onNavigateToReports) {
                                Text("Laporan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        IncomeBarChart(items = chartItems)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Omset akhir pekan biasanya lebih ramai. Pastikan kapster dan persediaan siap.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Highlights: Layanan Terlaris & Kapster Terbaik
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TintedIconBox(
                                    icon = Icons.Default.Star,
                                    iconColor = AccentAmber,
                                    tintColor = TintAmber,
                                    size = 32.dp,
                                    iconSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Layanan Terlaris",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = topService,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                maxLines = 2
                            )
                        }
                    }

                    if (shopConfig.enableKapster) {
                        SoftCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 20.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TintedIconBox(
                                        icon = Icons.Default.ContentCut,
                                        iconColor = PrimaryNavy,
                                        tintColor = TintNavy,
                                        size = 32.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kapster Terbaik",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = topKapster,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // Low Stock Warning Alert if any
            if (shopConfig.enableStock && lowStockProducts.isNotEmpty()) {
                item {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        backgroundColor = Color(0xFFFFFBEB),
                        borderColor = Color(0xFFFDE68A)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TintedIconBox(
                                icon = Icons.Default.Warning,
                                iconColor = AccentAmber,
                                tintColor = TintAmber,
                                size = 42.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Peringatan Stok Menipis",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "${lowStockProducts.size} produk sisa sedikit: ${lowStockProducts.joinToString { "${it.name} (${it.stock})" }}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddQueueDialog) {
        AddQueueDialog(
            viewModel = viewModel,
            onDismiss = { showAddQueueDialog = false }
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    SoftCard(
        modifier = modifier,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconBox(
                icon = icon,
                iconColor = iconColor,
                tintColor = tintColor,
                size = 42.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
