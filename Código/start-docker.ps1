$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "Subindo Valoriza Ae completo com RabbitMQ..."
docker compose up --build
