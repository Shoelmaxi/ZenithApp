package com.example.zenithapp20.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.zenithapp20.MainActivity
import com.example.zenithapp20.R
import kotlinx.coroutines.delay

class BlackoutOverlayService : Service() {

    companion object {
        const val CHANNEL_ID          = "zenith_blackout"
        const val NOTIF_ID            = 9003
        const val FRASE_DESBLOQUEO    = "Mi enfoque es mi poder"
        private const val ACTION_STOP = "ACTION_STOP_BLACKOUT"

        fun iniciar(context: Context) {
            context.startForegroundService(Intent(context, BlackoutOverlayService::class.java))
        }

        fun detener(context: Context) {
            context.startService(
                Intent(context, BlackoutOverlayService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }

        fun crearCanal(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Modo Blackout",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description      = "Protocolo de sueño activo"
                setShowBadge(false)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView?         = null
    private val lifecycleOwner = ServiceLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        lifecycleOwner.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification())
        showOverlay()
        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.OPAQUE
        )

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setContent {
                BlackoutContent(
                    onUnlock = { detener(applicationContext) }
                )
            }
        }

        overlayView = composeView
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()
        windowManager.addView(composeView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                lifecycleOwner.onPause()
                lifecycleOwner.onStop()
                windowManager.removeView(it)
            } catch (_: Exception) { }
            overlayView = null
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🌙 Protocolo de Sueño Activo")
            .setContentText("Modo Blackout activado — descansa")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeOverlay()
        lifecycleOwner.onDestroy()
        super.onDestroy()
    }
}

// ── Lifecycle owner mínimo para usar Compose dentro de un Service ─────────────

class ServiceLifecycleOwner :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val ssrc              = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle             = lifecycleRegistry
    override val viewModelStore: ViewModelStore   = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry = ssrc.savedStateRegistry

    fun onCreate() {
        ssrc.performAttach()
        ssrc.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }
    fun onStart()   = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume()  = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onPause()   = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onStop()    = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
}

// ── UI del Blackout ───────────────────────────────────────────────────────────

@Composable
private fun BlackoutContent(onUnlock: () -> Unit) {

    var fraseInput   by remember { mutableStateOf("") }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding    by remember { mutableStateOf(false) }
    var modo         by remember { mutableStateOf(ModoDesbloqueo.FRASE) }
    var errorFrase   by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val start = System.currentTimeMillis()
            while (isHolding) {
                val elapsed = System.currentTimeMillis() - start
                holdProgress = (elapsed / 5000f).coerceIn(0f, 1f)
                if (holdProgress >= 1f) { onUnlock(); break }
                delay(16L)
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier         = Modifier.fillMaxSize().background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
        ) {

            // ── Cabecera ──────────────────────────────────────────────────────
            Text("🌙", fontSize = 56.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                "PROTOCOLO DE SUEÑO ACTIVO",
                color         = Color.White.copy(0.35f),
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "El progreso de mañana\nse construye hoy.",
                color     = Color.White,
                fontSize  = 26.sp,
                fontWeight = FontWeight.Light,
                textAlign  = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(56.dp))

            // ── Selector de modo ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(0.06f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                ModoDesbloqueo.entries.forEach { m ->
                    val selected = modo == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) Color.White.copy(0.12f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { modo = m; errorFrase = false; fraseInput = "" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            m.label,
                            color      = if (selected) Color.White else Color.White.copy(0.35f),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Contenido según modo ──────────────────────────────────────────
            when (modo) {

                ModoDesbloqueo.FRASE -> {
                    OutlinedTextField(
                        value         = fraseInput,
                        onValueChange = { fraseInput = it; errorFrase = false },
                        placeholder   = {
                            Text(
                                "\"${BlackoutOverlayService.FRASE_DESBLOQUEO}\"",
                                color    = Color.White.copy(0.2f),
                                fontSize = 12.sp
                            )
                        },
                        isError  = errorFrase,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color.White.copy(0.5f),
                            unfocusedBorderColor = Color.White.copy(0.15f),
                            focusedTextColor     = Color.White,
                            unfocusedTextColor   = Color.White,
                            cursorColor          = Color.White,
                            errorBorderColor     = Color(0xFFFF4444).copy(0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (errorFrase) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Escribe la frase exactamente para continuar.",
                            color     = Color(0xFFFF4444).copy(0.8f),
                            fontSize  = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick  = {
                            if (fraseInput.trim().equals(
                                    BlackoutOverlayService.FRASE_DESBLOQUEO, ignoreCase = true
                                )
                            ) onUnlock() else errorFrase = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Text("DESBLOQUEAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                ModoDesbloqueo.MANTENER -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (holdProgress > 0f) {
                            CircularProgressIndicator(
                                progress  = { holdProgress },
                                modifier  = Modifier.size(80.dp),
                                color     = Color.White,
                                trackColor = Color.White.copy(0.08f),
                                strokeWidth = 6.dp
                            )
                            Spacer(Modifier.height(12.dp))
                            val secsLeft = ((1f - holdProgress) * 5).toInt() + 1
                            Text(
                                "${secsLeft}s",
                                color      = Color.White,
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isHolding = true
                                            tryAwaitRelease()
                                            isHolding = false
                                        }
                                    )
                                },
                            color = if (isHolding) Color.White.copy(0.15f)
                            else Color.White.copy(0.07f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isHolding) "MANTENIENDO..." else "MANTÉN PRESIONADO 5s",
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
            Text(
                "¿Vale más ese scroll que tu descanso?",
                color     = Color.White.copy(0.15f),
                fontSize  = 12.sp,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

private enum class ModoDesbloqueo(val label: String) {
    FRASE("Escribir frase"),
    MANTENER("Mantener 5s")
}