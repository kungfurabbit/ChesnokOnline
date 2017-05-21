package Model;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import Interface.SimpleCallback;

/**
 * Created by AlexKarlov on 5/15/2017.
 */

public class Comment {

    private String comment_text;
    private String user_id;
    private long date;

    public Comment(){}

    public Comment(String comment_text, String user_id, long date) {
        this.comment_text = comment_text;
        this.user_id = user_id;
        this.date = date;
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
        String value = null;
        GregorianCalendar gcalendar = new GregorianCalendar();
        Long dateCurrent = gcalendar.getTimeInMillis();
        Long valueTime = dateCurrent-date;
        if(valueTime<3){
            value = valueTime.toString();
        }
        else if(valueTime >=3) {
           value = new java.text.SimpleDateFormat("dd/MM/yy HH:mm").
                    format(new java.util.Date((dateCurrent - date) * 100));
        }
        return value;
    }

    public void setDate(long date) {
        this.date = date;
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
