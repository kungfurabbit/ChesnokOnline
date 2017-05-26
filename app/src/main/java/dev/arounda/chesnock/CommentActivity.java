package dev.arounda.chesnock;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
    Button authButton;
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mGalleryButton.setVisibility(View.VISIBLE);
        commentText.setVisibility(View.VISIBLE);
        sendBtn.setVisibility(View.VISIBLE);

        if(!getIntent().hasExtra("user_id")){
            mGalleryButton.setVisibility(View.INVISIBLE);
            commentText.setVisibility(View.INVISIBLE);
            sendBtn.setVisibility(View.INVISIBLE);

             authButton = (Button)findViewById(R.id.auth);
            authButton.setVisibility(View.VISIBLE);
            authButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CommentActivity.this, AuthActivity.class));
                    finish();
                    authButton.setVisibility(View.GONE);

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

                    Toast.makeText(CommentActivity.this,
                            "Удерживайте для выбора нескольких изображений",
                            Toast.LENGTH_LONG).show();
                }
                else  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2){
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_REQUEST);
                }


            }
        });


        mCommentList = (RecyclerView)findViewById(R.id.comment_list);

        setupUI(mCommentList);

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

                Typeface CFB = Typeface.createFromAsset(getAssets(), "NotoSans-Bold.ttf");
                Typeface CFR = Typeface.createFromAsset(getAssets(), "NotoSans-Regular.ttf");
                Typeface RR = Typeface.createFromAsset(getAssets(), "3966.ttf");

                viewHolder.userViewPost.setTypeface(CFB);
                viewHolder.commentViewPost.setTypeface(CFR);
                viewHolder.dateView.setTypeface(RR);


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
                                viewHolder.imageViewButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent viewImageIntent = new Intent(CommentActivity.this, ImageViewActivity.class);
                                        viewImageIntent.putStringArrayListExtra("urls", viewHolder.listUrl);
                                        startActivity(viewImageIntent);
                                    }
                                });
                                if(viewHolder.roundedImageViewArray!=null)
                                    for(int  i =0; i<viewHolder.listUrl.size(); i++){
                                    viewHolder.roundedImageViewArray.get(i).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent viewImageIntent = new Intent(CommentActivity.this, ImageViewActivity.class);
                                            viewImageIntent.putStringArrayListExtra("urls", viewHolder.listUrl);
                                            startActivity(viewImageIntent);
                                        }
                                    });
                                }

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
            commentText.setText(null);
            hideKeyboard(this);
            if(mCommentList.getAdapter().getItemCount()>1)
            mCommentList.smoothScrollToPosition(mCommentList.getAdapter().getItemCount() - 1);
        }
        else {
            Toast.makeText(CommentActivity.this, "Пожалуйста, введите сообщение", Toast.LENGTH_LONG).show();
            mProgress.hide();
            mProgress.closeOptionsMenu();
        }

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private Long date(){
        Long date = System.currentTimeMillis()/1000;
        return date;
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(CommentActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
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
                final int element = i;
                roundedImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roundedImageView.setVisibility(View.GONE);
                        imagesUrl.remove(element);
                    }
                });
            }
    }


    public static class CommentViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        Context mContext;
        Button viewButton;
        ArrayList<String> listUrl = new ArrayList<String>();
        TextView userViewPost;
        TextView commentViewPost;
        CircleImageView userImageView;
        TextView dateView;
        LinearLayout layout;
        Button imageViewButton;
        RoundedImageView roundedImageView;
        ArrayList<RoundedImageView> roundedImageViewArray = new ArrayList<RoundedImageView>();

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mContext    = mView.getContext();
            viewButton = new Button(mContext);
            userViewPost = (TextView)mView.findViewById(R.id.user_textView);
            commentViewPost = (TextView)mView.findViewById(R.id.comment_text);
            userImageView = (CircleImageView) mView.findViewById(R.id.userImageView);
            dateView = (TextView)mView.findViewById(R.id.dateView);
            imageViewButton = (Button)mView.findViewById(R.id.imageViewButton);
            layout = (LinearLayout) mView.findViewById(R.id.image_container_view);

        }

        public void setUser(String user){
            userViewPost.setText(user);
        }

        public void setText(String text){
            commentViewPost.setText(text);
        }

        private  void setUserImage(String image){

            Picasso.with(mContext).load(image).into(userImageView);
        }

        private  void setImage(ArrayList<String> images) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            layoutParams.setMargins(5, 5, 5, 5);
            layoutParams.gravity = Gravity.CENTER;
            layout.removeAllViews();
            for (int i = 0; i < images.size(); i++) {
                if(i<3){
                    roundedImageViewArray.add(new  RoundedImageView(mContext));
                    Uri  imageUri = Uri.parse(images.get(i).toString());
                    roundedImageViewArray.get(roundedImageViewArray.size()-1).setScaleType(ImageView.ScaleType.CENTER_CROP);
                    roundedImageViewArray.get(roundedImageViewArray.size()-1).setRadius(10);
                    roundedImageViewArray.get(roundedImageViewArray.size()-1).setSquare(true);
                    Picasso.with(mContext).load(imageUri).into( roundedImageViewArray.get(roundedImageViewArray.size()-1));
                    roundedImageViewArray.get(roundedImageViewArray.size()-1).setLayoutParams(layoutParams);
                    layout.addView( roundedImageViewArray.get(roundedImageViewArray.size()-1));
                    layout.destroyDrawingCache();
                }
            }
            if(listUrl.size() > 3){
                imageViewButton.setText("+" + (listUrl.size() - 3));
            }
            if(listUrl.size() < 4 && listUrl.size() != 0){
                imageViewButton.setText("" + (listUrl.size()));
            }
            imageViewButton.setLayoutParams(layoutParams);
//            if(viewButton.getParent()!=null)
//                ((ViewGroup)viewButton.getParent()).removeView(viewButton); // <- fix
            layout.addView(imageViewButton);
            if(listUrl.size() != 0 ){
                layout.setVisibility(View.VISIBLE);
            }
            else {
                layout.setVisibility(View.GONE);
            }
            layout.destroyDrawingCache();

        }

        private void setDate(String date){
            dateView.setText(date);
        }
    }
}