# Descanso Visual

Recordatorio automático de pausas con pantalla simulada de bloqueo.
Cada 20 min → descanso visual. Cada 60 min → pausa activa con ejercicios.
Avisa hora de almuerzo (12:00) y de dormir (22:30).

## Características

- Pantalla de bloqueo simulada con reloj, fecha y ASCII art animado (perro/gato)
- Estadísticas de pausas (hoy/total) mostradas en cada pantalla
- Música lo-fi opcional durante cada descanso
- 4 modos: visual, activo, almuerzo, dormir
- Todo configurable via JSON

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

Requiere: `python3` + `gtk3` (PyGObject) + `ffplay` (para música).

## Windows

1. Copiar `windows/` a `C:\Users\tuUsuario\scripts\`
2. **Auto-inicio:** `Win+R` → `shell:startup` → acceso directo a `descanso.bat`
3. O manual: ejecutar `descanso.bat`

## Configuración

Editar `~/.config/descanso-visual/config.json`:

```json
{
  "visual_interval_min": 20,
  "visual_duration_sec": 30,
  "active_interval_min": 60,
  "active_duration_sec": 180,
  "lunch_time": "12:00",
  "sleep_time": "22:30",
  "music_url": "https://streams.fluxfm.de/lofi/mp3-128/",
  "animal": "perro"
}
```

`animal`: `perro` o `gato`.
`music_url`: URL de stream o vacío `""` para desactivar música.

## Modos de pantalla

| Modo | Cuándo | Qué muestra | Duración |
|---|---|---|---|
| Visual | Cada 20 min | Reloj, animal animado, cuenta atrás 30s, curiosidad, stats | 30s |
| Activo | Cada 60 min | Reloj, animal, cuenta atrás 3 min, ejercicio, stats | 3 min |
| Almuerzo | 12:00 | Cuenco de comida, sugerencia, stats, sin temporizador | auto cierra 10 min |
| Dormir | 22:30 | Luna, tip de sueño, stats, sin temporizador | auto cierra 10 min |

## Estadísticas

Se guardan en `~/.config/descanso-visual/stats.json`.
Cada pantalla muestra el resumen del día:

```
3 visuales  ·  1 activas  ·  1 almuerzos
```

El scheduler también muestra stats en los logs tras cada pausa.

## ASCII Art

Clásicos de internet:
- **Perro**: perfil con cola animada
- **Gato**: Felix Lee dormido con parpadeo
