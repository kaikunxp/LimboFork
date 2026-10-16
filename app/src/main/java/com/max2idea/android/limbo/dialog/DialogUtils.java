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
package com.max2idea.android.limbo.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {
    private static final String TAG = "DialogUtils";

    public static void UIAlert(Activity activity, String title, String body) {
        TextView textView = new TextView(activity);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(body);
        ScrollView view = new ScrollView(activity);
        view.addView(textView);

        new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(activity.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void UIAlert(Activity activity, String title, String body, int textSize, boolean cancelable,
                               String button1title, DialogInterface.OnClickListener button1Listener,
                               String button2title, DialogInterface.OnClickListener button2Listener,
                               String button3title, DialogInterface.OnClickListener button3Listener
    ) {
        TextView textView = new TextView(activity);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(body);
        if (textSize > 0)
            textView.setTextSize(textSize);
        ScrollView view = new ScrollView(activity);
        view.addView(textView);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(title);
        builder.setView(view);

        if (button1title != null)
            builder.setPositiveButton(button1title, button1Listener);
        if (button2title != null)
            builder.setNegativeButton(button2title, button2Listener);
        if (button3title != null)
            builder.setNeutralButton(button3title, button3Listener);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.show();
    }
}
