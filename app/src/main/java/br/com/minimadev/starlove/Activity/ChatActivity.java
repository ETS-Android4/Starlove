package br.com.minimadev.starlove.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import br.com.minimadev.starlove.Adapter.MessageAdapter;
import br.com.minimadev.starlove.Objects.MessageObject;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.R;
import br.com.minimadev.starlove.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Activity responsible for displaying chat's messages and for sending messages
 */
public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private LinearLayoutManager mChatLayoutManager;

    private EditText mSendEditText;

    private ImageView mImage;

    private TextView mName;

    private String currentUserID, matchId, chatId;

    DatabaseReference mDatabaseUser, mDatabaseChat;

    private ArrayList<MessageObject> resultsChat = new ArrayList<MessageObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        matchId = getIntent().getExtras().getString("matchId");

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("ChatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");

        getChatId();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(true);
        mChatLayoutManager = new LinearLayoutManager(this);
        mChatLayoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(resultsChat, this);
        mRecyclerView.setAdapter(mChatAdapter);


        mName = findViewById(R.id.name);
        mImage = findViewById(R.id.image);

        mSendEditText = findViewById(R.id.message);
        ImageView mSendButton = findViewById(R.id.send);
        ImageView mBack = findViewById(R.id.back);

        mBack.setOnClickListener(v -> finish());

        mSendButton.setOnClickListener(view -> sendMessage());

        getMatchInfo();
    }

    /**
     * Send new message to this chat
     *
     * Checks if text to send is empty before that.
     */
    private void sendMessage() {
        String sendMessageText = mSendEditText.getText().toString();

        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserID);
            newMessage.put("text", sendMessageText);

            SendNotification sendNotification = new SendNotification();
            sendNotification.SendNotification(sendMessageText, "new Message!", matchId);

            newMessageDb.setValue(newMessage);
        }
        mSendEditText.setText(null);
    }

    /**
     * Get chatId and calls the getChatMessages() after
     */
    private void getChatId(){
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Get every message of the current chat and populates the recycler view
     * by adding each MessageObject to the resultsChat and updating the adapter
     *
     */
    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    MessageObject mMessage = new MessageObject();
                    mMessage.parseObject(dataSnapshot);

                    resultsChat.add(mMessage);
                    mChatLayoutManager.scrollToPosition(resultsChat.size() - 1);
                    mChatAdapter.notifyDataSetChanged();

                    //makes sure the recyclerview scrolls to the newest message
                    mRecyclerView.postDelayed(() -> mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1), 1000);


                }

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }




    /**
     * Fetches match info and populates the related design elements
     */
    private void getMatchInfo(){
        DatabaseReference mMatchDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId);
        mMatchDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){

                    UserObject mUser = new UserObject();
                    mUser.parseObject(dataSnapshot);

                    mName.setText(mUser.getName());
                    if(!mUser.getProfileImageUrl().equals("default"))
                        Glide.with(getApplicationContext()).load(mUser.getProfileImageUrl()).apply(RequestOptions.circleCropTransform()).into(mImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
