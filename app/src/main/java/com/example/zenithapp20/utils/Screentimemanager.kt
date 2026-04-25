package com.example.zenithapp20.utils

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Process
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

/**
 * Wrapper de UsageStatsManager.
 * Devuelve tiempo total y por app para el día de hoy.
 * Gestiona la lista personalizable de apps de distracción.
 */
object ScreenTimeManager {

    private const val PREFS_NAME       = "zenith_screen_time"
    private const val KEY_CUSTOM_APPS  = "custom_distraction_apps"
    private val gson = Gson()

    // ── Apps de distracción conocidas ────────────────────────────────────────
    val APPS_DEFAULT: Map<String, String> = mapOf(
        "com.google.android.youtube"    to "YouTube",
        "com.zhiliaoapp.musically"      to "TikTok",
        "com.instagram.android"         to "Instagram",
        "com.twitter.android"           to "Twitter / X",
        "com.facebook.katana"           to "Facebook",
        "com.reddit.frontpage"          to "Reddit",
        "com.netflix.mediaclient"       to "Netflix",
        "com.discord"                   to "Discord",
        "com.snapchat.android"          to "Snapchat",
        "tv.twitch.android.viewer"      to "Twitch",
        "com.facebook.orca"             to "Messenger",
        "com.whatsapp"                  to "WhatsApp",
        "com.telegram.messenger"        to "Telegram",
        "com.spotify.music"             to "Spotify",
        "com.amazon.avod.thirdpartyclient" to "Prime Video"
    )

    // ── Permiso ──────────────────────────────────────────────────────────────

    fun tienePermiso(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode   = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ── Tiempo total de pantalla hoy (todas las apps) ────────────────────────

    fun getTiempoTotalHoy(context: Context): Long {
        if (!tienePermiso(context)) return 0L
        val usm   = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, inicioDiaHoy(), System.currentTimeMillis()
        )
        return stats?.sumOf { it.totalTimeInForeground } ?: 0L
    }

    // ── Tiempo por app de distracción hoy ────────────────────────────────────

    fun getTiempoDistracciones(context: Context): List<AppUsage> {
        if (!tienePermiso(context)) return emptyList()

        val usm   = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, inicioDiaHoy(), System.currentTimeMillis()
        ) ?: return emptyList()

        val statsMap = stats.associate { it.packageName to it.totalTimeInForeground }

        val todasLasApps = APPS_DEFAULT + getAppsPersonalizadas(context)
            .associate { pkg -> pkg to pkg.substringAfterLast(".").replaceFirstChar { it.uppercase() } }

        return todasLasApps
            .mapNotNull { (pkg, nombre) ->
                val tiempo = statsMap[pkg] ?: 0L
                if (tiempo > 60_000L) AppUsage(paquete = pkg, nombre = nombre, tiempoMs = tiempo)
                else null
            }
            .sortedByDescending { it.tiempoMs }
    }

    // ── Apps personalizadas (SharedPrefs) ────────────────────────────────────

    fun getAppsPersonalizadas(context: Context): List<String> {
        val json = prefs(context).getString(KEY_CUSTOM_APPS, "[]") ?: "[]"
        val tipo = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, tipo)
    }

    fun agregarAppPersonalizada(context: Context, paquete: String) {
        val lista = getAppsPersonalizadas(context).toMutableList()
        if (!lista.contains(paquete)) {
            lista.add(paquete)
            prefs(context).edit().putString(KEY_CUSTOM_APPS, gson.toJson(lista)).apply()
        }
    }

    fun eliminarAppPersonalizada(context: Context, paquete: String) {
        val lista = getAppsPersonalizadas(context).toMutableList().also { it.remove(paquete) }
        prefs(context).edit().putString(KEY_CUSTOM_APPS, gson.toJson(lista)).apply()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun tiempoATexto(ms: Long): String {
        val horas   = ms / 3_600_000L
        val minutos = (ms % 3_600_000L) / 60_000L
        return when {
            horas > 0 && minutos > 0 -> "${horas}h ${minutos}m"
            horas > 0                -> "${horas}h"
            else                     -> "${minutos}m"
        }
    }

    /** Convierte minutos de distracción en equivalencias de productividad. */
    fun calcularEquivalencias(totalMs: Long): List<Equivalencia> {
        val minutos = (totalMs / 60_000L).toInt()
        if (minutos < 5) return emptyList()

        val equivalencias = mutableListOf<Equivalencia>()
        val paginasLibro  = (minutos * 0.5).toInt()
        if (paginasLibro >= 10) {
            equivalencias.add(Equivalencia("📖", "~$paginasLibro páginas de lectura"))
        }
        val sesionesDeepWork = (minutos / 25)
        if (sesionesDeepWork >= 1) {
            equivalencias.add(Equivalencia("🧠", "$sesionesDeepWork sesión${if (sesionesDeepWork != 1) "es" else ""} de Deep Work (25 min)"))
        }
        val rutinaGym = (minutos / 60.0)
        if (rutinaGym >= 0.5) {
            val texto = if (rutinaGym >= 1.0)
                "${rutinaGym.toInt()} rutina${if (rutinaGym.toInt() != 1) "s" else ""} de gym"
            else
                "medio entrenamiento de gym"
            equivalencias.add(Equivalencia("🏋️", texto))
        }
        val horasDescanso = (minutos / 90.0)
        if (horasDescanso >= 1.0) {
            equivalencias.add(
                Equivalencia("😴", "${horasDescanso.toInt()} ciclo${if (horasDescanso.toInt() != 1) "s" else ""} de sueño (90 min)")
            )
        }
        return equivalencias
    }

    private fun inicioDiaHoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

data class AppUsage(
    val paquete: String,
    val nombre:  String,
    val tiempoMs: Long
)

data class Equivalencia(
    val emoji:  String,
    val texto:  String
)