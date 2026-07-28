package com.bramen.descanso

import android.content.Context

object Messages {
    private val pools = mapOf(
        Mode.VISUAL to listOf(
            "Mira a lo lejos 20 segundos para relajar la vista.",
            "Parpadea varias veces: lubrica tus ojos.",
            "Enfoca un punto lejano por la ventana.",
            "Cierra los ojos y cuenta hasta veinte.",
            "Relaja las cejas y la frente.",
        ),
        Mode.AGUA to listOf(
            "Toma un vaso de agua.",
            "Hidrátate, tu cuerpo lo necesita.",
            "Tu cerebro es 75% agua. ¡Hidrátalo!",
            "Beber agua mejora tu concentración.",
            "Bebe despacio, disfruta cada sorbo.",
        ),
        Mode.ACTIVA to listOf(
            "Estira los brazos hacia arriba — 15 segundos.",
            "Gira los hombros hacia atrás — 10 repeticiones.",
            "Ponte de pie y camina un poco.",
            "Gira los tobillos y estira las piernas.",
            "Abre el pecho con los brazos atrás — 15 segundos.",
            "Relaja la mandíbula y baja los hombros.",
        ),
        Mode.ALMUERZO to listOf(
            "Aléjate de la pantalla mientras comes.",
            "Tómate mínimo 20 minutos, sin prisas.",
            "Incluye proteínas y verduras en tu plato.",
            "Come algo nutritivo y equilibrado.",
        ),
        Mode.DORMIR to listOf(
            "Apaga las pantallas 30 min antes de dormir.",
            "Prepara tu habitación: oscura, fresca y silenciosa.",
            "Evita cafeína a estas horas.",
            "Respira 4-7-8 cinco veces.",
            "Lee un libro antes de dormir.",
        ),
    )

    /** Tip del modo, más los que el usuario haya escrito en la configuración. */
    fun random(ctx: Context, m: Mode): String {
        val custom = Reminder.customTips(ctx)
            .lines().map { it.trim() }.filter { it.isNotEmpty() }
        return ((pools[m] ?: emptyList()) + custom).random()
    }
}
