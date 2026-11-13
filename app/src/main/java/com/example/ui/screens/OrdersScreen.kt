package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.components.ConfirmDeleteDialog
import com.example.components.EmptyStateView
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.SearchField
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.OrderItemDraft
import com.example.domain.OrderStatus
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.OrderWithItems
import com.example.entities.ProductWithVariants
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoContainer
import com.example.ui.theme.OnGold
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import com.example.viewmodel.OrdersUiState

@Composable
fun OrdersScreen(
    state: OrdersUiState,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (OrderStatus?) -> Unit,
    onSaveOrder: (
        orderId: Long?,
        clientId: Long?,
        clientName: String,
        clientPhone: String,
        items: List<OrderItemDraft>,
        paidCents: Long,
        status: OrderStatus,
        expectedDeliveryMillis: Long?,
        notes: String
    ) -> Unit,
    onUpdateStatus: (Long, OrderStatus) -> Unit,
    onRegisterPayment: (Long, Long) -> Unit,
    onDeleteOrder: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFormForOrder by remember { mutableStateOf<OrderWithItems?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<OrderWithItems?>(null) }
    var orderForPayment by remember { mutableStateOf<OrderWithItems?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Pedidos & Encomendas",
                subtitle = "${state.orders.size} pedido(s) • Separado do faturamento de vendas",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNew = true; showFormForOrder = null },
                containerColor = GoldPrimary,
                contentColor = OnGold,
                modifier = Modifier.testTag("add_order_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo pedido")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ResultBanner(result = actionResult, onDismiss = onDismissResult, modifier = Modifier.padding(bottom = 8.dp))

            SearchField(
                query = state.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Buscar por cliente ou peça...",
                testTag = "orders_search_input"
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allSelected = state.selectedStatusFilter == null
                FilterChip(
                    selected = allSelected,
                    onClick = { onStatusFilterChange(null) },
                    label = { Text("Todos") },
                    colors = orderChipColors(),
                    border = orderChipBorder(allSelected)
                )
                OrderStatus.entries.forEach { st ->
                    val selected = state.selectedStatusFilter == st
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(if (selected) null else st) },
                        label = { Text(st.label) },
                        colors = orderChipColors(),
                        border = orderChipBorder(selected)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (state.orders.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.LocalShipping,
                    title = "Nenhum pedido encontrado",
                    description = "Registre encomendas e reservas com valor pago, saldo restante e data prevista de entrega.",
                    actionLabel = "Novo pedido",
                    onAction = { isCreatingNew = true; showFormForOrder = null }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.orders, key = { it.order.id }) { owi ->
                        OrderCard(
                            orderWithItems = owi,
                            onEdit = { showFormForOrder = owi; isCreatingNew = false },
                            onStatusChange = { status -> onUpdateStatus(owi.order.id, status) },
                            onRegisterPayment = { orderForPayment = owi },
                            onDelete = { orderToDelete = owi }
                        )
                    }
                }
            }
        }
    }

    if (isCreatingNew || showFormForOrder != null) {
        OrderFormDialog(
            existing = showFormForOrder,
            clients = state.clients,
            products = state.products,
            onSave = { clientId, name, phone, items, paidCents, status, deliveryMillis, notes ->
                onSaveOrder(showFormForOrder?.order?.id, clientId, name, phone, items, paidCents, status, deliveryMillis, notes)
                isCreatingNew = false
                showFormForOrder = null
            },
            onDismiss = { isCreatingNew = false; showFormForOrder = null }
        )
    }

    orderToDelete?.let { owi ->
        ConfirmDeleteDialog(
            title = "Excluir Pedido",
            message = "Deseja excluir o pedido #${owi.order.id} de '${owi.order.clientNameSnapshot}'?",
            onConfirm = {
                onDeleteOrder(owi.order.id)
                orderToDelete = null
            },
            onDismiss = { orderToDelete = null }
        )
    }

    orderForPayment?.let { owi ->
        var amountInput by remember(owi.order.id) {
            mutableStateOf(CurrencyUtils.centsToInputString(owi.order.remainingCents))
        }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { orderForPayment = null },
            containerColor = ElevatedSurface,
            titleContentColor = WhiteText,
            title = { Text("Registrar Pagamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Saldo restante: ${CurrencyUtils.formatCents(owi.order.remainingCents)}",
                        color = GoldLight,
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it; err = null },
                        label = { Text("Valor recebido (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (err != null) Text(err!!, color = CoralDanger, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cents = CurrencyUtils.parseToCents(amountInput)
                        if (cents == null || cents <= 0L) {
                            err = "Informe um valor maior que zero."
                        } else {
                            onRegisterPayment(owi.order.id, cents)
                            orderForPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderForPayment = null }) {
                    Text("Cancelar", color = GraySecondary)
                }
            }
        )
    }
}

