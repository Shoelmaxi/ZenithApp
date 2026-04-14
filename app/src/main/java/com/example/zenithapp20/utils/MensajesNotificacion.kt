package com.example.zenithapp20.utils

import android.content.Context

object MensajesNotificacion {

    // --- POOLS DE MENSAJES POR MOMENTO DEL DÍA ---

    val mensajesMañana = listOf(
        Pair("☀️ Nuevo día, misma misión", "Tus hábitos de hoy no se van a hacer solos. Empieza ahora y el resto del día será más fácil."),
        Pair("🌅 Buenos días", "Cada día que abres Zenith es un día que te tomas en serio. ¿Hoy también?"),
        Pair("⚡ El día acaba de empezar", "Los que llegan lejos no esperan motivación. Abren la app y lo hacen igual."),
        Pair("🎯 Hoy es un buen día para no fallar", "Llevas una racha que vale la pena proteger. Empieza antes del mediodía."),
        Pair("💪 Disciplina > Motivación", "No necesitas ganas. Solo necesitas abrir Zenith y marcar lo que toca."),
        Pair("🔥 Tu racha sigue viva", "Por ahora. Pero solo sobrevive si actúas hoy. ¿Vas a empezar?"),
        Pair("📋 El resumen de hoy", "Tienes hábitos pendientes. El mejor momento para empezar es ahora, el segundo mejor también."),
        Pair("🌄 Mañana temprano", "Los que tienen rachas largas no esperan la noche. ¿Cuándo vas a empezar hoy?")
    )

    val mensajesMediadia = listOf(
        Pair("⏳ Ya es mediodía", "La mañana se fue. ¿Qué tan lejos estás de cerrar el día?"),
        Pair("🕐 Mitad del día", "Si no empezaste en la mañana, empieza ahora. Todavía hay tiempo."),
        Pair("📊 Control de mediodía", "Los días perfectos no se hacen solos. Revisa tus hábitos antes de que se te olvide."),
        Pair("⚠️ El día avanza", "Cada hora que pasa es una hora menos para completar tus hábitos. Abre Zenith."),
        Pair("🎯 Pausa de 30 segundos", "Eso es todo lo que necesitas para marcar tus hábitos. ¿Lo tienes?"),
        Pair("💡 Recordatorio de mediodía", "No dejes tus hábitos para la noche. La noche tiene sus propios problemas."),
        Pair("🔔 Son las 12:30", "Ideal para revisar cómo vas. Los hábitos de hoy no se hacen mañana."),
        Pair("📅 Hoy todavía cuenta", "La racha de ayer no sirve si no actúas hoy. Revisa Zenith.")
    )

    val mensajesTarde = listOf(
        Pair("🌆 Son las 5 PM", "Si no lo hiciste en la mañana ni al mediodía, la tarde es tu última oportunidad tranquila."),
        Pair("⚠️ La noche se acerca", "Y con ella el cansancio. Haz tus hábitos ahora que todavía tienes energía."),
        Pair("🎯 Empujón de tarde", "El 80% de las rachas rotas pasan porque 'lo dejo para después'. No seas ese 80%."),
        Pair("🔥 Rachas en riesgo", "Algunos de tus hábitos aún no están marcados. Cada hora que pasa pesa más."),
        Pair("⏰ Todavía estás a tiempo", "Pero la ventana se cierra. Abre Zenith, marca lo que toca, cierra el día."),
        Pair("💪 La disciplina no descansa a las 5", "Los que tienen rachas de semanas no paran porque es tarde. Tú tampoco."),
        Pair("📋 Revisión de tarde", "¿Cuántos hábitos llevas hoy? Abre Zenith y compruébalo."),
        Pair("🚦 Señal amarilla", "No es urgente todavía. Pero si esperas dos horas más, sí lo será.")
    )

    val mensajesNoche = listOf(
        Pair("🌙 Última oportunidad tranquila", "Dentro de poco el sueño va a ganar. Haz tus hábitos ahora antes de que sea un drama."),
        Pair("🚨 Son las 8 PM", "Si no actúas en la próxima hora, tu racha se rompe a medianoche. Así de simple."),
        Pair("🔥 El contador no espera", "Medianoche es la hora cero. Cada minuto que pasa es uno menos para salvar tu racha."),
        Pair("⏰ No lo dejes para las 11", "A las 11 ya no tienes energía. A las 8 todavía sí. Entra a Zenith ahora."),
        Pair("💀 Las rachas mueren de noche", "No por decisión. Por olvido. No dejes que la tuya sea otra estadística."),
        Pair("📱 Abre Zenith — te toma 30 segundos", "Eso es todo. 30 segundos para proteger días o semanas de progreso."),
        Pair("🎯 El día termina a medianoche", "No mañana. No en un rato. A las 00:00. ¿Vas a cerrar bien el día?"),
        Pair("⚡ Energía de reserva", "Sé que estás cansado. Pero romper una racha duele más que el cansancio. Entra y márcalo.")
    )

    val mensajesUltimoAviso = listOf(
        Pair("💀 Menos de 90 minutos", "A medianoche todo se resetea. Tu racha, tu progreso de hoy, todo. Abre Zenith AHORA."),
        Pair("🚨 Son las 10:30 PM — actúa ya", "No hay segunda oportunidad después de las 00:00. Lo que no marques hoy, no cuenta."),
        Pair("⏰ El tiempo se acaba", "Cada notificación que ignoras es un día menos de racha. Esta es la última del día."),
        Pair("🔥 Tu racha muere en 90 minutos", "A menos que hagas algo ahora. Entra a Zenith. Son 20 segundos."),
        Pair("😤 ¿De verdad vas a perder tu racha por no abrir la app?", "Llevas días construyendo algo. No lo tires por no hacer clic."),
        Pair("🛑 Última llamada", "Después de esto, no hay más avisos. Lo que no hagas antes de medianoche se pierde."),
        Pair("💡 Último recordatorio del día", "Mañana te vas a arrepentir si no abres Zenith ahora. Ya lo sabes."),
        Pair("🎯 El día termina pronto", "Y con él tu oportunidad. No esperes a mañana para hacer lo de hoy.")
    )

    val mensajesExito = listOf(
        Pair("✅ Día perfecto", "Completaste todo hoy. Eso no es suerte, es disciplina. Vuelve mañana."),
        Pair("🏆 Lo lograste hoy", "Todos los hábitos marcados. Así se construyen las rachas largas. Hasta mañana."),
        Pair("💪 Día cerrado", "Completo. Eso es lo que separa a los que avanzan de los que no. Descansa."),
        Pair("🔥 Racha asegurada", "Hoy no faltaste. Mañana tampoco vas a fallar. Ya lo sabes."),
        Pair("⭐ Día perfecto confirmado", "No necesitas motivación cuando tienes constancia. Nos vemos mañana."),
        Pair("✨ Lo hiciste de nuevo", "Un día más en la lista. Cada uno cuenta. Descansa bien.")
    )

    // --- SELECTOR ALEATORIO CON MEMORIA (no repite el último) ---
    private val prefs_key_prefix = "last_msg_index_"

    fun obtenerMensaje(
        context: Context,
        pool: List<Pair<String, String>>,
        poolKey: String
    ): Pair<String, String> {
        val prefs = context.getSharedPreferences("zenith_notif_prefs", Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt("$prefs_key_prefix$poolKey", -1)

        val candidatos = pool.indices.filter { it != lastIndex }
        val nuevoIndex = candidatos.random()

        prefs.edit().putInt("$prefs_key_prefix$poolKey", nuevoIndex).apply()
        return pool[nuevoIndex]
    }
}