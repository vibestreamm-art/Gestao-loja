package com.example.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.domain.OrderStatus
import com.example.entities.ClientEntity
import com.example.entities.ExpenseEntity
import com.example.entities.OrderEntity
import com.example.entities.OrderItemEntity
import com.example.entities.OrderWithItems
import com.example.entities.ProductEntity
import com.example.entities.ProductVariantEntity
import com.example.entities.ProductWithVariants
import com.example.entities.SaleEntity
import com.example.entities.SaleItemEntity
import com.example.entities.SaleWithItems
import com.example.entities.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAllProductsWithVariants(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun observeProductWithVariants(productId: Long): Flow<ProductWithVariants?>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductWithVariants(productId: Long): ProductWithVariants?

    @Transaction
    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsWithVariantsOnce(): List<ProductWithVariants>

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM product_variants WHERE id = :variantId LIMIT 1")
    suspend fun getVariantById(variantId: Long): ProductVariantEntity?

    @Query("SELECT * FROM product_variants WHERE productId = :productId ORDER BY color ASC, size ASC")
    suspend fun getVariantsForProduct(productId: Long): List<ProductVariantEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVariant(variant: ProductVariantEntity): Long

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ProductVariantEntity)

    @Query("UPDATE product_variants SET quantity = :newQuantity WHERE id = :variantId")
    suspend fun updateVariantQuantity(variantId: Long, newQuantity: Int)

    @Query("DELETE FROM product_variants WHERE productId = :productId")
    suspend fun deleteVariantsByProductId(productId: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun observeAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    fun observeClientById(clientId: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clients ORDER BY id ASC")
    suspend fun getAllClientsOnce(): List<ClientEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClientById(clientId: Long)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()
}

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAtMillis DESC")
    fun observeAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE clientId = :clientId ORDER BY createdAtMillis DESC")
    fun observeSalesByClient(clientId: Long): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE createdAtMillis BETWEEN :startMillis AND :endMillis ORDER BY createdAtMillis DESC")
    fun observeSalesInRange(startMillis: Long, endMillis: Long): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY id ASC")
    suspend fun getAllSalesWithItemsOnce(): List<SaleWithItems>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY orderDateMillis DESC")
    fun observeAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderWithItems(orderId: Long): OrderWithItems?

    @Transaction
    @Query("SELECT * FROM orders ORDER BY id ASC")
    suspend fun getAllOrdersWithItemsOnce(): List<OrderWithItems>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun observeAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observeExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY id ASC")
    suspend fun getAllExpensesOnce(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecentMovements(limit: Int = 150): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAtMillis DESC")
    fun observeMovementsByProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY id ASC")
    suspend fun getAllMovementsOnce(): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("DELETE FROM stock_movements")
    suspend fun deleteAllMovements()
}
