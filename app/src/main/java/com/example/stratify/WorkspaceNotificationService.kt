package com.example.stratify

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WorkspaceNotificationService : Service() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null
    private val notificationId = 1001
    private val channelId = "workspace_updates"

    companion object {
        const val EXTRA_WORKSPACE_ID = "workspace_id"

        fun start(context: Context, workspaceId: String) {
            val intent = Intent(context, WorkspaceNotificationService::class.java).apply {
                putExtra(EXTRA_WORKSPACE_ID, workspaceId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkspaceNotificationService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val workspaceId = intent?.getStringExtra(EXTRA_WORKSPACE_ID) ?: return START_NOT_STICKY

        val notification = createForegroundNotification("Monitoring workspace updates...")
        startForeground(notificationId, notification)

        listenToWorkspaceChanges(workspaceId)

        return START_STICKY
    }

    private fun listenToWorkspaceChanges(workspaceId: String) {
        listener = db.collection("workspaces")
            .document(workspaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val workspace = snapshot?.toObject(Workspace::class.java) ?: return@addSnapshotListener

                val lastStatus = getLastKnownStatus(workspaceId)

                if (lastStatus != null && lastStatus != workspace.status) {
                    showStatusUpdateNotification(
                        workspaceName = workspace.name,
                        newStatus = workspace.status,
                        workspaceId = workspaceId
                    )
                }

                saveLastKnownStatus(workspaceId, workspace.status)
            }
    }

    private fun showStatusUpdateNotification(
        workspaceName: String,
        newStatus: String,
        workspaceId: String
    ) {
        val emoji = when (newStatus) {
            "Done" -> "✅"
            "In Progress" -> "🔄"
            "To Verify" -> "👀"
            else -> "📋"
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("workspace_id", workspaceId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("$workspaceName $emoji")
            .setContentText("Status changed to: $newStatus")
            .setSmallIcon(R.drawable.ic_workspace_status_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createForegroundNotification(text: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Stratify")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_workspace_status_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Workspace Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for workspace status changes"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getLastKnownStatus(workspaceId: String): String? {
        val prefs = getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        return prefs.getString("status_$workspaceId", null)
    }

    private fun saveLastKnownStatus(workspaceId: String, status: String) {
        val prefs = getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("status_$workspaceId", status).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}