package br.com.minimadev.starlove.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import br.com.minimadev.starlove.Adapter.ChatListAdapter;
import br.com.minimadev.starlove.Adapter.MatchesAdapter;
import br.com.minimadev.starlove.Objects.MatchesObject;
import br.com.minimadev.starlove.Objects.MessageObject;
import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.R;


import java.util.ArrayList;
import java.util.List;

/**
 * Displays MAthces Available to the user in two recyclerView, one for the most recent ones
 * the other for the ones with the most recent messages
 */
public class MatchesFragment extends Fragment {

    private RecyclerView.Adapter mMatchesAdapter, mChatAdapter;

    private String currentUserID;
    private View view;

    private TextView  mNoMatches, mNoChats;

    public MatchesFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_matches, container, false);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mNoMatches = view.findViewById(R.id.no_matches_layout);
        mNoChats = view.findViewById(R.id.no_chats_layout);

        getUserMatchId();
        initChats();
        initNewMatches();

        return view;
    }

    /**
     * Initializes new Matches recyclerview
     */
    private void initNewMatches(){
        RecyclerView mMatch = view.findViewById(R.id.match);
        mMatch.setNestedScrollingEnabled(false);
        mMatch.setHasFixedSize(false);
        RecyclerView.LayoutManager mMatchesLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mMatch.setLayoutManager(mMatchesLayoutManager);
        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), getContext());
        mMatch.setAdapter(mMatchesAdapter);
    }

    /**
     * Initializes new Chats recyclerview
     */
    private void initChats(){
        RecyclerView mChat = view.findViewById(R.id.chat);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        RecyclerView.LayoutManager mChatLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        mChat.addItemDecoration(dividerItemDecoration);
        mChatAdapter = new ChatListAdapter(getDataSetChat(), getContext());
        mChat.setAdapter(mChatAdapter);
    }

    private void getUserMatchId() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches");
        matchDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        FetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchMatchInformation(String key) {
        for(int i = 0; i < resultsMatches.size(); i++){
            if(resultsMatches.get(i).getUser().getUserId().equals(key))
                return;
        }

        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String  userId = dataSnapshot.getKey(),
                            chatId = "";

                    UserObject mUser = new UserObject();
                    mUser.parseObject(dataSnapshot);

                    if(dataSnapshot.child("connections").child("matches").child(FirebaseAuth.getInstance().getUid()).child("ChatId").getValue()!=null)
                        chatId = dataSnapshot.child("connections").child("matches").child(FirebaseAuth.getInstance().getUid()).child("ChatId").getValue().toString();


                    mNoMatches.setVisibility(View.GONE);

                    for(int i = 0; i < resultsMatches.size(); i++){
                        if(resultsMatches.get(i).getUser().getUserId().equals(userId))
                            return;
                    }

                    MatchesObject obj = new MatchesObject(mUser, chatId, "");
                    resultsMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();
                    if(!chatId.equals(""))
                        FetchLastMessage(chatId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void FetchLastMessage(String key) {

        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Chat").child(key);
        Query query = userDb.orderByKey().limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String chatId = dataSnapshot.getRef().getKey();

                    for (DataSnapshot firstSnapshot: dataSnapshot.getChildren()) { dataSnapshot = firstSnapshot; }

                    MessageObject mMessage = new MessageObject();
                    mMessage.parseObject(dataSnapshot);



                    mNoChats.setVisibility(View.GONE);
                    for(int i = 0; i < resultsMatches.size(); i++){
                        if(resultsMatches.get(i).getChatId().equals(chatId)) {
                            resultsMatches.get(i).setLastMessage(mMessage.getMessage());
                            for(int j = 0; j < resultsChat.size(); j++){
                                if(resultsChat.get(j).getChatId().equals(chatId)){
                                    resultsMatches.get(i).setLastMessage(mMessage.getMessage());
                                    mChatAdapter.notifyDataSetChanged();
                                    return;
                                }
                            }

                            resultsChat.add(resultsMatches.get(i));
                            mChatAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private ArrayList<MatchesObject> resultsChat = new ArrayList<>();
    private List<MatchesObject> getDataSetChat() {
        return resultsChat;
    }

    private ArrayList<MatchesObject> resultsMatches = new ArrayList<>();
    private List<MatchesObject> getDataSetMatches() {
        return resultsMatches;
    }

}