package com.example.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val SALES = "sales"
    const val CLIENTS = "clients"
    const val MORE = "more"

    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val PRODUCT_FORM = "product_form/{productId}"
    const val INVENTORY_MOVEMENTS = "inventory_movements"
    const val SALE_HISTORY = "sale_history"
    const val CLIENT_DETAIL = "client_detail/{clientId}"

    const val ORDERS = "orders"
    const val FINANCE = "finance"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    fun productDetail(productId: Long) = "product_detail/$productId"
    fun productForm(productId: Long = 0L) = "product_form/$productId"
    fun clientDetail(clientId: Long) = "client_detail/$clientId"

    val topLevelRoutes = setOf(DASHBOARD, INVENTORY, SALES, CLIENTS, MORE)
}
