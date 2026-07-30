package com.bramen.descanso

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.time.LocalDate

class MainActivity : Activity() {

    // Colores para modo oscuro
    private val darkBg = 0xFF0A0A1A.toInt()
    private val darkCardBg = 0xFF16162E.toInt()
    private val darkTrack = 0xFF23234A.toInt()
    private val darkPrimary = 0xFFE6E6FF.toInt()
    private val darkMuted = 0xFF8C8CB0.toInt()
    private val darkAccent = 0xFF6E8CFF.toInt()
    private val darkGreen = 0xFF6CE08C.toInt()
    
    // Colores para modo claro
    private val lightBg = 0xFFF8F9FA.toInt()
    private val lightCardBg = 0xFFFFFFFF.toInt()
    private val lightTrack = 0xFFE0E0E0.toInt()
    private val lightPrimary = 0xFF1A1A1A.toInt()
    private val lightMuted = 0xFF666666.toInt()
    private val lightAccent = 0xFF4466CC.toInt()
    private val lightGreen = 0xFF2E8B57.toInt()
    
    // Colores actuales (se sobrescriben en onCreate; lateinit no aplica a Int)
    private var bg: Int = darkBg
    private var cardBg: Int = darkCardBg
    private var track: Int = darkTrack
    private var primary: Int = darkPrimary
    private var muted: Int = darkMuted
    private var accent: Int = darkAccent
    private var green: Int = darkGreen
    
    // Detector de modo oscuro
    private fun isDarkMode(): Boolean {
        val mode = themeModeSetting()
        return when (mode) {
            "light" -> false
            "dark" -> true
            else -> {
                // auto: detectar modo del sistema
                val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    private fun themeModeSetting(): String {
        return Reminder.themeMode(this)
    }
    
    private fun updateColors() {
        val dark = isDarkMode()
        bg = if (dark) darkBg else lightBg
        cardBg = if (dark) darkCardBg else lightCardBg
        track = if (dark) darkTrack else lightTrack
        primary = if (dark) darkPrimary else lightPrimary
        muted = if (dark) darkMuted else lightMuted
        accent = if (dark) darkAccent else lightAccent
        green = if (dark) darkGreen else lightGreen
    }
    
    private val counters = ArrayList<Pair<Mode, TextView>>()
    private val h = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        updateColors()
        Reminder.ensurePermissions(this)
        Reminder.ensureScheduled(this)
        val paused = Reminder.isPaused(this)
        counters.clear()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        // ============ HEADER ============
        col.addView(topBar())
        col.addView(space(dp(12)))
        
        col.addView(TextView(this).apply {
            text = "Descanso"
            textSize = 32f
            setTextColor(primary)
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        })
        col.addView(TextView(this).apply {
            text = "Cuida tus ojos · muévete · hidrátate"
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
        })
        col.addView(space(dp(24)))

        // ============ ESTADO (Pausado/Activo) ============
        col.addView(card(
            if (paused) "⏸ Pausado" else "● Activo",
            if (paused) muted else green,
            body(if (paused) "Los recordatorios están en pausa."
                 else "Te avisaré automáticamente,\naunque cierres la app.", 15, muted)))
        col.addView(space(dp(16)))

        // ============ PRÓXIMO DESCANSO ============
        val nextMode = Reminder.soonest(this)
        if (nextMode != null) {
            val nextTime = Reminder.nextAt(this, nextMode)
            val eta = if (nextTime > 0) eta(nextTime) else "pronto"
            col.addView(card("🎯 Próximo: ${nextMode.emoji} ${nextMode.title}", nextMode.tint,
                body("En: $eta", 18, primary, bold = true)))
            col.addView(space(dp(16)))
        }

        // ============ MODOS ACTIVOS ============
        val activos = Mode.values().filter { Reminder.isOn(this, it) }
        if (activos.isNotEmpty()) {
            val activeLabels = activos.joinToString(" · ") { "${it.emoji}" }
            col.addView(card("📋 Modos activos", accent,
                body(activeLabels, 16, primary)))
            col.addView(space(dp(16)))
        }

        // ============ ESTADÍSTICAS ============
        val todayCount = Reminder.todayCount(this)
        val totalCount = Reminder.totalCount(this)
        val streak = Reminder.streak(this)
        val weekCount = Reminder.weekDays(this).sum()
        
        val statsText = when {
            streak > 0 && todayCount > 0 -> "🔥 $streak días  •  Hoy: $todayCount  •  Semana: $weekCount  •  Total: $totalCount"
            streak > 0 -> "🔥 $streak días  •  Semana: $weekCount  •  Total: $totalCount"
            todayCount > 0 -> "Hoy: $todayCount  •  Semana: $weekCount  •  Total: $totalCount"
            else -> "Semana: $weekCount  •  Total: $totalCount"
        }
        
        col.addView(card("📊 Estadísticas", accent,
            body(statsText, 16, primary)))
        col.addView(space(dp(24)))

        // ============ BOTÓN PAUSAR/REAUDAR ============
        col.addView(pill(if (paused) "▶ Reanudar" else "⏸ Pausar") {
            if (Reminder.isPaused(this)) Reminder.resume(this) else Reminder.pause(this)
            recreate()
        })
        col.addView(space(dp(16)))

        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(col) })
    }

