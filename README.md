# Descanso Visual

Recordatorio automático de pausas con pantalla simulada de bloqueo.

## Características

- **4 modos + agua**: visual (20 min), activo (60 min), almuerzo (12:00), dormir (22:30), agua (30 min)
- **ASCII art animado**: perro o gato con animaciones
- **Teclas de control**: Espacio para saltar, Z para snooze 5 min
- **Sonido**: beep al iniciar cada pausa
- **Tema claro/oscuro**: auto (según hora) o manual
- **Estadísticas**: hoy y semana, mostradas en pantalla y logs
- **Mensajes personalizados**: edita `messages.json` para agregar tus propios textos
- **Clima**: muestra el clima actual (configura `weather_city`)
- **Citas motivacionales**: mezcladas con curiosidades y ejercicios
- **Música lo-fi**: opcional durante las pausas

## Linux

```bash
# iniciar ahora
systemctl --user start descanso

# auto-inicio al prender el PC
systemctl --user enable descanso

# ver logs
journalctl --user -u descanso -f

# ejecutar manualmente (sin systemd)
~/.local/bin/descanso
```

Requiere: `python3` + `gtk3` (PyGObject) + `ffplay` (para música y sonido).

## Windows

### Opcion A: .exe (recomendado)

1. Ejecutar `windows\build.bat` (instala ps2exe y compila los .exe)
2. **Auto-inicio:** `Win+R` → `shell:startup` → acceso directo a `windows\descanso.exe`
3. O ejecutar `windows\descanso.exe` manualmente

### Opcion B: PowerShell scripts

1. Copiar `windows/` a `C:\Users\tuUsuario\scripts\`
2. **Auto-inicio:** `Win+R` → `shell:startup` → acceso directo a `descanso.bat`
3. O manual: ejecutar `descanso.bat`

## Controles en pantalla

| Tecla | Acción |
|---|---|
| `Espacio` | Saltar pausa actual |
| `Z` | Snooze: pospone la próxima pausa 5 min |
| `Enter` | Desbloquear (cuando el timer termina) |
| `Escape` | Cerrar (cuando el timer termina o en modos sin timer) |

## Configuración

Editar `~/.config/descanso-visual/config.json`:

```json
{
  "visual_interval_min": 20,
  "visual_duration_sec": 30,
  "active_interval_min": 60,
  "active_duration_sec": 180,
  "water_interval_min": 30,
  "water_duration_sec": 10,
  "lunch_time": "12:00",
  "sleep_time": "22:30",
  "music_url": "https://streams.fluxfm.de/lofi/mp3-128/",
  "animal": "perro",
  "theme": "dark",
  "weather_city": "",
  "sound": true
}
```

| Campo | Descripción | Default |
|---|---|---|
| `visual_interval_min` | Minutos entre pausas visuales | 20 |
| `visual_duration_sec` | Segundos de pausa visual | 30 |
| `active_interval_min` | Minutos entre pausas activas | 60 |
| `active_duration_sec` | Segundos de pausa activa | 180 |
| `water_interval_min` | Minutos entre recordatorios de agua | 30 |
| `water_duration_sec` | Segundos del recordatorio de agua | 10 |
| `lunch_time` | Hora de almuerzo (HH:MM) | 12:00 |
| `sleep_time` | Hora de dormir (HH:MM) | 22:30 |
| `music_url` | URL de música lo-fi (vacío = desactivar) | FluxFM |
| `animal` | `perro` o `gato` | perro |
| `theme` | `dark`, `light`, o `auto` | dark |
| `weather_city` | Ciudad para el clima (vacío = desactivar) | vacío |
| `sound` | Sonido al iniciar pausa | true |

## Mensajes personalizados

Editar `~/.config/descanso-visual/messages.json`:

```json
{
  "curiosidades": ["Mi dato curioso 1", "Mi dato curioso 2"],
  "ejercicios": ["Mi ejercicio 1"],
  "motivacionales": ["Mi frase motivacional"],
  "comidas": ["Mi consejo de comida"],
  "dormir": ["Mi tip para dormir"],
  "agua": ["Mi mensaje de hidratación"]
}
```

Los mensajes personalizados se mezclan con los incorporados.

## Modos de pantalla

| Modo | Cuándo | Qué muestra | Duración |
|---|---|---|---|
| Visual | Cada 20 min | Reloj, animal, cuenta atrás, curiosidad/motivacional, clima, stats | 30s |
| Activo | Cada 60 min | Reloj, animal, cuenta atrás, ejercicio/motivacional, clima, stats | 3 min |
| Agua | Cada 30 min | Reloj, gota de agua, mensaje hidratación, clima, stats | 10s |
| Almuerzo | 12:00 | Reloj, cuenco, sugerencia comida, clima, stats | auto 10 min |
| Dormir | 22:30 | Reloj, luna, tip sueño, clima, stats | auto 10 min |

## Estadísticas

Se guardan en `~/.config/descanso-visual/stats.json`.
Cada pantalla muestra:
- Contador del día: "👁 3 visuales  ·  🏃 1 activas  ·  💧 2 aguas"
- Contador semanal: "Semana: 👁 15 visuales  ·  🏃 4 activas"

El scheduler también muestra stats en los logs tras cada pausa.

## ASCII Art

Clásicos de internet:
- **Perro**: perfil con cola animada
- **Gato**: Felix Lee dormido con parpadeo

## Archivos de configuración

| Archivo | Ubicación |
|---|---|
| Configuración | `~/.config/descanso-visual/config.json` |
| Mensajes custom | `~/.config/descanso-visual/messages.json` |
| Estadísticas | `~/.config/descanso-visual/stats.json` |
| Snooze flag | `~/.config/descanso-visual/snooze_until` |
| Cache clima | `~/.config/descanso-visual/weather_cache` |
