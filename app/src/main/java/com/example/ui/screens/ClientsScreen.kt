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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.components.EmptyStateView
import com.example.components.LuxuryCard
import com.example.components.ResultBanner
import com.example.components.SearchField
import com.example.components.StoreTopBar
import com.example.components.storeTextFieldColors
import com.example.domain.UiResult
import com.example.entities.ClientEntity
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GraySecondary
import com.example.ui.theme.OnGold
import com.example.ui.theme.WhiteText
import com.example.utils.CurrencyUtils
import com.example.utils.DateUtils
import com.example.viewmodel.ClientsUiState

@Composable
fun ClientsScreen(
    state: ClientsUiState,
    actionResult: UiResult,
    onDismissResult: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSaveClient: (clientId: Long?, fullName: String, phone: String, notes: String) -> Unit,
    onOpenClientDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFormDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("clients_screen"),
        containerColor = DeepBlack,
        topBar = {
            StoreTopBar(
                title = "Clientes",
                subtitle = "${state.clients.size} cadastrado(s)"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormDialog = true },
                containerColor = GoldPrimary,
                contentColor = OnGold,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Cadastrar cliente")
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
                query = state.searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Pesquisar por nome ou telefone...",
                testTag = "clients_search_input"
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (state.clients.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Person,
                    title = "Nenhum cliente encontrado",
                    description = "Cadastre clientes para acompanhar histórico de compras, telefone e preferências.",
                    actionLabel = "Novo cliente",
                    onAction = { showFormDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.clients, key = { it.client.id }) { row ->
                        LuxuryCard(
                            onClick = { onOpenClientDetail(row.client.id) },
                            modifier = Modifier.testTag("client_card_${row.client.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(GoldContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = row.client.fullName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(row.client.fullName, style = MaterialTheme.typography.titleMedium, color = WhiteText)
                                    Text(
                                        text = row.client.phone.ifBlank { "Sem telefone" } + " • Desde ${DateUtils.formatDate(row.client.createdAtMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GraySecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatCents(row.totalSpentCents),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${row.purchasesCount} compra(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GraySecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        ClientFormDialog(
            initialClient = null,
            onSave = { name, phone, notes ->
                onSaveClient(null, name, phone, notes)
                showFormDialog = false
            },
            onDismiss = { showFormDialog = false }
        )
    }
}

@Composable
fun ClientFormDialog(
    initialClient: ClientEntity?,
    onSave: (fullName: String, phone: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(initialClient?.id) { mutableStateOf(initialClient?.fullName ?: "") }
    var phone by remember(initialClient?.id) { mutableStateOf(initialClient?.phone ?: "") }
    var notes by remember(initialClient?.id) { mutableStateOf(initialClient?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        titleContentColor = WhiteText,
        title = { Text(if (initialClient == null) "Novo Cliente" else "Editar Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome completo *") },
                    singleLine = true,
                    colors = storeTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = storeTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_phone_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
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
                    if (name.isBlank()) {
                        error = "Informe o nome completo do cliente."
                    } else {
                        onSave(name, phone, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGold),
                modifier = Modifier.testTag("save_client_button")
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
