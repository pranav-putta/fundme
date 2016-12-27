package net.codealizer.fundme.ui.login.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnProgressScreenListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Pranav on 11/20/16.
 */

public class SignUpEmailPage3Fragment extends Fragment implements View.OnClickListener {

    Button next;
    Button birthdaySelect;

    EditText birthday;
    Spinner gender;

    DatePickerDialog datePickerDialog;
    Calendar calendar;
    SimpleDateFormat format;

    OnProgressScreenListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup_email_page3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        next = (Button) getView().findViewById(R.id.next);
        birthday = (EditText) getView().findViewById(R.id.signup_birthday);
        gender = (Spinner) getView().findViewById(R.id.signup_gender);
        birthdaySelect = (Button) getView().findViewById(R.id.signup_birthday_select);

        calendar = Calendar.getInstance();
        format = new SimpleDateFormat("MM-dd-yyy", Locale.US);
        datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(year, monthOfYear, dayOfMonth);
                birthday.setText(format.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Select Birthday");

        birthday.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    ServiceManager.hideSoftKeyboard(SignUpEmailPage3Fragment.this.getActivity(), birthday);
                }

                return false;
            }
        });

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        birthdaySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        birthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });
        birthday.clearFocus();
        birthday.setText(format.format(calendar.getTime()));
        ServiceManager.hideSoftKeyboard(getActivity(), birthday);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String bday = birthday.getText().toString();
        String sex = gender.getSelectedItem().toString().toLowerCase();

        if (!bday.isEmpty()) {
            Pair<String, String> a = new Pair<>("birthday", bday);
            Pair<String, String> b = new Pair<>("gender", sex);

            listener.onScreenProgress(3, a, b);
        }
    }

    public void setListeners(OnProgressScreenListener listeners) {
        this.listener = listeners;
    }
}
