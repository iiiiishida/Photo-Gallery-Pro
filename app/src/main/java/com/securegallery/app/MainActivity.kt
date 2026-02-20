package com.securegallery.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.securegallery.app.ui.MainGallery
import com.securegallery.app.ui.pin.PinScreen
import com.securegallery.app.ui.theme.SecureGalleryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SecureGalleryApp
        val security = com.securegallery.app.data.SecurityManager(this)
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { }
            LaunchedEffect(Unit) {
                val permissions = when {
                    Build.VERSION.SDK_INT >= 34 -> arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                    Build.VERSION.SDK_INT >= 33 -> arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionLauncher.launch(permissions)
            }
            SecureGalleryTheme {
                Surface(Modifier.fillMaxSize()) {
                    val locked by security.isLocked.collectAsState(initial = true)
                    val pinSet by security.isPinSet.collectAsState(initial = false)
                    var pinError by remember { mutableStateOf<String?>(null) }

                    if (locked) {
                        PinScreen(
                            isSetup = !pinSet,
                            error = pinError,
                            onPinEntered = { pin ->
                                lifecycleScope.launch {
                                    if (pinSet) {
                                        if (security.unlock(pin)) pinError = null
                                        else pinError = "Wrong PIN"
                                    } else {
                                        security.setPin(pin)
                                        pinError = null
                                    }
                                }
                            },
                            onSkip = if (!pinSet) {
                                {
                                    lifecycleScope.launch { security.clearPin() }
                                }
                            } else null
                        )
                    } else {
                        MainGallery(
                            app = app,
                            security = security,
                            onLock = { lifecycleScope.launch { security.lock() } }
                        )
                    }
                }
            }
        }
    }
}
