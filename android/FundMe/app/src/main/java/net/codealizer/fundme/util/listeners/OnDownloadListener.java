package net.codealizer.fundme.util.listeners;

/**
 * Created by Pranav on 12/21/16.
 */

public interface OnDownloadListener {
    <D> void onDownloadSuccessful(D data);

    void onDownloadFailed(String message);
}
