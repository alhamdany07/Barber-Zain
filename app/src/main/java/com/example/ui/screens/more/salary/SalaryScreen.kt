package com.example.ui.screens.more.salary

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CashAdvanceType
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.SalaryType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.HeroGradientCard
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintNavy
import com.example.ui.viewmodel.BarberViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryScreen(
    viewModel: BarberViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val shopConfig by viewModel.shopConfig.collectAsState()
    val allKapsters by viewModel.allKapsters.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var selectedKapster by remember { mutableStateOf<KapsterEntity?>(allKapsters.firstOrNull()) }
    var selectedPeriod by remember { mutableStateOf("BULAN_INI") }
    var showAddAdvanceDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val (startTime, periodLabel) = remember(selectedPeriod) {
        when (selectedPeriod) {
            "HARIAN" -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "Hari Ini")
            }
            "MINGGUAN" -> {
                calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "Minggu Ini")
            }
            else -> {
                calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                Pair(calendar.timeInMillis, "Bulan Ini")
            }
        }
    }

    val kapster = selectedKapster ?: allKapsters.firstOrNull()

    val kapsterTransactions = allTransactions.filter {
        it.transaction.timestamp >= startTime &&
                it.transaction.kapsterId == kapster?.id &&
                !it.transaction.isVoided
    }

    val totalServices = kapsterTransactions.flatMap { it.items }.filter { it.itemType == "SERVICE" }.sumOf { it.quantity }
    val totalCommission = kapsterTransactions.flatMap { it.items }.sumOf { it.kapsterCommission }
    val baseSalary = kapster?.baseSalary ?: 0.0

    var bonusAmount by remember { mutableStateOf(0.0) }
    var kasbonAmount by remember { mutableStateOf(0.0) }
    var deductionAmount by remember { mutableStateOf(0.0) }

    val netSalary = (baseSalary + totalCommission + bonusAmount - deductionAmount - kasbonAmount).coerceAtLeast(0.0)

    // Kapster initial letter
    val initialLetter = kapster?.name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "K"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gaji & Komisi Kapster", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (allKapsters.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = "Belum Ada Kapster",
                message = "Tambahkan data kapster di Manajemen Data terlebih dahulu."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Kapster dengan Avatar Inisial & Selector Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initialLetter,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = kapster?.name ?: "-",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                val systemName = when (kapster?.salaryType) {
                                    SalaryType.PERCENTAGE -> "Bagi Hasil ${kapster.salaryRate.toInt()}% per layanan"
                                    SalaryType.FIXED_PER_SERVICE -> "${Formatters.formatRupiah(kapster.salaryRate)} per kepala"
                                    SalaryType.BASE_SALARY -> "Gaji Pokok Murni"
                                    SalaryType.BASE_PLUS_PERCENTAGE -> "Pokok + Bagi Hasil ${kapster.salaryRate.toInt()}%"
                                    else -> "-"
                                }
                                Text(
                                    text = systemName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        // Selector chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allKapsters) { k ->
                                val selected = kapster?.id == k.id
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedKapster = k },
                                    label = { Text(k.name, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) },
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
                }

                // Period Chips (Pill shape)
                item {
                    val periods = listOf("HARIAN" to "Harian", "MINGGUAN" to "Mingguan", "BULAN_INI" to "Bulanan")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                // Hero Card: Total Gaji Bersih
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
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Total Gaji Bersih",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 14.sp
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = Color(0x26FFFFFF)
                                ) {
                                    Text(
                                        text = periodLabel,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = Formatters.formatRupiah(netSalary),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$totalServices Layanan selesai dikerjakan",
                                color = Color(0xFFDBEAFE),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Rincian Gaji dalam Daftar Rapi
                item {
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Rincian Perhitungan Gaji",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            SalaryItemRow("Total Layanan Diselesaikan", "$totalServices Pelanggan")
                            SalaryItemRow("Total Komisi Bagi Hasil", Formatters.formatRupiah(totalCommission), isBold = true)
                            if (baseSalary > 0) {
                                SalaryItemRow("Gaji Pokok", Formatters.formatRupiah(baseSalary))
                            }
                            if (bonusAmount > 0) {
                                SalaryItemRow("Bonus Tambahan", "+${Formatters.formatRupiah(bonusAmount)}")
                            }
                            if (kasbonAmount > 0) {
                                SalaryItemRow("Kasbon / Pinjaman", "-${Formatters.formatRupiah(kasbonAmount)}")
                            }
                            if (deductionAmount > 0) {
                                SalaryItemRow("Potongan", "-${Formatters.formatRupiah(deductionAmount)}")
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gaji Siap Dibayarkan", fontWeight = FontWeight.Bold)
                                Text(
                                    text = Formatters.formatRupiah(netSalary),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryNavy,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action Buttons (52dp, rounded-full)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showAddAdvanceDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(99.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryNavy)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kasbon / Bonus", color = PrimaryNavy, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        shareSalarySlip(
                                            context = context,
                                            shopName = shopConfig.shopName,
                                            kapsterName = kapster?.name ?: "-",
                                            period = periodLabel,
                                            servicesCount = totalServices,
                                            commission = totalCommission,
                                            base = baseSalary,
                                            bonus = bonusAmount,
                                            deduction = deductionAmount,
                                            advance = kasbonAmount,
                                            net = netSalary
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(99.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bagikan Slip", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Recent Service Details for this Kapster
                item {
                    Text(
                        text = "Riwayat Layanan ($periodLabel)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                if (kapsterTransactions.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.Payments,
                            title = "Belum Ada Transaksi",
                            message = "Belum ada transaksi pengerjaan layanan untuk kapster ini pada periode yang dipilih."
                        )
                    }
                } else {
                    items(kapsterTransactions) { trxWithItems ->
                        val trx = trxWithItems.transaction
                        val services = trxWithItems.items.filter { it.itemType == "SERVICE" }
                        val commission = services.sumOf { it.kapsterCommission }
                        SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = services.joinToString { it.itemName },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${trx.customerName} • ${Formatters.formatDateTime(trx.timestamp)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Text(
                                    text = Formatters.formatRupiah(commission),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAdvanceDialog) {
        Dialog(onDismissRequest = { showAddAdvanceDialog = false }) {
            Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                var amountText by remember { mutableStateOf("") }
                var selectedType by remember { mutableStateOf(CashAdvanceType.KASBON) }
                var notesText by remember { mutableStateOf("") }

                Column(modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState())) {
                    Text("Catat Kasbon / Bonus / Potongan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(14.dp))

                    val types = listOf(CashAdvanceType.KASBON to "Kasbon (Pinjaman)", CashAdvanceType.BONUS to "Bonus Tambahan", CashAdvanceType.POTONGAN to "Potongan")
                    types.forEach { (type, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            androidx.compose.material3.RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Nominal (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Keterangan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showAddAdvanceDialog = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(99.dp)) { Text("Batal") }
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    when (selectedType) {
                                        CashAdvanceType.KASBON -> kasbonAmount += amt
                                        CashAdvanceType.BONUS -> bonusAmount += amt
                                        CashAdvanceType.POTONGAN -> deductionAmount += amt
                                    }
                                    if (kapster != null) {
                                        viewModel.addCashAdvance(kapster.id, amt, selectedType, notesText)
                                    }
                                    showAddAdvanceDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                        ) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalaryItemRow(label: String, value: String, isBold: Boolean = false) {
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

private fun shareSalarySlip(
    context: Context,
    shopName: String,
    kapsterName: String,
    period: String,
    servicesCount: Int,
    commission: Double,
    base: Double,
    bonus: Double,
    deduction: Double,
    advance: Double,
    net: Double
) {
    val text = buildString {
        appendLine("================================")
        appendLine("       SLIP GAJI KAPSTER        ")
        appendLine("     ${shopName.uppercase()}     ")
        appendLine("================================")
        appendLine("Nama Kapster : $kapsterName")
        appendLine("Periode      : $period")
        appendLine("Tanggal      : ${Formatters.formatDate(System.currentTimeMillis())}")
        appendLine("--------------------------------")
        appendLine("Layanan Selesai : $servicesCount")
        appendLine("Komisi Layanan  : ${Formatters.formatRupiah(commission)}")
        if (base > 0) appendLine("Gaji Pokok      : ${Formatters.formatRupiah(base)}")
        if (bonus > 0) appendLine("Bonus           : +${Formatters.formatRupiah(bonus)}")
        if (advance > 0) appendLine("Kasbon / Pinjaman : -${Formatters.formatRupiah(advance)}")
        if (deduction > 0) appendLine("Potongan        : -${Formatters.formatRupiah(deduction)}")
        appendLine("================================")
        appendLine("TOTAL GAJI DITERIMA:".padEnd(20) + Formatters.formatRupiah(net).padStart(12))
        appendLine("================================")
        appendLine("Status: SUDAH DIBAYARKAN")
        appendLine("Terima kasih atas kerja kerasnya!")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Slip Gaji $kapsterName"))
}
