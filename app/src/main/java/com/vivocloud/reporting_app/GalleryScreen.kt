package com.vivocloud.reporting_app

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.vivocloud.reporting_app.api.RetrofitClient
import com.vivocloud.reporting_app.data.ImageEntity
import com.vivocloud.reporting_app.ui.theme.DarkGreenAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val items: List<Pair<ImageEntity, String>>) : GalleryUiState()
    data class Error(val message: String) : GalleryUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    var uiState by remember { mutableStateOf<GalleryUiState>(GalleryUiState.Loading) }
    var selectedItem by remember { mutableStateOf<Pair<ImageEntity, String>?>(null) }
    val scope = rememberCoroutineScope()

    fun loadImages() {
        uiState = GalleryUiState.Loading
        scope.launch {
            uiState = fetchGalleryItems()
        }
    }

    LaunchedEffect(Unit) {
        loadImages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.gallery_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { loadImages() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is GalleryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GalleryUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadImages() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                is GalleryUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_images_found),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.items) { itemPair ->
                                GalleryImageCard(
                                    imageEntity = itemPair.first,
                                    imageUrl = itemPair.second,
                                    onClick = { selectedItem = itemPair }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedItem != null) {
            ImageDetailsDialog(
                imageEntity = selectedItem!!.first,
                imageUrl = selectedItem!!.second,
                onDismiss = { selectedItem = null },
                onUpdated = { loadImages() }
            )
        }
    }
}

@Composable
fun GalleryImageCard(
    imageEntity: ImageEntity,
    imageUrl: String,
    onClick: () -> Unit
) {
    val confidence = imageEntity.confidence ?: 0.0
    val isLowConfidence = confidence < 95.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
            ) {
                val context = LocalContext.current
                val imageRequest = remember(imageUrl) { buildImageRequest(context, imageUrl) }

                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Image", color = Color.DarkGray, fontSize = 12.sp)
                        }
                    }
                )

                // Category Badge (Top Left)
                val categoryText = imageEntity.category?.ifBlank { null } ?: "GENERAL"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = categoryText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Confidence % Score Badge (Top Right)
                val confidenceColor = if (isLowConfidence) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = confidenceColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", confidence)}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Description & Department Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = imageEntity.aiDescription?.ifBlank { null } ?: "AI Image Report",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = imageEntity.recommendedDepartment?.ifBlank { null } ?: "Department N/A",
                    fontSize = 12.sp,
                    color = DarkGreenAccent,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun buildImageRequest(context: Context, imageUrl: String): ImageRequest {
    val builder = ImageRequest.Builder(context)
        .data(imageUrl)
        .addHeader("ngrok-skip-browser-warning", "true")

    AuthTokenManager.token?.let {
        builder.addHeader("Authorization", "Bearer $it")
    }

    return builder.build()
}

private suspend fun fetchGalleryItems(): GalleryUiState = withContext(Dispatchers.IO) {
    val token = AuthTokenManager.token ?: ""
    val authHeader = if (token.isNotBlank()) "Bearer $token" else ""

    try {
        val response = RetrofitClient.getApiService().getUserImages(authHeader)
        if (response.isSuccessful) {
            val entities = response.body() ?: emptyList()
            val items = entities.map { entity ->
                val imageId = entity.id ?: ""
                val imageUrl = when {
                    !entity.downloadUrl.isNullOrBlank() -> entity.downloadUrl
                    !entity.url.isNullOrBlank() -> entity.url
                    !entity.imageUrl.isNullOrBlank() -> entity.imageUrl
                    imageId.isNotBlank() -> ApiEndpoints.imageDownloadUrl(imageId)
                    else -> ""
                }
                Pair(entity, imageUrl)
            }
            GalleryUiState.Success(items)
        } else {
            Log.e("GalleryScreen", "GET /api/v1/images/me failed code=${response.code()}")
            GalleryUiState.Error("Server returned code ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("GalleryScreen", "Failed to fetch images from Retrofit", e)
        GalleryUiState.Error("Error: ${e.localizedMessage ?: "Failed to connect"}")
    }
}
