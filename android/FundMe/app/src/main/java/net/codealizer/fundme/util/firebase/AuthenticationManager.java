package net.codealizer.fundme.util.firebase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnEmailValidatedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pranav on 11/19/16.
 */

public class AuthenticationManager {

    // Log Tag
    private static final String TAG = "AuthenticationManager";

    // User keys
    private static final String KEY_FB_ID = "idFacebook";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PIC = "profilePictureURL";

    // Firebase constants

    /**
     * Resumes an already intiated login session
     *
     * @param listener Callback listener on completed authentication
     */
    public static void refreshLogin(String uid, final OnAuthenticatedListener listener, Context context) {
        User base = new User(uid);
        DatabaseManager.downloadUserDBData(base, listener, true, context);
    }

    /**
     * Attempts to startLogin to FundMe servers with facebook data
     */
    public static void attemptFacebookLogin(final JSONObject object, String token, Activity activity, final OnAuthenticatedListener listener) {
        if (ServiceManager.isNetworkAvailable(activity)) {
            AuthCredential credential = FacebookAuthProvider.getCredential(token);
            User user = new User("", retrieveFacebookGraphData(object));

            login(credential, user, listener, activity);
        } else {
            listener.onNetworkError();
        }
    }

    /**
     * Attempts to startLogin a user using email and password
     *
     * @param email    Email -- user id -- of user
     * @param password Identifying password of user
     * @param activity Activity/context for screen
     * @param listener Authentication listener
     */
    public static void attemptEmailLogin(final String email, final String password, final Activity activity, final OnAuthenticatedListener listener) {
        boolean isNetworkAvailable = ServiceManager.isNetworkAvailable(activity);

        if (isNetworkAvailable) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    login(email, password, listener, activity);
                }
            });
        } else {
            listener.onNetworkError();
        }
    }

    /**
     * Attempts to create an account for a user using email and password
     *
     * @param data     user data information
     * @param listener Authentication listener
     */
    public static void attemptEmailSignup(final Bundle data, final OnAuthenticatedListener listener, final Context context) {
        String email = data.getString("email");
        String password = data.getString("password");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(task.getResult().getUser().getUid(), data);
                            user.setUid(task.getResult().getUser().getUid());
                            DatabaseManager.downloadUserDBData(user, listener, false, context);
                        } else {
                            if (task.getException() != null)
                                listener.onAuthenticationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Attempts to startLogin a user using the google authentication system
     *
     * @param act      GoogleSignInAccount including all user credentials
     * @param activity Activity/Context for screen
     * @param listener Authentication listener
     */
    public static void attemptGoogleLogin(GoogleSignInAccount act, Activity activity, final OnAuthenticatedListener listener) {
        if (ServiceManager.isNetworkAvailable(activity)) {
            AuthCredential credential = GoogleAuthProvider.getCredential(act.getIdToken(), act.getIdToken());
            User user = new User("", retrieveGoogleData(act));

            login(credential, user, listener, activity);
        } else {
            listener.onNetworkError();
        }
    }

    /**
     * Internal startLogin method for social integration
     *
     * @param authCredential Token credential retrieved from respective social startLogin authentication systems
     * @param user           User profile information, as managed by their social profile
     * @param listener       Authentication listener
     */
    private static void login(AuthCredential authCredential, final User user, final OnAuthenticatedListener listener, final Context context) {
        FirebaseAuth.getInstance().signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user.setUid(task.getResult().getUser().getUid());
                            DatabaseManager.downloadUserDBData(user, listener, false, context);
                        } else {
                            if (task.getException() != null)
                                listener.onAuthenticationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Internal startLogin method for startLogin with email and password
     *
     * @param email    Email -- user id -- of user
     * @param password Identifying password for user
     * @param listener Authentication listener
     */
    private static void login(String email, String password, final OnAuthenticatedListener listener, final Context context) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(task.getResult().getUser().getUid());
                            DatabaseManager.downloadUserDBData(user, listener, false, context);
                        } else {
                            listener.onAuthenticationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Convert facebook graph json data into readable data
     *
     * @param object JSONObject response
     * @return A bundle containing all values from facebook graph data
     */
    private static Bundle retrieveFacebookGraphData(JSONObject object) {
        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                Log.i(KEY_PROFILE_PIC, profile_pic + "");
                bundle.putString(KEY_PROFILE_PIC, profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString(KEY_FB_ID, id);
            if (object.has("first_name"))
                bundle.putString(KEY_FIRST_NAME, object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString(KEY_LAST_NAME, object.getString("last_name"));
            if (object.has(KEY_EMAIL))
                bundle.putString(KEY_EMAIL, object.getString(KEY_EMAIL));


            return bundle;
        } catch (JSONException e) {
            Log.d(TAG, "Error parsing JSON");
            return null;
        }
    }

    /**
     * Retrieves all google profile information for a user, and stores into a bundle
     *
     * @param account Account information for user
     * @return a bundle containing all user google profile information
     */
    private static Bundle retrieveGoogleData(GoogleSignInAccount account) {
        Bundle data = new Bundle();
        data.putString(KEY_PROFILE_PIC, account.getPhotoUrl().toString());
        data.putString(KEY_FIRST_NAME, account.getGivenName());
        data.putString(KEY_LAST_NAME, account.getFamilyName());
        data.putString(KEY_EMAIL, account.getEmail());

        return data;
    }


    /**
     * Sends a password reset request to firebase
     *
     * @param response email provided by the user
     * @param runnable callback action
     */
    public static void forgotPassword(String response, final Runnable runnable) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(response).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runnable.run();
            }
        });
    }

    /**
     * checks if email has been used by another user in the database
     *
     * @param email    email provided by the user
     * @param context  context of activity
     * @param listener callback action
     */
    public static void isEmailValid(String email, Context context, final OnEmailValidatedListener listener) {
        if (ServiceManager.isNetworkAvailable(context)) {
            FirebaseAuth.getInstance().fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().getProviders().size() == 0) {
                            listener.onEmailValidated(true);
                        } else {
                            listener.onEmailValidated(false);
                        }
                    } else {
                        listener.onEmailValidationFailed(task.getException().getMessage());
                    }
                }
            });
        } else {
            listener.onNetworkError();
        }
    }
}
