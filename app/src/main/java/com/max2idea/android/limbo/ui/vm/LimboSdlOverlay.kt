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

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.max2idea.android.limbo.keyboard.KeyboardUtils
import com.max2idea.android.limbo.machine.MachineAction
import com.max2idea.android.limbo.machine.MachineController
import com.max2idea.android.limbo.main.LimboSDLActivity
import com.max2idea.android.limbo.ui.theme.LimboTheme

object LimboSdlOverlay {

    @JvmStatic
    fun attachOverlay(activity: LimboSDLActivity, topLayout: ViewGroup) {
        val composeView = ComposeView(activity).apply {
            setContent {
                LimboTheme {
                    VmControlOverlay(
                        vmName = MachineController.getInstance().machineName.ifEmpty { "Virtual Machine" },
                        isPaused = MachineController.getInstance().isPaused,
                        onToggleKeyboard = {
                            KeyboardUtils.showKeyboard(activity, true, activity.surfaceView)
                        },
                        onToggleKeyMapper = {
                            activity.toggleKeyMapper()
                        },
                        onToggleMouseMode = {
                            activity.promptMouseMode()
                        },
                        onToggleDisplayScale = {
                            activity.toggleDisplayScaleMode()
                        },
                        onSendCtrlAltDel = {
                            activity.sendCtrlAltDel()
                        },
                        onSendTextPrompt = {
                            activity.promptSendText()
                        },
                        onPauseResume = {
                            if (MachineController.getInstance().isPaused) {
                                activity.notifyAction(MachineAction.CONTINUE_VM, 0)
                            } else {
                                activity.promptPause()
                            }
                        },
                        onStopVm = {
                            activity.promptStopVm()
                        }
                    )
                }
            }
        }
        topLayout.addView(composeView)
    }
}
