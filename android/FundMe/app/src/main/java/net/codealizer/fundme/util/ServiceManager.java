package net.codealizer.fundme.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Pranav on 11/19/16.
 */

public class ServiceManager {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_PICK_IMAGE = 2;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    public static Uri uri;

    public static String strSeparator = "__,__";

    public static String convertArrayToString(List<String> a) {
        try {
            if (a != null) {
                String array[] = a.toArray(new String[0]);

                String str = "";
                for (int i = 0; i < array.length; i++) {
                    str = str + array[i];
                    // Do not append comma at the end of last element
                    if (i < array.length - 1) {
                        str = str + strSeparator;
                    }
                }
                return str;
            } else {
                return "";
            }
        } catch (Exception ex) {
            return "";
        }
    }

    public static List<String> convertStringToArray(String str) {
        if (!str.equals("")) {
            String[] arr = str.split(strSeparator);
            return Arrays.asList(arr);
        } else {
            return new ArrayList<>();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String dataFromURL (String u) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(u);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static void hideSoftKeyboard(Context c, View view) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static Uri captureImage(Activity activity) {
        File filename = null;
        try {
            filename = createImageFile(activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", filename);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);


        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }



        return uri;
    }

    private static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public static void chooseImage(Activity activity) {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
    }

    public static long distance(Address a1, Address a2) {
        double lat_a = a1.getLatitude();
        double lng_a = a1.getLongitude();
        double lat_b = a2.getLatitude();
        double lng_b = a2.getLongitude();

        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        float meters = new Float(distance * meterConversion).floatValue();
        double miles = meters / 1609.344;

        return Math.round(miles);
    }

    public static String getTimePassed(long time) {
        long timeInMilliSeconds = System.currentTimeMillis() - time;
        long seconds = timeInMilliSeconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days == 0 && hours == 0 && minutes == 0) {
            return seconds + " seconds ago";
        } else if (days == 0 && hours == 0) {
            return minutes + " minutes ago";
        } else if (days == 0) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    public static class ImageHelper {

        public static final int SIZE_LIMIT = 250; //in KB

        public static Bitmap getImageFromResource(int resource, Context context) {
            return BitmapFactory.decodeResource(context.getResources(), resource);
        }

        public static void getBitmapFromUrl(String src, OnDownloadListener li) {
            new DownloadTask(src, li).execute();
        }

        public static Bitmap getBitmapFromUrl(String src) throws IOException, ExecutionException, InterruptedException {
            return new DownloadTask(src, null).execute().get();
        }

        public static String encodeToBase64(Bitmap image) {
            Bitmap immage = image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

            return imageEncoded;
        }

        public static Bitmap decodeToBase64(String input) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }

        public static Bitmap compressImage(Bitmap image) {
            int width = image.getWidth();
            int height = image.getHeight();

            float bitmapRatio = (float) width / (float) height;
            if (bitmapRatio > 0) {
                width = SIZE_LIMIT;
                height = (int) (width / bitmapRatio);
            } else {
                height = SIZE_LIMIT;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(image, width, height, true);
        }

        private static class DownloadTask extends AsyncTask<Void, Void, Bitmap> {

            private String mSource;
            private OnDownloadListener mListener;
            private Bitmap myBitmap;

            public DownloadTask(String source, OnDownloadListener listener) {
                mSource = source;
                mListener = listener;
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    java.net.URL url = new java.net.URL(mSource);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;

            }

            @Override
            protected void onPostExecute(Bitmap aVoid) {
                if (mListener != null) {
                    if (aVoid != null) {
                        mListener.onDownloadSuccessful(myBitmap);
                    } else {
                        mListener.onDownloadFailed("Couldn't download");
                    }
                }
            }
        }

    }

}
