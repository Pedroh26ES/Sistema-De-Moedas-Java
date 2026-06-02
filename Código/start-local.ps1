$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "Iniciando Valoriza Ae local sem Docker..."
Write-Host "Este modo nao envia WhatsApp nem e-mail real. As notificacoes ficam no painel."

$env:VALORIZA_RABBITMQ_FAIL_ON_STARTUP="false"
$env:VALORIZA_RABBITMQ_AUTO_START="true"
$env:VALORIZA_RABBITMQ_LOCAL_FALLBACK="true"
$env:VALORIZA_WHATSAPP_ENABLED="false"
if (-not $env:VALORIZA_WHATSAPP_RECIPIENT_OVERRIDES) { $env:VALORIZA_WHATSAPP_RECIPIENT_OVERRIDES="" }
$env:VALORIZA_EMAILJS_ENABLED="false"

Write-Host "Gerando frontend React..."
npm run build:frontend

Write-Host "Iniciando Valoriza Ae em http://localhost:8080 ..."
mvn quarkus:dev
