package com.example.altin_kaynak.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.altin_kaynak.data.model.GoldPrice
import com.example.altin_kaynak.ui.viewmodel.PriceSource
import com.example.altin_kaynak.ui.viewmodel.PricesState
import com.example.altin_kaynak.ui.viewmodel.PricesViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PricesScreen(viewModel: PricesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.selectSource(PriceSource.ALTINKAYNAK) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSource == PriceSource.ALTINKAYNAK)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (selectedSource == PriceSource.ALTINKAYNAK)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Altınkaynak")
            }
            Button(
                onClick = { viewModel.selectSource(PriceSource.HAREM) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSource == PriceSource.HAREM)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (selectedSource == PriceSource.HAREM)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Harem")
            }
        }

        when (val s = state) {
            is PricesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PricesState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hata: ${s.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tekrar Dene")
                    }
                }
            }
            is PricesState.Success -> {
                val favoriteItems = remember(s.prices, favorites, selectedSource) {
                    s.prices.filter { favorites.contains("${selectedSource.name}:${it.symbol}") }
                }
                val otherItems = remember(s.prices, favorites, selectedSource) {
                    s.prices.filterNot { favorites.contains("${selectedSource.name}:${it.symbol}") }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (favoriteItems.isNotEmpty()) {
                        item(key = "header_favorites") { SectionHeader("Favoriler") }
                        items(favoriteItems, key = { "fav_${it.symbol}" }) { price ->
                            PriceCard(
                                price = price,
                                isFavorite = true,
                                onToggleFavorite = { viewModel.toggleFavorite(price.symbol) }
                            )
                        }
                        item(key = "header_all") { SectionHeader("Tümü") }
                    }
                    items(otherItems, key = { it.symbol }) { price ->
                        PriceCard(
                            price = price,
                            isFavorite = false,
                            onToggleFavorite = { viewModel.toggleFavorite(price.symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun PriceCard(price: GoldPrice, isFavorite: Boolean = false, onToggleFavorite: () -> Unit = {}) {
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("tr", "TR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier
                    .clickable(onClick = onToggleFavorite)
                    .padding(end = 12.dp)
            )
            Text(
                text = price.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Alış: ${formatter.format(price.buy)} ₺",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Satış: ${formatter.format(price.sell)} ₺",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
