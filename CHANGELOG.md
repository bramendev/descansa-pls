# Changelog

Todas las versiones publicadas. Cada tag `v*` genera un
[Release](https://github.com/bramendev/descansa-pls/releases) con el APK, y
las notas salen de la sección correspondiente de este archivo.

## v1.8 — 2026-07-30

### Arreglado (Android)

- La tarjeta "Próximo descanso" del dashboard mostraba la cuenta atrás
  congelada: `tick()` refresca cada segundo lo que haya en `counters`, pero
  esa lista solo la llenaba `modeCard()`, una función que `onCreate` nunca
  llama. `counters` pasa de `ArrayList<Pair<Mode, TextView>>` a
  `ArrayList<() -> Unit>` (closures de actualización) para que cualquier
  vista pueda registrarse, no solo las de `modeCard()`.

## v1.7 — 2026-07-30

El APK de v1.6 no llegó a compilar en CI (varios errores de Kotlin en el
diálogo de configuración); esta versión los arregla y de paso trae widget
flotante y sonido ambiente nuevos en la versión de escritorio.

### Arreglado (Android)

- `lateinit var` sobre `Int` no compila en Kotlin; los colores de tema pasan
  a `var` con valor por defecto.
- El botón "Guardar" leía `this` sin calificar dentro de un lambda anidado en
  un `LinearLayout.apply{}`, así que resolvía al `LinearLayout` en vez de la
  `Activity` — de ahí los "Context expected, LinearLayout found". Se califica
  `this@MainActivity` donde corresponde.
- `.children` requiere la dependencia `core-ktx`, que el proyecto no tiene;
  se reemplaza por `getChildAt()`.
- El selector de tema leía el checkbox equivocado: había un `View` espaciador
  entre cada opción y el índice no lo tenía en cuenta, así que tocar "Guardar"
  con "Claro" seleccionado habría lanzado `ClassCastException` en runtime.

### Nuevo (Escritorio)

- **Widget flotante**: burbuja cuadrada de bordes redondeados, arrastrable y
  siempre visible, con cuenta regresiva y barra de progreso para los
  recordatorios visual/activo/agua. Se lanza sola junto con `descanso`
  (desactivable con `"widget": false` en la config).
- **Sonido ambiente local**: reemplaza el streaming de radio lo-fi (`music_url`,
  a veces con gente hablando encima) por lluvia u olas — audio CC0 bundleado
  en `sounds/`, elegido al azar en cada pausa por defecto
  (`"ambient_sound": "aleatorio"`).

### Cambios técnicos

- `descanso` escribe `~/.config/descanso-visual/state.json` en cada ciclo del
  loop principal para que el widget pueda leer los próximos descansos sin
  acoplarse al proceso.

## v1.6 — 2026-07-30

Mejora de usabilidad y experiencia en la app Android: notificaciones al final
de las pausas, configuración rediseñada, modo oscuro, animación de animal y temas personalizables.

### Mejoras

- **Alertas al finalizar la pausa**: vibración y sonido cuando el cronómetro
  llega a cero, con cambio visual (timer en verde, "¡Listo! Puedes continuar").
- **Animación de animal**: pantalla de pausa muestra perro o gato caminando en
  frames animados (como en escritorio). Seleccionable en Configuración → Apariencia.
- **Configuración reorganizada**: diálogo de ajustes con secciones claras
  (Apariencia, Notificaciones, No molestar, Mensajes, Modos).
- **Soporte para modo oscuro**: sigue el modo del sistema automáticamente,
  con opción para forzar claro/oscuro/auto en Configuración.
- **Botón guardar al final**: ahora está al final del diálogo de ajustes.
- **Pantalla inicial simplificada**: diseño más limpio con secciones:
  estado, próximo descanso, modos activos, estadísticas.
- **Estadísticas en pantalla de pausa**: muestra racha, hoy, semana, total.
- **Validación de datos**: comprueba duración e intervalos > 0.
- **Feedback al guardar**: toast "✓ Configuración guardada".

### Cambios técnicos

- El modo oscuro se resuelve por código (`isDarkMode()`/`updateColors()` en
  `MainActivity`/`BreakActivity`, según `Configuration.UI_MODE_NIGHT_MASK` o
  la preferencia guardada), no vía tema de sistema: el manifest sigue fijo en
  `Theme.Material.Light.NoActionBar`. Un intento anterior con
  `values-night/themes.xml` se descartó por referenciar temas inexistentes.
- `Reminder`: añade `themeMode()`, `animal()`; `saveGlobal()` actualizada.
- `BreakActivity`: animación de frames, `vibrateEnd()`, estadísticas, colores adaptados.
- `MainActivity`: configuración reorganizada, pantalla inicial simplificada.
- **Recursos**: añadidos `dog_frame0-3.png` y `cat_frame0-3.png`.

## v1.4 — 2026-07-27

Release grande de la app Android: cinco tipos de recordatorio, configuración
dentro de la app, estadísticas, icono propio y un repaso de seguridad.

### Nuevo

- **Cinco modos de recordatorio**, como en la versión de escritorio: descanso
  visual, hidratación, pausa activa, hora de almorzar y hora de dormir. Cada
  uno se activa por separado, con su intervalo, su duración y sus tips. Los dos
  últimos avisan a una hora fija del día en vez de cada N minutos.
- **Configuración en la app** (botón ⚙): activar o desactivar cada modo,
  intervalos, duraciones, horas fijas, vibración y notificación de estado.
- **Horario de silencio** ("no molestar", 22:00–8:00 por defecto). Los avisos
  que caerían dentro se corren automáticamente al final de la franja, así que
  el teléfono ya no despierta a nadie de madrugada. La hora de dormir es la
  excepción a propósito: si no, no sonaría nunca.
- **Estadísticas**: descansos de hoy, de la semana y totales, con un gráfico de
  barras de los últimos siete días y **racha de días seguidos**.
- **Tips propios**: se escriben en la configuración, uno por línea, y se
  mezclan con los que ya trae cada modo.
- **Notificación fija** con cuenta atrás en vivo al próximo descanso. La lleva
  el cronómetro del sistema, así que no consume batería refrescándose. Se puede
  desactivar.
- **Icono propio**: icono adaptativo (con variante monocroma para los temas de
  Android 13+) y silueta para la barra de estado, ambos vectoriales.

### Seguridad

- `BootReceiver` está exportado por obligación, pero ahora exige que quien
  emita el broadcast tenga `RECEIVE_BOOT_COMPLETED` y además comprueba la
  acción del intent antes de hacer nada.
- El workflow ya no interpola `${{ github.ref_name }}` dentro de un `run:`: un
  tag o rama con nombre malicioso podía ejecutar comandos en el runner.
- `softprops/action-gh-release` queda fijado a un SHA en vez de al tag móvil
  `v2`. Es una acción de terceros que corre con permiso de escritura sobre el
  repositorio.
- `weather_city` de la config de escritorio se escapa antes de meterla en la
  URL de wttr.in, así que ya no puede añadir parámetros ni salirse de la ruta.
- `android:usesCleartextTraffic="false"`: la app no hace red, y ahora tampoco
  podría hacerla en claro.

### Cambios

- `versionCode` lo genera el CI a partir del número de ejecución, en vez de
  quedarse clavado en 1 en todas las releases.
- La tarjeta de frecuencia desaparece: cada modo ya muestra su propia cadencia.
- La cuenta atrás pasa a formato `1 h 05 min` cuando falta más de una hora.

### Pendiente

- El APK se sigue firmando con la clave de depuración de Android, que es
  pública. Ver la nota de seguridad en el README.

## v1.3 — 2026-07-27

- **Arreglado**: tras una pausa activa, el siguiente descanso visual se agendaba
  a +60 min (el intervalo de la pausa activa) en vez de +20, dejando una hora
  entera sin cumplir la regla 20-20-20.
- **Arreglado**: la cuenta atrás de la pantalla de inicio de Android despertaba
  un `Handler` cada segundo indefinidamente con la app en segundo plano; ahora
  solo corre entre `onResume` y `onPause`.
- `visual_duration_sec: 0` en la config de escritorio ya no revienta con
  `ZeroDivisionError`.
- Limpieza de código muerto y `.gitignore` para los directorios de Gradle.

## v1.2 — 2026-07-27

- Tips aleatorios en la pantalla de pausa de Android.
- Botones de posponer 5 minutos y saltar.
- Vibración al avisar.
- Pausar y reanudar los recordatorios.

## v1.1 — 2026-07-27

- Pantalla de inicio de Android con tarjetas, cuenta regresiva en vivo y
  descansos del día.

## v1.0 — 2026-07-27

- Primera versión con app Android en Kotlin y build de APK por versión en
  GitHub Actions.
- Versión de escritorio en Python + tkinter: cinco modos, ASCII art animado,
  estadísticas, clima, música, mensajes personalizados e i18n ES/EN.
