package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val durationMinutes: Int = 30,
    val category: String = "Layanan Utama",
    val isActive: Boolean = true,
    val customCommission: Double? = null // if set, specific commission for this service
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sellPrice: Double,
    val buyPrice: Double, // Modal / HPP
    val stock: Int = 0,
    val minStock: Int = 3,
    val category: String = "Produk",
    val isActive: Boolean = true
)

enum class SalaryType {
    PERCENTAGE,            // Bagi hasil % dari total layanan
    FIXED_PER_SERVICE,     // Nominal tetap per layanan (misal Rp 15.000/kepala)
    BASE_SALARY,           // Gaji pokok murni (Harian/Mingguan/Bulanan)
    BASE_PLUS_PERCENTAGE   // Gaji pokok + bagi hasil %
}

@Entity(tableName = "kapsters")
data class KapsterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val photoUri: String? = null,
    val isActive: Boolean = true,
    val salaryType: SalaryType = SalaryType.PERCENTAGE,
    val salaryRate: Double = 50.0, // e.g. 50% or Rp 15000
    val baseSalary: Double = 0.0,  // e.g. Rp 50.000 per hari / periode
    val baseSalaryPeriod: String = "HARIAN" // HARIAN, MINGGUAN, BULANAN
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val notes: String = "",
    val totalSpent: Double = 0.0,
    val visitCount: Int = 0,
    val lastVisitTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val category: String, // Sewa, Listrik & Air, Beli Stok, Perlengkapan, Makan/Minum, Lain-lain
    val amount: Double,
    val notes: String = ""
)

@Entity(
    tableName = "transactions",
    indices = [Index("timestamp"), Index("kapsterId"), Index("customerId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "Umum",
    val kapsterId: Long? = null,
    val kapsterName: String? = null,
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val tipAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "TUNAI", // TUNAI, QRIS, TRANSFER
    val cashGiven: Double = 0.0,
    val changeAmount: Double = 0.0,
    val notes: String = "",
    val isVoided: Boolean = false
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val itemId: Long,
    val itemName: String,
    val itemType: String, // "SERVICE" or "PRODUCT"
    val price: Double,
    val costPrice: Double = 0.0,
    val quantity: Int = 1,
    val kapsterCommission: Double = 0.0
)

enum class QueueStatus {
    WAITING,        // Menunggu
    CALLED,         // Dipanggil
    IN_SERVICE,     // Sedang Dilayani
    COMPLETED,      // Selesai
    CANCELLED       // Dilewati / Batal
}

@Entity(
    tableName = "queues",
    indices = [Index("createdTimestamp"), Index("status")]
)
data class QueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queueNumber: String, // e.g. "A001"
    val customerName: String = "Pelanggan",
    val customerPhone: String = "",
    val serviceId: Long? = null,
    val serviceName: String = "Potong Rambut",
    val serviceDuration: Int = 30, // in minutes
    val kapsterId: Long? = null,
    val kapsterName: String? = null,
    val chairNumber: String = "1",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val calledTimestamp: Long? = null,
    val completedTimestamp: Long? = null,
    val status: QueueStatus = QueueStatus.WAITING,
    val notes: String = ""
)

enum class CashAdvanceType {
    KASBON,
    BONUS,
    POTONGAN
}

@Entity(
    tableName = "cash_advances",
    indices = [Index("kapsterId"), Index("dateTimestamp")]
)
data class CashAdvanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kapsterId: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val amount: Double,
    val type: CashAdvanceType = CashAdvanceType.KASBON,
    val notes: String = "",
    val isSettled: Boolean = false
)

@Entity(
    tableName = "salary_records",
    indices = [Index("kapsterId")]
)
data class SalaryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kapsterId: Long,
    val kapsterName: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalServices: Int,
    val totalCommission: Double,
    val baseSalary: Double,
    val bonus: Double,
    val deduction: Double,
    val cashAdvance: Double,
    val netSalary: Double,
    val isPaid: Boolean = false,
    val paidTimestamp: Long? = null,
    val notes: String = ""
)
