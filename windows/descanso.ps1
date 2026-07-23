param(
    [int]$visualIntervalMin = 20,
    [int]$visualDurationSec = 30,
    [int]$activeIntervalMin = 60,
    [int]$activeDurationSec = 180,
    [int]$waterIntervalMin = 30,
    [int]$waterDurationSec = 10,
    [string]$lunchTime = "12:00",
    [string]$sleepTime = "22:30",
    [string]$animal = "perro",
    [string]$musicUrl = "",
    [string]$weatherCity = "",
    [switch]$sound = $true
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pantallaExe = Join-Path $scriptDir "pantalla-descanso.exe"
$pantallaPs1 = Join-Path $scriptDir "pantalla-descanso.ps1"
$pantalla = if (Test-Path $pantallaExe) { $pantallaExe } else { $pantallaPs1 }
$vi = $visualIntervalMin * 60
$ai = $activeIntervalMin * 60
$wi = $waterIntervalMin * 60

$configDir = Join-Path $env:USERPROFILE ".config\descanso-visual"
$snoozeFile = Join-Path $configDir "snooze_until"
$weatherFile = Join-Path $configDir "weather_cache.json"
$statsFile = Join-Path $configDir "stats.json"

if (-not (Test-Path $configDir)) { New-Item -ItemType Directory -Path $configDir -Force | Out-Null }

$nextVisual = [DateTime]::Now
$nextActive = [DateTime]::Now.AddSeconds($ai)
$nextWater = [DateTime]::Now.AddSeconds($wi)
$lastLunchDate = $null
$lastSleepDate = $null

function Load-Stats {
    try { if (Test-Path $statsFile) { return Get-Content $statsFile -Raw | ConvertFrom-Json } } catch {}
    return $null
}

function Show-Stats {
    $stats = Load-Stats
    if (-not $stats) { return }
    $today = (Get-Date).ToString("yyyy-MM-dd")
    $labels = @{ visual="visuales"; activo="activas"; agua="aguas"; almuerzo="almuerzos"; dormir="dormir" }
    foreach ($modo in @("visual","activo","agua","almuerzo","dormir")) {
        if ($stats.$modo) {
            $t = if ($stats.$modo.date -eq $today) { $stats.$modo.today } else { 0 }
            $tot = $stats.$modo.total
            if ($t -gt 0 -or $tot -gt 0) { Write-Host "  $t $($labels[$modo]) (total $tot)" }
        }
    }
    if (-not $stats.history) { return }
    $wkStart = (Get-Date).AddDays(-[int](Get-Date).DayOfWeek).Date
    $totals = @{}
    for ($i = 0; $i -lt 7; $i++) {
        $day = $wkStart.AddDays($i).ToString("yyyy-MM-dd")
        if ($stats.history.$day) {
            foreach ($kv in $stats.history.$day.PSObject.Properties) {
                $totals[$kv.Name] = ($totals[$kv.Name] -or 0) + [int]$kv.Value
            }
        }
    }
    if ($totals.Count -gt 0) {
        $parts = @()
        foreach ($modo in @("visual","activo","agua","almuerzo","dormir")) {
            if ($totals[$modo] -and $totals[$modo] -gt 0) {
                $parts += "$($totals[$modo]) $($labels[$modo])"
            }
        }
        if ($parts.Count -gt 0) { Write-Host "  Semana: $(($parts -join '  ·  '))" }
    }
}

function Get-Weather($city) {
    if (-not $city) { return "" }
    try {
        if (Test-Path $weatherFile) {
            $cached = Get-Content $weatherFile -Raw | ConvertFrom-Json
            if ((Get-Date).ToFileTime() - $cached.time -lt 18000000000) { return $cached.text }
        }
        $url = "https://wttr.in/$city?format=%t+%C&lang=es"
        $text = (Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing).Content.Trim()
        if ($text) {
            @{ text=$text; time=(Get-Date).ToFileTime() } | ConvertTo-Json | Set-Content $weatherFile
        }
        return $text
    } catch { return "" }
}

function Write-Snooze {
    [System.IO.File]::WriteAllText($snoozeFile, ([DateTimeOffset]::UtcNow.ToUnixTimeSeconds() + 300).ToString())
}

function Is-Snoozed {
    try {
        if (Test-Path $snoozeFile) {
            $until = [long](Get-Content $snoozeFile)
            if ([DateTimeOffset]::UtcNow.ToUnixTimeSeconds() -lt $until) { return $true }
            Remove-Item $snoozeFile -Force -ErrorAction SilentlyContinue
        }
    } catch {}
    return $false
}

function Launch-Break($mode, $duration) {
    $args = @("-Segundos", $duration, "-Modo", $mode, "-Animal", $animal)
    $w = Get-Weather $weatherCity
    if ($w) { $args += "-Weather"; $args += "`"$w`"" }
    if (Test-Path $pantalla) { & $pantalla $args }
}

# Music
function Play-Music($duration) {
    if (-not $musicUrl) { return $null }
    try {
        $p = [System.Diagnostics.Process]::Start("ffplay", "-nodisp","-autoexit","-t","$duration","`"$musicUrl`"")
        return $p
    } catch { return $null }
}
function Stop-Music($proc) {
    if (-not $proc -or $proc.HasExited) { return }
    try { $proc.Kill(); $proc.WaitForExit(2000) } catch {}
}
function Play-Sound {
    try {
        $p = [System.Diagnostics.Process]::Start("ffplay", "-nodisp","-autoexit","-f","lavfi","-i","sine=frequency=523:duration=0.3,volume=0.15")
        if ($p) { Start-Sleep -Milliseconds 500; if (-not $p.HasExited) { $p.Kill() } }
    } catch {}
}

Write-Host "── Descanso Visual ──"
Write-Host "  Visual:   cada ${visualIntervalMin} min  (${visualDurationSec}s)"
Write-Host "  Activo:   cada ${activeIntervalMin} min  (${activeDurationSec}s)"
Write-Host "  Agua:     cada ${waterIntervalMin} min  (${waterDurationSec}s)"
Write-Host "  Almuerzo: $lunchTime"
Write-Host "  Dormir:   $sleepTime"
Write-Host "  Animal:   $animal"
Write-Host "  Sonido:   $sound"
if ($musicUrl) { Write-Host "  Musica:   si" }
$w = Get-Weather $weatherCity
if ($w) { Write-Host "  Clima:    $w" }
Write-Host "── Estadisticas ────"
Show-Stats
Write-Host "────────────────────"

while ($true) {
    $now = Get-Date
    $today = $now.Date

    if ($lastLunchDate -ne $today) {
        try {
            $lunch = [DateTime]::ParseExact($lunchTime, "HH:mm", $null)
            $lunchEnd = $lunch.AddMinutes(30)
            if ($now -ge $lunch -and $now -le $lunchEnd) {
                Write-Host "[$($now.ToString('HH:mm'))] ⏰ Almuerzo"
                if ($sound) { Play-Sound }
                Launch-Break "almuerzo" 0
                Show-Stats; $lastLunchDate = $today
            }
        } catch {}
    }

    if ($lastSleepDate -ne $today) {
        try {
            $sleep = [DateTime]::ParseExact($sleepTime, "HH:mm", $null)
            $sleepEnd = $sleep.AddMinutes(30)
            if ($now -ge $sleep -and $now -le $sleepEnd) {
                Write-Host "[$($now.ToString('HH:mm'))] 🌙 Dormir"
                if ($sound) { Play-Sound }
                Launch-Break "dormir" 0
                Show-Stats; $lastSleepDate = $today
            }
        } catch {}
    }

    if (-not (Is-Snoozed)) {
        if ((Get-Date) -ge $nextWater) {
            Write-Host "[$((Get-Date).ToString('HH:mm'))] 💧 Agua"
            if ($sound) { Play-Sound }
            Launch-Break "agua" $waterDurationSec
            Show-Stats; $nextWater = (Get-Date).AddSeconds($wi)
        }

        if ((Get-Date) -ge $nextActive) {
            Write-Host "[$((Get-Date).ToString('HH:mm'))] 🏃 Pausa activa"
            if ($sound) { Play-Sound }
            $_mp = Play-Music $activeDurationSec
            Launch-Break "activo" $activeDurationSec
            Stop-Music $_mp
            Show-Stats
            $nextActive = (Get-Date).AddSeconds($ai); $nextVisual = (Get-Date).AddSeconds($ai)
        }

        if ((Get-Date) -ge $nextVisual) {
            Write-Host "[$((Get-Date).ToString('HH:mm'))] 👀 Descanso visual"
            if ($sound) { Play-Sound }
            $_mp = Play-Music $visualDurationSec
            Launch-Break "visual" $visualDurationSec
            Stop-Music $_mp
            Show-Stats; $nextVisual = (Get-Date).AddSeconds($vi)
        }
    }

    Start-Sleep -Seconds 5
}
