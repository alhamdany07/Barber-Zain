package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.KapsterDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.QueueDao
import com.example.data.local.dao.SalaryDao
import com.example.data.local.dao.ServiceDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.CashAdvanceEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.KapsterEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.QueueEntity
import com.example.data.local.entity.SalaryRecordEntity
import com.example.data.local.entity.SalaryType
import com.example.data.local.entity.ServiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ServiceEntity::class,
        ProductEntity::class,
        KapsterEntity::class,
        CustomerEntity::class,
        ExpenseEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        QueueEntity::class,
        CashAdvanceEntity::class,
        SalaryRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun productDao(): ProductDao
    abstract fun kapsterDao(): KapsterDao
    abstract fun customerDao(): CustomerDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun queueDao(): QueueDao
    abstract fun salaryDao(): SalaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pangkas_zain_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Seed Services
            val initialServices = listOf(
                ServiceEntity(name = "Potong Rambut Dewasa", price = 35000.0, durationMinutes = 30, category = "Layanan Utama", isActive = true),
                ServiceEntity(name = "Potong Rambut Anak", price = 30000.0, durationMinutes = 25, category = "Layanan Utama", isActive = true),
                ServiceEntity(name = "Cukur Kumis & Jenggot", price = 15000.0, durationMinutes = 15, category = "Grooming", isActive = true),
                ServiceEntity(name = "Cuci Rambut + Pijat Kepala", price = 25000.0, durationMinutes = 20, category = "Treatment", isActive = true),
                ServiceEntity(name = "Paket Ganteng Lengkap (Potong + Cuci + Cukur)", price = 60000.0, durationMinutes = 45, category = "Paket Hemat", isActive = true),
                ServiceEntity(name = "Creambath & Relaksasi", price = 50000.0, durationMinutes = 40, category = "Treatment", isActive = true),
                ServiceEntity(name = "Pewarnaan Rambut / Hair Color", price = 75000.0, durationMinutes = 60, category = "Coloring", isActive = true),
                ServiceEntity(name = "Hair Tattoo / Tribal Line", price = 20000.0, durationMinutes = 15, category = "Layanan Utama", isActive = true)
            )
            database.serviceDao().insertServices(initialServices)

            // Seed Products
            val initialProducts = listOf(
                ProductEntity(name = "Pomade Matte Clay 100g", sellPrice = 55000.0, buyPrice = 35000.0, stock = 18, minStock = 4, category = "Pomade & Styling", isActive = true),
                ProductEntity(name = "Oil Based Pomade Strong 100g", sellPrice = 50000.0, buyPrice = 30000.0, stock = 12, minStock = 3, category = "Pomade & Styling", isActive = true),
                ProductEntity(name = "Hair Tonic Ginseng 150ml", sellPrice = 40000.0, buyPrice = 24000.0, stock = 8, minStock = 3, category = "Perawatan", isActive = true),
                ProductEntity(name = "Shaving Foam Cool Menthol 200ml", sellPrice = 35000.0, buyPrice = 20000.0, stock = 5, minStock = 3, category = "Cukur", isActive = true),
                ProductEntity(name = "Beard Oil Organic 30ml", sellPrice = 45000.0, buyPrice = 28000.0, stock = 10, minStock = 2, category = "Grooming", isActive = true),
                ProductEntity(name = "Hair Spray Extreme Hold 250ml", sellPrice = 38000.0, buyPrice = 23000.0, stock = 2, minStock = 3, category = "Pomade & Styling", isActive = true) // Trigger low stock
            )
            database.productDao().insertProducts(initialProducts)

            // Seed Kapsters
            val initialKapsters = listOf(
                KapsterEntity(name = "Zain (Owner)", phone = "0812-3456-7890", isActive = true, salaryType = SalaryType.PERCENTAGE, salaryRate = 60.0),
                KapsterEntity(name = "Budi Santoso", phone = "0813-9876-5432", isActive = true, salaryType = SalaryType.PERCENTAGE, salaryRate = 50.0),
                KapsterEntity(name = "Rian Pratama", phone = "0857-1122-3344", isActive = true, salaryType = SalaryType.FIXED_PER_SERVICE, salaryRate = 18000.0)
            )
            database.kapsterDao().insertKapsters(initialKapsters)

            // Seed Customers
            val initialCustomers = listOf(
                CustomerEntity(name = "Mas Dimas", phone = "0812-9988-7766", notes = "Suka gaya Fade low taper, langganan tetap", totalSpent = 175000.0, visitCount = 4),
                CustomerEntity(name = "Pak Hendra", phone = "0813-4455-6677", notes = "Rambut samping tipis, cukur kumis rapi", totalSpent = 120000.0, visitCount = 2),
                CustomerEntity(name = "Bro Kevin", phone = "0878-1234-5678", notes = "Sering ambil Pomade Clay", totalSpent = 110000.0, visitCount = 2)
            )
            database.customerDao().insertCustomers(initialCustomers)

            // Seed Expenses
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val initialExpenses = listOf(
                ExpenseEntity(dateTimestamp = now - dayMillis, category = "Perlengkapan", amount = 45000.0, notes = "Beli silet dorco & tisu leher"),
                ExpenseEntity(dateTimestamp = now - 2 * dayMillis, category = "Listrik & Air", amount = 150000.0, notes = "Token listrik barbershop")
            )
            for (expense in initialExpenses) {
                database.expenseDao().insertExpense(expense)
            }
        }
    }
}
