package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.components.EmptyStateView
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.SearchField
import com.example.components.StatusBadge
import com.example.components.StoreTopBar
import com.example.domain.Audience
import com.example.domain.UiResult
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
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.viewmodel.InventoryUiState

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAudienceChange: (Audience?) -> Unit,
    onColorChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onToggleLowStock: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onAddProduct: () -> Unit,
    onOpenProductDetail: (Long) -> Unit,
    onOpenMovements: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Estoque",
                subtitle = "${state.products.size} modelo(s) • ${state.products.sumOf { it.totalStock }} peça(s)",
                actions = {
                    IconButton(
                        onClick = onOpenMovements,
                        modifier = Modifier.testTag("inventory_movements_button")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Histórico de movimentações",
                            tint = GoldPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = GoldPrimary,
                contentColor = OnGold,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Cadastrar produto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ResultBanner(
                result = actionResult,
                onDismiss = onDismissResult,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SearchField(
                query = state.filter.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Pesquisar por nome ou código...",
                testTag = "inventory_search_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.filter.onlyLowStock,
                    onClick = { onToggleLowStock(!state.filter.onlyLowStock) },
                    label = { Text("Estoque baixo") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = chipColors(),
                    border = chipBorder(state.filter.onlyLowStock)
                )

                Audience.entries.forEach { aud ->
                    val selected = state.filter.selectedAudience == aud
                    FilterChip(
                        selected = selected,
                        onClick = { onAudienceChange(if (selected) null else aud) },
                        label = { Text(aud.label) },
                        colors = chipColors(),
                        border = chipBorder(selected)
                    )
                }

                Box {
                    val catSelected = state.filter.selectedCategory != "Todas"
                    FilterChip(
                        selected = catSelected,
                        onClick = { showCategoryMenu = true },
                        label = { Text("Cat: ${state.filter.selectedCategory}") },
                        colors = chipColors(),
                        border = chipBorder(catSelected)
                    )
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false },
                        containerColor = ElevatedSurface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas", color = WhiteText) },
                            onClick = { onCategoryChange("Todas"); showCategoryMenu = false }
                        )
                        state.allCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = WhiteText) },
                                onClick = { onCategoryChange(cat); showCategoryMenu = false }
                            )
                        }
                    }
                }

                Box {
                    val colorSelected = state.filter.selectedColor != "Todas"
                    FilterChip(
                        selected = colorSelected,
                        onClick = { showColorMenu = true },
                        label = { Text("Cor: ${state.filter.selectedColor}") },
                        colors = chipColors(),
                        border = chipBorder(colorSelected)
                    )
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false },
                        containerColor = ElevatedSurface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas", color = WhiteText) },
                            onClick = { onColorChange("Todas"); showColorMenu = false }
                        )
                        state.allColors.forEach { col ->
                            DropdownMenuItem(
                                text = { Text(col, color = WhiteText) },
                                onClick = { onColorChange(col); showColorMenu = false }
                            )
                        }
                    }
                }

                Box {
                    val sizeSelected = state.filter.selectedSize != "Todos"
                    FilterChip(
                        selected = sizeSelected,
                        onClick = { showSizeMenu = true },
                        label = { Text("Tam: ${state.filter.selectedSize}") },
                        colors = chipColors(),
                        border = chipBorder(sizeSelected)
                    )
                    DropdownMenu(
                        expanded = showSizeMenu,
                        onDismissRequest = { showSizeMenu = false },
                        containerColor = ElevatedSurface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos", color = WhiteText) },
                            onClick = { onSizeChange("Todos"); showSizeMenu = false }
                        )
                        state.allSizes.forEach { sz ->
                            DropdownMenuItem(
                                text = { Text(sz, color = WhiteText) },
                                onClick = { onSizeChange(sz); showSizeMenu = false }
                            )
                        }
                    }
                }

                if (state.filter.searchQuery.isNotEmpty() ||
                    state.filter.selectedCategory != "Todas" ||
                    state.filter.selectedAudience != null ||
                    state.filter.selectedColor != "Todas" ||
                    state.filter.selectedSize != "Todos" ||
                    state.filter.onlyLowStock
                ) {
                    TextButton(onClick = onClearFilters) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpar", color = GoldPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    EmptyStateView(
                        icon = Icons.Default.Checkroom,
                        title = "Nenhum produto encontrado",
                        description = "Cadastre produtos com controle de cores e tamanhos ou ajuste os filtros ativos.",
                        actionLabel = "Cadastrar produto",
                        onAction = onAddProduct,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.products, key = { it.product.id }) { pwv ->
                        ProductInventoryCard(
                            item = pwv,
                            lowStockThreshold = state.lowStockThreshold,
                            onClick = { onOpenProductDetail(pwv.product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductInventoryCard(
    item: ProductWithVariants,
    lowStockThreshold: Int,
    onClick: () -> Unit
) {
    val hasOut = item.variants.any { it.quantity == 0 }
    val hasLow = item.variants.any { it.quantity <= lowStockThreshold }

    LuxuryCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${item.product.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.product.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = item.product.photoUri,
                    contentDescription = item.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElevatedSurface)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(GoldContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = WhiteText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = CurrencyUtils.formatCents(item.product.salePriceCents),
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.product.code} • ${item.product.category} • ${item.product.audience.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GraySecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.variants.size} variação(ões) • ${item.totalStock} un.",
                        style = MaterialTheme.typography.labelSmall,
                        color = GraySecondary
                    )
                    when {
                        hasOut -> StatusBadge("Com esgotados", CoralDanger, CoralContainer)
                        hasLow -> StatusBadge("Estoque baixo", AmberWarning, AmberContainer)
                        else -> StatusBadge("Normal", EmeraldSuccess, EmeraldContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = GoldPrimary,
    selectedLabelColor = OnGold,
    selectedLeadingIconColor = OnGold,
    containerColor = CharcoalSurface,
    labelColor = GraySecondary,
    iconColor = GoldPrimary
)

@Composable
private fun chipBorder(selected: Boolean) = FilterChipDefaults.filterChipBorder(
    enabled = true,
    selected = selected,
    borderColor = SubtleBorder,
    selectedBorderColor = GoldPrimary
)
