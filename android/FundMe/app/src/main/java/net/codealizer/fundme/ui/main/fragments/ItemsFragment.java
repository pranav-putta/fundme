package net.codealizer.fundme.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.ui.main.CreateOrganizationActivity;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.util.db.LocalDatabaseManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/26/16.
 */

public class ItemsFragment extends Fragment {

    private RecyclerView list;
    private TextView notFound;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_items, container, false);
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
    }

    private void initialize() {
        list = (RecyclerView) getView().findViewById(R.id.items_list);
        notFound = (TextView) getView().findViewById(R.id.search_no_results);
        ArrayList<Item> items = new LocalDatabaseManager(getActivity()).getAllItems();

        Adapter adapter = new Adapter(new LocalDatabaseManager(getActivity()).getAllItems());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(adapter);

        if (items.size() > 0) {
            list.setVisibility(View.VISIBLE);
            notFound.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            notFound.setVisibility(View.VISIBLE);
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private List<Item> items;

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, description, price;
            public ImageView image;
            public CardView card;

            public ViewHolder(View view) {
                super(view);

                title = (TextView) view.findViewById(R.id.card_item_title);
                description = (TextView) view.findViewById(R.id.card_item_description);
                price = (TextView) view.findViewById(R.id.card_item_price);

                image = (ImageView) view.findViewById(R.id.card_item_backdrop);

                card = (CardView) view.findViewById(R.id.card_item);
            }

        }

        public Adapter(List<Item> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            int price = (int) Math.round(items.get(position).getPrice());

            holder.title.setText(items.get(position).getTitle());
            holder.description.setText(items.get(position).getDescription());
            holder.price.setText("$" + String.valueOf(price));
            holder.image.setImageBitmap(items.get(position).getImage());
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                    intent.putExtra(ViewItemActivity.KEY_ITEM, items.get(position));
                    getActivity().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}