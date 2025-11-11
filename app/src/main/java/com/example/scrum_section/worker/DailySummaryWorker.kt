package com.example.scrum_section.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.scrum_section.data.TaskRepository
import com.example.scrum_section.util.TaskStatus
import com.example.stratify.R

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "STRATIFY_DAILY_SUMMARY_CHANNEL"
        const val NOTIFICATION_ID = 101
        const val WORK_NAME = "DailySummaryWorker"
    }

    override suspend fun doWork(): Result {
        try {
            Log.d(WORK_NAME, "Worker dimulai...")

            // 1. Ambil semua tugas dari repositori
            val allTasks = TaskRepository.getTasks()

            // 2. Filter tugas yang belum selesai (TODO atau IN_PROGRESS)
            val pendingTasks = allTasks.filter {
                it.status == TaskStatus.TODO ||
                        it.status == TaskStatus.IN_PROGRESS
            }

            if (pendingTasks.isEmpty()) {
                Log.d(WORK_NAME, "Tidak ada tugas yang perlu diingatkan. Pekerjaan selesai.")
                return Result.success()
            }

            // 3. Konten notifikasi
            val notificationTitle = "Ringkasan Tugas Harian Anda"
            val notificationContent = "Anda memiliki ${pendingTasks.size} tugas yang belum selesai. Ayo selesaikan!"

            // 4. Tampilkan notifikasi
            showNotification(notificationTitle, notificationContent)

            Log.d(WORK_NAME, "Notifikasi ringkasan harian berhasil ditampilkan.")
            return Result.success()

        } catch (e: Exception) {
            Log.e(WORK_NAME, "Pekerjaan gagal", e)
            return Result.failure()
        }
    }

    private fun showNotification(title: String, content: String) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_scrum)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Periksa izin notifikasi sebelum menampilkan
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(WORK_NAME, "Izin notifikasi tidak diberikan.")
            return
        }

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ringkasan Tugas Harian"
            val descriptionText = "Notifikasi harian untuk tugas yang belum selesai"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}