param(
    [int]$Minutos = 20,
    [int]$SegundosDescanso = 30
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$pantalla = Join-Path $scriptDir "pantalla-descanso.ps1"
$intervalo = $Minutos * 60

while ($true) {
    Start-Sleep -Seconds $intervalo
    if (Test-Path $pantalla) {
        & $pantalla -Segundos $SegundosDescanso
    }
}
