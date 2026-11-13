package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.components.GoldOutlinedButton
import com.example.components.GoldPrimaryButton
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.StorePreferences
import com.example.domain.UiResult
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.WhiteText
import com.example.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: StorePreferences,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onNavigateBack: () -> Unit,
    onSaveIdentity: (storeName: String, businessInfo: String) -> Unit,
    onSavePreferences: (minStock: Int, showAlerts: Boolean, confirmSale: Boolean) -> Unit,
    onBuildBackupJson: suspend () -> Result<String>,
    onRestoreBackupJson: (String) -> Unit,
    onNotifyResult: (UiResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var storeName by remember { mutableStateOf(preferences.storeName) }
    var businessInfo by remember { mutableStateOf(preferences.businessInfo) }
    var minStockText by remember { mutableStateOf(preferences.defaultMinStock.toString()) }
    var showLowStockAlerts by remember { mutableStateOf(preferences.showLowStockAlerts) }
    var confirmSale by remember { mutableStateOf(preferences.confirmBeforeCompletingSale) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(preferences) {
        storeName = preferences.storeName
        businessInfo = preferences.businessInfo
        minStockText = preferences.defaultMinStock.toString()
        showLowStockAlerts = preferences.showLowStockAlerts
        confirmSale = preferences.confirmBeforeCompletingSale
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val jsonResult = onBuildBackupJson()
                jsonResult.onSuccess { json ->
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Não foi possível gravar o arquivo de backup.")
                    }.onSuccess {
                        onNotifyResult(UiResult.Success("Backup salvo com sucesso!"))
                    }.onFailure { err ->
                        onNotifyResult(UiResult.Error(err.message ?: "Erro ao salvar backup."))
                    }
                }.onFailure { err ->
                    onNotifyResult(UiResult.Error(err.message ?: "Erro ao gerar backup."))
                }
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("Não foi possível ler o arquivo selecionado.")
            }.onSuccess { content ->
                pendingRestoreJson = content
            }.onFailure { err ->
                onNotifyResult(UiResult.Error(err.message ?: "Erro ao ler arquivo de backup."))
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Configurações",
                subtitle = "Loja, preferências e backup local",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { ResultBanner(result = actionResult, onDismiss = onDismissResult) }

            item {
                LuxuryCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Identidade da Loja", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("Nome da loja") },
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_store_name_input")
                        )
                        OutlinedTextField(
                            value = businessInfo,
                            onValueChange = { businessInfo = it },
                            label = { Text("Informações comerciais (segmento, endereço ou telefone)") },
                            colors = storeTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        GoldPrimaryButton(
                            text = "Salvar Dados da Loja",
                            icon = Icons.Default.Save,
                            onClick = { onSaveIdentity(storeName, businessInfo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_store_identity_button")
                        )
                    }
                }
            }

            item {
                LuxuryCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Estoque e Operação", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        OutlinedTextField(
                            value = minStockText,
                            onValueChange = { minStockText = it },
                            label = { Text("Estoque mínimo padrão (alerta)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = storeTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_min_stock_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Alertas de estoque baixo", style = MaterialTheme.typography.bodyLarge, color = WhiteText)
                                Text("Exibir variações críticas no painel inicial", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                            Switch(
                                checked = showLowStockAlerts,
                                onCheckedChange = { showLowStockAlerts = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGold,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedTrackColor = GoldContainer
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Confirmar antes de concluir venda", style = MaterialTheme.typography.bodyLarge, color = WhiteText)
                                Text("Exibir resumo antes de baixar o estoque", style = MaterialTheme.typography.bodySmall, color = GraySecondary)
                            }
                            Switch(
                                checked = confirmSale,
                                onCheckedChange = { confirmSale = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGold,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedTrackColor = GoldContainer
                                )
                            )
                        }
                        GoldOutlinedButton(
                            text = "Salvar Preferências",
                            onClick = {
                                val parsed = minStockText.trim().toIntOrNull() ?: 0
                                onSavePreferences(parsed, showLowStockAlerts, confirmSale)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_preferences_button")
                        )
                    }
                }
            }

            item {
                LuxuryCard(highlightGold = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Backup & Restauração Local", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                        Text(
                            "Exporte todos os dados (produtos, variações, clientes, vendas, pedidos, despesas e movimentações) para um arquivo JSON no aparelho.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GraySecondary
                        )
                        GoldPrimaryButton(
                            text = "Gerar Backup Local (JSON)",
                            icon = Icons.Default.Backup,
                            onClick = {
                                val stamp = DateUtils.formatDate(System.currentTimeMillis()).replace("/", "-")
                                createBackupLauncher.launch("backup_gestao_store_$stamp.json")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_backup_button")
                        )
                        GoldOutlinedButton(
                            text = "Restaurar de Arquivo Backup",
                            icon = Icons.Default.Restore,
                            onClick = { openBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restore_backup_button")
                        )
                    }
                }
            }

            item {
                LuxuryCard {
                    Text("Sobre o Aplicativo", style = MaterialTheme.typography.titleMedium, color = WhiteText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Gestão Store v1.0", style = MaterialTheme.typography.titleSmall, color = GoldPrimary)
                    Text(
                        "Sistema nativo Android de gestão comercial offline para lojas de moda masculina e feminina. Armazenamento local seguro via Room Database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GraySecondary
                    )
                }
            }
        }
    }

    pendingRestoreJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingRestoreJson = null },
            containerColor = ElevatedSurface,
            titleContentColor = WhiteText,
            title = { Text("Confirmar Restauração") },
            text = {
                Text(
                    "Restaurar este backup substituirá todos os dados atuais cadastrados no aplicativo. Deseja continuar?",
                    color = GraySecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreBackupJson(json)
                        pendingRestoreJson = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDanger, contentColor = WhiteText)
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreJson = null }) {
                    Text("Cancelar", color = GraySecondary)
                }
            }
        )
    }
}
