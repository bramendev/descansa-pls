param([int]$Segundos = 30)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$curiosidades = @(
    "Los pulpos tienen tres corazones y su sangre es azul."
    "Un día en Venus dura más que un año en Venus."
    "Las hormigas nunca duermen; toman micro-siestas de 8 segundos."
    "El corazón humano late unas 100.000 veces al día."
    "Los árboles se comunican entre sí por una red de hongos llamada 'Wood Wide Web'."
    "El agua caliente se congela más rápido que el agua fría (efecto Mpemba)."
    "Los flamencos no nacen rosados, se vuelven rosados por su alimentación."
    "La miel nunca se echa a perder. Se encontró miel comestible en tumbas egipcias de 3.000 años."
    "Las nubes no son ingrávidas; una nube típica pesa unas 500 toneladas."
    "El ojo humano puede distinguir unos 10 millones de colores."
    "Los koalas tienen huellas dactilares casi idénticas a las humanas."
    "La Tierra viaja a 107.000 km/h alrededor del Sol."
    "Las jirafas solo necesitan dormir 30 minutos al día."
    "Apagar la vista 20 segundos cada 20 minutos reduce la fatiga visual un 40%."
    "Mirar algo a 6 metros de distancia relaja los músculos oculares."
    "Los bebés nacen sin rótulas; se desarrollan entre los 2 y 6 años."
    "Las huellas de la lengua son tan únicas como las huellas dactilares."
    "El ADN humano tiene un 50% de similitud con el de un plátano."
    "La mariposa monarca viaja 4.000 km cada año para migrar."
    "Una persona produce suficiente saliva en su vida para llenar dos piscinas."
    "El brócoli tiene más proteína que el filete por cada 100 calorías."
    "Hay más estrellas en el universo que granos de arena en todas las playas."
    "La luz tarda 8 min y 20 s en viajar del Sol a la Tierra."
    "El chocolate blanco no es chocolate (no tiene pasta de cacao)."
    "Los gatos no pueden saborear lo dulce."
    "Las vacas tienen mejores amigos y se estresan si se separan de ellos."
    "Las pirámides de Egipto se construyeron con más precisión que muchos edificios modernos."
    "Los pingüinos proponen matrimonio con una piedra."
    "Tu cuerpo reemplaza millones de células cada segundo."
    "La risa libera endorfinas y reduce el estrés."
)

$form = New-Object System.Windows.Forms.Form
$form.Text = "Descanso Visual"
$form.WindowState = "Maximized"
$form.FormBorderStyle = "None"
$form.TopMost = $true
$form.BackColor = [System.Drawing.Color]::FromArgb(10, 10, 26)
$form.Cursor = [System.Windows.Forms.Cursors]::No
$form.ControlBox = $false
$form.ShowInTaskbar = $false

$form.KeyPreview = $true

$tiempoRestante = $Segundos
$inicio = [System.Diagnostics.Stopwatch]::StartNew()

$reloj = New-Object System.Windows.Forms.Label
$reloj.Font = New-Object System.Drawing.Font("Segoe UI", 72, [System.Drawing.FontStyle]::Bold)
$reloj.ForeColor = [System.Drawing.Color]::FromArgb(224, 224, 255)
$reloj.BackColor = [System.Drawing.Color]::Transparent
$reloj.TextAlign = "MiddleCenter"
$reloj.Size = New-Object System.Drawing.Size(600, 120)
$reloj.Location = New-Object System.Drawing.Point(0, 180)
$reloj.Left = ($form.ClientSize.Width - $reloj.Width) / 2
$reloj.Anchor = "None"

$fecha = New-Object System.Windows.Forms.Label
$fecha.Font = New-Object System.Drawing.Font("Segoe UI", 18)
$fecha.ForeColor = [System.Drawing.Color]::FromArgb(144, 144, 176)
$fecha.BackColor = [System.Drawing.Color]::Transparent
$fecha.TextAlign = "MiddleCenter"
$fecha.Size = New-Object System.Drawing.Size(500, 40)
$fecha.Location = New-Object System.Drawing.Point(0, 300)
$fecha.Left = ($form.ClientSize.Width - $fecha.Width) / 2
$fecha.Anchor = "None"

$timerLabel = New-Object System.Windows.Forms.Label
$timerLabel.Font = New-Object System.Drawing.Font("Segoe UI", 24)
$timerLabel.ForeColor = [System.Drawing.Color]::FromArgb(255, 204, 102)
$timerLabel.BackColor = [System.Drawing.Color]::Transparent
$timerLabel.TextAlign = "MiddleCenter"
$timerLabel.Size = New-Object System.Drawing.Size(500, 50)
$timerLabel.Location = New-Object System.Drawing.Point(0, 360)
$timerLabel.Left = ($form.ClientSize.Width - $timerLabel.Width) / 2
$timerLabel.Anchor = "None"

