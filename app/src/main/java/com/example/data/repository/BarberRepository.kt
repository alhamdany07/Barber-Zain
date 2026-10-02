package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.TransactionWithItems
import com.example.data.local.entity.CashAdvanceEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.QueueStatus
import com.example.data.local.entity.SalaryRecordEntity
import com.example.data.local.entity.SalaryType
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CartItem(
    val id: Long,
    val name: String,
    val price: Double,
    val costPrice: Double = 0.0,
    val type: String, // "SERVICE" or "PRODUCT"
    var quantity: Int = 1,
    var notes: String = "",
    val durationMinutes: Int = 30,
    val customCommission: Double? = null
)

data class TodayDashboardSummary(
    val totalRevenue: Double = 0.0,
    val transactionCount: Int = 0,
    val customerCount: Int = 0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val activeQueueCount: Int = 0,
    val topServiceName: String = "-",
    val topKapsterName: String = "-",
    val lowStockCount: Int = 0
)

class BarberRepository(private val db: AppDatabase) {

    // Services
    val allServices: Flow<List<ServiceEntity>> = db.serviceDao().getAllServices()
    val activeServices: Flow<List<ServiceEntity>> = db.serviceDao().getActiveServices()
    suspend fun insertService(service: ServiceEntity) = db.serviceDao().insertService(service)
    suspend fun updateService(service: ServiceEntity) = db.serviceDao().updateService(service)
    suspend fun deleteService(service: ServiceEntity) = db.serviceDao().deleteService(service)

    // Products
    val allProducts: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = db.productDao().getActiveProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = db.productDao().getLowStockProducts()
    suspend fun insertProduct(product: ProductEntity) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = db.productDao().deleteProduct(product)

    // Kapsters
    val allKapsters: Flow<List<KapsterEntity>> = db.kapsterDao().getAllKapsters()
    val activeKapsters: Flow<List<KapsterEntity>> = db.kapsterDao().getActiveKapsters()
    suspend fun insertKapster(kapster: KapsterEntity) = db.kapsterDao().insertKapster(kapster)
    suspend fun updateKapster(kapster: KapsterEntity) = db.kapsterDao().updateKapster(kapster)
    suspend fun deleteKapster(kapster: KapsterEntity) = db.kapsterDao().deleteKapster(kapster)

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = db.customerDao().getAllCustomers()
    suspend fun insertCustomer(customer: CustomerEntity) = db.customerDao().insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = db.customerDao().updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = db.customerDao().deleteCustomer(customer)
    suspend fun searchCustomers(q: String) = db.customerDao().searchCustomers(q)

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    fun getExpensesBetween(start: Long, end: Long) = db.expenseDao().getExpensesBetween(start, end)
    suspend fun insertExpense(expense: ExpenseEntity) = db.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = db.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = db.expenseDao().deleteExpense(expense)

    // Queues
    fun getTodayQueues(startOfDay: Long): Flow<List<QueueEntity>> = db.queueDao().getTodayQueues(startOfDay)
    fun getActiveQueues(startOfDay: Long): Flow<List<QueueEntity>> = db.queueDao().getActiveQueues(startOfDay)
    fun getActiveQueueCount(startOfDay: Long): Flow<Int> = db.queueDao().getActiveQueueCount(startOfDay)
    suspend fun insertQueue(queue: QueueEntity) = db.queueDao().insertQueue(queue)
    suspend fun updateQueue(queue: QueueEntity) = db.queueDao().updateQueue(queue)
    suspend fun deleteQueue(queue: QueueEntity) = db.queueDao().deleteQueue(queue)
    suspend fun updateQueueStatus(id: Long, status: QueueStatus) = db.queueDao().updateStatus(id, status)
    suspend fun updateQueueCalled(id: Long, timestamp: Long) = db.queueDao().updateStatusAndCalled(id, QueueStatus.CALLED, timestamp)
    suspend fun updateQueueCompleted(id: Long, timestamp: Long) = db.queueDao().updateStatusAndCompleted(id, QueueStatus.COMPLETED, timestamp)

    // Transactions
    val allTransactions: Flow<List<TransactionWithItems>> = db.transactionDao().getAllTransactions()
    fun getTransactionsBetween(start: Long, end: Long) = db.transactionDao().getTransactionsBetween(start, end)

