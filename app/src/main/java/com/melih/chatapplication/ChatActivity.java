package com.melih.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    EditText messageText;
    RecylerViewAdaper recylerViewAdaper;

    private ArrayList<String> chatMessages = new ArrayList<>();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        messageText = findViewById(R.id.edittext_chatactivity_message);
        recylerViewAdaper = new RecylerViewAdaper(chatMessages);

        RecyclerView.LayoutManager recylemanager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recylemanager);
        recyclerView.setAdapter(recylerViewAdaper);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        getdata();

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(final String userId, String registrationId) {

                UUID uuid = UUID.randomUUID();
                final String uuidtostring= uuid.toString();

                DatabaseReference databaseReference1 = firebaseDatabase.getReference("UserIDs");
                databaseReference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> playerIDs = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                            String currentId = hashMap.get("PlayerID");
                            playerIDs.add(currentId);
                        }
                        if(!playerIDs.contains(userId)){
                            databaseReference.child("UserIDs").child(uuidtostring).child("PlayerID").setValue(userId);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.option_menu_signout){
            firebaseAuth.signOut();
            Intent intent = new Intent(ChatActivity.this,SignUpActivity.class);
            startActivity(intent);
            finish();
        }
        if(item.getItemId() == R.id.option_menu_profile){
            Intent intent = new Intent(ChatActivity.this,ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendmessage(View view){
        final String messagetosend = messageText.getText().toString();

        UUID uuid = UUID.randomUUID();
        String uuidstring = uuid.toString();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        databaseReference.child("Chats").child(uuidstring).child("usermessage").setValue(messagetosend);
        databaseReference.child("Chats").child(uuidstring).child("useremail").setValue(firebaseUser.getEmail());
        databaseReference.child("Chats").child(uuidstring).child("usermessagetime").setValue(ServerValue.TIMESTAMP);
        messageText.setText("");

        getdata();

        //onesignal
        DatabaseReference databaseReference =firebaseDatabase.getReference("UserIDs");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String PlayerId = hashMap.get("PlayerID");
                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'"+messagetosend+"'}, 'include_player_ids': ['" + PlayerId + "']}"), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getdata(){
        DatabaseReference databaseReference1 = firebaseDatabase.getReference("Chats");
        Query query = databaseReference1.orderByChild("usermessagetime");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatMessages.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String usermessage = hashMap.get("usermessage");
                    String useremail = hashMap.get("useremail");
                    chatMessages.add(useremail + " : " + usermessage);

                    recylerViewAdaper.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
