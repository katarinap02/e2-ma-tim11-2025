package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.User;

import java.util.List;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.FriendViewHolder> {
    private final List<User> friends;
    private final List<User> selectedFriends;

    public AllianceMemberAdapter(List<User> friends, List<User> selectedFriends) {
        this.friends = friends;
        this.selectedFriends = selectedFriends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_member, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.tvFriendName.setText(friend.getUsername());


        holder.cbSelectFriend.setOnCheckedChangeListener(null);
        holder.cbSelectFriend.setChecked(selectedFriends.contains(friend));

        holder.cbSelectFriend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedFriends.contains(friend)) selectedFriends.add(friend);
            } else {
                selectedFriends.remove(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        CheckBox cbSelectFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            cbSelectFriend = itemView.findViewById(R.id.cbSelectFriend);
        }
    }
}
