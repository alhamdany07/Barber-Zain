package com.example.ui.screens.more.data

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SalaryType
import com.example.data.local.entity.ServiceEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.SoftCard
import com.example.ui.components.TintedIconBox
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.TintAmber
import com.example.ui.theme.TintBlue
import com.example.ui.theme.TintGreen
import com.example.ui.theme.TintNavy
import com.example.ui.theme.TintRed
import com.example.ui.viewmodel.BarberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDataScreen(
    viewModel: BarberViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf(0) } // 0: Layanan, 1: Produk, 2: Kapster, 3: Pelanggan, 4: Pengeluaran
    val tabTitles = listOf("Layanan", "Produk", "Kapster", "Pelanggan", "Pengeluaran")

    val allServices by viewModel.allServices.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allKapsters by viewModel.allKapsters.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()

    var showServiceDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var showKapsterDialog by remember { mutableStateOf(false) }
    var editingKapster by remember { mutableStateOf<KapsterEntity?>(null) }

    var showCustomerDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Data", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> { editingService = null; showServiceDialog = true }
                        1 -> { editingProduct = null; showProductDialog = true }
                        2 -> { editingKapster = null; showKapsterDialog = true }
                        3 -> { editingCustomer = null; showCustomerDialog = true }
                        4 -> { editingExpense = null; showExpenseDialog = true }
                    }
                },
                containerColor = PrimaryNavy,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("manage_data_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Data", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category/Tab horizontal chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabTitles.indices.toList()) { index ->
                    val selected = selectedTab == index
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTab = index },
                        label = { Text(tabTitles[index], fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp) },
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

            Spacer(modifier = Modifier.height(4.dp))

            when (selectedTab) {
                0 -> { // Layanan
                    if (allServices.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.ContentCut,
                            title = "Belum Ada Layanan",
                            message = "Tambahkan layanan pangkas rambut dengan tombol + di bawah."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allServices) { service ->
                                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TintedIconBox(
                                                icon = Icons.Default.ContentCut,
                                                iconColor = PrimaryNavy,
                                                tintColor = TintNavy,
                                                size = 44.dp,
                                                iconSize = 22.dp
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                    if (!service.isActive) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("(Nonaktif)", color = AccentRose, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                Text(
                                                    "${Formatters.formatRupiah(service.price)} • ${service.durationMinutes} menit • ${service.category}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingService = service; showServiceDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.deleteService(service) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRose)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Produk
                    if (allProducts.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Inventory,
                            title = "Belum Ada Produk",
                            message = "Tambahkan produk seperti Pomade atau Hair Tonic dengan tombol +."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allProducts) { product ->
                                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TintedIconBox(
                                                icon = Icons.Default.Inventory,
                                                iconColor = AccentGold,
                                                tintColor = TintAmber,
                                                size = 44.dp,
                                                iconSize = 22.dp
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    "Jual: ${Formatters.formatRupiah(product.sellPrice)} • Modal: ${Formatters.formatRupiah(product.buyPrice)}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = PrimaryNavy
                                                )
                                                Text(
                                                    "Stok: ${product.stock} (Min: ${product.minStock}) • ${product.category}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = if (product.stock <= product.minStock) AccentRose else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingProduct = product; showProductDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.deleteProduct(product) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRose)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Kapster
                    if (allKapsters.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.People,
                            title = "Belum Ada Kapster",
                            message = "Daftarkan nama tukang pangkas rambut / kapster di barbershop Anda."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allKapsters) { kapster ->
                                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val kapsterInitial = kapster.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(TintNavy),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = kapsterInitial,
                                                    color = PrimaryNavy,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(kapster.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                val salaryDesc = when (kapster.salaryType) {
                                                    SalaryType.PERCENTAGE -> "Bagi Hasil ${kapster.salaryRate.toInt()}% per layanan"
                                                    SalaryType.FIXED_PER_SERVICE -> "${Formatters.formatRupiah(kapster.salaryRate)} per kepala"
                                                    SalaryType.BASE_SALARY -> "Gaji Pokok ${Formatters.formatRupiah(kapster.baseSalary)}"
                                                    SalaryType.BASE_PLUS_PERCENTAGE -> "Pokok ${Formatters.formatRupiah(kapster.baseSalary)} + Bagi Hasil ${kapster.salaryRate.toInt()}%"
                                                }
                                                Text(
                                                    salaryDesc,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                    color = PrimaryNavy
                                                )
                                                if (kapster.phone.isNotBlank()) {
                                                    Text("Telp: ${kapster.phone}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingKapster = kapster; showKapsterDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.deleteKapster(kapster) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRose)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> { // Pelanggan
                    if (allCustomers.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Person,
                            title = "Belum Ada Pelanggan Terdaftar",
                            message = "Data pelanggan langganan akan tersimpan otomatis saat transaksi."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allCustomers) { customer ->
                                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TintedIconBox(
                                                icon = Icons.Default.Person,
                                                iconColor = PrimaryNavy,
                                                tintColor = TintNavy,
                                                size = 44.dp,
                                                iconSize = 22.dp
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                Text("Telp: ${customer.phone.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (customer.notes.isNotBlank()) {
                                                    Text("Catatan: ${customer.notes}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = PrimaryNavy)
                                                }
                                                Text(
                                                    "Total Belanja: ${Formatters.formatRupiah(customer.totalSpent)} (${customer.visitCount} kunjungan)",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingCustomer = customer; showCustomerDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRose)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> { // Pengeluaran
                    if (allExpenses.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.MoneyOff,
                            title = "Belum Ada Pengeluaran",
                            message = "Catat pengeluaran listrik, sewa tempat, atau perlengkapan dengan tombol +."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allExpenses) { expense ->
                                SoftCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TintedIconBox(
                                                icon = Icons.Default.MoneyOff,
                                                iconColor = AccentRose,
                                                tintColor = TintRed,
                                                size = 44.dp,
                                                iconSize = 22.dp
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(expense.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                Text(Formatters.formatRupiah(expense.amount), fontWeight = FontWeight.ExtraBold, color = AccentRose)
                                                if (expense.notes.isNotBlank()) {
                                                    Text(expense.notes, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Text(Formatters.formatDate(expense.dateTimestamp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color.Gray)
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { editingExpense = expense; showExpenseDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy)
                                            }
                                            IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRose)
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

    // Dialogs
    if (showServiceDialog) {
        ServiceFormDialog(
            initial = editingService,
            onDismiss = { showServiceDialog = false },
            onSave = { service ->
                viewModel.saveService(service)
                showServiceDialog = false
            }
        )
    }

    if (showProductDialog) {
        ProductFormDialog(
            initial = editingProduct,
            onDismiss = { showProductDialog = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showProductDialog = false
            }
        )
    }

    if (showKapsterDialog) {
        KapsterFormDialog(
            initial = editingKapster,
            onDismiss = { showKapsterDialog = false },
            onSave = { kapster ->
                viewModel.saveKapster(kapster)
                showKapsterDialog = false
            }
        )
    }

    if (showCustomerDialog) {
        CustomerFormDialog(
            initial = editingCustomer,
            onDismiss = { showCustomerDialog = false },
            onSave = { customer ->
                viewModel.saveCustomer(customer)
                showCustomerDialog = false
            }
        )
    }

    if (showExpenseDialog) {
        ExpenseFormDialog(
            initial = editingExpense,
            onDismiss = { showExpenseDialog = false },
            onSave = { expense ->
                viewModel.saveExpense(expense)
                showExpenseDialog = false
            }
        )
    }
}

// Dialog Components
@Composable
private fun ServiceFormDialog(
    initial: ServiceEntity?,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(if (initial != null) initial.price.toLong().toString() else "") }
    var duration by remember { mutableStateOf(if (initial != null) initial.durationMinutes.toString() else "30") }
    var category by remember { mutableStateOf(initial?.category ?: "Layanan Utama") }
    var customCommission by remember { mutableStateOf(if (initial?.customCommission != null) initial.customCommission.toLong().toString() else "") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(text = if (initial == null) "Tambah Layanan" else "Edit Layanan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Layanan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durasi (Menit)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = customCommission, onValueChange = { customCommission = it }, label = { Text("Komisi Khusus Rp (Opsional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Status Aktif")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    ServiceEntity(
                                        id = initial?.id ?: 0L,
                                        name = name,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        durationMinutes = duration.toIntOrNull() ?: 30,
                                        category = category.ifBlank { "Layanan Utama" },
                                        isActive = isActive,
                                        customCommission = customCommission.toDoubleOrNull()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initial: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var sellPrice by remember { mutableStateOf(if (initial != null) initial.sellPrice.toLong().toString() else "") }
    var buyPrice by remember { mutableStateOf(if (initial != null) initial.buyPrice.toLong().toString() else "") }
    var stock by remember { mutableStateOf(if (initial != null) initial.stock.toString() else "10") }
    var minStock by remember { mutableStateOf(if (initial != null) initial.minStock.toString() else "3") }
    var category by remember { mutableStateOf(initial?.category ?: "Produk") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(text = if (initial == null) "Tambah Produk" else "Edit Produk", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = { Text("Harga Jual (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = buyPrice, onValueChange = { buyPrice = it }, label = { Text("Harga Modal / HPP (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Min Stok") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    ProductEntity(
                                        id = initial?.id ?: 0L,
                                        name = name,
                                        sellPrice = sellPrice.toDoubleOrNull() ?: 0.0,
                                        buyPrice = buyPrice.toDoubleOrNull() ?: 0.0,
                                        stock = stock.toIntOrNull() ?: 0,
                                        minStock = minStock.toIntOrNull() ?: 3,
                                        category = category.ifBlank { "Produk" }
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KapsterFormDialog(
    initial: KapsterEntity?,
    onDismiss: () -> Unit,
    onSave: (KapsterEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var salaryType by remember { mutableStateOf(initial?.salaryType ?: SalaryType.PERCENTAGE) }
    var salaryRate by remember { mutableStateOf(if (initial != null) initial.salaryRate.toInt().toString() else "50") }
    var baseSalary by remember { mutableStateOf(if (initial != null) initial.baseSalary.toLong().toString() else "0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(text = if (initial == null) "Tambah Kapster" else "Edit Kapster", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Kapster") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No. HP / WA") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))

                Text("Sistem Gaji / Komisi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                val types = listOf(
                    SalaryType.PERCENTAGE to "Persentase Bagi Hasil (%)",
                    SalaryType.FIXED_PER_SERVICE to "Nominal Tetap per Layanan (Rp)",
                    SalaryType.BASE_SALARY to "Gaji Pokok Saja",
                    SalaryType.BASE_PLUS_PERCENTAGE to "Gaji Pokok + Bagi Hasil (%)"
                )
                types.forEach { (type, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = salaryType == type,
                            onClick = { salaryType = type }
                        )
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (salaryType == SalaryType.PERCENTAGE || salaryType == SalaryType.BASE_PLUS_PERCENTAGE) {
                    OutlinedTextField(value = salaryRate, onValueChange = { salaryRate = it }, label = { Text("Bagi Hasil (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                } else if (salaryType == SalaryType.FIXED_PER_SERVICE) {
                    OutlinedTextField(value = salaryRate, onValueChange = { salaryRate = it }, label = { Text("Nominal per Layanan (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                if (salaryType == SalaryType.BASE_SALARY || salaryType == SalaryType.BASE_PLUS_PERCENTAGE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = baseSalary, onValueChange = { baseSalary = it }, label = { Text("Gaji Pokok (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    KapsterEntity(
                                        id = initial?.id ?: 0L,
                                        name = name,
                                        phone = phone,
                                        salaryType = salaryType,
                                        salaryRate = salaryRate.toDoubleOrNull() ?: 50.0,
                                        baseSalary = baseSalary.toDoubleOrNull() ?: 0.0
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@Composable
private fun CustomerFormDialog(
    initial: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = if (initial == null) "Tambah Pelanggan" else "Edit Pelanggan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No. HP / WA") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan Gaya Rambut") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    CustomerEntity(
                                        id = initial?.id ?: 0L,
                                        name = name,
                                        phone = phone,
                                        notes = notes,
                                        totalSpent = initial?.totalSpent ?: 0.0,
                                        visitCount = initial?.visitCount ?: 0
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@Composable
private fun ExpenseFormDialog(
    initial: ExpenseEntity?,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var category by remember { mutableStateOf(initial?.category ?: "Perlengkapan") }
    var amount by remember { mutableStateOf(if (initial != null) initial.amount.toLong().toString() else "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val categories = listOf("Perlengkapan", "Listrik & Air", "Sewa Tempat", "Beli Stok", "Makan & Minum", "Lain-lain")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(text = if (initial == null) "Catat Pengeluaran" else "Edit Pengeluaran", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))

                Text("Kategori Pengeluaran", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Jumlah Nominal (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan / Keterangan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Batal") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onSave(
                                    ExpenseEntity(
                                        id = initial?.id ?: 0L,
                                        category = category,
                                        amount = amt,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Simpan") }
                }
            }
        }
    }
}
