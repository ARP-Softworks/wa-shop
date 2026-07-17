$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
$Image = if ($args.Count -gt 0) { $args[0] } else { "wa-shop:local" }

Write-Host "==> docker build -t $Image"
Set-Location $RootDir
docker build -t $Image .

Write-Host "==> Image built: $Image"
docker image inspect $Image --format "{{.Id}} {{.Size}}"
