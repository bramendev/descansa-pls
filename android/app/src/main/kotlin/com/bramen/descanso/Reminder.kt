package com.bramen.descanso

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.json.JSONObject

/**
 * Los cinco tipos de aviso. Un modo avisa cada [defEvery] minutos, o bien una
 * vez al día a [defAtMin] minutos de la medianoche si [defEvery] es 0.
 */
enum class Mode(
    val key: String,
    val title: String,
    val emoji: String,
    val defEvery: Int,
    val defAtMin: Int,
    val defSec: Int,
    val defOn: Boolean,
    val tint: Int,
) {
    VISUAL("visual", "Descanso visual", "👀", 20, 0, 20, true, 0xFF6E8CFF.toInt()),
    AGUA("agua", "Hidratación", "💧", 30, 0, 15, false, 0xFF44AAFF.toInt()),
    ACTIVA("activa", "Pausa activa", "🏃", 60, 0, 180, false, 0xFF6CE08C.toInt()),
    ALMUERZO("almuerzo", "Hora de almorzar", "🍽", 0, 12 * 60, 60, false, 0xFFFFAA44.toInt()),
    DORMIR("dormir", "Hora de dormir", "🌙", 0, 22 * 60 + 30, 60, false, 0xFF8888FF.toInt());

    val isDaily get() = defEvery == 0
}

/** Programa los descansos y los dispara vía notificación full-screen. */
object Reminder {
    const val CHANNEL = "descanso"
    const val CHANNEL_STATUS = "estado"
    private const val STATUS_ID = 99
    private const val HISTORY_DAYS = 30

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("descanso", Context.MODE_PRIVATE)

    private fun pi(ctx: Context, m: Mode) = PendingIntent.getBroadcast(
        ctx, 100 + m.ordinal,
        Intent(ctx, BreakReceiver::class.java).putExtra("mode", m.key),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // --- ajustes por modo ---

    fun isOn(ctx: Context, m: Mode) = prefs(ctx).getBoolean("on_${m.key}", m.defOn)
    fun everyMin(ctx: Context, m: Mode) = prefs(ctx).getInt("every_${m.key}", m.defEvery)
    fun breakSec(ctx: Context, m: Mode) = prefs(ctx).getInt("sec_${m.key}", m.defSec)
    fun dailyMin(ctx: Context, m: Mode) = prefs(ctx).getInt("at_${m.key}", m.defAtMin)
    fun nextAt(ctx: Context, m: Mode) = prefs(ctx).getLong("nextAt_${m.key}", 0)

    fun saveMode(ctx: Context, m: Mode, on: Boolean, every: Int, sec: Int, atMin: Int) {
        prefs(ctx).edit()
            .putBoolean("on_${m.key}", on)
            .putInt("every_${m.key}", every)
            .putInt("sec_${m.key}", sec)
            .putInt("at_${m.key}", atMin)
            .apply()
    }

    // --- ajustes globales ---

    fun vibrates(ctx: Context) = prefs(ctx).getBoolean("vibrate", true)
    fun showStatus(ctx: Context) = prefs(ctx).getBoolean("status", true)
    fun quietFrom(ctx: Context) = prefs(ctx).getInt("quietFrom", 22)
    fun quietTo(ctx: Context) = prefs(ctx).getInt("quietTo", 8)
    fun customTips(ctx: Context) = prefs(ctx).getString("tips", "") ?: ""
    
    /** Modo de tema: "auto" (sigue sistema), "light" o "dark" */
    fun themeMode(ctx: Context) = prefs(ctx).getString("theme", "auto") ?: "auto"
    
    /** Animal para la animación: "perro" o "gato" */
    fun animal(ctx: Context) = prefs(ctx).getString("animal", "perro") ?: "perro"

    fun saveGlobal(ctx: Context, vibrate: Boolean, status: Boolean,
                   quietFrom: Int, quietTo: Int, tips: String, theme: String = "auto", animal: String = "perro") {
        prefs(ctx).edit()
            .putBoolean("vibrate", vibrate)
            .putBoolean("status", status)
            .putInt("quietFrom", quietFrom)
            .putInt("quietTo", quietTo)
            .putString("tips", tips)
            .putString("theme", theme)
            .putString("animal", animal)
            .apply()
    }

    // --- horario de silencio ---

    /** ¿Ese instante cae dentro del "no molestar"? Ventana vacía = nunca. */
    private fun inQuiet(ctx: Context, at: Long): Boolean {
        val f = quietFrom(ctx)
        val t = quietTo(ctx)
        if (f == t) return false
        val h = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).hour
        return if (f < t) h in f until t else (h >= f || h < t)
    }

