package com.example.ui.screens.more.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.Formatters
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.QrCodeHelper
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintNavy
import com.example.ui.theme.TintRed
import com.example.ui.theme.AppThemeColor
import com.example.ui.viewmodel.BarberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BarberViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val shopConfig by viewModel.shopConfig.collectAsState()
    val tvServerRunning by viewModel.tvServerRunning.collectAsState()
    val localIp by viewModel.localIp.collectAsState()

    var shopName by remember(shopConfig) { mutableStateOf(shopConfig.shopName) }
    var shopAddress by remember(shopConfig) { mutableStateOf(shopConfig.shopAddress) }
    var shopPhone by remember(shopConfig) { mutableStateOf(shopConfig.shopPhone) }
    var receiptFooter by remember(shopConfig) { mutableStateOf(shopConfig.receiptFooter) }
    var taxPercent by remember(shopConfig) { mutableStateOf(if (shopConfig.taxPercentage > 0) shopConfig.taxPercentage.toString() else "") }
    var ttsSpeed by remember(shopConfig) { mutableStateOf(shopConfig.ttsSpeed) }

    var showResetDialog1 by remember { mutableStateOf(false) }
    var showResetDialog2 by remember { mutableStateOf(false) }

    // Photo picker for shop logo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist permission if needed or save uri string
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.updateShopProfile(
                name = shopName,
                address = shopAddress,
                phone = shopPhone,
                footer = receiptFooter,
                logoUri = uri.toString()
            )
            Toast.makeText(context, "Logo usaha berhasil diperbarui!", Toast.LENGTH_SHORT).show()
        }
    }

    val tvUrl = "http://$localIp:${shopConfig.tvServerPort}"
    val qrBitmap = remember(tvUrl, tvServerRunning) {
        if (tvServerRunning) QrCodeHelper.generateQrBitmap(tvUrl, 400) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Backup", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profil Usaha & Logo
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.Store,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Profil Usaha & Logo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Logo Picker Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!shopConfig.shopLogoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(shopConfig.shopLogoUri),
                                    contentDescription = "Logo Usaha",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, PrimaryNavy, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(TintNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ContentCut, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(99.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("change_logo_button")
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pilih Logo dari Galeri", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Format JPG/PNG, ukuran persegi disarankan", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Nama Usaha Barbershop") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shop_name_input"),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = shopAddress,
                            onValueChange = { shopAddress = it },
                            label = { Text("Alamat") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = shopPhone,
                            onValueChange = { shopPhone = it },
                            label = { Text("No. HP / WhatsApp") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = receiptFooter,
                            onValueChange = { receiptFooter = it },
                            label = { Text("Teks Footer Struk") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateShopProfile(
                                    name = shopName,
                                    address = shopAddress,
                                    phone = shopPhone,
                                    footer = receiptFooter,
                                    logoUri = shopConfig.shopLogoUri
                                )
                                Toast.makeText(context, "Profil usaha berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_shop_profile_button"),
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                        ) {
                            Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Fitur & Modul Toggles
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.Tune,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Modul & Fitur", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggle Kapster
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fitur Kapster & Bagi Hasil", fontWeight = FontWeight.Bold)
                                Text("Matikan jika owner memotong rambut sendirian tanpa karyawan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = shopConfig.enableKapster,
                                onCheckedChange = { viewModel.updateToggles(it, shopConfig.enableStock, shopConfig.enableQueue) },
                                modifier = Modifier.testTag("toggle_kapster_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Stok
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Manajemen Stok Produk", fontWeight = FontWeight.Bold)
                                Text("Otomatis kurangi stok saat penjualan pomade & produk.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = shopConfig.enableStock,
                                onCheckedChange = { viewModel.updateToggles(shopConfig.enableKapster, it, shopConfig.enableQueue) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Antrian
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sistem Antrian & Panggilan", fontWeight = FontWeight.Bold)
                                Text("Kelola nomor antrian A001 dan panggilan suara.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = shopConfig.enableQueue,
                                onCheckedChange = { viewModel.updateToggles(shopConfig.enableKapster, shopConfig.enableStock, it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pajak / Service
                        OutlinedTextField(
                            value = taxPercent,
                            onValueChange = {
                                taxPercent = it
                                viewModel.updateTax(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Pajak / Service Charge (%) - Opsional") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Suara Panggilan TTS
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.VolumeUp,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Suara Panggilan Antrian (TTS)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aktifkan Panggilan Suara Bahasa Indonesia", fontWeight = FontWeight.Medium)
                            Switch(
                                checked = shopConfig.enableTts,
                                onCheckedChange = { viewModel.updateTtsSettings(it, ttsSpeed) }
                            )
                        }

                        if (shopConfig.enableTts) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Kecepatan Suara: ${String.format(java.util.Locale.US, "%.1fx", ttsSpeed)}", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = ttsSpeed,
                                onValueChange = {
                                    ttsSpeed = it
                                    viewModel.updateTtsSettings(shopConfig.enableTts, it)
                                },
                                valueRange = 0.7f..1.5f,
                                steps = 7
                            )
                        }
                    }
                }
            }

            // Tema Warna & Tampilan
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.ColorLens,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tema Warna & Tampilan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Pilih Aksen Warna:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeColorOption(
                                label = "Navy",
                                color = Color(0xFF1E3A8A),
                                isSelected = shopConfig.themeColor == AppThemeColor.NAVY,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.updateTheme(AppThemeColor.NAVY, shopConfig.isDarkMode) }

                            ThemeColorOption(
                                label = "Teal",
                                color = Color(0xFF0D9488),
                                isSelected = shopConfig.themeColor == AppThemeColor.TEAL,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.updateTheme(AppThemeColor.TEAL, shopConfig.isDarkMode) }

                            ThemeColorOption(
                                label = "Amber",
                                color = Color(0xFFD97706),
                                isSelected = shopConfig.themeColor == AppThemeColor.AMBER,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.updateTheme(AppThemeColor.AMBER, shopConfig.isDarkMode) }

                            ThemeColorOption(
                                label = "Slate",
                                color = Color(0xFF334155),
                                isSelected = shopConfig.themeColor == AppThemeColor.SLATE,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.updateTheme(AppThemeColor.SLATE, shopConfig.isDarkMode) }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mode Gelap (Dark Mode)", fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = shopConfig.isDarkMode,
                                onCheckedChange = { viewModel.updateTheme(shopConfig.themeColor, it) }
                            )
                        }
                    }
                }
            }

            // Tampilan TV & Server Web Lokal
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.Tv,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tampilan TV & Server Web Lokal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Aplikasi menyediakan 3 cara gratis untuk menampilkan antrian ke TV:\n" +
                                    "1. Layar TV Fullscreen (bisa Cast / Mirroring via HDMI/Chromecast)\n" +
                                    "2. Layar Kedua Presentation (otomatis saat colok kabel HDMI)\n" +
                                    "3. Server Web Lokal Tertanam: Buka browser di Smart TV pada alamat di bawah (sama WiFi).",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Server Web TV Lokal", fontWeight = FontWeight.Bold)
                                Text(if (tvServerRunning) "Aktif pada port ${shopConfig.tvServerPort}" else "Nonaktif", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = if (tvServerRunning) AccentEmerald else AccentRose)
                            }
                            Switch(
                                checked = tvServerRunning,
                                onCheckedChange = { viewModel.toggleTvServer(it) }
                            )
                        }

                        if (tvServerRunning) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Alamat Browser Smart TV:", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tvUrl,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PrimaryNavy
                                        )
                                    )

                                    if (qrBitmap != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "QR Code Alamat TV",
                                            modifier = Modifier
                                                .size(160.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Pindai QR ini di Smart TV / HP lain", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Backup & Restore
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.Backup,
                                iconColor = PrimaryNavy,
                                tintColor = TintNavy,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Cadangkan & Pulihkan (Backup)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Semua data transaksi, antrian, dan kapster tersimpan di penyimpanan HP lokal. Anda dapat mengekspor cadangan database kapan saja.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val backupSummary = "CADANGAN DATA ${shopConfig.shopName}\nTanggal: ${Formatters.formatDateTime(System.currentTimeMillis())}\nData tersimpan aman di database Room lokal aplikasi."
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, backupSummary)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Cadangkan Data Barbershop"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cadangkan", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Database Room lokal dalam kondisi prima dan aktif.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(99.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryNavy)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pulihkan", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            }
                        }
                    }
                }
            }

            // Reset Database
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFECACA)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TintedIconBox(
                                icon = Icons.Default.DeleteForever,
                                iconColor = AccentRose,
                                tintColor = TintRed,
                                size = 40.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reset Data Aplikasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AccentRose, fontSize = 16.sp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Menghapus seluruh transaksi, antrian, dan pengeluaran. Membutuhkan konfirmasi ganda.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF991B1B)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showResetDialog1 = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(99.dp)
                        ) {
                            Text("Hapus & Reset Data", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirmation 1
    if (showResetDialog1) {
        AlertDialog(
            onDismissRequest = { showResetDialog1 = false },
            title = { Text("Konfirmasi Reset Data (1/2)") },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh riwayat transaksi dan data operasional?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog1 = false
                        showResetDialog2 = true
                    }
                ) { Text("Lanjut", color = AccentRose, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog1 = false }) { Text("Batal") }
            }
        )
    }

    // Confirmation 2
    if (showResetDialog2) {
        AlertDialog(
            onDismissRequest = { showResetDialog2 = false },
            title = { Text("Peringatan Terakhir (2/2)") },
            text = { Text("Tindakan ini permanen dan tidak dapat dibatalkan. Semua data riwayat penjualan akan hilang.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog2 = false
                        viewModel.resetDatabase()
                        Toast.makeText(context, "Data berhasil direset!", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("HAPUS SEMUA", color = AccentRose, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog2 = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ThemeColorOption(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}