@Composable
private fun OrderCard(
    orderWithItems: OrderWithItems,
    onEdit: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onRegisterPayment: () -> Unit,
    onDelete: () -> Unit
) {
    val o = orderWithItems.order
    var showStatusMenu by remember { mutableStateOf(false) }

    val (fg, bg) = when (o.status) {
        OrderStatus.PENDENTE -> AmberWarning to AmberContainer
        OrderStatus.EM_ANDAMENTO -> InfoBlue to InfoContainer
        OrderStatus.CONCLUIDO -> EmeraldSuccess to EmeraldContainer
        OrderStatus.CANCELADO -> CoralDanger to CoralContainer
    }

    LuxuryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido #${o.id} • ${o.clientNameSnapshot}", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                Text(
                    text = "Pedido: ${DateUtils.formatDate(o.orderDateMillis)}" +
                        (o.expectedDeliveryMillis?.let { " • Entrega: ${DateUtils.formatDate(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = GraySecondary
                )
            }
            Box {
                Surface(
                    onClick = { showStatusMenu = true },
                    color = bg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, fg.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = o.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = fg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false },
                    containerColor = ElevatedSurface
                ) {
                    OrderStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.label, color = WhiteText) },
                            onClick = { onStatusChange(status); showStatusMenu = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = SubtleBorder)
        Spacer(modifier = Modifier.height(8.dp))

        orderWithItems.items.forEach { item ->
            Text(
                text = "${item.quantity}x ${item.productName} (${item.color}/${item.size}) — ${CurrencyUtils.formatCents(item.unitPriceCents * item.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = GraySecondary
            )
        }

        if (o.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Obs: ${o.notes}", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                Text(CurrencyUtils.formatCents(o.totalCents), style = MaterialTheme.typography.titleSmall, color = WhiteText)
            }
            Column {
                Text("Pago", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                Text(CurrencyUtils.formatCents(o.paidCents), style = MaterialTheme.typography.titleSmall, color = EmeraldSuccess)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Saldo restante", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                Text(
                    CurrencyUtils.formatCents(o.remainingCents),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (o.remainingCents > 0) GoldLight else EmeraldSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (o.remainingCents > 0 && o.status != OrderStatus.CANCELADO) {
                OutlinedButton(
                    onClick = onRegisterPayment,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Receber", color = GoldPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar pedido", tint = GoldPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir pedido", tint = CoralDanger)
            }
        }
    }
}

private data class EditableOrderLine(
    val productId: Long? = null,
    val variantId: Long? = null,
    val productName: String = "",
    val color: String = "",
    val size: String = "",
    val qtyText: String = "1",
    val priceText: String = ""
)