$barra = New-Object System.Windows.Forms.ProgressBar
$barra.Size = New-Object System.Drawing.Size(500, 12)
$barra.Location = New-Object System.Drawing.Point(0, 420)
$barra.Left = ($form.ClientSize.Width - $barra.Width) / 2
$barra.Style = "Continuous"
$barra.ForeColor = [System.Drawing.Color]::FromArgb(102, 136, 255)
$barra.BackColor = [System.Drawing.Color]::FromArgb(26, 26, 58)
$barra.Anchor = "None"

$factLabel = New-Object System.Windows.Forms.Label
$factLabel.Font = New-Object System.Drawing.Font("Segoe UI", 16, [System.Drawing.FontStyle]::Italic)
$factLabel.ForeColor = [System.Drawing.Color]::FromArgb(176, 176, 208)
$factLabel.BackColor = [System.Drawing.Color]::Transparent
$factLabel.TextAlign = "MiddleCenter"
$factLabel.Size = New-Object System.Drawing.Size(600, 60)
$factLabel.Location = New-Object System.Drawing.Point(0, 460)
$factLabel.Left = ($form.ClientSize.Width - $factLabel.Width) / 2
$factLabel.Text = $curiosidades | Get-Random
$factLabel.Anchor = "None"

$btnUnlock = New-Object System.Windows.Forms.Button
$btnUnlock.Font = New-Object System.Drawing.Font("Segoe UI", 16)
$btnUnlock.Text = "Desbloquear"
$btnUnlock.Size = New-Object System.Drawing.Size(200, 50)
$btnUnlock.Location = New-Object System.Drawing.Point(0, 540)
$btnUnlock.Left = ($form.ClientSize.Width - $btnUnlock.Width) / 2
$btnUnlock.FlatStyle = "Flat"
$btnUnlock.FlatAppearance.BorderSize = 2
$btnUnlock.BackColor = [System.Drawing.Color]::FromArgb(51, 68, 170)
$btnUnlock.ForeColor = [System.Drawing.Color]::White
$btnUnlock.FlatAppearance.BorderColor = [System.Drawing.Color]::FromArgb(85, 102, 204)
$btnUnlock.Visible = $false
$btnUnlock.Anchor = "None"

$form.Controls.AddRange(@($reloj, $fecha, $timerLabel, $barra, $factLabel, $btnUnlock))

$timer = New-Object System.Windows.Forms.Timer
$timer.Interval = 100
$timer.Add_Tick({
    $transcurrido = $inicio.Elapsed.TotalSeconds
    $restante = [Math]::Max(0, $Segundos - $transcurrido)
    $tiempoRestante = [int]$restante
    $fraccion = [Math]::Min($transcurrido / $Segundos, 1.0)

    $reloj.Text = (Get-Date).ToString("HH:mm")
    $fecha.Text = (Get-Date).ToString("dddd, d 'de' MMMM 'de' yyyy")
    $barra.Value = [int]($fraccion * 100)
    $timerLabel.Text = "Descansa $tiempoRestante segundos más…"

    if ($restante -le 0) {
        $barra.Value = 100
        $timerLabel.Text = "¡Ya puedes continuar!"
        $btnUnlock.Visible = $true
        $form.Cursor = [System.Windows.Forms.Cursors]::Default
        $timer.Stop()
    }
})

$form.Add_KeyDown({
    if ($_.KeyCode -eq "Escape" -and $tiempoRestante -le 0) { $form.Close() }
    if ($_.KeyCode -eq "Enter" -and $btnUnlock.Visible) { $form.Close() }
})

$btnUnlock.Add_Click({ $form.Close() })

$form.Add_Shown({
    $form.Activate()
    $reloj.Left = ($form.ClientSize.Width - $reloj.Width) / 2
    $fecha.Left = ($form.ClientSize.Width - $fecha.Width) / 2
    $timerLabel.Left = ($form.ClientSize.Width - $timerLabel.Width) / 2
    $barra.Left = ($form.ClientSize.Width - $barra.Width) / 2
    $factLabel.Left = ($form.ClientSize.Width - $factLabel.Width) / 2
    $btnUnlock.Left = ($form.ClientSize.Width - $btnUnlock.Width) / 2
    $timer.Start()
})

[System.Windows.Forms.Application]::Run($form)
