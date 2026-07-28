package com.bramen.descanso

import android.app.Activity
import android.app.AlertDialog
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
import java.time.LocalDate

class MainActivity : Activity() {

    private val bg = 0xFF0A0A1A.toInt()
    private val cardBg = 0xFF16162E.toInt()
    private val track = 0xFF23234A.toInt()
    private val primary = 0xFFE6E6FF.toInt()
    private val muted = 0xFF8C8CB0.toInt()
    private val accent = 0xFF6E8CFF.toInt()
    private val green = 0xFF6CE08C.toInt()

    private val counters = ArrayList<Pair<Mode, TextView>>()
    private val h = Handler(Looper.getMainLooper())

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        Reminder.ensurePermissions(this)
        Reminder.ensureScheduled(this)
        val paused = Reminder.isPaused(this)
        counters.clear()

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(24), dp(32), dp(24), dp(32))
        }

        col.addView(topBar())
        col.addView(text("Descanso", 32, primary, Gravity.CENTER, bold = true))
        col.addView(text("Cuida tus ojos · muévete · hidrátate", 14, muted, Gravity.CENTER))
        col.addView(space(dp(24)))

        col.addView(card(
            if (paused) "⏸ Pausado" else "● Activo",
            if (paused) muted else green,
            body(if (paused) "Los recordatorios están en pausa."
                 else "Te avisaré automáticamente,\naunque cierres la app.", 15, muted)))
        col.addView(space(dp(14)))

        val activos = Mode.values().filter { Reminder.isOn(this, it) }
        if (activos.isEmpty()) {
            col.addView(card("SIN RECORDATORIOS", muted,
                body("Activa alguno desde ⚙ Configuración.", 15, muted)))
            col.addView(space(dp(14)))
        }
        for (m in activos) {
            col.addView(modeCard(m))
            col.addView(space(dp(14)))
        }

        col.addView(card("ESTADÍSTICAS", accent, statsBody()))
        col.addView(space(dp(24)))

        col.addView(pill(if (paused) "Reanudar" else "Pausar recordatorios") {
            if (Reminder.isPaused(this)) Reminder.resume(this) else Reminder.pause(this)
            recreate()
        })

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
            text = "Vibrar al avisar"; isChecked = Reminder.vibrates(this@MainActivity)
        }
        val status = CheckBox(this).apply {
            text = "Notificación fija con la cuenta atrás"
            isChecked = Reminder.showStatus(this@MainActivity)
        }
        val qFrom = hourPicker(Reminder.quietFrom(this))
        val qTo = hourPicker(Reminder.quietTo(this))
        val tips = EditText(this).apply {
            setText(Reminder.customTips(this@MainActivity))
            hint = "Un tip por línea"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        val on = HashMap<Mode, CheckBox>()
        val every = HashMap<Mode, NumberPicker>()
        val secs = HashMap<Mode, NumberPicker>()
        val times = HashMap<Mode, TimePicker>()

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)

            addView(section("General"))
            addView(vib)
            addView(status)
            addView(label("No molestar desde / hasta (hora)"))
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(qFrom)
                addView(qTo)
            })
            addView(label("Tips propios (uno por línea)"))
            addView(tips)

            for (m in Mode.values()) {
                addView(section("${m.emoji}  ${m.title}"))
                val chk = CheckBox(this@MainActivity).apply {
                    text = "Activado"; isChecked = Reminder.isOn(this@MainActivity, m)
                }
                on[m] = chk
                addView(chk)
                if (m.isDaily) {
                    val at = Reminder.dailyMin(this@MainActivity, m)
                    val tp = TimePicker(this@MainActivity).apply {
                        setIs24HourView(true)
                        hour = at / 60
                        minute = at % 60
                    }
                    times[m] = tp
                    addView(tp)
                } else {
                    addView(label("Avisarme cada (minutos)"))
                    val np = numberPicker(1, 240, Reminder.everyMin(this@MainActivity, m))
                    every[m] = np
                    addView(np)
                }
                addView(label("Duración de la pausa (segundos)"))
                val sp = numberPicker(5, 600, Reminder.breakSec(this@MainActivity, m))
                secs[m] = sp
                addView(sp)
            }
        }

        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setTitle("Configuración")
            .setView(ScrollView(this).apply { addView(form) })
            .setPositiveButton("Guardar") { _, _ ->
                Reminder.saveGlobal(this, vib.isChecked, status.isChecked,
                    qFrom.value, qTo.value, tips.text.toString())
                for (m in Mode.values()) {
                    val atMin = times[m]?.let { it.hour * 60 + it.minute }
                        ?: Reminder.dailyMin(this, m)
                    Reminder.saveMode(this, m,
                        on[m]?.isChecked ?: Reminder.isOn(this, m),
                        every[m]?.value ?: Reminder.everyMin(this, m),
                        secs[m]?.value ?: Reminder.breakSec(this, m),
                        atMin)
                }
                // Reagenda para que los ajustes nuevos apliquen ya, no en el ciclo siguiente.
                if (!Reminder.isPaused(this)) Reminder.scheduleAll(this) else Reminder.status(this)
                recreate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun hourPicker(value: Int) = numberPicker(0, 23, value)

    private fun numberPicker(min: Int, max: Int, value: Int) = NumberPicker(this).apply {
        minValue = min
        maxValue = max
        this.value = value.coerceIn(min, max)
        // Dentro de un ScrollView el gesto lo robaría el scroll y no se podría girar.
        setOnTouchListener { v, _ -> v.parent.requestDisallowInterceptTouchEvent(true); false }
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
