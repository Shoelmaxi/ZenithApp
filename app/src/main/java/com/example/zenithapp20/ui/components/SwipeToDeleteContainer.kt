package com.example.zenithapp20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    mensajeConfirmacion: String = "Esta acción no se puede deshacer.",
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var mostrarDialog by remember { mutableStateOf(false) }

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                mostrarDialog = true
            }
            false // siempre false para que no desaparezca solo
        }
    )

    if (mostrarDialog) {
        ConfirmDeleteDialog(
            mensaje = mensajeConfirmacion,
            onConfirm = {
                mostrarDialog = false
                onDelete()
            },
            onDismiss = {
                mostrarDialog = false
            }
        )
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isDismissing = state.dismissDirection == SwipeToDismissBoxValue.EndToStart

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDismissing) Color.Red.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isDismissing) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
                }
            }
        },
        content = { content() }
    )
}