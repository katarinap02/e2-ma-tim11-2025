package com.example.team11project.presentation.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final OnUserClickListener listener;

    private static final Map<String, Integer> avatarMap = new HashMap<String, Integer>() {{
        put("avatar1", R.drawable.avatar1);
        put("avatar2", R.drawable.avatar2);
        put("avatar3", R.drawable.avatar3);
        put("avatar4", R.drawable.avatar4);
        put("avatar5", R.drawable.avatar5);

    }};
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UsersAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.txtUsername.setText(user.getUsername());

        int resId = avatarMap.getOrDefault(user.getAvatar(), R.drawable.avatar1);
        holder.imgAvatar.setImageResource(resId);
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUsername, txtTitle, txtLevel;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtLevel = itemView.findViewById(R.id.txtLevel);
        }
    }
}
