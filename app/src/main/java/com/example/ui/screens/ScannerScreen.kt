package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel
import com.example.ui.viewmodel.ScanUiState
import java.io.InputStream

@Composable
fun ScannerScreen(
    viewModel: NutriViewModel,
    onScanSuccess: (foodId: Int) -> Unit
) {
    val context = LocalContext.current
    val languageCode by viewModel.language.collectAsState()
    val scanState by viewModel.scanUiState.collectAsState()

    // Animation rotating state for loader
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    // Standard Android Activity Results Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.scanImage(bitmap, it.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                viewModel.scanImage(bitmap, null)
            }
        }
    }

    // Capture success transitions
    LaunchedEffect(scanState) {
        if (scanState is ScanUiState.Success) {
            onScanSuccess((scanState as ScanUiState.Success).foodItem.id)
            viewModel.clearScanState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("scanner_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Localization.getString("scanner", languageCode),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "AI Botanical OCR & Vision Systems",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // AI Viewfinder frame card with animated laser sweep line
            val infiniteLaserTransition = rememberInfiniteTransition(label = "laser_anim")
            val laserFraction by infiniteLaserTransition.animateFloat(
                initialValue = 0.05f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser_fraction"
            )

            val primaryColor = MaterialTheme.colorScheme.primary

            Card(
                modifier = Modifier
                    .size(240.dp)
                    .clickable {
                        // Open simple selection sheet
                        val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                        galleryLauncher.launch("image/*")
                    }
                    .testTag("viewfinder_box"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Tech grid overlay drawing behind symbols
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridCount = 6
                        val widthGap = size.width / gridCount
                        val heightGap = size.height / gridCount
                        val lineAccentColor = primaryColor.copy(alpha = 0.08f)
                        for (i in 1 until gridCount) {
                            drawLine(
                                color = lineAccentColor,
                                start = androidx.compose.ui.geometry.Offset(i * widthGap, 0f),
                                end = androidx.compose.ui.geometry.Offset(i * widthGap, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = lineAccentColor,
                                start = androidx.compose.ui.geometry.Offset(0f, i * heightGap),
                                end = androidx.compose.ui.geometry.Offset(size.width, i * heightGap),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // 2. Animated scanner glowing laser guideline
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 240.dp * laserFraction)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 3. Viewfinder corner marks
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("┏", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            Text("┓", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("┗", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            Text("┛", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Localization.getString("scan_now", languageCode),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Camera and Gallery buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val capIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(capIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_take_photo"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_upload_gallery"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // AI Quick Demo Section (Extremely tactile shortcuts)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.getString("or_quick_demo", languageCode),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoShortcutItem(
                        name = "Apple",
                        emoji = "🍎",
                        bgColor = Color(0xFFFFCDD2),
                        modifier = Modifier.weight(1f)
                    ) {
                        val mockBitmap = createTextBitmap("Red Apple crop profile", Color.Red)
                        viewModel.scanImage(mockBitmap, "demo_apple")
                    }

                    DemoShortcutItem(
                        name = "Broccoli",
                        emoji = "🥦",
                        bgColor = Color(0xFFC8E6C9),
                        modifier = Modifier.weight(1f)
                    ) {
                        val mockBitmap = createTextBitmap("Green Broccoli crop profile", Color.Green)
                        viewModel.scanImage(mockBitmap, "demo_broccoli")
                    }

                    DemoShortcutItem(
                        name = "Almonds",
                        emoji = "🥜",
                        bgColor = Color(0xFFD7CCC8),
                        modifier = Modifier.weight(1f)
                    ) {
                        val mockBitmap = createTextBitmap("Raw Almonds crop profile", Color.Magenta)
                        viewModel.scanImage(mockBitmap, "demo_almonds")
                    }
                }
            }
        }

        // Beautiful shimmering loading modal
        if (scanState is ScanUiState.Scanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .rotate(rotationAngle)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = Localization.getString("loading_scan", languageCode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Analyzing pigments & proteins...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Scanning Error Toast Dialog
        if (scanState is ScanUiState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.clearScanState() },
                confirmButton = {
                    Button(onClick = { viewModel.clearScanState() }) {
                        Text("Dismiss")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = { Text("Scanner Issue", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        (scanState as ScanUiState.Error).message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
fun DemoShortcutItem(
    name: String,
    emoji: String,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
            .testTag("demo_crop_${name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, bgColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Generate a simple colored Bitmap to simulate a captured photo for scanning
private fun createTextBitmap(text: String, color: Color): Bitmap {
    val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color.hashCode()
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 120f, 120f, paint)
    return bitmap
}
