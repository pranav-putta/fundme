package net.codealizer.fundme.ui.launch;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import net.codealizer.fundme.MainActivity;
import net.codealizer.fundme.ui.login.LoginActivity;
import net.codealizer.fundme.R;
import net.codealizer.fundme.util.UserSessionManager;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UserSessionManager sessionManager = new UserSessionManager(LaunchActivity.this);
                if (sessionManager.isUserLoggedIn()) {
                    Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        }, 1000);


    }
}
