package dev.arounda.chesnock;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;

import Interface.SimpleCallback;
import Model.Post;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private RecyclerView mPostList;
    private DatabaseReference mDatabase;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public String mUsername,mPhotoUrl,mUserEmail,mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");

        mPostList = (RecyclerView)findViewById(R.id.post_list);

        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void auth(){
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            mUserEmail = mFirebaseUser.getEmail();
            mUserId = mFirebaseUser.getUid();
            CheckUserExist();
        }
    }

    @Override
    public  void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.post_row,
                PostViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, Post model, final int position) {

                final String post_key = getRef(position).getKey();
                model.im(new SimpleCallback<Map<String, Object>>() {
                    @Override
                    public void callback(Map<String, Object> data) {
                        String[] url;
                        url= data.get("post_img").toString().split(",");
                        for(int i=0; i<url.length; i++){
                            String res = url[i].substring(url[i].indexOf("=") + 1, url[i].indexOf("}"));
                            Uri imageUri = Uri.parse( url[i]);
                        }
                    }
                }, post_key);
                String sUrl =model.getPost_img().get(0).toString();
                String result = sUrl.substring(sUrl.indexOf("=") + 1, sUrl.indexOf("}"));
                viewHolder.setTitle(model.getPost_title());
                viewHolder.setDate(model.getPost_date());
                viewHolder.setDesc(model.getPost_desc());
                viewHolder.setImg(result);

                viewHolder.mCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commnetIntent  = new Intent(MainActivity.this, CommentActivity.class);
                        commnetIntent.putExtra("post_id", post_key);
                        commnetIntent.putExtra("user_id", mUserId);
                        startActivity(commnetIntent);
                    }
                });
            }
        };
        mPostList.setAdapter(firebaseRecyclerAdapter);

    }

    private void CheckUserExist(){

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mUserId)){
                    mFirebaseDatabase.child(mUserId).child("img_url").setValue(mPhotoUrl);
                    mFirebaseDatabase.child(mUserId).child("email").setValue(mUserEmail);
                    mFirebaseDatabase.child(mUserId).child("name").setValue(mUsername);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_contacts)
        {
            startActivity(new Intent(this, ContactActivity.class));
            finish();
        }
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public static class PostViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        Button mCommentButton;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext     = mView.getContext();
            mCommentButton = (Button)mView.findViewById(R.id.comment);


        }

        public void setTitle(String title){
            TextView titleViewPost = (TextView)mView.findViewById(R.id.post_title);
            titleViewPost.setText(title);
        }

        public void setDate(String date){
            TextView dateViewPost = (TextView)mView.findViewById(R.id.post_date);
            dateViewPost.setText(date);
        }

        public void setDesc(String desc){
            TextView descViewPost = (TextView)mView.findViewById(R.id.post_desc);
            descViewPost.setText(desc);
        }

        public void setImg(String image){
            ImageView postImageView = (ImageView) mView.findViewById(R.id.post_img);
            Picasso.with(mContext).load(image).into(postImageView);
        }
    }
}


