package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingWindowService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    private var simulationJob: Job? = null

    // Theme definitions (Black & Neon green/blue)
    private val DarkBg = Color(0xFF070709)
    private val NeonGreen = Color(0xFF00FF66)
    private val NeonBlue = Color(0xFF00E5FF)
    private val GrayBorder = Color(0xFF1F1F24)
    private val MutedText = Color(0xFF8E8E93)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create persistent notification for Android Foreground Service compliance (Oreo+)
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        setupFloatingView()
        startSimulationLoop()
        FloatingPanelState.setServiceRunning(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Panel Active Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Floating Panel Overlay")
            .setContentText("The interactive overlay utility is currently running.")
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setOngoing(true)
            .build()
    }

    private fun setupFloatingView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@FloatingWindowService)
            setViewTreeViewModelStoreOwner(this@FloatingWindowService)
            setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)
            setContent {
                FloatingWindowUI()
            }
        }

        windowManager.addView(composeView, params)
    }

    @Composable
    fun FloatingWindowUI() {
        val isExpanded by FloatingPanelState.isExpanded.collectAsState()
        val feature1 by FloatingPanelState.feature1Enabled.collectAsState()
        val feature2 by FloatingPanelState.feature2Enabled.collectAsState()
        val feature3 by FloatingPanelState.feature3Enabled.collectAsState()
        val sensitivity by FloatingPanelState.sensitivity.collectAsState()
        val currentLogs by FloatingPanelState.logs.collectAsState()

        // Local state for tracking touch motion cleanly
        var dragOffsetStartX by remember { mutableStateOf(0f) }
        var dragOffsetStartY by remember { mutableStateOf(0f) }

        val modifierWithDrag = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    dragOffsetStartX = params.x.toFloat()
                    dragOffsetStartY = params.y.toFloat()
                },
                onDrag = { change, dragAmount ->
                    dragOffsetStartX += dragAmount.x
                    dragOffsetStartY += dragAmount.y
                    params.x = dragOffsetStartX.toInt()
                    params.y = dragOffsetStartY.toInt()
                    windowManager.updateViewLayout(composeView, params)
                }
            )
        }

        if (!isExpanded) {
            // Minimized Floating Orb
            Box(
                modifier = modifierWithDrag
                    .size(60.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF101015), DarkBg)
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, NeonBlue, CircleShape)
                    .clickable { FloatingPanelState.setExpanded(true) },
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                val scaleFactor by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Expand Floating Menu",
                    tint = NeonGreen,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(scaleFactor)
                        .rotate(rotation)
                )

                // Indication lights
                if (feature1 || feature2 || feature3) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NeonGreen, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset((-2).dp, 2.dp)
                    )
                }
            }
        } else {
            // Expanded System Command Center Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBg),
                modifier = Modifier
                    .width(300.dp)
                    .wrapContentHeight()
                    .border(1.dp, NeonBlue, RoundedCornerShape(16.dp))
                    .padding(1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Title Bar / Drag Handler
                    Row(
                        modifier = modifierWithDrag
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF151520), DarkBg)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Drag icon",
                                tint = NeonBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM X: PANEL",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Close/Minimize icon (shrinks to Orb)
                        IconButton(
                            onClick = { FloatingPanelState.setExpanded(false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Minimize Menu",
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Divider(color = GrayBorder, thickness = 1.dp)

                    // Options list
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Switch 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Target",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Automated coordinate alignment",
                                    color = MutedText,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = feature1,
                                onCheckedChange = { FloatingPanelState.setFeature1(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = NeonGreen,
                                    uncheckedThumbColor = MutedText,
                                    uncheckedTrackColor = Color(0xFF1C1C22)
                                )
                            )
                        }

                        // Switch 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Macro Clicker",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Simulated touch engine",
                                    color = MutedText,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = feature2,
                                onCheckedChange = { FloatingPanelState.setFeature2(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = NeonGreen,
                                    uncheckedThumbColor = MutedText,
                                    uncheckedTrackColor = Color(0xFF1C1C22)
                                )
                            )
                        }

                        // Switch 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Radar Sync",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "High-speed latency feedback",
                                    color = MutedText,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = feature3,
                                onCheckedChange = { FloatingPanelState.setFeature3(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = NeonGreen,
                                    uncheckedThumbColor = MutedText,
                                    uncheckedTrackColor = Color(0xFF1C1C22)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Sensitivity SeekBar/Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Target Sensitivity",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${sensitivity.toInt()}%",
                                    color = NeonBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = sensitivity,
                                onValueChange = { FloatingPanelState.setSensitivity(it) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonBlue,
                                    activeTrackColor = NeonBlue,
                                    inactiveTrackColor = Color(0xFF1C1C22)
                                )
                            )
                        }

                        // Inline status log ticker for real-time validation inside overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFF0F0F12), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF181822), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val activeLog = currentLogs.firstOrNull() ?: "[Awaiting signals]"
                            Text(
                                text = "LATEST LOG:\n$activeLog",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Fully close overlay service
                        Button(
                            onClick = { stopSelf() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF321215)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "TERMINATE SERVICE",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = lifecycleScope.launch {
            while (true) {
                delay(2500)
                if (FloatingPanelState.feature1Enabled.value) {
                    val angle = (0..359).random()
                    FloatingPanelState.addLog("Overlay Auto-Targeted vector at theta=$angle°")
                }
                if (FloatingPanelState.feature2Enabled.value) {
                    val rx = (100..900).random()
                    val ry = (200..1600).random()
                    val clickInterval = (110 - FloatingPanelState.sensitivity.value.toInt()) * 15
                    FloatingPanelState.addLog("Executing action click @ ($rx, $ry) | Cooldown: ${clickInterval}ms")
                    
                    // Dispatch real tap if accessibility service is active
                    FloatingAccessibilityService.instance?.dispatchTap(rx.toFloat(), ry.toFloat())
                }
                if (FloatingPanelState.feature3Enabled.value) {
                    val ping = (5..35).random()
                    FloatingPanelState.addLog("Radar sync payload validated in $ping ms.")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingPanelState.setServiceRunning(false)
        simulationJob?.cancel()
        if (::composeView.isInitialized) {
            try {
                windowManager.removeView(composeView)
            } catch (e: Exception) {
                // Ignore if view was already removed
            }
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _viewModelStore.clear()
    }

    companion object {
        private const val NOTIFICATION_ID = 404
        private const val CHANNEL_ID = "floating_panel_channel"
    }
}
