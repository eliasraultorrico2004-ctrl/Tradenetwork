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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradenetwork.ui.screens.home.CATEGORIES
import com.example.tradenetwork.viewmodel.CreateListingViewModel

val CONDITIONS = listOf("New", "Like New", "Good", "Fair", "Used")
val LOCATIONS = listOf("Santa Cruz", "La Paz", "Cochabamba", "Sucre", "Tarija", "Oruro", "Potosi", "Beni", "Pando")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    navController: NavController,
    viewModel: CreateListingViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val category by viewModel.category.collectAsState()
    val condition by viewModel.condition.collectAsState()
    val location by viewModel.location.collectAsState()
    val imageUris by viewModel.imageUris.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addImages(uris)
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    CreateListingContent(
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
        imageUris = imageUris,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onAddImagesClick = { imagePickerLauncher.launch("image/*") },
        onRemoveImage = { viewModel.removeImage(it) },
        onPublishClick = { viewModel.publishListing() },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingContent(
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
    imageUris: List<Uri>,
    isLoading: Boolean,
    errorMessage: String?,
    onAddImagesClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onPublishClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Listing") },
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

            // Image Picker Section
            Text("Product Images (Up to 10)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                for (uri in imageUris) {
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImage(uri) },
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

            // Price in Bs
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

            // Publish Button
            Button(
                onClick = onPublishClick,
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
                    Text("Publish Listing", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScrollableCategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (cat in categories) {
            FilterChip(
                selected = selectedCategory == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateListingScreenPreview() {
    MaterialTheme {
        CreateListingContent(
            title = "Acoustic Guitar",
            onTitleChange = {},
            description = "Fender acoustic guitar in great condition with soft case included.",
            onDescriptionChange = {},
            price = "800",
            onPriceChange = {},
            category = "Other",
            onCategoryChange = {},
            condition = "Good",
            onConditionChange = {},
            location = "La Paz",
            onLocationChange = {},
            imageUris = emptyList(),
            isLoading = false,
            errorMessage = null,
            onAddImagesClick = {},
            onRemoveImage = {},
            onPublishClick = {},
            onBackClick = {}
        )
    }
}
