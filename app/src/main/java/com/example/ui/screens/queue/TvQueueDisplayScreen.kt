package com.example.ui.screens.queue

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.QueueStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AppBgDark
import com.example.ui.theme.CardBgDark
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyLight
import com.example.ui.viewmodel.BarberViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TvQueueDisplayScreen(
    viewModel: BarberViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val shopConfig by viewModel.shopConfig.collectAsState()
    val todayQueues by viewModel.todayQueues.collectAsState()

    BackHandler { onBack() }

    // Keep screen awake while TV display is active
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Live Digital Clock
    var currentTimeStr by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    val currentServing = todayQueues.firstOrNull { it.status == QueueStatus.CALLED || it.status == QueueStatus.IN_SERVICE }
    val waitingList = todayQueues.filter { it.status == QueueStatus.WAITING }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Header: Logo, Shop Name, Clock, Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(CardBgDark, CircleShape)
                            .testTag("tv_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (!shopConfig.shopLogoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(shopConfig.shopLogoUri),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(2.dp, AccentAmber, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimaryNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                    }

                    Column {
                        Text(
                            text = shopConfig.shopName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Layar Antrian Digital Barbershop",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Digital Clock
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBgDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252D4A))
                ) {
                    Text(
                        text = currentTimeStr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8),
                            fontSize = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                    )
                }
            }

            // Main Split: Left (Number >= 120sp) | Right (Waiting list)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left: Hero Current Calling Card
                Card(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgDark),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryNavyLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = if (currentServing?.status == QueueStatus.IN_SERVICE) AccentGreen else AccentAmber,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = if (currentServing?.status == QueueStatus.IN_SERVICE) "SEDANG DILAYANI" else "SEDANG DIPANGGIL",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.padding(horizontal = 26.dp, vertical = 8.dp)
                            )
                        }

                        // Animated number transition with 130sp font size
                        AnimatedContent(
                            targetState = currentServing?.queueNumber ?: "---",
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                        (slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "queue_num_anim"
                        ) { targetNumber ->
                            Text(
                                text = targetNumber,
                                fontSize = 130.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 130.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentServing?.customerName ?: "Menunggu Antrian Berikutnya",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = Color(0xFFF1F5F9),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentServing?.serviceName ?: "Silakan ambil nomor antrian di meja kasir",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                color = Color(0xFF94A3B8)
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Chair / Kapster Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF131A2E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3A59))
                        ) {
                            val chairStr = "Kursi " + (currentServing?.chairNumber ?: "1")
                            val kapsterStr = if (!currentServing?.kapsterName.isNullOrBlank()) " • Kapster ${currentServing?.kapsterName}" else ""
                            Text(
                                text = "$chairStr$kapsterStr",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.padding(horizontal = 26.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                // Right: Upcoming Waiting List Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252D4A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Antrian Berikutnya",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color(0xFF94A3B8)
                            )
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = Color(0x26F59E0B)
                            ) {
                                Text(
                                    text = "${waitingList.size} Orang",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AccentAmber
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (waitingList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada antrian menunggu",
                                    color = Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(waitingList) { q ->
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = Color(0xFF131A2E),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252D4A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFF1A2036),
                                                    modifier = Modifier.padding(end = 12.dp)
                                                ) {
                                                    Text(
                                                        text = q.queueNumber,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Black,
                                                            color = AccentAmber,
                                                            fontSize = 18.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = q.customerName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                    Text(
                                                        text = q.serviceName,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF94A3B8)
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "~${q.serviceDuration} Mnt",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF38BDF8)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TV Footer Note
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${shopConfig.shopName} • Layar Antrian Real-Time • Ganteng Maksimal Bersama Zain",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
