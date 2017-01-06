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

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class CreateOrganizationActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnCompletedListener, OnAuthenticatedListener {

    private static final int RC_PERMISSION_LOCATION = 1;
    private static final long LOCATION_INTERVAL = 1000;

    public static final String KEY_EDIT_ORGANIZATION = "net.codealizer.fundme.ui.main.CreateOrganizationActivity.KEY_EDIT_ORGANIZATION";
    private static final int RC_PERMISSION_IMAGE = 2000;

    FloatingActionButton chooseImageButton;

    ImageView imageCover;

    AppCompatEditText titleEditText;
    AppCompatEditText descriptionEditText;
    AppCompatEditText priceEditText;
    AppCompatEditText locationEditText;
    AppCompatEditText linkEditText;

    ImageButton findLocationButton;
    Button saveButton;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private boolean findLocationClicked;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;

    private boolean hasImageChanged = false;

    private Organization organization;

    //Location
    boolean found;
    private ProgressDialog dialog;
    private String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.codealizer.fundme.R.layout.activity_create_organization);

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
            case R.id.create_organization_picture_button:
                choosePicture();
                break;
            case R.id.create_organization_picture:
                choosePicture();
                break;
            case R.id.create_organization_select_location:
                startCurrentLocation();
                break;
            case R.id.create_organization_save:
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
        chooseImageButton = (FloatingActionButton) findViewById(R.id.create_organization_picture_button);
        findLocationButton = (ImageButton) findViewById(R.id.create_organization_select_location);
        saveButton = (Button) findViewById(R.id.create_organization_save);

        imageCover = (ImageView) findViewById(R.id.create_organization_picture);

        titleEditText = (AppCompatEditText) findViewById(R.id.create_organization_title);
        descriptionEditText = (AppCompatEditText) findViewById(R.id.create_organization_description);
        priceEditText = (AppCompatEditText) findViewById(R.id.create_organization_price);
        locationEditText = (AppCompatEditText) findViewById(R.id.create_organization_location);
        linkEditText = (AppCompatEditText) findViewById(R.id.create_organization_link);

        chooseImageButton.setOnClickListener(this);
        imageCover.setOnClickListener(this);
        findLocationButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int end) {
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
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        buildGoogleApiClient();
        mGoogleApiClient.connect();

        if (getIntent().hasExtra(KEY_EDIT_ORGANIZATION)) {
            organization = getIntent().getParcelableExtra(KEY_EDIT_ORGANIZATION);

            imageCover.setImageBitmap(organization.getImage());

            titleEditText.setText(organization.getTitle());
            descriptionEditText.setText(organization.getDescription());
            priceEditText.setText(String.valueOf(Math.round(organization.getPrice())));
            linkEditText.setText(organization.getLink());
            locationEditText.setText(String.valueOf(organization.getZipCode()));

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
        String perms[] = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        findLocationClicked = true;
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (mGoogleApiClient.isConnected()) {
                final Location l = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
                if (l != null && l.getTime() < 30_000_000) {
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
            String link = linkEditText.getText().toString();

            dialog = AlertDialogManager.showProgressDialog(this);

            if (getIntent().hasExtra(KEY_EDIT_ORGANIZATION) && organization != null) {
                organization.setImage(image);
                organization.setTitle(title);
                organization.setDescription(description);
                organization.setPrice(price);
                organization.setZipCode(zipCode);
                organization.setDateCreated(dateCreated);
                organization.setLink(link);
                DatabaseManager.createOrganization(organization, this, this, true);
            } else {
                DatabaseManager.createOrganization(new Organization(title, description, price, zipCode, dateCreated, image, link, new ArrayList<String>(), 0, new ArrayList<String>(), 0),
                        this, this, false);

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

        if (locationEditText.getText().toString().isEmpty()) {
            locationEditText.setError("Required field");
            valid = false;
        }

        if (linkEditText.getText().toString().isEmpty()) {
            linkEditText.setError("Required field");
            valid = false;
        }


        return valid;
    }


    @Override
    public void onServiceSuccessful() {
        dialog.hide();
        Toast.makeText(CreateOrganizationActivity.this, "Item Created!", Toast.LENGTH_LONG).show();

        finish();
        CreateOrganizationActivity.super.onBackPressed();
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
