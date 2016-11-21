package net.codealizer.fundme.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
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

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.components.NonSwipeableViewPager;
import net.codealizer.fundme.ui.login.fragments.SignUpEmailPage1Fragment;
import net.codealizer.fundme.ui.login.fragments.SignUpEmailPage2Fragment;
import net.codealizer.fundme.ui.login.fragments.SignUpEmailPage3Fragment;
import net.codealizer.fundme.ui.login.fragments.WelcomeFragment;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.AuthenticationManager;
import net.codealizer.fundme.util.SignUpOption;
import net.codealizer.fundme.util.UserSessionManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;
import net.codealizer.fundme.util.listeners.SignUpOptionsSelectedListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity implements SignUpOptionsSelectedListener, Serializable, OnAuthenticatedListener, OnProgressScreenListener, GoogleApiClient.OnConnectionFailedListener {

    // Number of pages in the slide wizard
    private static final int NUM_PAGES = 5;
    private static final int RC_SIGN_IN = 9000;

    // UI Elements
    private NonSwipeableViewPager mViewPager;

    // Pager Adapter
    private PagerAdapter mPagerAdapter;

    // Bundle data
    Bundle data = new Bundle();

    // Facebook Elements
    private CallbackManager mCallbackManager;

    // Google Elements
    private GoogleSignInOptions gso;
    private GoogleApiClient googleApiClient;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.signup_toolbar);
        setSupportActionBar(toolbar);

        initialize();
    }

    /**
     * Initialize all UI Elements
     */
    private void initialize() {
        // Initialize UI Elements
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.signup_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), this);

        mViewPager.setAdapter(mPagerAdapter);

        initGoogle();
        initFacebook();

        // Setup actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        // Setup actionbar
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        int item = mViewPager.getCurrentItem();

        if (item == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(item - 1);
        }
    }

    @Override
    public void onSignUpOptionSelected(SignUpOption option) {
        switch (option) {
            case EMAIL:
                mViewPager.setCurrentItem(1);
                break;
            case FACEBOOK:
                loginWithFacebook();
                break;
            case GOOGLE:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
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

    @Override
    public void onScreenProgress(Pair<String, String>... data) {
        for (Pair<String, String> d : data) {
            SignUpActivity.this.data.putString(d.first, d.second);
        }

        if (mViewPager.getCurrentItem() < 3) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        } else {
            progressDialog = AlertDialogManager.showProgressDialog(SignUpActivity.this);
            AuthenticationManager.attemptEmailSignup(SignUpActivity.this.data, SignUpActivity.this);
        }
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
                final ProgressDialog dialog = AlertDialogManager.showProgressDialog(SignUpActivity.this);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        dialog.dismiss();

                        //Get Facebook data from login
                        String token = loginResult.getAccessToken().getToken();
                        AuthenticationManager.attemptFacebookLogin(object, token, SignUpActivity.this, SignUpActivity.this);
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
                Toast.makeText(SignUpActivity.this, "Facebook Error", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (progressDialog != null)
            progressDialog.dismiss();
        AlertDialogManager.showMessageDialog("Something went wrong", connectionResult.getErrorMessage(), this);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        OnProgressScreenListener listener;

        ScreenSlidePagerAdapter(FragmentManager fm, OnProgressScreenListener listener) {
            super(fm);

            this.listener = listener;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putSerializable(WelcomeFragment.KEY_SIGN_UP_LISTENER, listener);

            if (position == 0) {
                WelcomeFragment fragment = new WelcomeFragment();
                fragment.setArguments(args);
                return fragment;
            } else if (position == 1) {
                SignUpEmailPage1Fragment fragment = new SignUpEmailPage1Fragment();
                fragment.setArguments(args);
                return fragment;
            } else if (position == 2) {
                SignUpEmailPage2Fragment fragment = new SignUpEmailPage2Fragment();
                fragment.setArguments(args);
                return fragment;
            } else if (position == 3) {
                SignUpEmailPage3Fragment fragment = new SignUpEmailPage3Fragment();
                fragment.setArguments(args);
                return fragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
