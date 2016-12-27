package net.codealizer.fundme.assets;

import android.graphics.Bitmap;
import android.os.Bundle;

import net.codealizer.fundme.util.SignUpOption;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Pranav on 11/19/16.
 */

public class User {

    public String uid;
    public String firstName;
    public String lastName;
    public String email;
    public String profilePic;
    public String lastLoggedIn;
    public double moneyRaised;
    public double rating;

    private String profilePicture;

    private ArrayList<String> organizationUids;
    private ArrayList<String> itemUids;

    private ArrayList<Organization> organizations = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    public List<String> joinedOrganizations = new ArrayList<>();

    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_PIC = "profilePictureURL";
    private static final String KEY_LAST_LOGGED_IN = "lastLoggedIn";
    private static final String KEY_MONEY_RAISED = "moneyRaised";
    private static final String KEY_RATING = "rating";
    private static final String KEY_ORGANIZATIONS = "organizations";
    private static final String KEY_ITEMS = "items";
    private static final String KEY_MEMEBRS = "joinedOrganizations";


    public User(String uid, Bundle data) {
        this.uid = uid;
        this.firstName = data.getString(KEY_FIRST_NAME, "");
        this.lastName = data.getString(KEY_LAST_NAME, "");
        this.email = data.getString(KEY_EMAIL, "");
        this.profilePic = data.getString(KEY_PROFILE_PIC, "");
        this.lastLoggedIn = data.getString(KEY_LAST_LOGGED_IN, String.valueOf(System.currentTimeMillis()));
        this.moneyRaised = Double.parseDouble(data.getString(KEY_MONEY_RAISED, "0"));
        this.rating = Double.parseDouble(data.getString(KEY_RATING, "0"));
        this.organizations = (ArrayList<Organization>) data.get(KEY_ORGANIZATIONS);
        this.items = (ArrayList<Item>) data.get(KEY_ITEMS);
        this.organizationUids = data.getStringArrayList("organizationUids");
        this.itemUids = data.getStringArrayList("itemUids");
        this.joinedOrganizations = data.getStringArrayList("joinedOrganizations");

        if (this.organizations == null) {
            organizations = new ArrayList<>();
        }

        if (this.items == null) {
            items = new ArrayList<>();
        }

        if (this.organizationUids == null) {
            organizationUids = new ArrayList<>();
        }

        if (this.itemUids == null) {
            itemUids = new ArrayList<>();
        }

        if (this.joinedOrganizations == null) {
            joinedOrganizations = new ArrayList<>();
        }

    }

    public User(String uid) {
        this.uid = uid;
    }

    public User() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        if (profilePic.isEmpty() || profilePic.equals("")) {
            return "http://i.imgur.com/ZPk83zW.jpg";
        } else {
            return profilePic;
        }
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(String lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public double getMoneyRaised() {
        return moneyRaised;
    }

    public void setMoneyRaised(double moneyRaised) {
        this.moneyRaised = moneyRaised;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(ArrayList<Organization> organizations) {
        this.organizations = organizations;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
        itemUids.add(item.getUid());
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public ArrayList<String> getOrganizationUids() {
        return organizationUids;
    }

    public void setOrganizationUids(ArrayList<String> organizationUids) {
        this.organizationUids = organizationUids;
    }

    public ArrayList<String> getItemUids() {
        return itemUids;
    }

    public void setItemUids(ArrayList<String> itemUids) {
        this.itemUids = itemUids;
    }

    public void addOrganization(Organization organization) {
        organizations.add(organization);
        organizationUids.add(organization.getUid());
    }

    public List<String> getJoinedOrganizations() {
        return joinedOrganizations;
    }

    public void setJoinedOrganizations(List<String> joinedOrganizations) {
        this.joinedOrganizations = joinedOrganizations;
    }
}
