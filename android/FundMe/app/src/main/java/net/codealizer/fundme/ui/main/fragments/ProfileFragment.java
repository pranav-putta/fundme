package net.codealizer.fundme.ui.main.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;

import net.codealizer.fundme.FundMe;
import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.adapters.ItemsListAdapter;
import net.codealizer.fundme.ui.main.adapters.OrganizationsListAdapter;
import net.codealizer.fundme.ui.util.CircleTransform;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.db.LocalDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 11/26/16.
 */

public class ProfileFragment extends Fragment implements View.OnClickListener {

    CollapsingToolbarLayout toolbar;

    ImageView toolbarBackdrop;

    TextView name;

    private ViewPager pager;
    private TabLayout tabs;

    private TextView credits;
    private TextView items;
    private TextView organizations;

    private FollowersFragment fragment;
    private ItemsFragment itemsFragment;
    private OrganizationsFragment organizationsFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        fragment = new FollowersFragment();
        itemsFragment = new ItemsFragment();
        organizationsFragment = new OrganizationsFragment();

        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        initialize(getView());
    }

    private void initialize(View v) {
        User user = FundMe.userDataManager.getUser();

        // Profile toolbar
        toolbarBackdrop = (ImageView) v.findViewById(R.id.toolbar_profile_backdrop);
        name = (TextView) v.findViewById(R.id.toolbar_profile_name);

        credits = (TextView) v.findViewById(R.id.profile_credits);
        items = (TextView) v.findViewById(R.id.items);
        organizations = (TextView) v.findViewById(R.id.organizations);

        Glide.with(this).load(user.getProfilePic())
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getActivity()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(toolbarBackdrop);
        pager = (ViewPager) v.findViewById(R.id.profile_pager);
        setupViewPager(pager);

        tabs = (TabLayout) v.findViewById(R.id.toolbar_profile_tabs);
        tabs.setupWithViewPager(pager);

        items.setText(String.valueOf(user.getItemUids().size()));
        organizations.setText(String.valueOf(user.getOrganizationUids().size()));

        name.setText(user.getName());

        credits.setText(String.valueOf((int) user.getVirtualMoney()));

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(itemsFragment, "Items");
        adapter.addFragment(organizationsFragment, "Organizations");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.followers:
        }
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
