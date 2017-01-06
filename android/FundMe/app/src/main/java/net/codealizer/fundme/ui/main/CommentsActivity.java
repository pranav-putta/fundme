package net.codealizer.fundme.ui.main;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.codealizer.fundme.R;
import net.codealizer.fundme.assets.Comment;
import net.codealizer.fundme.assets.Item;
import net.codealizer.fundme.ui.util.AlertDialogManager;
import net.codealizer.fundme.ui.util.CircleTransform;
import net.codealizer.fundme.util.ServiceManager;
import net.codealizer.fundme.util.firebase.DatabaseManager;
import net.codealizer.fundme.util.listeners.OnDownloadListener;

import java.util.List;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener, OnDownloadListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String COMMENTS = "net.codealizer.fundme.main.ui.CommentsActivity.COMMENTS";
    public static final String ITEM = "net.codealizer.fundme.main.ui.CommentsActivity.ITEM";

    private RecyclerView commentsList;
    private TextView noComments;
    private Button newButton;
    private SwipeRefreshLayout refreshLayout;

    private List<Comment> comments;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initialize();
    }

    private void initialize() {
        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        noComments = (TextView) findViewById(R.id.comments_no_results);
        newButton = (Button) findViewById(R.id.comments_new);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.comments_refresh);

        item = getIntent().getParcelableExtra(ITEM);
        comments = item.comments;

        if (comments.size() <= 0) {
            noComments.setVisibility(View.VISIBLE);
            commentsList.setVisibility(View.GONE);
        } else {
            noComments.setVisibility(View.GONE);
            commentsList.setVisibility(View.VISIBLE);
        }

        commentsList.setLayoutManager(new LinearLayoutManager(this));
        commentsList.setItemAnimator(new DefaultItemAnimator());
        commentsList.setAdapter(new CommentsAdapter(comments));

        newButton.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    private void refresh() {
        if (comments.size() <= 0) {
            noComments.setVisibility(View.VISIBLE);
            commentsList.setVisibility(View.GONE);
        } else {
            noComments.setVisibility(View.GONE);
            commentsList.setVisibility(View.VISIBLE);
        }

        commentsList.setAdapter(new CommentsAdapter(comments));
    }

    @Override
    public void onClick(View view) {
        AlertDialogManager.showCommentDialog(item, this, this);
    }

    @Override
    public <D> void onDownloadSuccessful(D data) {
        item = (Item) data;
        comments = item.comments;

        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }

        refresh();
    }

    @Override
    public void onDownloadFailed(String message) {
        AlertDialogManager.showMessageSnackbar(newButton, "Couldn't create the comment");
    }

    @Override
    public void onRefresh() {
        if (ServiceManager.isNetworkAvailable(this)) {
            DatabaseManager.getItem(item.uid, this);
        } else {
            onDownloadFailed("No internet connection!");
        }
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

    private class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

        private List<Comment> comments;

        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(CommentsActivity.this).inflate(R.layout.list_item_comments, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Comment comment = comments.get(position);

            holder.userName.setText(comment.userName);
            Glide.with(CommentsActivity.this).load(comments.get(position).getImageURL())
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(CommentsActivity.this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePicture);
            holder.message.setText(comment.comment);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView profilePicture;
            TextView userName;
            TextView message;

            ViewHolder(View itemView) {
                super(itemView);

                profilePicture = (ImageView) itemView.findViewById(R.id.card_comments_image);
                userName = (TextView) itemView.findViewById(R.id.card_comments_user);
                message = (TextView) itemView.findViewById(R.id.card_comments_description);
            }
        }

    }

}
