#!/usr/bin/env python3
# ponytail: test mínimo del reset de timers tras suspensión.
# Verifica que un salto temporal grande no dispara breaks acumulados.
import importlib.machinery, importlib.util
loader = importlib.machinery.SourceFileLoader('descanso', 'descanso')
spec = importlib.util.spec_from_loader('descanso', loader)
descanso = importlib.util.module_from_spec(spec)
loader.exec_module(descanso)


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
        'music_url': '', 'animal': 'perro', 'sound': False,
        'weather_city': '', 'language': 'es', 'theme': 'dark',
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


if __name__ == '__main__':
    test_initial_fire()
    test_suspend_resets_timers()
    test_small_gap_no_reset()
    print("OK: 3 tests pasaron")