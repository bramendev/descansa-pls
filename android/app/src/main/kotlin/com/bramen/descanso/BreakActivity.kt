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
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDate

class BreakActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private var animalImage: ImageView? = null
    private var currentFrame = 0
    private lateinit var animalFrames: List<Int>
    
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
        if (Build.VERSION.SDK_INT >= 27) { 
            setShowWhenLocked(true); 
            setTurnScreenOn(true)
        }
        // Fullscreen para BreakActivity
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val mode = Mode.values().firstOrNull { it.key == intent.getStringExtra("mode") }
            ?: Mode.VISUAL
        
        // Inicializar frames del animal
        val animal = Reminder.animal(this)
        animalFrames = if (animal == "gato" || animal == "cat") {
            listOf(R.drawable.cat_frame0, R.drawable.cat_frame1, R.drawable.cat_frame2, R.drawable.cat_frame3)
        } else {
            listOf(R.drawable.dog_frame0, R.drawable.dog_frame1, R.drawable.dog_frame2, R.drawable.dog_frame3)
        }
        
        // Vibración y sonido al abrir
        vibrate()
        playSoundIfEnabled()
        // Contar el break
        Reminder.countBreak(this)

        val isDark = isDarkMode()
        val bgColor = if (isDark) 0xFF0A0A1A.toInt() else 0xFFF8F9FA.toInt()
        val textColor = if (isDark) 0xFFE0E0E0.toInt() else 0xFF1A1A1A.toInt()
        val mutedColor = if (isDark) 0xFF8C8C8C.toInt() else 0xFF666666.toInt()
        
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(bgColor)
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        // ========== TITULO ==========
        val title = TextView(this).apply {
            text = "${mode.emoji}  ${mode.title}"
            textSize = 28f
            setTextColor(mode.tint)
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        }
        col.addView(title)
        col.addView(space(dp(16)))
        
        // ========== ANIMAL ANIMADO ==========
        // Intentar cargar sprites si existen
        val animalName = Reminder.animal(this) // Necesitamos añadir esta función
        val hasSprites = try {
            val resId = resources.getIdentifier("frame0", "drawable", packageName)
            resId != 0
        } catch (e: Exception) {
            false
        }
        
        if (hasSprites) {
            animalImage = ImageView(this).apply {
                // Cargar frame inicial
                setImageResource(R.drawable.frame0)
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
            col.addView(animalImage)
            col.addView(space(dp(24)))
            
            // Iniciar animación
            h.postDelayed({ animateAnimal() }, 300)
        }
        
        // ========== TIMER ==========
        val timer = TextView(this).apply {
            textSize = 72f
            setTextColor(mode.tint)
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        }
        col.addView(timer)
        col.addView(space(dp(24)))
        
        // ========== STATS: RACHA Y CONTEO ==========
        val todayCount = Reminder.todayCount(this)
        val totalCount = Reminder.totalCount(this)
        val streak = Reminder.streak(this)
        
        val statsText = when (mode) {
            Mode.VISUAL -> {
                val weekCount = Reminder.weekDays(this).sum()
                if (streak > 0) {
                    "🔥 $streak días · Hoy: $todayCount · Semana: $weekCount · Total: $totalCount"
                } else {
                    "Hoy: $todayCount · Semana: ${Reminder.weekDays(this).sum()} · Total: $totalCount"
                }
            }
            Mode.ACTIVA -> {
                if (streak > 0) {
                    "🔥 Racha: $streak días · Total: $totalCount pausas activas"
                } else {
                    "Total: $totalCount pausas activas"
                }
            }
            else -> {
                if (streak > 0) {
                    "🔥 Racha: $streak días · Total: $totalCount"
                } else {
                    "Total: $totalCount"
                }
            }
        }
        
        val statsView = TextView(this).apply {
            text = statsText
            textSize = 15f
            setTextColor(mutedColor)
            gravity = Gravity.CENTER
        }
        col.addView(statsView)
        col.addView(space(dp(16)))
        
        // ========== TIP CONSEJO ==========
        val tip = TextView(this).apply {
            text = Messages.random(this@BreakActivity, mode)
            textSize = 17f
            setTextColor(textColor)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(32))
        }
        col.addView(tip)

        // ========== BOTONES ==========
        val snooze = pill("Posponer 5 min", if (isDark) 0xFF2A2A4A.toInt() else 0xFFE0E0E0.toInt())
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
                    timer.text = "$left s"
                    left--
                    h.postDelayed(this, 1000)
                } else {
                    timer.text = "✓"
                    title.text = "¡Listo! Puedes continuar"
                    timer.setTextColor(Color.GREEN)
                    
                    // Vibración y sonido al finalizar
                    vibrateEnd()
                    playCompletionSound()
                }
            }
        }
        tick.run()
    }
    
    private fun animateAnimal() {
        if (animalImage == null) return
        
        currentFrame = (currentFrame + 1) % animalFrames.size
        try {
            animalImage?.setImageResource(animalFrames[currentFrame])
        } catch (e: Exception) {
            // Si falla, paramos la animación
            return
        }
        h.postDelayed({ animateAnimal() }, 300)
    }
    
    private fun vibrateEnd() {
        // Vibración al final - siempre si está habilitado
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(300)
        }
    }
    
    private fun playSoundIfEnabled() {
        if (!Reminder.vibrates(this)) return
        playCompletionSound()
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
