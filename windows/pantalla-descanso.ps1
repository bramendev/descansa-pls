param(
    [int]$Segundos = 30,
    [string]$Modo = "visual",
    [string]$Animal = "perro"
)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$tieneTimer = ($Modo -eq "visual" -or $Modo -eq "activo")

$curiosidades = @(
    "Los pulpos tienen tres corazones y su sangre es azul."
    "Un día en Venus dura más que un año en Venus."
    "Las hormigas nunca duermen; toman micro-siestas de 8 segundos."
    "El corazón humano late unas 100.000 veces al día."
    "Los árboles se comunican por una red de hongos llamada 'Wood Wide Web'."
    "Los flamencos no nacen rosados, se vuelven rosados por su alimentación."
    "La miel nunca se echa a perder. Se encontró miel comestible en tumbas egipcias."
    "Las jirafas solo necesitan dormir 30 minutos al día."
    "Apagar la vista 20s cada 20 min reduce la fatiga visual un 40%."
    "El ADN humano tiene un 50% de similitud con el de un plátano."
    "Hay más estrellas en el universo que granos de arena en todas las playas."
    "Tu cuerpo reemplaza millones de células cada segundo."
    "Los pingüinos proponen matrimonio con una piedra."
    "Los gatos no pueden saborear lo dulce."
)

$ejercicios = @(
    "Estira los brazos hacia arriba — 15 segundos"
    "Gira los hombros hacia atrás — 10 repeticiones"
    "Inclina la cabeza a la izquierda — 15s cada lado"
    "Abre y cierra las manos — 10 veces"
    "Estira la espalda hacia atrás — 10 segundos"
    "Camina un poco — 2 minutos"
    "Círculos con los tobillos — 10 veces cada pie"
    "Estira las piernas — 15 segundos cada una"
    "Sube y baja los hombros — 10 veces"
    "Gira la cintura — 5 veces cada lado"
    "Abre el pecho con brazos atrás — 15 segundos"
    "Respira profundo: 4s inspira, 4s mantén, 4s exhala"
)

$consejos = @(
    "Beber agua ayuda a mantener la concentración."
    "La postura correcta previene dolores de cuello."
    "La luz azul de las pantallas afecta el sueño nocturno."
    "Cada hora de escritorio, 5 minutos de movimiento."
)

$comidas = @(
    "Come algo nutritivo y equilibrado."
    "Tómate mínimo 20 minutos para comer."
    "Aléjate de la pantalla mientras comes."
    "Incluye proteínas y verduras en tu plato."
    "Mastica despacio y disfruta cada bocado."
)

$dormirTips = @(
    "Apaga todas las pantallas 30 min antes de dormir."
    "Prepara tu habitación: oscura, fresca y silenciosa."
    "Lee un libro antes de dormir."
    "Respira profundamente 5 veces: 4-7-8."
    "Desconecta las notificaciones del teléfono."
)

$dogFrames = @(
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/    U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/   ~U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", "  /_____/    U"),
    @("     / \__", "    (    @\___", "    /          O", "   /    (_____/", " ~\_____/    U")
)

$catFrames = @(
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,4-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,~-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,4-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)"),
    @("   |\      _,,,---,,_", "   /,`.-'`'    -.  ;-;;,_", "  |,w-  ) )-,_..;\ (  `'-'", " '---''(_/--'  `-'\_)")
)

$bowl = @(" .-._.-. ", "(  o o  )", " \ o o / ", "  `---'  ")
$moon = @("   .-.   ", "  (   )  ", "   `-'   ", "  *   *  ", "   * *   ")

$frames = if ($Animal -eq "gato" -or $Animal -eq "cat") { $catFrames } else { $dogFrames }
$frameIdx = 0
$maxW = ($frames | ForEach-Object { $_ | ForEach-Object { $_.Length } } | Measure-Object -Maximum).Maximum

