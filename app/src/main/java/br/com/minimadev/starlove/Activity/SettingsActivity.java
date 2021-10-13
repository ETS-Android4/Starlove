package br.com.minimadev.starlove.Activity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
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
import com.ramotion.fluidslider.FluidSlider;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.Login.AuthenticationActivity;
import br.com.minimadev.starlove.R;


import java.util.HashMap;
import java.util.Map;

/**
 * Activity that control the search settings of the user:
 *  -Search interest
 *  -Search Distance
 *
 *  Also has option to log out the user
 */
public class SettingsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private SegmentedButtonGroup mRadioGroup;
    FluidSlider mSlider;
    private Button mLogOut;
    private CheckBox mCheckBoxPrefHomem, mCheckBoxPrefMulher, mCheckBoxPrefNonBin;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private TextView mSigno, mAscendente, textViewSigno, textViewAsc;
    private Switch mSwitch;
    private Boolean mSwitchBoolean;
    private AdView mAdView;

    UserObject mUser = new UserObject();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AdView adView = new AdView(this);
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
        textViewSigno = findViewById(R.id.textViewSigno);
        textViewAsc = findViewById(R.id.textViewAsc);

        mAscendente = findViewById(R.id.ascendente);
        mSwitch = findViewById(R.id.switchStarlove);

        mSlider = findViewById(R.id.fluidSlider);
        mLogOut = findViewById(R.id.logOut);
        mCheckBoxPrefHomem = findViewById(R.id.prefHomem);
        mCheckBoxPrefMulher = findViewById(R.id.prefMulher);
        mCheckBoxPrefNonBin = findViewById(R.id.prefNonBin);


        mAuth = FirebaseAuth.getInstance();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        getUserInfo();

        mLogOut.setOnClickListener(v -> logOut());

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b == true) {
                    mSigno.setVisibility(View.GONE);
                    textViewSigno.setVisibility(View.GONE);
                    mAscendente.setVisibility(View.GONE);
                    textViewAsc.setVisibility(View.GONE);

                    mSwitchBoolean = true;
                } else {
                    mSigno.setVisibility(View.VISIBLE);
                    mAscendente.setVisibility(View.VISIBLE);
                    textViewSigno.setVisibility(View.VISIBLE);
                    textViewAsc.setVisibility(View.VISIBLE);
                    mSwitchBoolean = false;
                }
            }
        });

    }


    /**
     * Fetch user search settings and populates elements
     */
    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {return;}

                mUser.parseObject(dataSnapshot);

                mSlider.setPosition(mUser.getSearchDistance() / 100);

                if(mUser.ismPrefHomem())
                    mCheckBoxPrefHomem.setChecked(true);
                else mCheckBoxPrefHomem.setChecked(false);
                if(mUser.ismPrefMulher())
                    mCheckBoxPrefMulher.setChecked(true);
                else mCheckBoxPrefMulher.setChecked(false);
                if(mUser.ismPrefNonBin())
                    mCheckBoxPrefNonBin.setChecked(true);
                else mCheckBoxPrefNonBin.setChecked(false);

                if(mUser.getmSwitchBoolean() == true)
                    mSwitch.setChecked(true);
                else mSwitch.setChecked(false);

                mSigno.setText(mUser.getmPrefSigno());
                mAscendente.setText(mUser.getmPrefAscendente());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saves user search settings to the database
     */
    private void saveUserInformation() {

        String signo = mSigno.getText().toString();
        String ascendente = mAscendente.getText().toString();

        if (!mCheckBoxPrefHomem.isChecked() && !mCheckBoxPrefMulher.isChecked() && !mCheckBoxPrefNonBin.isChecked()) {
            Toast.makeText(SettingsActivity.this, "Selecione pelo menos um gênero", Toast.LENGTH_SHORT).show();
            return;
        } else {

            Map userInfo = new HashMap();

            if (mCheckBoxPrefHomem.isChecked())
                userInfo.put("mPrefHomem", true);
            else
                userInfo.put("mPrefHomem", false);

            if (mCheckBoxPrefMulher.isChecked())
                userInfo.put("mPrefMulher", true);
            else
                userInfo.put("mPrefMulher", false);

            if (mCheckBoxPrefNonBin.isChecked())
                userInfo.put("mPrefNonBin", true);
            else
                userInfo.put("mPrefNonBin", false);


            if(mSwitchBoolean != null)
                userInfo.put("mSwitchBoolean", mSwitchBoolean);

            userInfo.put("mPrefSigno", signo);
            userInfo.put("mPrefAscendente", ascendente);




        userInfo.put("search_distance", Math.round(mSlider.getPosition() * 100));
        mUserDatabase.updateChildren(userInfo);
    }
    }

    /**
     * Logs out user and takes it to the AuthenticationActivity
     */
    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        saveUserInformation();
        finish();
        return false;
    }

    public void showSignosR(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_signos);
        popup.show();
    }
    public void showAscendenteR(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_ascendente);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {


        switch (menuItem.getItemId()) {

            case R.id.naoInfo:
                mSigno.setText("Todos");
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
                mAscendente.setText("Todos");
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
}
