package net.codealizer.fundme.ui.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import net.codealizer.fundme.ui.main.AddressActivity;
import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Comment;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.ui.main.ShopActivity;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnAlertCallbackListener;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Pranav on 11/19/16.
 */

public class AlertDialogManager {

    public static final int RC_ADDRESS = 1000;

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

    public static AlertDialog showInsufficientCreditsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Insufficient credits")
                .setMessage("You don't have enough credits to buy this item. Would you like to purchase some?")
                .setPositiveButton("Store", null)
                .setNegativeButton("No thanks", null);

        AlertDialog alert = builder.create();
        alert.show();

        return alert;

    }

    public static AlertDialog showInvalidAddressDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("No Address")
                .setMessage("You do not have an address associated with this account.")
                .setPositiveButton("Enter Address", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, AddressActivity.class);
                        context.startActivityForResult(intent, ViewItemActivity.RC_ADDRESS);
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();

        return alert;

    }

    public static AlertDialog showConfirmAddressDialog(final Activity context, final String address, final Item item, final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Is this the correct address?")
                .setMessage(address)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showBuyItemDialog(context, listener, item);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(context, AddressActivity.class);
                        intent.putExtra("ADDRESS", address);
                        context.startActivityForResult(intent, ViewItemActivity.RC_ADDRESS);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

        return alert;

    }

    public static AlertDialog showBuyItemDialog(Context context, final DialogInterface.OnClickListener listener, final Item item) {
        DecimalFormat df = new DecimalFormat("#.00");
        String price = "$" + df.format(item.getPrice());

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Buy item")
                .setMessage(Html.fromHtml("Would you like to request to buy this item for <strong>" + price + "</strong>?"))
                .setPositiveButton("Yes", listener)
                .setNegativeButton("No", null);

        AlertDialog alert = builder.create();
        alert.show();

        return alert;
    }

    public static void showMessageSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showMessageSnackbar(View view, String message, int i) {
        Snackbar.make(view, message, i).show();
    }

    public static void showChoosePictureDialog(final Activity activity) {
        String[] items = {"Take a Picture", "Choose image from gallery"};
        final Uri[] uri = new Uri[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select an image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        uri[0] = ServiceManager.captureImage(activity);
                        break;
                    case 1:
                        ServiceManager.chooseImage(activity);
                        break;
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }


    public static void showAcceptOrganizationDialog(final Context context, final OnDownloadListener listener) {

        DatabaseManager.getAllOrganizations(new OnDownloadListener() {
            @Override
            public <D> void onDownloadSuccessful(final D data) {
                final ArrayList<String> organizations = new ArrayList<String>();

                for (Organization o : (ArrayList<Organization>) data) {
                    organizations.add(o.getTitle());
                }

                String[] items = organizations.toArray(new String[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                if (organizations.size() > 0) {
                    builder.setTitle("Choose an organizations to donate to");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            listener.onDownloadSuccessful(((ArrayList<Organization>) data).get(i));
                        }
                    });
                } else {
                    builder.setTitle("Join an organization");
                    builder.setMessage("Before you can sell an item, you must join an organization");
                    builder.setPositiveButton("OK", null);
                }

                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public void onDownloadFailed(String message) {
                listener.onDownloadFailed(message);
            }
        });
    }

    public static void showCommentDialog(final Item item, final Context context, final OnDownloadListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.comment_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final EditText commentEditText = (EditText) view.findViewById(R.id.comment_dialog_edit_text);
        builder.setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Comment comment = new Comment(FundMe.userDataManager.getUser().getName(), commentEditText.getText().toString(), FundMe.userDataManager.getUser().getProfilePic(), System.currentTimeMillis());
                DatabaseManager.createComment(item, comment, context, listener);
            }
        })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    public static void showBuyConfirmDialog(String name, String price, final int pos, final Context context, final View container) {
        Spanned text = Html.fromHtml("Are you sure you would like to purchase <strong>" + name + "</strong> for <strong>" + price + "</strong>? You cannot undo this transaction");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        final ProgressDialog progress = showProgressDialog(context);

                        int numberOfCredits = context.getResources().getIntArray(R.array.shop_item_values)[pos];

                        DatabaseManager.purchaseCredits(numberOfCredits, new OnDownloadListener(){

                            @Override
                            public <D> void onDownloadSuccessful(D data) {
                                progress.dismiss();
                                showMessageSnackbar(container, "Your credits were successfully purchased!");
                            }

                            @Override
                            public void onDownloadFailed(String message) {
                                showMessageSnackbar(container, "Your transaction could not be completed :(");
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();
    }
}
