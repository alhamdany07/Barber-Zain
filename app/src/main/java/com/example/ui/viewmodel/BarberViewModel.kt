package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.dao.TransactionWithItems
import com.example.data.local.entity.CashAdvanceEntity
import com.example.data.local.entity.CashAdvanceType
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.QueueStatus
import com.example.data.local.entity.SalaryRecordEntity
import com.example.data.local.entity.SalaryType
import com.example.data.local.entity.ServiceEntity
import com.example.data.preferences.ShopConfig
import com.example.data.preferences.ShopPreferences
import com.example.data.repository.BarberRepository
import com.example.data.repository.CartItem
import com.example.data.repository.TodayDashboardSummary
import com.example.data.server.LocalQueueServer
import com.example.data.tts.TtsHelper
import com.example.ui.components.BarChartItem
import com.example.ui.components.DonutSlice
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AppThemeColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BarberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BarberRepository(db)
    val preferences = ShopPreferences(application)
    private val ttsHelper = TtsHelper(application)

    val shopConfig: StateFlow<ShopConfig> = preferences.shopConfigFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShopConfig())

    // Master Data Flows
    val allServices: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeServices: StateFlow<List<ServiceEntity>> = repository.activeServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKapsters: StateFlow<List<KapsterEntity>> = repository.allKapsters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeKapsters: StateFlow<List<KapsterEntity>> = repository.activeKapsters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithItems>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Queue Flows
    private val startOfDayMillis: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    val todayQueues: StateFlow<List<QueueEntity>> = repository.getTodayQueues(startOfDayMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQueues: StateFlow<List<QueueEntity>> = repository.getActiveQueues(startOfDayMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQueueCount: StateFlow<Int> = repository.getActiveQueueCount(startOfDayMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Local TV Server
    private val _tvServerRunning = MutableStateFlow(false)
    val tvServerRunning = _tvServerRunning.asStateFlow()

    private val _localIp = MutableStateFlow(LocalQueueServer.getLocalIpAddress())
    val localIp = _localIp.asStateFlow()

    private var localQueueServer: LocalQueueServer? = null

    init {
        // Init Local Server
        viewModelScope.launch {
            preferences.shopConfigFlow.collect { config ->
                if (config.tvServerEnabled && (localQueueServer == null || !localQueueServer!!.isRunning)) {
                    startLocalServer(config.tvServerPort)
                } else if (!config.tvServerEnabled && localQueueServer?.isRunning == true) {
                    stopLocalServer()
                }
            }
        }
    }

    private fun startLocalServer(port: Int) {
        localQueueServer?.stop()
        localQueueServer = LocalQueueServer(port) {
            val config = shopConfig.value
            Pair(config.shopName, todayQueues.value)
        }
        localQueueServer?.start()
        _tvServerRunning.value = true
        _localIp.value = LocalQueueServer.getLocalIpAddress()
    }

    private fun stopLocalServer() {
        localQueueServer?.stop()
        localQueueServer = null
        _tvServerRunning.value = false
    }

    fun toggleTvServer(enabled: Boolean) {
        viewModelScope.launch {
            val port = shopConfig.value.tvServerPort
            preferences.updateTvServer(enabled, port)
            if (enabled) {
                startLocalServer(port)
            } else {
                stopLocalServer()
            }
        }
    }

    // Cart / POS State
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val selectedKapster = MutableStateFlow<KapsterEntity?>(null)
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val discountPercent = MutableStateFlow(0.0)
    val discountAmountFixed = MutableStateFlow(0.0)
    val isDiscountPercent = MutableStateFlow(true)
    val tipAmount = MutableStateFlow(0.0)
    val paymentMethod = MutableStateFlow("TUNAI") // TUNAI, QRIS, TRANSFER
    val cashGiven = MutableStateFlow(0.0)
    val trxNotes = MutableStateFlow("")
    val activeQueueForCheckout = MutableStateFlow<Long?>(null)

    // Latest Completed Transaction (for showing receipt immediately)
    val latestCompletedTransaction = MutableStateFlow<TransactionWithItems?>(null)

    fun addToCart(service: ServiceEntity) {
        val current = cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == service.id && it.type == "SERVICE" }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(
                CartItem(
                    id = service.id,
                    name = service.name,
                    price = service.price,
                    type = "SERVICE",
                    quantity = 1,
                    durationMinutes = service.durationMinutes,
                    customCommission = service.customCommission
                )
            )
        }
        cartItems.value = current
    }

    fun addProductToCart(product: ProductEntity) {
        val current = cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == product.id && it.type == "PRODUCT" }
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(
                CartItem(
                    id = product.id,
                    name = product.name,
                    price = product.sellPrice,
                    costPrice = product.buyPrice,
                    type = "PRODUCT",
                    quantity = 1
                )
            )
        }
        cartItems.value = current
    }

    fun updateCartQuantity(itemId: Long, type: String, newQty: Int) {
        val current = cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId && it.type == type }
        if (index >= 0) {
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            cartItems.value = current
        }
    }

    fun updateCartItemNotes(itemId: Long, type: String, notes: String) {
        val current = cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId && it.type == type }
        if (index >= 0) {
            current[index] = current[index].copy(notes = notes)
            cartItems.value = current
        }
    }

    fun removeFromCart(itemId: Long, type: String) {
        cartItems.value = cartItems.value.filterNot { it.id == itemId && it.type == type }
    }

    fun clearCart() {
        cartItems.value = emptyList()
        selectedKapster.value = null
        selectedCustomer.value = null
        discountPercent.value = 0.0
        discountAmountFixed.value = 0.0
        tipAmount.value = 0.0
        cashGiven.value = 0.0
        trxNotes.value = ""
        activeQueueForCheckout.value = null
    }

    fun checkout(onSuccess: (TransactionWithItems) -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.price * it.quantity }
            val discount = if (isDiscountPercent.value) {
                subtotal * (discountPercent.value / 100.0)
            } else {
                discountAmountFixed.value
            }
            val taxRate = shopConfig.value.taxPercentage
            val taxAmount = (subtotal - discount).coerceAtLeast(0.0) * (taxRate / 100.0)
            val total = (subtotal - discount + tipAmount.value + taxAmount).coerceAtLeast(0.0)
            val cash = if (paymentMethod.value == "TUNAI") {
                if (cashGiven.value < total) total else cashGiven.value
            } else total
            val change = (cash - total).coerceAtLeast(0.0)

            val kapster = selectedKapster.value
            val customer = selectedCustomer.value

            val trxId = repository.checkoutTransaction(
                items = items,
                customerId = customer?.id,
                customerName = customer?.name ?: "Umum",
                kapsterId = kapster?.id,
                kapsterName = kapster?.name,
                subtotal = subtotal,
                discountAmount = discount,
                discountPercent = if (isDiscountPercent.value) discountPercent.value else 0.0,
                tipAmount = tipAmount.value,
                taxAmount = taxAmount,
                totalAmount = total,
                paymentMethod = paymentMethod.value,
                cashGiven = cash,
                changeAmount = change,
                notes = trxNotes.value,
                queueId = activeQueueForCheckout.value
            )

            val completedTrx = db.transactionDao().getTransactionById(trxId)
            clearCart()
            if (completedTrx != null) {
                latestCompletedTransaction.value = completedTrx
                onSuccess(completedTrx)
            }
        }
    }

    fun voidTransaction(trxId: Long) {
        viewModelScope.launch {
            repository.voidTransaction(trxId)
        }
    }

    // Queue Methods
    fun addCustomerToQueue(name: String, phone: String, service: ServiceEntity, kapster: KapsterEntity?, chair: String) {
        viewModelScope.launch {
            val queueNumber = repository.getNextQueueNumber(startOfDayMillis)
            val queueEntity = QueueEntity(
                queueNumber = queueNumber,
                customerName = name.ifBlank { "Pelanggan $queueNumber" },
                customerPhone = phone,
                serviceId = service.id,
                serviceName = service.name,
                serviceDuration = service.durationMinutes,
                kapsterId = kapster?.id,
                kapsterName = kapster?.name,
                chairNumber = chair.ifBlank { "1" },
                status = QueueStatus.WAITING
            )
            repository.insertQueue(queueEntity)
        }
    }

    fun callQueue(queue: QueueEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.updateQueueCalled(queue.id, now)

            if (shopConfig.value.enableTts) {
                val kapsterNotice = if (!queue.kapsterName.isNullOrBlank()) "bersama ${queue.kapsterName}" else ""
                val text = "Nomor antrian ${queue.queueNumber}, silakan menuju kursi ${queue.chairNumber} $kapsterNotice"
                ttsHelper.speak(text, shopConfig.value.ttsSpeed)
            }
        }
    }

    fun startServiceQueue(queue: QueueEntity) {
        viewModelScope.launch {
            repository.updateQueueStatus(queue.id, QueueStatus.IN_SERVICE)
        }
    }

    fun cancelQueue(queue: QueueEntity) {
        viewModelScope.launch {
            repository.updateQueueStatus(queue.id, QueueStatus.CANCELLED)
        }
    }

    fun prepareQueueForCheckout(queue: QueueEntity) {
        // Pre-fill POS Cart with Queue customer, service & kapster
        clearCart()
        activeQueueForCheckout.value = queue.id

        // Find customer if exists
        val matchedCustomer = allCustomers.value.firstOrNull {
            it.name.equals(queue.customerName, ignoreCase = true) ||
                    (queue.customerPhone.isNotBlank() && it.phone == queue.customerPhone)
        }
        if (matchedCustomer != null) {
            selectedCustomer.value = matchedCustomer
        } else {
            selectedCustomer.value = CustomerEntity(name = queue.customerName, phone = queue.customerPhone)
        }

        // Find kapster
        if (queue.kapsterId != null) {
            selectedKapster.value = allKapsters.value.firstOrNull { it.id == queue.kapsterId }
        }

        // Add service
        val service = allServices.value.firstOrNull { it.id == queue.serviceId }
            ?: ServiceEntity(name = queue.serviceName, price = 35000.0, durationMinutes = queue.serviceDuration)
        addToCart(service)
    }

    // Master Data Operations
    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            if (service.id == 0L) repository.insertService(service)
            else repository.updateService(service)
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch { repository.deleteService(service) }
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) repository.insertProduct(product)
            else repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun saveKapster(kapster: KapsterEntity) {
        viewModelScope.launch {
            if (kapster.id == 0L) repository.insertKapster(kapster)
            else repository.updateKapster(kapster)
        }
    }

    fun deleteKapster(kapster: KapsterEntity) {
        viewModelScope.launch { repository.deleteKapster(kapster) }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) repository.insertCustomer(customer)
            else repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            if (expense.id == 0L) repository.insertExpense(expense)
            else repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // Cash Advances & Payroll
    fun addCashAdvance(kapsterId: Long, amount: Double, type: CashAdvanceType, notes: String) {
        viewModelScope.launch {
            val advance = CashAdvanceEntity(
                kapsterId = kapsterId,
                amount = amount,
                type = type,
                notes = notes
            )
            repository.insertCashAdvance(advance)
        }
    }

    fun payKapsterSalary(record: SalaryRecordEntity) {
        viewModelScope.launch {
            repository.insertSalaryRecord(record)
            db.salaryDao().settleCashAdvances(record.kapsterId)
        }
    }

    // Settings
    fun updateShopProfile(name: String, address: String, phone: String, footer: String, logoUri: String?) {
        viewModelScope.launch {
            preferences.updateShopProfile(name, address, phone, footer, logoUri)
        }
    }

    fun updateTheme(themeColor: AppThemeColor, isDarkMode: Boolean) {
        viewModelScope.launch {
            preferences.updateTheme(themeColor, isDarkMode)
        }
    }

    fun updateToggles(kapster: Boolean, stock: Boolean, queue: Boolean) {
        viewModelScope.launch {
            preferences.updateToggles(kapster, stock, queue)
        }
    }

    fun updateTax(tax: Double) {
        viewModelScope.launch { preferences.updateTax(tax) }
    }

    fun updateTtsSettings(enabled: Boolean, speed: Float) {
        viewModelScope.launch { preferences.updateTts(enabled, speed) }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
        localQueueServer?.stop()
    }
}
