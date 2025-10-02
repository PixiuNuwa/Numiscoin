package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R
import java.text.SimpleDateFormat
import java.util.*

class BackupActivity : BaseActivity() {

    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var lastBackupTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        initViews()
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initViews() {
        downloadButton = findViewById(R.id.downloadButton)
        progressBar = findViewById(R.id.progressBar)
        statusTextView = findViewById(R.id.statusTextView)
        lastBackupTextView = findViewById(R.id.lastBackupTextView)

        // Configurar información del último backup
        updateLastBackupInfo()

        // Configurar botón de descarga
        downloadButton.setOnClickListener {
            startBackupProcess()
        }
    }

    private fun updateLastBackupInfo() {
        // Aquí obtendrías la información del último backup
        // val lastBackup = SharedPreferencesManager.getLastBackupDate()
        val lastBackup = "2024-01-15 14:30:00" // Ejemplo

        if (lastBackup.isNotEmpty()) {
            lastBackupTextView.text = "Última copia: $lastBackup"
        } else {
            lastBackupTextView.text = "No se han realizado copias de seguridad"
        }
    }

    private fun startBackupProcess() {
        // Simular proceso de backup
        downloadButton.isEnabled = false
        progressBar.visibility = ProgressBar.VISIBLE
        statusTextView.text = "Generando copia de seguridad en XML..."

        // Simular proceso con delay
        Thread {
            try {
                // Simular trabajo de backup
                Thread.sleep(3000)

                runOnUiThread {
                    backupCompleted()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    backupFailed(e.message ?: "Error desconocido")
                }
            }
        }.start()
    }

    private fun backupCompleted() {
        progressBar.visibility = ProgressBar.INVISIBLE
        downloadButton.isEnabled = true
        statusTextView.text = "Copia de seguridad generada exitosamente"

        // Actualizar fecha del último backup
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        lastBackupTextView.text = "Última copia: $currentDate"

        Toast.makeText(this, "Copia de seguridad descargada correctamente", Toast.LENGTH_LONG).show()
    }

    private fun backupFailed(error: String) {
        progressBar.visibility = ProgressBar.INVISIBLE
        downloadButton.isEnabled = true
        statusTextView.text = "Error al generar copia: $error"
        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BackupActivity::class.java)
            context.startActivity(intent)
        }
    }
}