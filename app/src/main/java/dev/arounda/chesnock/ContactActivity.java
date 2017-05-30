package dev.arounda.chesnock;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "0673664422")));
            }
        });



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface CFB = Typeface.createFromAsset(getAssets(), "NotoSans-Bold.ttf");
        TextView phone = (TextView)findViewById(R.id.phoneNumber);
        TextView email = (TextView)findViewById(R.id.emailText);
        TextView ToolTitle = (TextView)findViewById(R.id.toolbar_title);
        ToolTitle.setTypeface(CFB);
        phone.setTypeface(CFB);
        email.setTypeface(CFB);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "0673664422")));
            }
        });


        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, "r.berezhnyak@gmail.com");
                intent.setData(Uri.parse("mailto:"+"r.berezhnyak@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_FROM_BACKGROUND);

                try {
                    startActivity(Intent.createChooser(intent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ContactActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}
