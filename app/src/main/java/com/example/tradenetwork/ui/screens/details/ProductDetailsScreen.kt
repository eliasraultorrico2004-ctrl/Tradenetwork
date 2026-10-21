package com.example.tradenetwork.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradenetwork.data.model.Product
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.data.model.User
import com.example.tradenetwork.data.repository.ChatRepositoryImpl
import com.example.tradenetwork.navigation.Screen
import com.example.tradenetwork.ui.screens.report.ReportDialog
import com.example.tradenetwork.viewmodel.ProductDetailsUiState
import com.example.tradenetwork.viewmodel.ProductDetailsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavController,
    productId: String
) {
    val viewModel: ProductDetailsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProductDetailsViewModel(productId) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    if (showReportDialog) {
        val state = uiState
        if (state is ProductDetailsUiState.Success) {
            ReportDialog(
                targetType = "PRODUCT",
                targetId = state.product.id,
                onDismiss = { showReportDialog = false }
            )
        }
    }

    when (val state = uiState) {
        is ProductDetailsUiState.Loading -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Product Details") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        is ProductDetailsUiState.NotFound -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Product Details") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("This product is no longer available.", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }) {
                        Text("Return to Home")
                    }
                }
            }
        }
        is ProductDetailsUiState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Product Details") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is ProductDetailsUiState.Success -> {
            ProductDetailsContent(
                product = state.product,
                seller = state.seller,
                isFavorite = state.isFavorite,
                isOwner = state.isOwner,
                onBackClick = { navController.popBackStack() },
                onToggleFavorite = { viewModel.toggleFavorite() },
                onReportClick = { showReportDialog = true },
                onEditListing = { navController.navigate(Screen.EditListing.createRoute(state.product.id)) },
                onUpdateStatus = { nextStatus -> viewModel.updateStatus(nextStatus) },
                onContactSeller = {
                    scope.launch {
                        val repo = ChatRepositoryImpl()
                        val buyerId = auth.currentUser?.uid ?: return@launch
                        val result = repo.getOrCreateConversation(
                            buyerId = buyerId,
                            sellerId = state.product.sellerId,
                            productId = state.product.id,
                            productTitle = state.product.title,
                            productImage = state.product.images.firstOrNull() ?: ""
                        )
                        result.onSuccess { conversationId ->
                            navController.navigate(Screen.Chat.createRoute(conversationId))
                        }
                    }
                },
                onSellerClick = {
                    navController.navigate(Screen.SellerProfile.createRoute(state.product.sellerId))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsContent(
    product: Product,
    seller: User?,
    isFavorite: Boolean,
    isOwner: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReportClick: () -> Unit,
    onEditListing: () -> Unit,
    onUpdateStatus: (ProductStatus) -> Unit,
    onContactSeller: () -> Unit,
    onSellerClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { if (product.images.isEmpty()) 1 else product.images.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = onReportClick) {
                        Icon(Icons.Default.Flag, contentDescription = "Report")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isOwner) {
                        OutlinedButton(
                            onClick = onEditListing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Listing")
                        }
                        Button(
                            onClick = {
                                val nextStatus = when (product.status) {
                                    ProductStatus.AVAILABLE -> ProductStatus.SOLD
                                    ProductStatus.RESERVED -> ProductStatus.AVAILABLE
                                    ProductStatus.SOLD -> ProductStatus.AVAILABLE
                                }
                                onUpdateStatus(nextStatus)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (product.status == ProductStatus.SOLD) "Mark Available" else "Mark Sold")
                        }
                    } else {
                        if (product.status == ProductStatus.SOLD) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Product Sold")
                            }
                        } else {
                            Button(
                                onClick = onContactSeller,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = product.status == ProductStatus.AVAILABLE
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (product.status == ProductStatus.RESERVED) "Product Reserved (Contact Seller)" else "Contact Seller")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Image Pager Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.DarkGray)
            ) {
                if (product.images.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = product.images[page],
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${product.images.size}",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                if (product.status != ProductStatus.AVAILABLE) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        color = if (product.status == ProductStatus.SOLD) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = product.status.name,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Bs. ${product.price}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Publication Date
                Text(
                    text = formatTimestamp(product.createdAt),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )

                HorizontalDivider()

                // Details Chips / Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem(label = "Condition", value = product.condition)
                    DetailItem(label = "Category", value = product.category)
                    DetailItem(label = "Location", value = product.location)
                }

                HorizontalDivider()

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = product.description, fontSize = 14.sp)
                }

                HorizontalDivider()

                // Seller Card
                if (seller != null) {
                    SellerCard(
                        seller = seller,
                        onClick = onSellerClick
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SellerCard(seller: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = seller.name.ifEmpty { "Seller" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = seller.location ?: "Bolivia", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "Recently published"
    val date = timestamp.toDate()
    val now = Date()
    val diffMillis = now.time - date.time
    val diffDays = diffMillis / (1000 * 60 * 60 * 24)
    return when {
        diffDays == 0L -> "Published today"
        diffDays == 1L -> "Published 1 day ago"
        diffDays < 7L -> "Published $diffDays days ago"
        else -> "Published ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)}"
    }
}
