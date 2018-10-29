package com.example.anti2110.chatappfirebase.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.anti2110.chatappfirebase.MessageActivity;
import com.example.anti2110.chatappfirebase.Model.Chat;
import com.example.anti2110.chatappfirebase.Model.User;
import com.example.anti2110.chatappfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;

    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);

        holder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default")) {
            holder.profile_Image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_Image);
        }

        if (isChat) {
            lastMessage(user.getId(), holder.lastMsg);
        } else {
            holder.lastMsg.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public CircleImageView profile_Image;
        private CircleImageView imgOn;
        private CircleImageView imgOff;
        private TextView lastMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_Image = itemView.findViewById(R.id.profile_image);
            imgOn = itemView.findViewById(R.id.img_on);
            imgOff = itemView.findViewById(R.id.img_off);
            lastMsg = itemView.findViewById(R.id.last_msg);

        }
    }

    // Check for last message
    private void lastMessage(final String userId, final TextView lastMsg) {
        theLastMessage = "default";
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())) {
                        theLastMessage = chat.getMessage();
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        lastMsg.setText("No message");
                        break;
                    default:
                        lastMsg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
