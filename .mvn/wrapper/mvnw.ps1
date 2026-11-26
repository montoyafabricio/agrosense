<#
.SYNOPSIS
Maven Wrapper PowerShell script
#>
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Args
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent (Split-Path -Parent $scriptDir)
$propFile = Join-Path $scriptDir "maven-wrapper.properties"

$mvnVersion = "3.9.9"
$distUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mvnVersion/apache-maven-$mvnVersion-bin.zip"

if (Test-Path $propFile) {
    Get-Content $propFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -match "^distributionUrl=(.+)$") {
            $distUrl = $matches[1].Replace('\:', ':')
        }
    }
}

$mvnHome = Join-Path $HOME ".m2\wrapper\dists\apache-maven-$mvnVersion"
$mvnBin = Join-Path $mvnHome "apache-maven-$mvnVersion\bin\mvn.cmd"

if (-not (Test-Path $mvnBin)) {
    Write-Host "Descargando Apache Maven $mvnVersion..." -ForegroundColor Cyan
    $zipPath = Join-Path $env:TEMP "apache-maven-$mvnVersion-bin.zip"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $distUrl -OutFile $zipPath
    Write-Host "Extrayendo Maven..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $mvnHome | Out-Null
    Expand-Archive -Path $zipPath -DestinationPath $mvnHome -Force
    Remove-Item -Path $zipPath -Force
}

$mvnBase = Get-ChildItem -Path $mvnHome -Directory | Select-Object -First 1 -ExpandProperty FullName
$mvnExecutable = Join-Path $mvnBase "bin\mvn.cmd"

if (-not (Test-Path $mvnExecutable)) {
    $mvnExecutable = Join-Path $mvnHome "bin\mvn.cmd"
}

& $mvnExecutable @Args
exit $LASTEXITCODE
