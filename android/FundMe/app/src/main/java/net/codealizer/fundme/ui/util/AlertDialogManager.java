package net.codealizer.fundme.ui.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.login.LoginActivity;
import net.codealizer.fundme.ui.login.SignInActivity;
import net.codealizer.fundme.util.listeners.OnAlertCallbackListener;

/**
 * Created by Pranav on 11/19/16.
 */

public class AlertDialogManager {

    public static ProgressDialog showProgressDialog(Context c) {
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setTitle("Please Wait");
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    public static AlertDialog showNetworkErrorDialog(Context c) {
        AlertDialog alert = new AlertDialog.Builder(c)
                .setTitle("Network Error")
                .setMessage("Please connect to the internet")
                .setPositiveButton("OK", null).create();
        alert.show();
        return alert;
    }

    public static AlertDialog showMessageDialog(String s, String message, Context c) {
        AlertDialog alert = new AlertDialog.Builder(c)
                .setTitle(s)
                .setMessage(message)
                .setPositiveButton("OK", null).create();
        alert.show();
        return alert;
    }

    public static AlertDialog showEditTextDialog(String title, final OnAlertCallbackListener callback, Context c) {
        final View v = LayoutInflater.from(c).inflate(R.layout.alert_edit_text, null);

        AlertDialog alert = new AlertDialog.Builder(c)
                .setTitle(title)
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText t = (EditText) v.findViewById(R.id.email);
                        callback.onAlertCallback(t.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        alert.show();
        return alert;
    }
}
