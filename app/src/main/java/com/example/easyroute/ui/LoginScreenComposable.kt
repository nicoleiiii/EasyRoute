package com.example.easyroute.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import android.util.Patterns

@Composable
fun LoginScreenComposable(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Define the required brand colors
    val greenAccent = Color(0xFF4CAF50)
    val yellowAccent = Color(0xFFFFC107)
    val softGrayBackground = Color(0xFFF7F7F7)
    val secondaryTextColor = Color(0xFF616161)

    // State for input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 🌟 NEW: State for validation errors 🌟
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Function to validate fields
    fun validateFields(): Boolean {
        var isValid = true

        // --- Email Validation ---
        emailError = when {
            email.isBlank() -> "Email cannot be empty."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
            else -> null
        }
        if (emailError != null) isValid = false

        // --- Password Validation ---
        // For login, we only check if it's blank/empty (password complexity is checked during registration)
        passwordError = when {
            password.isBlank() -> "Password cannot be empty."
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
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Input Fields Container
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                // 1. Email Field
                CustomTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null }, // Clear error on change
                    placeholder = "Email Address",
                    yellowAccent = yellowAccent,
                    errorText = emailError // Pass the error state
                )

                // 2. Password Field
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

            // Login Button
            Button(
                onClick = {
                    // 🌟 MODIFIED: Check validation before calling onLoginSuccess
                    if (validateFields()) {
                        // In a real app, you would make an API call here.
                        // Assuming API call is successful:
                        onLoginSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secondary Action: Register Link
            Text(
                text = "Don't have an account? Register.",
                modifier = Modifier.clickable { onRegisterClick() },
                color = secondaryTextColor,
                fontSize = 14.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline)
            )
        }
    }
}

// NOTE: The CustomTextField composable is assumed to be defined elsewhere in your ui package
// and should be the same version used in the RegisterScreenComposable.}