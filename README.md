# Descanso Visual

Recordatorio automático de descanso visual cada 20 minutos.

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

Requiere: `python3` + `gtk3` (PyGObject).  
Opcional: `notify-send` (libnotify) como fallback.

## Windows

1. Copiar `windows/` a `C:\Users\tuUsuario\scripts\`
2. **Auto-inicio:** `Win+R` → `shell:startup` → acceso directo a `descanso.bat`
3. O manual: ejecutar `descanso.bat`

## Uso

```bash
descanso              # cada 20 min, pausa de 30 seg
descanso 45 60        # cada 45 min, pausa de 60 seg
descanso.ps1 -Minutos 15 -SegundosDescanso 30   # PowerShell
```
