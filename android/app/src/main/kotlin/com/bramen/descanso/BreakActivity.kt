package com.bramen.descanso

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.TextView

class BreakActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }

        val tv = TextView(this).apply {
            textSize = 28f
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF0A0A1A.toInt())
            setTextColor(0xFFE0E0FF.toInt())
        }
        setContentView(tv)

        var left = 20
        val h = Handler(Looper.getMainLooper())
        val tick = object : Runnable {
            override fun run() {
                tv.text = if (left > 0) "Descansa la vista\n\n$left s" else "¡Listo!\nToca para cerrar"
                if (left > 0) { left--; h.postDelayed(this, 1000) }
            }
        }
        tick.run()
        tv.setOnClickListener { if (left <= 0) finish() }
    }
}
