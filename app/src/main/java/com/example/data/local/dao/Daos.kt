package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.CashAdvanceEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.QueueStatus
import com.example.data.local.entity.SalaryRecordEntity
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY category ASC, name ASC")
    fun getActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: Long): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY category ASC, name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= minStock AND isActive = 1 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, quantity: Int)

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: Long, quantity: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface KapsterDao {
    @Query("SELECT * FROM kapsters ORDER BY isActive DESC, name ASC")
    fun getAllKapsters(): Flow<List<KapsterEntity>>

    @Query("SELECT * FROM kapsters WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveKapsters(): Flow<List<KapsterEntity>>

    @Query("SELECT * FROM kapsters WHERE id = :id LIMIT 1")
    suspend fun getKapsterById(id: Long): KapsterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKapster(kapster: KapsterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKapsters(kapsters: List<KapsterEntity>)

    @Update
    suspend fun updateKapster(kapster: KapsterEntity)

    @Delete
    suspend fun deleteKapster(kapster: KapsterEntity)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY lastVisitTimestamp DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' LIMIT 30")
    suspend fun searchCustomers(query: String): List<CustomerEntity>

    @Query("UPDATE customers SET visitCount = visitCount + 1, totalSpent = totalSpent + :amount, lastVisitTimestamp = :timestamp WHERE id = :id")
    suspend fun recordVisit(id: Long, amount: Double, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateTimestamp BETWEEN :startTime AND :endTime ORDER BY dateTimestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE dateTimestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalExpenseBetween(startTime: Long, endTime: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

data class TransactionWithItems(
    @androidx.room.Embedded val transaction: TransactionEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItemEntity>
)

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionWithItems>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionWithItems>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionWithItems?

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransaction(transactionId: Long): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE transactionId IN (SELECT id FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime AND isVoided = 0)")
    fun getItemsBetween(startTime: Long, endTime: Long): Flow<List<TransactionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("UPDATE transactions SET isVoided = 1 WHERE id = :id")
    suspend fun markVoided(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime AND isVoided = 0")
    suspend fun getCountBetween(startTime: Long, endTime: Long): Int

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime AND isVoided = 0")
    suspend fun getTotalSalesBetween(startTime: Long, endTime: Long): Double?
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queues WHERE createdTimestamp >= :startOfDayTimestamp ORDER BY CASE status WHEN 'CALLED' THEN 1 WHEN 'IN_SERVICE' THEN 2 WHEN 'WAITING' THEN 3 WHEN 'COMPLETED' THEN 4 ELSE 5 END, createdTimestamp ASC")
    fun getTodayQueues(startOfDayTimestamp: Long): Flow<List<QueueEntity>>

    @Query("SELECT * FROM queues WHERE createdTimestamp >= :startOfDayTimestamp AND status IN ('WAITING', 'CALLED', 'IN_SERVICE') ORDER BY CASE status WHEN 'CALLED' THEN 1 WHEN 'IN_SERVICE' THEN 2 ELSE 3 END, createdTimestamp ASC")
    fun getActiveQueues(startOfDayTimestamp: Long): Flow<List<QueueEntity>>

    @Query("SELECT * FROM queues WHERE id = :id LIMIT 1")
    suspend fun getQueueById(id: Long): QueueEntity?

    @Query("SELECT COUNT(*) FROM queues WHERE createdTimestamp >= :startOfDayTimestamp AND status IN ('WAITING', 'CALLED', 'IN_SERVICE')")
    fun getActiveQueueCount(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM queues WHERE createdTimestamp >= :startOfDayTimestamp")
    suspend fun getTodayTotalCount(startOfDayTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(queue: QueueEntity): Long

    @Update
    suspend fun updateQueue(queue: QueueEntity)

    @Query("UPDATE queues SET status = :status, calledTimestamp = :timestamp WHERE id = :id")
    suspend fun updateStatusAndCalled(id: Long, status: QueueStatus, timestamp: Long)

    @Query("UPDATE queues SET status = :status, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateStatusAndCompleted(id: Long, status: QueueStatus, timestamp: Long)

    @Query("UPDATE queues SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: QueueStatus)

    @Delete
    suspend fun deleteQueue(queue: QueueEntity)
}

@Dao
interface SalaryDao {
    @Query("SELECT * FROM cash_advances WHERE kapsterId = :kapsterId ORDER BY dateTimestamp DESC")
    fun getCashAdvancesForKapster(kapsterId: Long): Flow<List<CashAdvanceEntity>>

    @Query("SELECT * FROM cash_advances WHERE kapsterId = :kapsterId AND isSettled = 0")
    suspend fun getUnsettledCashAdvances(kapsterId: Long): List<CashAdvanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashAdvance(advance: CashAdvanceEntity): Long

    @Update
    suspend fun updateCashAdvance(advance: CashAdvanceEntity)

    @Query("UPDATE cash_advances SET isSettled = 1 WHERE kapsterId = :kapsterId AND isSettled = 0")
    suspend fun settleCashAdvances(kapsterId: Long)

    @Query("SELECT * FROM salary_records WHERE kapsterId = :kapsterId ORDER BY periodEnd DESC")
    fun getSalaryRecordsForKapster(kapsterId: Long): Flow<List<SalaryRecordEntity>>

    @Query("SELECT * FROM salary_records ORDER BY periodEnd DESC")
    fun getAllSalaryRecords(): Flow<List<SalaryRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryRecord(record: SalaryRecordEntity): Long

    @Update
    suspend fun updateSalaryRecord(record: SalaryRecordEntity)
}
