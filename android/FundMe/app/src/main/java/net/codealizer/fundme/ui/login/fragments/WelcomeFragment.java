package net.codealizer.fundme.ui.login.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.codealizer.fundme.R;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.SignUpOption;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;
import net.codealizer.fundme.util.listeners.SignUpOptionsSelectedListener;

/**
 * Created by Pranav on 11/20/16.
 */

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private Button signUpWithGoogleButton;
    private Button signUpWithFacebookButton;
    private Button signUpWithEmailButton;

    private OnProgressScreenListener progressScreenListener;

    public static final String KEY_SIGN_UP_LISTENER = "net.codealizer.fundme.ui.login.fragments.WelcomeFragment.KEY_SIGN_UP_LISTENER";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_welcome, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        // Get Fragment arguments
        progressScreenListener = (OnProgressScreenListener) getArguments().getSerializable(KEY_SIGN_UP_LISTENER);

        //Initialize UI Elements
        signUpWithEmailButton = (Button) getView().findViewById(R.id.email_signup_button);
        signUpWithFacebookButton = (Button) getView().findViewById(R.id.facebook_signup_button);
        signUpWithGoogleButton = (Button) getView().findViewById(R.id.google_signup_button);

        // Initialize On click listeners
        signUpWithEmailButton.setOnClickListener(this);
        signUpWithFacebookButton.setOnClickListener(this);
        signUpWithGoogleButton.setOnClickListener(this);

        ServiceManager.hideSoftKeyboard(getActivity(), getView());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_signup_button:
                progressScreenListener.onScreenProgress();
                break;
            case R.id.facebook_signup_button:
                break;
            case R.id.google_signup_button:
                break;
        }
    }
}
