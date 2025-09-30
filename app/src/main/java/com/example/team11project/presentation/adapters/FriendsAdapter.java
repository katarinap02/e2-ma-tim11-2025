package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.team11project.R;
import com.example.team11project.domain.model.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friends;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
    }

    public FriendsAdapter(){}
    public FriendsAdapter(List<User> friends) {
        this.friends = friends;
    }
    public FriendsAdapter(List<User> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    public void setFriends(List<User> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.bind(friend, listener);
    }

    @Override
    public int getItemCount() {
        return friends != null ? friends.size() : 0;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        private TextView tvFriendName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
        }

        public void bind(final User friend, final OnFriendClickListener listener) {
            tvFriendName.setText(friend.getUsername());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(friend);
                }
            });
        }
    }
}
