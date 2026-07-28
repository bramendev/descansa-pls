package com.bramen.descanso

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val bg = 0xFF0A0A1A.toInt()
    private val cardBg = 0xFF16162E.toInt()
    private val primary = 0xFFE6E6FF.toInt()
    private val muted = 0xFF8C8CB0.toInt()
    private val accent = 0xFF6E8CFF.toInt()
    private val green = 0xFF6CE08C.toInt()

    private lateinit var countdown: TextView
    private val h = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        Reminder.ensurePermissions(this)
        val paused = Reminder.isPaused(this)
        // No reinicia el temporizador si ya hay uno en curso, ni reactiva si está en pausa.
        if (!paused && Reminder.nextAt(this) < System.currentTimeMillis()) Reminder.schedule(this)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(24), dp(48), dp(24), dp(32))
        }

        col.addView(text("🐕", 56, primary, Gravity.CENTER))
        col.addView(text("Descanso", 32, primary, Gravity.CENTER, bold = true))
        col.addView(text("Cuida tus ojos · muévete · hidrátate", 14, muted, Gravity.CENTER))
        col.addView(space(dp(28)))

        col.addView(card(
            if (paused) "⏸ Pausado" else "● Activo",
            if (paused) muted else green,
            body(if (paused) "Los recordatorios están en pausa."
                 else "Te avisaré automáticamente,\naunque cierres la app.", 15, muted)))
        col.addView(space(dp(14)))

        countdown = body("—", 30, primary, bold = true)
        col.addView(card("PRÓXIMO DESCANSO", accent, countdown))
        col.addView(space(dp(14)))

        col.addView(card("DESCANSOS HOY", accent,
            body(Reminder.todayCount(this).toString(), 30, primary, bold = true)))
        col.addView(space(dp(14)))

        col.addView(card("FRECUENCIA", accent,
            body("Cada ${Reminder.INTERVAL_MIN} minutos", 18, primary)))
        col.addView(space(dp(28)))

        col.addView(pill(if (paused) "Reanudar" else "Pausar recordatorios") {
            if (Reminder.isPaused(this)) Reminder.schedule(this) else Reminder.pause(this)
            recreate()
        })

        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(col) })
    }

    // La cuenta regresiva solo corre con la app en pantalla: fuera de ella el
    // Handler seguiría despertando cada segundo sin que nadie lo vea.
    override fun onResume() { super.onResume(); tick() }
    override fun onPause() { super.onPause(); h.removeCallbacksAndMessages(null) }

    private fun tick() {
        val next = Reminder.nextAt(this)
        val ms = next - System.currentTimeMillis()
        countdown.text = when {
            Reminder.isPaused(this) || next == 0L -> "—"
            ms <= 0 -> "pronto"
            else -> "%d:%02d".format(ms / 1000 / 60, ms / 1000 % 60)
        }
        h.postDelayed(::tick, 1000)
    }

    // --- helpers de UI (sin XML ni dependencias) ---

    private fun text(t: String, size: Int, color: Int, gravity: Int = Gravity.START,
                     bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size.toFloat()
        setTextColor(color)
        this.gravity = gravity
        if (bold) setTypeface(typeface, Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    private fun body(t: String, size: Int, color: Int, bold: Boolean = false) =
        text(t, size, color, Gravity.START, bold)

    private fun card(title: String, titleColor: Int, bodyView: View): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(cardBg)
            }
            setPadding(dp(20), dp(16), dp(20), dp(18))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            addView(text(title, 13, titleColor, bold = true))
            addView(space(dp(6)))
            addView(bodyView)
        }
    }

    private fun space(px: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, px)
    }

    private fun pill(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(primary)
        textSize = 16f
        isAllCaps = false
        background = GradientDrawable().apply {
            cornerRadius = dp(24).toFloat()
            setColor(cardBg)
            setStroke(dp(1), accent)
        }
        setPadding(dp(24), dp(14), dp(24), dp(14))
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        setOnClickListener { onClick() }
    }
}
