package com.example.easyroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import android.util.Patterns // Required for email validation

@Composable
fun RegisterScreenComposable(
    onRegisterSuccess: () -> Unit,   // called after successful registration
    onLoginClick: () -> Unit         // called if user wants to switch to login
) {
    // Define the required brand colors
    val greenAccent = Color(0xFF4CAF50)
    val yellowAccent = Color(0xFFFFC107)
    val softGrayBackground = Color(0xFFF7F7F7)
    val secondaryTextColor = Color(0xFF616161)

    // State for input fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 🌟 NEW: State for validation errors 🌟
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Function to validate all fields
    fun validateFields(): Boolean {
        var isValid = true

        // --- Username Validation ---
        usernameError = when {
            username.isBlank() -> "Username cannot be empty."
            username.length < 3 -> "Username must be at least 3 characters."
            else -> null
        }
        if (usernameError != null) isValid = false

        // --- Email Validation ---
        emailError = when {
            email.isBlank() -> "Email cannot be empty."
            // Simple regex check for basic format (e.g., contains @ and .)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
            else -> null
        }
        if (emailError != null) isValid = false

        // --- Password Validation ---
        passwordError = when {
            password.isBlank() -> "Password cannot be empty."
            password.length < 8 -> "Password must be at least 8 characters."
            else -> null
        }
        if (passwordError != null) isValid = false

        return isValid
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(softGrayBackground)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Input Fields Container - Consistent Spacing 16dp
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                // 1. Username Field
                CustomTextField(
                    value = username,
                    onValueChange = { username = it; usernameError = null }, // Clear error on change
                    placeholder = "Username",
                    yellowAccent = yellowAccent,
                    errorText = usernameError // Pass the error state
                )

                // 2. Email Field
                CustomTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null }, // Clear error on change
                    placeholder = "Email Address",
                    yellowAccent = yellowAccent,
                    errorText = emailError // Pass the error state
                )

                // 3. Password Field
                CustomTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null }, // Clear error on change
                    placeholder = "Password",
                    yellowAccent = yellowAccent,
                    isPassword = true,
                    errorText = passwordError // Pass the error state
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    // 🌟 MODIFIED: Check validation before proceeding
                    if (validateFields()) {
                        onRegisterSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp), // Drop shadow
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Register",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secondary Action: Login Link
            Text(
                text = "Already have an account? Login.",
                modifier = Modifier.clickable { onLoginClick() },
                color = secondaryTextColor,
                fontSize = 14.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline)
            )
        }
    }
}

/**
 * Helper Composable for styled TextField consistent with design requirements.
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    yellowAccent: Color,
    isPassword: Boolean = false,
    errorText: String? = null // NEW PARAMETER
) {
    // Determine if an error exists
    val isError = errorText != null

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(placeholder) },
            placeholder = { Text(placeholder, color = Color.LightGray) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            isError = isError, // Set isError property
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = yellowAccent,
                unfocusedBorderColor = if (isError) Color.Red else Color.LightGray.copy(alpha = 0.5f), // Red border on error
                errorBorderColor = Color.Red, // Explicit error border color
                focusedLabelColor = yellowAccent,
                cursorColor = yellowAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                // 🌟 MODIFIED: Set elevation to 0.dp to remove the thick shadow
                .shadow(
                    elevation = 0.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, RoundedCornerShape(12.dp))
        )

        // Display the error message below the field
        if (isError) {
            Text(
                text = errorText ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}