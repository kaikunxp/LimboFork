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
package com.max2idea.android.limbo.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.limbo.emu.main.R;
import com.max2idea.android.limbo.dialog.DialogUtils;
import com.max2idea.android.limbo.files.FileUtils;
import com.max2idea.android.limbo.help.Help;
import com.max2idea.android.limbo.install.Installer;
import com.max2idea.android.limbo.machine.Machine;
import com.max2idea.android.limbo.machine.MachineAction;
import com.max2idea.android.limbo.network.NetworkUtils;
import com.max2idea.android.limbo.toast.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;

public class LimboActivityCommon {
    private static final String TAG = "LimboActivityCommon";

    public static void promptStopVM(final Activity activity, final ViewListener viewListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.ShutdownVM)
                        .setMessage(R.string.ShutdownVMWarning)
                        .setPositiveButton(R.string.shutdown, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.STOP_VM, null);
                            }
                        })
                        .setNeutralButton(R.string.Pause, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.PAUSE_VM, null);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
    }

    public static void promptPausedErrorVM(final Activity activity, final String msg, final ViewListener viewListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = msg != null ? msg : activity.getString(R.string.CouldNotPauseVMViewLogFileDetails);
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.Error)
                        .setMessage(message)
                        .setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Thread t = new Thread(new Runnable() {
                                    public void run() {
                                        viewListener.onAction(MachineAction.CONTINUE_VM, 0);
                                    }
                                });
                                t.start();
                            }
                        })
                        .show();
            }
        });
    }

    public static void promptPause(final Activity activity, final ViewListener viewListener) {
        if (!LimboSettingsManager.getEnableQmp(activity)) {
            ToastUtils.toastShort(activity, activity.getString(R.string.EnableQMPForSavingVMState));
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView stateView = new TextView(activity);
                stateView.setText(R.string.pauseVMWarning);
                stateView.setPadding(20, 20, 20, 20);

                new MaterialAlertDialogBuilder(activity)
                        .setTitle(activity.getString(R.string.PauseVM))
                        .setView(stateView)
                        .setPositiveButton(activity.getString(R.string.Pause), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.PAUSE_VM, null);
                            }
                        })
                        .setNegativeButton(activity.getString(android.R.string.cancel), null)
                        .show();
            }
        });
    }

    public static void promptResetVM(final Activity activity, final ViewListener viewListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.ResetVM)
                        .setMessage(R.string.ResetVMWarning)
                        .setPositiveButton(activity.getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.RESET_VM, null);
                            }
                        })
                        .setNegativeButton(activity.getString(android.R.string.no), null)
                        .show();
            }
        });
    }

    public static void promptPausedVM(final Activity activity, final ViewListener viewListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(activity)
                        .setCancelable(false)
                        .setTitle(R.string.Paused)
                        .setMessage(R.string.VMPausedPressOkToExit)
                        .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.STOP_VM, null);
                            }
                        })
                        .show();
            }
        });
    }

    public static void promptVNCServer(final Context context, final String msg, final ViewListener viewListener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(context)
                        .setTitle(context.getString(R.string.vncServer))
                        .setMessage(context.getString(R.string.vncServer) + ": "
                                + NetworkUtils.getVNCAddress(context)
                                + ":" + Config.defaultVNCPort + "\n"
                                + "\n" + msg)
                        .setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                viewListener.onAction(MachineAction.START_VM, null);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.Cancel), null)
                        .show();
            }
        });
    }

    public static void promptMachinesImported(final Activity activity, final ArrayList<Machine> machines) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.UIAlert(activity, activity.getString(R.string.importMachines),
                        activity.getString(R.string.machinesImported)
                                + ": " + machines.size() + "\n" + activity.getString(R.string.reassignDiskFilesInstructions),
                        16, false,
                        activity.getString(android.R.string.ok), null, null, null, null, null);
            }
        });
    }

    public static void promptLicense(final Activity activity, final String title, final String body) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(activity);
                textView.setText(body);
                textView.setTextSize(10);
                textView.setPadding(20, 20, 20, 20);

                ScrollView scrollView = new ScrollView(activity);
                scrollView.addView(textView);

                new MaterialAlertDialogBuilder(activity)
                        .setTitle(title)
                        .setView(scrollView)
                        .setPositiveButton(activity.getString(R.string.IAcknowledge), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (LimboSettingsManager.isFirstLaunch(activity)) {
                                    Installer.installFiles(activity, true);
                                    Help.showHelp(activity);
                                    showChangelog(activity);
                                }
                                LimboSettingsManager.setFirstLaunch(activity);
                            }
                        })
                        .show();
            }
        });
    }

    public static void tapNotSupported(final Activity activity, final String userid) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.UIAlert(activity, activity.getString(R.string.tapUserId) + ": " + userid,
                        activity.getString(R.string.tapNotSupportInstructions));
            }
        });
    }

    public static void promptTap(final Activity activity, final String userid) {
        final DialogInterface.OnClickListener helpListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                goToURL(activity, Config.faqLink);
            }
        };

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.UIAlert(activity,
                        activity.getString(R.string.TapDeviceFound),
                        activity.getString(R.string.tunDeviceWarning) + ": " + userid + "\n",
                        16, false, activity.getString(android.R.string.ok), null,
                        null, null, activity.getString(R.string.TAPHelp), helpListener);
            }
        });
    }

    public static void goToURL(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static void onNetworkUser(final Activity activity) {
        final DialogInterface.OnClickListener helpListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LimboActivityCommon.goToURL(activity, Config.faqLink);
            }
        };

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.UIAlert(activity,
                        activity.getString(R.string.network),
                        activity.getString(R.string.externalNetworkWarning),
                        16, false, activity.getString(android.R.string.ok), null,
                        null, null, activity.getString(R.string.faq), helpListener);
            }
        });
    }

    public static void showChangelog(Activity activity) {
        try {
            TextView textView = new TextView(activity);
            textView.setPadding(20, 20, 20, 20);
            textView.setText(FileUtils.LoadFile(activity, "CHANGELOG", false));
            ScrollView view = new ScrollView(activity);
            view.addView(textView);

            new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.CHANGELOG))
                    .setView(view)
                    .setPositiveButton(activity.getString(android.R.string.ok), null)
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
