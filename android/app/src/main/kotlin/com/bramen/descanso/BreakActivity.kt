package com.bramen.descanso

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BreakActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    
    private fun isDarkMode(): Boolean {
        val mode = Reminder.themeMode(this)
        return when (mode) {
            "light" -> false
            "dark" -> true
            else -> {
                val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }

        val mode = Mode.values().firstOrNull { it.key == intent.getStringExtra("mode") }
            ?: Mode.VISUAL
        vibrate()

        val isDark = isDarkMode()
        val bgColor = if (isDark) 0xFF0A0A1A.toInt() else 0xFFF8F9FA.toInt()
        val textColor = if (isDark) 0xFFE0E0E0.toInt() else 0xFF1A1A1A.toInt()
        val mutedColor = if (isDark) 0xFF8C8C8C.toInt() else 0xFF666666.toInt()
        
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(bgColor)
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        val title = TextView(this).apply {
            text = "${mode.emoji}  ${mode.title}"
            textSize = 22f; setTextColor(mutedColor); gravity = Gravity.CENTER
        }
        val timer = TextView(this).apply {
            textSize = 64f; setTextColor(mode.tint); gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        }
        val tip = TextView(this).apply {
            text = Messages.random(this@BreakActivity, mode)
            textSize = 18f; setTextColor(textColor); gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(32))
        }

        col.addView(title)
        col.addView(space(dp(16)))
        col.addView(timer)
        col.addView(tip)

        val snooze = pill("Posponer 5 min", 0xFF2A2A4A.toInt())
        val skip = pill("Saltar", mode.tint)
        snooze.setOnClickListener { Reminder.schedule(this, mode, 5); finish() }
        skip.setOnClickListener { finish() }
        col.addView(snooze)
        col.addView(space(dp(12)))
        col.addView(skip)

        setContentView(col)

        var left = Reminder.breakSec(this, mode)
        val tick = object : Runnable {
            override fun run() {
                if (left > 0) {
                    timer.text = "$left"
                    left--
                    h.postDelayed(this, 1000)
                } else {
                    timer.text = "✓"
                    title.text = "¡Listo! Puedes continuar"
                    timer.setTextColor(Color.GREEN)
                    
                    // Vibración al finalizar
                    vibrate()
                    
                    // Sonido de notificación al finalizar
                    playCompletionSound()
                }
            }
        }
        tick.run()
    }

    override fun onDestroy() {
        super.onDestroy()
        h.removeCallbacksAndMessages(null)
    }

    private fun vibrate(duration: Long = 200) {
        if (!Reminder.vibrates(this)) return
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun playCompletionSound() {
        if (!Reminder.vibrates(this)) return // Usamos el mismo ajuste para el sonido
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            ringtone.play()
        } catch (e: Exception) {
            // Si falla el sonido, al menos vibramos más tiempo
            vibrate(500)
        }
    }

    private fun pill(label: String, color: Int) = Button(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 16f
        isAllCaps = false
        background = GradientDrawable().apply { cornerRadius = dp(24).toFloat(); setColor(color) }
        setPadding(dp(36), dp(14), dp(36), dp(14))
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    private fun space(px: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, px)
    }
}
