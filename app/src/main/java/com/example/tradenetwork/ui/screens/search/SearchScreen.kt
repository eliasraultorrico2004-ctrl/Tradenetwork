package com.example.tradenetwork.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tradenetwork.navigation.Screen
import com.example.tradenetwork.ui.components.ProductCard
import com.example.tradenetwork.viewmodel.ProductUiState
import com.example.tradenetwork.viewmodel.SearchViewModel
import com.example.tradenetwork.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedCondition by viewModel.selectedCondition.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val activeFiltersCount = viewModel.activeFiltersCount()

    if (showFilterSheet) {
        FilterBottomSheet(
            currentCategory = selectedCategory,
            currentMinPrice = minPrice,
            currentMaxPrice = maxPrice,
            currentCondition = selectedCondition,
            currentLocation = selectedLocation,
            onApply = { cat, minP, maxP, cond, loc ->
                viewModel.setCategory(cat)
                viewModel.setPriceRange(minP, maxP)
                viewModel.setCondition(cond)
                viewModel.setLocation(loc)
            },
            onClear = {
                viewModel.clearAllFilters()
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Search Marketplace") }
                )
                
                // Search Bar and Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search title or description...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    // Filter Button with Badge
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge { Text(activeFiltersCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }

                    // Sort Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest first") },
                                onClick = { viewModel.setSortOption(SortOption.NEWEST); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest first") },
                                onClick = { viewModel.setSortOption(SortOption.OLDEST); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Lowest price") },
                                onClick = { viewModel.setSortOption(SortOption.PRICE_LOW_TO_HIGH); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest price") },
                                onClick = { viewModel.setSortOption(SortOption.PRICE_HIGH_TO_LOW); showSortMenu = false }
                            )
                        }
                    }
                }

                // Active Filters Chips Row
                if (activeFiltersCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCategory != "All") {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setCategory("All") },
                                label = { Text("Category: $selectedCategory") },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        if (minPrice.isNotEmpty() || maxPrice.isNotEmpty()) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setPriceRange("", "") },
                                label = { Text("Price: ${minPrice.ifEmpty { "0" }} - ${maxPrice.ifEmpty { "∞" }} Bs") },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        if (selectedCondition != null) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setCondition(null) },
                                label = { Text("Condition: $selectedCondition") },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        if (!selectedLocation.isNullOrEmpty()) {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setLocation(null) },
                                label = { Text("Location: $selectedLocation") },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${state.products.size} products found",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(state.products) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = {
                                        navController.navigate(Screen.ProductDetails.createRoute(product.id))
                                    }
                                )
                            }
                        }
                    }
                }
                is ProductUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No products match your search. Try changing your filters.",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                is ProductUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
