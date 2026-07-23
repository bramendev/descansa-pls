# Descanso Visual

Recordatorio automático de pausas con pantalla simulada de bloqueo.
Un solo código **Python + tkinter** para Linux y Windows.

## Características

- **5 modos**: visual (20 min), activo (60 min), agua (30 min), almuerzo (12:00), dormir (22:30)
- **ASCII art animado**: perro o gato con animaciones
- **Teclas**: Espacio saltar, Z snooze 5 min, Escape cerrar, Enter desbloquear
- **Sonido**: beep al iniciar cada pausa (ffplay o winsound)
- **Música lo-fi**: opcional durante las pausas
- **Tema claro/oscuro**: auto (según hora) o manual
- **Estadísticas**: hoy y semana, mostradas en pantalla y logs
- **Clima**: muestra el clima actual (configura `weather_city`)
- **Mensajes personalizados**: edita `messages.json`

## Linux

```bash
# instalar
cp descanso ~/.local/bin/
cp pantalla-descanso ~/.local/bin/

# iniciar ahora
systemctl --user start descanso

# auto-inicio
systemctl --user enable descanso

# logs
journalctl --user -u descanso -f

# ejecutar manual
~/.local/bin/descanso
```

Requiere: `python3` + `tkinter` (viene con Python) + `ffplay` (para sonido/música, opcional).

## Windows

### Opción A: .exe (recomendado)

1. Ejecutar `windows\build.bat` — convierte los scripts a .exe con ps2exe
2. Copiar `windows\descanso.exe` y `windows\pantalla-descanso.exe` a `C:\Users\tuUsuario\scripts\`
3. **Auto-inicio:** `Win+R` → `shell:startup` → acceso directo a `descanso.exe`

### Opción B: Python directo

1. Instalar Python 3 (con tkinter incluido)
2. Ejecutar `python descanso` desde la terminal

## Instalación desde cero

```bash
git clone git@github.com:bramendev/descansa-pls.git
cd descansa-pls
cp descanso ~/.local/bin/
cp pantalla-descanso ~/.local/bin/
# Linux: copiar servicio systemd
cp linux/descanso.service ~/.config/systemd/user/
systemctl --user daemon-reload
```

## Controles en pantalla

| Tecla | Acción |
|---|---|
| `Espacio` | Saltar pausa actual |
| `Z` | Snooze: pospone la próxima pausa 5 min |
| `Enter` | Desbloquear (cuando el timer termina) |
| `Escape` | Cerrar (cuando el timer termina o modos sin timer) |

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

## Modos de pantalla

| Modo | Cuándo | Duración |
|---|---|---|
| Visual | Cada 20 min | 30s |
| Activo | Cada 60 min | 3 min |
| Agua | Cada 30 min | 10s |
| Almuerzo | 12:00 | 10 min auto |
| Dormir | 22:30 | 10 min auto |

## Archivos de configuración

| Archivo | Ubicación |
|---|---|
| Configuración | `~/.config/descanso-visual/config.json` |
| Mensajes custom | `~/.config/descanso-visual/messages.json` |
| Estadísticas | `~/.config/descanso-visual/stats.json` |
| Snooze flag | `~/.config/descanso-visual/snooze_until` |
| Cache clima | `~/.config/descanso-visual/weather_cache` |
