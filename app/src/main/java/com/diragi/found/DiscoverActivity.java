package com.diragi.found;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.diragi.found.Models.TextPost;
import com.diragi.found.Models.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.security.PrivateKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscoverActivity extends AppCompatActivity {

    private Firebase ref;
    private RecyclerView r;
    private MainFeed m = new MainFeed();
    private String TAG = "DiscoverAct";
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        ref = new Firebase("https://foundout.firebaseio.com");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbarDisc);
        toolbar.setTitle("Find");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        r = (RecyclerView)findViewById(R.id.feed);
        r.setHasFixedSize(false);
        r.setLayoutManager(new LinearLayoutManager(this));

        populateList();

        final AuthData auth = ref.getAuth();

        if (auth != null) {
            uid = auth.getUid();
        }
    }

    private void populateList() {
        if (ref.getAuth() != null) {
            FirebaseRecyclerAdapter<User, PostViewHolder> adapter = new FirebaseRecyclerAdapter<User, PostViewHolder>(User.class, R.layout.discover_list_item, PostViewHolder.class, ref.child("users")) {
                @Override
                protected void populateViewHolder(final PostViewHolder postViewHolder, final User user, int i) {
                    postViewHolder.name.setText(user.getFullName());
                    postViewHolder.username.setText(user.getUserName());
                    postViewHolder.score.setText(user.getUserEmail());
                    postViewHolder.icon.setImageDrawable(null);
                    if (user.getUserIcon() != null && !user.getUserIcon().equals("")) {
                        Log.i(TAG, "Setting image");
                        Picasso.with(postViewHolder.icon.getContext()).load(user.getUserIcon()).into(postViewHolder.icon);
                    }
                    postViewHolder.bg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(DiscoverActivity.this, UserDetailActivity.class);
                            i.putExtra("uid", user.getUid());
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DiscoverActivity.this, postViewHolder.icon, "icon");
                            startActivity(i);
                        }
                    });
                }
            };
            r.setAdapter(adapter);
        } else {
            m.reAuth(DiscoverActivity.this);
        }
    }

    public class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

        private CircleImageView i;

        protected Bitmap doInBackground(Object... params) {
            this.i = (CircleImageView)params[1];
            try {
                return m.bitmapFromUrl((String) params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            i.setImageBitmap(result);
        }
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView username;
        TextView score;
        CircleImageView icon;
        View bg;

        public PostViewHolder(View v) {
            super(v);
            name = (TextView)v.findViewById(R.id.text1);
            username  = (TextView)v.findViewById(R.id.text2);
            score    = (TextView)v.findViewById(R.id.text3);
            bg = v.findViewById(R.id.cardBody);
            icon = (CircleImageView) v.findViewById(R.id.userImage);
        }
    }
}
