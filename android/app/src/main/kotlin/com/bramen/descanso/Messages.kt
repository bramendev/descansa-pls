package com.bramen.descanso

object Messages {
    private val pool = listOf(
        "Mira a lo lejos 20 segundos para relajar la vista.",
        "Parpadea varias veces: lubrica tus ojos.",
        "Estira el cuello y los hombros.",
        "Ponte de pie y camina un poco.",
        "Respira hondo: 4s inspira, 4s exhala.",
        "Toma un sorbo de agua.",
        "Relaja la mandíbula y baja los hombros.",
        "Enfoca un punto lejano por la ventana.",
        "Gira los tobillos y estira las piernas.",
        "Un pequeño descanso ahora es energía después.",
    )
    fun random() = pool.random()
}
