package net.codealizer.fundme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.ui.main.fragments.SearchFragment;
import net.codealizer.fundme.util.DataManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements FloatingSearchView.OnQueryChangeListener, FloatingSearchView.OnSearchListener {

    private FloatingSearchView searchbar;
    private TabLayout tabs;
    private ViewPager viewPager;

    private SearchFragment peopleSearchFragment;
    private SearchFragment itemsSearchFragment;
    private SearchFragment organizationsSearrchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchbar = (FloatingSearchView) findViewById(R.id.searchBar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        peopleSearchFragment = new SearchFragment();
        itemsSearchFragment = new SearchFragment();
        organizationsSearrchFragment = new SearchFragment();

        setupViewPager(viewPager);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        searchbar.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                finish();
                SearchActivity.super.onBackPressed();
            }
        });
        searchbar.setOnQueryChangeListener(this);
        searchbar.setOnSearchListener(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(peopleSearchFragment, "People");
        adapter.addFragment(itemsSearchFragment, "Items");
        adapter.addFragment(organizationsSearrchFragment, "Organizations");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        if (!oldQuery.equals("") && newQuery.equals("")) {
            searchbar.clearSuggestions();
        } else {
            searchbar.showProgress();

            DataManager.init(this);
            DataManager.findSuggestions(this, newQuery, 5, 250, new DataManager.OnFindSuggestionsListener() {
                @Override
                public void onResults(List<SearchItem> results) {
                    List<SearchItem> people = new ArrayList<>();
                    List<SearchItem> items = new ArrayList<>();
                    List<SearchItem> organizations = new ArrayList<>();

                    for (SearchItem item : results) {
                        if (item.getType() == SearchItem.SearchItemType.PERSON) {
                            people.add(item);
                        } else if (item.getType() == SearchItem.SearchItemType.ITEM) {
                            items.add(item);
                        } else if (item.getType() == SearchItem.SearchItemType.ORGANIZATION) {
                            organizations.add(item);
                        }
                    }

                    peopleSearchFragment.populateResults(people);
                    itemsSearchFragment.populateResults(items);
                    organizationsSearrchFragment.populateResults(organizations);

                    searchbar.hideProgress();
                }
            });
        }
    }

    @Override
    public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        SearchItem item = (SearchItem) searchSuggestion;

        if (item.getType() == SearchItem.SearchItemType.PERSON) {

        } else if (item.getType() == SearchItem.SearchItemType.ITEM) {
            Intent intent = new Intent(this, ViewItemActivity.class);
            intent.putExtra(ViewItemActivity.KEY_ITEM_UID, item.getUid());
            startActivity(intent);
        } else if (item.getType() == SearchItem.SearchItemType.ORGANIZATION) {

        }

    }

    @Override
    public void onSearchAction(String currentQuery) {
        searchbar.showProgress();

        DataManager.init(this);
        DataManager.findSuggestions(this, currentQuery, 5, 250, new DataManager.OnFindSuggestionsListener() {
            @Override
            public void onResults(List<SearchItem> results) {
                List<SearchItem> people = new ArrayList<>();
                List<SearchItem> items = new ArrayList<>();
                List<SearchItem> organizations = new ArrayList<>();

                for (SearchItem item : results) {
                    if (item.getType() == SearchItem.SearchItemType.PERSON) {
                        people.add(item);
                    } else if (item.getType() == SearchItem.SearchItemType.ITEM) {
                        items.add(item);
                    } else if (item.getType() == SearchItem.SearchItemType.ORGANIZATION) {
                        organizations.add(item);
                    }
                }

                peopleSearchFragment.populateResults(people);
                itemsSearchFragment.populateResults(items);
                organizationsSearrchFragment.populateResults(organizations);

                searchbar.hideProgress();
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
