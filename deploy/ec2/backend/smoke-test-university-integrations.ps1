param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$AdminUsername = $env:APP_BOOTSTRAP_ADMIN_USERNAME,
  [string]$AdminPassword = $env:APP_BOOTSTRAP_ADMIN_PASSWORD,
  [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

function Read-DotEnv([string]$Path) {
  $values = @{}
  if (-not (Test-Path -LiteralPath $Path)) {
    return $values
  }
  Get-Content -LiteralPath $Path | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) {
      return
    }
    $parts = $line.Split("=", 2)
    $values[$parts[0].Trim()] = $parts[1].Trim()
  }
  return $values
}

function Value-OrDefault($Values, [string]$Name, [string]$Default = "") {
  if ($Values.ContainsKey($Name) -and $Values[$Name]) {
    return $Values[$Name]
  }
  $envValue = [Environment]::GetEnvironmentVariable($Name)
  if ($envValue) {
    return $envValue
  }
  return $Default
}

$envValues = Read-DotEnv $EnvFile
$username = if ($AdminUsername) { $AdminUsername } else { Value-OrDefault $envValues "APP_BOOTSTRAP_ADMIN_USERNAME" "admin" }
$password = if ($AdminPassword) { $AdminPassword } else { Value-OrDefault $envValues "APP_BOOTSTRAP_ADMIN_PASSWORD" "" }

if (-not $password) {
  throw "Admin password is required. Pass -AdminPassword or set APP_BOOTSTRAP_ADMIN_PASSWORD in the env file."
}

$loginBody = @{ username = $username; password = $password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/public/auth/login" -ContentType "application/json" -Body $loginBody
$headers = @{ Authorization = "Bearer $($login.token)" }

$connections = @(
  @{
    integrationKey = "lms"
    displayName = "Learning Management System"
    endpointUrl = Value-OrDefault $envValues "UNIVERSITY_ERP_LMS_ENDPOINT"
    adapterMode = "HTTP"
    authType = Value-OrDefault $envValues "UNIVERSITY_ERP_LMS_AUTH_TYPE" "BEARER_TOKEN"
    secretRef = Value-OrDefault $envValues "UNIVERSITY_ERP_LMS_SECRET_REF" "env:UNIVERSITY_ERP_LMS_TOKEN"
    enabled = $true
  },
  @{
    integrationKey = "bank"
    displayName = "Bank payment gateway"
    endpointUrl = Value-OrDefault $envValues "UNIVERSITY_ERP_BANK_ENDPOINT"
    adapterMode = "HTTP"
    authType = Value-OrDefault $envValues "UNIVERSITY_ERP_BANK_AUTH_TYPE" "API_KEY"
    secretRef = Value-OrDefault $envValues "UNIVERSITY_ERP_BANK_SECRET_REF" "env:UNIVERSITY_ERP_BANK_API_KEY"
    enabled = $true
  },
  @{
    integrationKey = "notification"
    displayName = "Notification service"
    endpointUrl = Value-OrDefault $envValues "UNIVERSITY_ERP_NOTIFICATION_ENDPOINT"
    adapterMode = "HTTP"
    authType = Value-OrDefault $envValues "UNIVERSITY_ERP_NOTIFICATION_AUTH_TYPE" "API_KEY"
    secretRef = Value-OrDefault $envValues "UNIVERSITY_ERP_NOTIFICATION_SECRET_REF" "env:UNIVERSITY_ERP_NOTIFICATION_API_KEY"
    enabled = $true
  },
  @{
    integrationKey = "government"
    displayName = "Government reporting"
    endpointUrl = Value-OrDefault $envValues "UNIVERSITY_ERP_GOVERNMENT_ENDPOINT"
    adapterMode = "HTTP"
    authType = Value-OrDefault $envValues "UNIVERSITY_ERP_GOVERNMENT_AUTH_TYPE" "BEARER_TOKEN"
    secretRef = Value-OrDefault $envValues "UNIVERSITY_ERP_GOVERNMENT_SECRET_REF" "env:UNIVERSITY_ERP_GOVERNMENT_TOKEN"
    enabled = $true
  }
)

foreach ($connection in $connections) {
  if (-not $connection.endpointUrl) {
    throw "$($connection.integrationKey) endpoint is missing in $EnvFile or environment."
  }
  $body = $connection | ConvertTo-Json
  Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/university-erp/integrations/connections" -Headers $headers -ContentType "application/json" -Body $body | Out-Null
  Write-Host "Configured $($connection.integrationKey)"
}

$smoke = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/university-erp/integrations/smoke-test" -Headers $headers
$smoke | Format-Table key, adapterMode, authType, status, secretResolved, message -AutoSize

$failed = @($smoke | Where-Object { $_.status -ne "READY" })
if ($failed.Count -gt 0) {
  throw "$($failed.Count) integration smoke test(s) failed."
}

Write-Host "All university ERP integration smoke tests passed."
