package org.cf0x.spicecompose.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cf0x.spicecompose.platform.LocalFullscreenMode
import org.cf0x.spicecompose.ui.LocalUiMode
import org.cf0x.spicecompose.ui.SpiceBackHandler
import org.cf0x.spicecompose.ui.UiMode
import org.cf0x.spicecompose.ui.i18n.LocalAppStrings
import org.cf0x.spicecompose.ui.component.AdaptiveTopAppBar
import org.cf0x.spicecompose.ui.component.FullscreenAction
import org.cf0x.spicecompose.ui.theme.CustomPreferences
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.Scaffold as M3Scaffold

enum class ControllerStatus { Implemented, Wip, NotYetImplemented }

data class ControllerFaqEntry(
    val code: String,
    val firmware: String,
    val name: String,
    val status: ControllerStatus,
    val maxPlayerNum: Int = 1,
)

val controllerFaq = listOf(
    ControllerFaqEntry("BBC",  "R66",                     "BishiBashi Channel",                               ControllerStatus.Wip,               maxPlayerNum = 4),
    ControllerFaqEntry("DDR",  "JDX / KDX / MDX / TDX",    "DanceDanceRevolution",                             ControllerStatus.Implemented,       maxPlayerNum = 2),
    ControllerFaqEntry("DRS",  "REC",                     "DANCERUSH STARDOM",                                ControllerStatus.Wip,               maxPlayerNum = 2),
    ControllerFaqEntry("FTT",  "MMD",                     "Future TomTom",                                    ControllerStatus.Wip),
    ControllerFaqEntry("HPM",  "JMP",                     "HELLO! POP\u2019N MUSIC",                          ControllerStatus.Wip,               maxPlayerNum = 2),
    ControllerFaqEntry("IIDX", "JDZ / KDZ / LDJ / TDJ",    "beatmania IIDX",                                   ControllerStatus.Implemented,               maxPlayerNum = 2),
    ControllerFaqEntry("JB",   "J44 / K44 / L44",         "jubeat",                                           ControllerStatus.Wip),
    ControllerFaqEntry("LP",   "KLP",                     "LOVEPLUS / \u30E9\u30D6\u30D7\u30E9\u30B9 EVERY",  ControllerStatus.Wip),
    ControllerFaqEntry("NOST", "PAN",                     "NOSTALGIA",                                        ControllerStatus.Wip),
    ControllerFaqEntry("POPN", "K39 / L39 / M39",         "pop\u2019n music",                                 ControllerStatus.Wip),
    ControllerFaqEntry("RF3D", "JGT",                     "ROAD FIGHTERS",                                    ControllerStatus.Wip),
    ControllerFaqEntry("SDVX", "KFC / UFC",               "SOUND VOLTEX",                                     ControllerStatus.Implemented),
    ControllerFaqEntry("WE",   "KCK / NCK",               "World Soccer Winning Eleven Arcade Game Styles",   ControllerStatus.Wip),
    ControllerFaqEntry("PLC",  "XIF",                     "Polaris Chord",                                    ControllerStatus.Wip),
    ControllerFaqEntry("BTS",  "NBT",                     "BeatStream",                                       ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("CCJ",  "UJK",                     "CHASE CHASE JOKERS",                               ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("DEA",  "KDM",                     "DanceEvolution ARCADE",                            ControllerStatus.NotYetImplemented, maxPlayerNum = 2),
    ControllerFaqEntry("GD",   "J32..M32 (6 models)",     "GITADORA",                                         ControllerStatus.NotYetImplemented, maxPlayerNum = 3),
    ControllerFaqEntry("MFC",  "KK9",                     "MAHJONG FIGHT CLUB",                               ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("MFG",  "VFG",                     "MAHJONG FIGHT GIRL",                               ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("MGS",  "I36",                     "METAL GEAR SOLID THE ARCADE",                      ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("MSC",  "PIX",                     "M\u00DASECA",                                      ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("OD",   "NCG",                     "Oto&co D\u2019or / \u30AA\u30C8\u30AB\u30C9\u30FC\u30EB", ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("OGP",  "JC9",                     "ONGAKU PARADISE",                                  ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("PAS",  "LA9",                     "PASELI Charging Machine",                          ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("QKS",  "UKS",                     "QuizKnock STADIUM",                                ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("QMA",  "JMA / KMA / LMA",         "QUIZ MAGIC ACADEMY",                               ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("REF",  "KBR / LBR / MBR",         "REFLEC BEAT",                                      ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("SC",   "KGG",                     "STEEL CHRONICLE",                                  ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("SCO",  "NSC",                     "SCOTTO",                                           ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("SPC",  "N/A",                     "SILENT SCOPE CHRONOS GEIST",                       ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("TCS",  "KBI",                     "\u5929\u4E0B\u4E00\u5C06\u68CB\u4F1A / TENKAICHI SHOGIKAI", ControllerStatus.NotYetImplemented),
    ControllerFaqEntry("WBS",  "TBS",                     "\u6B66\u88C5\u795E\u59EB ARMORED PRINCESS BATTLE CONDUCTOR", ControllerStatus.NotYetImplemented),
)

@Composable
fun ControllerFaqScreen(onBack: () -> Unit) {
    val fullscreen = LocalFullscreenMode.current
    val uiMode = LocalUiMode.current
    val p = CustomPreferences

    val strings = LocalAppStrings.current

    SpiceBackHandler(enabled = fullscreen.value) { fullscreen.value = false }

    if (uiMode == UiMode.Miuix) {
        val scrollBehavior = MiuixScrollBehavior()
        MiuixScaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    SmallTopAppBar(
                        title = strings.controllerFaq,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            MiuixIconButton(onClick = onBack) {
                                MiuixIcon(MiuixIcons.Back, null)
                            }
                        },
                        actions = { FullscreenAction() },
                    )
                }
            },
        ) { innerPadding ->
            val topPadding = if (fullscreen.value) 0.dp else innerPadding.calculateTopPadding()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = topPadding),
            ) {
                item {
                    MiuixText(
                        strings.implemented,
                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
                val implemented = controllerFaq.filter { it.status == ControllerStatus.Implemented }
                if (implemented.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            implemented.forEach { entry -> FaqRowMiuix(entry, ControllerStatus.Implemented) }
                        }
                    }
                } else {
                    item {
                        MiuixText(
                            strings.noneYet,
                            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
                item {
                    MiuixText(
                        strings.wip,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        controllerFaq.filter { it.status == ControllerStatus.Wip }.forEach { entry ->
                            FaqRowMiuix(entry, ControllerStatus.Wip)
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
                item {
                    MiuixText(
                        strings.notYetImplemented,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        controllerFaq.filter { it.status == ControllerStatus.NotYetImplemented }.forEach { entry ->
                            FaqRowMiuix(entry, ControllerStatus.NotYetImplemented)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        }
    } else {
        M3Scaffold(
            topBar = {
                if (!fullscreen.value && !p.toolbarHidden) {
                    AdaptiveTopAppBar(
                        title = { M3Text(strings.controllerFaq) },
                        navigationIcon = {
                            M3IconButton(onClick = onBack) {
                                M3Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backDesc)
                            }
                        },
                        actions = { FullscreenAction() },
                    )
                }
            },
        ) { innerPadding ->
            val pad = if (fullscreen.value) PaddingValues(0.dp) else innerPadding
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    ListItem(
                        headlineContent = {
                            M3Text(strings.implemented, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        },
                    )
                }
                val implemented = controllerFaq.filter { it.status == ControllerStatus.Implemented }
                if (implemented.isNotEmpty()) {
                    implemented.forEach { entry ->
                        item {
                            ListItem(
                                headlineContent = { M3Text(entry.name) },
                                supportingContent = { M3Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                                leadingContent = { M3Icon(Icons.Rounded.Gamepad, null, tint = MaterialTheme.colorScheme.primary) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                } else {
                    item {
                        ListItem(
                            headlineContent = {
                                M3Text(strings.noneYet, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
                item { HorizontalDivider() }
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    ListItem(
                        headlineContent = {
                            M3Text(strings.wip, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        },
                    )
                }
                controllerFaq.filter { it.status == ControllerStatus.Wip }.forEach { entry ->
                    item {
                        ListItem(
                            headlineContent = { M3Text(entry.name) },
                            supportingContent = { M3Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                            leadingContent = { M3Icon(Icons.Rounded.Gamepad, null, tint = MaterialTheme.colorScheme.tertiary) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
                item { HorizontalDivider() }
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    ListItem(
                        headlineContent = {
                            M3Text(strings.notYetImplemented, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )
                }
                controllerFaq.filter { it.status == ControllerStatus.NotYetImplemented }.forEach { entry ->
                    item {
                        ListItem(
                            headlineContent = { M3Text(entry.name) },
                            supportingContent = { M3Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                            leadingContent = {
                                M3Icon(Icons.Rounded.SportsEsports, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun FaqRowMiuix(entry: ControllerFaqEntry, status: ControllerStatus) {
    val tint = when (status) {
        ControllerStatus.Implemented -> MiuixTheme.colorScheme.primary
        ControllerStatus.Wip -> MiuixTheme.colorScheme.primary.copy(alpha = 0.6f)
        ControllerStatus.NotYetImplemented -> MiuixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.5f)
    }
    val icon = when (status) {
        ControllerStatus.Implemented, ControllerStatus.Wip -> Icons.Rounded.Gamepad
        ControllerStatus.NotYetImplemented -> Icons.Rounded.SportsEsports
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        MiuixIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            MiuixText(
                entry.name,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
            MiuixText(
                "${entry.code}  |  Firmware: ${entry.firmware}",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
