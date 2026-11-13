package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.components.GoldOutlinedButton
import com.example.components.GoldPrimaryButton
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.Audience
import com.example.domain.UiResult
import com.example.domain.VariantInput
import com.example.entities.ProductWithVariants
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.ImageStorageHelper
import kotlinx.coroutines.launch

private data class EditableVariantRow(
    val id: Long = 0L,
    val color: String = "",
    val size: String = "",
    val quantityText: String = "0"
)

@Composable
fun ProductFormScreen(
    existingProduct: ProductWithVariants?,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: (
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
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf(Audience.FEMININO) }
    var costText by remember { mutableStateOf("") }
    var saleText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var initializedId by remember { mutableStateOf<Long?>(null) }

    val variantRows = remember {
        mutableStateListOf(EditableVariantRow(color = "Preta", size = "M", quantityText = "1"))
    }

    LaunchedEffect(existingProduct?.product?.id) {
        if (existingProduct != null && initializedId != existingProduct.product.id) {
            initializedId = existingProduct.product.id
            val p = existingProduct.product
            name = p.name
            code = p.code
            category = p.category
            description = p.description
            audience = p.audience
            costText = CurrencyUtils.centsToInputString(p.costPriceCents)
            saleText = CurrencyUtils.centsToInputString(p.salePriceCents)
            notes = p.notes
            photoUri = p.photoUri
            variantRows.clear()
            if (existingProduct.variants.isEmpty()) {
                variantRows.add(EditableVariantRow())
            } else {
                existingProduct.variants.forEach { v ->
                    variantRows.add(
                        EditableVariantRow(
                            id = v.id,
                            color = v.color,
                            size = v.size,
                            quantityText = v.quantity.toString()
                        )
                    )
                }
            }
        }
    }

    // Zero-permission Android Photo Picker per Google Play Policy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val savedPath = ImageStorageHelper.savePickedPhotoToInternalStorage(context, uri)
                if (savedPath != null) {
                    photoUri = savedPath
                } else {
                    localError = "Não foi possível carregar a imagem selecionada."
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("product_form_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = if (existingProduct == null) "Novo Produto" else "Editar Produto",
                onNavigateBack = onNavigateBack
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
                if (localError != null) {
                    Text(localError!!, color = CoralDanger, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                LuxuryCard {
                    Text("Foto do Produto (Opcional)", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GoldContainer)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Foto do produto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Selecionar foto", tint = GoldPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            GoldOutlinedButton(
                                text = if (photoUri == null) "Escolher foto" else "Trocar foto",
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (photoUri != null) {
                                TextButton(onClick = { photoUri = null }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = CoralDanger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remover foto", color = CoralDanger)
                                }
                            }
                        }
                    }
                }
            }

            item {
                LuxuryCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Dados Principais", style = MaterialTheme.typography.titleSmall, color = WhiteText)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; localError = null },
                            label = { Text("Nome do produto *") },
                            placeholder = { Text("Ex.: Camiseta Básica Algodão") },
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_name_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it; localError = null },
                                label = { Text("Código *") },
                                placeholder = { Text("Ex.: CAM-001") },
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_code_input")
                            )
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it; localError = null },
                                label = { Text("Categoria *") },
                                placeholder = { Text("Ex.: Camisetas") },
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_category_input")
                            )
                        }

                        Text("Público", style = MaterialTheme.typography.labelMedium, color = GraySecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Audience.entries.forEach { aud ->
                                val selected = audience == aud
                                FilterChip(
                                    selected = selected,
                                    onClick = { audience = aud },
                                    label = { Text(aud.label) },
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

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = costText,
                                onValueChange = { costText = it; localError = null },
                                label = { Text("Preço de compra (R$) *") },
                                placeholder = { Text("0,00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_cost_input")
                            )
                            OutlinedTextField(
                                value = saleText,
                                onValueChange = { saleText = it; localError = null },
                                label = { Text("Preço de venda (R$) *") },
                                placeholder = { Text("0,00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = storeTextFieldColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_sale_input")
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descrição") },
                            colors = storeTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Observações") },
                            colors = storeTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Variações (Cor e Tamanho)", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        Text("Controle de quantidade por combinação", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                    }
                    TextButton(
                        onClick = {
                            variantRows.add(EditableVariantRow(color = "", size = "", quantityText = "0"))
                        },
                        modifier = Modifier.testTag("add_variant_row_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar", color = GoldPrimary)
                    }
                }
            }

            itemsIndexed(variantRows) { index, row ->
                LuxuryCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = row.color,
                            onValueChange = { variantRows[index] = row.copy(color = it) },
                            label = { Text("Cor") },
                            placeholder = { Text("Preta") },
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = row.size,
                            onValueChange = { variantRows[index] = row.copy(size = it) },
                            label = { Text("Tam.") },
                            placeholder = { Text("M") },
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier.weight(0.9f)
                        )
                        OutlinedTextField(
                            value = row.quantityText,
                            onValueChange = { variantRows[index] = row.copy(quantityText = it) },
                            label = { Text("Qtd.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier.weight(0.9f)
                        )
                        if (variantRows.size > 1) {
                            IconButton(
                                onClick = { variantRows.removeAt(index) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Remover variação", tint = CoralDanger)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                GoldPrimaryButton(
                    text = if (existingProduct == null) "Cadastrar Produto" else "Salvar Alterações",
                    icon = Icons.Default.Save,
                    onClick = {
                        val costCents = CurrencyUtils.parseToCents(costText)
                        val saleCents = CurrencyUtils.parseToCents(saleText)
                        if (name.isBlank()) {
                            localError = "Informe o nome do produto."
                            return@GoldPrimaryButton
                        }
                        if (code.isBlank()) {
                            localError = "Informe o código de identificação."
                            return@GoldPrimaryButton
                        }
                        if (category.isBlank()) {
                            localError = "Informe a categoria."
                            return@GoldPrimaryButton
                        }
                        if (costCents == null) {
                            localError = "Preço de compra inválido."
                            return@GoldPrimaryButton
                        }
                        if (saleCents == null || saleCents <= 0L) {
                            localError = "Preço de venda deve ser maior que zero."
                            return@GoldPrimaryButton
                        }
                        val parsedVariants = variantRows.mapNotNull { vr ->
                            val qty = vr.quantityText.trim().toIntOrNull()
                            if (vr.color.isBlank() || vr.size.isBlank() || qty == null || qty < 0) {
                                null
                            } else {
                                VariantInput(
                                    id = vr.id,
                                    color = vr.color.trim(),
                                    size = vr.size.trim().uppercase(),
                                    quantity = qty
                                )
                            }
                        }
                        if (parsedVariants.size != variantRows.size || parsedVariants.isEmpty()) {
                            localError = "Preencha cor, tamanho e quantidade válida (>= 0) em todas as variações."
                            return@GoldPrimaryButton
                        }

                        onSave(
                            existingProduct?.product?.id,
                            name,
                            description,
                            category,
                            audience,
                            photoUri,
                            code,
                            costCents,
                            saleCents,
                            notes,
                            parsedVariants
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_product_button")
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
