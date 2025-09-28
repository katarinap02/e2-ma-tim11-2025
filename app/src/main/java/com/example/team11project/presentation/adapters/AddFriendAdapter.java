package com.example.team11project.presentation.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.User;
import com.example.team11project.presentation.activities.ProfileActivity;

import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder> {

    private List<User> users;
    private OnAddFriendClickListener listener;

    // Interfejs koji Activity implementira
    public interface OnAddFriendClickListener {
        void onAddFriendClick(User user);
    }

    public AddFriendAdapter(List<User> users, OnAddFriendClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void setUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_non_friend, parent, false);
        return new AddFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class AddFriendViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final Button btnAddFriend;

        public AddFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvNonFriendName); // poklapa se sa XML
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }

        public void bind(final User user, final OnAddFriendClickListener listener) {
            tvUsername.setText(user.getUsername());

            btnAddFriend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddFriendClick(user);
                }
            });

            tvUsername.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ProfileActivity.class);
                intent.putExtra("userId", user.getId());
                itemView.getContext().startActivity(intent);
            });

        }
    }
}
