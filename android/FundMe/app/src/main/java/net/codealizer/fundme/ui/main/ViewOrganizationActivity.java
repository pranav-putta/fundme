package net.codealizer.fundme.ui.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.MembersActivity;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.ui.main.CreateOrganizationActivity;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewOrganizationActivity extends AppCompatActivity implements OnDownloadListener, OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks, View.OnClickListener {

    public static final String KEY_ORGANIZATION_UID = "net.codealizer.fundme.main.ViewOrganizationActivity.ORGANIZATION_UID";
    public static final String KEY_ORGANIZATION = "net.codealizer.fundme.main.ViewOrganizationActivity.ORGANIZATION";

    private static final int RC_PERMISSION_LOCATION = 1;


    private static final long LOCATION_INTERVAL = 1000;
    private CoordinatorLayout container;

    private CollapsingToolbarLayout toolbar;
    private ImageView toolbarBackdrop;
    private Toolbar pToolbar;
    private TextView description;
    private TextView created;
    private TextView location;
    private TextView distance;
    private TextView goalText;
    private TextView loved;
    private TextView viewed;
    private boolean isLiked;
    private boolean joined;
    private RoundCornerProgressBar goal;

    private NestedScrollView content;
    private NestedScrollView unable;

    private LinearLayout buttons;
    private Button joinGroupButton;
    private Button membersButton;
    private FloatingActionButton editButton;

    private MapFragment mapFragment;

    private ProgressDialog progressDialog;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private Menu menu;

    private boolean findLocationClicked;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;

    private String uid;
    private Address address;
    private Address current;
    private Organization mOrganization;

    private int reloaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_organization);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();

        // Force a reload of information
        if (getIntent().hasExtra(KEY_ORGANIZATION_UID)) {
            uid = getIntent().getStringExtra(KEY_ORGANIZATION_UID);
        } else if (getIntent().hasExtra(KEY_ORGANIZATION)) {
            mOrganization = getIntent().getParcelableExtra(KEY_ORGANIZATION);
        }

        if (reloaded > 0) {
            if (mOrganization != null) {
                uid = mOrganization.getUid();
                mOrganization = null;
            }
        }

        initializeUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_item, menu);

        this.menu = menu;


        return true;
    }

    @Override
    public <D> void onDownloadSuccessful(D data) {
        if (data instanceof Organization) {
            AlertDialogManager.showMessageSnackbar(container, "organization refreshed!");

            mOrganization = (Organization) data;
            startCurrentLocation();
        } else {
            onDownloadFailed("Organization was invalid");
            progressDialog.hide();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem organization) {
        switch (organization.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.view_item_like:
                like();
                break;
        }

        return true;
    }

    private void like() {
        if (isLiked) {
            isLiked = false;
            menu.getItem(0).setIcon(R.drawable.ic_like_white);
            mOrganization = DatabaseManager.likeOrganization(mOrganization, null, this, false);
        } else {
            isLiked = true;
            menu.getItem(0).setIcon(R.drawable.ic_like_filled);
            DatabaseManager.likeOrganization(mOrganization, null, this, true);
        }
        loved.setText(String.valueOf(mOrganization.getLoved().size()));

    }

    private void join() {
        if (joined) {
            joined = false;
            joinGroupButton.setText("Join");
            mOrganization = DatabaseManager.joinOrganization(mOrganization, this, false);
        } else {
            joined = true;
            joinGroupButton.setText("Leave");
            mOrganization = DatabaseManager.joinOrganization(mOrganization, this, true);
        }
    }

    @Override
    public void onDownloadFailed(String message) {
        progressDialog.hide();
        AlertDialogManager.showMessageSnackbar(container, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (address != null) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(address.getLatitude(), address.getLongitude()))
                    .title("Organization location"));

            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(address.getLatitude(), address.getLongitude()));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            map.moveCamera(center);
            map.animateCamera(zoom);
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        selectLocation(location);
        fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
        progressDialog.hide();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mOrganization == null) {
            retrieveOrganizationInformation();
        } else {
            onDownloadSuccessful(mOrganization);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mOrganization == null) {
            retrieveOrganizationInformation();
            AlertDialogManager.showMessageSnackbar(container, "Couldn't connect to google maps");
        } else {
            onDownloadSuccessful(mOrganization);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mOrganization == null) {
            retrieveOrganizationInformation();
            AlertDialogManager.showMessageSnackbar(container, "Couldn't connect to google maps");
        } else {
            onDownloadSuccessful(mOrganization);
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
        if (findLocationClicked) {
            startCurrentLocation();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        AlertDialogManager.showMessageSnackbar(container, "Couldn't find your location");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_organization_edit:
                Intent intent = new Intent(this, CreateOrganizationActivity.class);
                intent.putExtra(CreateOrganizationActivity.KEY_EDIT_ORGANIZATION, mOrganization);

                this.startActivity(intent);
                break;
            case R.id.view_organization_join_group_button:
                join();
                break;
            case R.id.view_organization_members_button:
                members();
                break;
        }
    }

    private void members() {
        Intent intent = new Intent(this, MembersActivity.class);
        intent.putExtra(MembersActivity.KEY_ORGANIZATION_UID, mOrganization.getUid());
        startActivity(intent);
    }

    private void retrieveOrganizationInformation() {
        if (uid != null) {
            if (ServiceManager.isNetworkAvailable(this)) {
                DatabaseManager.getOrganization(uid, this);
            } else {
                onDownloadFailed("No internet connection!");
            }
        }
    }

    private void initializeUI() {
        container = (CoordinatorLayout) findViewById(R.id.view_organization_container);
        toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_view_organization_container);
        pToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarBackdrop = (ImageView) findViewById(R.id.toolbar_view_organization_backdrop);
        description = (TextView) findViewById(R.id.view_organization_description);
        created = (TextView) findViewById(R.id.view_organization_created);
        location = (TextView) findViewById(R.id.view_organization_location);
        distance = (TextView) findViewById(R.id.view_organization_distance);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.view_organization_map);
        buttons = (LinearLayout) findViewById(R.id.view_organization_action_button_container);
        editButton = (FloatingActionButton) findViewById(R.id.view_organization_edit);
        unable = (NestedScrollView) findViewById(R.id.view_organization_unable);
        content = (NestedScrollView) findViewById(R.id.view_organization_content);
        goal = (RoundCornerProgressBar) findViewById(R.id.view_organization_goal_progress);
        goalText = (TextView) findViewById(R.id.view_organization_goal_string);
        loved = (TextView) findViewById(R.id.liked_count);
        viewed = (TextView) findViewById(R.id.viewed_count);
        joinGroupButton = (Button) findViewById(R.id.view_organization_join_group_button);
        membersButton = (Button) findViewById(R.id.view_organization_members_button);

        setSupportActionBar(pToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = AlertDialogManager.showProgressDialog(this);

        editButton.setOnClickListener(this);
        joinGroupButton.setOnClickListener(this);
        membersButton.setOnClickListener(this);

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        unable.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);


        reloaded += 1;
    }

    private void initializeData() {
        mOrganization = DatabaseManager.addViewedOrganization(mOrganization, this);

        String p = "$" + Math.round(mOrganization.getPrice());
        String c = "Created " + ServiceManager.getTimePassed(mOrganization.getDateCreated());
        String zip = mOrganization.getZipCode() + "";
        String loc;
        String d = "";

        try {
            address = new Geocoder(this).getFromLocationName(zip, 1).get(0);
            loc = address.getLocality() + ", " + getShortState(address.getAdminArea()) + " " + zip + ", " + address.getCountryCode();
        } catch (IOException e) {
            loc = "Couldn't find location";
            e.printStackTrace();
        }

        if (address != null && current != null) {
            d = String.valueOf(ServiceManager.distance(address, current)) + " miles away";
        }

        toolbar.setTitle(mOrganization.getTitle());

        toolbarBackdrop.setColorFilter(Color.argb(50, 0, 0, 0));
        toolbarBackdrop.setImageBitmap(mOrganization.getImage());
        description.setText(mOrganization.getDescription());
        created.setText(c);
        location.setText(loc);
        distance.setText(d);
        goal.setMax((float) mOrganization.getPrice());
        goal.setProgress(mOrganization.getMoneyRaised());
        goalText.setText("$" + mOrganization.getMoneyRaised() + " of $" + mOrganization.getPrice());

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);

        mapFragment.getMapAsync(this);

        unable.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);

        loved.setText(String.valueOf(mOrganization.getLoved().size()));
        viewed.setText(String.valueOf(mOrganization.getViewed()));

        if (mOrganization.getUserUID().equals(FundMe.userDataManager.getUser().getUid())) {
            joinGroupButton.setText("My Group");
            joinGroupButton.setEnabled(false);
            editButton.setVisibility(View.VISIBLE);
        }

        if (mOrganization.getLoved().contains(FundMe.userDataManager.getUser().getUid())) {
            menu.getItem(0).setIcon(R.drawable.ic_like_filled);
            isLiked = true;
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_like_white);
            isLiked = false;
        }

        if (mOrganization.getMembers().contains(FundMe.userDataManager.getUser().getUid())) {
            joinGroupButton.setText("Leave");
            joined = true;
        } else {
            joinGroupButton.setText("Join");
            joined = false;
        }


    }

    private String getShortState(String state) {
        Map<String, String> states = new HashMap<String, String>();
        states.put("Alabama", "AL");
        states.put("Alaska", "AK");
        states.put("Alberta", "AB");
        states.put("American Samoa", "AS");
        states.put("Arizona", "AZ");
        states.put("Arkansas", "AR");
        states.put("Armed Forces (AE)", "AE");
        states.put("Armed Forces Americas", "AA");
        states.put("Armed Forces Pacific", "AP");
        states.put("British Columbia", "BC");
        states.put("California", "CA");
        states.put("Colorado", "CO");
        states.put("Connecticut", "CT");
        states.put("Delaware", "DE");
        states.put("District Of Columbia", "DC");
        states.put("Florida", "FL");
        states.put("Georgia", "GA");
        states.put("Guam", "GU");
        states.put("Hawaii", "HI");
        states.put("Idaho", "ID");
        states.put("Illinois", "IL");
        states.put("Indiana", "IN");
        states.put("Iowa", "IA");
        states.put("Kansas", "KS");
        states.put("Kentucky", "KY");
        states.put("Louisiana", "LA");
        states.put("Maine", "ME");
        states.put("Manitoba", "MB");
        states.put("Maryland", "MD");
        states.put("Massachusetts", "MA");
        states.put("Michigan", "MI");
        states.put("Minnesota", "MN");
        states.put("Mississippi", "MS");
        states.put("Missouri", "MO");
        states.put("Montana", "MT");
        states.put("Nebraska", "NE");
        states.put("Nevada", "NV");
        states.put("New Brunswick", "NB");
        states.put("New Hampshire", "NH");
        states.put("New Jersey", "NJ");
        states.put("New Mexico", "NM");
        states.put("New York", "NY");
        states.put("Newfoundland", "NF");
        states.put("North Carolina", "NC");
        states.put("North Dakota", "ND");
        states.put("Northwest Territories", "NT");
        states.put("Nova Scotia", "NS");
        states.put("Nunavut", "NU");
        states.put("Ohio", "OH");
        states.put("Oklahoma", "OK");
        states.put("Ontario", "ON");
        states.put("Oregon", "OR");
        states.put("Pennsylvania", "PA");
        states.put("Prince Edward Island", "PE");
        states.put("Puerto Rico", "PR");
        states.put("Quebec", "PQ");
        states.put("Rhode Island", "RI");
        states.put("Saskatchewan", "SK");
        states.put("South Carolina", "SC");
        states.put("South Dakota", "SD");
        states.put("Tennessee", "TN");
        states.put("Texas", "TX");
        states.put("Utah", "UT");
        states.put("Vermont", "VT");
        states.put("Virgin Islands", "VI");
        states.put("Virginia", "VA");
        states.put("Washington", "WA");
        states.put("West Virginia", "WV");
        states.put("Wisconsin", "WI");
        states.put("Wyoming", "WY");
        states.put("Yukon Territory", "YT");

        return states.get(state);
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

    private void startCurrentLocation() {
        String perms[] = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
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
                progressDialog.hide();
                initializeData();
                Toast.makeText(this, "Could not connect to google locations", Toast.LENGTH_LONG).show();
            }
        } else {
            progressDialog.hide();
            EasyPermissions.requestPermissions(this, getString(R.string.location_permission_rationale), RC_PERMISSION_LOCATION, perms);
        }
    }

    private void selectLocation(Location location) {
        findLocationClicked = false;

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addr = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addr.size() > 0) {
                current = addr.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeData();
        progressDialog.hide();

    }

}
