package br.com.minimadev.starlove.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import br.com.minimadev.starlove.Objects.UserObject;
import br.com.minimadev.starlove.Adapter.CardAdapter;
import br.com.minimadev.starlove.Activity.MainActivity;

import br.com.minimadev.starlove.R;
import br.com.minimadev.starlove.Utils.SendNotification;
import br.com.minimadev.starlove.Activity.ZoomCardActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the cards to the user
 *
 * It displays them in a way that is within the search params of the current logged in user
 */
public class CardFragment  extends Fragment {

    private CardAdapter cardAdapter;
    int searchDistance = 100;

    private FirebaseAuth mAuth;

    private String currentUId;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    private DatabaseReference usersDb;

    private Boolean mPrefHomem =false,
            mPrefMulher = false,
            mPrefNonBin = false,
            mSwitchBoolean;

    private String mSex, mPrefSigno, mPrefAscendente, mSigno, mAscendente;

    List<UserObject> rowItems;

    View view;

    public CardFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_card, container, false);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
            return view;
        currentUId = mAuth.getCurrentUser().getUid();

        fetchUserSearchParams();

        rowItems = new ArrayList<>();

        cardAdapter = new CardAdapter(getContext(), R.layout.item_card, rowItems );

        final SwipeFlingAdapterView flingContainer = view.findViewById(R.id.frame);

        flingContainer.setAdapter(cardAdapter);

        //Handling swipe of cards
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                cardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                UserObject obj = (UserObject) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                UserObject obj = (UserObject) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener((itemPosition, dataObject) -> {
            UserObject UserObject = (UserObject) dataObject;
            Intent i = new Intent(getContext(), ZoomCardActivity.class);
            i.putExtra("UserObject", UserObject);
            startActivity(i);
        });




        FloatingActionButton fabLike = view.findViewById(R.id.fabLike);
        FloatingActionButton fabNope = view.findViewById(R.id.fabNope);

        //Listeners for the fab buttons, they do the same as the swipe feature, but withe the click of the buttons
        fabLike.setOnClickListener(v -> {
            if(rowItems.size()!=0)
                flingContainer.getTopCardListener().selectRight();
        });
        fabNope.setOnClickListener(v -> {
            if(rowItems.size()!=0)
                flingContainer.getTopCardListener().selectLeft();
        });
        return view;
    }


    GeoQuery geoQuery;
    /**
     * Fetch closest users to the current user using a GeoQuery.
     *
     * The users found are within a radius defined in the SearchObject and the center of
     * the radius is the current user's location
     * @param lastKnowLocation - user last know location
     */
    public void getCloseUsers(Location lastKnowLocation){
        if(rowItems != null)
          rowItems.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("location");
        GeoFire geoFire = new GeoFire(ref);

        if(geoQuery!=null)
            geoQuery.removeAllListeners();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()), 100);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                getUsersInfo(key);
            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }
            @Override
            public void onGeoQueryReady() {
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    /**
     * Checks if new connection is a match if it is then add it to the database and create a new chat
     * @param userId
     */
    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    showMatchAlert();


                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);

                    SendNotification sendNotification = new SendNotification();
                    sendNotification.SendNotification("Olha só!", "Novo Match!", dataSnapshot.getKey());

                    Snackbar.make(view.findViewById(R.id.layout), "Novo Match!", Snackbar.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showMatchAlert() {

        dialogBuilder = new AlertDialog.Builder(getContext());
        View layoutView = getLayoutInflater().inflate(R.layout.match_alert, null);
        Button dialogButton = layoutView.findViewById(R.id.btnDialog);
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


    /**
     * Fetches user search params from the database
     *
     * After that call isLocationEnabled which will see if the location services are enabled
     * and then fetch the last location known.
     */
    private void fetchUserSearchParams(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){


                    if(dataSnapshot.child("mPrefHomem").getValue() != null)
                        mPrefHomem = (Boolean) dataSnapshot.child("mPrefHomem").getValue();

                    if(dataSnapshot.child("mPrefMulher").getValue() != null)
                        mPrefMulher = (Boolean) dataSnapshot.child("mPrefMulher").getValue();

                    if(dataSnapshot.child("mPrefNonBin").getValue() != null)
                        mPrefNonBin = (Boolean) dataSnapshot.child("mPrefNonBin").getValue();

                    if(dataSnapshot.child("sex").getValue() != null)
                        mSex = dataSnapshot.child("sex").getValue().toString();

                    if(dataSnapshot.child("signo").getValue() != null)
                        mSigno = dataSnapshot.child("signo").getValue().toString();
                    
                    if(dataSnapshot.child("ascendente").getValue() != null)
                        mAscendente = dataSnapshot.child("ascendente").getValue().toString();
                    
                    if(dataSnapshot.child("mPrefSigno").getValue() != null)
                        mPrefSigno = (String) dataSnapshot.child("mPrefSigno").getValue().toString();

                    if(dataSnapshot.child("mPrefAscendente").getValue() != null)
                        mPrefAscendente = (String) dataSnapshot.child("mPrefAscendente").getValue().toString();

                    if(dataSnapshot.child("mSwitchBoolean").getValue() != null)
                        mSwitchBoolean = (Boolean) dataSnapshot.child("mSwitchBoolean").getValue();






                    if (dataSnapshot.child("search_distance").getValue() != null)
                        searchDistance = Integer.parseInt(dataSnapshot.child("search_distance").getValue().toString());

                    ((MainActivity)getActivity()).isLocationEnable();
                    rowItems.clear();
                    cardAdapter.notifyDataSetChanged();


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Get info of a user and check if that user is within the search params, if it is then
     * add it to the list and update the adapter.
     *
     * Does not add the user if it is already a connection.
     * @param userId - id of the user that's a possible user to display the card of
     */
    private void getUsersInfo(String userId){

        usersDb.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){return;}

                UserObject mUser = new UserObject();
                mUser.parseObject(dataSnapshot);

                if(mUser.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){return;}
                if(dataSnapshot.child("connections").child("nope").hasChild(currentUId)){return;}
                if(dataSnapshot.child("connections").child("yeps").hasChild(currentUId)) {return;}



                if(mPrefHomem) {
                    if(mUser.getUserSex().equals("Male")) {
                        if (mSex.equals("Male") && mUser.ismPrefHomem())

                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);
                    }



                        if (mSex.equals("Female") && mUser.ismPrefMulher())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);


                        if (mSex.equals("NonBin") && mUser.ismPrefNonBin()) {
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);

                        }
                    }



                if(mPrefMulher) {
                    if(mUser.getUserSex().equals("Female")) {
                        if (mSex.equals("Male") && mUser.ismPrefHomem())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);

                        if (mSex.equals("Female") && mUser.ismPrefMulher())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);

                        if (mSex.equals("NonBin") && mUser.ismPrefNonBin())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);
                    }
                }


                if(mPrefNonBin) {
                    if(mUser.getUserSex().equals("NonBin")){
                        if (mSex.equals("Male") && mUser.ismPrefHomem())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);

                        if (mSex.equals("Female") && mUser.ismPrefMulher())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);

                        if (mSex.equals("NonBin") && mUser.ismPrefNonBin())
                            if (!mSwitchBoolean) {

                                checkSignoPref(mPrefSigno, mUser);
                                checkAscPref(mPrefAscendente, mUser);


                            } else sugestaoStarlove(mSigno, mUser);
                    }


                }


                cardAdapter.notifyDataSetChanged();



                }

            private void sugestaoStarlove(String mSigno, UserObject mUser) {
                switch (mSigno) {

                    case "Não informado":
                        Toast.makeText(getContext(), "Favor informar seu signo!", Toast.LENGTH_SHORT).show();
                        rowItems.add(mUser);
                        return;
                    case "Leão":
                        if (mUser.getSigno().equals("Aquário"))
                            rowItems.add(mUser);
                        return;

                    case "Aries":
                        if (mUser.getSigno().equals("Libra"))
                            rowItems.add(mUser);
                        return;

                    case "Peixes":
                        if (mUser.getSigno().equals("Virgem"))
                            rowItems.add(mUser);
                        return;

                    case "Escorpião":
                        if (mUser.getSigno().equals("Touro"))
                            rowItems.add(mUser);
                        return;

                    case "Capricórnio":
                        if (mUser.getSigno().equals("Câncer"))
                            rowItems.add(mUser);
                        return;

                    case "Câncer":
                        if (mUser.getSigno().equals("Capricórnio"))
                            rowItems.add(mUser);
                        return;

                    case "Virgem":
                        if (mUser.getSigno().equals("Peixes"))
                            rowItems.add(mUser);
                        return;

                    case "Touro":
                        if (mUser.getSigno().equals("Escorpião"))
                            rowItems.add(mUser);
                        return;

                    case "Sagitário":
                        if (mUser.getSigno().equals("Gêmeos"))
                            rowItems.add(mUser);
                        return;

                    case "Gêmeos":
                        if (mUser.getSigno().equals("Sagitário"))
                            rowItems.add(mUser);
                        return;

                    case "Aquário":
                        if (mUser.getSigno().equals("Leão"))
                            rowItems.add(mUser);
                        return;

                    case "Libra":
                        if (mUser.getSigno().equals("Aries"))
                            rowItems.add(mUser);
                        return;
                    default:
                        Toast.makeText(getContext(), "teste default", Toast.LENGTH_SHORT).show();
                        return;

                }


            }

            private void checkAscPref(String mPrefAscendente, UserObject mUser) {
                switch (mPrefAscendente) {

                    case "Todos":
                        return;

                    case "Leão":
                        if (!mUser.getAscendente().equals("Leão"))
                            rowItems.remove(mUser);
                        return;

                    case "Aries":
                        if (!mUser.getAscendente().equals("Aries"))
                            rowItems.remove(mUser);
                        return;

                    case "Peixes":
                        if (!mUser.getAscendente().equals("Peixes"))
                            rowItems.remove(mUser);
                        return;

                    case "Escorpião":
                        if (!mUser.getAscendente().equals("Escorpião"))
                            rowItems.remove(mUser);
                        return;

                    case "Capricórnio":
                        if (!mUser.getAscendente().equals("Capricórnio"))
                            rowItems.remove(mUser);
                        return;

                    case "Câncer":
                        if (!mUser.getAscendente().equals("Câncer"))
                            rowItems.remove(mUser);
                        return;

                    case "Virgem":
                        if (!mUser.getAscendente().equals("Virgem"))
                            rowItems.remove(mUser);
                        return;

                    case "Touro":
                        if (!mUser.getAscendente().equals("Touro"))
                            rowItems.remove(mUser);
                        return;

                    case "Sagitário":
                        if (!mUser.getAscendente().equals("Sagitário"))
                            rowItems.remove(mUser);
                        return;

                    case "Gêmeos":
                        if (!mUser.getAscendente().equals("Gêmeos"))
                            rowItems.remove(mUser);
                        return;

                    case "Aquário":
                        if (!mUser.getAscendente().equals("Aquário"))
                            rowItems.remove(mUser);
                        return;

                    case "Libra":
                        if (!mUser.getAscendente().equals("Libra"))
                            rowItems.remove(mUser);
                        return;
                    default:
                        return;

                }




            }

            private void checkSignoPref(String mPrefSigno, UserObject mUser) {
                switch (mPrefSigno) {

                    case "Todos":
                        rowItems.add(mUser);
                        return;

                    case "Leão":
                        if (mUser.getSigno().equals("Leão"))
                            rowItems.add(mUser);
                        return;

                    case "Aries":
                        if (mUser.getSigno().equals("Aries"))
                            rowItems.add(mUser);
                        return;

                    case "Peixes":
                        if (mUser.getSigno().equals("Peixes"))
                            rowItems.add(mUser);
                        return;

                    case "Escorpião":
                        if (mUser.getSigno().equals("Escorpião"))
                            rowItems.add(mUser);
                        return;

                    case "Capricórnio":
                        if (mUser.getSigno().equals("Capricórnio"))
                            rowItems.add(mUser);
                        return;

                    case "Câncer":
                        if (mUser.getSigno().equals("Câncer"))
                            rowItems.add(mUser);
                        return;

                    case "Virgem":
                        if (mUser.getSigno().equals("Virgem"))
                            rowItems.add(mUser);
                        return;

                    case "Touro":
                        if (mUser.getSigno().equals("Touro"))
                            rowItems.add(mUser);
                        return;

                    case "Sagitário":
                        if (mUser.getSigno().equals("Sagitário"))
                            rowItems.add(mUser);
                        return;

                    case "Gêmeos":
                        if (mUser.getSigno().equals("Gêmeos"))
                            rowItems.add(mUser);
                        return;

                    case "Aquário":
                        if (mUser.getSigno().equals("Aquário"))
                            rowItems.add(mUser);
                        return;

                    case "Libra":
                        if (mUser.getSigno().equals("Libra"))
                            rowItems.add(mUser);
                        return;
                    default:
                        return;

                }
            }




            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}