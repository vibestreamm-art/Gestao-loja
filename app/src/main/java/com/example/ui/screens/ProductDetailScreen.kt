package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.components.ConfirmDeleteDialog
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.UiResult
import com.example.entities.ProductVariantEntity
import com.example.entities.ProductWithVariants
import com.example.entities.StockMovementEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils

@Composable
fun ProductDetailScreen(
    productWithVariants: ProductWithVariants?,
    movements: List<StockMovementEntity>,
    lowStockThreshold: Int,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onAdjustVariantStock: (variantId: Long, newQty: Int, reason: String) -> Unit,
    onDeleteProduct: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var variantToAdjust by remember { mutableStateOf<ProductVariantEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("product_detail_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = productWithVariants?.product?.name ?: "Detalhes do Produto",
                subtitle = productWithVariants?.product?.code,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (productWithVariants != null) {
                        IconButton(
                            onClick = { onEditProduct(productWithVariants.product.id) },
                            modifier = Modifier.testTag("edit_product_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar produto", tint = GoldPrimary)
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_product_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir produto", tint = CoralDanger)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (productWithVariants == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Produto não encontrado.", color = GraySecondary)
            }
            return@Scaffold
        }

        val product = productWithVariants.product
        val marginCents = product.salePriceCents - product.costPriceCents

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
                LuxuryCard(highlightGold = true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!product.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = product.photoUri,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .background(GoldContainer, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Checkroom,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleLarge, color = WhiteText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${product.category} • ${product.audience.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cadastro: ${DateUtils.formatDate(product.createdAtMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GraySecondary
                            )
                        }
                    }

                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(product.description, style = MaterialTheme.typography.bodyMedium, color = GraySecondary)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Compra", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                            Text(CurrencyUtils.formatCents(product.costPriceCents), style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        }
                        Column {
                            Text("Venda", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                            Text(CurrencyUtils.formatCents(product.salePriceCents), style = MaterialTheme.typography.titleMedium, color = GoldLight, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Margem unit.", style = MaterialTheme.typography.labelSmall, color = GraySecondary)
                            Text(
                                CurrencyUtils.formatCents(marginCents),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (marginCents >= 0) EmeraldSuccess else CoralDanger
                            )
                        }
                    }

                    if (product.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Obs: ${product.notes}", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Variações (${productWithVariants.variants.size})", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                    Text("Total: ${productWithVariants.totalStock} un.", style = MaterialTheme.typography.labelLarge, color = GoldPrimary)
                }
            }

            items(productWithVariants.variants, key = { it.id }) { variant ->
                LuxuryCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${variant.color} • Tamanho ${variant.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = WhiteText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            when {
                                variant.quantity == 0 -> StatusBadge("Esgotado", CoralDanger, CoralContainer)
                                variant.quantity <= lowStockThreshold -> StatusBadge("Baixo (${variant.quantity})", AmberWarning, AmberContainer)
                                else -> StatusBadge("Em estoque: ${variant.quantity}", EmeraldSuccess, EmeraldContainer)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (variant.quantity > 0) {
                                        onAdjustVariantStock(variant.id, variant.quantity - 1, "Saída rápida (-1)")
                                    }
                                },
                                enabled = variant.quantity > 0,
                                modifier = Modifier.testTag("variant_dec_${variant.id}")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir estoque", tint = if (variant.quantity > 0) WhiteText else GraySecondary)
                            }
                            TextButton(
                                onClick = { variantToAdjust = variant },
                                modifier = Modifier.testTag("variant_qty_${variant.id}")
                            ) {
                                Text(
                                    text = variant.quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    onAdjustVariantStock(variant.id, variant.quantity + 1, "Entrada rápida (+1)")
                                },
                                modifier = Modifier.testTag("variant_inc_${variant.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar estoque", tint = GoldPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Text("Movimentações do Produto", style = MaterialTheme.typography.titleMedium, color = WhiteText)
            }

            if (movements.isEmpty()) {
                item {
                    LuxuryCard {
                        Text("Nenhuma movimentação registrada para este produto.", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                }
            } else {
                items(movements.take(20), key = { it.id }) { mov ->
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${mov.type.label} • ${mov.variantLabelSnapshot}", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                                Text("${DateUtils.formatDateTime(mov.createdAtMillis)} • ${mov.reason}", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                            val deltaStr = if (mov.quantityDelta > 0) "+${mov.quantityDelta}" else mov.quantityDelta.toString()
                            Text(
                                text = "$deltaStr (${mov.newQuantity})",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (mov.quantityDelta >= 0) EmeraldSuccess else CoralDanger
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && productWithVariants != null) {
        ConfirmDeleteDialog(
            title = "Excluir Produto",
            message = "Deseja excluir '${productWithVariants.product.name}' e todas as suas variações do estoque?",
            onConfirm = {
                showDeleteDialog = false
                onDeleteProduct(productWithVariants.product.id)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    variantToAdjust?.let { variant ->
        var qtyInput by remember(variant.id) { mutableStateOf(variant.quantity.toString()) }
        var reasonInput by remember(variant.id) { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { variantToAdjust = null },
            containerColor = ElevatedSurface,
            titleContentColor = WhiteText,
            title = { Text("Ajustar Estoque (${variant.color}/${variant.size})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it; error = null },
                        label = { Text("Nova quantidade") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Motivo (opcional)") },
                        placeholder = { Text("Ex.: Reposição de fornecedor") },
                        singleLine = true,
                        colors = storeTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(error!!, color = CoralDanger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = qtyInput.trim().toIntOrNull()
                        if (parsed == null || parsed < 0) {
                            error = "Informe uma quantidade válida (0 ou maior)."
                        } else {
                            onAdjustVariantStock(variant.id, parsed, reasonInput)
                            variantToAdjust = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { variantToAdjust = null }) {
                    Text("Cancelar", color = GraySecondary)
                }
            }
        )
    }
}
