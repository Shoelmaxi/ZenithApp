package com.example.zenithapp20.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.utils.BackupManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuBackupScreen(navController: NavController) {
    val context = LocalContext.current
    var backups by remember { mutableStateOf(BackupManager.obtenerBackupsDisponibles(context)) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var backupARestaurar by remember { mutableStateOf<File?>(null) }
    var showConfirmRestaurar by remember { mutableStateOf(false) }

    val compartirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    if (showConfirmRestaurar && backupARestaurar != null) {
        AlertDialog(
            onDismissRequest = { showConfirmRestaurar = false },
            containerColor = MainCardBackground,
            title = {
                Text("¿Restaurar backup?", color = Color.White, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Se reemplazarán todos los datos actuales con los del backup. Esta acción no se puede deshacer.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val exito = BackupManager.restaurarBackup(context, backupARestaurar!!)
                    if (exito) {
                        mensajeExito = "Backup restaurado. Reinicia la app para ver los cambios."
                    } else {
                        mensajeError = "Error al restaurar el backup."
                    }
                    showConfirmRestaurar = false
                    backupARestaurar = null
                }) {
                    Text("RESTAURAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestaurar = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text("BACKUP", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MENSAJES
        mensajeExito?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00C853).copy(0.1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C853).copy(0.3f))
            ) {
                Text(
                    it,
                    color = Color(0xFF00C853),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        mensajeError?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Red.copy(0.1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.3f))
            ) {
                Text(
                    it,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTONES DE ACCIÓN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // EXPORTAR
            Button(
                onClick = {
                    val exito = BackupManager.exportarBackup(context)
                    if (exito) {
                        mensajeExito = "Backup guardado en Zenith/Backup"
                        mensajeError = null
                        backups = BackupManager.obtenerBackupsDisponibles(context)
                    } else {
                        mensajeError = "Error al crear el backup"
                        mensajeExito = null
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("EXPORTAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // COMPARTIR
            Button(
                onClick = {
                    val intent = BackupManager.compartirBackup(context)
                    if (intent != null) {
                        compartirLauncher.launch(intent)
                    } else {
                        mensajeError = "Error al compartir el backup"
                        mensajeExito = null
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainCardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("COMPARTIR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("BACKUPS GUARDADOS", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (backups.isEmpty()) {
            Text("No hay backups guardados todavía", color = SecondaryText, fontSize = 14.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(backups) { archivo ->
                    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(archivo.lastModified()))
                    val tamaño = "%.1f KB".format(archivo.length() / 1024f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MainCardBackground, RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fecha, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(tamaño, color = SecondaryText, fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                backupARestaurar = archivo
                                showConfirmRestaurar = true
                            }
                        ) {
                            Text("RESTAURAR", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}