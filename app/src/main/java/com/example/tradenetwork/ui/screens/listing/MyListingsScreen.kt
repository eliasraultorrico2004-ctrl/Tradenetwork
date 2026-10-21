package com.example.tradenetwork.ui.screens.listing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.navigation.Screen
import com.example.tradenetwork.viewmodel.MyListingsUiState
import com.example.tradenetwork.viewmodel.MyListingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    navController: NavController,
    viewModel: MyListingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Listing") },
            text = { Text("Are you sure you want to delete this listing?") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.id?.let { viewModel.deleteProduct(it) }
                    productToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    MyListingsContent(
        uiState = uiState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onBackClick = { navController.popBackStack() },
        onCreateClick = { navController.navigate(Screen.CreateListing.route) },
        onEditClick = { productId -> navController.navigate(Screen.EditListing.createRoute(productId)) },
        onDeleteClick = { product -> productToDelete = product },
        onStatusChange = { productId, newStatus -> viewModel.updateStatus(productId, newStatus) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsContent(
    uiState: MyListingsUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onStatusChange: (String, ProductStatus) -> Unit
) {
    val tabs = listOf("All", "Available", "Reserved", "Sold")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCreateClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Listing")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PrimaryScrollableTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = uiState) {
                is MyListingsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MyListingsUiState.Success -> {
                    val filteredProducts = when (selectedTab) {
                        1 -> state.products.filter { it.status == ProductStatus.AVAILABLE }
                        2 -> state.products.filter { it.status == ProductStatus.RESERVED }
                        3 -> state.products.filter { it.status == ProductStatus.SOLD }
                        else -> state.products
                    }

                    if (filteredProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No listings found in this category.", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProducts) { product ->
                                MyListingCard(
                                    product = product,
                                    onEdit = { onEditClick(product.id) },
                                    onDelete = { onDeleteClick(product) },
                                    onStatusChange = { newStatus ->
                                        onStatusChange(product.id, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
                is MyListingsUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("You haven't posted any listings yet.", color = MaterialTheme.colorScheme.outline)
                    }
                }
                is MyListingsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun MyListingCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ProductStatus) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = product.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "Bs. ${product.price}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = when (product.status) {
                        ProductStatus.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer
                        ProductStatus.RESERVED -> MaterialTheme.colorScheme.tertiaryContainer
                        ProductStatus.SOLD -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = product.status.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                    HorizontalDivider()
                    if (product.status != ProductStatus.AVAILABLE) {
                        DropdownMenuItem(
                            text = { Text("Mark Available") },
                            onClick = {
                                menuExpanded = false
                                onStatusChange(ProductStatus.AVAILABLE)
                            }
                        )
                    }
                    if (product.status != ProductStatus.RESERVED) {
                        DropdownMenuItem(
                            text = { Text("Mark Reserved") },
                            onClick = {
                                menuExpanded = false
                                onStatusChange(ProductStatus.RESERVED)
                            }
                        )
                    }
                    if (product.status != ProductStatus.SOLD) {
                        DropdownMenuItem(
                            text = { Text("Mark Sold") },
                            onClick = {
                                menuExpanded = false
                                onStatusChange(ProductStatus.SOLD)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyListingsScreenPreview() {
    MaterialTheme {
        MyListingsContent(
            uiState = MyListingsUiState.Success(
                listOf(
                    Product(id = "1", title = "Bluetooth Speaker", price = 200.0, status = ProductStatus.AVAILABLE),
                    Product(id = "2", title = "Office Chair", price = 400.0, status = ProductStatus.SOLD)
                )
            ),
            selectedTab = 0,
            onTabSelected = {},
            onBackClick = {},
            onCreateClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onStatusChange = { _, _ -> }
        )
    }
}
