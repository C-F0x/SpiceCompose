package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.theme.ThemePreferences
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

data class ControllerFaqEntry(
    val code: String,
    val firmware: String,
    val name: String,
    val hasController: Boolean,
)

val controllerFaq = listOf(
    // ── Implemented controllers ──────────────────────────────────────────
    ControllerFaqEntry("BBC",  "R66",                     "BishiBashi Channel",                               true),
    ControllerFaqEntry("DDR",  "JDX / KDX / MDX",         "DanceDanceRevolution",                             true),
    ControllerFaqEntry("DRS",  "REC",                     "DANCERUSH STARDOM",                                true),
    ControllerFaqEntry("FTT",  "MMD",                     "Future TomTom",                                    true),
    ControllerFaqEntry("HPM",  "JMP",                     "HELLO! POP\u2019N MUSIC",                          true),
    ControllerFaqEntry("IIDX", "JDZ / KDX / LDJ",         "beatmania IIDX",                                   true),
    ControllerFaqEntry("JB",   "J44 / K44 / L44",         "jubeat",                                           true),
    ControllerFaqEntry("LP",   "KLP",                     "LOVEPLUS / \u30E9\u30D6\u30D7\u30E9\u30B9 EVERY",  true),
    ControllerFaqEntry("NOST", "PAN",                     "NOSTALGIA",                                        true),
    ControllerFaqEntry("POPN", "K39 / L39 / M39",         "pop\u2019n music",                                 true),
    ControllerFaqEntry("RF3D", "JGT",                     "ROAD FIGHTERS",                                    true),
    ControllerFaqEntry("SDVX", "KFC",                     "SOUND VOLTEX",                                     true),
    ControllerFaqEntry("WE",   "KCK / NCK",               "World Soccer Winning Eleven Arcade Game Styles",   true),

    // ── Not yet implemented ──────────────────────────────────────────────
    ControllerFaqEntry("BTS",  "NBT",                     "BeatStream",                                       false),
    ControllerFaqEntry("CCJ",  "UJK",                     "CHASE CHASE JOKERS",                               false),
    ControllerFaqEntry("DEA",  "KDM",                     "DanceEvolution ARCADE",                            false),
    ControllerFaqEntry("GD",   "J32..M32 (6 models)",     "GITADORA",                                         false),
    ControllerFaqEntry("MFC",  "KK9",                     "MAHJONG FIGHT CLUB",                               false),
    ControllerFaqEntry("MFG",  "VFG",                     "MAHJONG FIGHT GIRL",                               false),
    ControllerFaqEntry("MGS",  "I36",                     "METAL GEAR SOLID THE ARCADE",                      false),
    ControllerFaqEntry("MSC",  "PIX",                     "M\u00DASECA",                                      false),
    ControllerFaqEntry("OD",   "NCG",                     "Oto&co D\u2019or / \u30AA\u30C8\u30AB\u30C9\u30FC\u30EB", false),
    ControllerFaqEntry("OGP",  "JC9",                     "ONGAKU PARADISE",                                  false),
    ControllerFaqEntry("PAS",  "LA9",                     "PASELI Charging Machine",                          false),
    ControllerFaqEntry("PLC",  "XIF",                     "Polaris Chord",                                    true),
    ControllerFaqEntry("QKS",  "UKS",                     "QuizKnock STADIUM",                                false),
    ControllerFaqEntry("QMA",  "JMA / KMA / LMA",         "QUIZ MAGIC ACADEMY",                               false),
    ControllerFaqEntry("REF",  "KBR / LBR / MBR",         "REFLEC BEAT",                                      false),
    ControllerFaqEntry("SC",   "KGG",                     "STEEL CHRONICLE",                                  false),
    ControllerFaqEntry("SCO",  "NSC",                     "SCOTTO",                                           false),
    ControllerFaqEntry("SPC",  "N/A",                     "SILENT SCOPE CHRONOS GEIST",                       false),
    ControllerFaqEntry("TCS",  "KBI",                     "\u5929\u4E0B\u4E00\u5C06\u68CB\u4F1A / TENKAICHI SHOGIKAI", false),
    ControllerFaqEntry("WBS",  "TBS",                     "\u6B66\u88C5\u795E\u59EB ARMORED PRINCESS BATTLE CONDUCTOR", false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerFaqScreen(onBack: () -> Unit) {
    val fullscreen = LocalFullscreenMode.current
    val uiMode = LocalUiMode.current
    val p = ThemePreferences

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (uiMode == UiMode.Miuix) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = "Controller FAQ",
                        navigationIcon = {
                            top.yukonga.miuix.kmp.basic.IconButton(onClick = onBack) {
                                top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Back, null)
                            }
                        },
                        actions = { FullscreenAction() },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            FaqContent(pad)
        }
    } else {
        Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    TopAppBar(
                        title = { Text("Controller FAQ") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = { FullscreenAction() },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            FaqContent(pad)
        }
    }
}

@Composable
private fun FaqContent(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // ── Implemented ──────────────────────────────────────────────────
        item {
            ListItem(
                headlineContent = {
                    Text("Implemented", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                },
            )
        }
        controllerFaq.filter { it.hasController }.forEach { entry ->
            item {
                ListItem(
                    headlineContent = { Text(entry.name) },
                    supportingContent = { Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                    leadingContent = { Icon(Icons.Rounded.Gamepad, null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }

        // ── Divider ──────────────────────────────────────────────────────
        item { Spacer(Modifier.height(8.dp)) }
        item { HorizontalDivider() }
        item { Spacer(Modifier.height(8.dp)) }

        // ── Not yet implemented ──────────────────────────────────────────
        item {
            ListItem(
                headlineContent = {
                    Text("Not yet implemented", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
            )
        }
        controllerFaq.filter { !it.hasController }.forEach { entry ->
            item {
                ListItem(
                    headlineContent = { Text(entry.name) },
                    supportingContent = { Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                    leadingContent = {
                        Icon(Icons.Rounded.SportsEsports, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
