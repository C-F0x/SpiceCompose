package org.cf0x.spicecompose.ui.screen.controllers.diy

/**
 * Hardcoded button/analog definitions for every game supported by Spice2x.
 *
 * Data sourced from spice2x `games/<game>/io.cpp` and `games/<game>/io.h`.
 * Replaces runtime [buttonsRead]/[analogsRead] calls — no network needed
 * for the bind dialog, and the list is always complete (not dependent on
 * whether SpiceTools is currently running).
 *
 * CODE → GameBindings lookup is O(1) via [all].
 */

// ── Data types ──────────────────────────────────────────────────────────

data class GameBindings(
    val label: String,           // e.g. "Beatmania IIDX"
    val buttons: List<String>,   // names for buttonsWrite API
    val analogs: List<String>,   // names for analogsWrite API
)

// ── Master registry ─────────────────────────────────────────────────────

val allBindings: Map<String, GameBindings> = mapOf(

    // ── Beatmania IIDX ──────────────────────────────────────────────────
    "iidx" to GameBindings(
        label = "Beatmania IIDX",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "P1 1", "P1 2", "P1 3", "P1 4", "P1 5", "P1 6", "P1 7",
            "P1 TT+", "P1 TT-", "P1 TT+/-", "P1 TT+/- Alternate",
            "P1 Start",
            "P2 1", "P2 2", "P2 3", "P2 4", "P2 5", "P2 6", "P2 7",
            "P2 TT+", "P2 TT-", "P2 TT+/-", "P2 TT+/- Alternate",
            "P2 Start",
            "EFFECT", "VEFX",
            "P1 Headphone", "P2 Headphone",
        ),
        analogs = listOf(
            "Turntable P1", "Turntable P2",
            "VEFX", "Low-EQ", "Hi-EQ", "Filter", "Play Volume",
        ),
    ),

    // ── Sound Voltex ────────────────────────────────────────────────────
    "sdvx" to GameBindings(
        label = "Sound Voltex",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "BT-A", "BT-B", "BT-C", "BT-D",
            "FX-L", "FX-R",
            "Start",
            "VOL-L Left", "VOL-L Right", "VOL-R Left", "VOL-R Right",
            "Headphone",
        ),
        analogs = listOf("VOL-L", "VOL-R"),
    ),

    // ── Dance Dance Revolution ──────────────────────────────────────────
    "ddr" to GameBindings(
        label = "Dance Dance Revolution",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "P1 Start", "P1 Panel Up", "P1 Panel Down", "P1 Panel Left", "P1 Panel Right",
            "P1 Menu Up", "P1 Menu Down", "P1 Menu Left", "P1 Menu Right",
            "P2 Start", "P2 Panel Up", "P2 Panel Down", "P2 Panel Left", "P2 Panel Right",
            "P2 Menu Up", "P2 Menu Down", "P2 Menu Left", "P2 Menu Right",
        ),
        analogs = listOf(
            "P1 Left-Right (Axis Fix)", "P1 Up-Down (Axis Fix)",
            "P2 Left-Right (Axis Fix)", "P2 Up-Down (Axis Fix)",
        ),
    ),

    // ── Pop'n Music ─────────────────────────────────────────────────────
    "popn" to GameBindings(
        label = "Pop'n Music",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "Headphones",
            "Button 1", "Button 2", "Button 3", "Button 4", "Button 5",
            "Button 6", "Button 7", "Button 8", "Button 9",
            "Red Pop-Kun", "Blue Pop-Kun",
        ),
        analogs = emptyList(),
    ),

    // ── Jubeat ──────────────────────────────────────────────────────────
    "jb" to GameBindings(
        label = "Jubeat",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "Button 1", "Button 2", "Button 3", "Button 4",
            "Button 5", "Button 6", "Button 7", "Button 8",
            "Button 9", "Button 10", "Button 11", "Button 12",
            "Button 13", "Button 14", "Button 15", "Button 16",
        ),
        analogs = emptyList(),
    ),

    // ── Nostalgia ───────────────────────────────────────────────────────
    "nost" to GameBindings(
        label = "Nostalgia",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            // Key 1-28 (velocity-sensitive keys)
            "Key 1", "Key 2", "Key 3", "Key 4", "Key 5", "Key 6", "Key 7",
            "Key 8", "Key 9", "Key 10", "Key 11", "Key 12", "Key 13", "Key 14",
            "Key 15", "Key 16", "Key 17", "Key 18", "Key 19", "Key 20",
            "Key 21", "Key 22", "Key 23", "Key 24", "Key 25", "Key 26", "Key 27", "Key 28",
            // Soft
            "Key 1 Soft", "Key 2 Soft", "Key 3 Soft", "Key 4 Soft",
            "Key 5 Soft", "Key 6 Soft", "Key 7 Soft",
            "Key 8 Soft", "Key 9 Soft", "Key 10 Soft", "Key 11 Soft",
            "Key 12 Soft", "Key 13 Soft", "Key 14 Soft",
            "Key 15 Soft", "Key 16 Soft", "Key 17 Soft", "Key 18 Soft",
            "Key 19 Soft", "Key 20 Soft", "Key 21 Soft",
            "Key 22 Soft", "Key 23 Soft", "Key 24 Soft", "Key 25 Soft",
            "Key 26 Soft", "Key 27 Soft", "Key 28 Soft",
            // Medium
            "Key 1 Medium", "Key 2 Medium", "Key 3 Medium", "Key 4 Medium",
            "Key 5 Medium", "Key 6 Medium", "Key 7 Medium",
            "Key 8 Medium", "Key 9 Medium", "Key 10 Medium", "Key 11 Medium",
            "Key 12 Medium", "Key 13 Medium", "Key 14 Medium",
            "Key 15 Medium", "Key 16 Medium", "Key 17 Medium", "Key 18 Medium",
            "Key 19 Medium", "Key 20 Medium", "Key 21 Medium",
            "Key 22 Medium", "Key 23 Medium", "Key 24 Medium", "Key 25 Medium",
            "Key 26 Medium", "Key 27 Medium", "Key 28 Medium",
            // Hard
            "Key 1 Hard", "Key 2 Hard", "Key 3 Hard", "Key 4 Hard",
            "Key 5 Hard", "Key 6 Hard", "Key 7 Hard",
            "Key 8 Hard", "Key 9 Hard", "Key 10 Hard", "Key 11 Hard",
            "Key 12 Hard", "Key 13 Hard", "Key 14 Hard",
            "Key 15 Hard", "Key 16 Hard", "Key 17 Hard", "Key 18 Hard",
            "Key 19 Hard", "Key 20 Hard", "Key 21 Hard",
            "Key 22 Hard", "Key 23 Hard", "Key 24 Hard", "Key 25 Hard",
            "Key 26 Hard", "Key 27 Hard", "Key 28 Hard",
            // Touch / Swipe
            "Swipe Next Page", "Swipe Previous Page",
            "Touch Confirm", "Touch Back",
            "Touch Song 1", "Touch Song 2", "Touch Song 3",
            "Touch Song 4", "Touch Song 5", "Touch Song 6",
            "Touch Difficulty 1", "Touch Difficulty 2",
            "Touch Difficulty 3", "Touch Difficulty 4",
        ),
        analogs = (1..28).map { "Key $it" },
    ),

    // ── DANCERUSH STARDOM ───────────────────────────────────────────────
    "drs" to GameBindings(
        label = "DANCERUSH STARDOM",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "P1 Start", "P1 Up", "P1 Down", "P1 Left", "P1 Right",
            "P2 Start", "P2 Up", "P2 Down", "P2 Left", "P2 Right",
        ),
        analogs = emptyList(),
    ),

    // ── Bishi Bashi Channel ─────────────────────────────────────────────
    "bbc" to GameBindings(
        label = "Bishi Bashi Channel",
        buttons = listOf(
            "Service", "Test",
            "P1 R", "P1 G", "P1 B", "P1 Disk-", "P1 Disk+", "P1 Disk -/+ Slowdown",
            "P2 R", "P2 G", "P2 B", "P2 Disk-", "P2 Disk+", "P2 Disk -/+ Slowdown",
            "P3 R", "P3 G", "P3 B", "P3 Disk-", "P3 Disk+", "P3 Disk -/+ Slowdown",
            "P4 R", "P4 G", "P4 B", "P4 Disk-", "P4 Disk+", "P4 Disk -/+ Slowdown",
        ),
        analogs = listOf("P1 Disk", "P2 Disk", "P3 Disk", "P4 Disk"),
    ),

    // ── HELLO! Pop'n Music ──────────────────────────────────────────────
    "hpm" to GameBindings(
        label = "HELLO! Pop'n Music",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "P1 Start", "P1 1", "P1 2", "P1 3", "P1 4",
            "P2 Start", "P2 1", "P2 2", "P2 3", "P2 4",
        ),
        analogs = emptyList(),
    ),

    // ── Road Fighters 3D ────────────────────────────────────────────────
    "rf3d" to GameBindings(
        label = "Road Fighters 3D",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "View", "2D/3D",
            "Lever Up", "Lever Down", "Lever Left", "Lever Right",
            "Wheel Left", "Wheel Right", "Accelerate", "Brake",
            "Auto Lever Down", "Auto Lever Up",
        ),
        analogs = listOf("Wheel", "Accelerate", "Brake"),
    ),

    // ── FutureTomTom ────────────────────────────────────────────────────
    "ftt" to GameBindings(
        label = "FutureTomTom",
        buttons = listOf("Service", "Test", "Pad 1", "Pad 2", "Pad 3", "Pad 4"),
        analogs = listOf("Pad 1", "Pad 2", "Pad 3", "Pad 4"),
    ),

    // ── LovePlus ────────────────────────────────────────────────────────
    "lp" to GameBindings(
        label = "LovePlus",
        buttons = listOf("Service", "Test", "Left", "Right"),
        analogs = emptyList(),
    ),

    // ── Winning Eleven ──────────────────────────────────────────────────
    "we" to GameBindings(
        label = "Winning Eleven",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "Start",
            "Up", "Down", "Left", "Right",
            "Button A", "Button B", "Button C", "Button D", "Button E", "Button F",
            "Pad Start", "Pad Select",
            "Pad Up", "Pad Down", "Pad Left", "Pad Right",
            "Pad Triangle", "Pad Cross", "Pad Square", "Pad Circle",
            "Pad L1", "Pad L2", "Pad L3", "Pad R1", "Pad R2", "Pad R3",
        ),
        analogs = listOf(
            "Pad Stick Left X", "Pad Stick Left Y",
            "Pad Stick Right X", "Pad Stick Right Y",
        ),
    ),

    // ── Polaris Chord ───────────────────────────────────────────────────
    "xif" to GameBindings(
        label = "Polaris Chord",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "Button 1", "Button 2", "Button 3", "Button 4",
            "Button 5", "Button 6", "Button 7", "Button 8",
            "Button 9", "Button 10", "Button 11", "Button 12",
            "Fader-L Left", "Fader-L Right", "Fader-R Left", "Fader-R Right",
            "Headphone",
        ),
        analogs = listOf("Fader-L", "Fader-R"),
    ),

    // ── GitaDora ────────────────────────────────────────────────────────
    "gitadora" to GameBindings(
        label = "GitaDora",
        buttons = listOf(
            "Service", "Test", "Coin", "Headphone",
            // Guitar P1
            "Guitar P1 Start", "Guitar P1 Up", "Guitar P1 Down",
            "Guitar P1 Left", "Guitar P1 Right", "Guitar P1 Help",
            "Guitar P1 Effect 1", "Guitar P1 Effect 2", "Guitar P1 Effect 3",
            "Guitar P1 Effect Pedal",
            "Guitar P1 Button Extra 1", "Guitar P1 Button Extra 2",
            "Guitar P1 Pick Up", "Guitar P1 Pick Down",
            "Guitar P1 R", "Guitar P1 G", "Guitar P1 B", "Guitar P1 Y", "Guitar P1 P",
            "Guitar P1 Knob Up", "Guitar P1 Knob Down",
            "Guitar P1 Wail Up", "Guitar P1 Wail Down",
            // Guitar P2
            "Guitar P2 Start", "Guitar P2 Up", "Guitar P2 Down",
            "Guitar P2 Left", "Guitar P2 Right", "Guitar P2 Help",
            "Guitar P2 Effect 1", "Guitar P2 Effect 2", "Guitar P2 Effect 3",
            "Guitar P2 Effect Pedal",
            "Guitar P2 Button Extra 1", "Guitar P2 Button Extra 2",
            "Guitar P2 Pick Up", "Guitar P2 Pick Down",
            "Guitar P2 R", "Guitar P2 G", "Guitar P2 B", "Guitar P2 Y", "Guitar P2 P",
            "Guitar P2 Knob Up", "Guitar P2 Knob Down",
            "Guitar P2 Wail Up", "Guitar P2 Wail Down",
            // Drum
            "Drum Start", "Drum Up", "Drum Down", "Drum Left", "Drum Right",
            "Drum Help", "Drum Button Extra 1", "Drum Button Extra 2",
            "Left Cymbal", "Hi-Hat", "Left Pedal", "Snare", "Hi-Tom",
            "Bass Pedal", "Low-Tom", "Floor Tom", "Right Cymbal",
            "Hi-Hat Closed", "Hi-Hat Half-Open",
        ),
        analogs = listOf(
            "Guitar P1 Wail X", "Guitar P1 Wail Y", "Guitar P1 Wail Z",
            "Guitar P1 Knob",
            "Guitar P2 Wail X", "Guitar P2 Wail Y", "Guitar P2 Wail Z",
            "Guitar P2 Knob",
        ),
    ),

    // ── Museca ──────────────────────────────────────────────────────────
    "museca" to GameBindings(
        label = "Museca",
        buttons = listOf(
            "Service", "Test", "Start",
            "Disk1-", "Disk1+", "Disk1 Press",
            "Disk2-", "Disk2+", "Disk2 Press",
            "Disk3-", "Disk3+", "Disk3 Press",
            "Disk4-", "Disk4+", "Disk4 Press",
            "Disk5-", "Disk5+", "Disk5 Press",
            "Foot Pedal", "Analog Slowdown",
        ),
        analogs = listOf("Disk1", "Disk2", "Disk3", "Disk4", "Disk5"),
    ),

    // ── Metal Gear ──────────────────────────────────────────────────────
    "mga" to GameBindings(
        label = "Metal Gear",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "Start",
            "Top", "Front Top", "Front Bottom",
            "Side Left", "Side Right", "Side Lever",
            "Trigger Button", "Switch Button",
            "Joy Forwards", "Joy Backwards", "Joy Left", "Joy Right",
        ),
        analogs = listOf("Joy X", "Joy Y"),
    ),

    // ── Steel Chronicle ─────────────────────────────────────────────────
    "sc" to GameBindings(
        label = "Steel Chronicle",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "L1 Button", "L2 Button", "L Stick Button",
            "R1 Button", "R2 Button", "R Stick Button",
            "Jog Switch Left", "Jog Switch Right",
        ),
        analogs = listOf("Left Stick X", "Left Stick Y", "Right Stick X", "Right Stick Y"),
    ),

    // ── Quiz Magic Academy ──────────────────────────────────────────────
    "qma" to GameBindings(
        label = "Quiz Magic Academy",
        buttons = listOf(
            "Service", "Test", "Select", "Coin Mech",
            "Select 1", "Select 2", "Left", "Right", "OK",
            // Touch keyboard
            "Touch Keyboard - 1", "Touch Keyboard - 2", "Touch Keyboard - 3",
            "Touch Keyboard - 4", "Touch Keyboard - 5", "Touch Keyboard - 6",
            "Touch Keyboard - 7", "Touch Keyboard - 8", "Touch Keyboard - 9", "Touch Keyboard - 0",
            "Touch Keyboard - -",
            "Touch Keyboard - Q", "Touch Keyboard - W", "Touch Keyboard - E",
            "Touch Keyboard - R", "Touch Keyboard - T", "Touch Keyboard - Y",
            "Touch Keyboard - U", "Touch Keyboard - I", "Touch Keyboard - O", "Touch Keyboard - P",
            "Touch Keyboard - A", "Touch Keyboard - S", "Touch Keyboard - D",
            "Touch Keyboard - F", "Touch Keyboard - G",
            "Touch Keyboard - H", "Touch Keyboard - J", "Touch Keyboard - K", "Touch Keyboard - L",
            "Touch Keyboard - Z", "Touch Keyboard - X", "Touch Keyboard - C",
            "Touch Keyboard - V", "Touch Keyboard - B", "Touch Keyboard - N", "Touch Keyboard - M",
            "Touch Keyboard - Backspace", "Touch Keyboard - Enter",
        ),
        analogs = emptyList(),
    ),

    // ── Reflec Beat ─────────────────────────────────────────────────────
    "rb" to GameBindings(
        label = "Reflec Beat",
        buttons = listOf("Service", "Test"),
        analogs = emptyList(),
    ),

    // ── Chase Chase Jokers ──────────────────────────────────────────────
    "ccj" to GameBindings(
        label = "Chase Chase Jokers",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "Joystick Up", "Joystick Down", "Joystick Left", "Joystick Right",
            "Dash", "Action", "Jump", "Slide", "Special", "Headphones",
            "Trackball Up", "Trackball Down", "Trackball Left", "Trackball Right",
        ),
        analogs = listOf("Joystick X", "Joystick Y", "Trackball DX", "Trackball DY"),
    ),

    // ── QuizKnock STADIUM ───────────────────────────────────────────────
    "qks" to GameBindings(
        label = "QuizKnock STADIUM",
        buttons = listOf(
            "Test", "Service", "Coin",
            "Q Button Press",
            "Q Button Sensor 1", "Q Button Sensor 2", "Q Button Sensor 3",
            "Headphones Detect", "Microphone Detect",
        ),
        analogs = emptyList(),
    ),

    // ── Busou Shinki ────────────────────────────────────────────────────
    "bc" to GameBindings(
        label = "Busou Shinki",
        buttons = listOf(
            "Service", "Test",
            "Up", "Down", "Left", "Right",
            "Joystick Button",
            "Trigger 1", "Trigger 2",
            "Button 1", "Button 2", "Button 3", "Button 4",
        ),
        analogs = listOf("Stick X", "Stick Y"),
    ),

    // ── Dance Evolution ─────────────────────────────────────────────────
    "dea" to GameBindings(
        label = "Dance Evolution",
        buttons = listOf(
            "Service", "Test",
            "P1 Start", "P1 Left", "P1 Right",
            "P2 Start", "P2 Left", "P2 Right",
        ),
        analogs = emptyList(),
    ),

    // ── Beatstream ──────────────────────────────────────────────────────
    "bs" to GameBindings(
        label = "Beatstream",
        buttons = listOf("Service", "Test", "Coin Mech"),
        analogs = emptyList(),
    ),

    // ── Silent Scope: Bone Eater ────────────────────────────────────────
    "silentscope" to GameBindings(
        label = "Silent Scope: Bone Eater",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "Start",
            "Up", "Down", "Left", "Right",
            "Gun Pressed", "Scope Right", "Scope Left",
        ),
        analogs = listOf("Gun X", "Gun Y"),
    ),

    // ── Scotto ──────────────────────────────────────────────────────────
    "scotto" to GameBindings(
        label = "Scotto",
        buttons = listOf(
            "Service", "Test", "Coin Mech", "Start", "Up", "Down",
            "Cup 1", "Cup 2", "First Pad",
            "Pad A (Left Bottom)", "Pad B (Left Middle)", "Pad C (Left Top)",
            "Pad D (Right Top)", "Pad E (Right Middle)", "Pad F (Right Bottom)",
        ),
        analogs = emptyList(),
    ),

    // ── Otoca D'or ──────────────────────────────────────────────────────
    "otoca" to GameBindings(
        label = "Otoca D'or",
        buttons = listOf(
            "Service", "Test", "Coin Mech",
            "Button Left", "Button Right",
            "Lever Up", "Lever Down", "Lever Left", "Lever Right",
        ),
        analogs = emptyList(),
    ),

    // ── Mahjong Fight Club ──────────────────────────────────────────────
    "mfc" to GameBindings(
        label = "Mahjong Fight Club",
        buttons = listOf(
            "Select", "Service", "Test", "Coin",
            "Joystick Up", "Joystick Down", "Joystick Enter",
        ),
        analogs = emptyList(),
    ),

    // ── Mahjong Fight Girl ──────────────────────────────────────────────
    "mfg" to GameBindings(
        label = "Mahjong Fight Girl",
        buttons = listOf("Service", "Test", "Coin Mech"),
        analogs = emptyList(),
    ),

    // ── Tenkaichi Shogikai ──────────────────────────────────────────────
    "shogikai" to GameBindings(
        label = "Tenkaichi Shogikai",
        buttons = listOf("Service", "Test", "Coin Mech", "Select"),
        analogs = emptyList(),
    ),

    // ── Ongaku Paradise ─────────────────────────────────────────────────
    "onpara" to GameBindings(
        label = "Ongaku Paradise",
        buttons = listOf("Service", "Test", "Start", "Headphone"),
        analogs = emptyList(),
    ),

    // ── Charge Machine ──────────────────────────────────────────────────
    "pcm" to GameBindings(
        label = "Charge Machine",
        buttons = listOf(
            "Service", "Test",
            "Insert 1000 Yen Bill", "Insert 2000 Yen Bill",
            "Insert 5000 Yen Bill", "Insert 10000 Yen Bill",
        ),
        analogs = emptyList(),
    ),
)

/**
 * Returns the bindings for [gameModel] or `null` if unknown.
 *
 * [gameModel] is the DiyLayout.gameModel short code (e.g. "iidx", "sdvx").
 * Pass an empty string for "Generic" = first result.
 */
fun bindingsFor(gameModel: String): GameBindings? = allBindings[gameModel]

/** All known game codes (for populating pickers). */
val allGameCodes: List<String> = allBindings.keys.toList()
