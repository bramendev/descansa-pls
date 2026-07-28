# Descanso Visual

Recordatorio automático de pausas con pantalla simulada de bloqueo.
Un solo script en **Python + tkinter**, funciona igual en cualquier sistema
operativo — solo necesitas Python instalado.

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
- **Idioma**: español o inglés, detectado automáticamente del sistema (o fija `language` en la config)

## Instalación

Requiere **Python 3** (con `tkinter`, viene incluido) y opcionalmente
`ffplay` (sonido/música).

```bash
git clone git@github.com:bramendev/descansa-pls.git
cd descansa-pls
python3 descanso   # Linux/macOS
python descanso    # Windows
```

Eso es todo — corre directo desde la carpeta clonada, sin copiar nada
ni compilar nada.

### Auto-inicio (opcional)

Si quieres que arranque solo con la sesión:

- **Linux (systemd):**
  ```bash
  cp descanso ~/.local/bin/
  cp pantalla-descanso ~/.local/bin/
  cp descanso.service ~/.config/systemd/user/
  systemctl --user daemon-reload
  systemctl --user enable --now descanso
  journalctl --user -u descanso -f   # logs
  ```
- **Windows:** `Win+R` → `shell:startup` → acceso directo apuntando a
  `pythonw.exe C:\ruta\a\descansa-pls\descanso`.

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
  "sound": true,
  "language": "auto"
}
```

`language` acepta `"auto"` (detecta español o inglés del sistema),
`"es"` o `"en"` para forzarlo.

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
