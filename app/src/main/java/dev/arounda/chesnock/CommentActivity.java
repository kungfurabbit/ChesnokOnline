package dev.arounda.chesnock;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Map;

import Interface.SimpleCallback;
import Model.Comment;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private String post_key, user_id;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;


    private  Uri imageUri = null;
    private RecyclerView mCommentList;
    private EditText commentText;

    private Button sendBtn;
    private ImageButton mGalleryButton;


    private ProgressDialog mProgress;

    private static  final int GALLERY_REQUEST = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        MultiDex.install(this);

        post_key = getIntent().getExtras().getString("post_id");
        user_id = getIntent().getExtras().getString("user_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(post_key);
        mStorage = FirebaseStorage.getInstance().getReference();

        commentText = (EditText)findViewById(R.id.commentEditText);
        sendBtn = (Button)findViewById(R.id.sendButton);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });

        mProgress = new ProgressDialog(this);

        mGalleryButton = (ImageButton)findViewById(R.id.galerryBtn);
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        mCommentList = (RecyclerView)findViewById(R.id.comment_list);
        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public  void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Comment, CommentActivity.CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentActivity.CommentViewHolder>(
                Comment.class,
                R.layout.comment_row,
                CommentActivity.CommentViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final CommentActivity.CommentViewHolder viewHolder, final Comment model, final int position) {
                User user = new User();
                user.getInfo(new SimpleCallback<Map<String, Object>>() {
                    @Override
                    public void callback(Map<String, Object> data) {
                        viewHolder.setUser(data.get("name").toString());
                        viewHolder.setUserImage(data.get("img_url").toString());
                    }
                },model.getUser_id() );

                viewHolder.setText(model.getComment_text());

            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);

    }

    private void startPosting(){
        mProgress.setMessage("Sending....");
        mProgress.show();
        final String textComment = commentText.getText().toString().trim();

        if (!TextUtils.isEmpty(textComment)) {
           StorageReference filepath = mStorage.child("CommentImages").child(post_key).child(imageUri.getLastPathSegment());

            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  //  Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    MainActivity mUserId = new MainActivity();
                    DatabaseReference newComment =  mDatabase.push();
                    newComment.child("comment_text").setValue(textComment);
                    newComment.child("user_id").setValue(user_id);
                    mProgress.hide();
                    mProgress.closeOptionsMenu();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
       super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            imageUri = data.getData();
            mGalleryButton.setImageURI(imageUri);
        }
    }

    public static class CommentViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        Context mContext;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext     = mView.getContext();

        }

        public void setUser(String user){
            TextView userViewPost = (TextView)mView.findViewById(R.id.user_textView);
            userViewPost.setText(user);
        }

        public void setText(String text){
            TextView commentViewPost = (TextView)mView.findViewById(R.id.comment_text);
            commentViewPost.setText(text);
        }

        private  void setUserImage(String image){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userImageView);
            Picasso.with(mContext).load(image).into(userImageView);
        }

    }
}
