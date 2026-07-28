package com.bramen.descanso

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        vibrate()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF0A0A1A.toInt())
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        val title = TextView(this).apply {
            text = "Hora de descansar"
            textSize = 22f; setTextColor(0xFF9090B0.toInt()); gravity = Gravity.CENTER
        }
        val timer = TextView(this).apply {
            textSize = 64f; setTextColor(0xFFE6E6FF.toInt()); gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        }
        val tip = TextView(this).apply {
            text = Messages.random()
            textSize = 18f; setTextColor(0xFFB8B8E0.toInt()); gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(32))
        }

        col.addView(title)
        col.addView(space(dp(16)))
        col.addView(timer)
        col.addView(tip)

        val snooze = pill("Posponer 5 min", 0xFF2A2A4A.toInt())
        val skip = pill("Saltar", 0xFF3344AA.toInt())
        snooze.setOnClickListener { Reminder.schedule(this, 5); finish() }
        skip.setOnClickListener { finish() }
        col.addView(snooze)
        col.addView(space(dp(12)))
        col.addView(skip)

        setContentView(col)

        var left = 20
        val tick = object : Runnable {
            override fun run() {
                if (left > 0) { timer.text = "$left"; left--; h.postDelayed(this, 1000) }
                else { timer.text = "✓"; title.text = "¡Listo! Puedes continuar" }
            }
        }
        tick.run()
    }

    override fun onDestroy() {
        super.onDestroy()
        h.removeCallbacksAndMessages(null)
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26)
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
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
