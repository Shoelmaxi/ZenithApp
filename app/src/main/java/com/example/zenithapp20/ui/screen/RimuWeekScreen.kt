package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zenithapp20.data.model.AgendaItem
import com.example.zenithapp20.data.model.Habito
import com.example.zenithapp20.data.model.TipoAgenda
import com.example.zenithapp20.ui.theme.*
import com.example.zenithapp20.ui.viewmodel.AgendaViewModel
import com.example.zenithapp20.ui.viewmodel.HabitosViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RimuWeekScreen(
    navController: NavController,
    agendaViewModel: AgendaViewModel,
    habitosViewModel: HabitosViewModel
) {
    val listaAgenda by agendaViewModel.todaLaAgenda.collectAsState()
    val listaHabitos by habitosViewModel.habitos.collectAsState()

    // Semana actual — de lunes a domingo
    val diasSemana = remember { obtenerDiasSemana() }
    var diaSeleccionado by remember { mutableStateOf(diasSemana.first { it.esHoy }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text(
                "SEMANA",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SELECTOR DE DÍAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            diasSemana.forEach { dia ->
                val isSelected = dia == diaSeleccionado
                val habitosCompletadosHoy = listaHabitos.count { habito ->
                    habito.checks.any { check ->
                        check >= dia.inicioMillis && check < dia.inicioMillis + 86400000
                    }
                }
                val totalHabitos = listaHabitos.size
                val eventosDia = filtrarAgendaPorDia(listaAgenda, dia)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { diaSeleccionado = dia }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dia.nombreCorto,
                        color = if (isSelected) Color.White else SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                when {
                                    isSelected -> Color.White
                                    dia.esHoy -> Color.White.copy(0.1f)
                                    else -> Color.Transparent
                                },
                                CircleShape
                            )
                            .border(
                                1.dp,
                                when {
                                    isSelected -> Color.White
                                    dia.esHoy -> Color.White.copy(0.3f)
                                    else -> Color.Transparent
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dia.numeroDia,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // puntos indicadores de eventos y hábitos
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (eventosDia.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        if (totalHabitos > 0 && habitosCompletadosHoy == totalHabitos) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFFFFD700), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = CardBorderColor)
        Spacer(modifier = Modifier.height(16.dp))

        // CONTENIDO DEL DÍA SELECCIONADO
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // EVENTOS DE AGENDA
            val eventosDia = filtrarAgendaPorDia(listaAgenda, diaSeleccionado)

            item {
                SemanaSeccionHeader(
                    titulo = "AGENDA",
                    cantidad = eventosDia.size
                )
            }

            if (eventosDia.isEmpty()) {
                item {
                    Text(
                        "Sin eventos este día",
                        color = SecondaryText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(eventosDia) { evento ->
                    EventoSemanaItem(evento = evento, diaKey = diaSeleccionado.diaKey)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // HÁBITOS
            item {
                SemanaSeccionHeader(
                    titulo = "HÁBITOS",
                    cantidad = listaHabitos.size
                )
            }

            if (listaHabitos.isEmpty()) {
                item {
                    Text(
                        "Sin hábitos creados",
                        color = SecondaryText,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(listaHabitos) { habito ->
                    HabitoSemanaItem(
                        habito = habito,
                        iniciodia = diaSeleccionado.inicioMillis
                    )
                }
            }
        }
    }
}

// --- COMPONENTES INTERNOS ---

@Composable
fun SemanaSeccionHeader(titulo: String, cantidad: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(titulo, color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .background(Color.White.copy(0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(cantidad.toString(), color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EventoSemanaItem(evento: AgendaItem, diaKey: String) {
    val completado = evento.diasCompletados.contains(diaKey)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (completado) Color(0xFF4CAF50).copy(0.5f) else CardBorderColor,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(evento.hora, color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (evento.tipo == TipoAgenda.FECHA_ESPECIFICA) {
                Text("📅", fontSize = 10.sp)
            } else {
                Text("🔁", fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                evento.nombre,
                color = if (completado) Color.Gray else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (evento.descripcion.isNotBlank()) {
                Text(evento.descripcion, color = SecondaryText, fontSize = 12.sp)
            }
        }

        if (completado) {
            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun HabitoSemanaItem(habito: Habito, iniciodia: Long) {
    val completado = habito.checks.any { it >= iniciodia && it < iniciodia + 86400000 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MainCardBackground, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (completado) Color(0xFF00C853).copy(0.5f) else CardBorderColor,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(habito.icono, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    habito.nombre,
                    color = if (completado) Color.Gray else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(habito.meta, color = SecondaryText, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (completado) Color(0xFF00C853) else Color.Transparent,
                    CircleShape
                )
                .border(1.dp, if (completado) Color(0xFF00C853) else Color.Gray.copy(0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completado) {
                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// --- MODELOS Y HELPERS ---

data class DiaSemana(
    val nombreCorto: String,
    val numeroDia: String,
    val diaKey: String,
    val inicioMillis: Long,
    val esHoy: Boolean
)

fun obtenerDiasSemana(): List<DiaSemana> {
    val hoy = Calendar.getInstance()
    val lunes = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            add(Calendar.DAY_OF_YEAR, -6)
        } else {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    val nombresDias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val keysMap = mapOf(
        Calendar.MONDAY to "L", Calendar.TUESDAY to "M", Calendar.WEDNESDAY to "X",
        Calendar.THURSDAY to "J", Calendar.FRIDAY to "V", Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "D"
    )

    return (0..6).map { i ->
        val cal = lunes.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, i)
        val esHoy = cal.get(Calendar.DAY_OF_MONTH) == hoy.get(Calendar.DAY_OF_MONTH) &&
                cal.get(Calendar.MONTH) == hoy.get(Calendar.MONTH)

        DiaSemana(
            nombreCorto = nombresDias[i],
            numeroDia = cal.get(Calendar.DAY_OF_MONTH).toString(),
            diaKey = keysMap[cal.get(Calendar.DAY_OF_WEEK)] ?: "L",
            inicioMillis = cal.timeInMillis,
            esHoy = esHoy
        )
    }
}

fun filtrarAgendaPorDia(agenda: List<AgendaItem>, dia: DiaSemana): List<AgendaItem> {
    return agenda.filter { item ->
        when (item.tipo) {
            TipoAgenda.RECURRENTE -> item.dias.contains(dia.diaKey)
            TipoAgenda.FECHA_ESPECIFICA -> {
                item.fechaEspecificaMillis?.let { fecha ->
                    fecha >= dia.inicioMillis && fecha < dia.inicioMillis + 86400000
                } ?: false
            }
        }
    }.sortedBy { it.hora }
}