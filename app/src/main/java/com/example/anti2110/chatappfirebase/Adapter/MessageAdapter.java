package com.example.anti2110.chatappfirebase.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.anti2110.chatappfirebase.Model.Chat;
import com.example.anti2110.chatappfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT =0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChats;
    private String imageUrl;

    private FirebaseUser currentUser;

    public MessageAdapter(Context mContext, List<Chat> mChats, String imageUrl) {
        this.mContext = mContext;
        this.mChats = mChats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mChats.get(position);

        holder.showMessage.setText(chat.getMessage());
        if (imageUrl.equals("default")) {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageUrl).into(holder.profileImage);
        }
        
        if (position == mChats.size() -1) {
            if(chat.isIsseen()) {
                holder.txtSeen.setText("Seen");
            } else {
                holder.txtSeen.setText("Delivered");
            }
        } else {
            holder.txtSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView showMessage;
        public CircleImageView profileImage;

        public TextView txtSeen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            profileImage = itemView.findViewById(R.id.profile_image);
            txtSeen = itemView.findViewById(R.id.txt_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(currentUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

}
