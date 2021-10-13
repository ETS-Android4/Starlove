package br.com.minimadev.starlove.Objects;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;

/**
 * Object of each card
 */
public class UserObject implements Serializable {
    private String  userId = "--";
    private String name = "--";
    private String profileImageUrl = "default";
    private String idade = "";


    private String signo = "Todos";
    private String ascendente = "Todos";


    private String mPrefSigno = "Todos";
    private String mPrefAscendente = "Todos";
    private boolean mSwitchBoolean = true;


    private String about = "--";
    private String job = "--";
    private String userSex = "--";
    private String phone = "--";
    private boolean mPrefHomem = true;
    private boolean mPrefMulher = true;
    private boolean mPrefNonBin = true;


    float searchDistance = 100;

    public UserObject(){}


    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}
        userId = dataSnapshot.getKey();

        if(dataSnapshot.child("name").getValue()!=null)
            name = dataSnapshot.child("name").getValue().toString();
        if(dataSnapshot.child("sex").getValue()!=null)
            userSex = dataSnapshot.child("sex").getValue().toString();
        if(dataSnapshot.child("age").getValue()!=null)
            idade = dataSnapshot.child("age").getValue().toString();
        if(dataSnapshot.child("job").getValue()!=null)
            job = dataSnapshot.child("job").getValue().toString();
        if(dataSnapshot.child("about").getValue()!=null)
            about = dataSnapshot.child("about").getValue().toString();
        if (dataSnapshot.child("profileImageUrl").getValue()!=null)
            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
        if (dataSnapshot.child("phone").getValue()!=null)
            phone = dataSnapshot.child("phone").getValue().toString();
        if (dataSnapshot.child("signo").getValue()!=null)
            signo = dataSnapshot.child("signo").getValue().toString();
        if (dataSnapshot.child("ascendente").getValue()!=null)
            ascendente = dataSnapshot.child("ascendente").getValue().toString();
        if (dataSnapshot.child("mPrefAscendente").getValue()!=null)
            mPrefAscendente = dataSnapshot.child("mPrefAscendente").getValue().toString();
        if (dataSnapshot.child("mPrefSigno").getValue()!=null)
            mPrefSigno = dataSnapshot.child("mPrefSigno").getValue().toString();
        if (dataSnapshot.child("mSwitchBoolean").getValue()!=null)
            mSwitchBoolean = (Boolean) dataSnapshot.child("mSwitchBoolean").getValue();






        if(dataSnapshot.child("mPrefHomem").getValue() != null)
            if((boolean) dataSnapshot.child("mPrefHomem").getValue() == true)
                mPrefHomem = true;
            else mPrefHomem = false;

        if(dataSnapshot.child("mPrefMulher").getValue() != null)
            if((boolean) dataSnapshot.child("mPrefMulher").getValue() == true)
                mPrefMulher = true;
            else mPrefMulher = false;

        if(dataSnapshot.child("mPrefNonBin").getValue() != null)
            if((boolean) dataSnapshot.child("mPrefNonBin").getValue() == true)
                mPrefNonBin = true;
            else mPrefNonBin = false;

        if(dataSnapshot.child("search_distance").getValue()!=null)
            searchDistance = Float.parseFloat(dataSnapshot.child("search_distance").getValue().toString());

    }

    public String getUserId(){
        return userId;
    }
    public String getName(){
        return name;
    }
    public String getIdade(){
        return idade;
    }
    public String getAbout(){
        return about;
    }
    public String getJob(){
        return job;
    }
    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public float getSearchDistance() {
        return searchDistance;
    }


    public String getUserSex() {
        return userSex;
    }

    public String getPhone() {
        return phone;
    }
    public boolean ismPrefHomem() {
        return mPrefHomem;
    }

    public boolean ismPrefMulher() {
        return mPrefMulher;
    }

    public boolean ismPrefNonBin() {
        return mPrefNonBin;
    }

    public String getSigno() { return signo; }

    public String getAscendente() {return ascendente;}

    public String getmPrefSigno() { return mPrefSigno; }
    public String getmPrefAscendente() { return mPrefAscendente; }
    public Boolean getmSwitchBoolean() { return mSwitchBoolean; }

}
