package Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import dev.arounda.chesnock.R;

/**
 * Created by AlexKarlov on 5/12/2017.
 */

public class ViewPagerAdapter extends PagerAdapter {

    Activity activity;
    String[] images;
    private Context mContext;
    private LayoutInflater layoutInflater;
    public ViewPagerAdapter(Context context) {
        this.mContext = context;
        }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public  Object instantiateItem(ViewGroup container, int position){

    layoutInflater = (LayoutInflater)activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       // View itemView  = layoutInflater.inflate(R.layout.viewpager_item, container, false);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(
                R.layout.viewpager_item, container, false);
        ImageView image;
        image = (ImageView)layout.findViewById(R.id.post_img);
        DisplayMetrics dis = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dis);
        int height = dis.heightPixels;
        int weight = dis.widthPixels;
        image.setMinimumHeight(height);
        image.setMinimumWidth(weight);

        try{
            Picasso.with(activity.getApplicationContext())
                    .load(images[position])
                    .placeholder(R.mipmap.add_btn)
                    .error(R.mipmap.ic_launcher)
                    .into(image);

        }
        catch(Exception ex){}
        container.addView(layout);
        return  layout;
    }
}
