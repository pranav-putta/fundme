package net.codealizer.fundme.ui.util;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.codealizer.fundme.R;

/**
 * Created by Pranav on 5/7/17.
 */
public class MasonryCardItem extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public TextView textView;

    public MasonryCardItem(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.card_item_image);
        textView = (TextView) itemView.findViewById(R.id.card_item_name);

    }
}