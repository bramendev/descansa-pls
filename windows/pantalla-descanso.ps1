param(
    [int]$Segundos = 30,
    [string]$Modo = "visual",
    [string]$Animal = "perro",
    [string]$Weather = ""
)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$tieneTimer = ($Modo -eq "visual" -or $Modo -eq "activo")

$configDir = Join-Path $env:USERPROFILE ".config\descanso-visual"
$statsFile = Join-Path $configDir "stats.json"
$configFile = Join-Path $configDir "config.json"
$messagesFile = Join-Path $configDir "messages.json"
$snoozeFile = Join-Path $configDir "snooze_until"

if (-not (Test-Path $configDir)) { New-Item -ItemType Directory -Path $configDir -Force | Out-Null }

# --- Config ---
function Load-Config {
    try {
        if (Test-Path $configFile) { return Get-Content $configFile -Raw | ConvertFrom-Json }
    } catch {}
    return [PSCustomObject]@{ theme = "dark" }
}

# --- Stats ---
function Load-Stats {
    try {
        if (Test-Path $statsFile) { return Get-Content $statsFile -Raw | ConvertFrom-Json }
    } catch {}
    return [PSCustomObject]@{
        visual = [PSCustomObject]@{ total=0; today=0; date="" }
        activo = [PSCustomObject]@{ total=0; today=0; date="" }
        almuerzo = [PSCustomObject]@{ total=0; today=0; date="" }
        dormir = [PSCustomObject]@{ total=0; today=0; date="" }
        agua = [PSCustomObject]@{ total=0; today=0; date="" }
    }
}

function Save-Stats($s) { $s | ConvertTo-Json -Depth 5 | Set-Content $statsFile }

function Increment-Stat($modo) {
    $stats = Load-Stats
    $today = (Get-Date).ToString("yyyy-MM-dd")
    foreach ($key in @("visual","activo","almuerzo","dormir","agua")) {
        if (-not $stats.$key) { $stats | Add-Member -NotePropertyName $key -NotePropertyValue ([PSCustomObject]@{ total=0; today=0; date=$today }) }
    }
    if ($stats.$mode.date -ne $today) { $stats.$mode.today = 0 }
    $stats.$mode.total++; $stats.$mode.today++; $stats.$mode.date = $today
    if (-not $stats.history) { $stats | Add-Member -NotePropertyName history -NotePropertyValue ([PSCustomObject]@{}) }
    if (-not $stats.history.$today) { $stats.history | Add-Member -NotePropertyName $today -NotePropertyValue ([PSCustomObject]@{}) -Force }
    $dayEntry = $stats.history.$today
    $current = if ($dayEntry.$mode) { $dayEntry.$mode } else { 0 }
    $dayEntry | Add-Member -NotePropertyName $mode -NotePropertyValue ($current + 1) -Force
    Save-Stats $stats
    return $stats
}

function Get-Stats-Line($stats) {
    $today = (Get-Date).ToString("yyyy-MM-dd")
    $parts = @()
    $map = @{ visual="visuales"; activo="activas"; almuerzo="almuerzos"; dormir="dormir"; agua="aguas" }
    foreach ($modo in @("visual","activo","almuerzo","dormir","agua")) {
        if ($stats.$modo) {
            $t = if ($stats.$modo.date -eq $today) { $stats.$modo.today } else { 0 }
            if ($t -gt 0) { $parts += "$t $($map[$modo])" }
        }
    }
    if ($parts.Count -eq 0) { return "Hoy aun no hay pausas registradas" }
    return $parts -join "  ·  "
}

function Write-Snooze {
    [System.IO.File]::WriteAllText($snoozeFile, ([DateTimeOffset]::UtcNow.ToUnixTimeSeconds() + 300).ToString())
}

# --- Custom messages ---
function Load-Custom {
    try {
        if (Test-Path $messagesFile) { return Get-Content $messagesFile -Raw | ConvertFrom-Json }
    } catch {}
    return $null
}
$customMsg = Load-Custom

