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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vivocloud.reporting_app.ui.theme.DarkGreenAccent
import com.vivocloud.reporting_app.ui.theme.LightGreenBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGreenBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.create_account_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreenAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.sign_up_subtitle),
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(R.string.first_name_label)) },
                    modifier = Modifier.weight(1f),
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

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(R.string.last_name_label)) },
                    modifier = Modifier.weight(1f),
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
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password_label)) },
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
                    if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val result = performRegister(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            password = password
                        )
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, R.string.signup_success, Toast.LENGTH_SHORT).show()
                            onSignUpSuccess()
                        } else {
                            val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
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
                        text = stringResource(R.string.sign_up_text),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = Color(0xFF49454F)
                )
                Text(
                    text = stringResource(R.string.login_text),
                    color = DarkGreenAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

private suspend fun performRegister(
    firstName: String,
    lastName: String,
    email: String,
    password: String
): Result<Unit> = withContext(Dispatchers.IO) {
    val client = com.vivocloud.reporting_app.api.RetrofitClient.okHttpClient
    val registerUrl = ApiEndpoints.registerUrl

    if (ApiEndpoints.baseUrl.isBlank()) {
        Log.e("SignUpScreen", "API URL is missing or blank in local.properties")
        return@withContext Result.failure(Exception("REPORTING_API_URL is missing in local.properties"))
    }

    Log.d("SignUpScreen", "Sending POST register request to: $registerUrl")

    val jsonBody = JSONObject().apply {
        put("firstName", firstName)
        put("lastName", lastName)
        put("email", email)
        put("password", password)
        put("role", "USER")
    }.toString()

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(registerUrl)
        .addHeader("ngrok-skip-browser-warning", "true")
        .addHeader("Accept", "application/json")
        .post(requestBody)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            Log.d("SignUpScreen", "Response code: ${response.code}")
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                AuthTokenManager.saveTokenFromResponse(bodyString)
                Result.success(Unit)
            } else {
                val errorMsg = response.body?.string() ?: "Error code ${response.code}"
                Log.e("SignUpScreen", "Registration failed code=${response.code}, body=$errorMsg")
                
                val parsedMsg = try {
                    if (errorMsg.isNotBlank()) {
                        JSONObject(errorMsg).optString("message", "").ifBlank { null }
                    } else null
                } catch (e: Exception) { null }

                Result.failure(Exception(parsedMsg ?: "Registration failed (${response.code})"))
            }
        }
    } catch (e: Exception) {
        Log.e("SignUpScreen", "Network error during registration to $registerUrl", e)
        Result.failure(Exception("Connection error: ${e.localizedMessage}"))
    }
}
