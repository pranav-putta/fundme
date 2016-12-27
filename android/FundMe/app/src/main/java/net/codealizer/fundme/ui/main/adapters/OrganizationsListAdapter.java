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
import net.codealizer.fundme.assets.Organization;
import net.codealizer.fundme.ui.main.CreateItemActivity;
import net.codealizer.fundme.ui.main.CreateOrganizationActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pranav on 12/20/16.
 */

public class OrganizationsListAdapter extends RecyclerView.Adapter<OrganizationsListAdapter.ViewHolder> {

    private ArrayList<Organization> mOrganizations;
    private Context mContext;

    public OrganizationsListAdapter(Context context, ArrayList<Organization> organizations) {
        this.mOrganizations = organizations;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organization, parent, false);

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Organization organization = mOrganizations.get(position);

        holder.setTitle(organization.getTitle());
        holder.setBackdrop(organization.getImage());
        holder.setCardClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CreateOrganizationActivity.class);
                intent.putExtra(CreateOrganizationActivity.KEY_EDIT_ORGANIZATION, organization);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mOrganizations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView backdrop;
        private TextView title;
        private CardView card;

        ViewHolder(View view) {
            super(view);

            backdrop = (ImageView) view.findViewById(R.id.item_organization_backdrop);
            title = (TextView) view.findViewById(R.id.item_organization_title);
            card = (CardView) view.findViewById(R.id.item_organization_container);

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


    }
}