    // Cash Advances & Salaries
    fun getCashAdvances(kapsterId: Long): Flow<List<CashAdvanceEntity>> = db.salaryDao().getCashAdvancesForKapster(kapsterId)
    suspend fun insertCashAdvance(advance: CashAdvanceEntity) = db.salaryDao().insertCashAdvance(advance)
    suspend fun getSalaryRecords(kapsterId: Long): Flow<List<SalaryRecordEntity>> = db.salaryDao().getSalaryRecordsForKapster(kapsterId)
    val allSalaryRecords: Flow<List<SalaryRecordEntity>> = db.salaryDao().getAllSalaryRecords()
    suspend fun insertSalaryRecord(record: SalaryRecordEntity) = db.salaryDao().insertSalaryRecord(record)
    suspend fun updateSalaryRecord(record: SalaryRecordEntity) = db.salaryDao().updateSalaryRecord(record)

    suspend fun checkoutTransaction(
        items: List<CartItem>,
        customerId: Long?,
        customerName: String,
        kapsterId: Long?,
        kapsterName: String?,
        subtotal: Double,
        discountAmount: Double,
        discountPercent: Double,
        tipAmount: Double,
        taxAmount: Double,
        totalAmount: Double,
        paymentMethod: String,
        cashGiven: Double,
        changeAmount: Double,
        notes: String,
        queueId: Long?
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val trxNumber = "TRX-${timeFormat.format(Date(now))}"

        val kapster = kapsterId?.let { db.kapsterDao().getKapsterById(it) }

        val transactionEntity = TransactionEntity(
            transactionNumber = trxNumber,
            timestamp = now,
            customerId = customerId,
            customerName = customerName.ifBlank { "Umum" },
            kapsterId = kapsterId,
            kapsterName = kapsterName,
            subtotal = subtotal,
            discountAmount = discountAmount,
            discountPercent = discountPercent,
            tipAmount = tipAmount,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            cashGiven = cashGiven,
            changeAmount = changeAmount,
            notes = notes,
            isVoided = false
        )

        val trxId = db.transactionDao().insertTransaction(transactionEntity)

        val itemEntities = items.map { item ->
            // Calculate commission for service if kapster is present
            var commission = 0.0
            if (item.type == "SERVICE" && kapster != null) {
                if (item.customCommission != null && item.customCommission > 0) {
                    commission = item.customCommission * item.quantity
                } else {
                    commission = when (kapster.salaryType) {
                        SalaryType.PERCENTAGE, SalaryType.BASE_PLUS_PERCENTAGE -> {
                            (item.price * item.quantity) * (kapster.salaryRate / 100.0)
                        }
                        SalaryType.FIXED_PER_SERVICE -> {
                            kapster.salaryRate * item.quantity
                        }
                        SalaryType.BASE_SALARY -> 0.0
                    }
                }
            }

            // Deduct product stock
            if (item.type == "PRODUCT") {
                db.productDao().decreaseStock(item.id, item.quantity)
            }

            TransactionItemEntity(
                transactionId = trxId,
                itemId = item.id,
                itemName = item.name,
                itemType = item.type,
                price = item.price,
                costPrice = item.costPrice,
                quantity = item.quantity,
                kapsterCommission = commission
            )
        }

        db.transactionDao().insertTransactionItems(itemEntities)

        // Update customer stats if selected
        if (customerId != null && customerId > 0) {
            db.customerDao().recordVisit(customerId, totalAmount, now)
        }

        // Complete queue if matched
        if (queueId != null && queueId > 0) {
            db.queueDao().updateStatusAndCompleted(queueId, QueueStatus.COMPLETED, now)
        }

        trxId
    }

    suspend fun voidTransaction(transactionId: Long) = withContext(Dispatchers.IO) {
        val trxWithItems = db.transactionDao().getTransactionById(transactionId) ?: return@withContext
        if (trxWithItems.transaction.isVoided) return@withContext

        // Restore product stock
        for (item in trxWithItems.items) {
            if (item.itemType == "PRODUCT") {
                db.productDao().increaseStock(item.itemId, item.quantity)
            }
        }

        db.transactionDao().markVoided(transactionId)
    }

    suspend fun getNextQueueNumber(startOfDay: Long): String = withContext(Dispatchers.IO) {
        val totalToday = db.queueDao().getTodayTotalCount(startOfDay)
        val nextSeq = totalToday + 1
        String.format(Locale.US, "A%03d", nextSeq)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        // Clear all tables and re-populate default services and products
        val database = db
        database.clearAllTables()
    }
}
