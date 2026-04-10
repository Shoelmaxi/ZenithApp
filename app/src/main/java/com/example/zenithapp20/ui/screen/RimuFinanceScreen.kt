package com.example.zenithapp20.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.zenithapp20.ui.components.FinanceForm
import com.example.zenithapp20.ui.components.FinanzasItem
import com.example.zenithapp20.ui.components.formatCLP
import com.example.zenithapp20.ui.theme.CardBorderColor
import com.example.zenithapp20.ui.theme.DeepBackground
import com.example.zenithapp20.ui.theme.MainCardBackground
import com.example.zenithapp20.ui.theme.SecondaryText
import com.example.zenithapp20.ui.viewmodel.FinanzasViewModel // Importa tu ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RimuFinanceScreen(navController: NavController, viewModel: FinanzasViewModel) {
    // --- CONEXIÓN A ROOM VÍA VIEWMODEL ---
    val listaTransacciones by viewModel.transacciones.collectAsState()
    val ingresos by viewModel.ingresos.collectAsState()
    val egresos by viewModel.egresos.collectAsState()
    val balance by viewModel.balance.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Cabecera
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = SecondaryText)
            }
            Text("FINANZAS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TARJETA DE BALANCE TOTAL
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MainCardBackground,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BALANCE DISPONIBLE", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatCLP(balance),
                    color = if (balance >= 0) Color.White else Color.Red,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FinanceStat(label = "INGRESOS", value = ingresos, color = Color.Green)
                    FinanceStat(label = "EGRESOS", value = egresos, color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // LISTA DE MOVIMIENTOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MOVIMIENTOS", color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, null, tint = Color.Green)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (listaTransacciones.isEmpty()) {
                item {
                    Text(
                        "No hay movimientos este mes",
                        color = SecondaryText,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            } else {
                items(listaTransacciones) { transaccion ->
                    // Aquí podrías envolverlo en tu SwipeToDeleteContainer si quieres borrar
                    FinanzasItem(transaccion = transaccion)
                }
            }
        }
    }

    // Modal para agregar transacción
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = MainCardBackground
        ) {
            FinanceForm(onSave = { nueva ->
                viewModel.guardarTransaccion(nueva) // Guardamos en la BD
                showAddSheet = false
            })
        }
    }
}

@Composable
fun FinanceStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SecondaryText, fontSize = 10.sp)
        Text(formatCLP(value), color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
