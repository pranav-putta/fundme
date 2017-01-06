package net.codealizer.fundme.ui.main.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.DatabaseItem;
import net.codealizer.fundme.assets.DatabaseUser;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Notification;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.ui.util.CircleTransform;
import net.codealizer.fundme.util.SendMail;
import net.codealizer.fundme.util.db.LocalDatabaseManager;
import net.codealizer.fundme.util.firebase.AuthenticationManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Pranav on 12/27/16.
 */

public class NotificationsFragment extends Fragment implements OnDownloadListener, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView notificationsList;
    private TextView notFound;
    private List<Notification> notifications;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        notificationsList = (RecyclerView) getView().findViewById(R.id.notificationsList);
        notFound = (TextView) getView().findViewById(R.id.search_no_results);
        refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.notifications_refresh);

        LocalDatabaseManager databaseManager = new LocalDatabaseManager(getActivity());
        ArrayList<Item> items = databaseManager.getAllItems();

        // Notifications about buy requests
        ArrayList<String> userUids = new ArrayList<>();

        for (Item item : items) {
            for (String uid : item.getBuyRequests()) {
                userUids.add(uid);
            }
        }

        DatabaseManager.getUsers(userUids, this);

        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public <D> void onDownloadSuccessful(D data) {
        notifications = FundMe.userDataManager.getUser().getNotifications();

        final NotificationsAdapter adapter = new NotificationsAdapter(notifications);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        notificationsList.setLayoutManager(layoutManager);
        notificationsList.setItemAnimator(new DefaultItemAnimator());
        notificationsList.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new NotificationTouchHelper(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(notificationsList);

        if (notifications.size() > 0) {
            notificationsList.setVisibility(View.VISIBLE);
            notFound.setVisibility(View.GONE);
        } else {
            notificationsList.setVisibility(View.GONE);
            notFound.setVisibility(View.VISIBLE);
        }
    }

    public void sendAddressEmail(User u, DatabaseUser u1, DatabaseItem i, Organization o) {
        SendMail mail = new SendMail(getContext(), u, u1, o, i, true);
        mail.execute();

        mail = new SendMail(getContext(), u, u1, o, i, false);
        mail.execute();
    }

    @Override
    public void onDownloadFailed(String message) {
        AlertDialogManager.showMessageSnackbar(notificationsList, "Couldn't access your notifications");
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private void refresh() {
        final ProgressDialog progressDialog = AlertDialogManager.showProgressDialog(getActivity());

        AuthenticationManager.refreshLogin(FundMe.userDataManager.getUser().uid, new OnAuthenticatedListener() {
            @Override
            public void onAuthenticationSuccessful(User data) {
                DatabaseManager.saveItemsAndOrganizations(getActivity(), new OnCompletedListener() {
                    @Override
                    public void onServiceSuccessful() {
                        AlertDialogManager.showMessageSnackbar(refreshLayout, "Refreshed data!");

                        initialize();

                        refreshLayout.setRefreshing(false);
                        progressDialog.hide();
                    }

                    @Override
                    public void onServiceFailed() {
                        onAuthenticationFailed("Could not refresh");
                    }
                });
            }

            @Override
            public void onAuthenticationFailed(String message) {
                progressDialog.hide();

                AlertDialogManager.showMessageSnackbar(refreshLayout, "Couldn't refresh data");
            }

            @Override
            public void onNetworkError() {
                AlertDialogManager.showMessageSnackbar(refreshLayout, "No internet connection");
            }
        }, getActivity());
    }

    private class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        private List<Notification> items;
        private ProgressDialog progressDialog;

        public NotificationsAdapter(List<Notification> items) {
            this.items = items;
        }

        public void remove(int pos) {
            DatabaseManager.removeNotification(items.get(pos));
            items.remove(pos);
            notifyItemRemoved(pos);

        }

        @Override
        public NotificationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_notifications, parent, false);

            return new NotificationsAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(NotificationsAdapter.ViewHolder holder, final int position) {
            holder.title.setText(items.get(position).getDescription());
            Glide.with(getActivity()).load(items.get(position).getImageURL())
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(getActivity()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                    intent.putExtra(ViewItemActivity.KEY_ITEM_UID, items.get(position).getItem().uid);
                    startActivity(intent);
                }
            });
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialogManager.showAcceptOrganizationDialog(getActivity(), new OnDownloadListener() {
                        @Override
                        public <D> void onDownloadSuccessful(D data) {
                            final Organization o = (Organization) data;

                            DatabaseManager.donate(items.get(position), o, getActivity(), new OnCompletedListener() {
                                @Override
                                public void onServiceSuccessful() {
                                    refresh();
                                    AlertDialogManager.showMessageDialog("Success", "Your item has been sold successfully! The address and shipping information of the recipent will " +
                                            "be emailed to " + FundMe.userDataManager.getUser().getEmail() + "", getActivity());
                                    sendAddressEmail(FundMe.userDataManager.getUser(), items.get(position).getUser(), items.get(position).getItem(), o);
                                }


                                @Override
                                public void onServiceFailed() {

                                }
                            });
                        }

                        @Override
                        public void onDownloadFailed(String message) {

                        }
                    });
                }
            });
            holder.revoke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseManager.revokeItem(items.get(position).getUser(), items.get(position).getItem(), getActivity());
                    refresh();
                }
            });

            if (items.get(position).getType() == Notification.NotificationType.ACCEPT) {
                holder.accept.setVisibility(View.VISIBLE);
                holder.revoke.setVisibility(View.VISIBLE);
            } else if (items.get(position).getType() == Notification.NotificationType.INFO) {
                holder.accept.setVisibility(View.GONE);
                holder.revoke.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public ImageView image;
            public CardView card;
            public Button accept, revoke;

            public ViewHolder(View view) {
                super(view);

                title = (TextView) view.findViewById(R.id.card_notifications_description);

                image = (ImageView) view.findViewById(R.id.card_notifications_image);

                card = (CardView) view.findViewById(R.id.card_notifications);

                accept = (Button) view.findViewById(R.id.card_notifications_accept);
                revoke = (Button) view.findViewById(R.id.card_notifications_reject);
            }

        }
    }

    private class NotificationTouchHelper extends ItemTouchHelper.SimpleCallback {
        private NotificationsAdapter adapter;

        public NotificationTouchHelper(NotificationsAdapter adapter) {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //TODO: Not implemented here
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //Remove item
            if (notifications.get(viewHolder.getAdapterPosition()).getType() == Notification.NotificationType.INFO)
                adapter.remove(viewHolder.getAdapterPosition());
        }
    }
}
