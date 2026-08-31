package com.vivocloud.reporting_app

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vivocloud.reporting_app.api.RetrofitClient
import com.vivocloud.reporting_app.data.ImageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var capturedFile by remember { mutableStateOf<File?>(null) }

    if (cameraPermissionState.status.isGranted) {
        if (capturedFile == null) {
            CameraPreviewContent(onImageCaptured = { file ->
                capturedFile = file
            })
        } else {
            PhotoPreviewScreen(
                file = capturedFile!!,
                onRetake = {
                    capturedFile?.delete()
                    capturedFile = null
                },
                onUploadSuccess = {
                    capturedFile?.delete()
                    capturedFile = null
                }
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.camera_permission_required))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun CameraPreviewContent(onImageCaptured: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(lensFacing) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraScreen", "Binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = stringResource(R.string.switch_camera),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    takePhoto(context, imageCapture, onImageCaptured)
                },
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = stringResource(R.string.take_photo),
                    tint = Color.Black,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun PhotoPreviewScreen(
    file: File,
    onRetake: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var uploadedEntity by remember { mutableStateOf<ImageEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = rememberAsyncImagePainter(file),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isUploading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.uploading), color = Color.White)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRetake,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.retake_button))
            }

            Button(
                onClick = {
                    isUploading = true
                    scope.launch {
                        val resultEntity = uploadPhoto(file)
                        isUploading = false
                        if (resultEntity != null) {
                            Toast.makeText(context, R.string.upload_success, Toast.LENGTH_SHORT).show()
                            uploadedEntity = resultEntity
                        } else {
                            Toast.makeText(context, context.getString(R.string.upload_failed, "Upload Error"), Toast.LENGTH_LONG).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                enabled = !isUploading
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.upload_button))
            }
        }
    }

    if (uploadedEntity != null) {
        ImageDetailsDialog(
            imageEntity = uploadedEntity!!,
            imageUrl = file.absolutePath,
            onDismiss = {
                uploadedEntity = null
                onUploadSuccess()
            }
        )
    }
}

@Composable
fun ImageDetailsDialog(
    imageEntity: ImageEntity,
    imageUrl: String,
    onDismiss: () -> Unit
) { AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Image Uploaded") },
        text = {
            Column {
                Text(text = "ID: ${imageEntity.id}")
                Text(text = "Path: $imageUrl")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCaptured: (File) -> Unit) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val file = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "$name.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(file)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Capture failed: ${exception.message}", exception)
            }
        }
    )
}

private suspend fun uploadPhoto(file: File): ImageEntity? = withContext(Dispatchers.IO) {
    val token = AuthTokenManager.token ?: ""
    val authHeader = if (token.isNotBlank()) "Bearer $token" else ""

    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val bodyPart = MultipartBody.Part.createFormData("file", file.name, requestFile)

    try {
        val response = RetrofitClient.getApiService().uploadImage(authHeader, bodyPart)
        if (response.isSuccessful) {
            return@withContext response.body() ?: ImageEntity()
        } else {
            Log.e("Upload", "Upload failed code=${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.e("Upload", "Failed to upload photo", e)
        null
    }
}
