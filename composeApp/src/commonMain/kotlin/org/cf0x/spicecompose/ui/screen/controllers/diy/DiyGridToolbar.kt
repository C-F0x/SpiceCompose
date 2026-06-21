package org.cf0x.spicecompose.ui.screen.controllers.diy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Permanent inline toolbar at the bottom of the editor canvas.
 *
 * Unlike the old popup dialogs, this is always visible in edit mode:
 *  - Grid toggle + X/Y step sliders
 *  - Snap mode: grid lines / guide points / off (radio)
 *  - Quick-add buttons that insert GuideLineWidget / GuidePointWidget into the widgets list
 */
@Composable
fun DiyGridToolbar(
    grid: GridSettings,
    onGridChange: (GridSettings) -> Unit,
    onAddGuideLine: (orient: String, pos: Float) -> Unit,
    onAddGuidePoint: (x: Float, y: Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var enabled by remember { mutableStateOf(grid.enabled) }
    var xStep by remember { mutableFloatStateOf(grid.xStep.toFloat()) }
    var yStep by remember { mutableFloatStateOf(grid.yStep.toFloat()) }
    var snapToLine by remember { mutableStateOf(grid.snapToLine) }
    var snapToPoint by remember { mutableStateOf(grid.snapToPoint) }

    fun commit() {
        onGridChange(GridSettings(enabled, xStep.toInt(), yStep.toInt(), snapToLine, snapToPoint))
    }

    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        // Header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Grid & Guides", style = MaterialTheme.typography.labelMedium)
            IconButton(onClick = { commit(); onDismiss() }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
            }
        }

        // Grid toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Show grid", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            Switch(checked = enabled, onCheckedChange = { enabled = it; commit() })
        }

        // X step
        Text("X step: ${xStep.toInt()}%", style = MaterialTheme.typography.labelSmall)
        Slider(value = xStep, onValueChange = { xStep = it }, onValueChangeFinished = { commit() },
            valueRange = 1f..20f, steps = 18, modifier = Modifier.fillMaxWidth())

        // Y step
        Text("Y step: ${yStep.toInt()}%", style = MaterialTheme.typography.labelSmall)
        Slider(value = yStep, onValueChange = { yStep = it }, onValueChangeFinished = { commit() },
            valueRange = 1f..20f, steps = 18, modifier = Modifier.fillMaxWidth())

        // Snap mode
        Text("Snap", style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = snapToLine && !snapToPoint, onClick = { snapToLine = true; snapToPoint = false; commit() })
            Text("Grid lines", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = snapToPoint && !snapToLine, onClick = { snapToPoint = true; snapToLine = false; commit() })
            Text("Guide points", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !snapToLine && !snapToPoint, onClick = { snapToLine = false; snapToPoint = false; commit() })
            Text("Off", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        }

        // Quick-add buttons
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { onAddGuideLine("h", 0.5f) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, "Add H Line", modifier = Modifier.size(18.dp))
            }
            Text("H Line", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = { onAddGuideLine("v", 0.5f) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, "Add V Line", modifier = Modifier.size(18.dp))
            }
            Text("V Line", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = { onAddGuidePoint(0.5f, 0.5f) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, "Add Point", modifier = Modifier.size(18.dp))
            }
            Text("Point", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.labelSmall)
        }
    }
}
