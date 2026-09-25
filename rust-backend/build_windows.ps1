<#
.SYNOPSIS
    Build the Rust spice-backend native library for Windows desktop.
.DESCRIPTION
    Builds spice_backend.dll for x86_64-pc-windows-msvc and copies it to the
    desktop Compose resources so JNA can load the same C ABI used by Android
    and iOS.
.NOTES
    Requires the MSVC linker from Visual Studio Build Tools or Visual Studio.
    The Rust target is installed automatically after confirmation when needed.
#>

$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "SpiceCompose: Build Windows Rust Library"

$windowsTarget = "x86_64-pc-windows-msvc"
$libraryName = "spice_backend.dll"
$targetReleaseDir = Join-Path $PSScriptRoot "target\$windowsTarget\release"
$sourceLibrary = Join-Path $targetReleaseDir $libraryName
$desktopResources = Join-Path $PSScriptRoot "..\composeApp\src\desktopMain\resources"
$desktopLibrary = Join-Path $desktopResources $libraryName

function Write-Stage([string]$title) {
    Write-Host "`n============================================================" -ForegroundColor Cyan
    Write-Host ">>> $title" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Fail([string]$message) {
    Write-Host "`n>>> ERROR: $message" -ForegroundColor Red
    exit 1
}

# ============================================================================
# Stage 0: Check Rust toolchain
# ============================================================================

Write-Stage "Stage 0: Checking Rust toolchain..."

$cargo = Get-Command cargo -ErrorAction SilentlyContinue
$rustup = Get-Command rustup -ErrorAction SilentlyContinue
if (-not $cargo -or -not $rustup) {
    if ($cargo) {
        Write-Host "    cargo      OK" -ForegroundColor Green
    } else {
        Write-Host "    cargo      NOT FOUND" -ForegroundColor Red
    }
    if ($rustup) {
        Write-Host "    rustup     OK" -ForegroundColor Green
    } else {
        Write-Host "    rustup     NOT FOUND" -ForegroundColor Red
    }
    Write-Host "    Install Rust from https://rustup.rs and run this script again." -ForegroundColor Red
    exit 1
}

$rustcVersion = & rustc --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Fail "Rust compiler is not available. Run 'rustup default stable' or 'rustup default nightly'."
}
Write-Host "    rustc      OK  $rustcVersion" -ForegroundColor Green
Write-Host "    cargo      OK" -ForegroundColor Green
Write-Host "    rustup     OK" -ForegroundColor Green

# ============================================================================
# Stage 1: Check Windows target
# ============================================================================

Write-Stage "Stage 1: Checking Windows cross-compilation target..."

$installedTargets = @(& rustup target list --installed)
if ($installedTargets -contains $windowsTarget) {
    Write-Host "    $windowsTarget  OK" -ForegroundColor Green
} else {
    Write-Host "    $windowsTarget  NOT INSTALLED" -ForegroundColor Red
    $answer = Read-Host "    Install this target now? [Y/n]"
    if ($answer -eq '' -or $answer -match '^[Yy]') {
        & rustup target add $windowsTarget
        if ($LASTEXITCODE -ne 0) {
            Fail "Failed to install $windowsTarget."
        }
        Write-Host "    Target installed." -ForegroundColor Green
    } else {
        Write-Host "    Aborting. Install manually with:" -ForegroundColor Red
        Write-Host "      rustup target add $windowsTarget" -ForegroundColor Red
        exit 1
    }
}

# ============================================================================
# Stage 2: Build the Rust native library
# ============================================================================

Write-Stage "Stage 2: Building Rust library for Windows..."

Push-Location $PSScriptRoot
try {
    & cargo build --release --target $windowsTarget --lib
    if ($LASTEXITCODE -ne 0) {
        Fail "Rust build failed. Verify that the MSVC linker is installed and available."
    }
} finally {
    Pop-Location
}

if (-not (Test-Path $sourceLibrary)) {
    Fail "Expected library was not produced: $sourceLibrary"
}

$sourceSize = [math]::Round((Get-Item $sourceLibrary).Length / 1KB, 1)
Write-Host "    $libraryName built (${sourceSize} KB)" -ForegroundColor Green

# ============================================================================
# Stage 3: Deploy the DLL to desktop resources
# ============================================================================

Write-Stage "Stage 3: Copying DLL to desktop resources..."

New-Item -ItemType Directory -Force -Path $desktopResources | Out-Null
Copy-Item -Path $sourceLibrary -Destination $desktopLibrary -Force

if (-not (Test-Path $desktopLibrary)) {
    Fail "Failed to copy the desktop library to $desktopLibrary"
}

$deployedSize = [math]::Round((Get-Item $desktopLibrary).Length / 1KB, 1)
Write-Host "    $desktopLibrary" -ForegroundColor Green
Write-Host "    Deployed size: ${deployedSize} KB" -ForegroundColor Green

# ============================================================================
# Stage 4: Verify the exported C ABI symbols
# ============================================================================

Write-Stage "Stage 4: Verifying native library..."

$dumpbin = Get-Command dumpbin.exe -ErrorAction SilentlyContinue
if ($dumpbin) {
    $exports = & $dumpbin.Source /exports $desktopLibrary 2>$null
    $requiredExports = @(
        "spice_native_connect",
        "spice_native_request",
        "spice_native_touch_request",
        "spice_native_disconnect",
        "spice_native_last_error",
        "spice_native_free_string"
    )
    foreach ($symbol in $requiredExports) {
        if (-not ($exports -match $symbol)) {
            Fail "Required C ABI export is missing: $symbol"
        }
    }
    Write-Host "    C ABI exports      OK" -ForegroundColor Green
} else {
    Write-Host "    dumpbin.exe not found; skipped export inspection." -ForegroundColor DarkYellow
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Done: Windows Rust library built and deployed successfully." -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "    Target:  $windowsTarget" -ForegroundColor DarkGray
Write-Host "    Output:  $desktopLibrary" -ForegroundColor DarkGray
Write-Host ""
