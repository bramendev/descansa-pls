#!/usr/bin/env python3
# ponytail: test mínimo del reset de timers tras suspensión.
# Verifica que un salto temporal grande no dispara breaks acumulados.
import importlib.machinery, importlib.util
loader = importlib.machinery.SourceFileLoader('descanso', 'descanso')
spec = importlib.util.spec_from_loader('descanso', loader)
descanso = importlib.util.module_from_spec(spec)
loader.exec_module(descanso)

# descanso.time ES el módulo real time (no una copia): los tests que lo
# mockean pisan time.time()/sleep() para todo el proceso. Guardamos las
# funciones reales ANTES de que nada las mute, para poder restaurarlas.
_REAL_TIME = descanso.time.time
_REAL_SLEEP = descanso.time.sleep


def run_sim(ticks):
    """ticks: un valor por iteración del loop (avanza con cada sleep)."""
    calls = {'breaks': 0}
    state = {'i': 0}

    def fake_time():
        return ticks[state['i']]

    def fake_sleep(_s):
        state['i'] += 1
        if state['i'] >= len(ticks):
            raise StopIteration

    def fake_launch(mode, duration, animal, weather, use_sound, music_url=None, show_weekly=False):
        calls['breaks'] += 1

    descanso.time.time = fake_time
    descanso.time.sleep = fake_sleep
    descanso.launch_break = lambda *a, **k: None
    descanso.play_sound = lambda: None
    descanso.play_music = lambda *a, **k: None
    descanso.stop_music = lambda *a, **k: None
    descanso.trigger_break = fake_launch
    descanso.is_snoozed = lambda: False
    descanso.get_weather = lambda *a, **k: None
    # config con lunch/sleep fuera de ventana e intervals controlados
    descanso.load_config = lambda: {
        'visual_interval_min': 20, 'visual_duration_sec': 30,
        'active_interval_min': 60, 'active_duration_sec': 180,
        'water_interval_min': 30, 'water_duration_sec': 10,
        'lunch_time': '03:00', 'sleep_time': '03:30',
        'ambient_sound': 'ninguno', 'animal': 'perro', 'sound': False,
        'weather_city': '', 'language': 'es', 'theme': 'dark',
        'widget': False,
    }

    t0 = ticks[0]
    try:
        descanso.main()
    except StopIteration:
        pass
    return calls['breaks']


def test_initial_fire():
    # al arrancar, next_visual=time.time() → dispara visual inmediatamente
    ticks = [0, 5, 10, 15]
    n = run_sim(ticks)
    assert n == 1, f"esperaba 1 (visual inicial), got {n}"


def test_suspend_resets_timers():
    # intervalos default: visual 20min, activo 60min, agua 30min.
    # arranca (visual inicial=1), luego gap 1h → reset, sin burst.
    # sin reset habría 1+3=4; con reset solo el inicial=1.
    ticks = [0, 3600, 3605, 3610]
    n = run_sim(ticks)
    assert n == 1, f"esperaba 1 break tras suspensión, got {n} (se acumularon)"


def test_small_gap_no_reset():
    # gaps de 5s (operación normal) NO disparan reset; solo el visual inicial
    ticks = [0, 5, 10, 15, 20, 25]
    n = run_sim(ticks)
    assert n == 1, f"esperaba 1 (solo inicial), got {n}"


def test_write_state_rollover_and_content():
    import tempfile, json as _json
    from pathlib import Path
    descanso.time.time = _REAL_TIME
    descanso.time.sleep = _REAL_SLEEP
    tmpdir = Path(tempfile.mkdtemp())
    descanso.STATE_FILE = tmpdir / 'state.json'
    descanso.SNOOZE_FILE = tmpdir / 'snooze_until'  # no existe -> snoozed_until None
    descanso.CONFIG_DIR = tmpdir
    now = descanso.datetime(2026, 7, 30, 23, 0)  # después de almuerzo (12:00) y dormir (22:30)
    hoy = now.date()
    config = {'lunch_time': '12:00', 'sleep_time': '22:30', 'theme': 'dark'}
    descanso.write_state(100.0, 200.0, 300.0, 1200, 3600, 1800, config, hoy, now)
    data = _json.loads(descanso.STATE_FILE.read_text())
    assert data['next_visual'] == 100.0
    assert data['visual_interval'] == 1200
    assert data['snoozed_until'] is None
    lunch_dt = descanso.datetime.fromtimestamp(data['next_lunch'])
    assert lunch_dt.date() == hoy + descanso.timedelta(days=1), \
        "almuerzo ya pasó hoy, debería rodar a mañana"


def test_widget_fmt_remaining():
    loader = importlib.machinery.SourceFileLoader('descanso_widget', 'descanso-widget')
    spec = importlib.util.spec_from_loader('descanso_widget', loader)
    widget = importlib.util.module_from_spec(spec)
    loader.exec_module(widget)
    assert widget.fmt_remaining(125, 0) == '02:05'
    assert widget.fmt_remaining(3725, 0) == '1h 02m'
    assert widget.fmt_remaining(-5, 0) == 'ahora'


if __name__ == '__main__':
    test_initial_fire()
    test_suspend_resets_timers()
    test_small_gap_no_reset()
    test_write_state_rollover_and_content()
    test_widget_fmt_remaining()
    print("OK: 5 tests pasaron")