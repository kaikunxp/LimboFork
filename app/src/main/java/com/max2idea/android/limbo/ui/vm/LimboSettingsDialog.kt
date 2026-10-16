/*
Copyright (C) Max Kastanas 2012
Copyright (C) 2024 LimboFork Contributors

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.max2idea.android.limbo.ui.vm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.max2idea.android.limbo.main.LimboSettingsManager

@Composable
fun LimboSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var alwaysShowMenu by remember {
        mutableStateOf(LimboSettingsManager.getAlwaysShowMenuToolbar(context))
    }
    var fullScreen by remember {
        mutableStateOf(LimboSettingsManager.getFullscreen(context))
    }
    var keyDelay by remember {
        mutableStateOf(LimboSettingsManager.getKeyPressDelay(context).toFloat())
    }
    var mouseDelay by remember {
        mutableStateOf(LimboSettingsManager.getMouseButtonDelay(context).toFloat())
    }
    var enableAaudio by remember {
        mutableStateOf(LimboSettingsManager.getEnableAaudio(context))
    }
    var dnsServer by remember {
        mutableStateOf(LimboSettingsManager.getDNSServer(context))
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Global Emulator Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Display & UI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Always Show Menu Bar")
                    Switch(
                        checked = alwaysShowMenu,
                        onCheckedChange = { alwaysShowMenu = it }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fullscreen Mode")
                    Switch(
                        checked = fullScreen,
                        onCheckedChange = { fullScreen = it }
                    )
                }

                Text("Input Sensitivity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Column {
                    Text("Keyboard Press Delay: ${keyDelay.toInt()} ms")
                    Slider(
                        value = keyDelay,
                        onValueChange = { keyDelay = it },
                        valueRange = 50f..500f,
                        steps = 9
                    )
                }

                Column {
                    Text("Mouse Click Delay: ${mouseDelay.toInt()} ms")
                    Slider(
                        value = mouseDelay,
                        onValueChange = { mouseDelay = it },
                        valueRange = 50f..500f,
                        steps = 9
                    )
                }

                Text("Audio & Network", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable AAudio")
                    Switch(
                        checked = enableAaudio,
                        onCheckedChange = { enableAaudio = it }
                    )
                }

                OutlinedTextField(
                    value = dnsServer,
                    onValueChange = { dnsServer = it },
                    label = { Text("DNS Server") },
                    placeholder = { Text("8.8.8.8") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    LimboSettingsManager.setDNSServer(context, dnsServer)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
