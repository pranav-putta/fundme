package net.codealizer.fundme.ui.main.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.lapism.searchview.SearchView;

import net.codealizer.fundme.Config;
import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.ItemComparator;
import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.ui.main.CreateOrganizationActivity;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.ui.main.adapters.ItemsListDecoration;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.ui.util.CircleTransform;
import net.codealizer.fundme.util.db.LocalDatabaseManager;
import net.codealizer.fundme.util.firebase.AuthenticationManager;
import net.codealizer.fundme.util.DataManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by Pranav on 11/26/16.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, FloatingSearchView.OnQueryChangeListener, SwipeRefreshLayout.OnRefreshListener, OnAuthenticatedListener, FloatingSearchView.OnSearchListener {

    FloatingActionButton newItem;
    FloatingActionButton newOrganization;
    FloatingActionsMenu addItem;

    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView list;

    private List<Item> items;
    private ProgressDialog dialog;

    public SearchView searchView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();


        if (shouldRefresh()) {

            onRefresh();
        } else {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            ItemsListDecoration decoration = new ItemsListDecoration(16);
            list.setLayoutManager(layoutManager);
            list.addItemDecoration(decoration);
            items = new LocalDatabaseManager(getActivity()).getAllItems();
            list.setItemAnimator(new DefaultItemAnimator());
            list.setAdapter(new HomeAdapter(items));
        }
    }


    private void initialize() {
        newItem = (FloatingActionButton) getView().findViewById(R.id.home_create_new_item_button);
        newOrganization = (FloatingActionButton) getView().findViewById(R.id.home_create_new_organization_button);
        addItem = (FloatingActionsMenu) getView().findViewById(R.id.home_create_new_button);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.home_swipe_layout);

        list = (RecyclerView) getView().findViewById(R.id.home_items_list);

        newItem.setOnClickListener(this);
        newOrganization.setOnClickListener(this);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.yellow_primary, R.color.md_red_400);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private boolean shouldRefresh() {
        if (System.currentTimeMillis() - Config.lastRefresh > 5 * 60 * 1000) {
            Config.lastRefresh = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        Class activity = null;
        switch (view.getId()) {
            case R.id.home_create_new_item_button:
                activity = CreateItemActivity.class;
                break;
            case R.id.home_create_new_organization_button:
                activity = CreateOrganizationActivity.class;
                break;
        }

        if (activity != null) {
            Intent intent = new Intent(getActivity(), activity);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {

    }

    @Override
    public void onRefresh() {
        dialog = AlertDialogManager.showProgressDialog(getActivity());

        dialog.show();
        swipeRefreshLayout.setRefreshing(true);

        if (ServiceManager.isNetworkAvailable(getActivity())) {
            AuthenticationManager.refreshLogin(FundMe.userDataManager.getUser().uid, this, getActivity());
        } else {
            onNetworkError();
        }
    }

    @Override
    public void onAuthenticationSuccessful(User data) {
        DatabaseManager.saveItemsAndOrganizations(getActivity(), new OnCompletedListener() {
            @Override
            public void onServiceSuccessful() {
                LocalDatabaseManager localDatabaseManager = new LocalDatabaseManager(getActivity());
                items = localDatabaseManager.getAllItems();

                AlertDialogManager.showMessageSnackbar(addItem, "Refreshed data!");

                swipeRefreshLayout.setRefreshing(false);
                dialog.hide();
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                ItemsListDecoration decoration = new ItemsListDecoration(16);
                list.setLayoutManager(layoutManager);
                list.addItemDecoration(decoration);
                list.setItemAnimator(new DefaultItemAnimator());
                list.setAdapter(new HomeAdapter(items));
            }

            @Override
            public void onServiceFailed() {
                onAuthenticationFailed("Something went wrong :(");
            }
        });

    }


    @Override
    public void onAuthenticationFailed(String message) {
        AlertDialogManager.showMessageSnackbar(addItem, message);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNetworkError() {
        AlertDialogManager.showMessageSnackbar(addItem, "No internet connection!");

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        SearchItem item = (SearchItem) searchSuggestion;

        if (item.getType() == SearchItem.SearchItemType.ITEM) {
            Intent intent = new Intent(getActivity(), ViewItemActivity.class);
            intent.putExtra(ViewItemActivity.KEY_ITEM_UID, item.getUid());
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onSearchAction(String currentQuery) {

    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

        List<Item> mItems;

        public HomeAdapter(List<Item> items) {
            mItems = items;

            Collections.sort(mItems, new ItemComparator());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.card_home_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Item item = mItems.get(position);

            holder.title.setText(item.title);
            holder.backdrop.setColorFilter(Color.argb(50, 0, 0, 0));
            Glide.with(getActivity()).load(item.getImageURL())
                    .crossFade()
                    .thumbnail(0.5f)
                    .into(holder.backdrop);

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                    intent.putExtra(ViewItemActivity.KEY_ITEM_UID, item.getUid());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView title;
            private ImageView backdrop;
            private CardView card;

            public ViewHolder(View itemView) {
                super(itemView);

                title = (TextView) itemView.findViewById(R.id.card_home_item_title);
                backdrop = (ImageView) itemView.findViewById(R.id.card_home_item_backdrop);

                card = (CardView) itemView.findViewById(R.id.card_home_item);
            }
        }

    }
}
