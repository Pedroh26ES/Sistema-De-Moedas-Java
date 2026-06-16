$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

function Import-LocalEnv {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return
    }

    foreach ($rawLine in Get-Content $Path) {
        $line = $rawLine.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            continue
        }

        $separator = $line.IndexOf("=")
        if ($separator -le 0) {
            continue
        }

        $name = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        Set-Item -Path "Env:$name" -Value $value
    }
}

Import-LocalEnv "$projectDir\.env.local"

Write-Host "Iniciando Valoriza Ae local sem Docker..."
Write-Host "Este modo nao envia WhatsApp real. EmailJS sera usado se estiver habilitado no .env.local."

$env:VALORIZA_RABBITMQ_FAIL_ON_STARTUP="false"
$env:VALORIZA_RABBITMQ_AUTO_START="true"
$env:VALORIZA_RABBITMQ_LOCAL_FALLBACK="true"
$env:VALORIZA_WHATSAPP_ENABLED="false"
if (-not $env:VALORIZA_WHATSAPP_RECIPIENT_OVERRIDES) { $env:VALORIZA_WHATSAPP_RECIPIENT_OVERRIDES="" }
if (-not $env:VALORIZA_EMAILJS_ENABLED) { $env:VALORIZA_EMAILJS_ENABLED="false" }

Write-Host "Gerando frontend React..."
npm run build:frontend

Write-Host "Iniciando Valoriza Ae em http://localhost:8080 ..."
mvn quarkus:dev
