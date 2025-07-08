package com.heard.mobile.utils

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

enum class PermissionStatus {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied;

    val isGranted get() = this == Granted
    val isDenied get() = this == Denied || this == PermanentlyDenied
}

interface MultiplePermissionHandler {
    val statuses: Map<String, PermissionStatus>
    fun launchPermissionRequest()
}

@Composable
fun rememberMultiplePermissions(
    permissions: List<String>,
    onResult: (status: Map<String, PermissionStatus>) -> Unit
): MultiplePermissionHandler {
    val context = LocalContext.current

    var statuses by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED)
                    PermissionStatus.Granted
                else
                    PermissionStatus.Unknown
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { newPermissions ->
        val newStatuses = newPermissions.mapValues { (permission, isGranted) ->
            when {
                isGranted -> PermissionStatus.Granted
                else -> {
                    // Nota: shouldShowRequestPermissionRationale richiede Activity
                    // Per semplicità, assumiamo che sia negato se non concesso
                    PermissionStatus.Denied
                }
            }
        }
        statuses = statuses + newStatuses
        onResult(newStatuses)
    }

    val permissionHandler = remember(permissionLauncher) {
        object : MultiplePermissionHandler {
            override val statuses get() = statuses
            override fun launchPermissionRequest() =
                permissionLauncher.launch(permissions.toTypedArray())
        }
    }
    return permissionHandler
}