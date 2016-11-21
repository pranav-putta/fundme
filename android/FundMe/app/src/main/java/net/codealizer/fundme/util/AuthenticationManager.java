package net.codealizer.fundme.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.plus.model.people.Person;
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
import net.codealizer.fundme.ui.login.fragments.SignUpEmailPage1Fragment;
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
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_PROFILE_PIC = "profile_pic";

    // Firebase constants
    private static final String DATABASE_REF = "/server/fundme/users";

    /**
     * Attempts to login to FundMe servers with facebook data
     */
    public static void attemptFacebookLogin(final JSONObject object, String token, Activity activity, final OnAuthenticatedListener listener) {
        if (ServiceManager.isNetworkAvailable(activity)) {
            AuthCredential credential = FacebookAuthProvider.getCredential(token);
            User user = new User("", retrieveFacebookGraphData(object));

            login(credential, user, listener);
        } else {
            listener.onNetworkError();
        }
    }

    /**
     * Attempts to login a user using email and password
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
                    login(email, password, listener);
                }
            });
        } else {
            listener.onNetworkError();
        }
    }

    public static void attemptEmailSignup(final Bundle data, final OnAuthenticatedListener listener) {
        String email = data.getString("email");
        String password = data.getString("password");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(task.getResult().getUser().getUid(), data);
                            user.setUid(task.getResult().getUser().getUid());
                            updateDBData(user, listener);
                        } else {
                            if (task.getException() != null)
                                listener.onAuthenticationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Attempts to login a user using the google authentication system
     *
     * @param act      GoogleSignInAccount including all user credentials
     * @param activity Activity/Context for screen
     * @param listener Authentication listener
     * @param person   Google Api Client, in which to access the person id from
     */
    public static void attemptGoogleLogin(GoogleSignInAccount act, Activity activity, final OnAuthenticatedListener listener, final Person person) {
        if (ServiceManager.isNetworkAvailable(activity)) {
            AuthCredential credential = GoogleAuthProvider.getCredential(act.getIdToken(), act.getIdToken());
            User user = new User("", retrieveGoogleData(act, person));

            login(credential, user, listener);
        } else {
            listener.onNetworkError();
        }
    }

    /**
     * Internal login method for social integration
     *
     * @param authCredential Token credential retrieved from respective social login authentication systems
     * @param user           User profile information, as managed by their social profile
     * @param listener       Authentication listener
     */
    private static void login(AuthCredential authCredential, final User user, final OnAuthenticatedListener listener) {
        FirebaseAuth.getInstance().signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user.setUid(task.getResult().getUser().getUid());
                            updateDBData(user, listener);
                        } else {
                            if (task.getException() != null)
                                listener.onAuthenticationFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Internal login method for login with email and password
     *
     * @param email    Email -- user id -- of user
     * @param password Identifying password for user
     * @param listener Authentication listener
     */
    private static void login(String email, String password, final OnAuthenticatedListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(task.getResult().getUser().getUid());
                            updateDBData(user, listener);
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
            if (object.has(KEY_FIRST_NAME))
                bundle.putString(KEY_FIRST_NAME, object.getString(KEY_FIRST_NAME));
            if (object.has(KEY_LAST_NAME))
                bundle.putString(KEY_LAST_NAME, object.getString(KEY_LAST_NAME));
            if (object.has(KEY_EMAIL))
                bundle.putString(KEY_EMAIL, object.getString(KEY_EMAIL));
            if (object.has(KEY_GENDER))
                bundle.putString(KEY_GENDER, object.getString(KEY_GENDER));
            if (object.has(KEY_BIRTHDAY))
                bundle.putString(KEY_BIRTHDAY, object.getString(KEY_BIRTHDAY));
            if (object.has(KEY_LOCATION))
                bundle.putString(KEY_LOCATION, object.getJSONObject(KEY_LOCATION).getString("name"));

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
     * @param person  Google plus information for user
     * @return a bundle containing all user google profile information
     */
    private static Bundle retrieveGoogleData(GoogleSignInAccount account, Person person) {
        Bundle data = new Bundle();
        data.putString(KEY_PROFILE_PIC, account.getPhotoUrl().toString());
        data.putString(KEY_FIRST_NAME, account.getGivenName());
        data.putString(KEY_LAST_NAME, account.getFamilyName());
        data.putString(KEY_EMAIL, account.getEmail());

        if (person.getGender() == 0) {
            data.putString(KEY_GENDER, "male");
        } else {
            data.putString(KEY_GENDER, "female");
        }

        data.putString(KEY_BIRTHDAY, person.getBirthday());
        data.putString(KEY_LOCATION, person.getCurrentLocation());
        return data;
    }

    /**
     * Updates the user information from the database
     *
     * @param myUser
     * @param listener
     */
    private static void updateDBData(final User myUser, final OnAuthenticatedListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(myUser.getUid())) {
                    dataSnapshot = dataSnapshot.child(myUser.getUid());

                    Bundle data = new Bundle();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        data.putString(d.getKey(), d.getValue().toString());
                    }

                    User u = new User(myUser.getUid(), data);
                    u.setLast_logged_in(String.valueOf(System.currentTimeMillis()));
                    reference.child(myUser.getUid()).setValue(u);

                    listener.onAuthenticationSuccessful(u);

                } else {
                    myUser.setLast_logged_in(String.valueOf(System.currentTimeMillis()));
                    reference.child(myUser.getUid()).setValue(myUser);
                    listener.onAuthenticationSuccessful(myUser);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onAuthenticationFailed(databaseError.getMessage());
            }
        });

    }


    public static void forgotPassword(String response, final Runnable runnable) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(response).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                runnable.run();
            }
        });
    }

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
