package com.example.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PermissionInfo(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit
) {
    val permissions = listOf(
        PermissionInfo(
            icon = Icons.Filled.Mic,
            title = "Microphone",
            description = "Record audio for voice notes and transcription",
            color = Color(0xFF2196F3)
        ),
        PermissionInfo(
            icon = Icons.Filled.CameraAlt,
            title = "Camera",
            description = "Capture photos and scan documents",
            color = Color(0xFF4CAF50)
        ),
        PermissionInfo(
            icon = Icons.Filled.Storage,
            title = "Storage",
            description = "Save and access your notes, images, and documents",
            color = Color(0xFFFF9800)
        ),
        PermissionInfo(
            icon = Icons.Filled.Vibration,
            title = "Vibration",
            description = "Provide haptic feedback for better user experience",
            color = Color(0xFF9C27B0)
        )
    )

    // Full screen overlay - no Dialog wrapper
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Permissions Required",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Logion needs these permissions to work properly",
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
            }

            // Permissions list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                permissions.forEach { permission ->
                    PermissionItem(permission = permission)
                }
            }

            // Privacy note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.PrivacyTip,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Privacy Protected",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            "Your data is processed securely and never stored permanently without your consent.",
                            color = Color(0xFFB0B0B0),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons in column layout - Full width
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onGrantPermissions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Grant Permissions", 
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF444444))
                ) {
                    Text(
                        "Not Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(permission: PermissionInfo) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = permission.color.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    permission.icon,
                    contentDescription = null,
                    tint = permission.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                permission.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                permission.description,
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                lineHeight = 20.sp
            )
        }
    }
}
