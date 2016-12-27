package net.codealizer.fundme.ui.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.login.LoginActivity;
import net.codealizer.fundme.ui.login.SignInActivity;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAlertCallbackListener;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Pranav on 11/19/16.
 */

public class AlertDialogManager {

    /**
     * Displays a generic "Please Wait" progress dialog in the middle of the screen
     *
     * @param c Context in which to display the dialog
     * @return returns the instance of the dialog
     */
    public static ProgressDialog showProgressDialog(Context c) {
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setTitle("Please Wait");
        dialog.setMessage("Loading Data...");
        dialog.setCancelable(false);
        dialog.show();

        return dialog;
    }

    /**
     * Shows a generic "Network Error" dialog, prompting the user to connect to the internet
     *
     * @param c Context in which to display the dialog
     * @return returns the instance of the dialog
     */
    public static AlertDialog showNetworkErrorDialog(Context c) {
        AlertDialog alert = new AlertDialog.Builder(c)
                .setTitle("Network Error")
                .setMessage("Please connect to the internet")
                .setPositiveButton("OK", null).create();
        alert.show();
        return alert;
    }

    /**
     * Displays a dialog in the middle of the screen with the given title and message
     *
     * @param s       Title of the dialog
     * @param message Message to be displayed in the body of the dialog
     * @param c       Context in which to display the dialog
     * @return returns the instance of the dialog
     */
    public static AlertDialog showMessageDialog(String s, String message, Context c) {
        AlertDialog alert = new AlertDialog.Builder(c)
                .setTitle(s)
                .setMessage(message)
                .setPositiveButton("OK", null).create();
        alert.show();
        return alert;
    }

    /**
     * Displays a prompt dialog, in which the user can input string into a text field
     *
     * @param title    Title of the dialog
     * @param callback Callback in which to handle the result of the dialog input
     * @param c        Context in which to display the dialog
     * @return returns the instance of the dialog
     */
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

    public static void showMessageSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showMessageSnackbar(View view, String message, int i) {
        Snackbar.make(view, message, i).show();
    }

    public static AlertDialog showChoosePictureDialog(final Activity activity) {
        String[] items = {"Take a Picture", "Choose image from gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select an image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        ServiceManager.captureImage(activity);
                        break;
                    case 1:
                        ServiceManager.chooseImage(activity);
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        return alert;
    }


}
