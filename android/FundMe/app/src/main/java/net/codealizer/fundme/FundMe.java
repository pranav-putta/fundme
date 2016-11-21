package net.codealizer.fundme;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Pranav on 11/18/16.
 */

public class FundMe extends Application {

    public static final String API_DOMAIN = "10.0.0.226";
    public static final String API_APP_CODE = "1234567890";
    public static final int API_PORT = 9000;

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
