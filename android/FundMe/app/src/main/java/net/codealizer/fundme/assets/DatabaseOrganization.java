package net.codealizer.fundme.assets;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOrganization {
    public String uid;
    public String title;
    public String description;
    public double price;
    public int zipCode;
    public long dateCreated;
    public String userUID;
    public String link;
    public String imageURL;
    public List<String> loved;
    public int viewed;
    public List<String> members;

    public DatabaseOrganization (Organization item) {
        uid = item.uid;
        title = item.title;
        description = item.description;
        price = item.price;
        zipCode = item.zipCode;
        dateCreated = item.dateCreated;
        userUID = item.userUID;
        link = item.link;
        imageURL = item.imageURL;
        loved = item.getLoved();
        viewed = item.getViewed();
        members = item.getMembers();

        if (loved == null) {
            loved = new ArrayList<>();
        }

        if (members == null) {
            members = new ArrayList<>();
        }
    }
}