    /** Empuja un instante al final del silencio si cae dentro de él. */
    private fun afterQuiet(ctx: Context, at: Long): Long {
        if (!inQuiet(ctx, at)) return at
        val z = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault())
        var end = z.withHour(quietTo(ctx)).withMinute(0).withSecond(0).withNano(0)
        if (!end.isAfter(z)) end = end.plusDays(1)
        return end.toInstant().toEpochMilli()
    }

    // --- agenda ---

    private fun nextFire(ctx: Context, m: Mode, minutes: Int?): Long {
        // Posponer es una acción explícita del usuario: se respeta tal cual.
        if (minutes != null) return System.currentTimeMillis() + minutes * 60_000L
        if (m.isDaily) {
            // Los modos de hora fija ignoran el silencio: "dormir" cae dentro a propósito.
            val at = dailyMin(ctx, m)
            val now = ZonedDateTime.now()
            var z = now.withHour(at / 60).withMinute(at % 60).withSecond(0).withNano(0)
            if (!z.isAfter(now)) z = z.plusDays(1)
            return z.toInstant().toEpochMilli()
        }
        return afterQuiet(ctx, System.currentTimeMillis() + everyMin(ctx, m) * 60_000L)
    }

    fun schedule(ctx: Context, m: Mode, minutes: Int? = null) {
        if (isPaused(ctx) || !isOn(ctx, m)) {
            cancel(ctx, m)
            return
        }
        val at = nextFire(ctx, m, minutes)
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Exacto y atraviesa Doze. No es repetitivo: cada disparo se re-agenda.
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi(ctx, m))
        prefs(ctx).edit().putLong("nextAt_${m.key}", at).apply()
        status(ctx)
    }

    /** Reprograma todos los modos desde cero (tras reboot o cambio de ajustes). */
    fun scheduleAll(ctx: Context) {
        for (m in Mode.values()) schedule(ctx, m)
        status(ctx)
    }

    /**
     * Agenda solo lo que haga falta: no reinicia una cuenta atrás en curso, así
     * que abrir la app no retrasa el próximo descanso.
     */
    fun ensureScheduled(ctx: Context) {
        if (isPaused(ctx)) return
        for (m in Mode.values()) {
            if (!isOn(ctx, m)) cancel(ctx, m)
            else if (nextAt(ctx, m) < System.currentTimeMillis()) schedule(ctx, m)
        }
        status(ctx)
    }

    fun cancel(ctx: Context, m: Mode) {
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi(ctx, m))
        prefs(ctx).edit().putLong("nextAt_${m.key}", 0).apply()
    }

    fun isPaused(ctx: Context) = prefs(ctx).getBoolean("paused", false)

    fun pause(ctx: Context) {
        prefs(ctx).edit().putBoolean("paused", true).apply()
        for (m in Mode.values()) cancel(ctx, m)
        status(ctx)
    }

    fun resume(ctx: Context) {
        prefs(ctx).edit().putBoolean("paused", false).apply()
        scheduleAll(ctx)
    }

    /** El modo activo que avisará antes; null si no hay ninguno agendado. */
    fun soonest(ctx: Context): Mode? = Mode.values()
        .filter { isOn(ctx, it) && nextAt(ctx, it) > 0 }
        .minByOrNull { nextAt(ctx, it) }

    // --- estadísticas ---

    /** Descansos por día (clave ISO yyyy-MM-dd), últimos 30 días. */
    fun history(ctx: Context): Map<String, Int> {
        val out = HashMap<String, Int>()
        try {
            val o = JSONObject(prefs(ctx).getString("history", "{}") ?: "{}")
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = o.optInt(k, 0)
            }
        } catch (e: Exception) {
            // historial corrupto: preferimos empezar de cero antes que reventar
        }
        return out
    }

    /** Suma un descanso a hoy y poda el historial a 30 días. */
    fun countBreak(ctx: Context) {
        val hoy = LocalDate.now()
        val cutoff = hoy.minusDays(HISTORY_DAYS - 1L).toString()
        val h = history(ctx).filterKeys { it >= cutoff }.toMutableMap()
        h[hoy.toString()] = (h[hoy.toString()] ?: 0) + 1
        val o = JSONObject()
        for ((k, v) in h) o.put(k, v)
        prefs(ctx).edit()
            .putString("history", o.toString())
            .putInt("total", totalCount(ctx) + 1)
            .apply()
    }

    fun todayCount(ctx: Context) = history(ctx)[LocalDate.now().toString()] ?: 0

    /** Conteo por día de la semana en curso, de lunes a domingo. */
    fun weekDays(ctx: Context): List<Int> {
        val h = history(ctx)
        val hoy = LocalDate.now()
        val lunes = hoy.minusDays(hoy.dayOfWeek.value - 1L)
        return (0..6).map { h[lunes.plusDays(it.toLong()).toString()] ?: 0 }
    }

    /**
     * Días seguidos con al menos un descanso. Un día que aún no empieza no
     * rompe la racha: si hoy va en cero, se cuenta desde ayer.
     */
    fun streak(ctx: Context): Int {
        val h = history(ctx)
        var d = LocalDate.now()
        if ((h[d.toString()] ?: 0) == 0) d = d.minusDays(1)
        var n = 0
        while ((h[d.toString()] ?: 0) > 0) {
            n++
            d = d.minusDays(1)
        }
        return n
    }

    /** Acumulado histórico; sobrevive a la poda de 30 días. */
    fun totalCount(ctx: Context) = prefs(ctx).getInt("total", 0)

    // --- notificaciones ---

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Descansos", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_STATUS, "Estado", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) }
        )
    }

    /**
     * Notificación fija con la cuenta regresiva al próximo descanso. El cronómetro
     * lo lleva el sistema (setChronometerCountDown), así que no hay que refrescarla.
     */
    fun status(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val next = soonest(ctx)
        if (!showStatus(ctx) || isPaused(ctx) || next == null) {
            nm.cancel(STATUS_ID)
            return
        }
        ensureChannels(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(ctx, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.ic_stat_descanso)
            .setContentTitle("${next.emoji} ${next.title}")
            .setContentText("Próximo descanso")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(nextAt(ctx, next))
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setContentIntent(open)
            .build()
        nm.notify(STATUS_ID, n)
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
}

class BreakReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val m = Mode.values().firstOrNull { it.key == i.getStringExtra("mode") } ?: Mode.VISUAL
        Reminder.ensureChannels(ctx)
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val full = PendingIntent.getActivity(
            ctx, m.ordinal,
            Intent(ctx, BreakActivity::class.java)
                .putExtra("mode", m.key)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(ctx, Reminder.CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_descanso)
            .setContentTitle("${m.emoji} ${m.title}")
            .setContentText(Messages.random(ctx, m))
            .setFullScreenIntent(full, true)
            .setAutoCancel(true)
            .build()
        nm.notify(m.ordinal + 1, n)
        Reminder.countBreak(ctx)
        Reminder.schedule(ctx, m) // agenda el siguiente
    }
}

class BootReceiver : BroadcastReceiver() {
    // Está exportado por obligación (el sistema emite BOOT_COMPLETED), así que
    // no confiamos en el intent: solo actuamos ante las acciones que esperamos.
    override fun onReceive(ctx: Context, i: Intent) {
        if (i.action != Intent.ACTION_BOOT_COMPLETED &&
            i.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        // Reboot y actualización borran las alarmas: hay que reprogramarlas todas.
        if (!Reminder.isPaused(ctx)) Reminder.scheduleAll(ctx)
    }
}
