package com.example.data

import com.example.domain.Audience
import com.example.domain.MovementType
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.domain.StorePreferences
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
import org.json.JSONArray
import org.json.JSONObject

data class BackupPayload(
    val version: Int,
    val exportedAtMillis: Long,
    val preferences: StorePreferences,
    val products: List<ProductWithVariants>,
    val clients: List<ClientEntity>,
    val sales: List<SaleWithItems>,
    val orders: List<OrderWithItems>,
    val expenses: List<ExpenseEntity>,
    val movements: List<StockMovementEntity>
)

object BackupManager {
    private const val BACKUP_VERSION = 1

    fun toJson(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportedAtMillis", payload.exportedAtMillis)

        root.put("preferences", JSONObject().apply {
            put("storeName", payload.preferences.storeName)
            put("businessInfo", payload.preferences.businessInfo)
            put("defaultMinStock", payload.preferences.defaultMinStock)
            put("showLowStockAlerts", payload.preferences.showLowStockAlerts)
            put("confirmBeforeCompletingSale", payload.preferences.confirmBeforeCompletingSale)
        })

        val productsArr = JSONArray()
        payload.products.forEach { pwv ->
            val p = pwv.product
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("description", p.description)
                put("category", p.category)
                put("audience", p.audience.name)
                put("photoUri", p.photoUri ?: JSONObject.NULL)
                put("code", p.code)
                put("costPriceCents", p.costPriceCents)
                put("salePriceCents", p.salePriceCents)
                put("createdAtMillis", p.createdAtMillis)
                put("notes", p.notes)
                val vArr = JSONArray()
                pwv.variants.forEach { v ->
                    vArr.put(JSONObject().apply {
                        put("id", v.id)
                        put("productId", v.productId)
                        put("color", v.color)
                        put("size", v.size)
                        put("quantity", v.quantity)
                    })
                }
                put("variants", vArr)
            }
            productsArr.put(pObj)
        }
        root.put("products", productsArr)

        val clientsArr = JSONArray()
        payload.clients.forEach { c ->
            clientsArr.put(JSONObject().apply {
                put("id", c.id)
                put("fullName", c.fullName)
                put("phone", c.phone)
                put("notes", c.notes)
                put("createdAtMillis", c.createdAtMillis)
            })
        }
        root.put("clients", clientsArr)

        val salesArr = JSONArray()
        payload.sales.forEach { swi ->
            val s = swi.sale
            salesArr.put(JSONObject().apply {
                put("id", s.id)
                put("clientId", s.clientId ?: JSONObject.NULL)
                put("clientNameSnapshot", s.clientNameSnapshot ?: JSONObject.NULL)
                put("subtotalCents", s.subtotalCents)
                put("discountCents", s.discountCents)
                put("totalCents", s.totalCents)
                put("totalCostCents", s.totalCostCents)
                put("paymentMethod", s.paymentMethod.name)
                put("createdAtMillis", s.createdAtMillis)
                put("notes", s.notes)
                val iArr = JSONArray()
                swi.items.forEach { item ->
                    iArr.put(JSONObject().apply {
                        put("id", item.id)
                        put("saleId", item.saleId)
                        put("productId", item.productId)
                        put("variantId", item.variantId)
                        put("productNameSnapshot", item.productNameSnapshot)
                        put("productCodeSnapshot", item.productCodeSnapshot)
                        put("colorSnapshot", item.colorSnapshot)
                        put("sizeSnapshot", item.sizeSnapshot)
                        put("quantity", item.quantity)
                        put("unitSalePriceCents", item.unitSalePriceCents)
                        put("unitCostPriceCents", item.unitCostPriceCents)
                        put("lineTotalCents", item.lineTotalCents)
                        put("lineCostCents", item.lineCostCents)
                    })
                }
                put("items", iArr)
            })
        }
        root.put("sales", salesArr)

        val ordersArr = JSONArray()
        payload.orders.forEach { owi ->
            val o = owi.order
            ordersArr.put(JSONObject().apply {
                put("id", o.id)
                put("clientId", o.clientId ?: JSONObject.NULL)
                put("clientNameSnapshot", o.clientNameSnapshot)
                put("clientPhoneSnapshot", o.clientPhoneSnapshot)
                put("totalCents", o.totalCents)
                put("paidCents", o.paidCents)
                put("status", o.status.name)
                put("orderDateMillis", o.orderDateMillis)
                put("expectedDeliveryMillis", o.expectedDeliveryMillis ?: JSONObject.NULL)
                put("notes", o.notes)
                val iArr = JSONArray()
                owi.items.forEach { item ->
                    iArr.put(JSONObject().apply {
                        put("id", item.id)
                        put("orderId", item.orderId)
                        put("productId", item.productId ?: JSONObject.NULL)
                        put("variantId", item.variantId ?: JSONObject.NULL)
                        put("productName", item.productName)
                        put("color", item.color)
                        put("size", item.size)
                        put("quantity", item.quantity)
                        put("unitPriceCents", item.unitPriceCents)
                        put("unitCostCents", item.unitCostCents)
                    })
                }
                put("items", iArr)
            })
        }
        root.put("orders", ordersArr)

