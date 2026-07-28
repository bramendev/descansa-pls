package com.bramen.descanso

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/** Programa el próximo descanso y lo dispara vía notificación full-screen. */
object Reminder {
    const val INTERVAL_MIN = 20
    const val CHANNEL = "descanso"
    private const val REQ = 1001

    fun schedule(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val at = System.currentTimeMillis() + INTERVAL_MIN * 60_000L
        val pi = PendingIntent.getBroadcast(
            ctx, REQ, Intent(ctx, BreakReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Exacto y atraviesa Doze. No es repetitivo: cada disparo se re-agenda.
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun ensurePermissions(act: Activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            act.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        val am = act.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            act.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }

    fun ensureChannel(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Descanso", NotificationManager.IMPORTANCE_HIGH)
        )
    }
}

class BreakReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        Reminder.ensureChannel(ctx)
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val full = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, BreakActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(ctx, Reminder.CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora de descansar")
            .setContentText("Descansa la vista 20 segundos")
            .setFullScreenIntent(full, true)
            .setAutoCancel(true)
            .build()
        nm.notify(1, n)
        Reminder.schedule(ctx) // agenda el siguiente
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) = Reminder.schedule(ctx)
}