# --- Messages ---
$curiosidades = @(
    "Los pulpos tienen tres corazones y su sangre es azul."
    "Un dia en Venus dura mas que un ano en Venus."
    "Las hormigas nunca duermen; toman micro-siestas de 8 segundos."
    "El corazon humano late unas 100.000 veces al dia."
    "Los arboles se comunican por una red de hongos llamada 'Wood Wide Web'."
    "El agua caliente se congela mas rapido que el agua fria."
    "La miel nunca se echa a perder."
    "Las nubes tipicas pesan unas 500 toneladas."
    "Los koalas tienen huellas dactilares casi identicas a las humanas."
    "La Tierra viaja a 107.000 km/h alrededor del Sol."
    "Las jirafas solo necesitan dormir 30 minutos al dia."
    "Apagar la vista 20s cada 20 min reduce la fatiga visual un 40%."
    "El ADN humano tiene un 50% de similitud con el de un platano."
    "Hay mas estrellas en el universo que granos de arena en las playas."
    "Los pingüinos proponen matrimonio con una piedra."
    "Los gatos no pueden saborear lo dulce."
    "Tu cuerpo reemplaza millones de celulas cada segundo."
)
if ($customMsg -and $customMsg.curiosidades) { $curiosidades += $customMsg.curiosidades }

$ejercicios = @(
    "Estira los brazos hacia arriba — 15 segundos"
    "Gira los hombros hacia atras — 10 repeticiones"
    "Inclina la cabeza a la izquierda — 15s cada lado"
    "Abre y cierra las manos — 10 veces"
    "Estira la espalda hacia atras — 10 segundos"
    "Camina un poco — 2 minutos"
    "Circulos con los tobillos — 10 veces cada pie"
    "Estira las piernas — 15 segundos cada una"
    "Sube y baja los hombros — 10 veces"
    "Gira la cintura — 5 veces cada lado"
    "Abre el pecho con brazos atras — 15 segundos"
    "Respira profundo: 4s inspira, 4s mantene, 4s exhala"
)
if ($customMsg -and $customMsg.ejercicios) { $ejercicios += $customMsg.ejercicios }

$motivacionales = @(
    "Cada paso cuenta, no importa cuan pequeño."
    "El progreso, no la perfeccion."
    "Tu esfuerzo de hoy es tu fuerza de mañana."
    "No te rindas, estas mas cerca de lo que crees."
    "La disciplina es el puente entre metas y logros."
    "Un descanso ahora es energia para despues."
    "Cuida tu cuerpo, es el unico lugar que tienes para vivir."
    "La constancia vence al talento."
    "Pequeños habitos, grandes cambios."
    "Tu salud es tu mayor inversion."
    "Respira profundo. Esto tambien pasara."
    "El mejor momento para empezar es ahora."
)
if ($customMsg -and $customMsg.motivacionales) { $motivacionales += $customMsg.motivacionales }

$comidas = @("Come algo nutritivo y equilibrado." "TOMate minimo 20 minutos para comer." "Alejate de la pantalla mientras comes." "Hidratate bien con agua." "Incluye proteinas y verduras.")
if ($customMsg -and $customMsg.comidas) { $comidas += $customMsg.comidas }

$dormirTips = @("Apaga las pantallas 30 min antes de dormir." "Prepara tu habitacion: oscura y fresca." "Lee un libro antes de dormir." "Evita cafeina despues de las 6 PM." "Respira profundamente: 4-7-8.")
if ($customMsg -and $customMsg.dormir) { $dormirTips += $customMsg.dormir }

$aguaMsgs = @("¡Toma un vaso de agua!" "Hidratate, tu cuerpo lo necesita." "Un vaso de agua ahora te dara energia." "Beber agua mejora tu concentracion." "Tu cerebro es 75% agua. ¡Hidratalo!")
if ($customMsg -and $customMsg.agua) { $aguaMsgs += $customMsg.agua }

