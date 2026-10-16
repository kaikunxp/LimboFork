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

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max2idea.android.limbo.files.FileUtils
import com.max2idea.android.limbo.machine.Machine
import com.max2idea.android.limbo.machine.MachineController
import com.max2idea.android.limbo.main.Config
import com.max2idea.android.limbo.main.LimboApplication
import com.max2idea.android.limbo.main.LimboComposeActivity
import com.max2idea.android.limbo.main.LimboSDLActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VmConfigState())
    val uiState: StateFlow<VmConfigState> = _uiState.asStateFlow()

    init {
        loadActiveMachineState()
    }

    private fun loadActiveMachineState() {
        try {
            val controller = MachineController.getInstance()
            val machine = controller.machine
            val storedMachines = controller.storedMachines ?: listOf("Default")

            if (machine != null) {
                val machineArch = machine.arch ?: "x86_64"
                when (machineArch.lowercase()) {
                    "x86_64", "x86" -> LimboApplication.arch = Config.Arch.x86_64
                    "arm64", "arm" -> LimboApplication.arch = Config.Arch.arm64
                    "powerpc", "ppc", "ppc64" -> LimboApplication.arch = Config.Arch.ppc64
                    "sparc", "sparc64" -> LimboApplication.arch = Config.Arch.sparc
                    else -> LimboApplication.arch = Config.Arch.x86_64
                }

                _uiState.update {
                    it.copy(
                        selectedMachineName = machine.name ?: "Default",
                        availableMachines = if (storedMachines.isEmpty()) listOf("Default") else storedMachines,
                        arch = machineArch,
                        machineType = machine.machineType ?: "pc",
                        cpuModel = machine.cpu ?: "qemu64",
                        cpuCores = if (machine.cpuNum > 0) machine.cpuNum else 1,
                        ramMb = if (machine.memory > 0) machine.memory else 512,
                        cdromPath = machine.cdImagePath ?: "",
                        hardDiskAPath = machine.hdaImagePath ?: "",
                        bootOrder = machine.bootDevice ?: "cdrom",
                        graphicsCard = machine.vga ?: "std",
                        soundCard = machine.soundCard ?: "sb16",
                        networkCard = machine.networkCard ?: "e1000",
                        extraParams = machine.extraParams ?: ""
                    )
                }
            }
        } catch (ignored: Throwable) {
            // Fallback on defaults
        }
    }

    fun selectMachine(name: String) {
        try {
            val controller = MachineController.getInstance()
            controller.setStoredMachine(name)
            loadActiveMachineState()
        } catch (ignored: Throwable) {}
    }

    fun createVm(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        try {
            val controller = MachineController.getInstance()
            controller.createVM(cleanName)
            loadActiveMachineState()
        } catch (ignored: Throwable) {}
    }

    fun createHardDiskImage(context: Context, filename: String, sizeGb: Int): String? {
        val templateImage = when (sizeGb) {
            1 -> "hd1g.qcow2"
            2 -> "hd2g.qcow2"
            4 -> "hd4g.qcow2"
            10 -> "hd10g.qcow2"
            20 -> "hd20g.qcow2"
            else -> "hd1g.qcow2"
        }
        var name = filename.trim()
        if (name.isEmpty()) return null
        if (!name.endsWith(".qcow2")) {
            name += ".qcow2"
        }

        return try {
            val createdPath = FileUtils.createImgFromTemplate(
                context, templateImage, name, Machine.FileType.HDA
            )
            if (!createdPath.isNullOrEmpty()) {
                updateHardDiskPath(createdPath)
            }
            createdPath
        } catch (t: Throwable) {
            null
        }
    }

    fun updateArch(arch: String) {
        _uiState.update { it.copy(arch = arch) }
        try {
            val machine = MachineController.getInstance().machine
            machine?.setArch(arch)
            when (arch.lowercase()) {
                "x86_64", "x86" -> {
                    LimboApplication.arch = Config.Arch.x86_64
                    machine?.setCpu("qemu64")
                    val targetMachineType = "pc"
                    machine?.setMachineType(targetMachineType)
                    _uiState.update { it.copy(machineType = targetMachineType, cpuModel = "qemu64") }
                }
                "arm64", "arm" -> {
                    LimboApplication.arch = Config.Arch.arm64
                    machine?.setCpu("cortex-a57")
                    val targetMachineType = "virt"
                    machine?.setMachineType(targetMachineType)
                    _uiState.update { it.copy(machineType = targetMachineType, cpuModel = "cortex-a57") }
                }
                "powerpc", "ppc", "ppc64" -> {
                    LimboApplication.arch = Config.Arch.ppc64
                    machine?.setCpu("G4")
                    val targetMachineType = "mac99"
                    machine?.setMachineType(targetMachineType)
                    _uiState.update { it.copy(machineType = targetMachineType, cpuModel = "G4") }
                }
                "sparc", "sparc64" -> {
                    LimboApplication.arch = Config.Arch.sparc
                    machine?.setCpu("Default")
                    val targetMachineType = "SS-5"
                    machine?.setMachineType(targetMachineType)
                    _uiState.update { it.copy(machineType = targetMachineType, cpuModel = "Default") }
                }
            }
        } catch (ignored: Throwable) {}
    }

    fun updateMachineType(type: String) {
        _uiState.update { it.copy(machineType = type) }
        try {
            val machine = MachineController.getInstance().machine
            machine?.setMachineType(type)
        } catch (ignored: Throwable) {}
    }

    fun updateRam(ramMb: Int) {
        _uiState.update { it.copy(ramMb = ramMb) }
        try {
            MachineController.getInstance().machine?.setMemory(ramMb)
        } catch (ignored: Throwable) {}
    }

    fun updateCpuCores(cores: Int) {
        _uiState.update { it.copy(cpuCores = cores) }
        try {
            MachineController.getInstance().machine?.setCpuNum(cores)
        } catch (ignored: Throwable) {}
    }

    fun updateCdromPath(path: String) {
        _uiState.update { it.copy(cdromPath = path) }
        try {
            MachineController.getInstance().machine?.setCdImagePath(path)
        } catch (ignored: Throwable) {}
    }

    fun updateHardDiskPath(path: String) {
        _uiState.update { it.copy(hardDiskAPath = path) }
        try {
            MachineController.getInstance().machine?.setHdaImagePath(path)
        } catch (ignored: Throwable) {}
    }

    fun updateFloppyPath(path: String) {
        _uiState.update { it.copy(floppyAPath = path) }
        try {
            MachineController.getInstance().machine?.setFdaImagePath(path)
        } catch (ignored: Throwable) {}
    }

    fun updateBootOrder(bootOrder: String) {
        _uiState.update { it.copy(bootOrder = bootOrder) }
        try {
            val device = when (bootOrder.lowercase()) {
                "cdrom", "cd-rom" -> "d"
                "floppy" -> "a"
                else -> "c"
            }
            MachineController.getInstance().machine?.setBootDevice(device)
        } catch (ignored: Throwable) {}
    }

    fun updateGraphicsCard(vga: String) {
        _uiState.update { it.copy(graphicsCard = vga) }
        try {
            MachineController.getInstance().machine?.setVga(vga)
        } catch (ignored: Throwable) {}
    }

    fun updateSoundCard(sound: String) {
        _uiState.update { it.copy(soundCard = sound) }
        try {
            MachineController.getInstance().machine?.setSoundCard(sound)
        } catch (ignored: Throwable) {}
    }

    fun updateNetworkCard(net: String) {
        _uiState.update { it.copy(networkCard = net) }
        try {
            MachineController.getInstance().machine?.setNetworkCard(net)
        } catch (ignored: Throwable) {}
    }

    fun updateExtraParams(params: String) {
        _uiState.update { it.copy(extraParams = params) }
        try {
            MachineController.getInstance().machine?.setExtraParams(params)
        } catch (ignored: Throwable) {}
    }

    fun startVm(context: Context) {
        viewModelScope.launch {
            if (Config.clientClass == null) {
                Config.clientClass = LimboComposeActivity::class.java
            }
            _uiState.update { it.copy(status = VmStatus.RUNNING) }
            val intent = Intent(context, LimboSDLActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun stopVm() {
        _uiState.update { it.copy(status = VmStatus.STOPPED) }
    }
}
