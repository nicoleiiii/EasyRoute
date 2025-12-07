package com.example.easyroute.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyroute.R

@Composable
fun LandingScreenComposable(onLogin: () -> Unit, onRegister: () -> Unit) {
    // Define the required brand colors
    val greenAccent = Color(0xFF4CAF50) // #4CAF50
    val yellowAccent = Color(0xFFFFC107) // #FFC107
    val softGrayBackground = Color(0xFFF0F0F0) // Soft gray/off-white background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(softGrayBackground) // Use the soft neutral background
            .padding(horizontal = 32.dp, vertical = 64.dp), // Add general padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Generous spacing between logo and buttons
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            // Logo (Assume R.drawable.ic_jeep_logo is the correct drawable for the logo)
            // Added shadow for emphasis
            Image(
                painter = painterResource(id = R.drawable.ic_map_logo),
                contentDescription = "Jeep App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(8.dp)) // Subtle shadow
            )

            // Optional Tagline (Friendly, modern small text)
            Text(
                text = "Easy Route",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp)) // Additional space before buttons

            // Buttons Container for spacing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp) // 16-24dp margin between buttons
            ) {
                // Register Button (Green Accent)
                Button(
                    onClick = onRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = greenAccent),
                    shape = RoundedCornerShape(16.dp), // Rounded corners
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp), // Drop shadow
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // Increased width for better prominence
                        .height(56.dp) // Slightly thick for tappability
                ) {
                    Text(
                        text = "Register",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, // Bold, sans-serif typography
                        color = Color.White
                    )
                }

                // Login Button (Yellow Accent)
                Button(
                    onClick = onLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = yellowAccent),
                    shape = RoundedCornerShape(16.dp), // Rounded corners
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp), // Drop shadow
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, // Bold, sans-serif typography
                        color = Color.Black // Use a contrasting color for yellow
                    )
                }
            }
        }
    }
}