$dogFrames = @(
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/    U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/   ~U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/    U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", " ~\_____/    U")
)
$catFrames = @(
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,4-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,4-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,w-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)")
)
$frames = if ($Animal -eq "gato" -or $Animal -eq "cat") { $catFrames } else { $dogFrames }
$maxW = ($frames | ForEach-Object { $_ | ForEach-Object { $_.Length } } | Measure-Object -Maximum).Maximum

$bowl = @(" .-._.-. ", "(  o o  )", " \ o o / ", "  `---'  ")
$moon = @("   .-.   ", "  (   )  ", "   `-'   ", "  *   *  ", "   * *   ")
$waterDrop = @("    .", "   / \", "  /   \", " |     |", "  \   /", "   \ /", "    `")

# --- Theme ---
$cfg = Load-Config
$themePref = if ($cfg.theme) { $cfg.theme } else { "dark" }
if ($themePref -eq "auto") {
    $h = (Get-Date).Hour
    $themeName = if ($h -ge 7 -and $h -lt 19) { "light" } else { "dark" }
} else { $themeName = $themePref }
$isDark = ($themeName -eq "dark")

function HexToColor($hex) {
    $r = [Convert]::ToByte($hex.Substring(1,2), 16)
    $g = [Convert]::ToByte($hex.Substring(3,2), 16)
    $b = [Convert]::ToByte($hex.Substring(5,2), 16)
    return [System.Drawing.Color]::FromArgb($r, $g, $b)
}

function ModeColor($dark, $light) { if ($isDark) { $dark } else { $light } }

$modeColors = @{
    "visual" = @{ animal=(ModeColor "#c0c0e0" "#4040a0"); timer=(ModeColor "#ffcc66" "#cc6600")
        title=(ModeColor "#ff7755" "#cc4422"); text=(ModeColor "#d0d0a0" "#303060")
        pfill=(ModeColor "#6688ff" "#4466cc"); btn="#3344aa"; bord="#5566cc"; bhov="#4455cc"
        titulo="DESCANSO VISUAL" }
    "activo" = @{ animal=(ModeColor "#a0e0a0" "#208020"); timer=(ModeColor "#88ff88" "#228822")
        title=(ModeColor "#66ff66" "#226622"); text=(ModeColor "#c0e0c0" "#204020")
        pfill=(ModeColor "#44aa44" "#228822"); btn="#2a5a2a"; bord="#44aa44"; bhov="#3a7a3a"
        titulo="PAUSA ACTIVA" }
    "almuerzo" = @{ animal=(ModeColor "#d0b060" "#806020"); timer=(ModeColor "#ffaa44" "#cc6600")
        title=(ModeColor "#ff9944" "#cc6600"); text=(ModeColor "#e0c080" "#604020")
        pfill=(ModeColor "#cc8844" "#aa6622"); btn="#8a5a2a"; bord="#cc8844"; bhov="#aa6a3a"
        titulo="HORA DE ALMORZAR" }
    "dormir" = @{ animal=(ModeColor "#8080d0" "#4040a0"); timer=(ModeColor "#8888ff" "#4444cc")
        title=(ModeColor "#6666ff" "#4444cc"); text=(ModeColor "#8080d0" "#4040a0")
        pfill=(ModeColor "#4444cc" "#3333aa"); btn="#2222aa"; bord="#4444cc"; bhov="#3333cc"
        titulo="HORA DE DORMIR" }
    "agua" = @{ animal=(ModeColor "#80c0ff" "#2060a0"); timer=(ModeColor "#66ccff" "#2266aa")
        title=(ModeColor "#44aaff" "#2266aa"); text=(ModeColor "#a0d0ff" "#204080")
        pfill=(ModeColor "#4488ff" "#2266cc"); btn="#2244aa"; bord="#4466cc"; bhov="#3355cc"
        titulo="HIDRATACION" }
}
$bgColor = if ($isDark) { "#0a0a1a" } else { "#f0f0f5" }
$clockColor = if ($isDark) { "#e0e0ff" } else { "#1a1a3a" }
$dateColor = if ($isDark) { "#9090b0" } else { "#6060a0" }
$infoColor = if ($isDark) { "#b0b0d0" } else { "#505080" }
$ptrackColor = if ($isDark) { "#1a1a3a" } else { "#d0d0e0" }

$MC = $modeColors[$Modo]
if (-not $MC) { $MC = $modeColors["visual"] }

# --- Stats ---
$stats = Load-Stats
$statsLine = Get-Stats-Line $stats
$statsUpdated = $false

# --- Form ---
$form = New-Object System.Windows.Forms.Form
$form.Text = $MC.titulo
$form.WindowState = "Maximized"
$form.FormBorderStyle = "None"
$form.TopMost = $true
$form.BackColor = HexToColor $bgColor
$form.ControlBox = $false
$form.ShowInTaskbar = $false
$form.KeyPreview = $true
if ($tieneTimer) { $form.Cursor = [System.Windows.Forms.Cursors]::No }

$tiempoRestante = $Segundos
$inicio = [System.Diagnostics.Stopwatch]::StartNew()
$frameCounter = 0

# --- Clock ---
$reloj = New-Object System.Windows.Forms.Label
$reloj.Font = New-Object System.Drawing.Font("Segoe UI", 80, [System.Drawing.FontStyle]::Bold)
$reloj.ForeColor = HexToColor $clockColor
$reloj.BackColor = [System.Drawing.Color]::Transparent
$reloj.TextAlign = "MiddleCenter"
$reloj.Size = New-Object System.Drawing.Size(600, 130)
$reloj.Location = New-Object System.Drawing.Point(0, 140)

# --- Date ---
$fecha = New-Object System.Windows.Forms.Label
$fecha.Font = New-Object System.Drawing.Font("Segoe UI", 16)
$fecha.ForeColor = HexToColor $dateColor
$fecha.BackColor = [System.Drawing.Color]::Transparent
$fecha.TextAlign = "MiddleCenter"
$fecha.Size = New-Object System.Drawing.Size(500, 30)
$fecha.Location = New-Object System.Drawing.Point(0, 270)

$form.Controls.Add($reloj)
$form.Controls.Add($fecha)

# --- Weather ---
$yOffset = 310
if ($Weather) {
    $weatherLabel = New-Object System.Windows.Forms.Label
    $weatherLabel.Font = New-Object System.Drawing.Font("Segoe UI", 14)
    $weatherLabel.ForeColor = HexToColor $dateColor
    $weatherLabel.BackColor = [System.Drawing.Color]::Transparent
    $weatherLabel.TextAlign = "MiddleCenter"
    $weatherLabel.Size = New-Object System.Drawing.Size(500, 25)
    $weatherLabel.Location = New-Object System.Drawing.Point(0, 300)
    $weatherLabel.Text = $Weather
    $form.Controls.Add($weatherLabel)
    $yOffset = 340
}

# --- Mode-specific ---
function Do-Skip {
    if (-not $statsUpdated) { $script:stats = Increment-Stat $Modo; $script:statsUpdated = $true }
    $form.Close()
}
function Do-Snooze {
    Write-Snooze
    if (-not $statsUpdated) { $script:stats = Increment-Stat $Modo; $script:statsUpdated = $true }
    $form.Close()
}

if ($tieneTimer) {
    $animalLabel = New-Object System.Windows.Forms.Label
    $animalLabel.Font = New-Object System.Drawing.Font("Consolas", 16)
    $animalLabel.ForeColor = HexToColor $MC.animal
    $animalLabel.BackColor = [System.Drawing.Color]::Transparent
    $animalLabel.TextAlign = "MiddleCenter"
    $animalLabel.Size = New-Object System.Drawing.Size(400, 100)
    $animalLabel.Location = New-Object System.Drawing.Point(0, $yOffset)

    $timerLabel = New-Object System.Windows.Forms.Label
    $timerLabel.Font = New-Object System.Drawing.Font("Segoe UI", 22)
    $timerLabel.ForeColor = HexToColor $MC.timer
    $timerLabel.BackColor = [System.Drawing.Color]::Transparent
    $timerLabel.TextAlign = "MiddleCenter"
    $timerLabel.Size = New-Object System.Drawing.Size(500, 40)
    $timerLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 110))

    $barra = New-Object System.Windows.Forms.ProgressBar
    $barra.Size = New-Object System.Drawing.Size(400, 12)
    $barra.Location = New-Object System.Drawing.Point(0, ($yOffset + 160))
    $barra.Style = "Continuous"
    $barra.ForeColor = HexToColor $MC.pfill
    $barra.BackColor = HexToColor $ptrackColor

    $pool = if ($Modo -eq "visual") { $curiosidades + $motivacionales } else { $ejercicios + $motivacionales }
    $infoLabel = New-Object System.Windows.Forms.Label
    $infoLabel.Font = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Italic)
    $infoLabel.ForeColor = HexToColor $infoColor
    $infoLabel.BackColor = [System.Drawing.Color]::Transparent
    $infoLabel.TextAlign = "MiddleCenter"
    $infoLabel.Size = New-Object System.Drawing.Size(600, 60)
    $infoLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 190))
    $infoLabel.Text = $pool | Get-Random

    $statsLabel = New-Object System.Windows.Forms.Label
    $statsLabel.Font = New-Object System.Drawing.Font("Segoe UI", 13)
    $statsLabel.ForeColor = HexToColor $dateColor
    $statsLabel.BackColor = [System.Drawing.Color]::Transparent
    $statsLabel.TextAlign = "MiddleCenter"
    $statsLabel.Size = New-Object System.Drawing.Size(600, 25)
    $statsLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 260))
    $statsLabel.Text = $statsLine

    $form.Controls.AddRange(@($animalLabel, $timerLabel, $barra, $infoLabel, $statsLabel))

    $btnUnlock = New-Object System.Windows.Forms.Button
    $btnUnlock.Font = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Bold)
    $btnUnlock.Text = "Desbloquear"
    $btnUnlock.Size = New-Object System.Drawing.Size(220, 50)
    $btnUnlock.FlatStyle = "Flat"
    $btnUnlock.FlatAppearance.BorderSize = 2
    $btnUnlock.BackColor = HexToColor $MC.btn
    $btnUnlock.ForeColor = [System.Drawing.Color]::White
    $btnUnlock.FlatAppearance.BorderColor = HexToColor $MC.bord
    $btnUnlock.Visible = $false
    $btnUnlock.Location = New-Object System.Drawing.Point(0, ($yOffset + 300))
    $form.Controls.Add($btnUnlock)
    $btnUnlock.Add_Click({ Do-Skip })

    $hintLabel = New-Object System.Windows.Forms.Label
    $hintLabel.Font = New-Object System.Drawing.Font("Segoe UI", 11)
    $hintLabel.ForeColor = HexToColor $dateColor
    $hintLabel.BackColor = [System.Drawing.Color]::Transparent
    $hintLabel.TextAlign = "MiddleCenter"
    $hintLabel.Size = New-Object System.Drawing.Size(600, 20)
    $hintLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 360))
    $hintLabel.Text = "Espacio: saltar  ·  Z: snooze 5 min"
    $form.Controls.Add($hintLabel)

    $animTimer = New-Object System.Windows.Forms.Timer
    $animTimer.Interval = 400
    $animTimer.Add_Tick({
        $frameCounter++
        $idx = $frameCounter % $frames.Count
        $f = $frames[$idx]
        $text = ($f | ForEach-Object { $_.PadLeft($maxW).PadRight($maxW) }) -join "`n"
        $animalLabel.Text = $text
    })
    $animTimer.Start()

    $timer = New-Object System.Windows.Forms.Timer
    $timer.Interval = 100
    $timer.Add_Tick({
        $transcurrido = $inicio.Elapsed.TotalSeconds
        $restante = [Math]::Max(0, $Segundos - $transcurrido)
        $tiempoRestante = [int]$restante
        $reloj.Text = (Get-Date).ToString("HH:mm")
        $fecha.Text = (Get-Date).ToString("dddd, d 'de' MMMM 'de' yyyy")
        $barra.Value = [Math]::Min([int](($transcurrido / $Segundos) * 100), 100)
        $timerLabel.Text = "Descansa $tiempoRestante segundos mas…"
        if ($restante -le 0) {
            $barra.Value = 100
            $timerLabel.Text = "¡Ya puedes continuar!"
            $btnUnlock.Visible = $true
            $form.Cursor = [System.Windows.Forms.Cursors]::Default
            $timer.Stop()
        }
    })
    $timer.Start()
}
else {
    $form.Cursor = [System.Windows.Forms.Cursors]::Default

    $iconLines = switch ($Modo) { "almuerzo" { $bowl } "dormir" { $moon } default { $waterDrop } }
    $iconLabel = New-Object System.Windows.Forms.Label
    $iconLabel.Font = New-Object System.Drawing.Font("Consolas", 16)
    $iconLabel.ForeColor = HexToColor $MC.animal
    $iconLabel.BackColor = [System.Drawing.Color]::Transparent
    $iconLabel.TextAlign = "MiddleCenter"
    $iconLabel.Size = New-Object System.Drawing.Size(400, 120)
    $iconLabel.Location = New-Object System.Drawing.Point(0, $yOffset)
    $iconLabel.Text = ($iconLines | ForEach-Object { $_.PadLeft($maxW).PadRight($maxW) }) -join "`n"
    $form.Controls.Add($iconLabel)

    $titleLabel = New-Object System.Windows.Forms.Label
    $titleLabel.Font = New-Object System.Drawing.Font("Segoe UI", 36, [System.Drawing.FontStyle]::Bold)
    $titleLabel.ForeColor = HexToColor $MC.title
    $titleLabel.BackColor = [System.Drawing.Color]::Transparent
    $titleLabel.TextAlign = "MiddleCenter"
    $titleLabel.Size = New-Object System.Drawing.Size(600, 60)
    $titleLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 130))
    $titleLabel.Text = $MC.titulo
    $form.Controls.Add($titleLabel)

    $pool = switch ($Modo) { "almuerzo" { $comidas } "dormir" { $dormirTips } default { $aguaMsgs } }
    $textLabel = New-Object System.Windows.Forms.Label
    $textLabel.Font = New-Object System.Drawing.Font("Segoe UI", 20)
    $textLabel.ForeColor = HexToColor $MC.text
    $textLabel.BackColor = [System.Drawing.Color]::Transparent
    $textLabel.TextAlign = "MiddleCenter"
    $textLabel.Size = New-Object System.Drawing.Size(600, 40)
    $textLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 200))
    $textLabel.Text = $pool | Get-Random
    $form.Controls.Add($textLabel)

    $footText = if ($Modo -eq "agua") { "Bebe despacio, disfruta cada sorbo." } else { "Tomate tu tiempo." }
    $footLabel = New-Object System.Windows.Forms.Label
    $footLabel.Font = New-Object System.Drawing.Font("Segoe UI", 14, [System.Drawing.FontStyle]::Italic)
    $footLabel.ForeColor = HexToColor $infoColor
    $footLabel.BackColor = [System.Drawing.Color]::Transparent
    $footLabel.TextAlign = "MiddleCenter"
    $footLabel.Size = New-Object System.Drawing.Size(600, 30)
    $footLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 250))
    $footLabel.Text = $footText
    $form.Controls.Add($footLabel)

    $statsLabel = New-Object System.Windows.Forms.Label
    $statsLabel.Font = New-Object System.Drawing.Font("Segoe UI", 13)
    $statsLabel.ForeColor = HexToColor $dateColor
    $statsLabel.BackColor = [System.Drawing.Color]::Transparent
    $statsLabel.TextAlign = "MiddleCenter"
    $statsLabel.Size = New-Object System.Drawing.Size(600, 25)
    $statsLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 290))
    $statsLabel.Text = $statsLine
    $form.Controls.Add($statsLabel)

    $btnClose = New-Object System.Windows.Forms.Button
    $btnClose.Font = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Bold)
    $btnClose.Text = "Cerrar"
    $btnClose.Size = New-Object System.Drawing.Size(220, 50)
    $btnClose.FlatStyle = "Flat"
    $btnClose.FlatAppearance.BorderSize = 2
    $btnClose.BackColor = HexToColor $MC.btn
    $btnClose.ForeColor = [System.Drawing.Color]::White
    $btnClose.FlatAppearance.BorderColor = HexToColor $MC.bord
    $btnClose.Location = New-Object System.Drawing.Point(0, ($yOffset + 330))
    $form.Controls.Add($btnClose)
    $btnClose.Add_Click({ Do-Skip })

    $hintLabel = New-Object System.Windows.Forms.Label
    $hintLabel.Font = New-Object System.Drawing.Font("Segoe UI", 11)
    $hintLabel.ForeColor = HexToColor $dateColor
    $hintLabel.BackColor = [System.Drawing.Color]::Transparent
    $hintLabel.TextAlign = "MiddleCenter"
    $hintLabel.Size = New-Object System.Drawing.Size(600, 20)
    $hintLabel.Location = New-Object System.Drawing.Point(0, ($yOffset + 390))
    $hintLabel.Text = "Espacio o Z: cerrar"
    $form.Controls.Add($hintLabel)

    $autoSec = if ($Modo -eq "agua") { 10000 } else { 600000 }
    $closeTimer = New-Object System.Windows.Forms.Timer
    $closeTimer.Interval = $autoSec
    $closeTimer.Add_Tick({ Do-Skip })
    $closeTimer.Start()
}

