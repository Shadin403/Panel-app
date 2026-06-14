package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    DashboardScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe permission states with direct automatic re-checks when returning onResume
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityEnabled by remember { mutableStateOf(FloatingAccessibilityService.isServiceRunning) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasAccessibilityEnabled = FloatingAccessibilityService.isServiceRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Floating state bindings
    val isServiceRunning by FloatingPanelState.isServiceRunning.collectAsState()
    val isExpanded by FloatingPanelState.isExpanded.collectAsState()
    val feature1 by FloatingPanelState.feature1Enabled.collectAsState()
    val feature2 by FloatingPanelState.feature2Enabled.collectAsState()
    val feature3 by FloatingPanelState.feature3Enabled.collectAsState()
    val sensitivity by FloatingPanelState.sensitivity.collectAsState()
    val logs by FloatingPanelState.logs.collectAsState()

    // Cyber Colors
    val DarkCanvas = Color(0xFF030304)
    val MetallicCard = Color(0xFF0C0C0F)
    val HighTechNeonActive = Color(0xFF00FF66)
    val UltraNeonBlue = Color(0xFF00E5FF)
    val CyberCrimson = Color(0xFFFF2E56)
    val CyberGrey = Color(0xFF16161D)
    val TextGrey = Color(0xFF9E9EA4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(16.dp)
    ) {
        // Futuristic Cyber Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "CYBER PANEL SYS",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isServiceRunning) HighTechNeonActive else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServiceRunning) "OVERLAY ACTIVE" else "SERVICE OFFLINE",
                        color = if (isServiceRunning) HighTechNeonActive else CyberCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Power button icon matching the layout status
            IconButton(
                onClick = {
                    val serviceIntent = Intent(context, FloatingWindowService::class.java)
                    if (isServiceRunning) {
                        context.stopService(serviceIntent)
                    } else {
                        if (Settings.canDrawOverlays(context)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        } else {
                            Toast.makeText(context, "System Alert Overlay Permission required!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 1.dp,
                        color = if (isServiceRunning) HighTechNeonActive else UltraNeonBlue,
                        shape = CircleShape
                    )
                    .background(Color.Black, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Toggle Service Power",
                    tint = if (isServiceRunning) HighTechNeonActive else UltraNeonBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Divider(color = CyberGrey, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // PERMISSIONS CONTROLLERS
            item {
                Text(
                    text = "SYS ACCESS CONTROLS",
                    color = UltraNeonBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Overlay Permission card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!hasOverlayPermission) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Overlay permission is already authorized.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .border(1.dp, if (hasOverlayPermission) Color(0xFF152A1C) else Color(0xFF3B151F), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MetallicCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (hasOverlayPermission) Color(0xFF102517) else Color(0xFF301016),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasOverlayPermission) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasOverlayPermission) HighTechNeonActive else CyberCrimson
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Alert Overlay Permission",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Required to draw the control panel widget over other applications.",
                                color = TextGrey,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (hasOverlayPermission) "GRANTED" else "PENDING",
                            color = if (hasOverlayPermission) HighTechNeonActive else CyberCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accessibility Permission card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                            Toast.makeText(context, "Find 'Floating Tap Overlay Controller' in settings and enable it.", Toast.LENGTH_LONG).show()
                        }
                        .border(1.dp, if (hasAccessibilityEnabled) Color(0xFF152A1C) else Color(0xFF22222E), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MetallicCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (hasAccessibilityEnabled) Color(0xFF102517) else Color(0xFF1A1A24),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = if (hasAccessibilityEnabled) HighTechNeonActive else UltraNeonBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gesture Injection Service",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Required to emulate automated click script coordinates on screen (Dhap 4).",
                                color = TextGrey,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (hasAccessibilityEnabled) "ACTIVE" else "OPTIONAL",
                            color = if (hasAccessibilityEnabled) HighTechNeonActive else UltraNeonBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // FLOATING PANEL IN-APP INTERACTIVE PREVIEW
            item {
                Text(
                    text = "FLOATING PANEL WORK BENCH (SIMULATOR)",
                    color = UltraNeonBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF070709), RoundedCornerShape(16.dp))
                        .border(1.dp, CyberGrey, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isExpanded) {
                        // In-app preview of floating orb
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable { FloatingPanelState.setExpanded(true) }
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "innerPulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutCirc),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(pulseScale)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color(0xFF101015), Color.Black)
                                        ),
                                        CircleShape
                                    )
                                    .border(2.dp, UltraNeonBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Expand Simulator Menu",
                                    tint = HighTechNeonActive,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "TAP TO EXPAND INTERACTIVE MENU",
                                color = UltraNeonBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Simulated real-time state binds securely inside the workbench",
                                color = TextGrey,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        // Expanded Workbench Panel UI
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MetallicCard),
                            modifier = Modifier
                                .width(280.dp)
                                .wrapContentHeight()
                                .border(1.dp, UltraNeonBlue, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                // Mini Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF15151C))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = UltraNeonBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SIMULATOR PANEL v1",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { FloatingPanelState.setExpanded(false) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Collapse simulator",
                                            tint = TextGrey,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Divider(color = CyberGrey)

                                // Switches
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Simulated Feature 1
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Feature 1: Auto-Target",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Switch(
                                            checked = feature1,
                                            onCheckedChange = { FloatingPanelState.setFeature1(it) },
                                            modifier = Modifier.scale(0.8f)
                                        )
                                    }

                                    // Simulated Feature 2
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Feature 2: Macro Clicker",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Switch(
                                            checked = feature2,
                                            onCheckedChange = { FloatingPanelState.setFeature2(it) },
                                            modifier = Modifier.scale(0.8f)
                                        )
                                    }

                                    // Simulated Feature 3
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Feature 3: Radar Sync",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Switch(
                                            checked = feature3,
                                            onCheckedChange = { FloatingPanelState.setFeature3(it) },
                                            modifier = Modifier.scale(0.8f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Sensitivity Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Sensitivity Delay",
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "${sensitivity.toInt()}%",
                                                color = UltraNeonBlue,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Slider(
                                            value = sensitivity,
                                            onValueChange = { FloatingPanelState.setSensitivity(it) },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = UltraNeonBlue,
                                                activeTrackColor = UltraNeonBlue,
                                                inactiveTrackColor = CyberGrey
                                            ),
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TELEMETRY LIVE TERMINAL SECTOR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYSTEM TELEMETRY RAW LOGS",
                        color = UltraNeonBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = { FloatingPanelState.clearLogs() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "CLEAR TAPE",
                            color = CyberCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Feed display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF060608), RoundedCornerShape(12.dp))
                        .border(1.dp, CyberGrey, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Error") || log.contains("STOPPED")) CyberCrimson else HighTechNeonActive,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
