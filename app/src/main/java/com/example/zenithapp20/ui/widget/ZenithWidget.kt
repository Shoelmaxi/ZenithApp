package com.example.zenithapp20.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.zenithapp20.MainActivity
import com.example.zenithapp20.data.database.AppDatabase
import com.example.zenithapp20.data.model.Habito
import java.util.Calendar

class ZenithWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val habitos = db.habitosDao().getAllHabitosSync()

        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val completados = habitos.count { habito ->
            habito.checks.any { it >= hoy && it < hoy + 86400000 }
        }

        provideContent {
            WidgetContent(
                habitos = habitos,
                completados = completados,
                total = habitos.size,
                inicioHoy = hoy
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun WidgetContent(
    habitos: List<Habito>,
    completados: Int,
    total: Int,
    inicioHoy: Long
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1A1A1A)))
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // HEADER
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ZENITH",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$completados/$total",
                    style = TextStyle(
                        color = ColorProvider(
                            if (completados == total && total > 0) Color(0xFF00C853)
                            else Color(0xFF888888)
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (habitos.isEmpty()) {
                Text(
                    text = "Sin hábitos creados",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF888888)),
                        fontSize = 12.sp
                    )
                )
            } else {
                // mostrar máximo 4 hábitos
                habitos.take(4).forEach { habito ->
                    val completadoHoy = habito.checks.any {
                        it >= inicioHoy && it < inicioHoy + 86400000
                    }

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (completadoHoy) "✓" else "○",
                            style = TextStyle(
                                color = ColorProvider(
                                    if (completadoHoy) Color(0xFF00C853) else Color(0xFF555555)
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = habito.nombre,
                            style = TextStyle(
                                color = ColorProvider(
                                    if (completadoHoy) Color(0xFF888888) else Color.White
                                ),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        if (habito.rachaDias > 1) {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "🔥${habito.rachaDias}",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFFFD700)),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (habitos.size > 4) {
                    Text(
                        text = "+${habitos.size - 4} más",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF888888)),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

class ZenithWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZenithWidget()
}