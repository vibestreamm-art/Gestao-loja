package com.example.repository

import androidx.room.withTransaction
import com.example.data.BackupManager
import com.example.data.BackupPayload
import com.example.data.StorePreferencesManager
import com.example.database.AppDatabase
import com.example.domain.Audience
import com.example.domain.CartLineItem
import com.example.domain.MovementType
import com.example.domain.OrderItemDraft
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.domain.StorePreferences
import com.example.domain.VariantInput
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
import kotlinx.coroutines.flow.first

class StoreRepository(
    private val database: AppDatabase,
    private val preferencesManager: StorePreferencesManager
) {
    private val productDao = database.productDao()
    private val clientDao = database.clientDao()
    private val saleDao = database.saleDao()
    private val orderDao = database.orderDao()
    private val expenseDao = database.expenseDao()
    private val movementDao = database.stockMovementDao()

    val preferencesFlow: Flow<StorePreferences> = preferencesManager.preferencesFlow

    fun observeProducts(): Flow<List<ProductWithVariants>> = productDao.observeAllProductsWithVariants()
    fun observeProduct(productId: Long): Flow<ProductWithVariants?> = productDao.observeProductWithVariants(productId)
    fun observeClients(): Flow<List<ClientEntity>> = clientDao.observeAllClients()
    fun observeClient(clientId: Long): Flow<ClientEntity?> = clientDao.observeClientById(clientId)
    fun observeSales(): Flow<List<SaleWithItems>> = saleDao.observeAllSalesWithItems()
    fun observeSalesByClient(clientId: Long): Flow<List<SaleWithItems>> = saleDao.observeSalesByClient(clientId)
    fun observeOrders(): Flow<List<OrderWithItems>> = orderDao.observeAllOrdersWithItems()
    fun observeExpenses(): Flow<List<ExpenseEntity>> = expenseDao.observeAllExpenses()
    fun observeRecentMovements(limit: Int = 200): Flow<List<StockMovementEntity>> = movementDao.observeRecentMovements(limit)
    fun observeMovementsByProduct(productId: Long): Flow<List<StockMovementEntity>> = movementDao.observeMovementsByProduct(productId)

    suspend fun saveProduct(
        productId: Long?,
        name: String,
        description: String,
        category: String,
        audience: Audience,
        photoUri: String?,
        code: String,
        costPriceCents: Long,
        salePriceCents: Long,
        notes: String,
        variants: List<VariantInput>
    ): Long {
        val normalizedCode = code.trim().uppercase()
        require(name.isNotBlank()) { "O nome do produto é obrigatório." }
        require(category.isNotBlank()) { "A categoria é obrigatória." }
        require(normalizedCode.isNotBlank()) { "O código de identificação é obrigatório." }
        require(variants.isNotEmpty()) { "Cadastre ao menos uma variação de cor e tamanho." }

        val duplicateKeys = variants
            .groupBy { "${it.color.trim().lowercase()}|${it.size.trim().uppercase()}" }
            .any { it.value.size > 1 }
        require(!duplicateKeys) { "Existem variações repetidas (mesma cor e tamanho)." }

        return database.withTransaction {
            val existingWithCode = productDao.getProductByCode(normalizedCode)
            if (existingWithCode != null && existingWithCode.id != productId) {
                throw IllegalArgumentException("Já existe um produto com o código $normalizedCode.")
            }

            val now = System.currentTimeMillis()
            val savedId: Long

            if (productId == null || productId == 0L) {
                savedId = productDao.insertProduct(
                    ProductEntity(
                        name = name.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        audience = audience,
                        photoUri = photoUri,
                        code = normalizedCode,
                        costPriceCents = costPriceCents,
                        salePriceCents = salePriceCents,
                        createdAtMillis = now,
                        notes = notes.trim()
                    )
                )
                variants.forEach { v ->
                    val cleanColor = v.color.trim()
                    val cleanSize = v.size.trim().uppercase()
                    val qty = v.quantity.coerceAtLeast(0)
                    val variantId = productDao.insertVariant(
                        ProductVariantEntity(
                            productId = savedId,
                            color = cleanColor,
                            size = cleanSize,
                            quantity = qty
                        )
                    )
                    if (qty > 0) {
                        movementDao.insertMovement(
                            StockMovementEntity(
                                productId = savedId,
                                variantId = variantId,
                                productNameSnapshot = name.trim(),
                                variantLabelSnapshot = "$cleanColor / $cleanSize",
                                type = MovementType.ENTRADA,
                                quantityDelta = qty,
                                previousQuantity = 0,
                                newQuantity = qty,
                                reason = "Cadastro inicial de estoque",
                                createdAtMillis = now
                            )
                        )
                    }
                }
            } else {
                val current = productDao.getProductWithVariants(productId)
                    ?: throw IllegalStateException("Produto não encontrado.")
                savedId = productId
                productDao.updateProduct(
                    current.product.copy(
                        name = name.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        audience = audience,
                        photoUri = photoUri,
                        code = normalizedCode,
                        costPriceCents = costPriceCents,
                        salePriceCents = salePriceCents,
                        notes = notes.trim()
                    )
                )

                val existingMap = current.variants.associateBy { it.id }
                val incomingIds = variants.mapNotNull { if (it.id > 0L) it.id else null }.toSet()

                // Remove deleted variants and log adjustment if quantity > 0
                current.variants.filter { it.id !in incomingIds }.forEach { removed ->
                    productDao.deleteVariant(removed)
                    if (removed.quantity > 0) {
                        movementDao.insertMovement(
                            StockMovementEntity(
                                productId = savedId,
                                variantId = removed.id,
                                productNameSnapshot = name.trim(),
                                variantLabelSnapshot = "${removed.color} / ${removed.size}",
                                type = MovementType.AJUSTE,
                                quantityDelta = -removed.quantity,
                                previousQuantity = removed.quantity,
                                newQuantity = 0,
                                reason = "Variação removida na edição",
                                createdAtMillis = now
                            )
                        )
                    }
                }

                // Update existing or insert new variants
                variants.forEach { v ->
                    val cleanColor = v.color.trim()
                    val cleanSize = v.size.trim().uppercase()
                    val newQty = v.quantity.coerceAtLeast(0)
                    val existingVariant = if (v.id > 0L) existingMap[v.id] else null

                    if (existingVariant != null) {
                        productDao.updateVariant(
                            existingVariant.copy(
                                color = cleanColor,
                                size = cleanSize,
                                quantity = newQty
                            )
                        )
                        val delta = newQty - existingVariant.quantity
                        if (delta != 0) {
                            movementDao.insertMovement(
                                StockMovementEntity(
                                    productId = savedId,
                                    variantId = existingVariant.id,
                                    productNameSnapshot = name.trim(),
                                    variantLabelSnapshot = "$cleanColor / $cleanSize",
                                    type = if (delta > 0) MovementType.ENTRADA else MovementType.AJUSTE,
                                    quantityDelta = delta,
                                    previousQuantity = existingVariant.quantity,
                                    newQuantity = newQty,
                                    reason = "Atualização via edição de produto",
                                    createdAtMillis = now
                                )
                            )
                        }
                    } else {
                        val variantId = productDao.insertVariant(
                            ProductVariantEntity(
                                productId = savedId,
                                color = cleanColor,
                                size = cleanSize,
                                quantity = newQty
                            )
                        )
                        if (newQty > 0) {
                            movementDao.insertMovement(
                                StockMovementEntity(
                                    productId = savedId,
                                    variantId = variantId,
                                    productNameSnapshot = name.trim(),
                                    variantLabelSnapshot = "$cleanColor / $cleanSize",
                                    type = MovementType.ENTRADA,
                                    quantityDelta = newQty,
                                    previousQuantity = 0,
                                    newQuantity = newQty,
                                    reason = "Nova variação cadastrada",
                                    createdAtMillis = now
                                )
                            )
                        }
                    }
                }
            }
            savedId
        }
    }

    suspend fun adjustVariantStock(
        productId: Long,
        productName: String,
        variantId: Long,
        newQuantity: Int,
        reason: String
    ) {
        require(newQuantity >= 0) { "A quantidade em estoque não pode ser negativa." }
        database.withTransaction {
            val variant = productDao.getVariantById(variantId)
                ?: throw IllegalStateException("Variação não encontrada.")
            val delta = newQuantity - variant.quantity
            if (delta == 0) return@withTransaction

            productDao.updateVariantQuantity(variantId, newQuantity)
            movementDao.insertMovement(
                StockMovementEntity(
                    productId = productId,
                    variantId = variantId,
                    productNameSnapshot = productName,
                    variantLabelSnapshot = "${variant.color} / ${variant.size}",
                    type = if (delta > 0) MovementType.ENTRADA else MovementType.AJUSTE,
                    quantityDelta = delta,
                    previousQuantity = variant.quantity,
                    newQuantity = newQuantity,
                    reason = reason.trim().ifEmpty { "Ajuste manual de estoque" },
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteProduct(productId: Long) {
        productDao.deleteProductById(productId)
    }

    suspend fun saveClient(clientId: Long?, fullName: String, phone: String, notes: String): Long {
        require(fullName.isNotBlank()) { "O nome completo do cliente é obrigatório." }
        return if (clientId == null || clientId == 0L) {
            clientDao.insertClient(
                ClientEntity(
                    fullName = fullName.trim(),
                    phone = phone.trim(),
                    notes = notes.trim(),
                    createdAtMillis = System.currentTimeMillis()
                )
            )
        } else {
            val existing = clientDao.observeClientById(clientId).first()
                ?: throw IllegalStateException("Cliente não encontrado.")
            clientDao.updateClient(
                existing.copy(
                    fullName = fullName.trim(),
                    phone = phone.trim(),
                    notes = notes.trim()
                )
            )
            clientId
        }
    }

    suspend fun deleteClient(clientId: Long) {
        clientDao.deleteClientById(clientId)
    }

    /**
     * Completes a sale inside an atomic Room transaction:
     * 1. Validates stock availability for every item (prevents negative stock).
     * 2. Deducts stock quantities.
     * 3. Inserts SaleEntity + SaleItemEntity list.
     * 4. Logs StockMovementEntity records.
     */
    suspend fun completeSale(
        items: List<CartLineItem>,
        discountCents: Long,
        paymentMethod: PaymentMethod,
        client: ClientEntity?,
        notes: String
    ): Long {
        require(items.isNotEmpty()) { "Adicione ao menos um item à venda." }
        val subtotalCents = items.sumOf { it.lineTotalCents }
        require(discountCents >= 0L) { "O desconto não pode ser negativo." }
        require(discountCents <= subtotalCents) { "O desconto não pode ser maior que o subtotal." }

        val totalCents = subtotalCents - discountCents
        val totalCostCents = items.sumOf { it.lineCostCents }
        val now = System.currentTimeMillis()

        return database.withTransaction {
            val pendingMovements = mutableListOf<StockMovementEntity>()

            items.forEach { line ->
                val dbVariant = productDao.getVariantById(line.variantId)
                    ?: throw IllegalStateException("Variação de '${line.productName}' não encontrada.")
                if (dbVariant.quantity < line.quantity) {
                    throw IllegalStateException(
                        "Estoque insuficiente para ${line.productName} (${line.color}/${line.size}). Disponível: ${dbVariant.quantity}."
                    )
                }
                val newQty = dbVariant.quantity - line.quantity
                productDao.updateVariantQuantity(dbVariant.id, newQty)
                pendingMovements.add(
                    StockMovementEntity(
                        productId = line.productId,
                        variantId = line.variantId,
                        productNameSnapshot = line.productName,
                        variantLabelSnapshot = "${line.color} / ${line.size}",
                        type = MovementType.SAIDA_VENDA,
                        quantityDelta = -line.quantity,
                        previousQuantity = dbVariant.quantity,
                        newQuantity = newQty,
                        reason = "Venda registrada (${paymentMethod.label})",
                        createdAtMillis = now
                    )
                )
            }

            val saleId = saleDao.insertSale(
                SaleEntity(
                    clientId = client?.id,
                    clientNameSnapshot = client?.fullName,
                    subtotalCents = subtotalCents,
                    discountCents = discountCents,
                    totalCents = totalCents,
                    totalCostCents = totalCostCents,
                    paymentMethod = paymentMethod,
                    createdAtMillis = now,
                    notes = notes.trim()
                )
            )

            saleDao.insertSaleItems(
                items.map { line ->
                    SaleItemEntity(
                        saleId = saleId,
                        productId = line.productId,
                        variantId = line.variantId,
                        productNameSnapshot = line.productName,
                        productCodeSnapshot = line.productCode,
                        colorSnapshot = line.color,
                        sizeSnapshot = line.size,
                        quantity = line.quantity,
                        unitSalePriceCents = line.unitSalePriceCents,
                        unitCostPriceCents = line.unitCostPriceCents,
                        lineTotalCents = line.lineTotalCents,
                        lineCostCents = line.lineCostCents
                    )
                }
            )

            movementDao.insertMovements(pendingMovements)
            saleId
        }
    }

    suspend fun saveOrder(
        orderId: Long?,
        clientId: Long?,
        clientName: String,
        clientPhone: String,
        items: List<OrderItemDraft>,
        paidCents: Long,
        status: OrderStatus,
        expectedDeliveryMillis: Long?,
        notes: String
    ): Long {
        require(clientName.isNotBlank()) { "Informe o nome do cliente para o pedido." }
        require(items.isNotEmpty()) { "Adicione ao menos um item ao pedido." }
        val totalCents = items.sumOf { it.lineTotalCents }
        require(paidCents >= 0L) { "O valor pago não pode ser negativo." }
        require(paidCents <= totalCents) { "O valor pago não pode exceder o total do pedido." }

        return database.withTransaction {
            val now = System.currentTimeMillis()
            val savedOrderId: Long
            if (orderId == null || orderId == 0L) {
                savedOrderId = orderDao.insertOrder(
                    OrderEntity(
                        clientId = clientId,
                        clientNameSnapshot = clientName.trim(),
                        clientPhoneSnapshot = clientPhone.trim(),
                        totalCents = totalCents,
                        paidCents = paidCents,
                        status = status,
                        orderDateMillis = now,
                        expectedDeliveryMillis = expectedDeliveryMillis,
                        notes = notes.trim()
                    )
                )
            } else {
                val current = orderDao.getOrderWithItems(orderId)?.order
                    ?: throw IllegalStateException("Pedido não encontrado.")
                savedOrderId = orderId
                orderDao.updateOrder(
                    current.copy(
                        clientId = clientId,
                        clientNameSnapshot = clientName.trim(),
                        clientPhoneSnapshot = clientPhone.trim(),
                        totalCents = totalCents,
                        paidCents = paidCents,
                        status = status,
                        expectedDeliveryMillis = expectedDeliveryMillis,
                        notes = notes.trim()
                    )
                )
                orderDao.deleteOrderItemsByOrderId(orderId)
            }

            orderDao.insertOrderItems(
                items.map { draft ->
                    OrderItemEntity(
                        orderId = savedOrderId,
                        productId = draft.productId,
                        variantId = draft.variantId,
                        productName = draft.productName.trim(),
                        color = draft.color.trim(),
                        size = draft.size.trim().uppercase(),
                        quantity = draft.quantity,
                        unitPriceCents = draft.unitPriceCents,
                        unitCostCents = draft.unitCostCents
                    )
                }
            )
            savedOrderId
        }
    }

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun registerOrderPayment(orderId: Long, additionalCents: Long) {
        require(additionalCents > 0L) { "Informe um valor maior que zero." }
        database.withTransaction {
            val current = orderDao.getOrderWithItems(orderId)?.order
                ?: throw IllegalStateException("Pedido não encontrado.")
            val newPaid = (current.paidCents + additionalCents).coerceAtMost(current.totalCents)
            orderDao.updateOrder(current.copy(paidCents = newPaid))
        }
    }

    suspend fun deleteOrder(orderId: Long) {
        orderDao.deleteOrderById(orderId)
    }

    suspend fun saveExpense(
        expenseId: Long?,
        description: String,
        category: String,
        amountCents: Long,
        dateMillis: Long,
        notes: String
    ): Long {
        require(description.isNotBlank()) { "A descrição da despesa é obrigatória." }
        require(category.isNotBlank()) { "A categoria da despesa é obrigatória." }
        require(amountCents > 0L) { "O valor da despesa deve ser maior que zero." }

        return if (expenseId == null || expenseId == 0L) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    description = description.trim(),
                    category = category.trim(),
                    amountCents = amountCents,
                    dateMillis = dateMillis,
                    notes = notes.trim()
                )
            )
        } else {
            expenseDao.updateExpense(
                ExpenseEntity(
                    id = expenseId,
                    description = description.trim(),
                    category = category.trim(),
                    amountCents = amountCents,
                    dateMillis = dateMillis,
                    notes = notes.trim()
                )
            )
            expenseId
        }
    }

    suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }

    suspend fun updateStoreIdentity(storeName: String, businessInfo: String) {
        preferencesManager.updateStoreIdentity(storeName, businessInfo)
    }

    suspend fun updateStorePreferences(
        defaultMinStock: Int,
        showLowStockAlerts: Boolean,
        confirmBeforeCompletingSale: Boolean
    ) {
        preferencesManager.updatePreferences(defaultMinStock, showLowStockAlerts, confirmBeforeCompletingSale)
    }

    suspend fun exportBackupJson(): String {
        val payload = BackupPayload(
            version = 1,
            exportedAtMillis = System.currentTimeMillis(),
            preferences = preferencesFlow.first(),
            products = productDao.getAllProductsWithVariantsOnce(),
            clients = clientDao.getAllClientsOnce(),
            sales = saleDao.getAllSalesWithItemsOnce(),
            orders = orderDao.getAllOrdersWithItemsOnce(),
            expenses = expenseDao.getAllExpensesOnce(),
            movements = movementDao.getAllMovementsOnce()
        )
        return BackupManager.toJson(payload)
    }

    suspend fun restoreBackupJson(json: String) {
        val payload = BackupManager.fromJson(json)
        database.withTransaction {
            movementDao.deleteAllMovements()
            expenseDao.deleteAllExpenses()
            orderDao.deleteAllOrders()
            saleDao.deleteAllSales()
            productDao.deleteAllProducts()
            clientDao.deleteAllClients()

            payload.clients.forEach { clientDao.insertClient(it) }
            payload.products.forEach { pwv ->
                productDao.insertProduct(pwv.product)
                pwv.variants.forEach { v -> productDao.insertVariant(v) }
            }
            payload.sales.forEach { swi ->
                saleDao.insertSale(swi.sale)
                if (swi.items.isNotEmpty()) {
                    saleDao.insertSaleItems(swi.items)
                }
            }
            payload.orders.forEach { owi ->
                orderDao.insertOrder(owi.order)
                if (owi.items.isNotEmpty()) {
                    orderDao.insertOrderItems(owi.items)
                }
            }
            payload.expenses.forEach { expenseDao.insertExpense(it) }
            if (payload.movements.isNotEmpty()) {
                movementDao.insertMovements(payload.movements)
            }
        }
        preferencesManager.restoreFromBackup(payload.preferences)
    }
}
