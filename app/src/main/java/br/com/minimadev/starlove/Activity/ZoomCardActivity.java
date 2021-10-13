package br.com.minimadev.starlove.Activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.R;


/**
 * Activity displayed when user clicks on a card
 *
 * It displays the user that was clicked on's information in detail
 */
public class ZoomCardActivity extends AppCompatActivity {

    private TextView    mName,
                        mJob,
                        mAbout;

    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_card);

        Intent i = getIntent();
        UserObject mUserObject = (UserObject)i.getSerializableExtra("UserObject");

        mName = findViewById(R.id.name);
        mJob = findViewById(R.id.job);
        mAbout = findViewById(R.id.about);
        mImage = findViewById(R.id.image);

        mName.setText(mUserObject.getName() + ", " + mUserObject.getSigno());
        mJob.setText(mUserObject.getJob());
        mAbout.setText(mUserObject.getAbout());

        if(!mUserObject.getProfileImageUrl().equals("default"))
            Glide.with(getApplicationContext()).load(mUserObject.getProfileImageUrl()).into(mImage);
    }



}
