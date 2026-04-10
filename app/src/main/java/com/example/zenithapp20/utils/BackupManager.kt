package com.example.zenithapp20.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val DB_NAME = "zenith_database"

    fun exportarBackup(context: Context): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupDir = File(context.getExternalFilesDir(null), "Zenith/Backup")
            if (!backupDir.exists()) backupDir.mkdirs()

            val fecha = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "zenith_backup_$fecha.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun compartirBackup(context: Context): Intent? {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupDir = File(context.filesDir, "backup")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "zenith_backup.db")
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Zenith Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun restaurarBackup(context: Context, backupFile: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun obtenerBackupsDisponibles(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "Zenith/Backup")
        return if (backupDir.exists()) {
            backupDir.listFiles()
                ?.filter { it.name.endsWith(".db") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else emptyList()
    }
}