package com.example.tradenetwork.ui.screens.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tradenetwork.ui.screens.home.CATEGORIES
import com.example.tradenetwork.ui.screens.listing.CONDITIONS
import com.example.tradenetwork.ui.screens.listing.LOCATIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentCategory: String,
    currentMinPrice: String,
    currentMaxPrice: String,
    currentCondition: String?,
    currentLocation: String?,
    onApply: (category: String, minPrice: String, maxPrice: String, condition: String?, location: String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf(currentCategory) }
    var minPrice by remember { mutableStateOf(currentMinPrice) }
    var maxPrice by remember { mutableStateOf(currentMaxPrice) }
    var condition by remember { mutableStateOf(currentCondition) }
    var location by remember { mutableStateOf(currentLocation) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Products", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                TextButton(onClick = {
                    onClear()
                    onDismiss()
                }) {
                    Text("Clear All")
                }
            }

            // Category
            Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in CATEGORIES) {
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Price Range
            Text("Price Range (Bs)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it },
                    label = { Text("Min Bs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Text("-")
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    label = { Text("Max Bs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Condition
            Text("Condition", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = condition == null,
                    onClick = { condition = null },
                    label = { Text("Any") }
                )
                for (cond in CONDITIONS) {
                    FilterChip(
                        selected = condition == cond,
                        onClick = { condition = cond },
                        label = { Text(cond) }
                    )
                }
            }

            // Location
            Text("Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = location == null,
                    onClick = { location = null },
                    label = { Text("Any") }
                )
                for (loc in LOCATIONS) {
                    FilterChip(
                        selected = location == loc,
                        onClick = { location = loc },
                        label = { Text(loc) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onApply(category, minPrice, maxPrice, condition, location)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
