package Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import Model.Post;
import dev.arounda.chesnock.R;

/**
 * Created by AlexKarlov on 5/12/2017.
 */

public class FirebasePostViewHolder extends RecyclerView.ViewHolder  {
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    View mView;
    Context mContext;
    public FirebasePostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
    }

    public void bindRestaurant(Post post) {
        ImageView postImageView = (ImageView) mView.findViewById(R.id.post_img);
        TextView titleTextView = (TextView) mView.findViewById(R.id.post_title);
        TextView dateTextView = (TextView) mView.findViewById(R.id.post_date);
        TextView descTextView = (TextView) mView.findViewById(R.id.post_desc);

        String sUrl =post.getPost_img().get(0).toString();
        String result = sUrl.substring(sUrl.indexOf("=")+1, sUrl.indexOf("}"));

        Picasso.with(mContext)
                .load(result)
                .into(postImageView);

        titleTextView.setText(post.getPost_title());
        dateTextView.setText(post.getPost_date());
        descTextView.setText(post.getPost_desc());
    }


}
