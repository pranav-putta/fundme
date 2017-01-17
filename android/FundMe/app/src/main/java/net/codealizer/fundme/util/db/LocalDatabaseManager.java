package net.codealizer.fundme.util.db;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.util.ServiceManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/24/16.
 */

public class LocalDatabaseManager extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "fundMeDatabase";

    // Contacts table name
    private static final String TABLE_SEARCH_SUGGESTIONS = "searchSuggestions";
    private static final String TABLE_ORGANIZATIONS = "organizations";
    private static final String TABLE_ITEMS = "items";

    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";

    // Contacts Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";

    // Organizations Table Column names
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PRICE = "price";
    private static final String KEY_ZIPCODE = "zipCode";
    private static final String KEY_DATE_CREATED = "dateCreated";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER_UID = "userUID";
    private static final String KEY_LINK = "link";
    private static final String KEY_IMAGE_URL = "imageURL";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_LOVED = "loved";
    private static final String KEY_VIEWED = "viewed";
    private static final String KEY_BUY_REQ = "buyRequests";
    private static final String KEY_SOLD = "sold";
    private static final String KEY_MONEY_RAISED  = "moneyRaised";
    private static final String KEY_CONDITION = "condition";

    public LocalDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SEARCH_SUGGESTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_UID + " TEXT," + KEY_TYPE + " TEXT," + KEY_TAGS + " TEXT" + ")";
        String CREATE_ORGANIZATIONS_TABLE = "CREATE TABLE " + TABLE_ORGANIZATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_UID + " TEXT,"
                + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_PRICE + " TEXT," + KEY_ZIPCODE + " TEXT," +
                KEY_DATE_CREATED + " TEXT," + KEY_IMAGE + " TEXT," + KEY_USER_UID + " TEXT," +
                KEY_LINK + " TEXT," + KEY_IMAGE_URL + " TEXT," + KEY_MONEY_RAISED + " TEXT" + ")";
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_UID + " TEXT,"
                + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_PRICE + " TEXT," + KEY_ZIPCODE + " TEXT," +
                KEY_DATE_CREATED + " TEXT," + KEY_IMAGE + " TEXT," + KEY_USER_UID + " TEXT," + KEY_IMAGE_URL + " TEXT," + KEY_TAGS + " TEXT,"
                + KEY_LOVED + " TEXT," + KEY_VIEWED + " TEXT," + KEY_BUY_REQ + " TEXT, " + KEY_SOLD + " TEXT," + KEY_CONDITION + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_ORGANIZATIONS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_SUGGESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORGANIZATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addSearchItem(SearchItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_UID, item.getUid());
        values.put(KEY_TYPE, item.getType().toString());
        values.put(KEY_TAGS, ServiceManager.convertArrayToString(item.getTags()));

        // Inserting Row
        try {
            db.insertOrThrow(TABLE_SEARCH_SUGGESTIONS, null, values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        db.close();


        // Closing database connection
    }

    public void addOrganization(Organization organization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, organization.getUid());
        values.put(KEY_TITLE, organization.getTitle());
        values.put(KEY_DESCRIPTION, organization.getDescription());
        values.put(KEY_PRICE, organization.getPrice());
        values.put(KEY_ZIPCODE, organization.getZipCode());
        values.put(KEY_DATE_CREATED, organization.getDateCreated());
        values.put(KEY_IMAGE, ServiceManager.ImageHelper.encodeToBase64(organization.getImage()));
        values.put(KEY_USER_UID, organization.getUserUID());
        values.put(KEY_LINK, organization.getLink());
        values.put(KEY_IMAGE_URL, organization.getImageURL());
        values.put(KEY_MONEY_RAISED, organization.getMoneyRaised());


        // Inserting Row
        db.insert(TABLE_ORGANIZATIONS, null, values);
        db.close();
        // Closing database connection
    }

    public void addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, item.getUid());
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_PRICE, item.getPrice());
        values.put(KEY_ZIPCODE, item.getZipCode());
        values.put(KEY_DATE_CREATED, item.getDateCreated());
        values.put(KEY_IMAGE, ServiceManager.ImageHelper.encodeToBase64(item.getImage()));
        values.put(KEY_USER_UID, item.getUserUID());
        values.put(KEY_IMAGE_URL, item.getImageURL());
        values.put(KEY_TAGS, ServiceManager.convertArrayToString(item.getTags()));
        values.put(KEY_LOVED, ServiceManager.convertArrayToString(item.getLoved()));
        values.put(KEY_VIEWED, item.getViewed());
        values.put(KEY_BUY_REQ, ServiceManager.convertArrayToString(item.getBuyRequests()));
        values.put(KEY_SOLD, item.isSold());
        values.put(KEY_CONDITION, item.getCondition());

        // Inserting Row
        db.insert(TABLE_ITEMS, null, values);
        db.close(); // Closing database connection
    }

    public ArrayList<Organization> getAllOrganizations() {
        ArrayList<Organization> itemList = new ArrayList<Organization>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ORGANIZATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Organization o = new Organization();
                o.setUid(cursor.getString(1));
                o.setTitle(cursor.getString(2));
                o.setDescription(cursor.getString(3));
                o.setPrice(Double.parseDouble(cursor.getString(4)));
                o.setZipCode(Integer.parseInt(cursor.getString(5)));
                o.setDateCreated(Long.parseLong(cursor.getString(6)));
                o.setImage(ServiceManager.ImageHelper.decodeToBase64(cursor.getString(7)));
                o.setUserUID(cursor.getString(8));
                o.setLink(cursor.getString(9));
                o.setImageURL(cursor.getString(10));
                o.setMoneyRaised(Integer.parseInt(cursor.getString(11)));
                // Adding contact to list
                itemList.add(o);
            } while (cursor.moveToNext());
        }

        // return contact list
        return itemList;
    }

    public ArrayList<Item> getAllItems() {
        ArrayList<Item> itemList = new ArrayList<Item>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ITEMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    Item o = new Item();
                    o.setUid(cursor.getString(1));
                    o.setTitle(cursor.getString(2));
                    o.setDescription(cursor.getString(3));
                    o.setPrice(Double.parseDouble(cursor.getString(4)));
                    o.setZipCode(Integer.parseInt(cursor.getString(5)));
                    o.setDateCreated(Long.parseLong(cursor.getString(6)));
                    o.setImage(ServiceManager.ImageHelper.decodeToBase64(cursor.getString(7)));
                    o.setUserUID(cursor.getString(8));
                    o.setImageURL(cursor.getString(9));
                    o.setTags(ServiceManager.convertStringToArray(cursor.getString(10)));
                    o.setLoved(ServiceManager.convertStringToArray(cursor.getString(11)));
                    o.setViewed(Integer.parseInt(cursor.getString(12)));
                    o.setBuyRequests(ServiceManager.convertStringToArray(cursor.getString(13)));
                    o.setSold(Boolean.parseBoolean(cursor.getString(14)));
                    o.setCondition(Integer.parseInt(cursor.getString(15)));
                    // Adding contact to list
                    itemList.add(o);
                } catch (Exception ex) {
                }
            } while (cursor.moveToNext());
        }

        // return contact list
        return itemList;
    }

    // Getting single contact
    SearchItem getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SEARCH_SUGGESTIONS, new String[]{KEY_ID,
                        KEY_NAME, KEY_UID, KEY_TYPE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        SearchItem item = new SearchItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), ServiceManager.convertStringToArray(cursor.getString(4)), SearchItem.SearchItemType.valueOf(cursor.getString(3)));
        // return contact
        return item;
    }

    // Getting All Contacts
    public List<SearchItem> getAllSearchItems() {
        List<SearchItem> itemList = new ArrayList<SearchItem>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH_SUGGESTIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SearchItem item = new SearchItem();
                item.setId(Integer.parseInt(cursor.getString(0)));
                item.setName(cursor.getString(1));
                item.setUid(cursor.getString(2));
                item.setType(SearchItem.SearchItemType.valueOf(cursor.getString(3)));
                item.setTags(ServiceManager.convertStringToArray(cursor.getString(4)));

                // Adding contact to list
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        // return contact list
        return itemList;
    }

    public int updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, item.getUid());
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_PRICE, item.getPrice());
        values.put(KEY_ZIPCODE, item.getZipCode());
        values.put(KEY_DATE_CREATED, item.getDateCreated());
        values.put(KEY_IMAGE, ServiceManager.ImageHelper.encodeToBase64(item.getImage()));
        values.put(KEY_USER_UID, item.getUserUID());
        values.put(KEY_IMAGE_URL, item.getImageURL());
        values.put(KEY_TAGS, ServiceManager.convertArrayToString(item.getTags()));
        values.put(KEY_LOVED, ServiceManager.convertArrayToString(item.getLoved()));
        values.put(KEY_VIEWED, item.getViewed());
        values.put(KEY_BUY_REQ, ServiceManager.convertArrayToString(item.buyRequests));
        values.put(KEY_SOLD, item.sold);
        values.put(KEY_CONDITION, item.getCondition());

        // updating row
        return db.update(TABLE_ITEMS, values, KEY_UID + " = ?",
                new String[]{String.valueOf(item.getUid())});
    }

    public int updateOrganization(Organization organization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, organization.getUid());
        values.put(KEY_TITLE, organization.getTitle());
        values.put(KEY_DESCRIPTION, organization.getDescription());
        values.put(KEY_PRICE, organization.getPrice());
        values.put(KEY_ZIPCODE, organization.getZipCode());
        values.put(KEY_DATE_CREATED, organization.getDateCreated());
        values.put(KEY_IMAGE, ServiceManager.ImageHelper.encodeToBase64(organization.getImage()));
        values.put(KEY_USER_UID, organization.getUserUID());
        values.put(KEY_LINK, organization.getLink());
        values.put(KEY_IMAGE_URL, organization.getImageURL());
        values.put(KEY_MONEY_RAISED, organization.getMoneyRaised());

        // updating row
        return db.update(TABLE_ORGANIZATIONS, values, KEY_UID + " = ?",
                new String[]{String.valueOf(organization.getUid())});
    }

    // Updating single contact
    public int updateContact(SearchItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, item.getId());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_UID, item.getUid());
        values.put(KEY_TYPE, item.getType().toString());

        // updating row
        return db.update(TABLE_SEARCH_SUGGESTIONS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // Deleting single contact
    public void deleteContact(SearchItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SEARCH_SUGGESTIONS, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        db.close();
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SEARCH_SUGGESTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_SUGGESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORGANIZATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);

        // Create tables again
        onCreate(db);
    }


    public void resetItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_UID + " TEXT,"
                + KEY_TITLE + " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_PRICE + " TEXT," + KEY_ZIPCODE + " TEXT," +
                KEY_DATE_CREATED + " TEXT," + KEY_IMAGE + " TEXT," + KEY_USER_UID + " TEXT," + KEY_IMAGE_URL + " TEXT," + KEY_TAGS + " TEXT,"
                + KEY_LOVED + " TEXT," + KEY_VIEWED + " TEXT," + KEY_BUY_REQ + " TEXT, " + KEY_SOLD + " TEXT," + KEY_CONDITION + " TEXT" + ")";

        db.execSQL(CREATE_ITEMS_TABLE);
    }
}
