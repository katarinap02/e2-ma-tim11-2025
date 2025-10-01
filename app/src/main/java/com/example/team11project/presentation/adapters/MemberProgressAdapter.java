package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.team11project.R;
import com.example.team11project.domain.model.MemberProgress;
import android.graphics.Color;
import android.graphics.Typeface;

public class MemberProgressAdapter extends RecyclerView.Adapter<MemberProgressAdapter.ViewHolder> {

    private List<MemberProgress> memberProgressList = new ArrayList<>();
    private String currentUserId;
    private int bossMaxHp = 100; // Default vrednost
    private Map<String, String> userMap = new HashMap<>();

    public MemberProgressAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
        notifyDataSetChanged();
    }

    public void setMemberProgressList(List<MemberProgress> list) {
        this.memberProgressList = list;
        notifyDataSetChanged();
    }

    public void setBossMaxHp(int maxHp) {
        this.bossMaxHp = maxHp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberProgress member = memberProgressList.get(position);

        boolean isCurrentUser = member.getUserId().equals(currentUserId);
        String username = userMap.getOrDefault(member.getUserId(), "Član #" + (position + 1));

        // Ime člana ili "Vi" za trenutnog korisnika
        if (isCurrentUser) {
            holder.tvMemberName.setText("Vi (" + username + ")");
            holder.tvMemberName.setTextColor(Color.parseColor("#FFD700")); // Zlatna boja
            holder.tvMemberName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvMemberName.setText(username);
            holder.tvMemberName.setTextColor(Color.WHITE);
            holder.tvMemberName.setTypeface(null, Typeface.NORMAL);
        }

        // Prikaži total damage
        int totalDamage = member.getTotalDamageDealt();
        holder.tvMemberDamage.setText(totalDamage + " HP");

        // Progress bar - prikaži procenat od boss max HP
        holder.pbMemberProgress.setMax(bossMaxHp);
        holder.pbMemberProgress.setProgress(totalDamage);

        // Detalji samo za trenutnog korisnika
        if (isCurrentUser) {
            String details = String.format(
                    "Prodavnica: %d/5 | Napadi: %d/10 | Zadaci: %d/10 + %d/6",
                    member.getStorePurchases(),
                    member.getRegularBossHits(),
                    member.getEasyNormalTasks(),
                    member.getOtherTasks()
            );
            holder.tvMemberDetails.setText(details);
            holder.tvMemberDetails.setVisibility(View.VISIBLE);

            // Prikaži progress bar u drugoj boji za trenutnog korisnika
            holder.pbMemberProgress.setProgressTintList(
                    holder.itemView.getContext().getResources().getColorStateList(R.color.user_pp_color)
            );
        } else {
            String details = String.format(
                    "Prodavnica: %d/5 | Napadi: %d/10 | Zadaci: %d/10 + %d/6",
                    member.getStorePurchases(),
                    member.getRegularBossHits(),
                    member.getEasyNormalTasks(),
                    member.getOtherTasks()
            );
            holder.tvMemberDetails.setText(details);
            holder.tvMemberDetails.setVisibility(View.VISIBLE);

            // Obična boja za ostale članove
            holder.pbMemberProgress.setProgressTintList(
                    holder.itemView.getContext().getResources().getColorStateList(R.color.boss_hp_color)
            );
        }

        // Bonus poruka ako nema nerešenih zadataka
        holder.tvBonus.setVisibility(member.isNoUnresolvedTasks() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return memberProgressList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberDamage, tvMemberDetails, tvBonus;
        ProgressBar pbMemberProgress;

        ViewHolder(View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberDamage = itemView.findViewById(R.id.tvMemberDamage);
            tvMemberDetails = itemView.findViewById(R.id.tvMemberDetails);
            tvBonus = itemView.findViewById(R.id.tvBonus);
            pbMemberProgress = itemView.findViewById(R.id.pbMemberProgress);
        }
    }
}