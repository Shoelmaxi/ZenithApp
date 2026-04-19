package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.TipoAgenda
import com.example.zenithapp20.ui.components.CardAgua
import com.example.zenithapp20.ui.components.formatCLP
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.AgendaViewModel
import com.example.zenithapp20.ui.viewmodel.AguaViewModel
import com.example.zenithapp20.ui.viewmodel.FinanzasViewModel
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel
import java.util.Calendar

@Composable
fun RimuSummaryScreen(
    navController: NavController,
    habitosViewModel: HabitosViewModel,
    agendaViewModel: AgendaViewModel,
    finanzasViewModel: FinanzasViewModel,
    aguaViewModel: AguaViewModel
) {
    val habitos by habitosViewModel.habitos.collectAsState()
    val agenda by agendaViewModel.todaLaAgenda.collectAsState()
    val balance by finanzasViewModel.balance.collectAsState()
    val totalMlAgua by aguaViewModel.totalMlHoy.collectAsState()

    // FIX: Use two separate Calendar instances so that computing 'hoy'
    // does not mutate 'ahora' (they previously shared the same object via apply{}).
    val hora = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val minuto = remember { Calendar.getInstance().get(Calendar.MINUTE) }
    val hoy = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val saludo = when {
        hora < 12 -> "Buenos días"
        hora < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    val habitosCompletados = habitos.count { habito ->
        habito.checks.any { it >= hoy && it < hoy + 86400000 }
    }
    val totalHabitos = habitos.size
    val progresoHabitos = if (totalHabitos > 0)
        habitosCompletados.toFloat() / totalHabitos.toFloat() else 0f

    val diasMap = mapOf(
        Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
        Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "D"
    )
    val diaKey = remember { diasMap[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)] ?: "L" }

    val horaActualStr = remember { "%02d:%02d".format(hora, minuto) }
    val proximoEvento = agenda.filter { item ->
        when (item.tipo) {
            TipoAgenda.RECURRENTE -> item.dias.contains(diaKey) && item.hora > horaActualStr
            TipoAgenda.FECHA_ESPECIFICA -> item.fechaEspecificaMillis?.let { fecha ->
                fecha >= hoy && fecha < hoy + 86400000 && item.hora > horaActualStr
            } ?: false
        }
    }.minByOrNull { it.hora }

    val mejorRacha = habitos.maxOfOrNull { it.rachaDias } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text("RESUMEN", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {

            // SALUDO
            item {
                Column {
                    Text(
                        saludo,
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        when {
                            totalHabitos == 0 -> "Empieza creando tus hábitos"
                            habitosCompletados == totalHabitos -> "¡Día perfecto! Todo completado 🎉"
                            habitosCompletados == 0 -> "Tienes $totalHabitos hábitos pendientes"
                            else -> "Llevas $habitosCompletados de $totalHabitos hábitos"
                        },
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // HÁBITOS PROGRESO
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MainCardBackground,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "HÁBITOS HOY",
                                color = SecondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$habitosCompletados/$totalHabitos",
                                color = if (habitosCompletados == totalHabitos && totalHabitos > 0)
                                    Color(0xFF00C853) else Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progresoHabitos },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = Color(0xFF00C853),
                            trackColor = Color.White.copy(0.05f)
                        )

                        if (mejorRacha > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "🔥 Mejor racha activa: $mejorRacha días",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val pendientes = habitos.filter { habito ->
                            habito.checks.none { it >= hoy && it < hoy + 86400000 }
                        }
                        if (pendientes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "PENDIENTES",
                                color = SecondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            pendientes.take(3).forEach { habito ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(habito.icono, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        habito.nombre,
                                        color = Color.White.copy(0.7f),
                                        fontSize = 13.sp
                                    )
                                    if (habito.rachaDias > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "🔥${habito.rachaDias}d",
                                            color = Color(0xFFFFD700),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            if (pendientes.size > 3) {
                                Text(
                                    "+${pendientes.size - 3} más",
                                    color = SecondaryText,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // AGUA
            item {
                CardAgua(
                    totalMl = totalMlAgua,
                    onAgregarVaso = { aguaViewModel.agregarVaso() },
                    onQuitarVaso = { aguaViewModel.quitarUltimoVaso() }
                )
            }

            // PRÓXIMO EVENTO
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MainCardBackground,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "PRÓXIMO EVENTO",
                            color = SecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (proximoEvento != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    proximoEvento.hora,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(56.dp)
                                )
                                Column {
                                    Text(
                                        proximoEvento.nombre,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (proximoEvento.descripcion.isNotBlank()) {
                                        Text(
                                            proximoEvento.descripcion,
                                            color = SecondaryText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Sin eventos pendientes hoy",
                                color = SecondaryText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // FINANZAS RÁPIDO
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MainCardBackground,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "BALANCE",
                                color = SecondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatCLP(balance),
                                color = if (balance >= 0) Color.White else Color.Red,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        TextButton(onClick = { navController.navigate("rimu_finance") }) {
                            Text("VER →", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}