package net.codealizer.fundme.ui.main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Condition;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewItemActivity extends AppCompatActivity implements OnDownloadListener, OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks, View.OnClickListener, Runnable {

    public static final String KEY_ITEM_UID = "net.codealizer.fundme.main.ViewItemActivity.ITEM_UID";
    public static final String KEY_ITEM = "net.codealizer.fundme.main.ViewItemActivity.ITEM";

    private static final int RC_PERMISSION_LOCATION = 1;
    public static final int RC_ADDRESS = 2;


    private static final long LOCATION_INTERVAL = 1000;
    private CoordinatorLayout container;

    private CollapsingToolbarLayout toolbar;
    private ImageView toolbarBackdrop;
    private Toolbar pToolbar;
    private TextView price;
    private TextView description;
    private TextView created;
    private TextView location;
    private TextView distance;
    private TextView loved;
    private TextView viewed;
    private boolean isLiked;
    private RatingBar condition;
    private TextView conditionText;

    private NestedScrollView content;
    private NestedScrollView unable;

    private LinearLayout buttons;
    private Button buyButton;
    private Button chatButton;
    private FloatingActionButton editButton;

    private MapFragment mapFragment;

    private ProgressDialog progressDialog;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    private boolean findLocationClicked;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;

    private String uid;
    private Address address;
    private Address current;
    private Item mItem;

    private Menu menu;

    private int reloaded = 0;
    private boolean isBought = false;
    private boolean shouldUpdate = true;
    private boolean isLocationFound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldUpdate) {
            // Force a reload of information
            if (getIntent().hasExtra(KEY_ITEM_UID)) {
                uid = getIntent().getStringExtra(KEY_ITEM_UID);
            } else if (getIntent().hasExtra(KEY_ITEM)) {
                mItem = getIntent().getParcelableExtra(KEY_ITEM);
            }

            if (reloaded > 0) {
                if (mItem != null) {
                    uid = mItem.getUid();
                    mItem = null;
                }
            }

            initializeUI();
        } else {
            shouldUpdate = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ADDRESS) {
            if (data.getBooleanExtra("complete", false)) {
                AlertDialogManager.showBuyItemDialog(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mItem = DatabaseManager.createItemRequest(mItem, ViewItemActivity.this);
                        if (mItem.getBuyRequests().contains(FundMe.userDataManager.getUser().getUid())) {
                            AlertDialogManager.showMessageSnackbar(container, "Requested item! Please wait for the owner to respond", Snackbar.LENGTH_LONG);
                            buyButton.setText("Stop Buying");
                            isBought = true;
                        } else {
                            AlertDialogManager.showMessageSnackbar(container, "Canceled item request!", Snackbar.LENGTH_LONG);
                            buyButton.setText("Buy Item");
                            isBought = false;
                        }
                    }
                }, mItem);
            } else {
                AlertDialogManager.showMessageSnackbar(container, "Couldn't process the information");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_item, menu);

        this.menu = menu;


        return true;
    }

    @Override
    public <D> void onDownloadSuccessful(D data) {
        if (data instanceof Item) {
            AlertDialogManager.showMessageSnackbar(container, "Item refreshed!");

            mItem = (Item) data;
            startCurrentLocation();
        } else {
            onDownloadFailed("Item was invalid");
            progressDialog.hide();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.view_item_like:
                like();
        }

        return true;
    }

    private void like() {
        if (isLiked) {
            isLiked = false;
            menu.getItem(0).setIcon(R.drawable.ic_like_white);
            mItem = DatabaseManager.likeItem(mItem, null, this, false);
        } else {
            isLiked = true;
            menu.getItem(0).setIcon(R.drawable.ic_like_filled);
            DatabaseManager.likeItem(mItem, null, this, true);
        }
        loved.setText(String.valueOf(mItem.getLoved().size()));

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
                    .title("Item location"));

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
        if (mItem == null) {
            retrieveItemInformation();
        } else {
            onDownloadSuccessful(mItem);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mItem == null) {
            retrieveItemInformation();
            AlertDialogManager.showMessageSnackbar(container, "Couldn't connect to google maps");
        } else {
            onDownloadSuccessful(mItem);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mItem == null) {
            retrieveItemInformation();
            AlertDialogManager.showMessageSnackbar(container, "Couldn't connect to google maps");
        } else {
            onDownloadSuccessful(mItem);
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
        initializeData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_item_edit:
                Intent intent = new Intent(this, CreateItemActivity.class);
                intent.putExtra(CreateItemActivity.KEY_EDIT_ITEM, mItem);

                this.startActivity(intent);
                break;
            case R.id.view_item_buy_item:
                buyItem();
                break;
        }
    }

    private void buyItem() {
        if (!isBought) {
            // Check if the user has enough money to buy
            double userMoney = FundMe.userDataManager.getUser().getVirtualMoney();
            boolean canBuy = userMoney > mItem.getPrice();

            if (canBuy) {
                if (!FundMe.userDataManager.getUser().getAddress().isEmpty()) {
                    AlertDialogManager.showConfirmAddressDialog(this, FundMe.userDataManager.getUser().address, mItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mItem = DatabaseManager.createItemRequest(mItem, ViewItemActivity.this);
                            AlertDialogManager.showMessageSnackbar(container, "Requested item! Please wait for the owner to respond", Snackbar.LENGTH_LONG);
                            if (mItem.getBuyRequests().contains(FundMe.userDataManager.getUser().getUid())) {
                                buyButton.setText("Stop Buying");
                                isBought = true;
                            } else {
                                buyButton.setText("Buy Item");
                                AlertDialogManager.showMessageSnackbar(container, "Canceled item request!", Snackbar.LENGTH_LONG);
                                isBought = false;
                            }
                        }
                    });
                } else {
                    shouldUpdate = false;
                    AlertDialogManager.showInvalidAddressDialog(this);
                }
            } else {
                AlertDialogManager.showInsufficientCreditsDialog(this);
            }
        } else {
            mItem = DatabaseManager.removeItemRequest(mItem, this);
            if (mItem.getBuyRequests().contains(FundMe.userDataManager.getUser().getUid())) {
                AlertDialogManager.showMessageSnackbar(container, "Requested item! Please wait for the owner to respond", Snackbar.LENGTH_LONG);
                buyButton.setText("Stop Buying");
                isBought = true;
            } else {
                buyButton.setText("Buy Item");
                AlertDialogManager.showMessageSnackbar(container, "Canceled item request!", Snackbar.LENGTH_LONG);
                isBought = false;
            }

        }
    }


    private void retrieveItemInformation() {
        isLocationFound = false;

        if (uid != null) {
            if (ServiceManager.isNetworkAvailable(this)) {
                DatabaseManager.getItem(uid, this);
            } else {
                onDownloadFailed("No internet connection!");
            }
        }
    }

    private void initializeUI() {
        container = (CoordinatorLayout) findViewById(R.id.view_item_container);
        toolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_view_item_container);
        pToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarBackdrop = (ImageView) findViewById(R.id.toolbar_view_item_backdrop);
        price = (TextView) findViewById(R.id.view_item_price);
        description = (TextView) findViewById(R.id.view_item_description);
        created = (TextView) findViewById(R.id.view_item_created);
        location = (TextView) findViewById(R.id.view_item_location);
        distance = (TextView) findViewById(R.id.view_item_distance);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.view_item_map);
        buttons = (LinearLayout) findViewById(R.id.view_item_action_button_container);
        editButton = (FloatingActionButton) findViewById(R.id.view_item_edit);
        unable = (NestedScrollView) findViewById(R.id.view_item_unable);
        content = (NestedScrollView) findViewById(R.id.view_item_content);
        loved = (TextView) findViewById(R.id.liked_count);
        viewed = (TextView) findViewById(R.id.viewed_count);
        buyButton = (Button) findViewById(R.id.view_item_buy_item);
        chatButton = (Button) findViewById(R.id.view_item_chat);
        conditionText = (TextView) findViewById(R.id.view_item_condition_description);
        condition = (RatingBar) findViewById(R.id.view_item_condition);

        condition.setIsIndicator(true);

        setSupportActionBar(pToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        progressDialog = AlertDialogManager.showProgressDialog(this);

        editButton.setOnClickListener(this);

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        unable.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);

        reloaded += 1;
    }

    private void initializeData() {
        mItem = DatabaseManager.addViewedItem(mItem, this);

        String p = "$" + Math.round(mItem.getPrice());
        String c = "Created " + ServiceManager.getTimePassed(mItem.getDateCreated());
        String zip = mItem.getZipCode() + "";
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

        toolbar.setTitle(mItem.getTitle());

        toolbarBackdrop.setColorFilter(Color.argb(50, 0, 0, 0));
        toolbarBackdrop.setImageBitmap(mItem.getImage());
        price.setText(p);
        description.setText(mItem.getDescription());
        created.setText(c);
        location.setText(loc);
        distance.setText(d);
        loved.setText(String.valueOf(mItem.getLoved().size()));
        viewed.setText(String.valueOf(mItem.getViewed()));

        chatButton.setOnClickListener(this);
        buyButton.setOnClickListener(this);

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);


        mapFragment.getMapAsync(this);

        unable.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);

        if (mItem.getUserUID().equals(FundMe.userDataManager.getUser().getUid())) {
            buyButton.setVisibility(View.GONE);
            chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
            chatButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        } else {
            buyButton.setVisibility(View.VISIBLE);
            chatButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }

        if (mItem.getLoved().contains(FundMe.userDataManager.getUser().getUid())) {
            menu.getItem(0).setIcon(R.drawable.ic_like_filled);
            isLiked = true;
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_like_white);
            isLiked = false;
        }

        if (mItem.getBuyRequests().contains(FundMe.userDataManager.getUser().getUid())) {
            buyButton.setText("Stop Buying");
            isBought = true;
        } else {
            buyButton.setText("Buy Item");
            isBought = false;
        }

        if (mItem.isSold()) {
            buyButton.setEnabled(false);
            buyButton.setText("Sold");
            buyButton.setTextColor(Color.BLACK);
        }

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewItemActivity.this, CommentsActivity.class);
                intent.putExtra(CommentsActivity.ITEM, mItem);
                startActivity(intent);
            }
        });

        if (mItem.condition > 0) {
            condition.setRating(mItem.condition);
        } else {
            condition.setRating(1);
        }
        conditionText.setText(Condition.getCondition(mItem.condition).toString());


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
        String perms[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        findLocationClicked = true;
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (mGoogleApiClient.isConnected()) {
                final Location l = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
                if (l != null && (System.currentTimeMillis() - l.getTime()) < 1_200_000) {
                    selectLocation(l);
                } else {
                    new Handler().postDelayed(this, 3_000);
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
        isLocationFound = true;
        findLocationClicked = false;

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addr = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addr.size() > 0) {
                current = addr.get(0);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        initializeData();
        progressDialog.hide();

    }

    @Override
    public void run() {
        if (!isLocationFound) {
            fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
            AlertDialogManager.showMessageSnackbar(container, "Couldn't find your location");

            selectLocation(null);

            distance.setText("Current location unavailable");
        }
    }
}
