package net.codealizer.fundme.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.main.ViewOrganizationActivity;
import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.ui.main.ViewItemActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/26/16.
 */

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView searchResults;
    private TextView noSearchResults;

    private List<SearchItem> mResults;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();

        initialize();
        if (mResults != null) {
            populateResults(mResults);
        }
    }

    private void initialize() {
        searchResults = (ListView) getView().findViewById(R.id.search_results);
        noSearchResults = (TextView) getView().findViewById(R.id.search_no_results);

        searchResults.setOnItemClickListener(this);
    }

    public void populateResults(List<SearchItem> r) {
        if (searchResults != null && noSearchResults != null) {
            mResults = r;

            ArrayList<String> data = new ArrayList<>();

            for (SearchItem item : mResults) {
                data.add(item.getName());
            }
            if (data.size() > 0) {
                searchResults.setVisibility(View.VISIBLE);
                noSearchResults.setVisibility(View.GONE);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, data);
                searchResults.setAdapter(arrayAdapter);
            } else {
                searchResults.setVisibility(View.GONE);
                noSearchResults.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mResults != null) {
            SearchItem item = mResults.get(i);

            if (item.getType() == SearchItem.SearchItemType.PERSON) {

            } else if (item.getType() == SearchItem.SearchItemType.ITEM) {
                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra(ViewItemActivity.KEY_ITEM_UID, item.getUid());
                startActivity(intent);
            } else if (item.getType() == SearchItem.SearchItemType.ORGANIZATION) {
                Intent intent = new Intent(getActivity(), ViewOrganizationActivity.class);
                intent.putExtra(ViewOrganizationActivity.KEY_ORGANIZATION_UID, item.getUid());
                startActivity(intent);
            }

        }
    }
}