@Composable
private fun OrderFormDialog(
    existing: OrderWithItems?,
    clients: List<ClientEntity>,
    products: List<ProductWithVariants>,
    onSave: (
        clientId: Long?,
        clientName: String,
        clientPhone: String,
        items: List<OrderItemDraft>,
        paidCents: Long,
        status: OrderStatus,
        expectedDeliveryMillis: Long?,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedClientId by remember(existing?.order?.id) { mutableStateOf(existing?.order?.clientId) }
    var clientName by remember(existing?.order?.id) { mutableStateOf(existing?.order?.clientNameSnapshot ?: "") }
    var clientPhone by remember(existing?.order?.id) { mutableStateOf(existing?.order?.clientPhoneSnapshot ?: "") }
    var paidText by remember(existing?.order?.id) {
        mutableStateOf(existing?.order?.paidCents?.let { CurrencyUtils.centsToInputString(it) } ?: "")
    }
    var deliveryText by remember(existing?.order?.id) {
        mutableStateOf(existing?.order?.expectedDeliveryMillis?.let { DateUtils.formatDate(it) } ?: "")
    }
    var status by remember(existing?.order?.id) { mutableStateOf(existing?.order?.status ?: OrderStatus.PENDENTE) }
    var notes by remember(existing?.order?.id) { mutableStateOf(existing?.order?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val lines = remember(existing?.order?.id) {
        mutableStateListOf<EditableOrderLine>().apply {
            if (existing != null && existing.items.isNotEmpty()) {
                existing.items.forEach {
                    add(
                        EditableOrderLine(
                            productId = it.productId,
                            variantId = it.variantId,
                            productName = it.productName,
                            color = it.color,
                            size = it.size,
                            qtyText = it.quantity.toString(),
                            priceText = CurrencyUtils.centsToInputString(it.unitPriceCents)
                        )
                    )
                }
            } else {
                add(EditableOrderLine())
            }
        }
    }

    var showClientDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        titleContentColor = WhiteText,
        title = { Text(if (existing == null) "Novo Pedido" else "Editar Pedido #${existing.order.id}") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    if (clients.isNotEmpty()) {
                        Box {
                            OutlinedButton(
                                onClick = { showClientDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, SubtleBorder)
                            ) {
                                Text("Selecionar cliente cadastrado", color = GoldPrimary)
                            }
                            DropdownMenu(
                                expanded = showClientDropdown,
                                onDismissRequest = { showClientDropdown = false },
                                containerColor = CharcoalSurface
                            ) {
                                clients.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.fullName, color = WhiteText) },
                                        onClick = {
                                            selectedClientId = c.id
                                            clientName = c.fullName
                                            clientPhone = c.phone
                                            showClientDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it; selectedClientId = null; error = null },
                        label = { Text("Nome do cliente *") },
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Telefone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Itens do Pedido", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                        TextButton(onClick = { lines.add(EditableOrderLine()) }) {
                            Text("+ Item", color = GoldPrimary)
                        }
                    }
                }

                items(lines.size) { idx ->
                    val line = lines[idx]
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = line.productName,
                                onValueChange = { lines[idx] = line.copy(productName = it) },
                                label = { Text("Produto *") },
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = line.color,
                                    onValueChange = { lines[idx] = line.copy(color = it) },
                                    label = { Text("Cor *") },
                                    singleLine = true,
                                    colors = storeTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = line.size,
                                    onValueChange = { lines[idx] = line.copy(size = it) },
                                    label = { Text("Tam *") },
                                    singleLine = true,
                                    colors = storeTextFieldColors(),
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = line.qtyText,
                                    onValueChange = { lines[idx] = line.copy(qtyText = it) },
                                    label = { Text("Qtd *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = storeTextFieldColors(),
                                    modifier = Modifier.weight(0.8f)
                                )
                                OutlinedTextField(
                                    value = line.priceText,
                                    onValueChange = { lines[idx] = line.copy(priceText = it) },
                                    label = { Text("Unit. (R$) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = storeTextFieldColors(),
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                            if (products.isNotEmpty()) {
                                var showCatalogMenu by remember { mutableStateOf(false) }
                                Box {
                                    TextButton(onClick = { showCatalogMenu = true }) {
                                        Text("Preencher do catálogo", color = GoldPrimary)
                                    }
                                    DropdownMenu(
                                        expanded = showCatalogMenu,
                                        onDismissRequest = { showCatalogMenu = false },
                                        containerColor = ElevatedSurface
                                    ) {
                                        products.forEach { pwv ->
                                            val firstVar = pwv.variants.firstOrNull()
                                            DropdownMenuItem(
                                                text = { Text("${pwv.product.name} (${CurrencyUtils.formatCents(pwv.product.salePriceCents)})", color = WhiteText) },
                                                onClick = {
                                                    lines[idx] = line.copy(
                                                        productId = pwv.product.id,
                                                        variantId = firstVar?.id,
                                                        productName = pwv.product.name,
                                                        color = firstVar?.color ?: "Única",
                                                        size = firstVar?.size ?: "U",
                                                        priceText = CurrencyUtils.centsToInputString(pwv.product.salePriceCents)
                                                    )
                                                    showCatalogMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Valor já pago / sinal (R$)") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = deliveryText,
                        onValueChange = { deliveryText = it },
                        label = { Text("Data prevista entrega (DD/MM/AAAA)") },
                        placeholder = { Text("DD/MM/AAAA") },
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OrderStatus.entries.forEach { st ->
                            val selected = status == st
                            FilterChip(
                                selected = selected,
                                onClick = { status = st },
                                label = { Text(st.label) },
                                colors = orderChipColors(),
                                border = orderChipBorder(selected)
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações") },
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (error != null) {
                    item { Text(error!!, color = CoralDanger, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientName.isBlank()) {
                        error = "Informe o nome do cliente."
                        return@Button
                    }
                    val parsedItems = lines.mapNotNull { l ->
                        val qty = l.qtyText.trim().toIntOrNull()
                        val price = CurrencyUtils.parseToCents(l.priceText)
                        if (l.productName.isBlank() || l.color.isBlank() || l.size.isBlank() || qty == null || qty <= 0 || price == null || price <= 0L) {
                            null
                        } else {
                            OrderItemDraft(
                                productId = l.productId,
                                variantId = l.variantId,
                                productName = l.productName,
                                color = l.color,
                                size = l.size,
                                quantity = qty,
                                unitPriceCents = price
                            )
                        }
                    }
                    if (parsedItems.size != lines.size || parsedItems.isEmpty()) {
                        error = "Preencha todos os itens com quantidade e preço válidos."
                        return@Button
                    }
                    val paidCents = if (paidText.isBlank()) 0L else CurrencyUtils.parseToCents(paidText)
                    if (paidCents == null) {
                        error = "Valor pago inválido."
                        return@Button
                    }
                    val totalCents = parsedItems.sumOf { it.lineTotalCents }
                    if (paidCents > totalCents) {
                        error = "O valor pago não pode ser maior que o total (${CurrencyUtils.formatCents(totalCents)})."
                        return@Button
                    }
                    val deliveryMillis = if (deliveryText.isBlank()) null else {
                        DateUtils.parseDate(deliveryText) ?: run {
                            error = "Data de entrega inválida. Use DD/MM/AAAA."
                            return@Button
                        }
                    }
                    onSave(selectedClientId, clientName, clientPhone, parsedItems, paidCents, status, deliveryMillis, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = GraySecondary)
            }
        }
    )
}

@Composable
private fun orderChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = GoldPrimary,
    selectedLabelColor = OnGold,
    containerColor = CharcoalSurface,
    labelColor = GraySecondary
)

@Composable
private fun orderChipBorder(selected: Boolean) = FilterChipDefaults.filterChipBorder(
    enabled = true,
    selected = selected,
    borderColor = SubtleBorder,
    selectedBorderColor = GoldPrimary
)
