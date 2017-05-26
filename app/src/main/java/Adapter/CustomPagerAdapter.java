package Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dev.arounda.chesnock.R;

/**
 * Created by AlexKarlov on 5/20/2017.
 */

public class CustomPagerAdapter extends PagerAdapter {
    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<String> listUrl = new ArrayList<String>();


    public CustomPagerAdapter(Context context,  ArrayList<String> listUrl ) {
        mContext = context;
        this.listUrl = listUrl;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return listUrl.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = mLayoutInflater.inflate(R.layout.image_views, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar)itemView.findViewById(R.id.ImgProgressBar);
        Uri uri = Uri.parse(listUrl.get(position));

        Picasso.with(mContext).load(uri).into(imageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                // once the image is loaded, load the next image
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }

}
