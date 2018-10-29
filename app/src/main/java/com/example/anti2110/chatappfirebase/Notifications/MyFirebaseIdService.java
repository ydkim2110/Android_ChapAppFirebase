package com.example.anti2110.chatappfirebase.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (currentUser != null) {
            updateToken(refreshToken);
        }

    }

    private void updateToken(String refreshToken) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshToken);
        dbRef.child(currentUser.getUid()).setValue(token);
    }
}
