package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarChartItem
import com.example.ui.components.BarberTopAppBar
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DonutSlice
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.IncomeBarChart
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
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
fun ReportsScreen(viewModel: BarberViewModel) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf("7_HARI") } // HARI_INI, 7_HARI, BULAN_INI, SEMUA

    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    val (startTime, periodLabel) = remember(selectedPeriod, now) {
        when (selectedPeriod) {
            "HARI_INI" -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "Hari Ini")
            }
            "7_HARI" -> {
                calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "7 Hari Terakhir")
            }
            "BULAN_INI" -> {
                calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "Bulan Ini")
            }
            else -> Pair(0L, "Semua Waktu")
        }
    }

    val periodTransactions = allTransactions.filter { it.transaction.timestamp >= startTime && !it.transaction.isVoided }
    val periodExpenses = allExpenses.filter { it.dateTimestamp >= startTime }

    val hasTransactions = periodTransactions.isNotEmpty()
    val grossIncome = periodTransactions.sumOf { it.transaction.subtotal }
    val totalDiscount = periodTransactions.sumOf { it.transaction.discountAmount }
    val netSales = periodTransactions.sumOf { it.transaction.totalAmount }
    val totalExpenses = periodExpenses.sumOf { it.amount }
    val totalCommission = periodTransactions.flatMap { it.items }.sumOf { it.kapsterCommission }
    val totalHpp = periodTransactions.flatMap { it.items }.filter { it.itemType == "PRODUCT" }.sumOf { it.costPrice * it.quantity }
    val netProfit = netSales - totalExpenses - totalCommission - totalHpp

    // Donut chart slices
    val serviceRevenue = periodTransactions.flatMap { it.items }.filter { it.itemType == "SERVICE" }.sumOf { it.price * it.quantity }
    val productRevenue = periodTransactions.flatMap { it.items }.filter { it.itemType == "PRODUCT" }.sumOf { it.price * it.quantity }
    val donutSlices = listOf(
        DonutSlice("Layanan Pangkas", serviceRevenue, PrimaryNavy),
        DonutSlice("Produk & Pomade", productRevenue, AccentAmber)
    )

    // Daily Bar Chart
    val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val chartDaysCount = if (selectedPeriod == "HARI_INI") 1 else 7
    val barChartItems = (chartDaysCount - 1 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val s = cal.timeInMillis
        val e = s + 86400000L - 1
        val dailyTotal = allTransactions
            .filter { it.transaction.timestamp in s..e && !it.transaction.isVoided }
            .sumOf { it.transaction.totalAmount }
        BarChartItem(
            label = dayFormat.format(Date(s)),
            value = dailyTotal,
            formattedValue = Formatters.formatRupiah(dailyTotal)
        )
    }

    // Top selling items
    val itemQuantities = mutableMapOf<String, Pair<Int, Double>>()
    periodTransactions.flatMap { it.items }.forEach { item ->
        val existing = itemQuantities[item.itemName] ?: Pair(0, 0.0)
        itemQuantities[item.itemName] = Pair(existing.first + item.quantity, existing.second + (item.price * item.quantity))
    }
    val topItems = itemQuantities.toList().sortedByDescending { it.second.second }.take(5)

    // Payment methods
    val paymentBreakdown = periodTransactions.groupBy { it.transaction.paymentMethod }

    Scaffold(
        topBar = {
            BarberTopAppBar(
                title = "Laporan & Analisis",
                subtitle = "Ringkasan keuangan & laba rugi",
                logoUri = shopConfig.shopLogoUri,
                actions = {
                    IconButton(
                        onClick = {
                            exportReport(
                                context = context,
                                shopName = shopConfig.shopName,
                                period = periodLabel,
                                gross = grossIncome,
                                discount = totalDiscount,
                                sales = netSales,
                                expense = totalExpenses,
                                commission = totalCommission,
                                hpp = totalHpp,
                                net = netProfit,
                                trxCount = periodTransactions.size,
                                topItems = topItems
                            )
                        },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor Laporan", tint = PrimaryNavy)
                    }
                }
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
            // Period Filter Chips (Pill shape)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val periods = listOf(
                        "HARI_INI" to "Hari Ini",
                        "7_HARI" to "7 Hari",
                        "BULAN_INI" to "Bulan Ini",
                        "SEMUA" to "Semua"
                    )
                    items(periods) { (key, label) ->
                        val selected = selectedPeriod == key
                        FilterChip(
                            selected = selected,
                            onClick = { selectedPeriod = key },
                            label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) },
                            shape = RoundedCornerShape(99.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryNavy,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) PrimaryNavy else BorderSubtleLight
                            )
                        )
                    }
                }
            }

            // Summary Card (Netral Soft Card tanpa alarm merah tebal)
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Laporan Laba Bersih",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = TintNavy
                            ) {
                                Text(
                                    text = periodLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryNavy,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Netral Inner Box: warna merah/hijau hanya pada angka laba rugi saat ada data
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ESTIMASI LABA BERSIH",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (hasTransactions) Formatters.formatRupiah(netProfit) else "Rp 0",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 28.sp,
                                            color = when {
                                                !hasTransactions -> MaterialTheme.colorScheme.onSurface
                                                netProfit >= 0 -> AccentGreen
                                                else -> AccentRed
                                            }
                                        )
                                    )
                                }

                                TintedIconBox(
                                    icon = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    iconColor = if (netProfit >= 0) AccentGreen else AccentRed,
                                    tintColor = if (netProfit >= 0) TintGreen else TintRed,
                                    size = 46.dp,
                                    iconSize = 24.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Breakdown lines
                        ReportSummaryRow("Total Penjualan Bersih", Formatters.formatRupiah(netSales), isBold = true)
                        ReportSummaryRow("  • Total Diskon Diberikan", "-${Formatters.formatRupiah(totalDiscount)}")
                        ReportSummaryRow("Pengeluaran Operasional", "-${Formatters.formatRupiah(totalExpenses)}")
                        ReportSummaryRow("Komisi Bagi Hasil Kapster", "-${Formatters.formatRupiah(totalCommission)}")
                        if (shopConfig.enableStock) {
                            ReportSummaryRow("Modal Produk Terjual (HPP)", "-${Formatters.formatRupiah(totalHpp)}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Transaksi Selesai",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${periodTransactions.size} Transaksi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // Grafik Tren Penjualan Harian
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Tren Penjualan Harian",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        IncomeBarChart(items = barChartItems)
                    }
                }
            }

            // Komposisi Penjualan (Donut Chart)
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Komposisi Penjualan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CategoryDonutChart(slices = donutSlices)
                    }
                }
            }

            // Layanan & Produk Terlaris
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Layanan & Produk Terlaris",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (topItems.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.ReceiptLong,
                                title = "Belum Ada Transaksi",
                                message = "Belum ada item terjual pada periode ini."
                            )
                        } else {
                            topItems.forEachIndexed { idx, (name, data) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = TintNavy,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryNavy
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                )
                                            )
                                            Text(
                                                text = "${data.first} Terjual",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = Formatters.formatRupiah(data.second),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PrimaryNavy
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Metode Pembayaran
            if (paymentBreakdown.isNotEmpty()) {
                item {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Rincian Metode Pembayaran",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            paymentBreakdown.forEach { (method, list) ->
                                val total = list.sumOf { it.transaction.totalAmount }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$method (${list.size} trx)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                                    )
                                    Text(
                                        text = Formatters.formatRupiah(total),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PrimaryNavy
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Export Action Button (52dp, rounded-full)
            item {
                Button(
                    onClick = {
                        exportReport(
                            context = context,
                            shopName = shopConfig.shopName,
                            period = periodLabel,
                            gross = grossIncome,
                            discount = totalDiscount,
                            sales = netSales,
                            expense = totalExpenses,
                            commission = totalCommission,
                            hpp = totalHpp,
                            net = netProfit,
                            trxCount = periodTransactions.size,
                            topItems = topItems
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(99.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan / Ekspor Laporan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun exportReport(
    context: Context,
    shopName: String,
    period: String,
    gross: Double,
    discount: Double,
    sales: Double,
    expense: Double,
    commission: Double,
    hpp: Double,
    net: Double,
    trxCount: Int,
    topItems: List<Pair<String, Pair<Int, Double>>>
) {
    val text = buildString {
        appendLine("========================================")
        appendLine("      LAPORAN KEUANGAN BARBERSHOP       ")
        appendLine("          $shopName          ")
        appendLine("Periode: $period")
        appendLine("Tanggal Cetak: ${Formatters.formatDateTime(System.currentTimeMillis())}")
        appendLine("========================================")
        appendLine("Jumlah Transaksi  : $trxCount")
        appendLine("Total Penjualan   : ${Formatters.formatRupiah(sales)}")
        appendLine("Total Diskon      : ${Formatters.formatRupiah(discount)}")
        appendLine("Pengeluaran Ops   : ${Formatters.formatRupiah(expense)}")
        appendLine("Komisi Kapster    : ${Formatters.formatRupiah(commission)}")
        appendLine("Modal HPP Produk  : ${Formatters.formatRupiah(hpp)}")
        appendLine("----------------------------------------")
        appendLine("LABA BERSIH       : ${Formatters.formatRupiah(net)}")
        appendLine("========================================")
        appendLine("Layanan & Produk Terlaris:")
        topItems.forEachIndexed { i, (name, data) ->
            appendLine("${i + 1}. $name (${data.first}x) = ${Formatters.formatRupiah(data.second)}")
        }
        appendLine("========================================")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan Barbershop"))
}
