package br.com.minimadev.starlove.Objects;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

/**
 * Object of each message
 */

public class MessageObject {
    private String message = "--";
    private Boolean currentUser = false;

    public MessageObject(){}

    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}

        if(dataSnapshot.child("text").getValue()!=null)
            message = dataSnapshot.child("text").getValue().toString();

        String createdByUser;
        if(dataSnapshot.child("createdByUser").getValue()!=null){
            createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
            if(createdByUser.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                currentUser = true;
            }
        }
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String userID){
        this.message = message;
    }

    public Boolean getCurrentUser(){
        return currentUser;
    }
    public void setCurrentUser(Boolean currentUser){
        this.currentUser = currentUser;
    }
}
