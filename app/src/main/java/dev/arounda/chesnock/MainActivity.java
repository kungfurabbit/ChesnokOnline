package dev.arounda.chesnock;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import Adapter.PostPagerAdapter;
import Interface.SimpleCallback;
import Model.Post;
import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private RecyclerView mPostList;
    private DatabaseReference mDatabase;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private Boolean isSkiped=false;

    public String mUsername,mPhotoUrl,mUserEmail,mUserId;

    LinearLayoutManager PostLinearLayoutManager;

    Query sortDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        ActionBar menu = getSupportActionBar();
        menu.setLogo(R.mipmap.ab_logotype);
        menu.setDisplayUseLogoEnabled(true);


        if(getIntent().hasExtra("skip")){
        isSkiped = getIntent().getExtras().getBoolean("skip");
        }
        auth();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");

        sortDate = mDatabase.orderByChild("post_date");

        mPostList = (RecyclerView)findViewById(R.id.post_list);

        mPostList.setHasFixedSize(true);


        PostLinearLayoutManager = new LinearLayoutManager(MainActivity.this);
        PostLinearLayoutManager.setReverseLayout(true);
        PostLinearLayoutManager.setStackFromEnd(true);
        mPostList.setLayoutManager(PostLinearLayoutManager);

    }

    private void auth(){
        if(!isSkiped) {
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
    }

    @Override
    public  void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder >(
                Post.class,
                R.layout.post_row,
                PostViewHolder.class,
                sortDate
        ) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, Post model, final int position) {
                Typeface CFB = Typeface.createFromAsset(getAssets(), "NotoSans-Bold.ttf");
                Typeface CFR = Typeface.createFromAsset(getAssets(), "NotoSans-Regular.ttf");

                viewHolder.titleViewPost.setTypeface(CFB);
                viewHolder.dateViewPost.setTypeface(CFR);
                viewHolder.descViewPost.setTypeface(CFR);
                viewHolder.mCommentButton.setTypeface(CFB);

                final String post_key = getRef(position).getKey();
                model.im(new SimpleCallback<ArrayList<String>>() {
                    @Override
                    public void callback(ArrayList<String> data) {
                        viewHolder.UriList.clear();
                        viewHolder.imgesList.clear();
                        String[] url;
                        url= data.toString().split(",");
                        for(int i=0; i<url.length; i++){
                            if(url[i].contains("url")) {
                                Matcher m = Patterns.WEB_URL.matcher(url[i]);
                                while (m.find()) {
                                    String urls = m.group();
                                    viewHolder.UriList.add(urls);
                                    viewHolder.imgesList.add(urls);
                                }
                            }
                        }
                        viewHolder.setImgPager(viewHolder.imgesList);
                    }
                }, post_key);
                viewHolder.setTitle(model.getPost_title());
                viewHolder.setDate(model.getPost_date());
                viewHolder.setDesc(model.getPost_desc());
                viewHolder.mCommentButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent commentActivity  = new Intent(MainActivity.this, CommentActivity.class);
                                    commentActivity.putExtra("post_id", post_key);
                                    if(mUserId != null) {
                                        commentActivity.putExtra("user_id", mUserId);
                                    }
                                    startActivity(commentActivity);
                                }
                            });

                viewHolder.lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentActivity  = new Intent(MainActivity.this, CommentActivity.class);
                        commentActivity.putExtra("post_id", post_key);
                        if(mUserId != null) {
                            commentActivity.putExtra("user_id", mUserId);
                        }
                        startActivity(commentActivity);

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
            Intent contactActivity = new Intent(MainActivity.this, ContactActivity.class);
            startActivity(contactActivity);
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
        RelativeLayout lay;
        List<String> UriList  = new ArrayList<String>();
        ArrayList<String> imgesList = new ArrayList<String>();
        ImageView mSwitchImage;
        int position=0;

        TextView titleViewPost;
        TextView dateViewPost;
        TextView descViewPost;
        ViewPager mViewPager;
        CircleIndicator indicator;
        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext     = mView.getContext();
            lay = (RelativeLayout)mView.findViewById(R.id.background_layout);
            mCommentButton = (Button)mView.findViewById(R.id.comment);
            mSwitchImage = (ImageView) mView.findViewById(R.id.post_img);
            titleViewPost = (TextView)mView.findViewById(R.id.post_title);
            dateViewPost = (TextView)mView.findViewById(R.id.post_date);
            descViewPost = (TextView)mView.findViewById(R.id.post_desc);
            mViewPager = (ViewPager)mView. findViewById(R.id.pagerView);
            indicator =(CircleIndicator)mView.findViewById(R.id.indicator);
        }

        public void setTitle(String title){
            titleViewPost.setText(title);
        }

        public void setDate(String date){
            dateViewPost.setText(date);
        }

        public void setDesc(String desc){
            descViewPost.setText(desc);
        }


        public void setImgPager(ArrayList<String> imgList){
            PostPagerAdapter mCustomPagerAdapter = new PostPagerAdapter(mContext, imgList);
            mViewPager.setAdapter(mCustomPagerAdapter);
            indicator.setViewPager(mViewPager);
        }

    }
}


