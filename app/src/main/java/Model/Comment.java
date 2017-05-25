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
 * Created by AlexKarlov on 5/15/2017.
 */

public class Comment {

    private String comment_text;
    private String user_id;
    private long date;
    List<Object> comment_img = new ArrayList<Object>();

    private String[] MonthName = {"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня",
            "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};

    public Comment(){}

    public Comment(String comment_text, String user_id, long date, List<Object> comment_img) {
        this.comment_text = comment_text;
        this.user_id = user_id;
        this.date = date;
        this.comment_img = comment_img;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void getList(){

    }


    public String getDate() {

        String month = new java.text.SimpleDateFormat("MM").
                format(new java.util.Date(date * 1000));
        int monthnumber = Integer.parseInt(month);

        String value = new java.text.SimpleDateFormat("dd ").
                format(new java.util.Date(date * 1000));
        value +=MonthName[monthnumber-1];
        value+= new java.text.SimpleDateFormat(" HH:mm").
                format(new java.util.Date(date * 1000));
        return value;
    }
    public void setDate(long date) {
        this.date = date;
    }

    public List<Object> getComment_img() {
        return comment_img;
    }

    public void setComment_img(List<Object> comment_img) {
        this.comment_img = comment_img;
    }

    public void getListImg(@NonNull final SimpleCallback<ArrayList<String>> finishedCallback, String post_key, String comment_key){

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(post_key).child(comment_key).child("comment_img");

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> images = ( ArrayList<String>) dataSnapshot.getValue();
                finishedCallback.callback(images);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
