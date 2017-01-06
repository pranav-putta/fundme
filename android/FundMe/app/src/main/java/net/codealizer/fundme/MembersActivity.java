package net.codealizer.fundme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.codealizer.fundme.assets.User;
import net.codealizer.fundme.ui.main.CommentsActivity;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.ui.util.CircleTransform;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.util.List;

public class MembersActivity extends AppCompatActivity implements OnDownloadListener {

    public static final String KEY_ORGANIZATION_UID = "net.codealizer.fundme.ui.main.MembersActivity.KEY_ORGANIZATION_UID";
    private RecyclerView list;

    private ProgressDialog progressDialog;
    private String organizationUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list = (RecyclerView) findViewById(R.id.members_list);

        initialize();
    }

    private void initialize() {
        progressDialog = AlertDialogManager.showProgressDialog(this);

        organizationUID = getIntent().getStringExtra(KEY_ORGANIZATION_UID);

        DatabaseManager.getMembersFromOrganization(organizationUID, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public <D> void onDownloadSuccessful(D data) {
        List<User> members = (List<User>) data;

        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new MemberAdapter(members));

        progressDialog.dismiss();

        AlertDialogManager.showMessageSnackbar(list, "Downloaded information successfully!");
    }

    @Override
    public void onDownloadFailed(String message) {

    }

    class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

        private List<User> members;

        public MemberAdapter(List<User> members) {
            this.members = members;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MembersActivity.this).inflate(R.layout.card_member, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User user = members.get(position);

            holder.user.setText(user.getName());
            Glide.with(MembersActivity.this).load(user.getProfilePic())
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(MembersActivity.this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profile);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView user;
            ImageView profile;

            public ViewHolder(View itemView) {
                super(itemView);

                user = (TextView) itemView.findViewById(R.id.card_member_user);
                profile = (ImageView) itemView.findViewById(R.id.card_member_image);
            }
        }
    }
}
