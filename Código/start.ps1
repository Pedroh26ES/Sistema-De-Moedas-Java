$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "Subindo RabbitMQ do Valoriza Ae..."
docker compose up -d rabbitmq

Write-Host "Aguardando RabbitMQ ficar pronto..."
$ready = $false
for ($i = 1; $i -le 30; $i++) {
    $status = docker inspect -f "{{.State.Health.Status}}" valoriza-rabbitmq 2>$null
    if ($status -eq "healthy") {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
}

if (-not $ready) {
    throw "RabbitMQ nao ficou pronto. Abra o Docker Desktop e tente novamente."
}

Write-Host "Gerando frontend React..."
npm run build:frontend

Write-Host "Iniciando Valoriza Ae em http://localhost:8080 ..."
mvn quarkus:dev
