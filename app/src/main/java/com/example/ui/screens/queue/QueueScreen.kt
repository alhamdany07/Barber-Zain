package com.example.ui.screens.queue

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.QueueStatus
import com.example.data.local.entity.ServiceEntity
import com.example.ui.components.BarberTopAppBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.QueueStatusChip
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintBlue
import com.example.ui.theme.TintGreen
import com.example.ui.theme.TintNavy
import com.example.ui.viewmodel.BarberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: BarberViewModel,
    onNavigateToTvDisplay: () -> Unit,
    onNavigateToPosCheckout: () -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val todayQueues by viewModel.todayQueues.collectAsState()
    val activeQueues by viewModel.activeQueues.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    val currentServing = todayQueues.firstOrNull { it.status == QueueStatus.CALLED || it.status == QueueStatus.IN_SERVICE }
    val nextWaiting = todayQueues.firstOrNull { it.status == QueueStatus.WAITING }

    Scaffold(
        topBar = {
            BarberTopAppBar(
                title = "Manajemen Antrian",
                subtitle = "${activeQueues.size} antrian aktif hari ini",
                logoUri = shopConfig.shopLogoUri,
                actions = {
                    IconButton(
                        onClick = onNavigateToTvDisplay,
                        modifier = Modifier.testTag("open_tv_display_button")
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = "Tampilan TV", tint = PrimaryNavy)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryNavy,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp) // elevate above floating bottom nav
                    .testTag("add_queue_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Antrian", modifier = Modifier.size(28.dp))
            }
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
            // Card "Sedang Dipanggil" Besar - Latar teks transparan (tanpa kotak putih di dalam)
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    backgroundColor = TintNavy,
                    borderColor = PrimaryNavy.copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentServing?.status == QueueStatus.IN_SERVICE) "SEDANG DILAYANI" else "SEDANG DIPANGGIL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    fontSize = 12.sp
                                ),
                                color = PrimaryNavy
                            )
                            if (currentServing != null) {
                                QueueStatusChip(status = currentServing.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val displayQueue = currentServing ?: nextWaiting
                        if (displayQueue != null) {
                            // Angka nomor antrian sangat besar
                            Text(
                                text = displayQueue.queueNumber,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 52.sp,
                                    color = PrimaryNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = displayQueue.customerName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${displayQueue.serviceName} • Kursi ${displayQueue.chairNumber}" +
                                        if (!displayQueue.kapsterName.isNullOrBlank()) " • ${displayQueue.kapsterName}" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Large Action Buttons (52dp, rounded-full)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (currentServing == null && nextWaiting != null) {
                                    Button(
                                        onClick = { viewModel.callQueue(nextWaiting) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("call_next_queue_button"),
                                        shape = RoundedCornerShape(99.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Panggil Berikutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                } else if (currentServing != null) {
                                    // Panggil Ulang
                                    OutlinedButton(
                                        onClick = { viewModel.callQueue(currentServing) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("recall_queue_button"),
                                        shape = RoundedCornerShape(99.dp)
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryNavy)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Panggil Ulang", color = PrimaryNavy, fontWeight = FontWeight.Bold)
                                    }

                                    if (currentServing.status == QueueStatus.CALLED) {
                                        Button(
                                            onClick = { viewModel.startServiceQueue(currentServing) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .testTag("start_service_queue_button"),
                                            shape = RoundedCornerShape(99.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Mulai Layani", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.prepareQueueForCheckout(currentServing)
                                                onNavigateToPosCheckout()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .testTag("finish_pay_queue_button"),
                                            shape = RoundedCornerShape(99.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                                        ) {
                                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Selesai & Bayar", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Semua Antrian Selesai",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tekan tombol + di bawah untuk mendaftarkan pelanggan baru",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // TV Mode Launcher Card
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = onNavigateToTvDisplay
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.Tv,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 42.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Layar TV Antrian (Fullscreen)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                                )
                                Text(
                                    text = "Tampilan landscape TV, Chromecast / Browser Smart TV",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryNavy)
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Antrian Hari Ini",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "${todayQueues.size} Total",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Queue Items
            if (todayQueues.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Group,
                        title = "Belum Ada Antrian Hari Ini",
                        message = "Tekan tombol + untuk membuat tiket antrian pelanggan pertama."
                    )
                }
            } else {
                items(todayQueues) { queue ->
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = TintNavy,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = queue.queueNumber,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PrimaryNavy,
                                                fontSize = 18.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = queue.customerName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        )
                                        Text(
                                            text = "${queue.serviceName} (~${queue.serviceDuration} mnt)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                QueueStatusChip(status = queue.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kursi ${queue.chairNumber}" + if (!queue.kapsterName.isNullOrBlank()) " • ${queue.kapsterName}" else "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { shareQueueTicket(context, queue, shopConfig.shopName) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(16.dp), tint = PrimaryNavy)
                                    }

                                    when (queue.status) {
                                        QueueStatus.WAITING -> {
                                            TextButton(onClick = { viewModel.callQueue(queue) }) {
                                                Text("Panggil", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.cancelQueue(queue) }, modifier = Modifier.size(34.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Batal", tint = AccentRose, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        QueueStatus.CALLED -> {
                                            TextButton(onClick = { viewModel.startServiceQueue(queue) }) {
                                                Text("Layani", fontWeight = FontWeight.Bold, color = AccentEmerald)
                                            }
                                        }
                                        QueueStatus.IN_SERVICE -> {
                                            Button(
                                                onClick = {
                                                    viewModel.prepareQueueForCheckout(queue)
                                                    onNavigateToPosCheckout()
                                                },
                                                shape = RoundedCornerShape(99.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text("Selesai & Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddQueueDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQueueDialog(
    viewModel: BarberViewModel,
    onDismiss: () -> Unit
) {
    val activeServices by viewModel.activeServices.collectAsState()
    val activeKapsters by viewModel.activeKapsters.collectAsState()
    val shopConfig by viewModel.shopConfig.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf(activeServices.firstOrNull()) }
    var selectedKapster by remember { mutableStateOf<KapsterEntity?>(null) }
    var chairNumber by remember { mutableStateOf("1") }

    var serviceExpanded by remember { mutableStateOf(false) }
    var kapsterExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Tambah Antrian Baru",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Nama Pelanggan (Opsional)") },
                    placeholder = { Text("Contoh: Mas Ryan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("queue_customer_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("No. HP / WA (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Service Dropdown
                ExposedDropdownMenuBox(
                    expanded = serviceExpanded,
                    onExpandedChange = { serviceExpanded = !serviceExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedService?.name ?: "Pilih Layanan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Layanan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false }
                    ) {
                        activeServices.forEach { service ->
                            DropdownMenuItem(
                                text = { Text("${service.name} (${Formatters.formatRupiah(service.price)})") },
                                onClick = {
                                    selectedService = service
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kapster & Chair
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (shopConfig.enableKapster) {
                        ExposedDropdownMenuBox(
                            expanded = kapsterExpanded,
                            onExpandedChange = { kapsterExpanded = !kapsterExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = selectedKapster?.name ?: "Bebas / Siapa saja",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kapster") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kapsterExpanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = kapsterExpanded,
                                onDismissRequest = { kapsterExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bebas / Siapa saja") },
                                    onClick = {
                                        selectedKapster = null
                                        kapsterExpanded = false
                                    }
                                )
                                activeKapsters.forEach { kap ->
                                    DropdownMenuItem(
                                        text = { Text(kap.name) },
                                        onClick = {
                                            selectedKapster = kap
                                            kapsterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = chairNumber,
                        onValueChange = { chairNumber = it },
                        label = { Text("Kursi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val service = selectedService ?: activeServices.firstOrNull()
                            if (service != null) {
                                viewModel.addCustomerToQueue(
                                    name = customerName,
                                    phone = customerPhone,
                                    service = service,
                                    kapster = selectedKapster,
                                    chair = chairNumber
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_add_queue_button"),
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Text("Daftarkan")
                    }
                }
            }
        }
    }
}

private fun shareQueueTicket(context: Context, queue: QueueEntity, shopName: String) {
    val message = """
        === NOMOR ANTRIAN ===
        $shopName
        Nomor : ${queue.queueNumber}
        Nama  : ${queue.customerName}
        Layanan: ${queue.serviceName}
        Estimasi Durasi: ~${queue.serviceDuration} Menit
        Kursi : ${queue.chairNumber}
        Waktu : ${Formatters.formatTime(queue.createdTimestamp)}
        
        Terima kasih telah menunggu!
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Nomor Antrian"))
}
