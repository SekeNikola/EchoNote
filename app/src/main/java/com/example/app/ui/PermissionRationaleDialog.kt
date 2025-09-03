package com.example.app.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

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
            title = "Microphone Access",
            description = "Record voice notes and audio for transcription and AI processing",
            color = Color(0xFF4CAF50)
        ),
        PermissionInfo(
            icon = Icons.Filled.PhotoCamera,
            title = "Camera Access", 
            description = "Take photos for OCR text extraction and visual content processing",
            color = Color(0xFF2196F3)
        ),
        PermissionInfo(
            icon = Icons.Filled.Folder,
            title = "Storage Access",
            description = "Access your files, images, audio, and documents for upload and processing",
            color = Color(0xFFFF9800)
        ),
        PermissionInfo(
            icon = Icons.Filled.Vibration,
            title = "Vibration",
            description = "Provide haptic feedback for better user experience",
            color = Color(0xFF9C27B0)
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Permissions Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            "EchoNote needs these permissions to work properly",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Permissions list
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, false)
                ) {
                    permissions.forEach { permission ->
                        PermissionItem(permission = permission)
                        if (permission != permissions.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Privacy note
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.PrivacyTip,
                            contentDescription = null,
                            tint = Color(0xFF7B1FA2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Privacy Protected",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7B1FA2),
                                fontSize = 14.sp
                            )
                            Text(
                                "Your data is processed securely and never stored permanently without your consent.",
                                color = Color(0xFF7B1FA2),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Not Now")
                    }
                    Button(
                        onClick = onGrantPermissions,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Grant Permissions", color = Color.White)
                    }
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
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                permission.description,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                lineHeight = 20.sp
            )
        }
    }
}
