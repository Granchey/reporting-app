package com.vivocloud.reporting_app

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val imageUrls: List<String>) : GalleryUiState()
    data class Error(val message: String) : GalleryUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    var uiState by remember { mutableStateOf<GalleryUiState>(GalleryUiState.Loading) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun loadImages() {
        uiState = GalleryUiState.Loading
        scope.launch {
            uiState = fetchGalleryImages()
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
                    if (state.imageUrls.isEmpty()) {
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
                            items(state.imageUrls) { imageUrl ->
                                GalleryImageCard(
                                    imageUrl = imageUrl,
                                    onClick = { selectedImageUrl = imageUrl }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedImageUrl != null) {
            Dialog(onDismissRequest = { selectedImageUrl = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                ) {
                    val imageRequest = remember(selectedImageUrl) {
                        buildImageRequest(context, selectedImageUrl!!)
                    }

                    SubcomposeAsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    )

                    IconButton(
                        onClick = { selectedImageUrl = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close preview",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryImageCard(imageUrl: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageRequest = remember(imageUrl) {
        buildImageRequest(context, imageUrl)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                    Text(
                        text = "Image",
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                }
            }
        )
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

private suspend fun fetchGalleryImages(): GalleryUiState = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val baseUrl = ApiEndpoints.baseUrl

    if (baseUrl.isBlank()) {
        return@withContext GalleryUiState.Error("REPORTING_API_URL is missing or blank in local.properties")
    }

    val userImagesUrl = ApiEndpoints.userImagesUrl
    val allImagesUrl = ApiEndpoints.allImagesUrl

    try {
        fun buildReq(url: String): Request {
            val builder = Request.Builder()
                .url(url)
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("Accept", "application/json")
            AuthTokenManager.token?.let {
                builder.addHeader("Authorization", "Bearer $it")
            }
            return builder.build()
        }

        Log.d("GalleryScreen", "Fetching user images from: $userImagesUrl")
        var request = buildReq(userImagesUrl)
        var response = client.newCall(request).execute()

        if (!response.isSuccessful && (response.code == 404 || response.code == 403)) {
            Log.d("GalleryScreen", "GET $userImagesUrl returned ${response.code}, falling back to $allImagesUrl")
            response.close()
            request = buildReq(allImagesUrl)
            response = client.newCall(request).execute()
        }

        response.use { resp ->
            Log.d("GalleryScreen", "Response code: ${resp.code} for URL: ${resp.request.url}")
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string() ?: ""
                Log.e("GalleryScreen", "Gallery request failed code=${resp.code}, body=$errBody")
                return@withContext GalleryUiState.Error("Server returned code ${resp.code}")
            }

            val bodyString = resp.body?.string() ?: ""
            Log.d("GalleryScreen", "Received JSON response body: $bodyString")
            val parsedUrls = parseImageUrls(bodyString)
            Log.d("GalleryScreen", "Extracted ${parsedUrls.size} image URLs: $parsedUrls")
            return@withContext GalleryUiState.Success(parsedUrls)
        }
    } catch (e: Exception) {
        Log.e("GalleryScreen", "Failed to fetch images from $userImagesUrl", e)
        return@withContext GalleryUiState.Error("Error: ${e.localizedMessage ?: "Failed to connect"}")
    }
}

private fun parseImageUrls(json: String): List<String> {
    val urls = mutableListOf<String>()
    val trimmed = json.trim()
    if (trimmed.isEmpty()) return urls

    try {
        if (trimmed.startsWith("[")) {
            val jsonArray = JSONArray(trimmed)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.get(i)
                extractUrl(item)?.let { urls.add(it) }
            }
        } else if (trimmed.startsWith("{")) {
            val jsonObject = JSONObject(trimmed)
            val keys = listOf("images", "data", "files", "items", "result")
            var arrayFound: JSONArray? = null
            for (key in keys) {
                if (jsonObject.has(key)) {
                    arrayFound = jsonObject.optJSONArray(key)
                    if (arrayFound != null) break
                }
            }
            if (arrayFound != null) {
                for (i in 0 until arrayFound.length()) {
                    val item = arrayFound.get(i)
                    extractUrl(item)?.let { urls.add(it) }
                }
            } else {
                extractUrl(jsonObject)?.let { urls.add(it) }
            }
        }
    } catch (e: Exception) {
        Log.e("GalleryScreen", "JSON parsing error", e)
    }

    return urls
}

private fun extractUrl(item: Any): String? {
    val baseUrl = ApiEndpoints.baseUrl
    val uuidRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    return when (item) {
        is String -> {
            val trimmedStr = item.trim()
            when {
                trimmedStr.matches(uuidRegex) -> ApiEndpoints.imageDownloadUrl(trimmedStr)
                trimmedStr.startsWith("http://") || trimmedStr.startsWith("https://") -> trimmedStr
                trimmedStr.startsWith("/") -> "$baseUrl$trimmedStr"
                else -> "$baseUrl/$trimmedStr"
            }
        }
        is JSONObject -> {
            val idKey = listOf("id", "imageId", "uuid").firstOrNull { item.has(it) && !item.isNull(it) }
            if (idKey != null) {
                val idVal = item.optString(idKey)
                if (idVal.isNotBlank()) {
                    return ApiEndpoints.imageDownloadUrl(idVal)
                }
            }

            val urlKey = listOf("downloadUrl", "url", "imageUrl", "path", "filename", "name").firstOrNull { item.has(it) && !item.isNull(it) }
            if (urlKey != null) {
                val urlVal = item.optString(urlKey).trim()
                return when {
                    urlVal.matches(uuidRegex) -> ApiEndpoints.imageDownloadUrl(urlVal)
                    urlVal.startsWith("http://") || urlVal.startsWith("https://") -> urlVal
                    urlVal.startsWith("/") -> "$baseUrl$urlVal"
                    else -> "$baseUrl/$urlVal"
                }
            }
            null
        }
        else -> null
    }
}
