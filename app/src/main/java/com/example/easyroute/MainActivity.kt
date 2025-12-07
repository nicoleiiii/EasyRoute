package com.example.easyroute

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import android.preference.PreferenceManager
import com.example.easyroute.ui.LandingScreenComposable
import com.example.easyroute.ui.LoginScreenComposable
import com.example.easyroute.ui.MapScreen
import com.example.easyroute.ui.RegisterScreenComposable
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load OSMdroid config (so MapScreen can use it)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Request permission if needed
        requestLocationPermissionsIfNeeded()

        // Read saved login state (default: not logged in)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        setContent {
            // navigation state: "landing", "register", "login", "map"
            var currentScreen by remember { mutableStateOf(if (isLoggedIn) "map" else "landing") }

            MaterialTheme {
                Surface {
                    when (currentScreen) {
                        "landing" -> LandingScreenComposable(
                            onLogin = { currentScreen = "login" },
                            onRegister = { currentScreen = "register" }
                        )

                        "register" -> RegisterScreenComposable(
                            onRegisterSuccess = {
                                // 🌟 FIX: Navigate to the login screen after successful registration.
                                // The user must now manually log in.
                                currentScreen = "login"
                            },
                            onLoginClick = { currentScreen = "login" }
                        )

                        "login" -> LoginScreenComposable(
                            onLoginSuccess = {
                                // save login state and show map
                                prefs.edit().putBoolean("isLoggedIn", true).apply()
                                currentScreen = "map"
                            },
                            onRegisterClick = { currentScreen = "register" }
                        )

                        "map" -> MapScreen(
                            onLogout = {
                                // clear login state and go to landing screen
                                prefs.edit().putBoolean("isLoggedIn", false).apply()
                                currentScreen = "landing"
                            }
                        )

                        else -> LandingScreenComposable(
                            onLogin = { currentScreen = "login" },
                            onRegister = { currentScreen = "register" }
                        )
                    }
                }
            }
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine != PackageManager.PERMISSION_GRANTED || coarse != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
