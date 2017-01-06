package net.codealizer.fundme.ui.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jaredrummler.materialspinner.MaterialSpinner;

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Comment;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import mabbas007.tagsedittext.TagsEditText;
import pub.devrel.easypermissions.EasyPermissions;

public class CreateItemActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnCompletedListener, OnAuthenticatedListener {

    public static final String KEY_EDIT_ITEM = "net.codealizer.fundme.ui.main.CreateItemActivity.KEY_EDIT_ITEM";

    private static final int RC_PERMISSION_LOCATION = 1;
    private static final long LOCATION_INTERVAL = 1000;
    private static final int RC_PERMISSION_IMAGE = 2000;
    FloatingActionButton chooseImageButton;

    ImageView imageCover;

    AppCompatEditText titleEditText;
    AppCompatEditText descriptionEditText;
    AppCompatEditText priceEditText;
    AppCompatEditText locationEditText;
    TagsEditText tagsEditText;
    MaterialSpinner condition;

    ImageButton findLocationButton;
    Button saveButton;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private Item item;

    private boolean findLocationClicked;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;

    private boolean hasImageChanged = false;

    //Location
    int time;
    boolean found;
    private ProgressDialog dialog;

    private String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_create_item_save:
                save();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ServiceManager.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && ServiceManager.uri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), ServiceManager.uri);
                imageCover.setImageBitmap(ServiceManager.ImageHelper.compressImage(bitmap));
                hasImageChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == ServiceManager.REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                imageCover.setImageBitmap(ServiceManager.ImageHelper.compressImage(bitmap));
                hasImageChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_item_picture_button:
                choosePicture();
                break;
            case R.id.create_item_picture:
                choosePicture();
                break;
            case R.id.create_item_select_location:
                startCurrentLocation();
                break;
            case R.id.create_item_save:
                save();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_PERMISSION_LOCATION) {
            if (findLocationClicked) {
                startCurrentLocation();
            }
        } else if (requestCode == RC_PERMISSION_IMAGE) {
            choosePicture();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == RC_PERMISSION_LOCATION) {
            Toast.makeText(this, "Cannot find your location", Toast.LENGTH_LONG).show();
        } else if (requestCode == RC_PERMISSION_IMAGE) {
            Toast.makeText(this, "Couldn't create an image destination", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        selectLocation(location);
        fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
        found = false;
    }

    private void initialize() {
        chooseImageButton = (FloatingActionButton) findViewById(R.id.create_item_picture_button);
        findLocationButton = (ImageButton) findViewById(R.id.create_item_select_location);
        saveButton = (Button) findViewById(R.id.create_item_save);

        imageCover = (ImageView) findViewById(R.id.create_item_picture);

        titleEditText = (AppCompatEditText) findViewById(R.id.create_item_title);
        descriptionEditText = (AppCompatEditText) findViewById(R.id.create_item_description);
        priceEditText = (AppCompatEditText) findViewById(R.id.create_item_price);
        tagsEditText = (TagsEditText) findViewById(R.id.create_item_tags);
        locationEditText = (AppCompatEditText) findViewById(R.id.create_item_location);
        condition = (MaterialSpinner) findViewById(R.id.create_item_condition);

        chooseImageButton.setOnClickListener(this);
        imageCover.setOnClickListener(this);
        findLocationButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        tagsEditText.setTagsWithSpacesEnabled(true);
        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int end) {
                try {
                    if (!s.toString().equals(current)) {
                        priceEditText.removeTextChangedListener(this);

                        String cleanString = s.toString().replaceAll("[$,.]", "");

                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

                        current = formatted;
                        priceEditText.setText(formatted);
                        priceEditText.setSelection(formatted.length());

                        priceEditText.addTextChangedListener(this);
                    }
                } catch (Exception ex) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        condition.setItems("Used", "Acceptable", "Good", "Very good", "Brand New");

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        if (getIntent().hasExtra(KEY_EDIT_ITEM)) {
            item = getIntent().getParcelableExtra(KEY_EDIT_ITEM);

            imageCover.setImageBitmap(item.getImage());

            titleEditText.setText(item.getTitle());
            descriptionEditText.setText(item.getDescription());
            priceEditText.setText(String.valueOf(Math.round(item.getPrice())));
            tagsEditText.setText(ServiceManager.convertArrayToString(item.getTags()).replaceAll("__,__", " "));
            locationEditText.setText(String.valueOf(item.getZipCode()));

            if (item.condition > 0) {
                condition.setSelectedIndex(item.getCondition() - 1);
            } else {
                condition.setSelectedIndex(0);
            }
            hasImageChanged = true;

        }

    }

    private void choosePicture() {
        String perms[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            AlertDialogManager.showChoosePictureDialog(this);
        } else {
            EasyPermissions.requestPermissions(this, "Your images will be used for backdrops", RC_PERMISSION_IMAGE, perms);
        }
    }

    private void selectLocation(Location location) {
        findLocationClicked = false;

        Geocoder geocoder = new Geocoder(this);
        Address address = new Address(Locale.getDefault());

        try {
            List<Address> addr = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addr.size() > 0) {
                address = addr.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationEditText.setText(address.getPostalCode());

    }

    private void startCurrentLocation() {
        int conditi = condition.getSelectedIndex();

        String perms[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        findLocationClicked = true;
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (mGoogleApiClient.isConnected()) {
                final Location l = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
                if (l != null && (System.currentTimeMillis() - l.getTime()) < 1_200_000) {
                    selectLocation(l);
                } else {
                    fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                }
            } else {
                Toast.makeText(this, "Could not connect to google locations", Toast.LENGTH_LONG).show();
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_permission_rationale), RC_PERMISSION_LOCATION, perms);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void save() {
        if (checkRequiredFields()) {
            Bitmap image = (((BitmapDrawable) imageCover.getDrawable()).getBitmap());
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            double price = Double.parseDouble(priceEditText.getText().toString().replace("$", "").replace(",", ""));
            int zipCode = Integer.parseInt(locationEditText.getText().toString());
            long dateCreated = System.currentTimeMillis();
            String rawTags = tagsEditText.getText().toString();
            List<String> tags = Arrays.asList(rawTags.split(" "));
            List<String> loved = new ArrayList<>();
            List<String> itemsBought = new ArrayList<>();
            int viewed = 0;
            int cond = condition.getSelectedIndex() + 1;

            dialog = AlertDialogManager.showProgressDialog(this);

            if (getIntent().hasExtra(KEY_EDIT_ITEM)) {
                item.image = ((BitmapDrawable) imageCover.getDrawable()).getBitmap();
                item.title = titleEditText.getText().toString();
                item.description = descriptionEditText.getText().toString();
                item.price = Double.parseDouble(priceEditText.getText().toString().replace("$", "").replace(",", ""));
                item.zipCode = Integer.parseInt(locationEditText.getText().toString());
                item.tags = Arrays.asList(tagsEditText.getText().toString().split(" "));
                item.condition = condition.getSelectedIndex() + 1;

                DatabaseManager.createItem(item, this, this, true);
            } else {
                DatabaseManager.createItem(new Item(title, description, price, zipCode, dateCreated, image, tags, loved, viewed, itemsBought, false, new ArrayList<Comment>(), cond), this, this, false);

            }
        }
    }

    private boolean checkRequiredFields() {
        boolean valid = true;

        if (!hasImageChanged) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (titleEditText.getText().toString().isEmpty()) {
            titleEditText.setError("Required field");
            valid = false;
        }

        if (descriptionEditText.getText().toString().isEmpty()) {
            descriptionEditText.setError("Required field");
            valid = false;
        }

        if (priceEditText.getText().toString().isEmpty()) {
            priceEditText.setError("Required field");
            valid = false;
        }

        try {
            double price = Double.parseDouble(priceEditText.getText().toString().replace("$", "").replace(",", ""));
            if (price > 1 && price < 1000) {
                throw new Exception();
            }
        } catch (Exception ex) {
            priceEditText.setError("The");
        }

        if (locationEditText.getText().toString().isEmpty()) {
            locationEditText.setError("Required field");
            valid = false;
        }

        if (tagsEditText.getText().toString().isEmpty()) {
            tagsEditText.setError("Enter at least one tag");
            valid = false;
        }

        return valid;
    }


    @Override
    public void onServiceSuccessful() {

        dialog.hide();
        Toast.makeText(CreateItemActivity.this, "Item Created!", Toast.LENGTH_LONG).show();

        finish();
        CreateItemActivity.super.onBackPressed();

    }

    @Override
    public void onServiceFailed() {
        dialog.hide();

        Toast.makeText(this, "Couldn't create the item", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSuccessful(User data) {

    }

    @Override
    public void onAuthenticationFailed(String message) {
        dialog.hide();

        Toast.makeText(this, "Couldn't create the item", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNetworkError() {
        dialog.hide();

        Toast.makeText(this, "Couldn't create the item", Toast.LENGTH_LONG).show();
    }


}
