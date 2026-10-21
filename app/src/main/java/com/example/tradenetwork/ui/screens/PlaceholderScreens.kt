package com.example.tradenetwork.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Search Screen Placeholder")
    }
}

@Composable
fun SellPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Sell Screen Placeholder")
    }
}

@Composable
fun FavoritesPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Favorites Screen Placeholder")
    }
}

@Composable
fun ProfilePlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen Placeholder")
    }
}

@Composable
fun ProductDetailsPlaceholder(productId: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Product Details Placeholder for ID: $productId")
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPlaceholderPreview() {
    MaterialTheme { SearchPlaceholder() }
}

@Preview(showBackground = true)
@Composable
fun SellPlaceholderPreview() {
    MaterialTheme { SellPlaceholder() }
}

@Preview(showBackground = true)
@Composable
fun FavoritesPlaceholderPreview() {
    MaterialTheme { FavoritesPlaceholder() }
}

