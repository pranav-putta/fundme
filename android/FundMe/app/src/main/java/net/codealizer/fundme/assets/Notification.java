package net.codealizer.fundme.assets;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification object class
 *
 * Stores information in correspondence to its specific organization
 */

public class Notification implements Parcelable {

    public String imageURL;
    public String description;
    public NotificationType type;
    public boolean hasSeen;
    public DatabaseUser user;
    public DatabaseItem item;

    public Notification(String imageURL, String description, NotificationType type, boolean hasSeen, DatabaseUser user, DatabaseItem item) {
        this.imageURL = imageURL;
        this.description = description;
        this.type = type;
        this.hasSeen = hasSeen;
        this.user = user;
        this.item = item;
    }

    protected Notification(Parcel in) {
        imageURL = in.readString();
        description = in.readString();
        hasSeen = in.readByte() != 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isHasSeen() {
        return hasSeen;
    }

    public void setHasSeen(boolean hasSeen) {
        this.hasSeen = hasSeen;
    }

    public DatabaseUser getUser() {
        return user;
    }

    public void setUser(DatabaseUser user) {
        this.user = user;
    }

    public DatabaseItem getItem() {
        return item;
    }

    public void setItem(DatabaseItem item) {
        this.item = item;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static List<String> toJson(List<Notification> notificationList) {
        List<String> json = new ArrayList<>();

        if (notificationList != null) {
            for (Notification n : notificationList) {
                json.add(n.toJson());
            }
        }

        return json;
    }

    public static Notification fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Notification.class);
    }

    public static List<Notification> fromJson(List<String> json) {
        try {
            List<Notification> notifications = new ArrayList<>();
            for (String s : json) {
                notifications.add(fromJson(s));
            }

            return notifications;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageURL);
        parcel.writeString(description);
        parcel.writeByte((byte) (hasSeen ? 1 : 0));
    }


    public enum NotificationType {
        INFO, ACCEPT
    }

}
