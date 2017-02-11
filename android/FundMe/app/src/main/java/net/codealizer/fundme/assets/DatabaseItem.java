package net.codealizer.fundme.assets;

import java.util.List;

/**
 * Database item for an item
 *
 * This is the item which is uploaded to the server
 */

public class DatabaseItem {
    public String uid;
    public String title;
    public String description;
    public double price;
    public int zipCode;
    public long dateCreated;
    public String userUID;
    public String imageURL;
    public List<String> tags;
    public List<String> loved;
    public int viewed;
    public List<String> buyRequests;
    public boolean sold;
    public List<Comment> comments;
    public int condition;

    public DatabaseItem(Item item) {
        uid = item.uid;
        title = item.title;
        description = item.description;
        price = item.price;
        zipCode = item.zipCode;
        dateCreated = item.dateCreated;
        userUID = item.userUID;
        imageURL = item.imageURL;
        tags = item.tags;
        loved = item.loved;
        viewed = item.viewed;
        buyRequests = item.buyRequests;
        sold = item.sold;
        comments = item.comments;
        condition = item.condition;
    }
}