package com.example.tradenetwork.ui.screens.listing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradenetwork.data.model.ProductStatus
import com.example.tradenetwork.ui.screens.home.CATEGORIES
import com.example.tradenetwork.viewmodel.EditListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(
    navController: NavController,
    productId: String
) {
    val viewModel: EditListingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditListingViewModel(productId) as T
            }
        }
    )

    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val category by viewModel.category.collectAsState()
    val condition by viewModel.condition.collectAsState()
    val location by viewModel.location.collectAsState()
    val status by viewModel.status.collectAsState()
    val existingImages by viewModel.existingImages.collectAsState()
    val newImageUris by viewModel.newImageUris.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addNewImages(uris)
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    EditListingContent(
        title = title,
        onTitleChange = { viewModel.title.value = it },
        description = description,
        onDescriptionChange = { viewModel.description.value = it },
        price = price,
        onPriceChange = { viewModel.price.value = it },
        category = category,
        onCategoryChange = { viewModel.category.value = it },
        condition = condition,
        onConditionChange = { viewModel.condition.value = it },
        location = location,
        onLocationChange = { viewModel.location.value = it },
        status = status,
        onStatusChange = { viewModel.status.value = it },
        existingImages = existingImages,
        newImageUris = newImageUris,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onAddImagesClick = { imagePickerLauncher.launch("image/*") },
        onRemoveExistingImage = { viewModel.removeExistingImage(it) },
        onRemoveNewImage = { viewModel.removeNewImage(it) },
        onUpdateClick = { viewModel.updateListing() },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingContent(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    condition: String,
    onConditionChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    status: ProductStatus,
    onStatusChange: (ProductStatus) -> Unit,
    existingImages: List<String>,
    newImageUris: List<Uri>,
    isLoading: Boolean,
    errorMessage: String?,
    onAddImagesClick: () -> Unit,
    onRemoveExistingImage: (String) -> Unit,
    onRemoveNewImage: (Uri) -> Unit,
    onUpdateClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Image Section
            Text("Product Images", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (existingImages.size + newImageUris.size < 10) {
                    OutlinedButton(
                        onClick = onAddImagesClick,
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Image")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add", fontSize = 12.sp)
                        }
                    }
                }

                for (url in existingImages) {
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Existing Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveExistingImage(url) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                for (uri in newImageUris) {
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "New Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveNewImage(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                label = { Text("Price (Bs) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("Bs. ") }
            )

            // Category Selection
            Text("Category *", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            ScrollableCategorySelector(
                categories = CATEGORIES.filter { it != "All" },
                selectedCategory = category,
                onSelect = onCategoryChange
            )

            // Condition Selection
            Text("Condition *", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cond in CONDITIONS) {
                    FilterChip(
                        selected = condition == cond,
                        onClick = { onConditionChange(cond) },
                        label = { Text(cond) }
                    )
                }
            }

            // Location
            Text("Location *", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (loc in LOCATIONS) {
                    FilterChip(
                        selected = location == loc,
                        onClick = { onLocationChange(loc) },
                        label = { Text(loc) }
                    )
                }
            }

            // Status Selection
            Text("Status *", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (st in ProductStatus.values()) {
                    FilterChip(
                        selected = status == st,
                        onClick = { onStatusChange(st) },
                        label = { Text(st.name) }
                    )
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Update Button
            Button(
                onClick = onUpdateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Listing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditListingScreenPreview() {
    MaterialTheme {
        EditListingContent(
            title = "Mountain Bike",
            onTitleChange = {},
            description = "21-speed mountain bike in excellent condition.",
            onDescriptionChange = {},
            price = "1200",
            onPriceChange = {},
            category = "Vehicles",
            onCategoryChange = {},
            condition = "Good",
            onConditionChange = {},
            location = "Santa Cruz",
            onLocationChange = {},
            status = ProductStatus.AVAILABLE,
            onStatusChange = {},
            existingImages = emptyList(),
            newImageUris = emptyList(),
            isLoading = false,
            errorMessage = null,
            onAddImagesClick = {},
            onRemoveExistingImage = {},
            onRemoveNewImage = {},
            onUpdateClick = {},
            onBackClick = {}
        )
    }
}
