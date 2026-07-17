$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot

Write-Host "==> Frontend tests"
Set-Location "$RootDir\frontend"
npm ci
npm test -- --watch=false --browsers=ChromeHeadless

Write-Host "==> Frontend production build"
npm run build -- --configuration=production

Write-Host "==> Backend tests + package (with frontend assets)"
Set-Location "$RootDir\backend"
mvn -B test
mvn -B "-Pwith-frontend" package -DskipTests

Write-Host "==> Done. JAR: backend\target\wa-shop-*.jar"
Get-ChildItem "$RootDir\backend\target\wa-shop-*.jar" | ForEach-Object { Write-Host $_.FullName }
