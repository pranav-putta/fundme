package net.codealizer.fundme.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import net.codealizer.fundme.MainActivity;
import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.AuthenticationManager;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.util.UserSessionManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnAuthenticatedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9000;
    // All UI Elements
    Button loginWithFacebookButton;
    Button loginWithGoogleButton;
    Button signUpButton;
    Button moreInfoButton;
    Button loginButton;

    ProgressDialog progressDialog;

    // Constants
    private static final String URL_HOME = "http://www.codealizer.net";

    // Facebook Elements
    private CallbackManager mCallbackManager;

    // Google Elements
    private GoogleSignInOptions gso;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_more_info:
                openWebHomepage();
                break;
            case R.id.facebook_login_button:
                loginWithFacebook();
                break;
            case R.id.login_button:
                startLoginActivity();
                break;
            case R.id.login_sign_up:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                break;
            case R.id.google_login_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupActionBar();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            loginWithGoogle(data);
        } else {
            mCallbackManager.onActivityResult(requestCode,
                    resultCode, data);
        }
    }

    /**
     * Instantiates all elements of "activity_login.xml"
     * Runs any sequencing program to initialize the screen
     */
    private void initialize() {
        // Initialize Facebook Authentication
        initFacebook();

        // Initialize Google Authentication
        initGoogle();

        // Associate objects with respective elements
        loginWithFacebookButton = (Button) findViewById(R.id.facebook_login_button);
        loginWithGoogleButton = (Button) findViewById(R.id.google_login_button);
        signUpButton = (Button) findViewById(R.id.login_sign_up);
        moreInfoButton = (Button) findViewById(R.id.login_more_info);
        loginButton = (Button) findViewById(R.id.login_button);

        // Set OnClickListeners for the buttons
        moreInfoButton.setOnClickListener(this);
        loginWithFacebookButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        loginWithGoogleButton.setOnClickListener(this);

        setupActionBar();
    }

    private void setupActionBar() {
        // Setup actionbar
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Opens http://www.codealizer.net in a new webpage
     */
    private void openWebHomepage() {
        Uri uriUrl = Uri.parse(URL_HOME);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    /**
     * Initializes all facebook functions
     */
     private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d("LoginActivity", "Login Success");
                final ProgressDialog dialog = AlertDialogManager.showProgressDialog(LoginActivity.this);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        dialog.dismiss();

                        //Get Facebook data from login
                        String token = loginResult.getAccessToken().getToken();
                        AuthenticationManager.attemptFacebookLogin(object, token, LoginActivity.this, LoginActivity.this);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, gender, birthday, location");
                request.setParameters(parameters);
                request.executeAsync();
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

    /**
     * Initializes all google login functions
     */
    private void initGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();
    }

    /**
     * Attempts to login with facebook on login button click
     */
    private void loginWithFacebook() {
        progressDialog = AlertDialogManager.showProgressDialog(this);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    /**
     * Attempts to login to firebase database with the google authentication credential
     *
     * @param data Data retrieved by the google sign in attempt
     */
    private void loginWithGoogle(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);

            progressDialog = AlertDialogManager.showProgressDialog(this);
            AuthenticationManager.attemptGoogleLogin(acct, this, this, person);
        }
    }

    /**
     * Runs the login activity
     */
    private void startLoginActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    /**
     * Function in response to authentication method being successful
     *
     * @param data Data for user retrieved by the firebase database
     */
    @Override
    public void onAuthenticationSuccessful(User data) {
        Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show();

        if (progressDialog != null)
            progressDialog.dismiss();

        UserSessionManager manager = new UserSessionManager(this);
        manager.login(data);

        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Function in response to a failed authentication method
     *
     * @param message Message retrieved by firebase
     */
    @Override
    public void onAuthenticationFailed(String message) {
        if (progressDialog != null)
            progressDialog.dismiss();
        AlertDialogManager.showMessageDialog("Something went wrong", message, this);
    }

    /**
     * Function in response to the application being disconnected from the internet
     */
    @Override
    public void onNetworkError() {
        if (progressDialog != null)
            progressDialog.dismiss();
        AlertDialogManager.showNetworkErrorDialog(this);
    }

    /**
     * Function in response to the application not being able to connect to the google sign in attempt
     *
     * @param connectionResult The response from google
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (progressDialog != null)
            progressDialog.dismiss();
        AlertDialogManager.showMessageDialog("Something went wrong", connectionResult.getErrorMessage(), this);
    }
}
