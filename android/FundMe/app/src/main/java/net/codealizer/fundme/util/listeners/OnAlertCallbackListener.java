package net.codealizer.fundme.util.listeners;

import java.io.Serializable;

/**
 * Created by Pranav on 11/20/16.
 */

public interface OnAlertCallbackListener extends Serializable {

    void onAlertCallback(String response);

}
