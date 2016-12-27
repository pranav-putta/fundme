package net.codealizer.fundme.assets;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import net.codealizer.fundme.util.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/24/16.
 */

public class SearchItem implements SearchSuggestion {

    private int id;
    private String name;
    private String uid;
    private List<String> tags;
    private SearchItemType type;

    private boolean mIsHistory = false;

    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel parcel) {
            return new SearchItem(parcel);
        }

        @Override
        public SearchItem[] newArray(int i) {
            return new SearchItem[i];
        }
    };

    public SearchItem(int id, String name, String uid, List<String> tags, SearchItemType type) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.tags = tags;
        this.type = type;
    }

    public SearchItem(Parcel source) {
        this.id = source.readInt();
        this.name = source.readString();
        this.uid = source.readString();
        this.tags = ServiceManager.convertStringToArray(source.readString());
        this.type = SearchItemType.valueOf(source.readString());
        this.mIsHistory = source.readInt() != 0;
    }

    public SearchItem() {

    }

    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }

    @Override
    public String getBody() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(uid);
        parcel.writeString(ServiceManager.convertArrayToString(tags));
        parcel.writeString(type.toString());
        parcel.writeInt(mIsHistory ? 1 : 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public SearchItemType getType() {
        return type;
    }

    public void setType(SearchItemType type) {
        this.type = type;
    }

    public boolean ismIsHistory() {
        return mIsHistory;
    }

    public void setmIsHistory(boolean mIsHistory) {
        this.mIsHistory = mIsHistory;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public enum SearchItemType {
        ORGANIZATION, ITEM, PERSON
    }
}