$themes = @{
    "visual" = @{
        bg = "#0a0a1a"; clock = "#e0e0ff"; date = "#9090b0"
        animal = "#c0c0e0"; timer = "#ffcc66"; info = "#b0b0d0"
        title = "#ff7755"; text = "#d0d0a0"; pfill = "#6688ff"
        btn = "#3344aa"; bhov = "#4455cc"
        titulo = "DESCANSO VISUAL"
        item = $null
    }
    "activo" = @{
        bg = "#0a1a0a"; clock = "#c0ffc0"; date = "#80a080"
        animal = "#a0e0a0"; timer = "#88ff88"; info = "#b0d0b0"
        title = "#66ff66"; text = "#c0e0c0"; pfill = "#44aa44"
        btn = "#2a5a2a"; bhov = "#3a7a3a"
        titulo = "PAUSA ACTIVA"
        item = $ejercicios
    }
    "almuerzo" = @{
        bg = "#1a140a"; clock = "#ffd080"; date = "#b09070"
        animal = "#d0b060"; timer = "#ffaa44"; info = "#b09870"
        title = "#ff9944"; text = "#e0c080"; pfill = "#cc8844"
        btn = "#8a5a2a"; bhov = "#aa6a3a"
        titulo = "HORA DE ALMORZAR"
        item = $comidas
    }
    "dormir" = @{
        bg = "#080820"; clock = "#9090ff"; date = "#6060a0"
        animal = "#8080d0"; timer = "#8888ff"; info = "#6060a0"
        title = "#6666ff"; text = "#8080d0"; pfill = "#4444cc"
        btn = "#2222aa"; bhov = "#3333cc"
        titulo = "HORA DE DORMIR"
        item = $dormirTips
    }
}
$T = $themes[$Modo]
if (-not $T) { $T = $themes["visual"] }

function HexToColor($hex) {
    $r = [Convert]::ToByte($hex.Substring(1,2), 16)
    $g = [Convert]::ToByte($hex.Substring(3,2), 16)
    $b = [Convert]::ToByte($hex.Substring(5,2), 16)
    return [System.Drawing.Color]::FromArgb($r, $g, $b)
}

# --- Form ---
$form = New-Object System.Windows.Forms.Form
$form.Text = $T.titulo
$form.WindowState = "Maximized"
$form.FormBorderStyle = "None"
$form.TopMost = $true
$form.BackColor = HexToColor $T.bg
$form.ControlBox = $false
$form.ShowInTaskbar = $false
$form.KeyPreview = $true
if ($tieneTimer) { $form.Cursor = [System.Windows.Forms.Cursors]::No }

$tiempoRestante = $Segundos
$inicio = [System.Diagnostics.Stopwatch]::StartNew()
$frameCounter = 0

# --- Clock ---
$reloj = New-Object System.Windows.Forms.Label
$reloj.Font = New-Object System.Drawing.Font("Segoe UI", 72, [System.Drawing.FontStyle]::Bold)
$reloj.ForeColor = HexToColor $T.clock
$reloj.BackColor = [System.Drawing.Color]::Transparent
$reloj.TextAlign = "MiddleCenter"
$reloj.Size = New-Object System.Drawing.Size(600, 120)
$reloj.Location = New-Object System.Drawing.Point(0, 180)

# --- Date ---
$fecha = New-Object System.Windows.Forms.Label
$fecha.Font = New-Object System.Drawing.Font("Segoe UI", 18)
$fecha.ForeColor = HexToColor $T.date
$fecha.BackColor = [System.Drawing.Color]::Transparent
$fecha.TextAlign = "MiddleCenter"
$fecha.Size = New-Object System.Drawing.Size(500, 40)
$fecha.Location = New-Object System.Drawing.Point(0, 300)

$form.Controls.Add($reloj)
$form.Controls.Add($fecha)

