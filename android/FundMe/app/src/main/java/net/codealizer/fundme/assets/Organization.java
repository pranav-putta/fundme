package net.codealizer.fundme.assets;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import net.codealizer.fundme.util.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/20/16.
 */

public class Organization implements Parcelable {

    public String uid;
    public String title;
    public String description;
    public double price;
    public int zipCode;
    public long dateCreated;
    public Bitmap image;
    public String userUID;
    public String link;
    public String imageURL;
    public List<String> loved;
    public List<String> members;
    public int viewed;

    public static final Parcelable.Creator<Organization> CREATOR
            = new Parcelable.Creator<Organization>() {
        public Organization createFromParcel(Parcel in) {
            return new Organization(in);
        }

        public Organization[] newArray(int size) {
            return new Organization[size];
        }
    };

    private Organization(Parcel in) {
        uid = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readDouble();
        zipCode = in.readInt();
        dateCreated = in.readLong();
        image = ServiceManager.ImageHelper.decodeToBase64(in.readString());
        userUID = in.readString();
        link = in.readString();
        imageURL = in.readString();
        loved = ServiceManager.convertStringToArray(in.readString());
        viewed = in.readInt();
        members = ServiceManager.convertStringToArray(in.readString());
    }

    public Organization(String title, String description, double price, int zipCode, long dateCreated, Bitmap image, String link, List<String> loved, int viewed, List<String> members) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.zipCode = zipCode;
        this.dateCreated = dateCreated;
        this.image = image;
        this.link = link;
        this.imageURL = imageURL;
        this.loved = loved;
        this.viewed = viewed;
        this.members = members;

        if (loved == null) {
            this.loved = new ArrayList<>();
        }

        if (members == null) {
            this.members = new ArrayList<>();
        }
    }

    public Organization() {

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
        parcel.writeString(link);
        parcel.writeString(imageURL);
        parcel.writeString(ServiceManager.convertArrayToString(loved));
        parcel.writeInt(viewed);
        parcel.writeString(ServiceManager.convertArrayToString(members));
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
