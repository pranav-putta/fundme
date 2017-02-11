package net.codealizer.fundme.util.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.assets.Comment;
import net.codealizer.fundme.assets.DatabaseItem;
import net.codealizer.fundme.assets.DatabaseOrganization;
import net.codealizer.fundme.assets.DatabaseUser;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Notification;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.util.HttpHandler;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.db.LocalDatabaseManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;
import net.codealizer.fundme.util.listeners.OnDownloadListener;
import net.codealizer.fundme.util.listeners.OnUploadListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class DatabaseManager {

    private static final String USER_DATABASE_REF = "/server/fundme/users";
    private static final String STORAGE_REF = "gs://fundme-d9ecd.appspot.com";
    private static final String ITEM_DATABASE_REF = "/server/fundme/items";
    private static final String ORGANIZATION_DATABASE_REF = "/server/fundme/organizations";

    /**
     * Creates an organization in the firebase database, and updates data
     *
     * @param organization Organization data object to create
     * @param context      Application context
     * @param listener     Callback listener
     */
    public static void createOrganization(final Organization organization, final Context context, final OnCompletedListener listener, final boolean update) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Generate a key

                if (!update) {
                    String uuid = UUID.randomUUID().toString();
                    while (dataSnapshot.hasChild(uuid)) {
                        uuid = UUID.randomUUID().toString();
                    }

                    final String uid = uuid;


                    uploadImage(organization.getImage(), uid, new OnUploadListener() {
                        @Override
                        public void onUploadSuccessful(String url) {

                            User user = FundMe.userDataManager.getUser();

                            organization.setUid(uid);
                            organization.setUserUID(user.uid);
                            organization.setImageURL(url);

                            user.addOrganization(organization);

                            reference.child(uid).setValue(new DatabaseOrganization(organization));

                            userReference.child(user.getUid()).setValue(new DatabaseUser(user));

                            FundMe.userDataManager.updateUser(user);

                            LocalDatabaseManager manager = new LocalDatabaseManager(context);
                            manager.addOrganization(organization);

                            listener.onServiceSuccessful();
                        }

                        @Override
                        public void onUploadFailed() {

                        }
                    }, "organization");

                } else {
                    uploadImage(organization.getImage(), organization.getUid(), new OnUploadListener() {
                        @Override
                        public void onUploadSuccessful(String url) {
                            organization.setImageURL(url);
                            reference.child(organization.getUid()).setValue(new DatabaseOrganization(organization));

                            LocalDatabaseManager manager = new LocalDatabaseManager(context);
                            manager.updateOrganization(organization);

                            listener.onServiceSuccessful();
                        }

                        @Override
                        public void onUploadFailed() {

                        }
                    }, "item");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Couldn't create this item", Toast.LENGTH_LONG).show();
                listener.onServiceFailed();
            }
        });
    }

    /**
     * Creates an item in the firebase database, and updates data
     *
     * @param item    Item data object to create
     * @param context Application context
     * @param dialog  Callback listener
     */
    public static void createItem(final Item item, final Context context, final OnCompletedListener dialog, final boolean update) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Generate a key
                if (!update) {
                    String uuid = UUID.randomUUID().toString();
                    while (dataSnapshot.hasChild(uuid)) {
                        uuid = UUID.randomUUID().toString();
                    }

                    final String uid = uuid;


                    uploadImage(item.getImage(), uid, new OnUploadListener() {
                        @Override
                        public void onUploadSuccessful(String url) {

                            User user = FundMe.userDataManager.getUser();

                            item.setUid(uid);
                            item.setUserUID(user.uid);
                            item.setImageURL(url);

                            user.addItem(item);

                            reference.child(uid).setValue(new DatabaseItem(item));

                            userReference.child(user.getUid()).setValue(new DatabaseUser(user));

                            FundMe.userDataManager.updateUser(user);

                            LocalDatabaseManager manager = new LocalDatabaseManager(context);
                            manager.addItem(item);

                            dialog.onServiceSuccessful();
                        }

                        @Override
                        public void onUploadFailed() {

                        }
                    }, "item");
                } else {
                    uploadImage(item.getImage(), item.getUid(), new OnUploadListener() {
                        @Override
                        public void onUploadSuccessful(String url) {
                            item.setImageURL(url);
                            reference.child(item.getUid()).setValue(new DatabaseItem(item));

                            LocalDatabaseManager manager = new LocalDatabaseManager(context);
                            manager.updateItem(item);

                            dialog.onServiceSuccessful();
                        }

                        @Override
                        public void onUploadFailed() {

                        }
                    }, "item");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Couldn't create this item", Toast.LENGTH_LONG).show();

                dialog.onServiceFailed();
            }
        });
    }

    public static Item addViewedItem(final Item item, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);

        item.setViewed(item.getViewed() + 1);

        reference.child(item.getUid()).setValue(new DatabaseItem(item));

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateItem(item);

        return item;
    }

    public static void createComment(final Item item, final Comment comment, final Context context, final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);

        item.addComment(comment);

        reference.child(item.getUid()).setValue(new DatabaseItem(item));

        LocalDatabaseManager manager = new LocalDatabaseManager(context);
        manager.updateItem(item);

        listener.onDownloadSuccessful(item);
    }

    public static void getItem(final String uid, final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean used = false;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.hasChild("uid") && data.child("uid").getValue().toString().equals(uid)) {
                        try {
                            final String title = data.child("title").getValue().toString();
                            final String description = data.child("description").getValue().toString();
                            final double price = Double.parseDouble(data.child("price").getValue().toString());
                            final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                            final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                            final String userUID = data.child("userUID").getValue().toString();
                            final String imageURL = data.child("imageURL").getValue().toString();
                            final Bitmap image = ServiceManager.ImageHelper.getBitmapFromUrl(imageURL);
                            final List<String> tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            int viewed = data.child("viewed").getValue(new GenericTypeIndicator<Integer>() {
                            });
                            final List<String> itemsBought = data.child("buyRequests").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            final boolean sold = data.child("sold").getValue(new GenericTypeIndicator<Boolean>() {
                            });
                            final List<Comment> comments = data.child("comments").getValue(new GenericTypeIndicator<List<Comment>>() {
                            });
                            final int condition = Integer.parseInt(data.child("condition").getValue().toString());

                            Item o = new Item(title, description, price, zipCode, dateCreated, image, tags, loved, viewed, itemsBought, sold, comments, condition);
                            o.setUid(uid);
                            o.setUserUID(userUID);
                            o.setImageURL(imageURL);

                            reference.child(o.getUid()).setValue(new DatabaseItem(o));

                            listener.onDownloadSuccessful(o);
                            used = true;
                        } catch (IOException | InterruptedException | ExecutionException ex) {
                            listener.onDownloadFailed(ex.getMessage());
                        }
                    }
                }

                if (!used) {
                    listener.onDownloadSuccessful("Couldn't find the item");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static Item likeItem(final Item item, final OnCompletedListener listener, final Context context, final boolean like) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);

        List<String> loved = item.getLoved();

        if (like) {
            loved.add(FundMe.userDataManager.getUser().getUid());
        } else {
            loved.remove(FundMe.userDataManager.getUser().getUid());
        }

        item.setLoved(loved);
        reference.child(item.getUid()).setValue(new DatabaseItem(item));

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateItem(item);

        if (listener != null) {
            listener.onServiceSuccessful();
        }

        return item;
    }

    public static Organization addViewedOrganization(final Organization organization, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);

        organization.setViewed(organization.getViewed() + 1);

        reference.child(organization.getUid()).setValue(new DatabaseOrganization(organization));

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateOrganization(organization);

        return organization;
    }

    public static void getOrganization(final String uid, final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean used = false;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.hasChild("uid") && data.child("uid").getValue().toString().equals(uid)) {
                        try {
                            final String title = data.child("title").getValue().toString();
                            final String description = data.child("description").getValue().toString();
                            final double price = Double.parseDouble(data.child("price").getValue().toString());
                            final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                            final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                            final String userUID = data.child("userUID").getValue().toString();
                            final String imageURL = data.child("imageURL").getValue().toString();
                            final Bitmap image = ServiceManager.ImageHelper.getBitmapFromUrl(imageURL);
                            final String link = data.child("link").getValue().toString();
                            final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            int viewed = data.child("viewed").getValue(new GenericTypeIndicator<Integer>() {
                            });
                            final List<String> members = data.child("members").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            final int moneyRaised = data.child("moneyRaised").getValue(new GenericTypeIndicator<Integer>() {
                            });

                            Organization o = new Organization(title, description, price, zipCode, dateCreated, image, link, loved, viewed, members, moneyRaised);
                            o.setUid(uid);
                            o.setUserUID(userUID);
                            o.setImageURL(imageURL);

                            listener.onDownloadSuccessful(o);
                            used = true;
                        } catch (IOException | InterruptedException | ExecutionException ex) {
                            listener.onDownloadFailed(ex.getMessage());
                        }
                    }
                }

                if (!used) {
                    listener.onDownloadSuccessful("Couldn't find the item");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static Organization likeOrganization(final Organization organization, final OnCompletedListener listener, final Context context, final boolean like) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);

        List<String> loved = organization.getLoved();

        if (like) {
            loved.add(FundMe.userDataManager.getUser().getUid());
        } else {
            loved.remove(FundMe.userDataManager.getUser().getUid());
        }

        organization.setLoved(loved);
        reference.child(organization.getUid()).setValue(new DatabaseOrganization(organization));

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateOrganization(organization);

        if (listener != null) {
            listener.onServiceSuccessful();
        }

        return organization;
    }

    public static Organization joinOrganization(final Organization organization, final Context context, final boolean join) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);
        final User user = FundMe.userDataManager.getUser();

        List<String> members = organization.getMembers();
        List<String> joinedOrganizations = user.getJoinedOrganizations();

        if (join) {
            // Join the organization
            members.add(user.getUid());
            joinedOrganizations.add(organization.getUid());
        } else {
            // Leave the organization
            members.remove(user.getUid());
            joinedOrganizations.remove(organization.getUid());
        }

        organization.setMembers(members);
        user.setJoinedOrganizations(joinedOrganizations);

        reference.child(organization.getUid()).setValue(new DatabaseOrganization(organization));
        updateUser(user);

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateOrganization(organization);

        return organization;
    }

    public static void updateUser(final User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

        reference.child(user.getUid()).setValue(new DatabaseUser(user));
        FundMe.userDataManager.updateUser(user);
    }

    public static void updateUser(final User user, final OnCompletedListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

        reference.child(user.getUid()).setValue(new DatabaseUser(user)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FundMe.userDataManager.updateUser(user);

                    listener.onServiceSuccessful();
                } else {
                    listener.onServiceFailed();
                }
            }
        });
    }

    public static void removeNotification(Notification notification) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        User user = FundMe.userDataManager.getUser();

        List<Notification> notifications = new ArrayList<>(user.getNotifications());
        notifications.remove(notification);

        user.setNotifications(notifications);

        updateUser(user);
    }

    public static void getAllOrganizations(final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Organization> organizations = new ArrayList<Organization>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String uid = data.child("uid").getValue().toString();
                    try {
                        final String title = data.child("title").getValue().toString();
                        final String description = data.child("description").getValue().toString();
                        final double price = Double.parseDouble(data.child("price").getValue().toString());
                        final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                        final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                        final String userUID = data.child("userUID").getValue().toString();
                        final String link = data.child("link").getValue().toString();
                        final String imageURL = data.child("imageURL").getValue().toString();
                        final Bitmap image = ServiceManager.ImageHelper.getBitmapFromUrl(imageURL);
                        final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        int viewed = data.child("viewed").getValue(new GenericTypeIndicator<Integer>() {
                        });
                        final List<String> members = data.child("members").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        Organization o = new Organization(title, description, price, zipCode, dateCreated, image, link, loved, viewed, members, 0);
                        o.setUid(uid);
                        o.setUserUID(userUID);
                        o.setImageURL(imageURL);

                        organizations.add(o);
                    } catch (IOException | InterruptedException | ExecutionException ignored) {
                    }
                }

                listener.onDownloadSuccessful(organizations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onDownloadFailed("Error happened");
            }
        });
    }

    public static void revokeItem(final DatabaseUser dbUser, final DatabaseItem dbItem, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference itemReference = database.getReference(ITEM_DATABASE_REF);
        LocalDatabaseManager manager = new LocalDatabaseManager(context);

        final User user = new User(dbUser);

        ArrayList<Item> items = manager.getAllItems();
        Item item = null;

        for (Item i : items) {
            if (i.getUid().equals(dbItem.uid)) {
                item = i;
            }
        }

        if (item != null) {
            try {
                List<String> userItemsBought = dbUser.itemsBought;
                userItemsBought.remove(item.uid);
                user.setItemsBought(userItemsBought);

                List<String> buyRequests = new ArrayList<>(item.getBuyRequests());
                buyRequests.remove(user.getUid());
                item.setBuyRequests(buyRequests);

                final String title = FundMe.userDataManager.getUser().getName() + " has revoked your request for \"" + item.getTitle() + "\"";

                itemReference.child(item.getUid()).setValue(new DatabaseItem(item));

                manager.updateItem(item);


                final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot = dataSnapshot.child(user.getUid());

                        Bundle data = new Bundle();

                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String key = d.getKey();

                            if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                                };
                                data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                            } else {
                                data.putString(key, d.getValue().toString());
                            }
                        }
                        final User u = new User(user.getUid(), data);
                        u.addNotification(new Notification(FundMe.userDataManager.getUser().getProfilePic(), title, Notification.NotificationType.INFO, false, dbUser, dbItem));

                        u.removeItemBought(dbItem.uid);

                        reference.child(user.getUid()).setValue(new DatabaseUser(u));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                User u = FundMe.userDataManager.getUser();
                List<Notification> notifications = u.getNotifications();

                for (Notification n : notifications) {
                    if (n.getUser().uid.equals(dbUser.uid) && n.getItem().uid.equals(dbItem.uid)) {
                        notifications.remove(n);
                    }
                }
                u.setNotifications(notifications);

                updateUser(u);
            } catch (Exception ex) {
                String message = ex.getMessage();
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void getAllItems(final OnDownloadListener listener, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Item> items = new ArrayList<Item>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String uid = data.child("uid").getValue().toString();
                    try {
                        final String title = data.child("title").getValue().toString();
                        final String description = data.child("description").getValue().toString();
                        final double price = Double.parseDouble(data.child("price").getValue().toString());
                        final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                        final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                        final String userUID = data.child("userUID").getValue().toString();
                        final String imageURL = data.child("imageURL").getValue().toString();
                        final Bitmap image = null;
                        final List<String> tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        final int viewed = Integer.parseInt(data.child("viewed").getValue().toString());
                        final List<String> itemsBought = data.child("buyRequests").getValue(new GenericTypeIndicator<List<String>>() {
                        });
                        final List<Comment> comments = data.child("comments").getValue(new GenericTypeIndicator<List<Comment>>() {
                        });
                        final int condition = Integer.parseInt(data.child("condition").getValue().toString());

                        Item o = new Item(title, description, price, zipCode, dateCreated, image, tags, loved, viewed, itemsBought, false, comments, condition);
                        o.setUid(uid);
                        o.setUserUID(userUID);
                        o.setImageURL(imageURL);

                        items.add(o);
                    } catch (Exception ignored) {

                    }
                }

                LocalDatabaseManager manager = new LocalDatabaseManager(context);
                manager.resetItems();

                for (Item item : items) {
                    manager.addItem(item);
                }

                listener.onDownloadSuccessful(items);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Updates the user information from the database
     *
     * @param myUser   temporary user to download data from
     * @param listener Callback listener
     */
    static void downloadUserDBData(final User myUser, final OnAuthenticatedListener listener, final boolean resume, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(USER_DATABASE_REF);
        final DatabaseReference organizationReference = database.getReference(ORGANIZATION_DATABASE_REF);
        final DatabaseReference itemReference = database.getReference(ITEM_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(myUser.getUid())) {
                    // At this point, the program has confirmed that this is an existing user, and that the data will be secured from the database
                    dataSnapshot = dataSnapshot.child(myUser.getUid());

                    Bundle data = new Bundle();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        String key = d.getKey();

                        if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                            GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                            };
                            data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                        } else {
                            data.putString(key, d.getValue().toString());
                        }
                    }
                    final User u = new User(myUser.getUid(), data);
                    u.setLastLoggedIn(String.valueOf(System.currentTimeMillis()));

                    if (!resume) {
                        downloadImage(myUser.getUid(), new OnDownloadListener() {
                            @Override
                            public void onDownloadSuccessful(Object data) {
                                u.setProfilePicture(ServiceManager.ImageHelper.encodeToBase64((Bitmap) data));
                                updateUser(u);
                                saveSearchData(listener, reference, organizationReference, itemReference, new LocalDatabaseManager(context), u);
                            }

                            @Override
                            public void onDownloadFailed(String message) {
                                listener.onAuthenticationFailed("Could not download your profile picture");
                            }
                        }, "profile");
                    } else {
                        updateUser(u);
                        saveSearchData(listener, reference, organizationReference, itemReference, new LocalDatabaseManager(context), u);
                    }
                } else {
                    // This user is brand new, and will be added to the existing database
                    myUser.setLastLoggedIn(String.valueOf(System.currentTimeMillis()));
                    reference.child(myUser.getUid()).setValue(new DatabaseUser(myUser));

                    ServiceManager.ImageHelper.getBitmapFromUrl(myUser.getProfilePic(), new OnDownloadListener() {
                        @Override
                        public void onDownloadSuccessful(Object data) {
                            final Bitmap profilePic = (Bitmap) data;

                            uploadImage(profilePic, myUser.getUid(), new OnUploadListener() {
                                @Override
                                public void onUploadSuccessful(String url) {
                                    myUser.setProfilePicture(ServiceManager.ImageHelper.encodeToBase64(profilePic));
                                    listener.onAuthenticationSuccessful(myUser);
                                }

                                @Override
                                public void onUploadFailed() {
                                    reference.child(myUser.getUid()).removeValue();
                                    listener.onAuthenticationFailed("Could not upload your desired profile picture");
                                }
                            }, "profile");
                        }

                        @Override
                        public void onDownloadFailed(String message) {
                            listener.onAuthenticationFailed("Could not retrieve your desired profile picture");
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onAuthenticationFailed(databaseError.getMessage());
            }
        });

    }

    /**
     * Retrieves all user, item, and organization information from the firebase database. {name, uid}
     *
     * @param listener              Callback listener
     * @param userReference         Database reference for users
     * @param organizationReference Database reference for organizations
     * @param itemReference         Database reference for items
     * @param manager               Local database managing system
     * @param user                  User which has been retrieved form the download operation
     */
    private static void saveSearchData(final OnAuthenticatedListener listener, DatabaseReference userReference, final DatabaseReference organizationReference,
                                       final DatabaseReference itemReference, final LocalDatabaseManager manager, final User user) {
        // Save Item and Organization uids as search items 
        manager.reset();
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String name = data.child("firstName").getValue().toString() + " " + data.child("lastName").getValue().toString();
                    String uid = data.child("uid").getValue().toString();
                    SearchItem.SearchItemType type = SearchItem.SearchItemType.PERSON;
                    List<String> tags;

                    tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                    });
                    if (tags == null) {
                        tags = new ArrayList<>();
                    }
                    SearchItem item = new SearchItem(0, name, uid, tags, type);

                    manager.addSearchItem(item);
                }
                organizationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String name = data.child("title").getValue().toString();
                            String uid = data.child("uid").getValue().toString();
                            SearchItem.SearchItemType type = SearchItem.SearchItemType.ORGANIZATION;
                            List<String> tags;

                            tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                            });
                            if (tags == null) {
                                tags = new ArrayList<>();
                            }
                            SearchItem item = new SearchItem(0, name, uid, tags, type);

                            manager.addSearchItem(item);
                        }

                        itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    String name = data.child("title").getValue().toString();
                                    String uid = data.child("uid").getValue().toString();
                                    SearchItem.SearchItemType type = SearchItem.SearchItemType.ITEM;
                                    List<String> tags;

                                    tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                                    });
                                    if (tags == null) {
                                        tags = new ArrayList<>();
                                    }
                                    SearchItem item = new SearchItem(0, name, uid, tags, type);

                                    manager.addSearchItem(item);
                                    List<SearchItem> items = manager.getAllSearchItems();
                                    items.size();
                                }

                                listener.onAuthenticationSuccessful(user);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                listener.onAuthenticationFailed("Could not load information");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onAuthenticationFailed("Could not load information");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onAuthenticationFailed("Could not load information");
            }
        });

    }

    /**
     * Saves user's items and organizations to the local database
     *
     * @param context  Application context
     * @param listener Callback listener
     */
    public static void saveItemsAndOrganizations(final Context context, OnCompletedListener listener) {
        new SaveToDatabaseTask(context, listener, FundMe.userDataManager.getUser()).execute();
    }

    /**
     * Uploads image to the server
     *
     * @param image    Bitmap representation of the image
     * @param uid      String unique id of the object that is being represented by the image
     * @param listener Callback listener
     */
    private static void uploadImage(Bitmap image, String uid, final OnUploadListener listener, String prefix) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl(STORAGE_REF);
        final StorageReference profileRef = storageRef.child("server/fundme/images/" + prefix + "-" + uid);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte data[] = baos.toByteArray();

        UploadTask uploadTask = profileRef.putBytes(data);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    profileRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            listener.onUploadSuccessful(task.getResult().toString());

                        }
                    });
                } else {
                    listener.onUploadFailed();
                }
            }
        });
    }

    /**
     * Downloads image from the server
     *
     * @param uid      String unique id of the object is being represented by the image
     * @param listener Callback listener
     * @param prefix   type of the image that is being represented by the iamge
     */
    private static void downloadImage(String uid, final OnDownloadListener listener, String prefix) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl(STORAGE_REF);
        StorageReference profileRef = storageRef.child("server/fundme/images/" + prefix + "-" + uid);

        final long ONE_MEGABYTE = 1024 * 1024;
        profileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                listener.onDownloadSuccessful(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                listener.onDownloadFailed(exception.getMessage());
            }
        });
    }

    public static Item createItemRequest(final Item item, Context context) {
        final User user = FundMe.userDataManager.getUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        List<String> items = item.getBuyRequests();
        List<String> userItems = user.getItemsBought();

        items.add(FundMe.userDataManager.getUser().getUid());
        userItems.add(item.getUid());

        item.setBuyRequests(items);
        user.setItemsBought(userItems);

        reference.child(item.getUid()).setValue(new DatabaseItem(item));
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot = dataSnapshot.child(item.getUserUID());

                Bundle data = new Bundle();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String key = d.getKey();

                    if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                        };
                        data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                    } else {
                        data.putString(key, d.getValue().toString());
                    }
                }
                final User u = new User(item.getUserUID(), data);

                List<Notification> notifications = u.getNotifications();
                notifications.add(new Notification(user.getProfilePic(), user.getName() + " has requested to buy \"" + item.getTitle() + "\" from you", Notification.NotificationType.ACCEPT, false,
                        new DatabaseUser(user, true), new DatabaseItem(item)));
                u.setNotifications(notifications);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

                reference.child(u.getUid()).setValue(new DatabaseUser(u));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateItem(item);

        updateUser(user);


        return item;
    }

    public static Item removeItemRequest(final Item item, Context context) {
        final User user = FundMe.userDataManager.getUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(ITEM_DATABASE_REF);
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        List<String> items = item.getBuyRequests();
        List<String> userItems = user.getItemsBought();

        items.remove(FundMe.userDataManager.getUser().getUid());
        userItems.remove(item.getUid());

        item.setBuyRequests(items);
        user.setItemsBought(userItems);

        reference.child(item.getUid()).setValue(new DatabaseItem(item));

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(context);
        databaseManager.updateItem(item);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot = dataSnapshot.child(item.getUserUID());

                Bundle data = new Bundle();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String key = d.getKey();

                    if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                        };
                        data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                    } else {
                        data.putString(key, d.getValue().toString());
                    }
                }
                final User u = new User(item.getUserUID(), data);

                List<Notification> notifications = u.getNotifications();
                notifications.add(new Notification(user.getProfilePic(), user.getName() + " does not want to buy \"" + item.getTitle() + "\" anymore", Notification.NotificationType.INFO, false,
                        new DatabaseUser(user, true), new DatabaseItem(item)));
                u.setNotifications(notifications);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

                reference.child(u.getUid()).setValue(new DatabaseUser(u));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        updateUser(user);

        return item;
    }

    public static void getUsers(final ArrayList<String> userUids, final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference(USER_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> mUsers = new ArrayList<User>();

                for (String u : userUids) {
                    DataSnapshot data = dataSnapshot.child(u);

                    Bundle userData = new Bundle();

                    for (DataSnapshot d : data.getChildren()) {
                        String key = d.getKey();

                        if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                            GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                            };
                            userData.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                        } else {
                            userData.putString(key, d.getValue().toString());
                        }
                    }

                    final User user = new User(u, userData);

                    mUsers.add(user);
                }

                listener.onDownloadSuccessful(mUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onDownloadFailed(databaseError.getMessage());
            }
        });
    }

    public static void donate(final Notification notification, final Organization o, final Context context, final OnCompletedListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);
        final DatabaseReference itemReference = database.getReference(ITEM_DATABASE_REF);
        final DatabaseReference organizationReference = database.getReference(ORGANIZATION_DATABASE_REF);

        // Deduct credits from buyer
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot = dataSnapshot.child(notification.getUser().uid);

                Bundle data = new Bundle();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String key = d.getKey();

                    if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                        };
                        data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                    } else {
                        data.putString(key, d.getValue().toString());
                    }
                }
                final User u = new User(notification.getUser().uid, data);
                final String title = "You have successfully bought the \"" + notification.getItem().title + "\"";
                u.addNotification(new Notification(FundMe.userDataManager.getUser().getProfilePic(), title,
                        Notification.NotificationType.INFO, false, notification.getUser(), notification.getItem()));

                u.setVirtualMoney(u.getVirtualMoney() - notification.getItem().price);

                userReference.child(notification.getUser().uid).setValue(new DatabaseUser(u));

                // Remove current user notification
                removeNotification(notification);

                //Sell item
                final LocalDatabaseManager manager = new LocalDatabaseManager(context);
                ArrayList<Item> items = manager.getAllItems();
                Item item = null;

                for (Item i : items) {
                    if (i.getUid().equals(notification.getItem().uid)) {
                        item = i;
                    }
                }

                final Item mItem = item;

                if (mItem != null) {
                    mItem.sold = true;

                    itemReference.child(mItem.uid).setValue(new DatabaseItem(mItem));
                    new LocalDatabaseManager(context).updateItem(mItem);

                    getAllOrganizations(new OnDownloadListener() {
                        @Override
                        public <D> void onDownloadSuccessful(D data) {
                            Organization organization = null;

                            for (Organization i : ((ArrayList<Organization>) data)) {
                                if (i.getUid().equals(o.uid)) {
                                    organization = i;
                                }
                            }

                            if (organization != null) {
                                organization.moneyRaised += mItem.price;

                                organizationReference.child(organization.uid).setValue(new DatabaseOrganization(organization));
                                manager.updateOrganization(organization);
                            }

                            listener.onServiceSuccessful();

                        }

                        @Override
                        public void onDownloadFailed(String message) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static void purchaseCredits(int numberOfCredits, final OnDownloadListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        final User user = FundMe.userDataManager.getUser();
        user.setVirtualMoney(user.getVirtualMoney() + numberOfCredits);
        DatabaseUser dbUser = new DatabaseUser(user);

        userReference.child(dbUser.uid).setValue(dbUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FundMe.userDataManager.updateUser(user);
                    listener.onDownloadSuccessful(0);
                } else {
                    listener.onDownloadFailed(task.getException().getMessage());
                }
            }
        });

    }

    public static void getMembersFromOrganization(final String organizationUID, final OnDownloadListener onDownloadListener) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(ORGANIZATION_DATABASE_REF);
        final DatabaseReference userReference = database.getReference(USER_DATABASE_REF);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> memberUIDs = dataSnapshot.child(organizationUID).child("members").getValue(new GenericTypeIndicator<List<String>>() {
                });
                final List<User> members = new ArrayList<User>();

                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot datasnapshot) {
                        for (String uid : memberUIDs) {

                            DataSnapshot dataSnapshot = datasnapshot.child(uid);

                            if (dataSnapshot != null && dataSnapshot.exists()) {
                                Bundle data = new Bundle();

                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    String key = d.getKey();

                                    if (key.equals("itemsBought") || key.equals("organizationUids") || key.equals("itemUids") || key.equals("joinedOrganizations") || key.equals("notifications")) {
                                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                                        };
                                        data.putStringArrayList(key, (ArrayList<String>) d.getValue(t));
                                    } else {
                                        data.putString(key, d.getValue().toString());
                                    }
                                }
                                final User u = new User(uid, data);
                                members.add(u);
                            }
                        }

                        onDownloadListener.onDownloadSuccessful(members);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDownloadListener.onDownloadFailed(databaseError.getMessage());
            }
        });

    }

    /**
     * Saves user items and organizations to the local database
     * <p>
     * NOTE: may be time consuming, depending on picture data
     */
    private static class SaveToDatabaseTask extends AsyncTask<String, String, String> {

        private OnCompletedListener listener;
        private User user;
        private Context context;

        SaveToDatabaseTask(Context context, OnCompletedListener listener, User user) {
            this.listener = listener;
            this.user = user;
            this.context = context;
        }


        @Override
        protected String doInBackground(String... strings) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference organizationReference = database.getReference(ORGANIZATION_DATABASE_REF);
            final DatabaseReference itemReference = database.getReference(ITEM_DATABASE_REF);

            final LocalDatabaseManager manager = new LocalDatabaseManager(context);

            organizationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String uid = data.child("uid").getValue().toString();
                            try {
                                final String title = data.child("title").getValue().toString();
                                final String description = data.child("description").getValue().toString();
                                final double price = Double.parseDouble(data.child("price").getValue().toString());
                                final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                                final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                                final String userUID = data.child("userUID").getValue().toString();
                                final String link = data.child("link").getValue().toString();
                                final String imageURL = data.child("imageURL").getValue().toString();
                                final Bitmap image = ServiceManager.ImageHelper.getBitmapFromUrl(imageURL);
                                final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                                });
                                int viewed = data.child("viewed").getValue(new GenericTypeIndicator<Integer>() {
                                });
                                final List<String> members = data.child("members").getValue(new GenericTypeIndicator<List<String>>() {
                                });
                                Organization o = new Organization(title, description, price, zipCode, dateCreated, image, link, loved, viewed, members, 0);
                                o.setUid(uid);
                                o.setUserUID(userUID);
                                o.setImageURL(imageURL);

                                manager.addOrganization(o);
                            } catch (IOException | InterruptedException | ExecutionException ignored) {
                            }

                    }

                    itemReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                String uid = data.child("uid").getValue().toString();
                                    try {
                                        final String title = data.child("title").getValue().toString();
                                        final String description = data.child("description").getValue().toString();
                                        final double price = Double.parseDouble(data.child("price").getValue().toString());
                                        final int zipCode = Integer.parseInt(data.child("zipCode").getValue().toString());
                                        final long dateCreated = Long.parseLong(data.child("dateCreated").getValue().toString());
                                        final String userUID = data.child("userUID").getValue().toString();
                                        final String imageURL = data.child("imageURL").getValue().toString();
                                        final Bitmap image = ServiceManager.ImageHelper.getBitmapFromUrl(imageURL);
                                        final List<String> tags = data.child("tags").getValue(new GenericTypeIndicator<List<String>>() {
                                        });
                                        final List<String> loved = data.child("loved").getValue(new GenericTypeIndicator<List<String>>() {
                                        });
                                        final int viewed = Integer.parseInt(data.child("viewed").getValue().toString());
                                        final List<String> itemsBought = data.child("buyRequests").getValue(new GenericTypeIndicator<List<String>>() {
                                        });
                                        final List<Comment> comments = data.child("comments").getValue(new GenericTypeIndicator<List<Comment>>() {
                                        });
                                        final int condition = Integer.parseInt(data.child("condition").getValue().toString());

                                        Item o = new Item(title, description, price, zipCode, dateCreated, image, tags, loved, viewed, itemsBought, false, comments, condition);
                                        o.setUid(uid);
                                        o.setUserUID(userUID);
                                        o.setImageURL(imageURL);

                                        manager.addItem(o);
                                    } catch (IOException | InterruptedException | ExecutionException ignored) {
                                    }

                            }

                            listener.onServiceSuccessful();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onServiceFailed();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listener.onServiceFailed();
                }
            });


            return null;
        }
    }

}
