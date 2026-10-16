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

enum class VmStatus {
    STOPPED, RUNNING, PAUSED
}

data class VmConfigState(
    val selectedMachineName: String = "Default",
    val availableMachines: List<String> = listOf("Default"),
    val arch: String = "x86_64",
    val machineType: String = "pc",
    val cpuModel: String = "qemu64",
    val cpuCores: Int = 1,
    val ramMb: Int = 512,
    val cdromPath: String = "",
    val hardDiskAPath: String = "",
    val hardDiskBPath: String = "",
    val floppyAPath: String = "",
    val bootOrder: String = "cdrom",
    val graphicsCard: String = "std",
    val soundCard: String = "sb16",
    val networkCard: String = "e1000",
    val extraParams: String = "",
    val status: VmStatus = VmStatus.STOPPED
)
