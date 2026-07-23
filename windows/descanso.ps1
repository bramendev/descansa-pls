param(
    [int]$visualIntervalMin = 20,
    [int]$visualDurationSec = 30,
    [int]$activeIntervalMin = 60,
    [int]$activeDurationSec = 180,
    [string]$lunchTime = "12:00",
    [string]$sleepTime = "22:30",
    [string]$animal = "perro"
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pantalla = Join-Path $scriptDir "pantalla-descanso.ps1"
$vi = $visualIntervalMin * 60
$ai = $activeIntervalMin * 60

$nextVisual = [DateTime]::Now
$nextActive = [DateTime]::Now.AddSeconds($ai)
$lastLunchDate = $null
$lastSleepDate = $null

Write-Host "── Descanso Visual ──"
Write-Host "  Visual:  cada ${visualIntervalMin} min  (${visualDurationSec}s)"
Write-Host "  Activo:  cada ${activeIntervalMin} min  (${activeDurationSec}s)"
Write-Host "  Almuerzo: $lunchTime"
Write-Host "  Dormir:   $sleepTime"
Write-Host "  Animal:   $animal"
Write-Host "────────────────────"

function Launch-Break($mode, $duration) {
    if (Test-Path $pantalla) {
        & $pantalla -Segundos $duration -Modo $mode -Animal $animal
    }
}

while ($true) {
    $now = Get-Date
    $today = $now.Date

    if ($lastLunchDate -ne $today) {
        $lunch = [DateTime]::ParseExact($lunchTime, "HH:mm", $null)
        $lunchEnd = $lunch.AddMinutes(30)
        if ($now -ge $lunch -and $now -le $lunchEnd) {
            Write-Host "[$($now.ToString('HH:mm'))] ⏰ Almuerzo"
            Launch-Break "almuerzo" 0
            $lastLunchDate = $today
        }
    }

    if ($lastSleepDate -ne $today) {
        $sleep = [DateTime]::ParseExact($sleepTime, "HH:mm", $null)
        $sleepEnd = $sleep.AddMinutes(30)
        if ($now -ge $sleep -and $now -le $sleepEnd) {
            Write-Host "[$($now.ToString('HH:mm'))] 🌙 Dormir"
            Launch-Break "dormir" 0
            $lastSleepDate = $today
        }
    }

    if ((Get-Date) -ge $nextActive) {
        Write-Host "[$((Get-Date).ToString('HH:mm'))] 🏃 Pausa activa"
        Launch-Break "activo" $activeDurationSec
        $nextActive = (Get-Date).AddSeconds($ai)
        $nextVisual = (Get-Date).AddSeconds($ai)
    }

    if ((Get-Date) -ge $nextVisual) {
        Write-Host "[$((Get-Date).ToString('HH:mm'))] 👀 Descanso visual"
        Launch-Break "visual" $visualDurationSec
        $nextVisual = (Get-Date).AddSeconds($vi)
    }

    Start-Sleep -Seconds 5
}
