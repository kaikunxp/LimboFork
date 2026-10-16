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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmControlOverlay(
    vmName: String,
    isPaused: Boolean,
    onToggleKeyboard: () -> Unit,
    onToggleKeyMapper: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleDisplayScale: () -> Unit = {},
    onSendCtrlAltDel: () -> Unit,
    onSendTextPrompt: () -> Unit,
    onPauseResume: () -> Unit,
    onStopVm: () -> Unit,
    onSendSpecialKey: (keyCode: Int) -> Unit = {}
) {
    var isOverlayVisible by remember { mutableStateOf(true) }
    var showQuickKeys by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isOverlayVisible,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                color = Color(0xDD1D1B20), // Dark Expressive Surface Container
                contentColor = Color.White,
                modifier = Modifier
                    .statusBarsPadding()
                    .displayCutoutPadding()
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = vmName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            IconButton(onClick = onToggleKeyboard) {
                                Icon(Icons.Default.Keyboard, contentDescription = "Soft Keyboard", tint = Color(0xFFD0BCFF))
                            }
                            IconButton(onClick = { showQuickKeys = !showQuickKeys }) {
                                Icon(Icons.Default.SportsEsports, contentDescription = "Quick Keypad", tint = Color(0xFFCCC2DC))
                            }
                            IconButton(onClick = onToggleMouseMode) {
                                Icon(Icons.Default.TouchApp, contentDescription = "Mouse Mode", tint = Color(0xFFEFB8C8))
                            }
                            IconButton(onClick = onToggleDisplayScale) {
                                Icon(Icons.Default.FitScreen, contentDescription = "Display Scale & Zoom", tint = Color(0xFF81D4FA))
                            }
                            IconButton(onClick = onPauseResume) {
                                Icon(
                                    if (isPaused) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                                    contentDescription = if (isPaused) "Resume" else "Pause",
                                    tint = Color(0xFF42A5F5)
                                )
                            }
                            IconButton(onClick = onStopVm) {
                                Icon(Icons.Default.StopCircle, contentDescription = "Stop VM", tint = Color(0xFFF2B8B5))
                            }
                            IconButton(onClick = { isOverlayVisible = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Hide Controls", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )

                    // Quick Action Chips & Special Keys Bar
                    AnimatedVisibility(visible = showQuickKeys) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = onSendCtrlAltDel,
                                label = { Text("Ctrl+Alt+Del") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) }
                            )
                            AssistChip(
                                onClick = onSendTextPrompt,
                                label = { Text("Send Text") },
                                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                            )

                            listOf("Esc", "Tab", "Ctrl", "Alt", "F1", "F2", "F5", "F12", "↑", "↓", "←", "→").forEach { key ->
                                AssistChip(
                                    onClick = { /* Send key event */ },
                                    label = { Text(key) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating re-show button when overlay is hidden
        if (!isOverlayVisible) {
            IconButton(
                onClick = { isOverlayVisible = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC21005D),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = "Show Controls",
                        modifier = Modifier.padding(12.dp),
                        tint = Color(0xFFD0BCFF)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun VmControlOverlayPreview() {
    com.max2idea.android.limbo.ui.theme.LimboTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            VmControlOverlay(
                vmName = "Apple Power Mac G4",
                isPaused = false,
                onToggleKeyboard = {},
                onToggleKeyMapper = {},
                onToggleMouseMode = {},
                onSendCtrlAltDel = {},
                onSendTextPrompt = {},
                onPauseResume = {},
                onStopVm = {}
            )
        }
    }
}
