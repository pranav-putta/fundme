package net.codealizer.fundme.ui.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.codealizer.fundme.R;
import net.codealizer.fundme.ui.util.AlertDialogManager;

import java.util.Arrays;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    RecyclerView itemsList;
    ImageView backdrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initialize();
    }

    private void initialize() {
        itemsList = (RecyclerView) findViewById(R.id.shop_items);
        backdrop = (ImageView) findViewById(R.id.shop_toolbar_backdrop);

        itemsList.setLayoutManager(new LinearLayoutManager(this));
        itemsList.setItemAnimator(new DefaultItemAnimator());
        itemsList.setAdapter(new ShopItemAdapter());

        Glide.with(this).load("http://i.imgur.com/AMf9X7Er.jpg")
                .crossFade()
                .thumbnail(0.5f)
                .into(backdrop);    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }

        return true;
    }

    class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> {

        List<String> names;
        List<String> prices;

        public ShopItemAdapter () {
            names = Arrays.asList(getResources().getStringArray(R.array.shop_item_names));
            prices = Arrays.asList(getResources().getStringArray(R.array.shop_item_prices));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(ShopActivity.this).inflate(R.layout.shop_item, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.name.setText(names.get(position));
            holder.price.setText(prices.get(position));
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialogManager.showBuyConfirmDialog(names.get(position), prices.get(position), position, ShopActivity.this, itemsList);
                }
            });
        }

        @Override
        public int getItemCount() {
            return names.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView price;

            CardView card;

            public ViewHolder(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.shop_item_name);
                price = (TextView) itemView.findViewById(R.id.shop_item_price);

                card = (CardView) itemView.findViewById(R.id.shop_item);
            }
        }

    }

}
