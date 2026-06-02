$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

function Resolve-DockerCommand {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($docker) {
        return $docker.Source
    }

    $candidates = @(
        "C:\Program Files\Docker\Docker\resources\bin\docker.exe",
        "$env:LOCALAPPDATA\Programs\DockerDesktop\resources\bin\docker.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    Write-Host ""
    Write-Host "Docker nao foi encontrado neste terminal." -ForegroundColor Yellow
    Write-Host "Para testar WhatsApp via WAHA, instale/abra o Docker Desktop e reabra o VS Code." -ForegroundColor Yellow
    Write-Host "Depois confirme com: docker --version" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Se quiser rodar apenas o sistema sem WhatsApp/RabbitMQ real, use: .\start-local.ps1" -ForegroundColor Cyan
    throw "WAHA e RabbitMQ reais dependem do Docker neste projeto."
}

function Resolve-DockerDesktopCommand {
    $candidates = @(
        "$env:LOCALAPPDATA\Programs\DockerDesktop\Docker Desktop.exe",
        "$env:LOCALAPPDATA\Programs\DockerDesktop\frontend\Docker Desktop.exe",
        "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }
    return $null
}

function Test-DockerDaemon {
    param([string]$DockerCommand)
    & $DockerCommand info *> $null
    return $LASTEXITCODE -eq 0
}

function Ensure-DockerDaemon {
    param([string]$DockerCommand)

    if (Test-DockerDaemon $DockerCommand) {
        return
    }

    Write-Host "Docker encontrado, mas o motor ainda nao esta rodando." -ForegroundColor Yellow
    $desktop = Resolve-DockerDesktopCommand
    if ($desktop) {
        Write-Host "Iniciando Docker Desktop pelo terminal..." -ForegroundColor Cyan
        Start-Process -FilePath $desktop -WindowStyle Hidden
    } else {
        Write-Host "Nao encontrei o Docker Desktop para iniciar automaticamente." -ForegroundColor Yellow
    }

    Write-Host "Aguardando o Docker ficar pronto..."
    for ($i = 1; $i -le 90; $i++) {
        Start-Sleep -Seconds 2
        if (Test-DockerDaemon $DockerCommand) {
            Write-Host "Docker pronto." -ForegroundColor Green
            return
        }
    }

    Write-Host ""
    Write-Host "O Docker Desktop nao iniciou o motor a tempo." -ForegroundColor Yellow
    Write-Host "Se esta for a primeira execucao apos instalar, reinicie o Windows e tente novamente." -ForegroundColor Yellow
    Write-Host "Depois confirme com: docker info" -ForegroundColor Yellow
    throw "WAHA e RabbitMQ reais dependem do motor do Docker em execucao."
}


$docker = Resolve-DockerCommand
Ensure-DockerDaemon $docker

if (-not $env:VALORIZA_WAHA_HOOK_URL) { $env:VALORIZA_WAHA_HOOK_URL="http://app:8080/api/whatsapp/webhook" }
if (-not $env:VALORIZA_WAHA_HOOK_EVENTS) { $env:VALORIZA_WAHA_HOOK_EVENTS="message" }

Write-Host "Subindo Valoriza Ae completo com RabbitMQ e WAHA..."
& $docker compose up --build
