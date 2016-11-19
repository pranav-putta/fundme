package net.codealizer.fundme.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import net.codealizer.fundme.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    // All UI Elements
    Button loginWithFacebookButton;
    Button signUpButton;
    Button moreInfoButton;
    Button loginButton;

    // Constants
    private static final String URL_HOME = "http://www.codealizer.net";

    // Facebook Elements
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();
    }

    /**
     * Instantiates all elements of "activity_login.xml"
     * Runs any sequencing program to initialize the screen
     */
    private void initialize() {
        // Initialize Facebook Authentication
        initFacebook();

        // Associate objects with respective elements
        loginWithFacebookButton = (Button) findViewById(R.id.facebook_login_button);
        signUpButton = (Button) findViewById(R.id.login_sign_up);
        moreInfoButton = (Button) findViewById(R.id.login_more_info);
        loginButton = (Button) findViewById(R.id.login_button);


        // Set OnClickListeners for the buttons
        moreInfoButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_more_info:
                openWebHomepage();
                break;
            case R.id.facebook_login_button:

                break;

        }
    }

    private void openWebHomepage() {
        Uri uriUrl = Uri.parse(URL_HOME);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Success", "Login");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Facebook Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions();
    }
}
