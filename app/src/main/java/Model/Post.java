package Model;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Interface.SimpleCallback;

/**
 * Created by AlexKarlov on 5/12/2017.
 */

public class Post {
    private String  post_id;
    private String  post_title;
    private long  post_date;
    List<Object> post_img = new ArrayList<Object>();
    ArrayList<String> listUrl = new ArrayList<String>();
    private String  post_desc;
    private String[] MonthName = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня",
            "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

     public Post(){    }

    public Post(String post_id, String post_title, long post_date, List<Object> post_img, String post_desc) {
        this.post_id = post_id;
        this.post_title = post_title;
        this.post_date = post_date;
        this.post_img = post_img;
        this.post_desc = post_desc;
    }

    public void setPost_img(List<Object> post_img) {
        this.post_img = post_img;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_date() {
        String month = new java.text.SimpleDateFormat("MM").
                format(new java.util.Date(post_date * 1000));
        int monthnumber = Integer.parseInt(month);

        String value = new java.text.SimpleDateFormat("dd ").
                format(new java.util.Date(post_date * 1000));
        value +=MonthName[monthnumber-1];
        value+= new java.text.SimpleDateFormat(" HH:mm").
                format(new java.util.Date(post_date * 1000));
        return value;
    }

    public void setPost_date(long post_date) {
        this.post_date = post_date;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }
    public void im(@NonNull final SimpleCallback<ArrayList<String>> finishedCallback, String post_key){

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts").child(post_key).child("post_img");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> newPost = (ArrayList<String>) dataSnapshot.getValue();
                finishedCallback.callback(newPost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }
}
