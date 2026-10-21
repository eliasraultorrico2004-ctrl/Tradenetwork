package com.example.tradenetwork.ui.screens.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tradenetwork.data.model.REPORT_REASONS
import com.example.tradenetwork.viewmodel.ReportUiState
import com.example.tradenetwork.viewmodel.ReportViewModel

@Composable
fun ReportDialog(
    targetType: String,
    targetId: String,
    onDismiss: () -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    var selectedReason by remember { mutableStateOf(REPORT_REASONS[0]) }
    var description by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is ReportUiState.Success) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report ${targetType.lowercase()}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState is ReportUiState.Error) {
                    Text(
                        text = (uiState as ReportUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Text("Select a reason:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                for (reason in REPORT_REASONS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reason, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitReport(targetType, targetId, selectedReason, description)
                },
                enabled = uiState !is ReportUiState.Loading
            ) {
                if (uiState is ReportUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Report")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = uiState !is ReportUiState.Loading) {
                Text("Cancel")
            }
        }
    )
}
