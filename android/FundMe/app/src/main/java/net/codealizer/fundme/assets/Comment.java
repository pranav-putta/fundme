package net.codealizer.fundme.assets;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object class for a comment
 *
 * Stores corresponding user name, comment, user image url, and the time of its creation
 */

public class Comment implements Parcelable {

    public String userName;
    public String comment;
    public String imageURL;
    public long creationDate;

    public Comment(String userName, String comment, String imageURL, long creationDate) {
        this.userName = userName;
        this.comment = comment;
        this.imageURL = imageURL;
        this.creationDate = creationDate;
    }

    protected Comment(Parcel in) {
        userName = in.readString();
        comment = in.readString();
        imageURL = in.readString();
        creationDate = in.readLong();
    }

    public Comment () {

    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeString(comment);
        parcel.writeString(imageURL);
        parcel.writeLong(creationDate);
    }
}