    // La cuenta regresiva solo corre con la app en pantalla: fuera de ella el
    // Handler seguiría despertando cada segundo sin que nadie lo vea.
    override fun onResume() { super.onResume(); tick() }
    override fun onPause() { super.onPause(); h.removeCallbacksAndMessages(null) }

    private fun tick() {
        for ((m, tv) in counters) tv.text = eta(Reminder.nextAt(this, m))
        h.postDelayed(::tick, 1000)
    }

    private fun eta(at: Long): String {
        if (Reminder.isPaused(this) || at == 0L) return "—"
        val s = (at - System.currentTimeMillis()) / 1000
        return when {
            s <= 0 -> "pronto"
            s >= 3600 -> "%d h %02d min".format(s / 3600, s % 3600 / 60)
            else -> "%d:%02d".format(s / 60, s % 60)
        }
    }

    private fun modeCard(m: Mode): View {
        val cd = body("—", 30, primary, bold = true)
        counters.add(m to cd)
        val cadencia = if (m.isDaily) {
            val at = Reminder.dailyMin(this, m)
            "Todos los días a las %02d:%02d".format(at / 60, at % 60)
        } else {
            "Cada ${Reminder.everyMin(this, m)} min · pausa de ${Reminder.breakSec(this, m)} s"
        }
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            addView(cd)
            addView(body(cadencia, 13, muted))
        }
        return card("${m.emoji}  ${m.title.uppercase()}", m.tint, inner)
    }

    // --- estadísticas ---

    private fun statsBody(): View {
        val dias = Reminder.weekDays(this)
        val hoyIdx = LocalDate.now().dayOfWeek.value - 1
        val nombres = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val max = (dias.maxOrNull() ?: 0).coerceAtLeast(1)
        val racha = Reminder.streak(this)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            addView(body("${Reminder.todayCount(this@MainActivity)} hoy", 30, primary, bold = true))
            addView(body("${dias.sum()} esta semana · ${Reminder.totalCount(this@MainActivity)} en total",
                         14, muted))
            addView(space(dp(10)))
            addView(body(
                if (racha > 0) "🔥 Racha de $racha ${if (racha == 1) "día" else "días"} seguidos"
                else "Sin racha todavía: haz un descanso hoy",
                15, if (racha > 0) green else muted, bold = racha > 0))
            addView(space(dp(12)))
            for (i in 0..6) addView(dayBar(nombres[i], dias[i], max, i == hoyIdx))
        }
    }

    /** Una fila del gráfico semanal: etiqueta, barra proporcional y conteo. */
    private fun dayBar(label: String, n: Int, max: Int, isToday: Boolean): View {
        val color = if (isToday) green else if (n > 0) accent else track
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                .apply { topMargin = dp(6) }
            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(if (isToday) primary else muted)
                layoutParams = LinearLayout.LayoutParams(dp(36), WRAP_CONTENT)
            })
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                background = GradientDrawable().apply {
                    cornerRadius = dp(5).toFloat(); setColor(track)
                }
                layoutParams = LinearLayout.LayoutParams(0, dp(10), 1f)
                addView(View(this@MainActivity).apply {
                    background = GradientDrawable().apply {
                        cornerRadius = dp(5).toFloat(); setColor(color)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        0, MATCH_PARENT, n.toFloat().coerceAtLeast(0.0001f))
                })
                addView(View(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0, MATCH_PARENT, (max - n).toFloat().coerceAtLeast(0.0001f))
                })
            })
            addView(TextView(this@MainActivity).apply {
                text = "$n"
                textSize = 12f
                gravity = Gravity.END
                setTextColor(if (n > 0) primary else muted)
                layoutParams = LinearLayout.LayoutParams(dp(28), WRAP_CONTENT)
            })
        }
    }

    // --- configuración ---

    private fun openSettings() {
        val vib = CheckBox(this).apply {
            text = "Vibrar al avisar"
            isChecked = Reminder.vibrates(this@MainActivity)
            setTextColor(primary)
        }
        val status = CheckBox(this).apply {
            text = "Notificación fija con la cuenta atrás"
            isChecked = Reminder.showStatus(this@MainActivity)
            setTextColor(primary)
        }
        
        // Selector de tema
        val themeRadioGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val currentTheme = themeModeSetting()
            
            fun createRadio(text: String, value: String): CheckBox {
                return CheckBox(this@MainActivity).apply {
                    this.text = text
                    isChecked = (currentTheme == value)
                    setTextColor(primary)
                }
            }
            
            addView(createRadio("🌙  Oscuro", "dark"))
            addView(space(dp(8)))
            addView(createRadio("☀️  Claro", "light"))
            addView(space(dp(8)))
            addView(createRadio("🔄  Automático (sistema)", "auto"))
        }
        
        // Selector de animal
        val animalRadioGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val currentAnimal = Reminder.animal(this@MainActivity)
            
            fun createAnimalRadio(text: String, value: String): CheckBox {
                return CheckBox(this@MainActivity).apply {
                    this.text = text
                    isChecked = (currentAnimal == value)
                    setTextColor(primary)
                }
            }
            
            addView(createAnimalRadio("🐕 Perro", "perro"))
            addView(space(dp(8)))
            addView(createAnimalRadio("🐈 Gato", "gato"))
        }
        
        val qFrom = hourPicker(Reminder.quietFrom(this))
        val qTo = hourPicker(Reminder.quietTo(this))
        val tips = EditText(this).apply {
            setText(Reminder.customTips(this@MainActivity))
            hint = "Un tip por línea"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            maxLines = 10
            setBackgroundColor(cardBg)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setTextColor(primary)
            setHintTextColor(muted)
            // Estilo de borde
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(cardBg)
                setStroke(dp(1), track)
            }
        }

        val on = HashMap<Mode, CheckBox>()
        val every = HashMap<Mode, NumberPicker>()
        val secs = HashMap<Mode, NumberPicker>()
        val times = HashMap<Mode, TimePicker>()

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(24))

            // ============ SECCIÓN: GENERAL ============
            addView(sectionHeader("🎨  Apariencia", accent))
            addView(label("Tema de la app"))
            addView(themeRadioGroup)
            addView(space(dp(16)))
            addView(label("Animal de la animación"))
            addView(animalRadioGroup)
            addView(space(dp(16)))
            addView(divider())
            addView(space(dp(16)))

            // ============ SECCIÓN: NOTIFICACIONES ============
            addView(sectionHeader("🎵  Notificaciones", accent))
            
            addView(vib)
            addView(status)
            addView(space(dp(16)))
            addView(divider())
            addView(space(dp(16)))

            // ============ SECCIÓN: NO MOLESTAR ============
            addView(sectionHeader("🌙  No molestar", accent))
            addView(label("Horario de silencio"))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(16))
                
                addView(TextView(this@MainActivity).apply {
                    text = "De"
                    textSize = 14f
                    setTextColor(muted)
                    setPadding(dp(8), 0, dp(8), 0)
                    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                })
                addView(qFrom.apply { 
                    layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                })
                addView(TextView(this@MainActivity).apply {
                    text = "a"
                    textSize = 14f
                    setTextColor(muted)
                    setPadding(dp(8), 0, dp(8), 0)
                    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                })
                addView(qTo.apply { 
                    layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                })
            })
            addView(divider())
            addView(space(dp(16)))

            // ============ SECCIÓN: MENSAJES PERSONALIZADOS ============
            addView(sectionHeader("✏️  Mensajes personalizados", accent))
            addView(label("Tips propios (uno por línea)"))
            addView(tips)
            addView(space(dp(16)))
            addView(divider())
            addView(space(dp(16)))

            // ============ SECCIÓN: MODOS DE RECORDATORIO ============
            addView(sectionHeader("📊  Modos de recordatorio", accent))
            addView(label("Configura cada modo individualmente"))
            addView(space(dp(12)))

            for (m in Mode.values()) {
                // Card para cada modo
                addView(modeSettingsCard(m, on, every, secs, times))
                addView(space(dp(16)))
            }
            
            // ============ BOTÓN GUARDAR AL FINAL ============
            addView(divider())
            addView(space(dp(24)))
            addView(pill("✓ Guardar configuración") {
                // Validar datos
                if (!validateSettings(on, every, secs)) {
                    return@pill
                }
                
                // Obtener tema seleccionado (los índices impares son los
                // spacers entre checkboxes: dark=0, spacer=1, light=2, ...)
                val selectedTheme = when {
                    (themeRadioGroup.getChildAt(0) as CheckBox).isChecked -> "dark"
                    (themeRadioGroup.getChildAt(2) as CheckBox).isChecked -> "light"
                    else -> "auto"
                }

                // Obtener animal seleccionado
                val selectedAnimal = when {
                    (animalRadioGroup.getChildAt(0) as CheckBox).isChecked -> "perro"
                    else -> "gato"
                }

                // this dentro de este lambda hereda el receiver del LinearLayout.apply{}
                // que lo envuelve (el onClick no tiene receiver propio) — hay que calificar
                // explícitamente this@MainActivity para llegar al Context/Activity real.
                Reminder.saveGlobal(this@MainActivity, vib.isChecked, status.isChecked,
                    qFrom.value, qTo.value, tips.text.toString(), selectedTheme, selectedAnimal)
                for (m in Mode.values()) {
                    val atMin = times[m]?.let { it.hour * 60 + it.minute }
                        ?: Reminder.dailyMin(this@MainActivity, m)
                    Reminder.saveMode(this@MainActivity, m,
                        on[m]?.isChecked ?: Reminder.isOn(this@MainActivity, m),
                        every[m]?.value ?: Reminder.everyMin(this@MainActivity, m),
                        secs[m]?.value ?: Reminder.breakSec(this@MainActivity, m),
                        atMin)
                }
                // Reagenda para que los ajustes nuevos apliquen ya
                if (!Reminder.isPaused(this@MainActivity)) Reminder.scheduleAll(this@MainActivity)
                else Reminder.status(this@MainActivity)

                // Feedback al usuario
                Toast.makeText(this@MainActivity, "✓ Configuración guardada", Toast.LENGTH_SHORT).show()
                recreate()
            })
            addView(space(dp(16)))
        }

        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("⚙  Configuración")
            .setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Crea una tarjeta de configuración para un modo específico */
    private fun modeSettingsCard(
        m: Mode, 
        on: HashMap<Mode, CheckBox>,
        every: HashMap<Mode, NumberPicker>,
        secs: HashMap<Mode, NumberPicker>,
        times: HashMap<Mode, TimePicker>
    ): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(cardBg)
            }
            setPadding(dp(20), dp(16), dp(20), dp(16))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        
        // Header del modo
        card.addView(TextView(this).apply {
            text = "${m.emoji}  ${m.title}"
            textSize = 18f
            setTextColor(m.tint)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })
        
        // Checkbox de activado
        val chk = CheckBox(this).apply {
            text = "Activado"
            isChecked = Reminder.isOn(this@MainActivity, m)
            setTextColor(primary)
        }
        on[m] = chk
        card.addView(chk)
        card.addView(space(dp(16)))
        
        // Configuración específica del modo
        if (m.isDaily) {
            card.addView(label("Hora del recordatorio"))
            val tp = TimePicker(this).apply {
                setIs24HourView(true)
                hour = Reminder.dailyMin(this@MainActivity, m) / 60
                minute = Reminder.dailyMin(this@MainActivity, m) % 60
                setBackgroundColor(cardBg)
                setPadding(dp(8), dp(8), dp(8), dp(8))
            }
            times[m] = tp
            card.addView(tp)
            card.addView(space(dp(8)))
        } else {
            card.addView(label("Avisarme cada (minutos)"))
            val np = numberPicker(1, 240, Reminder.everyMin(this@MainActivity, m))
            every[m] = np
            card.addView(np)
            card.addView(space(dp(8)))
        }
        
        // Duración de la pausa
        card.addView(label("Duración de la pausa"))
        val sp = numberPicker(5, 600, Reminder.breakSec(this@MainActivity, m))
        secs[m] = sp
        card.addView(sp)
        
        return card
    }

    /** Valida que los datos de configuración sean válidos */
    private fun validateSettings(
        on: HashMap<Mode, CheckBox>,
        every: HashMap<Mode, NumberPicker>,
        secs: HashMap<Mode, NumberPicker>
    ): Boolean {
        for (m in Mode.values()) {
            if (on[m]?.isChecked == true) {
                // Si está activado, la duración debe ser > 0
                val duration = secs[m]?.value ?: Reminder.breakSec(this, m)
                if (duration <= 0) {
                    Toast.makeText(this, "La duración debe ser mayor a 0 segundos", Toast.LENGTH_LONG).show()
                    return false
                }
                
                // Para modos no diarios, el intervalo debe ser > 0
                if (!m.isDaily) {
                    val interval = every[m]?.value ?: Reminder.everyMin(this, m)
                    if (interval <= 0) {
                        Toast.makeText(this, "El intervalo debe ser mayor a 0 minutos", Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            }
        }
        return true
    }

    /** Crea un divisor horizontal */
    private fun divider(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(1))
        setBackgroundColor(track)
    }

    /** Crea un header de sección */
    private fun sectionHeader(text: String, color: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(color)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(8))
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    private fun hourPicker(value: Int) = numberPicker(0, 23, value).apply {
        // Estilo específico para el hora picker
        setBackgroundColor(cardBg)
    }

    private fun numberPicker(min: Int, max: Int, value: Int) = NumberPicker(this).apply {
        minValue = min
        maxValue = max
        this.value = value.coerceIn(min, max)
        // Dentro de un ScrollView el gesto lo robaría el scroll y no se podría girar.
        setOnTouchListener { v, _ -> v.parent.requestDisallowInterceptTouchEvent(true); false }
        // Estilo para que se vea mejor
        setBackgroundColor(cardBg)
        setPadding(dp(8), dp(8), dp(8), dp(8))
    }

    private fun section(t: String) = text(t, 16, accent, bold = true).apply {
        setPadding(0, dp(20), 0, dp(4))
    }

    private fun label(t: String) = text(t, 13, muted).apply { setPadding(0, dp(10), 0, 0) }

    // --- helpers de UI (sin XML ni dependencias) ---

    private fun topBar() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        addView(View(this@MainActivity).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(1), 1f)
        })
        addView(TextView(this@MainActivity).apply {
            text = "⚙"
            textSize = 20f
            setTextColor(muted)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat(); setColor(cardBg)
            }
            setPadding(dp(14), dp(8), dp(14), dp(10))
            contentDescription = "Configuración"
            setOnClickListener { openSettings() }
        })
    }

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
