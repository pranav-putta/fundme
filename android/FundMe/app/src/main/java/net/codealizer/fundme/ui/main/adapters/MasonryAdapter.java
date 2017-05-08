package net.codealizer.fundme.ui.main.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.ui.util.MasonryCardItem;

import java.util.List;

/**
 * Created by Pranav on 5/7/17.
 */

public class MasonryAdapter extends RecyclerView.Adapter<MasonryCardItem> {

    private Context context;
    private List<Item> mItems;

    public MasonryAdapter(Context c, List<Item> items) {
        this.context = c;
        this.mItems = items;
    }

    @Override
    public MasonryCardItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_grid_item, parent, false);
        MasonryCardItem item = new MasonryCardItem(layoutView);
        return item;
    }

    @Override
    public void onBindViewHolder(MasonryCardItem holder, int position) {
        Glide.with(context).load(mItems.get(position).getImageURL()).into(holder.imageView);
        holder.textView.setText(mItems.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
