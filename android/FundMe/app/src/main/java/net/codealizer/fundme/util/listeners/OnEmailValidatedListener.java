package net.codealizer.fundme.util.listeners;

/**
 * Created by Pranav on 11/20/16.
 */

public interface OnEmailValidatedListener {

    void onEmailValidated(boolean valid);

    void onEmailValidationFailed(String message);

    void onNetworkError();

}
