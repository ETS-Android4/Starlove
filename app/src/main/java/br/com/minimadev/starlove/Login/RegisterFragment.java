package br.com.minimadev.starlove.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;
import java.util.Map;

import br.com.minimadev.starlove.R;

/**
 * Fragment Responsible for registering a new user
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    EditText mName, mEmail, mPassword;
    Button mRegister;
    SegmentedButtonGroup mRadioGroup;


    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_registration, container, false);
        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }


    /**
     * Register the user, but before that check if every field is correct.
     * After that registers the user and creates an entry for it oin the database
     */
    private void register(){
        if(mEmail.getText().length()==0) {
            mEmail.setError("please fill this field");
            return;
        }
        if(mName.getText().length()==0) {
            mName.setError("please fill this field");
            return;
        }
        if(mPassword.getText().length()==0) {
            mPassword.setError("please fill this field");
            return;
        }
        if(mPassword.getText().length()< 6) {
            mPassword.setError("password must have at least 6 characters");
            return;
        }




        final String name = mName.getText().toString();
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String accountType;
        int selectId = mRadioGroup.getPosition();


        switch (selectId){
            case 0:
                accountType = "Male";
                break;
                case 1:
                accountType = "Female";
                break;
             case 2:
                accountType = "NonBin";
                break;

            default: accountType = "";
            break;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Snackbar.make(view.findViewById(R.id.layout), "sign up error", Snackbar.LENGTH_SHORT).show();
                }else{
                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Map userInfo = new HashMap();
                    userInfo.put("name", name);
                    userInfo.put("sex", accountType);
                    userInfo.put("search_distance", 100);
                    userInfo.put("profileImageUrl", "default");

                    userInfo.put("signo", "Não Informado");
                    userInfo.put("ascendente", "Não Informado");
                    userInfo.put("mPrefSigno", "Todos");
                    userInfo.put("mPrefAscendente", "Todos");
                    userInfo.put("mPrefHomem", true);
                    userInfo.put("mPrefMulher", true);
                    userInfo.put("mPrefNonBin", true);
                    userInfo.put("mSwitchBoolean", false);

                    FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).updateChildren(userInfo);
                }
            }
        });

    }

    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects(){
        mEmail = view.findViewById(R.id.email);
        mName = view.findViewById(R.id.name);
        mPassword = view.findViewById(R.id.password);
        mRegister = view.findViewById(R.id.register);
        mRadioGroup = view.findViewById(R.id.radioRealButtonGroup);

        mRegister.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                register();;
        }
    }
}