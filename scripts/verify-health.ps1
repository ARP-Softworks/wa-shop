$ErrorActionPreference = "Stop"

$BaseUrl = if ($args.Count -gt 0) { $args[0].TrimEnd("/") } else { "http://127.0.0.1:8080" }

Write-Host "==> GET $BaseUrl/api/health"
$health = Invoke-RestMethod -Uri "$BaseUrl/api/health" -Method Get
if ($health.status -ne "UP") { throw "Custom health not UP: $($health | ConvertTo-Json)" }
Write-Host "OK /api/health => $($health.status)"

Write-Host "==> GET $BaseUrl/api/actuator/health"
$actuator = Invoke-RestMethod -Uri "$BaseUrl/api/actuator/health" -Method Get
if ($actuator.status -ne "UP") { throw "Actuator health not UP: $($actuator | ConvertTo-Json)" }
Write-Host "OK /api/actuator/health => $($actuator.status)"

Write-Host "==> Admin API must not be public"
try {
  Invoke-WebRequest -Uri "$BaseUrl/api/admin/dashboard" -Method Get -UseBasicParsing | Out-Null
  throw "Admin dashboard should not be publicly reachable"
} catch {
  if ($_.Exception.Response.StatusCode.value__ -notin @(401, 403)) {
    throw "Unexpected status for admin dashboard: $($_.Exception.Response.StatusCode.value__)"
  }
  Write-Host "OK /api/admin/dashboard => $($_.Exception.Response.StatusCode.value__)"
}

Write-Host "==> SPA fallback (reload path)"
$spa = Invoke-WebRequest -Uri "$BaseUrl/catalogo/demo-reload" -Method Get -UseBasicParsing
if ($spa.StatusCode -ne 200) { throw "SPA route status $($spa.StatusCode)" }
if ($spa.Content -notmatch "<app-root|index|WA Shop|ng-") {
  Write-Host "WARN: response does not look like Angular index.html (check content)"
} else {
  Write-Host "OK SPA route returned HTML"
}

Write-Host "Health verification passed."
