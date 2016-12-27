package net.codealizer.fundme;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import net.codealizer.fundme.util.firebase.UserDataManager;

/**
 * Created by Pranav on 11/18/16.
 */

public class FundMe extends Application {

    public static UserDataManager userDataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Facebook SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        openSession();
    }

    public void openSession() {
        userDataManager = new UserDataManager(this);
    }
}
