package net.codealizer.fundme.assets;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import net.codealizer.fundme.util.ServiceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Pranav on 12/20/16.
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
    }

    public Item(String title, String description, double price, int zipCode, long dateCreated, Bitmap image, List<String> tags, List<String> loved, int viewed) {
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

        if (loved == null) {
            this.loved = new ArrayList<>();
        }
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
}
