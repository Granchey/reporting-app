package com.vivocloud.reporting_app

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vivocloud.reporting_app.ui.theme.DarkGreenAccent
import com.vivocloud.reporting_app.ui.theme.LightGreenBackground
import com.vivocloud.reporting_app.ui.theme.ReportingappTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGreenBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_welcome_back),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreenAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_to_your_account),
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1C1B1F),
                    unfocusedTextColor = Color(0xFF1C1B1F),
                    focusedLabelColor = DarkGreenAccent,
                    unfocusedLabelColor = Color(0xFF49454F),
                    focusedBorderColor = DarkGreenAccent,
                    unfocusedBorderColor = Color(0xFF79747E),
                    cursorColor = DarkGreenAccent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1C1B1F),
                    unfocusedTextColor = Color(0xFF1C1B1F),
                    focusedLabelColor = DarkGreenAccent,
                    unfocusedLabelColor = Color(0xFF49454F),
                    focusedBorderColor = DarkGreenAccent,
                    unfocusedBorderColor = Color(0xFF79747E),
                    cursorColor = DarkGreenAccent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val result = performLogin(email.trim(), password)
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreenAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(2.dp))
                } else {
                    Text(
                        text = stringResource(R.string.login_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = stringResource(R.string.no_account_text),
                    color = Color(0xFF49454F)
                )
                Text(
                    text = stringResource(R.string.sign_up_text),
                    color = DarkGreenAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

private suspend fun performLogin(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
    val client = com.vivocloud.reporting_app.api.RetrofitClient.okHttpClient
    val loginUrl = ApiEndpoints.loginUrl

    if (ApiEndpoints.baseUrl.isBlank()) {
        Log.e("LoginScreen", "API URL is missing or blank in local.properties")
        return@withContext Result.failure(Exception("REPORTING_API_URL is missing in local.properties"))
    }

    Log.d("LoginScreen", "Sending POST login request to: $loginUrl")

    val jsonBody = JSONObject().apply {
        put("email", email)
        put("password", password)
    }.toString()

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(loginUrl)
        .addHeader("ngrok-skip-browser-warning", "true")
        .addHeader("Accept", "application/json")
        .post(requestBody)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            Log.d("LoginScreen", "Response code: ${response.code}")
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                AuthTokenManager.saveTokenFromResponse(bodyString)
                Result.success(Unit)
            } else {
                val errorBody = response.body?.string() ?: ""
                Log.e("LoginScreen", "Login failed code=${response.code}, body=$errorBody")
                val parsedMsg = try {
                    if (errorBody.isNotBlank()) {
                        val json = JSONObject(errorBody)
                        json.optString("message", "").ifBlank { null }
                    } else null
                } catch (e: Exception) { null }

                val cleanMsg = when {
                    parsedMsg?.contains("Bad credentials", ignoreCase = true) == true -> "Invalid email or password"
                    !parsedMsg.isNullOrBlank() -> parsedMsg
                    else -> "Login failed (${response.code})"
                }
                Result.failure(Exception(cleanMsg))
            }
        }
    } catch (e: Exception) {
        Log.e("LoginScreen", "Network exception reaching $loginUrl", e)
        Result.failure(Exception("Connection error: ${e.localizedMessage}"))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ReportingappTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateToSignUp = {})
    }
}
