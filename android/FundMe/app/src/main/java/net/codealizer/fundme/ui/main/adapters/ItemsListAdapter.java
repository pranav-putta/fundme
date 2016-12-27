package net.codealizer.fundme.ui.main.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.ui.main.ViewItemActivity;
import net.codealizer.fundme.util.ServiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/20/16.
 */

public class ItemsListAdapter extends RecyclerView.Adapter<ItemsListAdapter.ViewHolder> {

    private ArrayList<Item> mItems;

    private Context mContext;

    public ItemsListAdapter(Context context, ArrayList<Item> items) {
        this.mItems = items;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_item, parent, false);

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item item = mItems.get(position);

        holder.setTitle(item.getTitle());
        holder.setBackdrop((item.getImage()));
        holder.setPrice(item.getPrice());
        holder.setCardClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewItemActivity.class);
                intent.putExtra(ViewItemActivity.KEY_ITEM, item);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView backdrop;
        private TextView title;
        private TextView price;
        private CardView card;

        ViewHolder(View view) {
            super(view);

            backdrop = (ImageView) view.findViewById(R.id.item_item_backdrop);
            title = (TextView) view.findViewById(R.id.item_item_title);
            price = (TextView) view.findViewById(R.id.item_item_price);
            card = (CardView) view.findViewById(R.id.item_item_container);


            backdrop.setColorFilter(Color.argb(50, 0, 0, 0));
        }

        void setTitle(String t) {
            title.setText(t);
        }

        void setBackdrop(Bitmap image) {
            backdrop.setImageBitmap(image);
        }

        void setCardClickListener(View.OnClickListener clickListener) {
            card.setOnClickListener(clickListener);
        }

        void setPrice(double p) {
            String pp = "$" + Math.round(p);
            price.setText(pp);
        }


    }
}
