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
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;

/**
 * Created by Pranav on 11/20/16.
 */

public class SignUpEmailPage2Fragment extends Fragment implements View.OnClickListener {

    Button next;

    EditText firstName;
    EditText lastName;
    EditText password;

    OnProgressScreenListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_email_page2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        next = (Button) getView().findViewById(R.id.next);
        firstName = (EditText) getView().findViewById(R.id.signup_first_name);
        lastName = (EditText) getView().findViewById(R.id.signup_last_name);
        password = (EditText) getView().findViewById(R.id.signup_password);


        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    ServiceManager.hideSoftKeyboard(SignUpEmailPage2Fragment.this.getActivity(), password);
                }

                return false;
            }
        });
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String first = firstName.getText().toString();
        String last = lastName.getText().toString();
        String pass = password.getText().toString();

        boolean cancel = false;

        if (first.isEmpty()) {
            cancel = true;
            firstName.setError("Enter a valid name");
        }
        if (last.isEmpty()) {
            cancel = true;
            lastName.setError("Enter a valid name");
        }
        if (pass.isEmpty() || pass.length() < 4) {
            cancel = true;
            password.setError("Your password is too short");
        }

        if (!cancel) {
            Pair<String, String> firstNamePair = new Pair<>("firstName", first);
            Pair<String, String> lastNamePair = new Pair<>("lastName", last);
            Pair<String, String> passwordPair = new Pair<>("password", pass);

            listener.onScreenProgress(2, firstNamePair, lastNamePair, passwordPair);
        }
    }

    public void setListeners(OnProgressScreenListener listener) {
        this.listener = listener;
    }
}
