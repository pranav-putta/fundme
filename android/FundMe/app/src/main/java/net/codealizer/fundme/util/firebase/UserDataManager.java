package net.codealizer.fundme.util.firebase;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import net.codealizer.fundme.assets.DatabaseUser;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.launch.LaunchActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Pranav on 11/18/16.
 */

public class UserDataManager {
    //Shared Preferences
    SharedPreferences preferences;

    //Editor for Shared Preferences
    SharedPreferences.Editor editor;

    //Context
    Context context;

    private User mUser;

    //SharedPreferences File Name
    private static final String PREF_NAME = "USER_LOGIN_MANAGER";

    // SHaredPreferences Mode
    private static final int PRIVATE_MODE = 0;

    //Preference Keys1
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public UserDataManager(Context context) {
        this.context = context;
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    /**
     * Clear Session and logout user
     */
    public void logout() {
        //Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        for (String key : preferences.getAll().keySet()) {
            editor.remove(key);
        }

        editor.commit();

        FirebaseAuth.getInstance().signOut();

        // After logout, restart the application
        Intent i = new Intent(context, LaunchActivity.class);

        // Closing all the activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new flag to start new activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Start Launch Activity
        context.startActivity(i);
    }


    /**
     * Quick check for if user is logged in
     */
    public boolean isUserLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false) && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Save all user data
     */
    public void startLogin(User user) {

        editor.putString("uid", user.uid);
        editor.putString("firstName", user.firstName);
        editor.putString("lastName", user.lastName);
        editor.putString("email", user.email);
        editor.putString("profilePic", user.profilePic);
        editor.putString("lastLoggedIn", user.lastLoggedIn);
        editor.putString("moneyRaised", String.valueOf(user.moneyRaised));
        editor.putString("rating", String.valueOf(user.rating));
        editor.putStringSet("organizationUids", new HashSet<String>(new DatabaseUser(user).organizationUids));
        editor.putStringSet("itemUids", new HashSet<String>(new DatabaseUser(user).itemUids));
        // Input the profile picture
        editor.putString("bitmap_profile_picture", (user.getProfilePicture()));
        editor.putStringSet("joinedOrganizations", new HashSet<String>(user.joinedOrganizations));

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        ArrayList<String> lol = new ArrayList<String>(preferences.getStringSet("itemUids", null));

        mUser = user;
    }

    /**
     * Resume a login state
     */
    public void resumeLogin(User user) {
        String encodedProfilePicture = preferences.getString("bitmap_profile_picture", "");

        if (!encodedProfilePicture.isEmpty()) {
            user.setProfilePicture(encodedProfilePicture);
        }
        startLogin(user);
    }

    public void resumeLogin() {
        User user = new User("");

        user.setUid(preferences.getString("uid", ""));
        user.setFirstName(preferences.getString("firstName", ""));
        user.setLastName(preferences.getString("lastName", ""));
        user.setEmail(preferences.getString("email", ""));
        user.setProfilePic(preferences.getString("profilePic", ""));
        user.setLastLoggedIn(preferences.getString("lastLoggedIn", ""));
        user.setProfilePicture((preferences.getString("bitmap_profile_picture", "")));
        user.setMoneyRaised(Double.parseDouble(preferences.getString("moneyRaised", "0")));
        user.setRating(Double.parseDouble(preferences.getString("rating", "0")));
        if (preferences.getStringSet("organizationUids", null) != null) {
            user.setOrganizationUids(new ArrayList<>(preferences.getStringSet("organizationUids", null)));
        }
        if (preferences.getStringSet("itemUids", null) != null) {
            user.setItemUids(new ArrayList<String>(preferences.getStringSet("itemUids", null)));
        }
        if (preferences.getStringSet("joinedOrganizations", null) != null) {
            user.setJoinedOrganizations(new ArrayList<String>(preferences.getStringSet("joinedOrganizations", null)));
        }

        mUser = user;
    }

    public User getUser() {
        return mUser;
    }

    public void updateUser(User user) {
        mUser = user;
        resumeLogin(user);
    }

}
