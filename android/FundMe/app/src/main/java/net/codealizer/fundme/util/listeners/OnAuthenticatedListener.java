package net.codealizer.fundme.util.listeners;

import net.codealizer.fundme.assets.User;

/**
 * Created by Pranav on 11/19/16.
 */

public interface OnAuthenticatedListener {

    void onAuthenticationSuccessful(User data);

    void onAuthenticationFailed(String message);

    void onNetworkError();

}