# --- Events ---
$form.Add_KeyDown({
    if ($_.KeyCode -eq "Space") { Do-Skip; return }
    if ($_.KeyCode -eq "Z") { Do-Snooze; return }
    if ($_.KeyCode -eq "Escape") {
        if (-not $tieneTimer -or $tiempoRestante -le 0) { Do-Skip }
    }
    if ($_.KeyCode -eq "Enter") {
        if ($btnUnlock -and $btnUnlock.Visible) { Do-Skip }
    }
})

$form.Add_Shown({
    $form.Activate()
    $reloj.Left = ($form.ClientSize.Width - $reloj.Width) / 2
    $fecha.Left = ($form.ClientSize.Width - $fecha.Width) / 2
    if ($Weather -and $weatherLabel) { $weatherLabel.Left = ($form.ClientSize.Width - $weatherLabel.Width) / 2 }
    if ($tieneTimer) {
        $animalLabel.Left = ($form.ClientSize.Width - $animalLabel.Width) / 2
        $timerLabel.Left = ($form.ClientSize.Width - $timerLabel.Width) / 2
        $barra.Left = ($form.ClientSize.Width - $barra.Width) / 2
        $infoLabel.Left = ($form.ClientSize.Width - $infoLabel.Width) / 2
        $statsLabel.Left = ($form.ClientSize.Width - $statsLabel.Width) / 2
        $btnUnlock.Left = ($form.ClientSize.Width - $btnUnlock.Width) / 2
        $hintLabel.Left = ($form.ClientSize.Width - $hintLabel.Width) / 2
    } else {
        $iconLabel.Left = ($form.ClientSize.Width - $iconLabel.Width) / 2
        $titleLabel.Left = ($form.ClientSize.Width - $titleLabel.Width) / 2
        $textLabel.Left = ($form.ClientSize.Width - $textLabel.Width) / 2
        $footLabel.Left = ($form.ClientSize.Width - $footLabel.Width) / 2
        $statsLabel.Left = ($form.ClientSize.Width - $statsLabel.Width) / 2
        $btnClose.Left = ($form.ClientSize.Width - $btnClose.Width) / 2
        $hintLabel.Left = ($form.ClientSize.Width - $hintLabel.Width) / 2
    }
})

[System.Windows.Forms.Application]::Run($form)
