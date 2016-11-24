package net.codealizer.fundme.util.listeners;

import android.util.Pair;

import java.io.Serializable;

/**
 * Created by Pranav on 11/20/16.
 */

public interface OnProgressScreenListener {

    void onScreenProgress(int screen, Pair<String, String>... data);

}
