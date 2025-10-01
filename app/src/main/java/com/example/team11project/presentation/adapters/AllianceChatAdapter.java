package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.AllianceMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceChatAdapter extends RecyclerView.Adapter<AllianceChatAdapter.MessageViewHolder> {

    private final List<AllianceMessage> messages;
    private final String currentUserId;

    public AllianceChatAdapter(List<AllianceMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 0 ? R.layout.item_message_right : R.layout.item_message_left;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        AllianceMessage message = messages.get(position);
        holder.username.setText(message.getSenderUsername());
        holder.content.setText(message.getMessage());
        holder.timestamp.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(message.getTimestamp())));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? 0 : 1;
    }

    public void setMessages(List<AllianceMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView username, content, timestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUsername);
            content = itemView.findViewById(R.id.tvMessage);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
