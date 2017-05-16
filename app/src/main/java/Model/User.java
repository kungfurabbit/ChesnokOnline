package Model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import Interface.SimpleCallback;

/**
 * Created by AlexKarlov on 5/16/2017.
 */

public class User {
    private  String email;
    private String img_url;
    private String name;

    public User(){

    }

    public User(String email, String img_url, String name) {
        this.email = email;
        this.img_url = img_url;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public  void getInfo(final SimpleCallback<Map<String, Object>> finishedCallback, String user_id){
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                finishedCallback.callback(newPost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
