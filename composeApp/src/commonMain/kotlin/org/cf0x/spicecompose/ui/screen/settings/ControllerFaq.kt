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
import org.cf0x.spicecompose.ui.theme.ThemePreferences
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

data class ControllerFaqEntry(
    val code: String,
    val firmware: String,
    val name: String,
    val hasController: Boolean,
)

val controllerFaq = listOf(
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

@Composable
fun ControllerFaqScreen(onBack: () -> Unit) {
    val fullscreen = LocalFullscreenMode.current
    val uiMode = LocalUiMode.current
    val p = ThemePreferences

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
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        controllerFaq.filter { it.hasController }.forEach { entry ->
                            FaqRowMiuix(entry, implemented = true)
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
                        controllerFaq.filter { !it.hasController }.forEach { entry ->
                            FaqRowMiuix(entry, implemented = false)
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
                controllerFaq.filter { it.hasController }.forEach { entry ->
                    item {
                        ListItem(
                            headlineContent = { M3Text(entry.name) },
                            supportingContent = { M3Text("${entry.code}  |  Firmware: ${entry.firmware}") },
                            leadingContent = { M3Icon(Icons.Rounded.Gamepad, null, tint = MaterialTheme.colorScheme.primary) },
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
                controllerFaq.filter { !it.hasController }.forEach { entry ->
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
private fun FaqRowMiuix(entry: ControllerFaqEntry, implemented: Boolean) {
    val tint = if (implemented) MiuixTheme.colorScheme.primary
               else MiuixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.5f)
    val icon = if (implemented) Icons.Rounded.Gamepad else Icons.Rounded.SportsEsports

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
