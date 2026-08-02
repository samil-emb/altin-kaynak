package com.example.altin_kaynak.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.altin_kaynak.data.db.SavingsEntity
import com.example.altin_kaynak.data.model.SAVINGS_TYPES
import com.example.altin_kaynak.ui.viewmodel.OwnerGroup
import com.example.altin_kaynak.ui.viewmodel.PriceSource
import com.example.altin_kaynak.ui.viewmodel.SavingsLine
import com.example.altin_kaynak.ui.viewmodel.SavingsViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavingsScreen(viewModel: SavingsViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsState()
    val totalValue by viewModel.totalValue.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val owners by viewModel.owners.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val simulatedGramPrice by viewModel.simulatedGramPrice.collectAsState()
    val currentGramPrice by viewModel.currentGramPrice.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSimulationDialog by remember { mutableStateOf(false) }
    var expandedOwners by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = { showSimulationDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Tahmin")
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ekle")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SourceButton(
                    text = "Altınkaynak",
                    selected = selectedSource == PriceSource.ALTINKAYNAK,
                    onClick = { viewModel.selectSource(PriceSource.ALTINKAYNAK) },
                    modifier = Modifier.weight(1f)
                )
                SourceButton(
                    text = "Harem",
                    selected = selectedSource == PriceSource.HAREM,
                    onClick = { viewModel.selectSource(PriceSource.HAREM) },
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Toplam Birikim Değeri",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatCurrency(totalValue),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (simulatedGramPrice != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simülasyon aktif: Gram Altın ${formatCurrency(simulatedGramPrice!!)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { viewModel.resetSimulation() }) {
                        Text("Sıfırla")
                    }
                }
            }

            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz birikim eklenmedi.\nSağ alttaki + butonuna basarak ekleyebilirsiniz.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(groups, key = { it.owner }) { group ->
                        OwnerGroupCard(
                            group = group,
                            expanded = expandedOwners.contains(group.owner),
                            onToggleExpand = {
                                expandedOwners = if (expandedOwners.contains(group.owner)) {
                                    expandedOwners - group.owner
                                } else {
                                    expandedOwners + group.owner
                                }
                            },
                            onDeleteItem = { viewModel.deleteSaving(it) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSavingsDialog(
                owners = owners,
                notes = notes,
                onDismiss = { showAddDialog = false },
                onAdd = { owner, type, quantity, note ->
                    viewModel.addSaving(owner, type, quantity, note)
                    showAddDialog = false
                }
            )
        }

        if (showSimulationDialog) {
            SimulationDialog(
                currentGramPrice = currentGramPrice,
                simulatedGramPrice = simulatedGramPrice,
                onDismiss = { showSimulationDialog = false },
                onApply = { price ->
                    viewModel.applySimulation(price)
                    showSimulationDialog = false
                },
                onReset = {
                    viewModel.resetSimulation()
                    showSimulationDialog = false
                }
            )
        }
    }
}

@Composable
fun SourceButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(text)
    }
}

@Composable
fun OwnerGroupCard(
    group: OwnerGroup,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteItem: (SavingsEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.owner,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = group.items.joinToString(", ") {
                            "${formatQuantity(it.entity.quantity)} ${it.entity.type} (${formatCurrency(it.value)})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(group.totalValue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Kapat" else "Detaylar"
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                group.items.forEach { line ->
                    val item = line.entity
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.type, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Miktar: ${formatQuantity(item.quantity)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(line.value),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (item.note.isNotEmpty()) {
                                Text(
                                    text = item.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteItem(item) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatQuantity(quantity: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("tr", "TR")).apply {
        maximumFractionDigits = 2
    }
    return formatter.format(quantity)
}

private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("tr", "TR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(value)} ₺"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsDialog(
    owners: List<String>,
    notes: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String) -> Unit
) {
    var owner by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SAVINGS_TYPES[0].label) }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Birikim Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditableDropdownField(
                    label = "Nereye eklensin",
                    value = owner,
                    onValueChange = { owner = it },
                    suggestions = owners
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tür") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        SAVINGS_TYPES.forEach { info ->
                            DropdownMenuItem(
                                text = { Text(info.label) },
                                onClick = {
                                    selectedType = info.label
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Miktar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                EditableDropdownField(
                    label = "Not (isteğe bağlı)",
                    value = note,
                    onValueChange = { note = it },
                    suggestions = notes
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.replace(",", ".").toDoubleOrNull()
                    if (q != null && q > 0 && owner.isNotBlank()) {
                        onAdd(owner.trim(), selectedType, q, note)
                    }
                },
                enabled = quantity.isNotEmpty() && owner.isNotBlank()
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun SimulationDialog(
    currentGramPrice: Double,
    simulatedGramPrice: Double?,
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit,
    onReset: () -> Unit
) {
    var input by remember(simulatedGramPrice, currentGramPrice) {
        mutableStateOf(formatQuantity(simulatedGramPrice ?: currentGramPrice))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tahmin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Güncel Gram Altın Alış: ${formatCurrency(currentGramPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Gram altına vereceğiniz değere göre tüm altın türleri ve toplam birikim oranlanarak güncellenir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Tahmini Gram Altın Fiyatı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = input.replace(".", "").replace(",", ".").toDoubleOrNull()
                    if (price != null && price > 0) {
                        onApply(price)
                    }
                }
            ) {
                Text("Uygula")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Sıfırla")
                }
                TextButton(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions) {
        if (value.isBlank()) suggestions
        else suggestions.filter { it.contains(value, ignoreCase = true) && it != value }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            trailingIcon = {
                if (suggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}