        val expensesArr = JSONArray()
        payload.expenses.forEach { e ->
            expensesArr.put(JSONObject().apply {
                put("id", e.id)
                put("description", e.description)
                put("category", e.category)
                put("amountCents", e.amountCents)
                put("dateMillis", e.dateMillis)
                put("notes", e.notes)
            })
        }
        root.put("expenses", expensesArr)

        val movementsArr = JSONArray()
        payload.movements.forEach { m ->
            movementsArr.put(JSONObject().apply {
                put("id", m.id)
                put("productId", m.productId)
                put("variantId", m.variantId)
                put("productNameSnapshot", m.productNameSnapshot)
                put("variantLabelSnapshot", m.variantLabelSnapshot)
                put("type", m.type.name)
                put("quantityDelta", m.quantityDelta)
                put("previousQuantity", m.previousQuantity)
                put("newQuantity", m.newQuantity)
                put("reason", m.reason)
                put("createdAtMillis", m.createdAtMillis)
            })
        }
        root.put("movements", movementsArr)

        return root.toString(2)
    }

    fun fromJson(json: String): BackupPayload {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        val exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis())

        val prefObj = root.optJSONObject("preferences")
        val preferences = if (prefObj != null) {
            StorePreferences(
                storeName = prefObj.optString("storeName", "Gestão Store"),
                businessInfo = prefObj.optString("businessInfo", "Moda Masculina & Feminina"),
                defaultMinStock = prefObj.optInt("defaultMinStock", 3).coerceAtLeast(1),
                showLowStockAlerts = prefObj.optBoolean("showLowStockAlerts", true),
                confirmBeforeCompletingSale = prefObj.optBoolean("confirmBeforeCompletingSale", true)
            )
        } else {
            StorePreferences()
        }

        val products = mutableListOf<ProductWithVariants>()
        val productsArr = root.optJSONArray("products") ?: JSONArray()
        for (i in 0 until productsArr.length()) {
            val obj = productsArr.getJSONObject(i)
            val product = ProductEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                description = obj.optString("description", ""),
                category = obj.getString("category"),
                audience = Audience.fromString(obj.optString("audience", "UNISSEX")),
                photoUri = if (obj.isNull("photoUri")) null else obj.optString("photoUri"),
                code = obj.getString("code"),
                costPriceCents = obj.getLong("costPriceCents"),
                salePriceCents = obj.getLong("salePriceCents"),
                createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                notes = obj.optString("notes", "")
            )
            val variants = mutableListOf<ProductVariantEntity>()
            val vArr = obj.optJSONArray("variants") ?: JSONArray()
            for (j in 0 until vArr.length()) {
                val vObj = vArr.getJSONObject(j)
                variants.add(
                    ProductVariantEntity(
                        id = vObj.getLong("id"),
                        productId = vObj.getLong("productId"),
                        color = vObj.getString("color"),
                        size = vObj.getString("size"),
                        quantity = vObj.getInt("quantity").coerceAtLeast(0)
                    )
                )
            }
            products.add(ProductWithVariants(product, variants))
        }

        val clients = mutableListOf<ClientEntity>()
        val clientsArr = root.optJSONArray("clients") ?: JSONArray()
        for (i in 0 until clientsArr.length()) {
            val obj = clientsArr.getJSONObject(i)
            clients.add(
                ClientEntity(
                    id = obj.getLong("id"),
                    fullName = obj.getString("fullName"),
                    phone = obj.optString("phone", ""),
                    notes = obj.optString("notes", ""),
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                )
            )
        }

        val sales = mutableListOf<SaleWithItems>()
        val salesArr = root.optJSONArray("sales") ?: JSONArray()
        for (i in 0 until salesArr.length()) {
            val obj = salesArr.getJSONObject(i)
            val sale = SaleEntity(
                id = obj.getLong("id"),
                clientId = if (obj.isNull("clientId")) null else obj.optLong("clientId"),
                clientNameSnapshot = if (obj.isNull("clientNameSnapshot")) null else obj.optString("clientNameSnapshot"),
                subtotalCents = obj.getLong("subtotalCents"),
                discountCents = obj.optLong("discountCents", 0L),
                totalCents = obj.getLong("totalCents"),
                totalCostCents = obj.optLong("totalCostCents", 0L),
                paymentMethod = PaymentMethod.fromString(obj.optString("paymentMethod", "DINHEIRO")),
                createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                notes = obj.optString("notes", "")
            )
            val items = mutableListOf<SaleItemEntity>()
            val iArr = obj.optJSONArray("items") ?: JSONArray()
            for (j in 0 until iArr.length()) {
                val iObj = iArr.getJSONObject(j)
                items.add(
                    SaleItemEntity(
                        id = iObj.getLong("id"),
                        saleId = iObj.getLong("saleId"),
                        productId = iObj.getLong("productId"),
                        variantId = iObj.getLong("variantId"),
                        productNameSnapshot = iObj.getString("productNameSnapshot"),
                        productCodeSnapshot = iObj.optString("productCodeSnapshot", ""),
                        colorSnapshot = iObj.getString("colorSnapshot"),
                        sizeSnapshot = iObj.getString("sizeSnapshot"),
                        quantity = iObj.getInt("quantity"),
                        unitSalePriceCents = iObj.getLong("unitSalePriceCents"),
                        unitCostPriceCents = iObj.optLong("unitCostPriceCents", 0L),
                        lineTotalCents = iObj.getLong("lineTotalCents"),
                        lineCostCents = iObj.optLong("lineCostCents", 0L)
                    )
                )
            }
            sales.add(SaleWithItems(sale, items))
        }

        val orders = mutableListOf<OrderWithItems>()
        val ordersArr = root.optJSONArray("orders") ?: JSONArray()
        for (i in 0 until ordersArr.length()) {
            val obj = ordersArr.getJSONObject(i)
            val order = OrderEntity(
                id = obj.getLong("id"),
                clientId = if (obj.isNull("clientId")) null else obj.optLong("clientId"),
                clientNameSnapshot = obj.getString("clientNameSnapshot"),
                clientPhoneSnapshot = obj.optString("clientPhoneSnapshot", ""),
                totalCents = obj.getLong("totalCents"),
                paidCents = obj.optLong("paidCents", 0L),
                status = OrderStatus.fromString(obj.optString("status", "PENDENTE")),
                orderDateMillis = obj.optLong("orderDateMillis", System.currentTimeMillis()),
                expectedDeliveryMillis = if (obj.isNull("expectedDeliveryMillis")) null else obj.optLong("expectedDeliveryMillis"),
                notes = obj.optString("notes", "")
            )
            val items = mutableListOf<OrderItemEntity>()
            val iArr = obj.optJSONArray("items") ?: JSONArray()
            for (j in 0 until iArr.length()) {
                val iObj = iArr.getJSONObject(j)
                items.add(
                    OrderItemEntity(
                        id = iObj.getLong("id"),
                        orderId = iObj.getLong("orderId"),
                        productId = if (iObj.isNull("productId")) null else iObj.optLong("productId"),
                        variantId = if (iObj.isNull("variantId")) null else iObj.optLong("variantId"),
                        productName = iObj.getString("productName"),
                        color = iObj.getString("color"),
                        size = iObj.getString("size"),
                        quantity = iObj.getInt("quantity"),
                        unitPriceCents = iObj.getLong("unitPriceCents"),
                        unitCostCents = iObj.optLong("unitCostCents", 0L)
                    )
                )
            }
            orders.add(OrderWithItems(order, items))
        }

        val expenses = mutableListOf<ExpenseEntity>()
        val expensesArr = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expensesArr.length()) {
            val obj = expensesArr.getJSONObject(i)
            expenses.add(
                ExpenseEntity(
                    id = obj.getLong("id"),
                    description = obj.getString("description"),
                    category = obj.getString("category"),
                    amountCents = obj.getLong("amountCents"),
                    dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                    notes = obj.optString("notes", "")
                )
            )
        }

        val movements = mutableListOf<StockMovementEntity>()
        val movementsArr = root.optJSONArray("movements") ?: JSONArray()
        for (i in 0 until movementsArr.length()) {
            val obj = movementsArr.getJSONObject(i)
            movements.add(
                StockMovementEntity(
                    id = obj.getLong("id"),
                    productId = obj.getLong("productId"),
                    variantId = obj.getLong("variantId"),
                    productNameSnapshot = obj.getString("productNameSnapshot"),
                    variantLabelSnapshot = obj.getString("variantLabelSnapshot"),
                    type = MovementType.fromString(obj.optString("type", "AJUSTE")),
                    quantityDelta = obj.getInt("quantityDelta"),
                    previousQuantity = obj.getInt("previousQuantity"),
                    newQuantity = obj.getInt("newQuantity"),
                    reason = obj.optString("reason", ""),
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                )
            )
        }

        return BackupPayload(
            version = version,
            exportedAtMillis = exportedAtMillis,
            preferences = preferences,
            products = products,
            clients = clients,
            sales = sales,
            orders = orders,
            expenses = expenses,
            movements = movements
        )
    }
}
