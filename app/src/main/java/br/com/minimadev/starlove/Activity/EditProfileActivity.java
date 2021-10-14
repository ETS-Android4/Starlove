package br.com.minimadev.starlove.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Activity responsible for handling the edit of user's data
 */
public class EditProfileActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private EditText    mName,
                        mPhone,
                        mJob,
                        mAbout;

    private TextView mIdade, mSigno, mAscendente;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private SegmentedButtonGroup mRadioGroup;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    private ImageView mProfileImage;
    private Button mSalvar;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private AdView mAdView;

    UserObject mUser = new UserObject();


    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AdView adView = new AdView(EditProfileActivity.this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7555075543404286/1223263083");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }

        });


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mSigno = findViewById(R.id.signo);
        mAscendente = findViewById(R.id.ascendente);

        mName = findViewById(R.id.name);
        mPhone = findViewById(R.id.phone);
        mJob = findViewById(R.id.job);
        mAbout = findViewById(R.id.about);
        mRadioGroup = findViewById(R.id.radioRealButtonGroup);
        mProfileImage = findViewById(R.id.profileImage);
        mSalvar = findViewById(R.id.edit_save);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mIdade = findViewById(R.id.idade);


        /*
        mDataNasc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(EditProfileActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
                dialog.show();
            }
        });


        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String date = day + "/" + month + "/" + year;
                mDataNasc.setText(date);


            }
        };
        */

        showWelcome();
        getUserInfo();

        //on profile image click allow user to choose another pic by caling the responding intentt
        mProfileImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });
        mSalvar.setOnClickListener(view -> saveUserInformation() );
    }



    /**
     * Get user's current info data and populate the corresponding Elements
     */
    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser.parseObject(dataSnapshot);


                mSigno.setText(mUser.getSigno());
                mAscendente.setText(mUser.getAscendente());
                mName.setText(mUser.getName());
                mPhone.setText(mUser.getPhone());
                mIdade.setText(mUser.getIdade());
                mJob.setText(mUser.getJob());
                mAbout.setText(mUser.getAbout());
                if(!mUser.getProfileImageUrl().equals("default"))
                    Glide.with(getApplicationContext()).load(mUser.getProfileImageUrl()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                if(mUser.getUserSex().equals("Male"))
                    mRadioGroup.setPosition(0, true);
                else if(mUser.getUserSex().equals("Female"))
                    mRadioGroup.setPosition(1, true);
                else
                    mRadioGroup.setPosition(2, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Store user's info in the database
     * if the image has been changed then upload it to the storage
     */
    private void saveUserInformation() {

        String userSex = "";

        String signo = mSigno.getText().toString();
        String ascendente = mAscendente.getText().toString();
        String name = mName.getText().toString();
        String phone = mPhone.getText().toString();
        String age = mIdade.getText().toString();
        String job = mJob.getText().toString();
        String about = mAbout.getText().toString();
        if(mRadioGroup.getPosition() == 0)
            userSex = "Male";
        if(mRadioGroup.getPosition() == 1)
            userSex = "Female";
        if(mRadioGroup.getPosition() == 2)
            userSex = "NonBin";

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("age", age);
        userInfo.put("job", job);
        userInfo.put("sex", userSex);
        userInfo.put("about", about);
        userInfo.put("signo", signo);
        userInfo.put("ascendente", ascendente);

        mUserDatabase.updateChildren(userInfo);
        Intent intent = new Intent(EditProfileActivity.this, SettingsActivity.class);


        if(resultUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            UploadTask uploadTask = filePath.putFile(resultUri);

            uploadTask.addOnFailureListener(e -> {
                startActivity(intent);
                finish();
                return;
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                Map newImage = new HashMap();
                newImage.put("profileImageUrl", uri.toString());
                mUserDatabase.updateChildren(newImage);

                startActivity(intent);
                finish();

            }).addOnFailureListener(exception -> {
                startActivity(intent);
                finish();
            }));
        }else{
            startActivity(intent);
            finish();
        }
    }

    /**
     * Get the uri of the image the user picked if the result is successful
     * @param requestCode - code of the request ( for the image request is 1)
     * @param resultCode - if the result was successful
     * @param data - data of the image fetched
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                Glide.with(getApplication())
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mProfileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        saveUserInformation();
        return false;
    }

    public void showSignos(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_signos);
        popup.show();
    }
    public void showAscendente(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_ascendente);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {


        switch (menuItem.getItemId()){

            case R.id.naoInfo:
                mSigno.setText("Não informado");
                return true;

            case R.id.leao:
                mSigno.setText("Leão");
                return true;

            case R.id.aries:
                mSigno.setText("Aries");
                return true;

            case R.id.peixes:
                mSigno.setText("Peixes");
                return true;

            case R.id.escorpiao:
                mSigno.setText("Escorpião");
                return true;

            case R.id.capricornio:
                mSigno.setText("Capricórnio");
                return true;

            case R.id.cancer:
                mSigno.setText("Câncer");
                return true;

            case R.id.virgem:
                mSigno.setText("Virgem");
                return true;

            case R.id.touro:
                mSigno.setText("Touro");
                return true;

            case R.id.sagitario:
                mSigno.setText("Sagitário");
                return true;

            case R.id.gemeos:
                mSigno.setText("Gêmeos");
                return true;

            case R.id.aquario:
                mSigno.setText("Aquário");
                return true;

            case R.id.libra:
                mSigno.setText("Libra");
                return true;

            case R.id.naoInfoAsc:
                mAscendente.setText("Não informado");
                return true;

            case R.id.leaoAsc:
                mAscendente.setText("Leão");
                return true;

            case R.id.ariesAsc:
                mAscendente.setText("Aries");
                return true;

            case R.id.peixesAsc:
                mAscendente.setText("Peixes");
                return true;

            case R.id.escorpiaoAsc:
                mAscendente.setText("Escorpião");
                return true;

            case R.id.capricornioAsc:
                mAscendente.setText("Capricórnio");
                return true;

            case R.id.cancerAsc:
                mAscendente.setText("Câncer");
                return true;

            case R.id.virgemAsc:
                mAscendente.setText("Virgem");
                return true;

            case R.id.touroAsc:
                mAscendente.setText("Touro");
                return true;

            case R.id.sagitarioAsc:
                mAscendente.setText("Sagitário");
                return true;

            case R.id.gemeosAsc:
                mAscendente.setText("Gêmeos");
                return true;

            case R.id.aquarioAsc:
                mAscendente.setText("Aquário");
                return true;

            case R.id.libraAsc:
                mAscendente.setText("Libra");
                return true;


            default:
                return false;




        }
    }
    private void showWelcome() {

        dialogBuilder = new android.app.AlertDialog.Builder(this);
        View layoutView = getLayoutInflater().inflate(R.layout.bemvindo, null);
        Button dialogButton = layoutView.findViewById(R.id.buttonContinuar);
        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}
