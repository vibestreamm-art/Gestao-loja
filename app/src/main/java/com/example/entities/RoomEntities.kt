package com.example.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.domain.Audience
import com.example.domain.MovementType
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["category"]),
        Index(value = ["audience"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val category: String,
    val audience: Audience = Audience.UNISSEX,
    val photoUri: String? = null,
    val code: String,
    val costPriceCents: Long,
    val salePriceCents: Long,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["productId", "color", "size"], unique = true),
        Index(value = ["quantity"])
    ]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val color: String,
    val size: String,
    val quantity: Int
)

@Entity(
    tableName = "clients",
    indices = [Index(value = ["fullName"])]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val phone: String = "",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["createdAtMillis"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val clientId: Long? = null,
    val clientNameSnapshot: String? = null,
    val subtotalCents: Long,
    val discountCents: Long = 0L,
    val totalCents: Long,
    val totalCostCents: Long,
    val paymentMethod: PaymentMethod,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val saleId: Long,
    val productId: Long,
    val variantId: Long,
    val productNameSnapshot: String,
    val productCodeSnapshot: String,
    val colorSnapshot: String,
    val sizeSnapshot: String,
    val quantity: Int,
    val unitSalePriceCents: Long,
    val unitCostPriceCents: Long,
    val lineTotalCents: Long,
    val lineCostCents: Long
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["status"]),
        Index(value = ["orderDateMillis"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val clientId: Long? = null,
    val clientNameSnapshot: String,
    val clientPhoneSnapshot: String = "",
    val totalCents: Long,
    val paidCents: Long = 0L,
    val status: OrderStatus = OrderStatus.PENDENTE,
    val orderDateMillis: Long = System.currentTimeMillis(),
    val expectedDeliveryMillis: Long? = null,
    val notes: String = ""
) {
    val remainingCents: Long get() = (totalCents - paidCents).coerceAtLeast(0L)
}

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderId: Long,
    val productId: Long? = null,
    val variantId: Long? = null,
    val productName: String,
    val color: String,
    val size: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val unitCostCents: Long = 0L
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["dateMillis"]),
        Index(value = ["category"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val description: String,
    val category: String,
    val amountCents: Long,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "stock_movements",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["createdAtMillis"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val variantId: Long,
    val productNameSnapshot: String,
    val variantLabelSnapshot: String,
    val type: MovementType,
    val quantityDelta: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ProductWithVariants(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val variants: List<ProductVariantEntity>
) {
    val totalStock: Int get() = variants.sumOf { it.quantity }
}

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
)

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)
