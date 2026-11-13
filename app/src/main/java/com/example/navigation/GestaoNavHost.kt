package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.MovementsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProductFormScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SaleHistoryScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.WhiteText
import com.example.viewmodel.ClientsViewModel
import com.example.viewmodel.DashboardViewModel
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.InventoryViewModel
import com.example.viewmodel.OrdersViewModel
import com.example.viewmodel.ReportsViewModel
import com.example.viewmodel.SalesViewModel
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.StoreViewModelFactory

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.INVENTORY, "Estoque", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    BottomNavItem(Routes.SALES, "Vendas", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    BottomNavItem(Routes.CLIENTS, "Clientes", Icons.Filled.People, Icons.Outlined.People),
    BottomNavItem(Routes.MORE, "Mais", Icons.Filled.Apps, Icons.Outlined.Apps)
)

@Composable
fun GestaoAppRoot(
    viewModelFactory: StoreViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = currentRoute in Routes.topLevelRoutes

    val navigateToTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isTopLevel) {
                    NavigationRail(
                        containerColor = CharcoalSurface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = GoldPrimary,
                                    selectedTextColor = GoldPrimary,
                                    indicatorColor = GoldContainer,
                                    unselectedIconColor = GraySecondary,
                                    unselectedTextColor = GraySecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${item.route}")
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    GestaoNavHost(
                        navController = navController,
                        viewModelFactory = viewModelFactory,
                        navigateToTopLevel = navigateToTopLevel,
                        modifier = Modifier.widthIn(max = 880.dp)
                    )
                }
            }
        } else {
            Scaffold(
                containerColor = DeepBlack,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (isTopLevel) {
                        NavigationBar(
                            containerColor = CharcoalSurface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = backStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTopLevel(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = GoldPrimary,
                                        selectedTextColor = GoldPrimary,
                                        indicatorColor = GoldContainer,
                                        unselectedIconColor = GraySecondary,
                                        unselectedTextColor = GraySecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${item.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                GestaoNavHost(
                    navController = navController,
                    viewModelFactory = viewModelFactory,
                    navigateToTopLevel = navigateToTopLevel,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}

@Composable
private fun GestaoNavHost(
    navController: NavHostController,
    viewModelFactory: StoreViewModelFactory,
    navigateToTopLevel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dashboardVm: DashboardViewModel = viewModel(factory = viewModelFactory)
    val inventoryVm: InventoryViewModel = viewModel(factory = viewModelFactory)
    val salesVm: SalesViewModel = viewModel(factory = viewModelFactory)
    val clientsVm: ClientsViewModel = viewModel(factory = viewModelFactory)
    val ordersVm: OrdersViewModel = viewModel(factory = viewModelFactory)
    val financeVm: FinanceViewModel = viewModel(factory = viewModelFactory)
    val reportsVm: ReportsViewModel = viewModel(factory = viewModelFactory)
    val settingsVm: SettingsViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Routes.DASHBOARD) {
            val state by dashboardVm.uiState.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                onOpenNewSale = { navigateToTopLevel(Routes.SALES) },
                onOpenInventory = { navigateToTopLevel(Routes.INVENTORY) },
                onOpenOrders = { navController.navigate(Routes.ORDERS) },
                onOpenProductDetail = { id -> navController.navigate(Routes.productDetail(id)) },
                onOpenSaleHistory = { navController.navigate(Routes.SALE_HISTORY) },
                modifier = Modifier.statusBarsPadding()
            )
        }

        composable(Routes.INVENTORY) {
            val state by inventoryVm.uiState.collectAsStateWithLifecycle()
            val result by inventoryVm.actionResult.collectAsStateWithLifecycle()
            InventoryScreen(
                state = state,
                actionResult = result,
                onDismissResult = inventoryVm::clearResult,
                onSearchChange = inventoryVm::updateSearch,
                onCategoryChange = inventoryVm::updateCategory,
                onAudienceChange = inventoryVm::updateAudience,
                onColorChange = inventoryVm::updateColor,
                onSizeChange = inventoryVm::updateSize,
                onToggleLowStock = inventoryVm::toggleLowStockOnly,
                onClearFilters = inventoryVm::clearFilters,
                onAddProduct = { navController.navigate(Routes.productForm(0L)) },
                onOpenProductDetail = { id -> navController.navigate(Routes.productDetail(id)) },
                onOpenMovements = { navController.navigate(Routes.INVENTORY_MOVEMENTS) }
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: 0L
            val invState by inventoryVm.uiState.collectAsStateWithLifecycle()
            val product by inventoryVm.observeProduct(productId).collectAsStateWithLifecycle(initialValue = null)
            val movements by inventoryVm.observeProductMovements(productId).collectAsStateWithLifecycle(initialValue = emptyList())
            val result by inventoryVm.actionResult.collectAsStateWithLifecycle()

            ProductDetailScreen(
                productWithVariants = product,
                movements = movements,
                lowStockThreshold = invState.lowStockThreshold,
                actionResult = result,
                onDismissResult = inventoryVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onEditProduct = { id -> navController.navigate(Routes.productForm(id)) },
                onAdjustVariantStock = { variantId, newQty, reason ->
                    product?.let { pwv ->
                        inventoryVm.adjustStock(pwv.product.id, pwv.product.name, variantId, newQty, reason)
                    }
                },
                onDeleteProduct = { id ->
                    inventoryVm.deleteProduct(id) { navController.popBackStack() }
                }
            )
        }

        composable(
            route = Routes.PRODUCT_FORM,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: 0L
            val existing by inventoryVm.observeProduct(productId).collectAsStateWithLifecycle(initialValue = null)
            val result by inventoryVm.actionResult.collectAsStateWithLifecycle()

            ProductFormScreen(
                existingProduct = if (productId > 0L) existing else null,
                actionResult = result,
                onDismissResult = inventoryVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onSave = { id, name, desc, cat, aud, photo, code, cost, sale, notes, variants ->
                    inventoryVm.saveProduct(
                        productId = id,
                        name = name,
                        description = desc,
                        category = cat,
                        audience = aud,
                        photoUri = photo,
                        code = code,
                        costPriceCents = cost,
                        salePriceCents = sale,
                        notes = notes,
                        variants = variants,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }

        composable(Routes.INVENTORY_MOVEMENTS) {
            val invState by inventoryVm.uiState.collectAsStateWithLifecycle()
            MovementsScreen(
                movements = invState.recentMovements,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SALES) {
            val state by salesVm.uiState.collectAsStateWithLifecycle()
            val result by salesVm.actionResult.collectAsStateWithLifecycle()
            SalesScreen(
                state = state,
                actionResult = result,
                onDismissResult = salesVm::clearResult,
                onAddItem = salesVm::addItemToCart,
                onUpdateItemQty = salesVm::updateCartItemQuantity,
                onRemoveItem = salesVm::removeCartItem,
                onSelectClient = salesVm::selectClient,
                onSelectPayment = salesVm::selectPaymentMethod,
                onDiscountChange = salesVm::updateDiscountCents,
                onNotesChange = salesVm::updateNotes,
                onCompleteSale = { salesVm.completeSale() },
                onOpenSaleHistory = { navController.navigate(Routes.SALE_HISTORY) }
            )
        }

        composable(Routes.SALE_HISTORY) {
            val state by salesVm.uiState.collectAsStateWithLifecycle()
            SaleHistoryScreen(
                sales = state.salesHistory,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CLIENTS) {
            val state by clientsVm.uiState.collectAsStateWithLifecycle()
            val result by clientsVm.actionResult.collectAsStateWithLifecycle()
            ClientsScreen(
                state = state,
                actionResult = result,
                onDismissResult = clientsVm::clearResult,
                onSearchChange = clientsVm::updateSearch,
                onSaveClient = { id, fullName, phone, notes ->
                    clientsVm.saveClient(id, fullName, phone, notes)
                },
                onOpenClientDetail = { id -> navController.navigate(Routes.clientDetail(id)) }
            )
        }

        composable(
            route = Routes.CLIENT_DETAIL,
            arguments = listOf(navArgument("clientId") { type = NavType.LongType })
        ) { entry ->
            val clientId = entry.arguments?.getLong("clientId") ?: 0L
            val client by clientsVm.observeClient(clientId).collectAsStateWithLifecycle(initialValue = null)
            val clientSales by clientsVm.observeClientSales(clientId).collectAsStateWithLifecycle(initialValue = emptyList())
            val result by clientsVm.actionResult.collectAsStateWithLifecycle()

            ClientDetailScreen(
                client = client,
                clientSales = clientSales,
                actionResult = result,
                onDismissResult = clientsVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onUpdateClient = { name, phone, notes ->
                    clientsVm.saveClient(clientId, name, phone, notes)
                },
                onDeleteClient = {
                    clientsVm.deleteClient(clientId) { navController.popBackStack() }
                }
            )
        }

        composable(Routes.MORE) {
            MoreScreen(
                onOpenOrders = { navController.navigate(Routes.ORDERS) },
                onOpenFinance = { navController.navigate(Routes.FINANCE) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenMovements = { navController.navigate(Routes.INVENTORY_MOVEMENTS) },
                modifier = Modifier.statusBarsPadding()
            )
        }

        composable(Routes.ORDERS) {
            val state by ordersVm.uiState.collectAsStateWithLifecycle()
            val result by ordersVm.actionResult.collectAsStateWithLifecycle()
            OrdersScreen(
                state = state,
                actionResult = result,
                onDismissResult = ordersVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onSearchChange = ordersVm::updateSearch,
                onStatusFilterChange = ordersVm::setStatusFilter,
                onSaveOrder = { orderId, clientId, name, phone, items, paid, status, delivery, notes ->
                    ordersVm.saveOrder(orderId, clientId, name, phone, items, paid, status, delivery, notes)
                },
                onUpdateStatus = ordersVm::updateStatus,
                onRegisterPayment = ordersVm::registerPayment,
                onDeleteOrder = ordersVm::deleteOrder
            )
        }

        composable(Routes.FINANCE) {
            val state by financeVm.uiState.collectAsStateWithLifecycle()
            val result by financeVm.actionResult.collectAsStateWithLifecycle()
            FinanceScreen(
                state = state,
                actionResult = result,
                onDismissResult = financeVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onSelectPeriod = financeVm::setPeriod,
                onSelectCustomRange = financeVm::setCustomRange,
                onSaveExpense = { id, desc, cat, amount, date, notes ->
                    financeVm.saveExpense(id, desc, cat, amount, date, notes)
                },
                onDeleteExpense = financeVm::deleteExpense
            )
        }

        composable(Routes.REPORTS) {
            val state by reportsVm.uiState.collectAsStateWithLifecycle()
            ReportsScreen(
                state = state,
                onNavigateBack = { navController.popBackStack() },
                onSelectPeriod = reportsVm::setPeriod,
                onSelectCustomRange = reportsVm::setCustomRange,
                onGenerateCsv = reportsVm::generateCsvContent
            )
        }

        composable(Routes.SETTINGS) {
            val prefs by settingsVm.preferences.collectAsStateWithLifecycle()
            val result by settingsVm.actionResult.collectAsStateWithLifecycle()
            SettingsScreen(
                preferences = prefs,
                actionResult = result,
                onDismissResult = settingsVm::clearResult,
                onNavigateBack = { navController.popBackStack() },
                onSaveIdentity = settingsVm::saveStoreIdentity,
                onSavePreferences = settingsVm::savePreferences,
                onBuildBackupJson = settingsVm::buildBackupJson,
                onRestoreBackupJson = settingsVm::restoreFromBackupJson,
                onNotifyResult = settingsVm::notifyMessage
            )
        }
    }
}
