package net.codealizer.fundme.assets;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.util.ServiceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Item class
 *
 * This is where all the item information is stored, and refined
 *
 * This class is different from the DatabaseItem class in that it automatically loads information such as
 * the bitmap representation fo the item, while DatabaseItem only stores textual information that it is
 * applicable to the FirebaseDatabase
 */

public class Item implements Parcelable {
    public String uid;
    public String title;
    public String description;
    public double price;
    public int zipCode;
    public long dateCreated;
    public Bitmap image;
    public String userUID;
    public String imageURL;
    public List<String> tags;
    public List<String> loved;
    public int viewed;
    public List<String> buyRequests;
    public boolean sold;
    public List<Comment> comments;
    public int condition;

    public static final Parcelable.Creator<Item> CREATOR
            = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    private Item(Parcel in) {
        uid = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        zipCode = in.readInt();
        dateCreated = in.readLong();
        image = ServiceManager.ImageHelper.decodeToBase64(in.readString());
        userUID = in.readString();
        imageURL = in.readString();
        tags = ServiceManager.convertStringToArray(in.readString());
        loved = ServiceManager.convertStringToArray(in.readString());
        viewed = in.readInt();
        buyRequests = ServiceManager.convertStringToArray(in.readString());
        sold = Boolean.parseBoolean(in.readString());
        condition = in.readInt();
        comments = in.createTypedArrayList(Comment.CREATOR);
    }

    public Item(String title, String description, double price, int zipCode, long dateCreated, Bitmap image, List<String> tags, List<String> loved, int viewed, List<String> buyRequests, boolean sold, List<Comment> comments, int condition) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.price = price;
        this.zipCode = zipCode;
        this.dateCreated = dateCreated;
        this.image = image;
        this.tags = tags;
        this.loved = loved;
        this.viewed = viewed;
        this.buyRequests = buyRequests;
        this.sold = sold;
        this.comments = comments;
        this.condition = condition;

        if (loved == null) {
            this.loved = new ArrayList<>();
        }

        if (buyRequests == null) {
            this.buyRequests = new ArrayList<>();
        }

        if (comments == null) {
            this.comments = new ArrayList<>();
        }
    }

    public Item(DatabaseItem item) {
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
        condition = item.condition;
    }

    public Item() {

    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeDouble(price);
        parcel.writeInt(zipCode);
        parcel.writeLong(dateCreated);
        parcel.writeString(ServiceManager.ImageHelper.encodeToBase64(image));
        parcel.writeString(userUID);
        parcel.writeString(imageURL);
        parcel.writeString(ServiceManager.convertArrayToString(tags));
        parcel.writeString(ServiceManager.convertArrayToString(loved));
        parcel.writeInt(viewed);
        parcel.writeString(ServiceManager.convertArrayToString(buyRequests));
        parcel.writeString(String.valueOf(sold));
        parcel.writeInt(condition);
        try {
            parcel.writeTypedArray(comments.toArray(new Parcelable[0]), 0);
        } catch (Exception ex) {

        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }


    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getLoved() {
        return loved;
    }

    public void setLoved(List<String> loved) {
        this.loved = loved;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
    }

    public List<String> getBuyRequests() {
        return buyRequests;
    }

    public void setBuyRequests(List<String> buyRequests) {
        this.buyRequests = buyRequests;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }


}
