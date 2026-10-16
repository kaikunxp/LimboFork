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
package com.max2idea.android.limbo.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.max2idea.android.limbo.files.FileInstaller
import com.max2idea.android.limbo.files.FileUtils
import com.max2idea.android.limbo.ui.theme.LimboTheme
import com.max2idea.android.limbo.ui.vm.VmDashboardScreen
import com.max2idea.android.limbo.ui.vm.VmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class LimboComposeActivity : AppCompatActivity() {

    private val viewModel: VmViewModel by viewModels()
    private var activeFileType: String = ""

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            try {
                val fileUriString = FileUtils.getFileUriFromIntent(this, result.data, false)
                if (!fileUriString.isNullOrEmpty()) {
                    when (activeFileType) {
                        "cdrom" -> viewModel.updateCdromPath(fileUriString)
                        "hda" -> viewModel.updateHardDiskPath(fileUriString)
                        "fda" -> viewModel.updateFloppyPath(fileUriString)
                    }
                }
            } catch (t: Throwable) {
                Log.e("LimboComposeActivity", "Error picking file: ${t.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadNativeLibrariesSafely()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FileInstaller.installFiles(this@LimboComposeActivity, false)
            } catch (t: Throwable) {
                Log.e("LimboComposeActivity", "Error installing BIOS ROM assets: ${t.message}")
            }
        }

        setContent {
            LimboTheme {
                VmDashboardScreen(
                    viewModel = viewModel,
                    onPickFile = { fileType ->
                        launchFilePicker(fileType)
                    }
                )
            }
        }
    }

    private fun launchFilePicker(fileType: String) {
        activeFileType = fileType
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        filePickerLauncher.launch(intent)
    }

    private fun loadNativeLibrariesSafely() {
        val libs = listOf(
            "compat-limbo", "compat-musl", "glib-2.0", "pixman-1",
            "compat-SDL2-addons", "SDL2", "compat-SDL2-ext", "limbo"
        )
        for (lib in libs) {
            try {
                System.loadLibrary(lib)
            } catch (t: Throwable) {
                Log.w("LimboComposeActivity", "Could not load library $lib: ${t.message}")
            }
        }
        loadQEMULibSafely()
    }

    open fun loadQEMULibSafely() {
        // Subclasses override for target guest arch
    }
}
