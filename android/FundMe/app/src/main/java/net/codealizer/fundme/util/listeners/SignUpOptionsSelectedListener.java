package net.codealizer.fundme.util.listeners;

import net.codealizer.fundme.util.SignUpOption;

import java.io.Serializable;

/**
 * Created by Pranav on 11/20/16.
 */

public interface SignUpOptionsSelectedListener extends Serializable {

    void onSignUpOptionSelected(SignUpOption option);

}
