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

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("descanso", Context.MODE_PRIVATE)

    private fun pi(ctx: Context) = PendingIntent.getBroadcast(
        ctx, REQ, Intent(ctx, BreakReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun schedule(ctx: Context, minutes: Int = INTERVAL_MIN) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val at = System.currentTimeMillis() + minutes * 60_000L
        // Exacto y atraviesa Doze. No es repetitivo: cada disparo se re-agenda.
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(ctx))
        prefs(ctx).edit().putLong("nextAt", at).putBoolean("paused", false).apply()
    }

    /** Cancela la alarma pendiente y marca en pausa. */
    fun pause(ctx: Context) {
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi(ctx))
        prefs(ctx).edit().putBoolean("paused", true).putLong("nextAt", 0).apply()
    }

    fun isPaused(ctx: Context) = prefs(ctx).getBoolean("paused", false)

    /** Momento (epoch ms) del próximo descanso; 0 si no hay. */
    fun nextAt(ctx: Context) = prefs(ctx).getLong("nextAt", 0)

    /** Suma un descanso al contador de hoy (reinicia al cambiar de día). */
    fun countBreak(ctx: Context) {
        val p = prefs(ctx)
        val today = java.time.LocalDate.now().toString()
        val n = if (p.getString("day", "") == today) p.getInt("count", 0) + 1 else 1
        p.edit().putString("day", today).putInt("count", n).apply()
    }

    fun todayCount(ctx: Context): Int {
        val p = prefs(ctx)
        return if (p.getString("day", "") == java.time.LocalDate.now().toString())
            p.getInt("count", 0) else 0
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
        Reminder.countBreak(ctx)
        Reminder.schedule(ctx) // agenda el siguiente
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        if (!Reminder.isPaused(ctx)) Reminder.schedule(ctx) // no reactivar si estaba en pausa
    }
}
