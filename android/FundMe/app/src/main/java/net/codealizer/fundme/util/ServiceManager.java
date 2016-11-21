package net.codealizer.fundme.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * Created by Pranav on 11/19/16.
 */

public class ServiceManager {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Bundle convertToBundle(Object object) {
        Bundle data = new Bundle();
        Class c = object.getClass();

        for (Field f : c.getFields()) {
            try {
                data.putString(f.getName(), String.valueOf(f.get(object)));
            } catch (IllegalAccessException ignore) {
                data.putString(f.getName(), "");
            }
        }

        return data;
    }

    public static void hideSoftKeyboard(Context c, View view) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
