package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.components.EmptyStateView
import com.example.components.GoldOutlinedButton
import com.example.components.GoldPrimaryButton
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.SearchField
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.PaymentMethod
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.entities.ProductEntity
import com.example.entities.ProductVariantEntity
import com.example.entities.ProductWithVariants
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.viewmodel.SalesUiState

@Composable
fun SalesScreen(
    state: SalesUiState,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onAddItem: (ProductEntity, ProductVariantEntity, Int) -> Unit,
    onUpdateItemQty: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onSelectClient: (ClientEntity?) -> Unit,
    onSelectPayment: (PaymentMethod) -> Unit,
    onDiscountChange: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompleteSale: () -> Unit,
    onOpenSaleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProductPicker by remember { mutableStateOf(false) }
    var showClientPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var discountInput by remember { mutableStateOf("") }

    LaunchedEffect(state.discountCents) {
        if (state.discountCents == 0L && discountInput.isNotEmpty()) {
            discountInput = ""
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Vendas (PDV)",
                subtitle = if (state.cartItems.isEmpty()) "Pronto para registrar" else "${state.cartItems.sumOf { it.quantity }} peça(s) no carrinho",
                actions = {
                    IconButton(
                        onClick = onOpenSaleHistory,
                        modifier = Modifier.testTag("sales_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Histórico de vendas", tint = GoldPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ResultBanner(result = actionResult, onDismiss = onDismissResult)
            }

            item {
                GoldOutlinedButton(
                    text = "Adicionar Produto ao Carrinho",
                    icon = Icons.Default.AddShoppingCart,
                    onClick = { showProductPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_to_sale_button")
                )
            }

            if (state.cartItems.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AddShoppingCart,
                        title = "Carrinho vazio",
                        description = "Toque no botão acima para escolher peças, cores e tamanhos do estoque.",
                        actionLabel = "Ver histórico de vendas",
                        onAction = onOpenSaleHistory
                    )
                }
            } else {
                items(state.cartItems, key = { it.variantId }) { line ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = line.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WhiteText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${line.color} / Tam. ${line.size} • Unit: ${CurrencyUtils.formatCents(line.unitSalePriceCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GraySecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Subtotal: ${CurrencyUtils.formatCents(line.lineTotalCents)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldLight
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateItemQty(line.variantId, line.quantity - 1) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", tint = WhiteText)
                                }
                                Text(
                                    text = line.quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WhiteText,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onUpdateItemQty(line.variantId, line.quantity + 1) },
                                    enabled = line.quantity < line.availableStock,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Aumentar",
                                        tint = if (line.quantity < line.availableStock) GoldPrimary else GraySecondary
                                    )
                                }
                                IconButton(
                                    onClick = { onRemoveItem(line.variantId) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remover item", tint = CoralDanger)
                                }
                            }
                        }
                    }
                }

                item {
                    LuxuryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Cliente (Opcional)", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PersonOutline, contentDescription = null, tint = GoldPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.selectedClient?.fullName ?: "Consumidor avulso (sem cadastro)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (state.selectedClient != null) WhiteText else GraySecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row {
                                    if (state.selectedClient != null) {
                                        IconButton(onClick = { onSelectClient(null) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remover cliente", tint = GraySecondary)
                                        }
                                    }
                                    TextButton(onClick = { showClientPicker = true }) {
                                        Text(if (state.selectedClient == null) "Selecionar" else "Trocar", color = GoldPrimary)
                                    }
                                }
                            }

                            HorizontalDivider(color = SubtleBorder)

                            Text("Forma de Pagamento", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentMethod.entries.forEach { method ->
                                    val selected = state.selectedPaymentMethod == method
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectPayment(method) },
                                        label = { Text(method.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldPrimary,
                                            selectedLabelColor = OnGold,
                                            containerColor = CharcoalSurface,
                                            labelColor = GraySecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = SubtleBorder,
                                            selectedBorderColor = GoldPrimary
                                        )
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = { raw ->
                                    discountInput = raw
                                    val cents = CurrencyUtils.parseToCents(raw) ?: 0L
                                    onDiscountChange(cents)
                                },
                                label = { Text("Desconto (R$)") },
                                placeholder = { Text("0,00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sale_discount_input")
                            )

                            OutlinedTextField(
                                value = state.saleNotes,
                                onValueChange = onNotesChange,
                                label = { Text("Observações da venda (opcional)") },
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    LuxuryCard(highlightGold = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = GraySecondary, style = MaterialTheme.typography.bodyMedium)
                            Text(CurrencyUtils.formatCents(state.subtotalCents), color = WhiteText, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Desconto", color = GraySecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("-${CurrencyUtils.formatCents(state.discountCents)}", color = CoralDanger, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = SubtleBorder)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Final", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                            Text(
                                text = CurrencyUtils.formatCents(state.totalCents),
                                style = MaterialTheme.typography.headlineSmall,
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        GoldPrimaryButton(
                            text = if (state.isSubmitting) "Registrando..." else "Finalizar Venda",
                            icon = Icons.Default.CheckCircle,
                            enabled = !state.isSubmitting && state.cartItems.isNotEmpty(),
                            onClick = {
                                if (state.confirmBeforeSale) {
                                    showConfirmDialog = true
                                } else {
                                    onCompleteSale()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complete_sale_button")
                        )
                    }
                }
            }
        }
    }

    if (showProductPicker) {
        ProductVariantPickerDialog(
            products = state.products,
            onSelect = { product, variant, qty ->
                onAddItem(product, variant, qty)
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }

    if (showClientPicker) {
        ClientPickerDialog(
            clients = state.clients,
            onSelect = {
                onSelectClient(it)
                showClientPicker = false
            },
            onDismiss = { showClientPicker = false }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = ElevatedSurface,
            titleContentColor = WhiteText,
            title = { Text("Confirmar Venda") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Total: ${CurrencyUtils.formatCents(state.totalCents)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight
                    )
                    Text(
                        "Pagamento: ${state.selectedPaymentMethod.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GraySecondary
                    )
                    Text(
                        "Cliente: ${state.selectedClient?.fullName ?: "Consumidor avulso"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GraySecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onCompleteSale()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = OnGold),
                    modifier = Modifier.testTag("confirm_sale_dialog_button")
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = GraySecondary)
                }
            }
        )
    }
}

@Composable
private fun ProductVariantPickerDialog(
    products: List<ProductWithVariants>,
    onSelect: (ProductEntity, ProductVariantEntity, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductWithVariants?>(null) }
    var selectedVariant by remember { mutableStateOf<ProductVariantEntity?>(null) }
    var qty by remember { mutableIntStateOf(1) }

    val filtered = remember(products, query) {
        val q = query.trim().lowercase()
        products.filter { pwv ->
            pwv.totalStock > 0 && (q.isEmpty() ||
                pwv.product.name.lowercase().contains(q) ||
                pwv.product.code.lowercase().contains(q))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        titleContentColor = WhiteText,
        title = {
            Text(
                text = selectedProduct?.product?.name ?: "Selecionar Produto",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (selectedProduct == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Buscar nome ou código..."
                    )
                    if (filtered.isEmpty()) {
                        Text(
                            "Nenhum produto com estoque disponível.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GraySecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filtered, key = { it.product.id }) { pwv ->
                                Surface(
                                    color = CharcoalSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SubtleBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProduct = pwv
                                            selectedVariant = pwv.variants.firstOrNull { it.quantity > 0 }
                                            qty = 1
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pwv.product.name, color = WhiteText, style = MaterialTheme.typography.titleSmall)
                                            Text("${pwv.product.code} • ${pwv.totalStock} em estoque", color = GraySecondary, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            CurrencyUtils.formatCents(pwv.product.salePriceCents),
                                            color = GoldLight,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val product = selectedProduct!!
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Escolha a variação (cor / tamanho):", color = GraySecondary, style = MaterialTheme.typography.bodySmall)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        product.variants.filter { it.quantity > 0 }.forEach { v ->
                            val isSelected = selectedVariant?.id == v.id
                            Surface(
                                color = if (isSelected) GoldContainer else CharcoalSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) GoldPrimary else SubtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedVariant = v
                                        qty = qty.coerceIn(1, v.quantity)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${v.color} • Tam. ${v.size}", color = WhiteText)
                                    Text("${v.quantity} un.", color = GoldLight)
                                }
                            }
                        }
                    }
                    selectedVariant?.let { v ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantidade:", color = WhiteText, style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (qty > 1) qty-- }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", tint = WhiteText)
                                }
                                Text(qty.toString(), color = GoldLight, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { if (qty < v.quantity) qty++ }) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = GoldPrimary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProduct != null && selectedVariant != null) {
                Button(
                    onClick = { onSelect(selectedProduct!!.product, selectedVariant!!, qty) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold)
                ) {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            if (selectedProduct != null) {
                TextButton(onClick = { selectedProduct = null; selectedVariant = null }) {
                    Text("Voltar", color = GraySecondary)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Fechar", color = GraySecondary)
                }
            }
        }
    )
}

@Composable
private fun ClientPickerDialog(
    clients: List<ClientEntity>,
    onSelect: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(clients, query) {
        val q = query.trim().lowercase()
        clients.filter { q.isEmpty() || it.fullName.lowercase().contains(q) || it.phone.lowercase().contains(q) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        titleContentColor = WhiteText,
        title = { Text("Selecionar Cliente") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchField(query = query, onQueryChange = { query = it }, placeholder = "Buscar cliente...")
                if (filtered.isEmpty()) {
                    Text("Nenhum cliente cadastrado.", color = GraySecondary, style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filtered, key = { it.id }) { client ->
                            Surface(
                                color = CharcoalSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(client) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(client.fullName, color = WhiteText, style = MaterialTheme.typography.titleSmall)
                                    if (client.phone.isNotBlank()) {
                                        Text(client.phone, color = GraySecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = GraySecondary)
            }
        }
    )
}
