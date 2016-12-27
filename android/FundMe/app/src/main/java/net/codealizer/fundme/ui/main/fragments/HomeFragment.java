package net.codealizer.fundme.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.lapism.searchview.SearchView;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.ui.main.CreateOrganizationActivity;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.util.firebase.AuthenticationManager;
import net.codealizer.fundme.util.DataManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.listeners.OnAuthenticatedListener;
import net.codealizer.fundme.util.listeners.OnCompletedListener;

import java.util.List;


/**
 * Created by Pranav on 11/26/16.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, FloatingSearchView.OnQueryChangeListener, SwipeRefreshLayout.OnRefreshListener, OnAuthenticatedListener, FloatingSearchView.OnSearchListener {

    FloatingActionButton newItem;
    FloatingActionButton newOrganization;
    FloatingActionsMenu addItem;

    SwipeRefreshLayout swipeRefreshLayout;

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
    }


    private void initialize() {
        newItem = (FloatingActionButton) getView().findViewById(R.id.home_create_new_item_button);
        newOrganization = (FloatingActionButton) getView().findViewById(R.id.home_create_new_organization_button);
        addItem = (FloatingActionsMenu) getView().findViewById(R.id.home_create_new_button);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.home_swipe_layout);


        newItem.setOnClickListener(this);
        newOrganization.setOnClickListener(this);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.yellow_primary, R.color.md_red_400);
        swipeRefreshLayout.setOnRefreshListener(this);
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
                AlertDialogManager.showMessageSnackbar(addItem, "Refreshed data!");

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onServiceFailed() {
                onAuthenticationFailed("Could not refresh");
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
}
