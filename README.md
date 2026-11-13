# Nothing TV Home (Android / Google TV Launcher)

[![License: MIT](https://img.shields.io/badge/License-MIT-black.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Google%20TV-red.svg)](https://developer.android.com/tv)
[![APK Size](https://img.shields.io/badge/Size-~600%20KB-brightgreen.svg)]()

A fast, lightweight, ad-free Android TV / Google TV home launcher styled after **Nothing OS**. Replaces slow, resource-heavy default launchers (like Google TV LauncherX) with an AMOLED-pure black dock, iconic red accent focus rings, physical remote hardware button mapping, and automated HDMI input boot.

Forked and customized from [mihirgupta0900/tv-home](https://github.com/mihirgupta0900/tv-home) for enhanced Sony Bravia remote support and input automation.

---

## Key Features

### 1. Nothing OS Aesthetic
- **Pure Black Backdrop (`#000000`)**: Designed for modern displays to minimize backlight bleed and optimize power.
- **Nothing Red Accent (`#D71921`)**: Clean dot-matrix focus outlines and highlights.
- **Minimal Dock**: Focus-driven app dock without sponsored banners, suggested video feeds, or intrusive ads.
- **Dot-Matrix Wallpaper**: Custom textured dark wallpaper bundled in assets.

### 2. Dedicated Hardware Remote Key Mapping (Sony Bravia `VRC 001`)
Stock Android TV launchers often misroute or intercept remote hotkeys. This launcher bundles a lightweight `AccessibilityService` (`RemoteKeyService`) and `GlobalButtonReceiver` that capture hardware key events and direct broadcasts:

| Remote Button | Android Keycode | Target Application |
| :--- | :--- | :--- |
| **YouTube** | `201` (`KEYCODE_BUTTON_14`), `172` (`KEYCODE_GUIDE`) | `com.google.android.youtube.tv` |
| **Netflix** | `190` (`KEYCODE_BUTTON_3`) | `com.netflix.ninja` |
| **Prime Video** | `188` (`KEYCODE_BUTTON_1`) | `com.amazon.amazonvideo.livingroom` |
| **Disney+ / Hotstar** | `196` (`KEYCODE_BUTTON_9`), `3005` | `in.startv.hotstar` |
| **Sony Pictures Core** | `245` (`KEYCODE_BUTTON_16`) | `com.sony.dtv.braviacore` |
| **YouTube Music** | `202` (`KEYCODE_BUTTON_15`) | `com.google.android.youtube.tvmusic` |
| **Settings (Gear)** | `197` (`KEYCODE_BUTTON_10`), `176`, `312`, `82` | Native Android TV Settings |

*Includes a 500ms software debounce filter to eliminate duplicate key triggers.*

### 3. Auto-Boot / Boot into Last HDMI Input
- Automatically switches to the last selected HDMI input (or default Set-Top Box / DTH input) upon TV wake or startup.
- Configurable directly from the launcher's inputs popup menu.
- Ideal for shared family setups where parents switch directly to cable or HDMI TV without navigating menus.

### 4. Extreme Performance & Low Memory Footprint
- **Size**: ~600 KB compiled APK (compared to 100+ MB OEM launchers).
- **RAM Footprint**: ~35–45 MB active memory (versus 200+ MB for Google TV LauncherX).
- **Instant Resume**: Sub-second wake times when using remote standby.

### 5. Windows PowerShell Build Pipeline (`build.ps1`)
- No Android Studio or Gradle installation required.
- Compiles Java source, packages with `aapt2`, optimizes Dalvik bytecode with `d8`, aligns with `zipalign`, and signs with `apksigner` directly from Android SDK command-line tools.

---

## Project Structure

```text
tv-home/
├── launcher/
│   ├── assets/wallpapers/           # Custom Nothing OS dot-matrix wallpapers
│   ├── res/
│   │   ├── values/config.xml        # Colors (Nothing Red #D71921, pure black #000)
│   │   └── xml/accessibility_service_config.xml # Remote key interception flags
│   ├── src/com/mihir/tvhome/
│   │   ├── MainActivity.java        # Home dock, input switching, auto-boot logic
│   │   ├── RemoteKeyService.java    # Hardware remote key listener & debouncer
│   │   └── GlobalButtonReceiver.java# Broadcast receiver for global button events
│   ├── AndroidManifest.xml          # Permissions & service declarations
│   ├── build.ps1                    # Windows automated build script
│   └── build-and-install.sh         # Linux/macOS build script
├── tools/                           # ADB debloat and management scripts
├── LICENSE
└── README.md
```

---

## Building & Installation

### Prerequisites
1. **Enable Developer Options on TV**:
   - Go to **Settings** → **System** → **About** → scroll to **Android TV OS build** and click **OK** 7 times.
   - Go back to **Developer options** and turn on **USB debugging**.
2. **Connect over ADB**:
   ```powershell
   adb connect <TV-IP-ADDRESS>:5555
   ```

### Option A: Build and Install with PowerShell (Windows)
From the repository root:
```powershell
cd launcher
.\build.ps1
adb install -r build\tvhome.apk
```

### Option B: Build and Install on macOS / Linux
```bash
cd launcher
./build-and-install.sh
```

---

## Post-Installation Setup

### 1. Grant Permissions
To allow the launcher to manage secure settings and enable accessibility services automatically:
```powershell
adb shell pm grant com.mihir.tvhome android.permission.WRITE_SECURE_SETTINGS
```

### 2. Enable Remote Key Accessibility Service
To activate physical remote hotkey interception (YouTube, Netflix, Prime, Settings):
```powershell
adb shell settings put secure enabled_accessibility_services com.mihir.tvhome/.RemoteKeyService
adb shell settings put secure accessibility_enabled 1
```

### 3. (Optional) Set as Default Launcher & Disable Bloat
To make Nothing TV Home the default launcher and prevent Google TV from hijacking the Home button:
```powershell
# Disable default ad-heavy launcher
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx

# Optional: Disable setupwraith to ensure clean Home button return
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
```

### 4. Optimize Execution Speed (AOT Compile)
```powershell
adb shell cmd package compile -m speed -f com.mihir.tvhome
```

---

## How to Restore / Revert

Everything modified is 100% reversible. If you ever want to return to the factory stock Google TV launcher:
```powershell
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
adb shell pm uninstall com.mihir.tvhome
```

---

## Credits
- Based on the original [tv-home](https://github.com/mihirgupta0900/tv-home) by [Mihir Gupta](https://github.com/mihirgupta0900).
- Nothing OS visual styling, Sony Bravia keycodes reverse-engineering, and auto-boot features maintained in this fork by [Arjid](https://github.com/arjid200).
