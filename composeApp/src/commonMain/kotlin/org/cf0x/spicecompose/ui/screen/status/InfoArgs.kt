package org.cf0x.spicecompose.ui.screen.status

/**
 * Human-readable descriptions for SPICE launcher arguments.
 * Source: spicecompanion-master/lib/views/resources.dart
 */
val infoArgsLookup: Map<String, String> = mapOf(
    "-cfg" to "Open configuration window",
    "-ea" to "Enable integrated EA server",
    "-eamaint" to "Enable integrated EA server",
    "-w" to "Windowed mode",
    "-c" to "Capture/clip mouse to window",
    "-s" to "Unhide cursor",
    "-k" to "Inject custom hook",
    "-stubs" to "Enable loading stubs",
    "-io" to "Manually enable ALL IO emulation",
    "-acio" to "Manually enable ACIO emulation",
    "-icca" to "Manually enable ICCA emulation",
    "-device" to "Manually enable DEVICE emulation",
    "-extdev" to "Manually enable EXTDEV emulation",
    "-sciunit" to "Manually enable SCIUNIT emu",
    "-sdvx" to "Manually enable SDVX module",
    "-iidx" to "Manually enable IIDX module",
    "-iidxflipcams" to "Flip the camera order",
    "-jb" to "Manually enable JB module",
    "-rb" to "Manually enable Reflec Beat module",
    "-pnm" to "Manually enable Pop'n Music module",
    "-mga" to "Manually enable Metal Gear module",
    "-gd" to "Manually enable GitaDora module",
    "-nostalgia" to "Manually enable Nostalgia module",
    "-bbc" to "Manually enable BishiBashi Channel module",
    "-2ch" to "Two channel audio for GitaDora",
    "-ddr" to "Manually enable DDR module",
    "-ddrsd" to "Enable DDR 4:3 mode",
    "-o" to "Enable DDR 4:3 mode",
    "-qma" to "Enable QMA module",
    "-sc" to "Enable SC module",
    "-network" to "Netfix network",
    "-subnet" to "Netfix subnet",
    "-netfixdisable" to "Disable network patches",
    "-acphookdisable" to "Disable ACP patches",
    "-signaldisable" to "Disable signal handling",
    "-createfiledebug" to "CreateFile debug prints",
    "-pebprint" to "Print PEB on startup",
    "-bt5api" to "Partial BT5 API compat layer",
    "-printer" to "SDVX Printer Emulation",
    "-printerpath" to "SDVX Printer Output Directory",
    "-printerformat" to "SDVX Printer Format (png/bmp/tga/jpg)",
    "-printerjpgquality" to "SDVX Printer JPG quality",
    "-printerclear" to "Clean up images on start",
    "-urlslash" to "Set urlslash value",
    "-disablenumpad" to "Disable numpad for reader",
    "-disabletoprow" to "Disable toprow for reader",
    "-realtime" to "Base process priority is RT",
    "-sleep" to "Delay (5s before boot)",
    "-h" to "Custom heap size in bytes",
    "-a" to "Custom path to app-config.xml",
    "-v" to "Custom path to avs-config.xml",
    "-e" to "Custom path to ea3-config.xml",
    "-y" to "Custom path to log.txt",
    "-p" to "Custom PCBID override",
    "-r" to "Custom SOFTID override",
    "-url" to "Custom service URL",
    "-modules" to "Set path to modules",
    "-scard" to "Use HID SmartCard readers",
    "-scardflip" to "Smartcard P1/P2 order flip",
    "-scardtoggle" to "Smartcard P1/P2 NumLock toggle",
    "-reader" to "COM ICCA card reader",
    "-togglereader" to "With NumLock toggle",
    "-cardio" to "Enable HID card readers",
    "-cardioflip" to "Cardio P1/P2 order flip",
    "-sde" to "Intel SDE automatic attach",
    "-api" to "Enable API",
    "-apipass" to "Set API password",
    "-apipretty" to "Pretty JSON printing",
    "-apilogging" to "Enable API logs (dec. perf.)",
    "-apidebug" to "Run API only on debug settings",
    "-dbghookdisable" to "Disable debug message logging",
)

/**
 * Format launcher arg string with human-readable descriptions.
 * Input:  "[-w, -sdvx, -iidx]"
 * Output: "-w (Windowed mode), -sdvx (Manually enable SDVX module)..."
 */
fun formatArgs(raw: String?): String {
    if (raw.isNullOrBlank()) return "..."
    val cleaned = raw.removeSurrounding("[", "]").trim()
    if (cleaned.isEmpty()) return "..."
    return cleaned.split(",").joinToString(", ") { arg ->
        val trimmed = arg.trim()
        val desc = infoArgsLookup[trimmed]
        if (desc != null) "$trimmed ($desc)" else trimmed
    }
}
