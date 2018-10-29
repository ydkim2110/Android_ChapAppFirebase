package com.example.anti2110.chatappfirebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.anti2110.chatappfirebase.Adapter.MessageAdapter;
import com.example.anti2110.chatappfirebase.Fragments.APIService;
import com.example.anti2110.chatappfirebase.Model.Chat;
import com.example.anti2110.chatappfirebase.Model.User;
import com.example.anti2110.chatappfirebase.Notifications.Client;
import com.example.anti2110.chatappfirebase.Notifications.Data;
import com.example.anti2110.chatappfirebase.Notifications.MyResponse;
import com.example.anti2110.chatappfirebase.Notifications.Sender;
import com.example.anti2110.chatappfirebase.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username;

    private ImageButton btnSend;
    private EditText textSend;

    private FirebaseUser currentUser;
    private DatabaseReference dbRef;

    private Intent intent;

    private MessageAdapter messageAdapter;
    private List<Chat> mChats;
    private RecyclerView recyclerView;

    private ValueEventListener seenListener;

    private String userId;

    private APIService apiService;

    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        intent = getIntent();
        userId = intent.getStringExtra("userId");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        textSend = findViewById(R.id.text_send);
        btnSend = findViewById(R.id.btn_send);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                if(user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }

                readMessages(currentUser.getUid(), userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = textSend.getText().toString();

                if(!msg.equals("")) {
                    sendMessage(currentUser.getUid(), userId, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message.", Toast.LENGTH_SHORT).show();
                }
                textSend.setText("");

            }
        });

        seenMessage(userId);
    }

    private void seenMessage(final String userId) {
        dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId)) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("isseen", true);

                        snapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", sender);
        messageMap.put("receiver", receiver);
        messageMap.put("message", message);
        messageMap.put("isseen", false);

        messageRef.child("Chats").push().setValue(messageMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(currentUser.getUid())
                .child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (notify) {
                    Toast.makeText(MessageActivity.this, "notify : true", Toast.LENGTH_SHORT).show();
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUser.getUid(), R.mipmap.ic_launcher, username+": "+message, "NewMessage", userId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myId, final String userId, final String imageUrl) {
        mChats = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    Toast.makeText(MessageActivity.this, "seen" + chat.isIsseen(), Toast.LENGTH_SHORT).show();

                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        mChats.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChats, imageUrl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void currentUser(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void status(String status) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);

        dbRef.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        dbRef.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

}
