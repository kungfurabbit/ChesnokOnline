package dev.arounda.chesnock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import Adapter.ViewPagerAdapter;
import Model.Post;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mPostList;
    private DatabaseReference mDatabase;

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    public String[] images = {"https://pp.userapi.com/c637825/v637825195/43ea7/L22XntZRT-4.jpg",
            "https://pp.userapi.com/c637825/v637825195/4356b/x0IOnDl5Fgw.jpg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");

        mPostList = (RecyclerView)findViewById(R.id.post_list);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
       // viewPagerAdapter = new ViewPagerAdapter(MainActivity.this, images);
        viewPager.setAdapter(new ViewPagerAdapter(this));

        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));
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
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {

                String sUrl =model.getPost_img().get(0).toString();
                String result = sUrl.substring(sUrl.indexOf("=") + 1, sUrl.indexOf("}"));


                viewHolder.setTitle(model.getPost_title());
                viewHolder.setDate(model.getPost_date());
                viewHolder.setDesc(model.getPost_desc());
            //    viewHolder.setImg(result);
            }
        };
        mPostList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        Context mContext;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext = mView.getContext();
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


