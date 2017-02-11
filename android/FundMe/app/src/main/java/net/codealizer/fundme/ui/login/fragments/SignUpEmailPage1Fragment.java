package net.codealizer.fundme.ui.login.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.firebase.AuthenticationManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnEmailValidatedListener;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;

/**
 * Created by Pranav on 11/20/16.
 */

public class SignUpEmailPage1Fragment extends Fragment implements View.OnClickListener, OnEmailValidatedListener {

    Button next;
    EditText email;

    OnProgressScreenListener listener;

    ProgressBar progress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_email_page1, container, false);
    }

    /**
     * Initialize the fragment
     */
    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    /**
     * Set up user interface, and log all such details
     */
    private void initialize() {
        next = (Button) getView().findViewById(R.id.next);
        email = (EditText) getView().findViewById(R.id.signup_email);
        progress = (ProgressBar) getView().findViewById(R.id.progress);

        // Hide keyboard when "done" button is clicked
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    ServiceManager.hideSoftKeyboard(SignUpEmailPage1Fragment.this.getActivity(), email);
                }

                return false;
            }
        });

        next.setOnClickListener(this);
    }

    /**
     * Start authentication process
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (isEmailValid(email.getText().toString())) {
            showProgress(true);
            AuthenticationManager.isEmailValid(email.getText().toString(), getActivity(), this);

        } else {
            email.setError("Enter a valid email address");
        }
    }

    /**
     * Check if email value is valid
     * @param email String representation of the email
     * @return true if the email has a valid format
     */
    private boolean isEmailValid(String email) {
        return email.contains("@") && !email.isEmpty();
    }

    /**
     * Called when the server has authenticated the email, and stated that is a valid value
     * @param valid represents validity of the item
     */
    @Override
    public void onEmailValidated(boolean valid) {
        showProgress(false);

        if (valid) {
            Pair<String, String> pair = new Pair<>("email", email.getText().toString());
            listener.onScreenProgress(1, pair);
        } else {
            Toast.makeText(getActivity(), "Email is being used", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the server has found that the email is already in use
     * @param message represents the message of the return
     */
    @Override
    public void onEmailValidationFailed(String message) {
        showProgress(false);
        Toast.makeText(getActivity(), "Something went wrong :(", Toast.LENGTH_LONG).show();
    }

    /**
     * Called if there is a network problem in the process
     */
    @Override
    public void onNetworkError() {
        showProgress(false);
        AlertDialogManager.showNetworkErrorDialog(getActivity());
    }

    /**
     * Shows the progress dialog
     * @param show true if dialog should be shown, false if it is to be hid
     */
    private void showProgress(boolean show) {
        if (show) {
            next.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        } else {
            next.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets listeners
     * @param listeners OnProgressScreenListener
     */
    public void setListeners(OnProgressScreenListener listeners) {
        this.listener = listeners;
    }
}
