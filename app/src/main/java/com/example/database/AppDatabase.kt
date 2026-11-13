package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.dao.ClientDao
import com.example.dao.ExpenseDao
import com.example.dao.OrderDao
import com.example.dao.ProductDao
import com.example.dao.SaleDao
import com.example.dao.StockMovementDao
import com.example.domain.Audience
import com.example.domain.MovementType
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.entities.ClientEntity
import com.example.entities.ExpenseEntity
import com.example.entities.OrderEntity
import com.example.entities.OrderItemEntity
import com.example.entities.ProductEntity
import com.example.entities.ProductVariantEntity
import com.example.entities.SaleEntity
import com.example.entities.SaleItemEntity
import com.example.entities.StockMovementEntity

class RoomConverters {
    @TypeConverter
    fun fromAudience(value: Audience): String = value.name

    @TypeConverter
    fun toAudience(value: String): Audience = Audience.fromString(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.fromString(value)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.fromString(value)

    @TypeConverter
    fun fromMovementType(value: MovementType): String = value.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.fromString(value)
}

@Database(
    entities = [
        ProductEntity::class,
        ProductVariantEntity::class,
        ClientEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ExpenseEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun saleDao(): SaleDao
    abstract fun orderDao(): OrderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        private const val DATABASE_NAME = "gestao_store.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // Enables safe schema updates in local offline builds without crashing on minor schema tweaks
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
