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

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.max2idea.android.limbo.links.LinksManager
import com.max2idea.android.limbo.log.Logger
import com.max2idea.android.limbo.machine.BIOSImporter
import com.max2idea.android.limbo.machine.MachineExporter
import com.max2idea.android.limbo.machine.MachineImporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmDashboardScreen(
    viewModel: VmViewModel,
    onPickFile: (type: String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600
    val scrollState = rememberScrollState()

    var showMachineDropdown by remember { mutableStateOf(false) }
    var showNewVmDialog by remember { mutableStateOf(false) }
    var showNewDiskDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        LimboSettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    if (showNewVmDialog) {
        CreateVmDialog(
            onDismiss = { showNewVmDialog = false },
            onCreate = { vmName ->
                viewModel.createVm(vmName)
                showNewVmDialog = false
            }
        )
    }

    if (showNewDiskDialog) {
        CreateDiskImageDialog(
            onDismiss = { showNewDiskDialog = false },
            onCreate = { filename, sizeGb ->
                viewModel.createHardDiskImage(context, filename, sizeGb)
                showNewDiskDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showMachineDropdown = true }
                        ) {
                            Icon(Icons.Default.Computer, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = state.selectedMachineName,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showMachineDropdown,
                            onDismissRequest = { showMachineDropdown = false }
                        ) {
                            state.availableMachines.forEach { machineName ->
                                DropdownMenuItem(
                                    text = { Text(machineName) },
                                    onClick = {
                                        viewModel.selectMachine(machineName)
                                        showMachineDropdown = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showNewVmDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New VM")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("OS Downloads & Quick Links") },
                                onClick = {
                                    showMoreMenu = false
                                    try {
                                        (context as? Activity)?.let { activity ->
                                            LinksManager(activity).show()
                                        }
                                    } catch (ignored: Throwable) {}
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Limbo Log File") },
                                onClick = {
                                    showMoreMenu = false
                                    try {
                                        (context as? Activity)?.let { activity ->
                                            Logger.viewLimboLog(activity)
                                        }
                                    } catch (ignored: Throwable) {}
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import VM Configuration") },
                                onClick = {
                                    showMoreMenu = false
                                    try {
                                        (context as? Activity)?.let { activity ->
                                            MachineImporter.promptImportMachines(activity)
                                        }
                                    } catch (ignored: Throwable) {}
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export VM Configuration") },
                                onClick = {
                                    showMoreMenu = false
                                    try {
                                        (context as? Activity)?.let { activity ->
                                            MachineExporter.promptExport(activity)
                                        }
                                    } catch (ignored: Throwable) {}
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Custom BIOS") },
                                onClick = {
                                    showMoreMenu = false
                                    try {
                                        (context as? Activity)?.let { activity ->
                                            BIOSImporter.promptImportBIOSFile(activity)
                                        }
                                    } catch (ignored: Throwable) {}
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (state.status == VmStatus.RUNNING) {
                        viewModel.stopVm()
                    } else {
                        viewModel.startVm(context)
                    }
                },
                icon = {
                    if (state.status == VmStatus.RUNNING) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                },
                text = {
                    Text(if (state.status == VmStatus.RUNNING) "Stop VM" else "Start VM")
                },
                containerColor = if (state.status == VmStatus.RUNNING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isExpandedScreen) {
                // Adaptive Two-Column Layout for Unfolded Foldables, Tablets, & Landscape Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HardwareCard(
                            state = state,
                            onArchChange = { viewModel.updateArch(it) },
                            onMachineTypeChange = { viewModel.updateMachineType(it) },
                            onRamChange = { viewModel.updateRam(it) },
                            onCoresChange = { viewModel.updateCpuCores(it) }
                        )

                        DisplayAndAudioCard(
                            state = state,
                            onGraphicsChange = { viewModel.updateGraphicsCard(it) },
                            onSoundChange = { viewModel.updateSoundCard(it) }
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StorageCard(
                            state = state,
                            onPickFile = onPickFile,
                            onCreateDiskClick = { showNewDiskDialog = true },
                            onBootOrderChange = { viewModel.updateBootOrder(it) }
                        )

                        NetworkingCard(
                            state = state,
                            onNetChange = { viewModel.updateNetworkCard(it) }
                        )

                        AdvancedParamsCard(
                            extraParams = state.extraParams,
                            onExtraParamsChange = { viewModel.updateExtraParams(it) }
                        )
                    }
                }
            } else {
                // Single Column Layout for Compact Folded Phones
                HardwareCard(
                    state = state,
                    onArchChange = { viewModel.updateArch(it) },
                    onMachineTypeChange = { viewModel.updateMachineType(it) },
                    onRamChange = { viewModel.updateRam(it) },
                    onCoresChange = { viewModel.updateCpuCores(it) }
                )

                StorageCard(
                    state = state,
                    onPickFile = onPickFile,
                    onCreateDiskClick = { showNewDiskDialog = true },
                    onBootOrderChange = { viewModel.updateBootOrder(it) }
                )

                DisplayAndAudioCard(
                    state = state,
                    onGraphicsChange = { viewModel.updateGraphicsCard(it) },
                    onSoundChange = { viewModel.updateSoundCard(it) }
                )

                NetworkingCard(
                    state = state,
                    onNetChange = { viewModel.updateNetworkCard(it) }
                )

                AdvancedParamsCard(
                    extraParams = state.extraParams,
                    onExtraParamsChange = { viewModel.updateExtraParams(it) }
                )
            }

            Spacer(Modifier.height(80.dp)) // Padding for FAB
        }
    }
}

@Composable
fun CreateVmDialog(
    onDismiss: () -> Unit,
    onCreate: (vmName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Virtual Machine") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Machine Name") },
                    placeholder = { Text("e.g. Windows_98") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiskImageDialog(
    onDismiss: () -> Unit,
    onCreate: (filename: String, sizeGb: Int) -> Unit
) {
    var filename by remember { mutableStateOf("") }
    var selectedSizeGb by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Hard Disk Image (qcow2)") },
        text = {
            Column {
                OutlinedTextField(
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text("Disk Filename") },
                    placeholder = { Text("e.g. hdd_drive1.qcow2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text("Disk Size (GB)", style = MaterialTheme.typography.labelLarge)
                val sizeOptions = listOf(1, 2, 4, 10, 20)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    sizeOptions.forEachIndexed { index, size ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = sizeOptions.size),
                            onClick = { selectedSizeGb = size },
                            selected = selectedSizeGb == size
                        ) {
                            Text(
                                text = "${size}G",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(filename, selectedSizeGb) },
                enabled = filename.isNotBlank()
            ) {
                Text("Create Image")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getHumanReadableMachineName(arch: String, machineType: String, cpuModel: String): String {
    val cleanArch = arch.lowercase()
    val cleanType = machineType.lowercase()

    return when {
        cleanArch.contains("ppc") || cleanArch.contains("powerpc") -> {
            when (cleanType) {
                "g3beige" -> "Apple Power Mac G3 (Beige)"
                "prep" -> "PowerPC PReP Board"
                else -> "Apple Power Mac G4 / iMac G3"
            }
        }
        cleanArch.contains("arm") -> {
            when (cleanType) {
                "versatilepb" -> "ARM VersatilePB Board"
                "realview-eb" -> "ARM RealView EB Board"
                else -> "ARM64 Virt Board"
            }
        }
        cleanArch.contains("sparc") -> {
            when (cleanType) {
                "sun4u" -> "Sun UltraSPARC Workstation"
                "ss-10" -> "Sun SPARCstation 10"
                else -> "Sun SPARCstation 5"
            }
        }
        else -> {
            when (cleanType) {
                "q35" -> "Modern Intel / AMD PC (Q35)"
                "isapc" -> "Legacy ISA PC"
                else -> "Standard Intel / AMD PC (i440FX)"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareCard(
    state: VmConfigState,
    onArchChange: (String) -> Unit,
    onMachineTypeChange: (String) -> Unit,
    onRamChange: (Int) -> Unit,
    onCoresChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val humanReadableMachineName = getHumanReadableMachineName(state.arch, state.machineType, state.cpuModel)

    val machineTypeOptions = when {
        state.arch.contains("ppc", ignoreCase = true) || state.arch.contains("powerpc", ignoreCase = true) -> listOf(
            "mac99" to "Power Mac G4",
            "g3beige" to "Power Mac G3",
            "prep" to "PReP Board"
        )
        state.arch.contains("arm", ignoreCase = true) -> listOf(
            "virt" to "ARM Virt Board",
            "versatilepb" to "VersatilePB",
            "realview-eb" to "RealView EB"
        )
        state.arch.contains("sparc", ignoreCase = true) -> listOf(
            "SS-5" to "SPARCstation 5",
            "SS-10" to "SPARCstation 10",
            "sun4u" to "UltraSPARC"
        )
        else -> listOf(
            "pc" to "Standard PC (i440FX)",
            "q35" to "Modern PC (Q35)",
            "isapc" to "Legacy ISA PC"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Icon(Icons.Default.Memory, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System & Hardware",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = humanReadableMachineName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${state.ramMb} MB | ${state.cpuCores} Cores",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Target Architecture",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    val archOptions = listOf("x86_64", "ARM64", "PowerPC", "SPARC")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        archOptions.forEachIndexed { index, label ->
                            val isSelected = state.arch.equals(label, ignoreCase = true) ||
                                    (label == "ARM64" && state.arch.startsWith("arm", ignoreCase = true)) ||
                                    (label == "x86_64" && state.arch.startsWith("x86", ignoreCase = true)) ||
                                    (label == "PowerPC" && state.arch.startsWith("ppc", ignoreCase = true))
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = archOptions.size),
                                onClick = { onArchChange(label) },
                                selected = isSelected
                            ) {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Emulated Machine Model",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        machineTypeOptions.forEachIndexed { index, (code, label) ->
                            val isSelected = state.machineType.equals(code, ignoreCase = true) ||
                                    (code == "mac99" && state.machineType.isEmpty())
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = machineTypeOptions.size),
                                onClick = { onMachineTypeChange(code) },
                                selected = isSelected
                            ) {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "RAM Memory: ${state.ramMb} MB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.ramMb.toFloat(),
                        onValueChange = { onRamChange(it.toInt()) },
                        valueRange = 128f..8192f,
                        steps = 63
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CPU Cores: ${state.cpuCores}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (state.cpuCores > 1) onCoresChange(state.cpuCores - 1) },
                                enabled = state.cpuCores > 1
                            ) {
                                Text("-")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { if (state.cpuCores < 16) onCoresChange(state.cpuCores + 1) },
                                enabled = state.cpuCores < 16
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCard(
    state: VmConfigState,
    onPickFile: (String) -> Unit,
    onCreateDiskClick: () -> Unit,
    onBootOrderChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Icon(Icons.Default.DiscFull, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Storage & Drives",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // CD-ROM
                    OutlinedTextField(
                        value = state.cdromPath.ifEmpty { "None Selected" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("CD-ROM (ISO)") },
                        trailingIcon = {
                            IconButton(onClick = { onPickFile("cdrom") }) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Browse CDROM")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Hard Disk A
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.hardDiskAPath.ifEmpty { "None Selected" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hard Disk A (IMG/QCOW2)") },
                            trailingIcon = {
                                IconButton(onClick = { onPickFile("hda") }) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse HDD")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedButton(onClick = onCreateDiskClick) {
                            Text("+ New Disk")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Boot Priority Order",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    val bootOptions = listOf(
                        "d,c" to "CD-ROM ➔ Hard Disk",
                        "c,d" to "Hard Disk ➔ CD-ROM",
                        "a,c" to "Floppy ➔ Hard Disk",
                        "c" to "Hard Disk Only",
                        "d" to "CD-ROM Only"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        bootOptions.forEach { (code, label) ->
                            val isSelected = state.bootOrder.equals(code, ignoreCase = true) ||
                                    (code == "d,c" && state.bootOrder.equals("CD-ROM", ignoreCase = true)) ||
                                    (code == "c,d" && state.bootOrder.equals("Hard Disk", ignoreCase = true))

                            OutlinedButton(
                                onClick = { onBootOrderChange(code) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (isSelected) {
                                    androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    if (isSelected) {
                                        Text("Selected", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAndAudioCard(
    state: VmConfigState,
    onGraphicsChange: (String) -> Unit,
    onSoundChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Icon(Icons.Default.Monitor, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Graphics & Audio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${state.graphicsCard} | ${state.soundCard}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("VGA Emulation: ${state.graphicsCard}", style = MaterialTheme.typography.labelLarge)
                    val vgaOptions = listOf("std", "cirrus", "vmware", "qxl")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        vgaOptions.forEachIndexed { index, vga ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = vgaOptions.size),
                                onClick = { onGraphicsChange(vga) },
                                selected = state.graphicsCard.equals(vga, ignoreCase = true)
                            ) {
                                Text(
                                    text = vga,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Sound Card: ${state.soundCard}", style = MaterialTheme.typography.labelLarge)
                    val soundOptions = listOf("sb16", "es1370", "ac97", "hda")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        soundOptions.forEachIndexed { index, sound ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = soundOptions.size),
                                onClick = { onSoundChange(sound) },
                                selected = state.soundCard.equals(sound, ignoreCase = true)
                            ) {
                                Text(
                                    text = sound,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkingCard(
    state: VmConfigState,
    onNetChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Networking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = state.networkCard,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text("Network Adapter: ${state.networkCard}", style = MaterialTheme.typography.labelLarge)
                    val netOptions = listOf("e1000", "rtl8139", "virtio-net")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        netOptions.forEachIndexed { index, net ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = netOptions.size),
                                onClick = { onNetChange(net) },
                                selected = state.networkCard.equals(net, ignoreCase = true)
                            ) {
                                Text(
                                    text = net,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedParamsCard(
    extraParams: String,
    onExtraParamsChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Text(
                    text = "Advanced QEMU Arguments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = extraParams,
                        onValueChange = onExtraParamsChange,
                        label = { Text("Extra Parameters") },
                        placeholder = { Text("-vnc :0 -accel tcg") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun VmDashboardPreview() {
    com.max2idea.android.limbo.ui.theme.LimboTheme {
        VmDashboardScreen(viewModel = VmViewModel())
    }
}
