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
import android.widget.Spinner;
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;

/**
 * Created by Pranav on 11/20/16.
 */

public class SignUpEmailPage3Fragment extends Fragment implements View.OnClickListener {

    Button next;

    EditText birthday;
    Spinner gender;

    OnProgressScreenListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_email_page3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        next = (Button) getView().findViewById(R.id.next);
        birthday = (EditText) getView().findViewById(R.id.signup_birthday);
        gender = (Spinner) getView().findViewById(R.id.signup_gender);
        listener = (OnProgressScreenListener) getArguments().getSerializable(WelcomeFragment.KEY_SIGN_UP_LISTENER);

        birthday.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    ServiceManager.hideSoftKeyboard(SignUpEmailPage3Fragment.this.getActivity(), birthday);
                }

                return false;
            }
        });
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String bday = birthday.getText().toString();
        String sex = gender.getSelectedItem().toString().toLowerCase();

        if (!bday.isEmpty()) {
            Pair<String, String> a = new Pair<>("birthday", bday);
            Pair<String, String> b = new Pair<>("gender", sex);

            listener.onScreenProgress(a, b);
        }
    }
}
