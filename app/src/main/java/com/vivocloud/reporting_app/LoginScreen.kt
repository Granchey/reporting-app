package com.vivocloud.reporting_app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.vivocloud.reporting_app.ui.theme.DarkGreenAccent
import com.vivocloud.reporting_app.ui.theme.LightGreenBackground
import com.vivocloud.reporting_app.ui.theme.ReportingappTheme
import com.vivocloud.reporting_app.BuildConfig
import com.vivocloud.reporting_app.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

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
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_label)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkGreenAccent,
                    focusedLabelColor = DarkGreenAccent,
                    cursorColor = DarkGreenAccent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkGreenAccent,
                    focusedLabelColor = DarkGreenAccent,
                    cursorColor = DarkGreenAccent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Handle Login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreenAccent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(R.string.login_button), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(R.string.or_divider),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    coroutineScope.launch {
                        try {
                            val result = credentialManager.getCredential(
                                context = context,
                                request = request
                            )
                            val credential = result.credential
                            if (credential is GoogleIdTokenCredential) {
                                val idToken = credential.idToken
                                Toast.makeText(context, context.getString(R.string.google_sign_in_success), Toast.LENGTH_SHORT).show()
                                // TODO: Use the ID token to authenticate with your backend or Firebase
                            } else {
                                Toast.makeText(context, context.getString(R.string.unexpected_credential_error), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: GetCredentialException) {
                            Toast.makeText(context, context.getString(R.string.sign_in_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = GoogleIcon,
                        contentDescription = stringResource(R.string.google_logo_description),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.sign_in_with_google),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(text = stringResource(R.string.no_account_text), color = Color.Gray)
                Text(
                    text = stringResource(R.string.sign_up_text),
                    color = DarkGreenAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Handle Sign Up */ }
                )
            }
        }
    }
}

val GoogleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Red
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12.0f, 5.04f)
            curveTo(13.86f, 5.04f, 15.53f, 5.68f, 16.84f, 6.94f)
            lineTo(20.47f, 3.31f)
            curveTo(18.25f, 1.23f, 15.39f, 0.0f, 12.0f, 0.0f)
            curveTo(7.34f, 0.0f, 3.35f, 2.68f, 1.41f, 6.58f)
            lineTo(5.54f, 9.78f)
            curveTo(6.51f, 6.94f, 9.01f, 5.04f, 12.0f, 5.04f)
            close()
        }
        // Blue
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(23.49f, 12.27f)
            curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10.0f)
            horizontalLineTo(12.0f)
            verticalLineTo(14.51f)
            horizontalLineTo(18.47f)
            curveTo(18.18f, 15.99f, 17.34f, 17.25f, 16.08f, 18.1f)
            lineTo(20.21f, 21.3f)
            curveTo(22.61f, 19.08f, 24.0f, 15.82f, 24.0f, 12.0f)
            curveTo(24.0f, 12.09f, 24.0f, 12.18f, 23.49f, 12.27f)
            close()
        }
        // Yellow
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(5.54f, 14.22f)
            curveTo(5.3f, 13.51f, 5.17f, 12.77f, 5.17f, 12.0f)
            curveTo(5.17f, 11.23f, 5.3f, 10.49f, 5.54f, 9.78f)
            lineTo(1.41f, 6.58f)
            curveTo(0.51f, 8.21f, 0.0f, 10.05f, 0.0f, 12.0f)
            curveTo(0.0f, 13.95f, 0.51f, 15.79f, 1.41f, 17.42f)
            lineTo(5.54f, 14.22f)
            close()
        }
        // Green
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12.0f, 18.96f)
            curveTo(9.01f, 18.96f, 6.51f, 17.06f, 5.54f, 14.22f)
            lineTo(1.41f, 17.42f)
            curveTo(3.35f, 21.32f, 7.34f, 24.0f, 12.0f, 24.0f)
            curveTo(15.3f, 24.0f, 18.09f, 22.91f, 20.21f, 21.09f)
            lineTo(16.08f, 17.89f)
            curveTo(14.96f, 18.63f, 13.6f, 18.96f, 12.0f, 18.96f)
            close()
        }
    }.build()

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ReportingappTheme {
        LoginScreen()
    }
}
