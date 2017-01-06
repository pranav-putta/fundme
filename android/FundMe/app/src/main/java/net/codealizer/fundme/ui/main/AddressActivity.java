package net.codealizer.fundme.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.jaredrummler.materialspinner.MaterialSpinner;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnCompletedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatEditText street;
    AppCompatEditText city;
    MaterialSpinner state;
    AppCompatEditText zipCode;

    Button ok;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();
    }

    private void initialize() {
        street = (AppCompatEditText) findViewById(R.id.address_street);
        city = (AppCompatEditText) findViewById(R.id.address_city);
        state = (MaterialSpinner) findViewById(R.id.address_state);
        zipCode = (AppCompatEditText) findViewById(R.id.address_zip_code);

        ok = (Button) findViewById(R.id.address_ok);
        cancel = (Button) findViewById(R.id.address_cancel);

        ok.setOnClickListener(this);

        state.setItems(getStates());

        if (getIntent().hasExtra("ADDRESS")) {
            String address = getIntent().getStringExtra("ADDRESS");

            String[] parts = address.split(", ");

            street.setText(parts[0]);
            city.setText(parts[1]);
            state.setSelectedIndex(getStates().indexOf(parts[2]));
            zipCode.setText(parts[3]);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.address_ok:
                setAddress();
                break;
            case R.id.address_cancel:
                finish();
                break;
        }
    }

    private void setAddress() {
        boolean good = checkRequired();

        if (good) {
            String address = street.getText().toString() + ", " + city.getText().toString() + ", "
                    + state.getText().toString() + ", " + zipCode.getText().toString();

            User user = FundMe.userDataManager.getUser();
            user.setAddress(address);

            final Intent intent = new Intent();

            DatabaseManager.updateUser(user, new OnCompletedListener() {
                @Override
                public void onServiceSuccessful() {
                    intent.putExtra("complete", true);

                    setResult(ViewItemActivity.RC_ADDRESS, intent);
                    finish();
                }

                @Override
                public void onServiceFailed() {
                    intent.putExtra("complete", false);

                    setResult(ViewItemActivity.RC_ADDRESS, intent);
                    finish();
                }
            });


        }
    }

    private boolean checkRequired() {
        boolean valid = true;

        if (street.getText().toString().isEmpty()) {
            valid = false;
            street.setError("Required field");
        }
        if (city.getText().toString().isEmpty()) {
            valid = false;
            street.setError("Required field");
        }
        if (state.getText().toString().isEmpty()) {
            valid = false;
            street.setError("Required field");
        }
        if (zipCode.getText().toString().isEmpty()) {
            valid = false;
            street.setError("Required field");
        }

        return valid;
    }

    private List<String> getStates() {
        ArrayList<String> states = new ArrayList<>();
        states.add("AL");
        states.add("AK");
        states.add("AB");
        states.add("AS");
        states.add("AZ");
        states.add("AR");
        states.add("AE");
        states.add("AA");
        states.add("AP");
        states.add("BC");
        states.add("CA");
        states.add("CO");
        states.add("CT");
        states.add("DE");
        states.add("DC");
        states.add("FL");
        states.add("GA");
        states.add("GU");
        states.add("HI");
        states.add("ID");
        states.add("IL");
        states.add("IN");
        states.add("IA");
        states.add("KS");
        states.add("KY");
        states.add("LA");
        states.add("ME");
        states.add("MB");
        states.add("MD");
        states.add("MA");
        states.add("MI");
        states.add("MN");
        states.add("MS");
        states.add("MO");
        states.add("MT");
        states.add("NE");
        states.add("NV");
        states.add("NB");
        states.add("NH");
        states.add("NJ");
        states.add("NM");
        states.add("NY");
        states.add("NF");
        states.add("NC");
        states.add("ND");
        states.add("NT");
        states.add("NS");
        states.add("NU");
        states.add("OH");
        states.add("OK");
        states.add("ON");
        states.add("OR");
        states.add("PA");
        states.add("PE");
        states.add("PR");
        states.add("PQ");
        states.add("RI");
        states.add("SK");
        states.add("SC");
        states.add("SD");
        states.add("TN");
        states.add("TX");
        states.add("UT");
        states.add("VT");
        states.add("VI");
        states.add("VA");
        states.add("WA");
        states.add("WV");
        states.add("WI");
        states.add("WY");
        states.add("YT");

        return states;
    }

}
