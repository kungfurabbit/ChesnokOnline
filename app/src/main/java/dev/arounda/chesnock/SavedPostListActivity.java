package dev.arounda.chesnock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Adapter.FirebasePostViewHolder;
import Model.Post;

/**
 * Created by AlexKarlov on 5/12/2017.
 */

public class SavedPostListActivity extends AppCompatActivity {

    private DatabaseReference mPostReference;
    private FirebaseRecyclerAdapter mFirebaseAdapter;

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView)findViewById(R.id.post_list);
        mPostReference = FirebaseDatabase.getInstance().getReference("posts");
        setUpFirebaseAdapter();
    }
    private void setUpFirebaseAdapter() {
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Post, FirebasePostViewHolder>
                (
                        Post.class,
                        R.layout.post_row,
                        FirebasePostViewHolder.class,
                        mPostReference
                ) {

            @Override
            protected void populateViewHolder(
                    FirebasePostViewHolder viewHolder,
                    Post model,
                    int position
            ) {
                viewHolder.bindRestaurant(model);
            }
        };
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }
}


