param(
    [int]$visualIntervalMin = 20,
    [int]$visualDurationSec = 30,
    [int]$activeIntervalMin = 60,
    [int]$activeDurationSec = 180,
    [string]$lunchTime = "12:00",
    [string]$sleepTime = "22:30",
    [string]$animal = "perro",
    [string]$musicUrl = "https://streams.fluxfm.de/lofi/mp3-128/"
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pantalla = Join-Path $scriptDir "pantalla-descanso.ps1"
$vi = $visualIntervalMin * 60
$ai = $activeIntervalMin * 60

$nextVisual = [DateTime]::Now
$nextActive = [DateTime]::Now.AddSeconds($ai)
$lastLunchDate = $null
$lastSleepDate = $null

# --- Stats ---
$configDir = Join-Path $env:USERPROFILE ".config\descanso-visual"
$statsFile = Join-Path $configDir "stats.json"

function Load-Stats {
    try {
        if (Test-Path $statsFile) {
            return Get-Content $statsFile -Raw | ConvertFrom-Json
        }
    } catch {}
    return $null
}

function Show-Stats {
    $stats = Load-Stats
    if (-not $stats) { return }
    $today = (Get-Date).ToString("yyyy-MM-dd")
    $labels = @{ visual = "visuales"; activo = "activas"; almuerzo = "almuerzos"; dormir = "dormir" }
    foreach ($modo in @("visual", "activo", "almuerzo", "dormir")) {
        if ($stats.$modo) {
            $t = if ($stats.$modo.date -eq $today) { $stats.$modo.today } else { 0 }
            $tot = $stats.$modo.total
            if ($t -gt 0 -or $tot -gt 0) {
                Write-Host "  $t $($labels[$modo]) (total $tot)"
            }
        }
    }
}

Write-Host "── Descanso Visual ──"
Write-Host "  Visual:   cada ${visualIntervalMin} min  (${visualDurationSec}s)"
Write-Host "  Activo:   cada ${activeIntervalMin} min  (${activeDurationSec}s)"
Write-Host "  Almuerzo: $lunchTime"
Write-Host "  Dormir:   $sleepTime"
Write-Host "  Animal:   $animal"
Write-Host "  Música:   $(if ($musicUrl) { 'sí' } else { 'no' })"
$hasStats = Load-Stats
if ($hasStats) {
    Write-Host "── Estadísticas ────"
    Show-Stats
}
Write-Host "────────────────────"

function Play-Music($duration) {
    if (-not $musicUrl) { return }
    try {
        Start-Process -FilePath "ffplay" -ArgumentList "-nodisp", "-autoexit", "-t", $duration, $musicUrl -WindowStyle Hidden -ErrorAction SilentlyContinue
    } catch {}
}

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
            Show-Stats
            $lastLunchDate = $today
        }
    }

    if ($lastSleepDate -ne $today) {
        $sleep = [DateTime]::ParseExact($sleepTime, "HH:mm", $null)
        $sleepEnd = $sleep.AddMinutes(30)
        if ($now -ge $sleep -and $now -le $sleepEnd) {
            Write-Host "[$($now.ToString('HH:mm'))] 🌙 Dormir"
            Launch-Break "dormir" 0
            Show-Stats
            $lastSleepDate = $today
        }
    }

    if ((Get-Date) -ge $nextActive) {
        Write-Host "[$((Get-Date).ToString('HH:mm'))] 🏃 Pausa activa"
        Play-Music $activeDurationSec
        Launch-Break "activo" $activeDurationSec
        Show-Stats
        $nextActive = (Get-Date).AddSeconds($ai)
        $nextVisual = (Get-Date).AddSeconds($ai)
    }

    if ((Get-Date) -ge $nextVisual) {
        Write-Host "[$((Get-Date).ToString('HH:mm'))] 👀 Descanso visual"
        Play-Music $visualDurationSec
        Launch-Break "visual" $visualDurationSec
        Show-Stats
        $nextVisual = (Get-Date).AddSeconds($vi)
    }

    Start-Sleep -Seconds 5
}
