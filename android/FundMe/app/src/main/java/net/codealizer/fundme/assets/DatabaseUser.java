package net.codealizer.fundme.assets;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUser {
    public String uid;
    public String firstName;
    public String lastName;
    public String email;
    public String profilePictureURL;
    public String lastLoggedIn;
    public double moneyRaised;
    public double rating;
    public List<String> organizationUids;
    public List<String> itemUids;
    public List<String> joinedOrganizations;

    public DatabaseUser(User user) {
        uid = user.uid;
        firstName = user.firstName;
        lastName = user.lastName;
        email = user.email;
        profilePictureURL = user.profilePic;
        lastLoggedIn = user.lastLoggedIn;
        moneyRaised = user.moneyRaised;
        rating = user.rating;
        organizationUids = user.getOrganizationUids();
        itemUids = user.getItemUids();
        joinedOrganizations = user.getJoinedOrganizations();
    }
}