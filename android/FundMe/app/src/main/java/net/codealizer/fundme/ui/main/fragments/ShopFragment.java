package net.codealizer.fundme.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.main.ShopActivity;

/**
 * Created by Pranav on 12/26/16.
 */

public class ShopFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize();
    }

    private void initialize() {
        Intent intent = new Intent(getActivity(), ShopActivity.class);
        getActivity().startActivity(intent);
    }
}