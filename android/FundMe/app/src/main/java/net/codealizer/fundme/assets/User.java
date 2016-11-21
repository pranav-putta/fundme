package net.codealizer.fundme.assets;

import android.os.Bundle;

import java.lang.reflect.Field;

/**
 * Created by Pranav on 11/19/16.
 */

public class User {

    public String uid;
    public String first_name;
    public String last_name;
    public String email;
    public String birthday;
    public String gender;
    public String profile_pic;
    public String last_logged_in;

    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_PROFILE_PIC = "profile_pic";
    private static final String KEY_LAST_LOGGED_IN = "last_logged_in";

    public User(String UID, String firstName, String lastName, String email, String birthday, String gender, String profilePic) {
        this.uid = UID;
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.profile_pic = profilePic;
    }


    public User(String uid, Bundle data) {
        this.uid = uid;
        this.first_name = data.getString(KEY_FIRST_NAME, "");
        this.last_name = data.getString(KEY_LAST_NAME, "");
        this.email = data.getString(KEY_EMAIL, "");
        this.birthday = data.getString(KEY_BIRTHDAY, "");
        this.gender = data.getString(KEY_GENDER, "");
        this.profile_pic = data.getString(KEY_PROFILE_PIC, "");
        this.last_logged_in = data.getString(KEY_LAST_LOGGED_IN, String.valueOf(System.currentTimeMillis()));

        if (this.birthday == null || this.birthday.isEmpty()) {
            this.birthday = "";
        }
    }

    public User(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getLast_logged_in() {
        return last_logged_in;
    }

    public void setLast_logged_in(String last_logged_in) {
        this.last_logged_in = last_logged_in;
    }


}
