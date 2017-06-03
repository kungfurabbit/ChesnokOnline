package Adapter;

import android.content.Context;
import android.content.Intent;
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

import dev.arounda.chesnock.ImageViewActivity;
import dev.arounda.chesnock.R;

/**
 * Created by AlexKarlov on 5/20/2017.
 */

public class PostPagerAdapter extends PagerAdapter {
   Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<String> listUrl = new ArrayList<String>();
    Uri[] url;

    public PostPagerAdapter(Context context,  ArrayList<String> listUrl ) {
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


        View itemView = mLayoutInflater.inflate(R.layout.post_image_view, container, false);
        final ProgressBar progressBar = (ProgressBar)itemView.findViewById(R.id.ImgProgressBar);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.post_img);
        Uri uri = Uri.parse(listUrl.get(position));
        Picasso
                .with(mContext)
                .load(uri)
                .resizeDimen(R.dimen.image_size, R.dimen.image_size)
                .centerInside()
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // once the image is loaded, load the next image
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewImageIntent = new Intent(mContext, ImageViewActivity.class);
                viewImageIntent.putStringArrayListExtra("urls", listUrl);
                mContext.startActivity(viewImageIntent);
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
