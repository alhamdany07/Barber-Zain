package com.example.ui.screens.pos

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.dao.TransactionWithItems
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ServiceEntity
import com.example.ui.components.BarberTopAppBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintBlue
import com.example.ui.theme.TintGreen
import com.example.ui.theme.TintNavy
import com.example.ui.theme.TintRed
import com.example.ui.theme.TintTeal
import com.example.ui.viewmodel.BarberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: BarberViewModel) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val activeServices by viewModel.activeServices.collectAsState()
    val activeProducts by viewModel.activeProducts.collectAsState()
    val activeKapsters by viewModel.activeKapsters.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showQuickAddCustomerDialog by remember { mutableStateOf(false) }
    var viewingReceiptTrx by remember { mutableStateOf<TransactionWithItems?>(null) }

    val totalCartCount = cartItems.sumOf { it.quantity }
    val cartSubtotal = cartItems.sumOf { it.price * it.quantity }

    val categories = remember(activeServices, activeProducts) {
        val list = mutableListOf("Semua", "Layanan", "Produk")
        val serviceCats = activeServices.map { it.category }.distinct()
        val prodCats = activeProducts.map { it.category }.distinct()
        list.addAll((serviceCats + prodCats).distinct().filter { it !in list })
        list
    }

    val filteredServices = activeServices.filter {
        val matchSearch = it.name.contains(searchQuery, ignoreCase = true)
        val matchCat = when (selectedCategory) {
            "Semua", "Layanan" -> true
            "Produk" -> false
            else -> it.category.equals(selectedCategory, ignoreCase = true)
        }
        matchSearch && matchCat
    }

    val filteredProducts = if (shopConfig.enableStock) {
        activeProducts.filter {
            val matchSearch = it.name.contains(searchQuery, ignoreCase = true)
            val matchCat = when (selectedCategory) {
                "Semua", "Produk" -> true
                "Layanan" -> false
                else -> it.category.equals(selectedCategory, ignoreCase = true)
            }
            matchSearch && matchCat
        }
    } else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                BarberTopAppBar(
                    title = "Kasir (POS)",
                    subtitle = "Layanan & produk pangkas rambut",
                    logoUri = shopConfig.shopLogoUri,
                    actions = {
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier.testTag("pos_history_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Riwayat Transaksi")
                        }

                        BadgedBox(
                            badge = {
                                if (totalCartCount > 0) {
                                    Badge(
                                        containerColor = PrimaryNavy,
                                        contentColor = Color.White
                                    ) {
                                        Text("$totalCartCount")
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { showCartSheet = true },
                                modifier = Modifier.testTag("pos_open_cart_button")
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("pos_search_input"),
                    placeholder = { Text("Cari layanan, paket, atau pomade...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryNavy) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(99.dp), // pill
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = PrimaryNavy,
                        unfocusedBorderColor = BorderSubtleLight
                    )
                )

                // Category Chips (pill shape, active navy with white text)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val selected = cat == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            shape = RoundedCornerShape(99.dp), // pill
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

                Spacer(modifier = Modifier.height(4.dp))

                // Grid of items
                if (filteredServices.isEmpty() && filteredProducts.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = "Item Tidak Ditemukan",
                        message = "Coba ubah kata kunci pencarian atau kategori filter."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 160.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Services
                        items(filteredServices) { service ->
                            val inCart = cartItems.firstOrNull { it.id == service.id && it.type == "SERVICE" }
                            val icon = getCategoryIcon(service.category, isProduct = false)
                            ModernPosItemCard(
                                title = service.name,
                                price = service.price,
                                subtitle = "${service.durationMinutes} mnt • ${service.category}",
                                icon = icon,
                                isProduct = false,
                                inCartQty = inCart?.quantity ?: 0,
                                onAdd = { viewModel.addToCart(service) }
                            )
                        }

                        // Products
                        items(filteredProducts) { product ->
                            val inCart = cartItems.firstOrNull { it.id == product.id && it.type == "PRODUCT" }
                            val icon = getCategoryIcon(product.category, isProduct = true)
                            ModernPosItemCard(
                                title = product.name,
                                price = product.sellPrice,
                                subtitle = "Stok: ${product.stock} • ${product.category}",
                                icon = icon,
                                isProduct = true,
                                inCartQty = inCart?.quantity ?: 0,
                                onAdd = { viewModel.addProductToCart(product) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Cart Summary Bar at Bottom (above the floating bottom navigation)
        if (cartItems.isNotEmpty()) {
            val barShape = RoundedCornerShape(24.dp)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 86.dp)
                    .shadow(10.dp, barShape, spotColor = PrimaryNavy.copy(alpha = 0.25f))
                    .clip(barShape)
                    .border(1.dp, BorderSubtleLight, barShape),
                shape = barShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showCartSheet = true }
                            .padding(end = 8.dp)
                    ) {
                        TintedIconBox(
                            icon = Icons.Default.ShoppingCart,
                            iconColor = PrimaryNavy,
                            tintColor = TintNavy,
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$totalCartCount Layanan/Item",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = Formatters.formatRupiah(cartSubtotal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryNavy,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        shape = RoundedCornerShape(99.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("pos_checkout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Text("Bayar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Cart Bottom Sheet
    if (showCartSheet) {
        CartBottomSheet(
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onProceedToCheckout = {
                showCartSheet = false
                showCheckoutDialog = true
            },
            onAddCustomer = { showQuickAddCustomerDialog = true }
        )
    }

    // Checkout Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            viewModel = viewModel,
            onDismiss = { showCheckoutDialog = false },
            onSuccess = { completedTrx ->
                showCheckoutDialog = false
                viewingReceiptTrx = completedTrx
            }
        )
    }

    // History Dialog
    if (showHistoryDialog) {
        TransactionHistoryDialog(
            viewModel = viewModel,
            onDismiss = { showHistoryDialog = false },
            onViewReceipt = { trx -> viewingReceiptTrx = trx }
        )
    }

    // Quick Add Customer Dialog
    if (showQuickAddCustomerDialog) {
        QuickAddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddCustomerDialog = false }
        )
    }

    // Receipt Dialog
    viewingReceiptTrx?.let { trxWithItems ->
        ReceiptDialog(
            transactionWithItems = trxWithItems,
            shopConfig = shopConfig,
            onDismiss = { viewingReceiptTrx = null }
        )
    }
}

private fun getCategoryIcon(category: String, isProduct: Boolean): ImageVector {
    val cat = category.lowercase()
    return when {
        cat.contains("cukur") || cat.contains("grooming") || cat.contains("jenggot") -> Icons.Default.Face
        cat.contains("cuci") || cat.contains("treatment") || cat.contains("pijat") || cat.contains("creambath") -> Icons.Default.Spa
        cat.contains("color") || cat.contains("warna") || cat.contains("cat") -> Icons.Default.ColorLens
        cat.contains("paket") || cat.contains("lengkap") || cat.contains("hemat") -> Icons.Default.CardGiftcard
        cat.contains("pomade") || cat.contains("clay") || cat.contains("wax") -> Icons.Default.LocalMall
        isProduct -> Icons.Default.Inventory2
        else -> Icons.Default.ContentCut
    }
}

@Composable
private fun ModernPosItemCard(
    title: String,
    price: Double,
    subtitle: String,
    icon: ImageVector,
    isProduct: Boolean,
    inCartQty: Int,
    onAdd: () -> Unit
) {
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${title.replace(" ", "_")}"),
        cornerRadius = 24.dp,
        elevation = if (inCartQty > 0) 3.dp else 1.dp,
        onClick = onAdd,
        backgroundColor = if (inCartQty > 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface,
        borderColor = if (inCartQty > 0) PrimaryNavy.copy(alpha = 0.5f) else BorderSubtleLight
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Category Icon with soft pale tint background
                TintedIconBox(
                    icon = icon,
                    iconColor = if (isProduct) AccentAmber else PrimaryNavy,
                    tintColor = if (isProduct) TintAmber else TintNavy,
                    size = 40.dp,
                    iconSize = 20.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bold Navy Price
                Text(
                    text = Formatters.formatRupiah(price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNavy,
                        fontSize = 16.sp
                    )
                )
            }

            // Small '+' button at the bottom-right corner of the card
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (inCartQty > 0) PrimaryNavy else TintNavy)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                if (inCartQty > 0) {
                    Text(
                        text = "$inCartQty",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = PrimaryNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartBottomSheet(
    viewModel: BarberViewModel,
    onDismiss: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onAddCustomer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cartItems by viewModel.cartItems.collectAsState()
    val shopConfig by viewModel.shopConfig.collectAsState()
    val activeKapsters by viewModel.activeKapsters.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val selectedKapster by viewModel.selectedKapster.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    var kapsterExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keranjang Layanan (${cartItems.sumOf { it.quantity }})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                TextButton(onClick = { viewModel.clearCart() }) {
                    Text("Kosongkan", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Kapster & Customer Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (shopConfig.enableKapster) {
                    ExposedDropdownMenuBox(
                        expanded = kapsterExpanded,
                        onExpandedChange = { kapsterExpanded = !kapsterExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedKapster?.name ?: "Pilih Kapster",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kapster") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kapsterExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = kapsterExpanded,
                            onDismissRequest = { kapsterExpanded = false }
                        ) {
                            activeKapsters.forEach { kapster ->
                                DropdownMenuItem(
                                    text = { Text(kapster.name) },
                                    onClick = {
                                        viewModel.selectedKapster.value = kapster
                                        kapsterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = !customerExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "Umum",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pelanggan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Umum (Tanpa Nama)") },
                            onClick = {
                                viewModel.selectedCustomer.value = null
                                customerExpanded = false
                            }
                        )
                        allCustomers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text(cust.name) },
                                onClick = {
                                    viewModel.selectedCustomer.value = cust
                                    customerExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Tambah Pelanggan Baru", color = PrimaryNavy, fontWeight = FontWeight.Bold) },
                            onClick = {
                                customerExpanded = false
                                onAddCustomer()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cart Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    SoftCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = Formatters.formatRupiah(item.price),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PrimaryNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateCartQuantity(item.id, item.type, item.quantity - 1) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Kurang",
                                        tint = if (item.quantity == 1) AccentRose else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.updateCartQuantity(item.id, item.type, item.quantity + 1) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))

            // Cart Total & Proceed
            val subtotal = cartItems.sumOf { it.price * it.quantity }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtotal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = Formatters.formatRupiah(subtotal),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNavy
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceedToCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cart_proceed_checkout_button"),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
            ) {
                Text("Lanjut ke Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CheckoutDialog(
    viewModel: BarberViewModel,
    onDismiss: () -> Unit,
    onSuccess: (TransactionWithItems) -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val discountAmountFixed by viewModel.discountAmountFixed.collectAsState()
    val isDiscountPercent by viewModel.isDiscountPercent.collectAsState()
    val tipAmount by viewModel.tipAmount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val cashGiven by viewModel.cashGiven.collectAsState()

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val discount = if (isDiscountPercent) subtotal * (discountPercent / 100.0) else discountAmountFixed
    val taxRate = shopConfig.taxPercentage
    val taxAmount = (subtotal - discount).coerceAtLeast(0.0) * (taxRate / 100.0)
    val total = (subtotal - discount + tipAmount + taxAmount).coerceAtLeast(0.0)
    val change = if (paymentMethod == "TUNAI") (cashGiven - total).coerceAtLeast(0.0) else 0.0

    var cashInputText by remember { mutableStateOf(if (cashGiven > 0) cashGiven.toLong().toString() else "") }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pembayaran",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Box
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = TintNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL TAGIHAN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatRupiah(total),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryNavy,
                                fontSize = 28.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Selector
                val methods = listOf("TUNAI", "QRIS", "TRANSFER")
                TabRow(
                    selectedTabIndex = methods.indexOf(paymentMethod)
                ) {
                    methods.forEach { method ->
                        Tab(
                            selected = paymentMethod == method,
                            onClick = {
                                viewModel.paymentMethod.value = method
                                if (method == "TUNAI" && cashGiven < total) {
                                    viewModel.cashGiven.value = total
                                    cashInputText = total.toLong().toString()
                                }
                            },
                            text = { Text(method, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (paymentMethod) {
                    "TUNAI" -> {
                        Column {
                            OutlinedTextField(
                                value = cashInputText,
                                onValueChange = {
                                    cashInputText = it
                                    val amount = it.toDoubleOrNull() ?: 0.0
                                    viewModel.cashGiven.value = amount
                                },
                                label = { Text("Uang Tunai Diterima") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cash_given_input"),
                                shape = RoundedCornerShape(14.dp),
                                prefix = { Text("Rp ") }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val suggestions = listOf(
                                "Uang Pas" to total,
                                "50.000" to 50000.0,
                                "100.000" to 100000.0,
                                "200.000" to 200000.0
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(suggestions) { (label, amt) ->
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = TintNavy,
                                        modifier = Modifier.clickable {
                                            viewModel.cashGiven.value = amt
                                            cashInputText = amt.toLong().toString()
                                        }
                                    ) {
                                        Text(
                                            text = label,
                                            color = PrimaryNavy,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kembalian", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = Formatters.formatRupiah(change),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (change >= 0) AccentEmerald else AccentRose
                                    )
                                )
                            }
                        }
                    }
                    "QRIS" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(TintNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(48.dp), tint = PrimaryNavy)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pindai QRIS Toko", fontWeight = FontWeight.Bold)
                            Text("Tunjukkan QRIS barbershop ke pelanggan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    "TRANSFER" -> {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text("Transfer Bank / E-Wallet", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("BCA / Mandiri / BRI / DANA / GoPay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.checkout(onSuccess = onSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_checkout_button"),
                    shape = RoundedCornerShape(99.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Selesaikan & Cetak Struk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryDialog(
    viewModel: BarberViewModel,
    onDismiss: () -> Unit,
    onViewReceipt: (TransactionWithItems) -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (allTransactions.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "Belum Ada Transaksi",
                        message = "Semua transaksi selesai akan tercatat di sini."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allTransactions) { trxWithItems ->
                            val trx = trxWithItems.transaction
                            SoftCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 18.dp,
                                onClick = { onViewReceipt(trxWithItems) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = trx.transactionNumber,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = Formatters.formatRupiah(trx.totalAmount),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (trx.isVoided) Color.Gray else PrimaryNavy
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${trx.customerName} • ${trx.kapsterName ?: "Admin"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Formatters.formatTime(trx.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (trx.isVoided) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "DIBATALKAN / VOID",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AccentRose
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { viewModel.voidTransaction(trx.id) }) {
                                                Text("Batalkan / Void", color = AccentRose, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddCustomerDialog(
    viewModel: BarberViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tambah Pelanggan Baru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Gaya Rambut") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            if (name.isNotBlank()) {
                                val customer = CustomerEntity(name = name, phone = phone, notes = notes)
                                viewModel.saveCustomer(customer)
                                viewModel.selectedCustomer.value = customer
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(99.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
