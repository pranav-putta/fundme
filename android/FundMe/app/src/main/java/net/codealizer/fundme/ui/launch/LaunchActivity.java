package net.codealizer.fundme.ui.launch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.MainActivity;
import net.codealizer.fundme.ui.login.LoginActivity;
import net.codealizer.fundme.R;
import net.codealizer.fundme.util.firebase.AuthenticationManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;

public class LaunchActivity extends AppCompatActivity implements OnAuthenticatedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FundMe.userDataManager.isUserLoggedIn()) {
            initUser();
        } else {
            Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
            startActivity(intent);
        }


    }

    private void initUser() {
        if (ServiceManager.isNetworkAvailable(this)) {
            if (FundMe.userDataManager.isUserLoggedIn()) {
                AuthenticationManager.refreshLogin(FirebaseAuth.getInstance().getCurrentUser().getUid(), this, this);
            } else {
                FundMe.userDataManager.logout();
            }
        } else {
            onNetworkError();
        }
    }


    /**
     * Callback when the authentication of the user was successful
     *
     * @param data User data to save into local sql database
     */
    @Override
    public void onAuthenticationSuccessful(User data) {
        FundMe.userDataManager.resumeLogin(data);

        DatabaseManager.saveItemsAndOrganizations(this, new OnCompletedListener() {
            @Override
            public void onServiceSuccessful() {
                finish();

                Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                intent.putExtra("refreshed", true);
                startActivity(intent);
            }

            @Override
            public void onServiceFailed() {
                onAuthenticationFailed("An error occurred in retrieving your user data");
            }
        });


    }

    /**
     * Callback when the authentication of the user was unsuccessful
     *
     * @param message Message to display to the user abotu the error
     */
    @Override
    public void onAuthenticationFailed(String message) {
        FundMe.userDataManager.resumeLogin();

        finish();

        Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
        intent.putExtra("refreshed", false);
        startActivity(intent);
    }

    /**
     * Callback when the application was not able to retrieve information
     */
    @Override
    public void onNetworkError() {
        FundMe.userDataManager.resumeLogin();

        finish();

        Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
        intent.putExtra("refreshed", false);
        startActivity(intent);
    }
}
