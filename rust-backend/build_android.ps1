<#
.SYNOPSIS
    Cross-compile Rust spice-backend for Android and deploy to jniLibs.
.DESCRIPTION
    Builds libspice_backend.so for all Android ABIs and places them
    into composeApp/src/androidMain/jniLibs/<abi>/.
.NOTES
    Detects NDK from %AndroidSdk%, %ANDROID_HOME%, %LOCALAPPDATA%, or %ANDROID_NDK_HOME%.
    Missing Rust cross-compilation targets are prompted for auto-install.
#>

$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "SpiceCompose: Build Android Rust Library"

# ═══════════════════════════════════════════════════════════════════
# Stage 0a: Check Rust toolchain — auto-install via winget if missing
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 0a: Checking Rust toolchain..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$rustup = Get-Command rustup -ErrorAction SilentlyContinue
if (-not $rustup) {
    Write-Host "    rustc      ✖  NOT FOUND" -ForegroundColor Red
    Write-Host "    cargo      ✖  NOT FOUND" -ForegroundColor Red
    Write-Host "    rustup     ✖  NOT FOUND" -ForegroundColor Red

    $winget = Get-Command winget -ErrorAction SilentlyContinue
    if ($winget) {
        Write-Host ""
        $answer = Read-Host "    Install Rust toolchain via winget? [Y/n]"
        if ($answer -eq '' -or $answer -match '^[Yy]') {
            Write-Host "    Installing Rustlang.Rustup via winget..." -ForegroundColor Yellow
            & winget install Rustlang.Rustup --silent --accept-package-agreements
            if ($LASTEXITCODE -ne 0) {
                Write-Host "    >>> ERROR: winget install failed." -ForegroundColor Red
                Write-Host "    Please install manually from: https://rustup.rs" -ForegroundColor Red
                exit 1
            }
            # Refresh PATH so rustup/cargo are available in this session
            $env:Path = "$env:USERPROFILE\.cargo\bin;$env:Path"
            Write-Host "    Installing nightly Rust toolchain (this may take a while)..." -ForegroundColor Yellow
            & rustup default nightly
            if ($LASTEXITCODE -ne 0) {
                Write-Host "    >>> ERROR: rustup default nightly failed." -ForegroundColor Red
                exit 1
            }
            Write-Host "    Rust toolchain installed. Re-running script..." -ForegroundColor Green
            & $PSCommandPath
            exit $LASTEXITCODE
        } else {
            Write-Host "    Aborting. Install manually from: https://rustup.rs" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "`n>>> ERROR: Rust toolchain is not installed and winget is not available." -ForegroundColor Red
        Write-Host "    Please install from: https://rustup.rs" -ForegroundColor Red
        Write-Host "    Then run this script again.`n" -ForegroundColor Red
        exit 1
    }
}

# rustup binary found — now check whether a toolchain is actually installed
$rustcOk = $false
$rustcVersion = ""
try {
    $rustcVersion = & rustc --version 2>$null
    if ($LASTEXITCODE -eq 0) { $rustcOk = $true }
} catch {}

if ($rustcOk) {
    Write-Host "    rustc      ✓  $rustcVersion" -ForegroundColor Green
    Write-Host "    cargo      ✓" -ForegroundColor Green
    Write-Host "    rustup     ✓" -ForegroundColor Green
} else {
    Write-Host "    rustc      ✖  no toolchain installed" -ForegroundColor Red
    Write-Host "    cargo      ✖  no toolchain installed" -ForegroundColor Red
    Write-Host "    rustup     ✓  (binary found)" -ForegroundColor Green
    Write-Host ""
    $answer = Read-Host "    Install nightly Rust toolchain? [Y/n]"
    if ($answer -eq '' -or $answer -match '^[Yy]') {
        Write-Host "    Installing nightly Rust toolchain (this may take a while)..." -ForegroundColor Yellow
        & rustup default nightly
        if ($LASTEXITCODE -ne 0) {
            Write-Host "    >>> ERROR: rustup default nightly failed." -ForegroundColor Red
            exit 1
        }
        Write-Host "    Toolchain installed. Re-running script..." -ForegroundColor Green
        & $PSCommandPath
        exit $LASTEXITCODE
    } else {
        Write-Host "    Aborting. Run 'rustup default nightly' manually." -ForegroundColor Red
        exit 1
    }
}

# ═══════════════════════════════════════════════════════════════════
# Stage 0b: Check Android cross-compilation targets
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 0b: Checking Android cross-compilation targets..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$androidTargets = @(
    "aarch64-linux-android",
    "armv7-linux-androideabi",
    "x86_64-linux-android",
    "i686-linux-android"
)

$installedTargets = @(& rustup target list --installed) -replace '\s.*$', ''

$targetsToInstall = @()
foreach ($t in $androidTargets) {
    if ($installedTargets -contains $t) {
        Write-Host "    $t  ✓" -ForegroundColor Green
    } else {
        Write-Host "    $t  ×  NOT INSTALLED" -ForegroundColor Red
        $targetsToInstall += $t
    }
}

if ($targetsToInstall.Count -gt 0) {
    Write-Host ""
    Write-Host "    Missing Android target(s): $($targetsToInstall -join ', ')" -ForegroundColor Yellow
    $answer = Read-Host "    Install now? [Y/n]"
    if ($answer -eq '' -or $answer -match '^[Yy]') {
        foreach ($t in $targetsToInstall) {
            Write-Host "    Installing $t ..." -ForegroundColor Yellow
            & rustup target add $t
            if ($LASTEXITCODE -ne 0) {
                Write-Host "    >>> ERROR: Failed to install target $t" -ForegroundColor Red
                exit 1
            }
        }
        Write-Host "    Targets installed. Re-running script..." -ForegroundColor Green
        & $PSCommandPath
        exit $LASTEXITCODE
    } else {
        Write-Host "    Aborting. Install manually with:" -ForegroundColor Red
        Write-Host "      rustup target add $($targetsToInstall -join ' ')" -ForegroundColor Red
        exit 1
    }
}

# ═══════════════════════════════════════════════════════════════════
# Stage 1: Locate Android NDK
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 1: Locating Android NDK..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# Search paths in priority order
$ndkSearchPaths = @()

# 1. %AndroidSdk% (user custom SDK path, e.g. D:\DevHub\Android\Sdk)
if ($env:AndroidSdk) {
    $ndkSearchPaths += "$env:AndroidSdk\ndk"
}

# 2. %ANDROID_NDK_HOME% (explicit NDK path)
if ($env:ANDROID_NDK_HOME) {
    $ndkSearchPaths += "$env:ANDROID_NDK_HOME"
    # ANDROID_NDK_HOME often points directly to NDK root, not the parent container dir.
    # We still add it to the search and will check it as a fallback below.
}

# 3. %ANDROID_HOME% (standard SDK variable)
if ($env:ANDROID_HOME) {
    $ndkSearchPaths += "$env:ANDROID_HOME\ndk"
}

# 4. Default Android Studio location
$ndkSearchPaths += "$env:LOCALAPPDATA\Android\Sdk\ndk"

# Deduplicate while preserving order
$seen = @{}
$ndkSearchPaths = $ndkSearchPaths | Where-Object { $_ -and -not $seen.ContainsKey($_) -and ($seen[$_] = $true) }

$ndkRoot = $null

# Try versioned subdirectories under each base
foreach ($base in $ndkSearchPaths) {
    if (Test-Path $base) {
        $latest = Get-ChildItem $base -Directory -ErrorAction SilentlyContinue `
            | Where-Object { $_.Name -match '^\d+\.\d+' } `
            | Sort-Object { [version]$_.Name } -Descending `
            | Select-Object -First 1
        if ($latest) {
            $ndkRoot = $latest.FullName
            Write-Host "    NDK found at: $ndkRoot" -ForegroundColor Green
            break
        }
    }
}

# Fallback: if %ANDROID_NDK_HOME% points directly to NDK root (not version subfolder)
if (-not $ndkRoot -and $env:ANDROID_NDK_HOME -and (Test-Path $env:ANDROID_NDK_HOME)) {
    $ndkRoot = $env:ANDROID_NDK_HOME
    Write-Host "    NDK found at (ANDROID_NDK_HOME): $ndkRoot" -ForegroundColor Green
}

if (-not $ndkRoot) {
    Write-Host "`n>>> ERROR: NDK not found." -ForegroundColor Red
    Write-Host "    Searched:" -ForegroundColor Red
    foreach ($p in $ndkSearchPaths) { Write-Host "      $p" -ForegroundColor DarkGray }
    Write-Host "    Install via Android Studio → SDK Manager → SDK Tools → NDK" -ForegroundColor Red
    exit 1
}

$ndkVersion = Split-Path $ndkRoot -Leaf
$toolchainBin = Join-Path $ndkRoot "toolchains\llvm\prebuilt\windows-x86_64\bin"

if (-not (Test-Path $toolchainBin)) {
    Write-Host ">>> ERROR: NDK toolchain not found at:" -ForegroundColor Red
    Write-Host "    $toolchainBin" -ForegroundColor Red
    Write-Host "    The NDK installation may be incomplete." -ForegroundColor Red
    exit 1
}

Write-Host "    NDK version: $ndkVersion" -ForegroundColor Green
Write-Host "    Toolchain:   $toolchainBin" -ForegroundColor Green

# ═══════════════════════════════════════════════════════════════════
# Stage 2: Setup cargo config for cross-compilation
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 2: Setting up cargo config..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$minSdk = 31
$configDir   = Join-Path $PSScriptRoot ".cargo"
$configFile  = Join-Path $configDir "config.toml"

$targets = @(
    @{ target = "aarch64-linux-android";   clang = "aarch64-linux-android"         },
    @{ target = "armv7-linux-androideabi"; clang = "armv7a-linux-androideabi"      },
    @{ target = "x86_64-linux-android";    clang = "x86_64-linux-android"          },
    @{ target = "i686-linux-android";      clang = "i686-linux-android"            }
)

$configContent = "# Auto-generated by build_android.ps1`n# NDK: $ndkVersion   minSdk: $minSdk`n`n"

foreach ($t in $targets) {
    $linker = "$toolchainBin\$($t.clang)$($minSdk)-clang.cmd" -replace '\\', '/'
    $configContent += @"

[target.$($t.target)]
linker = "$linker"
rustflags = [
    "-Clink-arg=-Wl,-z,max-page-size=16384",
    "-Clink-arg=-Wl,-z,common-page-size=16384",
    "-Clink-arg=-Wl,-z,relro",
    "-Clink-arg=-Wl,-z,now",
    "-Clink-arg=-Wl,--enable-new-dtags",
    "-Clink-arg=-Wl,-z,separate-loadable-segments",
]
"@
}

# Only rewrite if NDK path changed (avoids dirtying git and cargo clean)
$oldNdkVersion = ""
if (Test-Path $configFile) {
    $raw = Get-Content $configFile -Raw -ErrorAction SilentlyContinue
    if ($raw -match 'NDK:\s*([\d.]+)') { $oldNdkVersion = $Matches[1] }
}

if ($oldNdkVersion -ne $ndkVersion) {
    New-Item -ItemType Directory -Force -Path $configDir | Out-Null
    Set-Content -Path $configFile -Value $configContent
    Write-Host "    NDK $oldNdkVersion -> $ndkVersion, rewrote $configFile" -ForegroundColor Yellow
    $configChanged = $true
} else {
    Write-Host "    $configFile (NDK $ndkVersion, unchanged)" -ForegroundColor DarkGray
    $configChanged = $false
}

# ═══════════════════════════════════════════════════════════════════
# Stage 3: Build Rust library for each Android ABI
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 3: Building Rust library..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

Push-Location $PSScriptRoot

if ($configChanged) {
    Write-Host "    NDK path changed, cleaning stale build artifacts..." -ForegroundColor Yellow
    & cargo clean
}

$buildMap = @{
    "aarch64-linux-android"   = "arm64-v8a"
    "armv7-linux-androideabi" = "armeabi-v7a"
    "x86_64-linux-android"    = "x86_64"
    "i686-linux-android"      = "x86"
}

foreach ($target in $buildMap.Keys) {
    Write-Host "    Building $target ..." -ForegroundColor Yellow
    & cargo build --release --target $target
    if ($LASTEXITCODE -ne 0) {
        Write-Host "    >>> ERROR: Build failed for $target" -ForegroundColor Red
        Pop-Location
        exit $LASTEXITCODE
    }
    Write-Host "    $target OK" -ForegroundColor Green
}

Pop-Location

# ═══════════════════════════════════════════════════════════════════
# Stage 4: Copy .so files to jniLibs
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Stage 4: Copying shared libraries to jniLibs..." -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$jniLibsBase = Join-Path $PSScriptRoot "..\composeApp\src\androidMain\jniLibs"

foreach ($pair in $buildMap.GetEnumerator()) {
    $target = $pair.Key
    $abi    = $pair.Value

    $src  = Join-Path $PSScriptRoot "target\$target\release\libspice_backend.so"
    $dest = Join-Path $jniLibsBase $abi

    if (-not (Test-Path $src)) {
        Write-Host "    SKIP: $src not found" -ForegroundColor DarkYellow
        continue
    }

    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    Copy-Item -Path $src -Destination "$dest\libspice_backend.so" -Force

    $size = [math]::Round((Get-Item $src).Length / 1KB, 1)
    Write-Host "    libspice_backend.so → $abi\  ($size KB)" -ForegroundColor Green
}

# ═══════════════════════════════════════════════════════════════════
# Done
# ═══════════════════════════════════════════════════════════════════

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host ">>> Done: All targets built and deployed successfully." -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
