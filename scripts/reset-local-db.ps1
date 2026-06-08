param(
    [string]$Database = "scenik",
    [string]$User = "ssafy",
    [string]$Password = "ssafy",
    [string]$HostName = "localhost"
)

$ErrorActionPreference = "Stop"
$BackendRoot = Split-Path -Parent $PSScriptRoot
$Schema = Join-Path $BackendRoot "src/main/resources/schema.sql"
$Data = Join-Path $BackendRoot "src/main/resources/data.sql"

Write-Host "Dropping and recreating MySQL database '$Database' on $HostName..."
mysql -h $HostName -u $User "-p$Password" -e "DROP DATABASE IF EXISTS $Database; CREATE DATABASE $Database DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;"

Write-Host "Applying schema.sql..."
cmd /c "mysql -h $HostName -u $User -p$Password $Database < `"$Schema`""

if (Test-Path $Data) {
    Write-Host "Applying data.sql..."
    cmd /c "mysql -h $HostName -u $User -p$Password $Database < `"$Data`""
}

Write-Host "Local backend DB reset complete."
Write-Host "Create demo users through POST /api/auth/signup so password hashes use the application code."
