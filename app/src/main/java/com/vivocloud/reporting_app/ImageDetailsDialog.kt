package com.vivocloud.reporting_app

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.vivocloud.reporting_app.api.RetrofitClient
import com.vivocloud.reporting_app.data.ImageEntity
import com.vivocloud.reporting_app.ui.theme.DarkGreenAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageDetailsDialog(
    imageEntity: ImageEntity,
    imageUrl: String?,
    onDismiss: () -> Unit,
    onUpdated: (ImageEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val confidence = imageEntity.confidence ?: 0.0
    val isLowConfidence = confidence < 90.0

    var isEditing by remember { mutableStateOf(isLowConfidence) }
    var editedCategory by remember { mutableStateOf(imageEntity.category ?: "") }
    var editedDescription by remember { mutableStateOf(imageEntity.aiDescription ?: "") }
    var editedDepartment by remember { mutableStateOf(imageEntity.recommendedDepartment ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) "Review & Edit Service Details" else "AI Analysis Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image Display
                    if (!imageUrl.isNullOrBlank()) {
                        val request = remember(imageUrl) {
                            ImageRequest.Builder(context)
                                .data(imageUrl)
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .apply {
                                    AuthTokenManager.token?.let { addHeader("Authorization", "Bearer $it") }
                                }
                                .build()
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = request,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Confidence Score & Category Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // AI Category Badge
                        val categoryText = imageEntity.category?.ifBlank { null } ?: "GENERAL"
                        AssistChip(
                            onClick = {},
                            label = { Text(categoryText, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = DarkGreenAccent.copy(alpha = 0.15f),
                                labelColor = DarkGreenAccent
                            )
                        )

                        // Confidence % Badge
                        val confidenceColor = if (isLowConfidence) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = confidenceColor.copy(alpha = 0.12f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLowConfidence) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = confidenceColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "${String.format("%.1f", confidence)}% Accuracy",
                                    color = confidenceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Low Confidence Banner Notice (< 90%)
                    if (isLowConfidence) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "AI Confidence is below 95%. AI predictions are kept as suggestions below for you to adjust manually.",
                                    color = Color(0xFFE65100),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (isEditing) {
                        // Editable Manual Input Form (AI predictions kept as suggestions)
                        Text(
                            text = "Category / Service Type",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editedCategory,
                            onValueChange = { editedCategory = it },
                            placeholder = { Text("e.g. ROAD_MAINTENANCE") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreenAccent,
                                focusedLabelColor = DarkGreenAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Summary Description",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            placeholder = { Text("AI prediction description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreenAccent,
                                focusedLabelColor = DarkGreenAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Recommended Department",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editedDepartment,
                            onValueChange = { editedDepartment = it },
                            placeholder = { Text("e.g. Department of Public Works") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreenAccent,
                                focusedLabelColor = DarkGreenAccent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    val updatedEntity = imageEntity.copy(
                                        category = editedCategory.ifBlank { imageEntity.category },
                                        aiDescription = editedDescription.ifBlank { imageEntity.aiDescription },
                                        recommendedDepartment = editedDepartment.ifBlank { imageEntity.recommendedDepartment }
                                    )
                                    val success = saveImageUpdate(context, updatedEntity)
                                    isSaving = false
                                    if (success) {
                                        Toast.makeText(context, "Details saved successfully!", Toast.LENGTH_SHORT).show()
                                        onUpdated(updatedEntity)
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Details saved locally", Toast.LENGTH_SHORT).show()
                                        onUpdated(updatedEntity)
                                        onDismiss()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenAccent),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save & Confirm Details")
                            }
                        }
                    } else {
                        // Read-Only Display Mode (Confidence >= 95%)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Summary Description",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = imageEntity.aiDescription?.ifBlank { null } ?: "No description available",
                                    fontSize = 15.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Recommended Department",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = imageEntity.recommendedDepartment?.ifBlank { null } ?: "Not specified",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkGreenAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Service Details Manually")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun saveImageUpdate(context: Context, updatedEntity: ImageEntity): Boolean = withContext(Dispatchers.IO) {
    val imageId = updatedEntity.id ?: return@withContext false
    val token = AuthTokenManager.token ?: ""

    try {
        val authHeader = if (token.isNotBlank()) "Bearer $token" else ""
        val response = RetrofitClient.getApiService().updateImageDetails(authHeader, imageId, updatedEntity)
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}
