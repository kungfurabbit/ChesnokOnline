package dev.arounda.chesnock;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;

import Interface.SimpleCallback;
import Model.Comment;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private String post_key, user_id;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    ArrayList<String> listUri = new ArrayList<String>();

    private  Uri imageUri = null;
    private RecyclerView mCommentList;
    private EditText commentText;

    private ImageButton sendBtn;
    private ImageButton mGalleryButton;


    private ProgressDialog mProgress;

    private static  final int GALLERY_REQUEST = 3;

    private ArrayList<String> imagesUrl = new ArrayList<String>();

    LinearLayout layoutViewImage;
    LinearLayout.LayoutParams layoutParamsImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        MultiDex.install(this);

        layoutParamsImg = new LinearLayout.LayoutParams(100, 100);
        layoutViewImage =  (LinearLayout)findViewById(R.id.image_container);

        mGalleryButton = (ImageButton)findViewById(R.id.galerryBtn);
        commentText = (EditText)findViewById(R.id.commentEditText);
        commentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentText.setText(null);
            }
        });

        sendBtn = (ImageButton)findViewById(R.id.sendButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar menu = getSupportActionBar();

        menu.setDisplayHomeAsUpEnabled(true);


        if(!getIntent().hasExtra("user_id")){
            mGalleryButton.setVisibility(View.GONE);
            commentText.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);


            LinearLayout layout = (LinearLayout)findViewById(R.id.linearLayoutImage);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.gravity = Gravity.CENTER;
            final Button authButton = new  Button(getApplicationContext());

            authButton.setText("Authority");
            authButton.setLayoutParams(layoutParams);

            layout.addView(authButton);
            layout.destroyDrawingCache();

            authButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CommentActivity.this, AuthActivity.class));
                }
            });

        }
        post_key = getIntent().getExtras().getString("post_id");
        user_id = getIntent().getExtras().getString("user_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("comments").child(post_key);
        mStorage = FirebaseStorage.getInstance().getReference();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPosting();

            }
        });

        mProgress = new ProgressDialog(this);

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    String[] extraMimeTypes = {"image/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, GALLERY_REQUEST);
                }
                else  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_REQUEST);
                }

                Toast.makeText(CommentActivity.this,
                        "Single-selection: Tap on any file.\n" +
                                "Multi-selection: Tap & Hold on the first file, " +
                                "tap for more, tap on OPEN to finish.",
                        Toast.LENGTH_LONG).show();
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
                final User user = new User();
                final String comment_key = getRef(position).getKey();
                mDatabase.child(comment_key).child("user_id").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                      Object users_id;
                            users_id = dataSnapshot.getValue();
                        if(dataSnapshot.getValue() != null){
                        user.getInfo(new SimpleCallback<Map<String, Object>>() {
                            @Override
                            public void callback(Map<String, Object> data) {
                                viewHolder.setUser(data.get("name").toString());
                                viewHolder.setUserImage(data.get("img_url").toString());
                            }
                        }, users_id.toString());
                    }
                        mDatabase.child(comment_key).child("comment_img").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                viewHolder.listUrl.clear();
                                listUri.clear();
                                for(DataSnapshot dt:dataSnapshot.getChildren()){
                                    Matcher m = Patterns.WEB_URL.matcher(dt.getValue().toString());
                                    while (m.find()) {
                                        String urls = m.group();
                                        viewHolder.listUrl.add(urls);
                                        listUri.add(urls);
                                    }
                                }
                                viewHolder.setImage(listUri);
                                viewHolder.viewButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent viewImageIntent = new Intent(CommentActivity.this, ImageViewActivity.class);
                                        viewImageIntent.putStringArrayListExtra("urls", viewHolder.listUrl);
                                        startActivity(viewImageIntent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

  /*  model.getListImg(new SimpleCallback<ArrayList<String> >() {
                @Override
                public void callback(ArrayList<String>  data) {
                    if (data != null){
                        listUri.clear();
                        viewHolder.listUrl.clear();
                        String[] url;
                        url= data.toString().split(",");
                        for(int i=0; i<url.length; i++){
                            if(url[i].contains("url")) {
                                Matcher m = Patterns.WEB_URL.matcher(url[i]);
                                while (m.find()) {
                                    String urls = m.group();
                                    listUri.add(urls);
                                    viewHolder.listUrl.add(urls);

                                }
                            }
                        }
                        viewHolder.setImage();
                    }
                }
            },post_key, comment_key );*/

                viewHolder.setText(model.getComment_text());
                viewHolder.setDate(model.getDate());
            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);
    }

    private void startPosting(){
        mProgress.setMessage("Sending....");
        mProgress.show();
        final String textComment = commentText.getText().toString().trim();

        if (!TextUtils.isEmpty(textComment)) {
            final DatabaseReference newComment = mDatabase.push();
            newComment.child("comment_text").setValue(textComment);
            newComment.child("user_id").setValue(user_id);
            newComment.child("date").setValue(date()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mProgress.hide();
                    mProgress.closeOptionsMenu();
                }
            });
            if (imagesUrl != null) {
                mProgress.setMessage("Sending....");
                mProgress.show();
                for (int i = 0; i < imagesUrl.size(); i++) {
                    final int key = i;
                    imageUri = Uri.parse(imagesUrl.get(key).toString());
                    newComment.child("comment_img").child(key + "").child("name").setValue(imageUri.getLastPathSegment());
                    StorageReference filepath = mStorage.child("posts_images").child(post_key).child(newComment.getKey()).child(imageUri.getLastPathSegment());
                    filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            newComment.child("comment_img").child(key + "").child("url").setValue(downloadUrl.toString());

                        }
                    });
                }
                layoutViewImage.removeAllViews();
                mProgress.hide();
                mProgress.closeOptionsMenu();
                imagesUrl.clear();
            }
            commentText.setText("Введите сообщение...");
        }
        else {
            Toast.makeText(CommentActivity.this, "Пожалуйста, введите сообщение", Toast.LENGTH_LONG);
        }

    }

    private Long date(){
        Long date = System.currentTimeMillis()/1000;
        return date;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                ClipData clipData = null;
                clipData = data.getClipData();
                if(clipData == null){
                    imagesUrl.add(data.getData().toString());
                }
                else{
                    for(int i=0; i<clipData.getItemCount(); i++){
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        imagesUrl.add(uri.toString());
                    }
                }
            }
        }
        layoutViewImage.removeAllViews();

        layoutParamsImg = new LinearLayout.LayoutParams(100, 100);
        layoutViewImage = (LinearLayout)findViewById(R.id.image_container);
            for (int i = 0; i < imagesUrl.size(); i++) {

                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
                ImageSize targetSize = new ImageSize(80, 50);
                layoutParamsImg.setMargins(5, 5, 5, 5);
                layoutParamsImg.gravity = Gravity.CENTER;
                final RoundedImageView roundedImageView = new  RoundedImageView(getApplicationContext());
                imageUri = Uri.parse(imagesUrl.get(i).toString());
                roundedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                roundedImageView.setRadius(10);
                roundedImageView.setBorderWidth(4);
                roundedImageView.setSquare(true);
                roundedImageView.setBorderColor(R.color.common_google_signin_btn_text_dark_disabled);
                imageLoader.displayImage(imageUri.toString(), roundedImageView, targetSize);
                roundedImageView.setLayoutParams(layoutParamsImg);

                layoutViewImage.addView(roundedImageView);
                roundedImageView.destroyDrawingCache();

                roundedImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roundedImageView.setVisibility(View.GONE);
                    }
                });
            }
    }


    public static class CommentViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        Button viewButton;
        ArrayList<String> listUrl = new ArrayList<String>();
        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext    = mView.getContext();
            viewButton = new Button(mContext);

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

        private  void setImage(ArrayList<String> images) {
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.image_container_view);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.gravity = Gravity.CENTER;
            layout.removeAllViews();
            for (int i = 0; i < images.size(); i++) {
                if(i<3){
                    RoundedImageView roundedImageView = new  RoundedImageView(mContext);
                    Uri  imageUri = Uri.parse(images.get(i).toString());
                    roundedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    roundedImageView.setRadius(10);
                    roundedImageView.setBorderWidth(4);
                    roundedImageView.setSquare(true);
                    Picasso.with(mContext).load(imageUri).into(roundedImageView);
                    roundedImageView.setLayoutParams(layoutParams);
                    layout.addView(roundedImageView);
                    layout.destroyDrawingCache();
                }
            }
            if(listUrl.size() > 3){
                viewButton.setText("+" + (listUrl.size() - 3));
                viewButton.setAlpha((float) 0.5);

            }
            if(listUrl.size() < 4 && listUrl.size() != 0){
                viewButton.setText("" + listUrl.size());
                viewButton.setAlpha((float) 0.5);
            }
            viewButton.setLayoutParams(layoutParams);
            if(viewButton.getParent()!=null)
                ((ViewGroup)viewButton.getParent()).removeView(viewButton); // <- fix
            if(listUrl.size() != 0 ){
            layout.addView(viewButton);
            }
            layout.destroyDrawingCache();

        }

        private void setDate(String date){
            TextView dateView = (TextView)mView.findViewById(R.id.dateView);
            dateView.setText(date);
        }
    }
}