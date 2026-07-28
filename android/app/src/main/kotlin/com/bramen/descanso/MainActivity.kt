package com.bramen.descanso

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        Reminder.ensurePermissions(this)
        Reminder.schedule(this)
        setContentView(TextView(this).apply {
            text = "Descanso activo.\n\nTe avisaré cada ${Reminder.INTERVAL_MIN} min.\nPuedes cerrar esta pantalla."
            textSize = 20f
            setPadding(60, 140, 60, 60)
        })
    }
}