# --- Mode-specific ---
if ($tieneTimer) {
    # Animal
    $animalLabel = New-Object System.Windows.Forms.Label
    $animalLabel.Font = New-Object System.Drawing.Font("Consolas", 18)
    $animalLabel.ForeColor = HexToColor $T.animal
    $animalLabel.BackColor = [System.Drawing.Color]::Transparent
    $animalLabel.TextAlign = "MiddleCenter"
    $animalLabel.Size = New-Object System.Drawing.Size(400, 100)
    $animalLabel.Location = New-Object System.Drawing.Point(0, 340)

    # Timer text
    $timerLabel = New-Object System.Windows.Forms.Label
    $timerLabel.Font = New-Object System.Drawing.Font("Segoe UI", 22)
    $timerLabel.ForeColor = HexToColor $T.timer
    $timerLabel.BackColor = [System.Drawing.Color]::Transparent
    $timerLabel.TextAlign = "MiddleCenter"
    $timerLabel.Size = New-Object System.Drawing.Size(500, 40)
    $timerLabel.Location = New-Object System.Drawing.Point(0, 440)

    # Progress bar
    $barra = New-Object System.Windows.Forms.ProgressBar
    $barra.Size = New-Object System.Drawing.Size(400, 10)
    $barra.Location = New-Object System.Drawing.Point(0, 490)
    $barra.Style = "Continuous"
    $barra.ForeColor = HexToColor $T.pfill
    $barra.BackColor = HexToColor "#1a1a3a"

    # Info text
    $infoLabel = New-Object System.Windows.Forms.Label
    $infoLabel.Font = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Italic)
    $infoLabel.ForeColor = HexToColor $T.info
    $infoLabel.BackColor = [System.Drawing.Color]::Transparent
    $infoLabel.TextAlign = "MiddleCenter"
    $infoLabel.Size = New-Object System.Drawing.Size(600, 60)
    $infoLabel.Location = New-Object System.Drawing.Point(0, 520)
    if ($Modo -eq "visual") { $infoLabel.Text = $curiosidades | Get-Random }
    else { $infoLabel.Text = $consejos | Get-Random }

    $form.Controls.AddRange(@($animalLabel, $timerLabel, $barra, $infoLabel))

    # Close button (hidden until timer ends)
    $btnUnlock = New-Object System.Windows.Forms.Button
    $btnUnlock.Font = New-Object System.Drawing.Font("Segoe UI", 16)
    $btnUnlock.Text = "Desbloquear"
    $btnUnlock.Size = New-Object System.Drawing.Size(200, 50)
    $btnUnlock.FlatStyle = "Flat"
    $btnUnlock.FlatAppearance.BorderSize = 2
    $btnUnlock.BackColor = HexToColor $T.btn
    $btnUnlock.ForeColor = [System.Drawing.Color]::White
    $btnUnlock.FlatAppearance.BorderColor = HexToColor $T.btn
    $btnUnlock.Visible = $false
    $form.Controls.Add($btnUnlock)
    $btnUnlock.Add_Click({ $form.Close() })

    # Animal animation timer (400ms)
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

    # Countdown timer (100ms)
    $timer = New-Object System.Windows.Forms.Timer
    $timer.Interval = 100
    $timer.Add_Tick({
        $transcurrido = $inicio.Elapsed.TotalSeconds
        $restante = [Math]::Max(0, $Segundos - $transcurrido)
        $tiempoRestante = [int]$restante

        $reloj.Text = (Get-Date).ToString("HH:mm")
        $fecha.Text = (Get-Date).ToString("dddd, d 'de' MMMM 'de' yyyy")
        $barra.Value = [Math]::Min([int](($transcurrido / $Segundos) * 100), 100)
        $timerLabel.Text = "Descansa $tiempoRestante segundos más…"

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
    # No timer modes (almuerzo/dormir)
    $form.Cursor = [System.Windows.Forms.Cursors]::Default

    # Icon
    $iconLines = if ($Modo -eq "almuerzo") { $bowl } else { $moon }
    $iconLabel = New-Object System.Windows.Forms.Label
    $iconLabel.Font = New-Object System.Drawing.Font("Consolas", 18)
    $iconLabel.ForeColor = HexToColor $T.animal
    $iconLabel.BackColor = [System.Drawing.Color]::Transparent
    $iconLabel.TextAlign = "MiddleCenter"
    $iconLabel.Size = New-Object System.Drawing.Size(400, 120)
    $iconLabel.Location = New-Object System.Drawing.Point(0, 330)
    $iconLabel.Text = ($iconLines | ForEach-Object { $_.PadLeft($maxW).PadRight($maxW) }) -join "`n"
    $form.Controls.Add($iconLabel)

    # Title
    $titleLabel = New-Object System.Windows.Forms.Label
    $titleLabel.Font = New-Object System.Drawing.Font("Segoe UI", 38, [System.Drawing.FontStyle]::Bold)
    $titleLabel.ForeColor = HexToColor $T.title
    $titleLabel.BackColor = [System.Drawing.Color]::Transparent
    $titleLabel.TextAlign = "MiddleCenter"
    $titleLabel.Size = New-Object System.Drawing.Size(600, 60)
    $titleLabel.Location = New-Object System.Drawing.Point(0, 450)
    $titleLabel.Text = $T.titulo
    $form.Controls.Add($titleLabel)

    # Text
    $items = $T.item
    if ($items -and $items.Count -gt 0) {
        $textLabel = New-Object System.Windows.Forms.Label
        $textLabel.Font = New-Object System.Drawing.Font("Segoe UI", 20)
        $textLabel.ForeColor = HexToColor $T.text
        $textLabel.BackColor = [System.Drawing.Color]::Transparent
        $textLabel.TextAlign = "MiddleCenter"
        $textLabel.Size = New-Object System.Drawing.Size(600, 40)
        $textLabel.Location = New-Object System.Drawing.Point(0, 520)
        $textLabel.Text = $items | Get-Random
        $form.Controls.Add($textLabel)
    }

    # Footer
    $footLabel = New-Object System.Windows.Forms.Label
    $footLabel.Font = New-Object System.Drawing.Font("Segoe UI", 14, [System.Drawing.FontStyle]::Italic)
    $footLabel.ForeColor = HexToColor $T.info
    $footLabel.BackColor = [System.Drawing.Color]::Transparent
    $footLabel.TextAlign = "MiddleCenter"
    $footLabel.Size = New-Object System.Drawing.Size(600, 30)
    $footLabel.Location = New-Object System.Drawing.Point(0, 570)
    $footLabel.Text = "Tómate tu tiempo, la pantalla seguirá aquí."
    $form.Controls.Add($footLabel)

    # Close button (always visible)
    $btnClose = New-Object System.Windows.Forms.Button
    $btnClose.Font = New-Object System.Drawing.Font("Segoe UI", 16)
    $btnClose.Text = "Cerrar"
    $btnClose.Size = New-Object System.Drawing.Size(200, 50)
    $btnClose.FlatStyle = "Flat"
    $btnClose.FlatAppearance.BorderSize = 2
    $btnClose.BackColor = HexToColor $T.btn
    $btnClose.ForeColor = [System.Drawing.Color]::White
    $btnClose.FlatAppearance.BorderColor = HexToColor $T.btn
    $btnClose.Location = New-Object System.Drawing.Point(0, 620)
    $form.Controls.Add($btnClose)
    $btnClose.Add_Click({ $form.Close() })

    # Close after 10 min
    $closeTimer = New-Object System.Windows.Forms.Timer
    $closeTimer.Interval = 600000
    $closeTimer.Add_Tick({ $form.Close() })
    $closeTimer.Start()
}

# --- Events ---
$form.Add_KeyDown({
    if ($_.KeyCode -eq "Escape") {
        if (-not $tieneTimer -or $tiempoRestante -le 0) { $form.Close() }
    }
    if ($_.KeyCode -eq "Enter") {
        if ($btnUnlock.Visible) { $form.Close() }
    }
})

$form.Add_Shown({
    $form.Activate()
    $reloj.Left = ($form.ClientSize.Width - $reloj.Width) / 2
    $fecha.Left = ($form.ClientSize.Width - $fecha.Width) / 2
    if ($tieneTimer) {
        $animalLabel.Left = ($form.ClientSize.Width - $animalLabel.Width) / 2
        $timerLabel.Left = ($form.ClientSize.Width - $timerLabel.Width) / 2
        $barra.Left = ($form.ClientSize.Width - $barra.Width) / 2
        $infoLabel.Left = ($form.ClientSize.Width - $infoLabel.Width) / 2
        $btnUnlock.Left = ($form.ClientSize.Width - $btnUnlock.Width) / 2
    } else {
        $iconLabel.Left = ($form.ClientSize.Width - $iconLabel.Width) / 2
        $titleLabel.Left = ($form.ClientSize.Width - $titleLabel.Width) / 2
        if ($textLabel) { $textLabel.Left = ($form.ClientSize.Width - $textLabel.Width) / 2 }
        $footLabel.Left = ($form.ClientSize.Width - $footLabel.Width) / 2
        $btnClose.Left = ($form.ClientSize.Width - $btnClose.Width) / 2
    }
})

[System.Windows.Forms.Application]::Run($form)
