package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.StorePreferencesManager
import com.example.database.AppDatabase
import com.example.domain.Audience
import com.example.domain.CartLineItem
import com.example.domain.MovementType
import com.example.domain.OrderItemDraft
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.domain.VariantInput
import com.example.repository.StoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StoreRepositoryRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var prefsManager: StorePreferencesManager
    private lateinit var repository: StoreRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefsManager = StorePreferencesManager(context)
        repository = StoreRepository(database, prefsManager)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `product and variants CRUD and stock adjustment work correctly`() = runTest {
        val productId = repository.saveProduct(
            productId = null,
            name = "Camiseta Básica",
            description = "Algodão premium",
            category = "Camisetas",
            audience = Audience.MASCULINO,
            photoUri = null,
            code = "cam-001",
            costPriceCents = 3000L,
            salePriceCents = 7990L,
            notes = "Linha essencial",
            variants = listOf(
                VariantInput(color = "Preta", size = "P", quantity = 5),
                VariantInput(color = "Preta", size = "M", quantity = 8),
                VariantInput(color = "Branca", size = "P", quantity = 3)
            )
        )

        val saved = repository.observeProduct(productId).first()
        assertNotNull(saved)
        assertEquals("CAM-001", saved!!.product.code)
        assertEquals(3, saved.variants.size)
        assertEquals(16, saved.totalStock)

        val blackM = saved.variants.first { it.color == "Preta" && it.size == "M" }
        repository.adjustVariantStock(productId, saved.product.name, blackM.id, 12, "Reposição")

        val updated = repository.observeProduct(productId).first()!!
        assertEquals(20, updated.totalStock)

        val movements = repository.observeMovementsByProduct(productId).first()
        assertTrue(movements.any { it.type == MovementType.ENTRADA && it.quantityDelta == 4 })
    }

    @Test
    fun `completeSale deducts stock atomically and prevents negative stock`() = runTest {
        val productId = repository.saveProduct(
            productId = null,
            name = "Calça Jeans",
            description = "",
            category = "Calças",
            audience = Audience.FEMININO,
            photoUri = null,
            code = "CAL-10",
            costPriceCents = 6000L,
            salePriceCents = 14000L,
            notes = "",
            variants = listOf(VariantInput(color = "Azul", size = "38", quantity = 2))
        )

        val product = repository.observeProduct(productId).first()!!
        val variant = product.variants.first()
        val clientId = repository.saveClient(null, "Maria Souza", "11999990000", "")
        val client = repository.observeClient(clientId).first()

        // Attempt to sell 3 when only 2 available -> must throw and leave stock untouched
        try {
            repository.completeSale(
                items = listOf(
                    CartLineItem(
                        productId = productId,
                        variantId = variant.id,
                        productName = product.product.name,
                        productCode = product.product.code,
                        color = variant.color,
                        size = variant.size,
                        quantity = 3,
                        availableStock = 2,
                        unitSalePriceCents = 14000L,
                        unitCostPriceCents = 6000L
                    )
                ),
                discountCents = 0L,
                paymentMethod = PaymentMethod.PIX,
                client = client,
                notes = ""
            )
            fail("Expected IllegalStateException for insufficient stock")
        } catch (_: IllegalStateException) {
            // Expected rollback
        }

        val afterFailed = repository.observeProduct(productId).first()!!
        assertEquals(2, afterFailed.totalStock)
        assertTrue(repository.observeSales().first().isEmpty())

        // Valid sale of 2 units with R$ 20,00 discount
        val saleId = repository.completeSale(
            items = listOf(
                CartLineItem(
                    productId = productId,
                    variantId = variant.id,
                    productName = product.product.name,
                    productCode = product.product.code,
                    color = variant.color,
                    size = variant.size,
                    quantity = 2,
                    availableStock = 2,
                    unitSalePriceCents = 14000L,
                    unitCostPriceCents = 6000L
                )
            ),
            discountCents = 2000L,
            paymentMethod = PaymentMethod.CREDITO,
            client = client,
            notes = "Parcelado"
        )

        val sales = repository.observeSales().first()
        assertEquals(1, sales.size)
        assertEquals(saleId, sales.first().sale.id)
        assertEquals(28000L, sales.first().sale.subtotalCents)
        assertEquals(26000L, sales.first().sale.totalCents)
        assertEquals(12000L, sales.first().sale.totalCostCents)
        assertEquals(0, repository.observeProduct(productId).first()!!.totalStock)
    }

    @Test
    fun `orders expenses and backup restore roundtrip preserve all data`() = runTest {
        val productId = repository.saveProduct(
            productId = null,
            name = "Jaqueta Couro",
            description = "",
            category = "Casacos",
            audience = Audience.MASCULINO,
            photoUri = null,
            code = "JAQ-01",
            costPriceCents = 15000L,
            salePriceCents = 32000L,
            notes = "",
            variants = listOf(VariantInput(color = "Preta", size = "G", quantity = 4))
        )
        val clientId = repository.saveClient(null, "Carlos Lima", "21988887777", "VIP")
        val orderId = repository.saveOrder(
            orderId = null,
            clientId = clientId,
            clientName = "Carlos Lima",
            clientPhone = "21988887777",
            items = listOf(
                OrderItemDraft(
                    productId = productId,
                    variantId = null,
                    productName = "Jaqueta Couro",
                    color = "Preta",
                    size = "GG",
                    quantity = 1,
                    unitPriceCents = 32000L
                )
            ),
            paidCents = 10000L,
            status = OrderStatus.PENDENTE,
            expectedDeliveryMillis = System.currentTimeMillis() + 86_400_000L,
            notes = "Encomenda tamanho GG"
        )
        repository.registerOrderPayment(orderId, 5000L)
        repository.updateOrderStatus(orderId, OrderStatus.EM_ANDAMENTO)

        repository.saveExpense(
            expenseId = null,
            description = "Embalagens personalizadas",
            category = "Operacional",
            amountCents = 8500L,
            dateMillis = System.currentTimeMillis(),
            notes = ""
        )

        val backupJson = repository.exportBackupJson()

        // Wipe database via restore of an empty state? Better: delete product, client, order, expense, then restore!
        repository.deleteProduct(productId)
        repository.deleteClient(clientId)
        repository.deleteOrder(orderId)
        val exp = repository.observeExpenses().first().first()
        repository.deleteExpense(exp.id)

        assertTrue(repository.observeProducts().first().isEmpty())
        assertTrue(repository.observeOrders().first().isEmpty())
        assertTrue(repository.observeExpenses().first().isEmpty())

        // Restore full backup
        repository.restoreBackupJson(backupJson)

        val restoredProducts = repository.observeProducts().first()
        val restoredOrders = repository.observeOrders().first()
        val restoredExpenses = repository.observeExpenses().first()
        val restoredClients = repository.observeClients().first()

        assertEquals(1, restoredProducts.size)
        assertEquals("JAQ-01", restoredProducts.first().product.code)
        assertEquals(1, restoredClients.size)
        assertEquals("Carlos Lima", restoredClients.first().fullName)
        assertEquals(1, restoredOrders.size)
        assertEquals(15000L, restoredOrders.first().order.paidCents)
        assertEquals(17000L, restoredOrders.first().order.remainingCents)
        assertEquals(OrderStatus.EM_ANDAMENTO, restoredOrders.first().order.status)
        assertEquals(1, restoredExpenses.size)
        assertEquals(8500L, restoredExpenses.first().amountCents)
    }
}
