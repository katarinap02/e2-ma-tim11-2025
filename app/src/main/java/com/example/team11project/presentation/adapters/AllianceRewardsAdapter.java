package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.AllianceMissionReward;

import java.util.List;

public class AllianceRewardsAdapter extends RecyclerView.Adapter<AllianceRewardsAdapter.RewardViewHolder> {

    private List<AllianceMissionReward> rewards;

    public AllianceRewardsAdapter(List<AllianceMissionReward> rewards) {
        this.rewards = rewards;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        AllianceMissionReward reward = rewards.get(position);
        holder.bind(reward, position + 1);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public void updateRewards(List<AllianceMissionReward> newRewards) {
        this.rewards = newRewards;
        notifyDataSetChanged();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRewardNumber;
        private TextView tvCoins;
        private TextView tvPotionName;
        private TextView tvClothingName;
        private ImageView ivPotion;
        private ImageView ivClothing;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardNumber = itemView.findViewById(R.id.tvRewardNumber);
            tvCoins = itemView.findViewById(R.id.tvCoins);
            tvPotionName = itemView.findViewById(R.id.tvPotionName);
            tvClothingName = itemView.findViewById(R.id.tvClothingName);
            ivPotion = itemView.findViewById(R.id.ivPotion);
            ivClothing = itemView.findViewById(R.id.ivClothing);
        }

        public void bind(AllianceMissionReward reward, int number) {
            tvRewardNumber.setText("Nagrada #" + number);
            tvCoins.setText("Novčići: " + reward.getCoins());

            // Potion
            if (reward.getPotion() != null) {
                tvPotionName.setText(reward.getPotion().getName());
                loadImage(ivPotion, reward.getPotion().getImage());
            } else {
                tvPotionName.setText("Nema napitka");
                ivPotion.setVisibility(View.GONE);
            }

            // Clothing
            if (reward.getClothing() != null) {
                tvClothingName.setText(reward.getClothing().getName());
                loadImage(ivClothing, reward.getClothing().getImage());
            } else {
                tvClothingName.setText("Nema odeće");
                ivClothing.setVisibility(View.GONE);
            }
        }

        private void loadImage(ImageView imageView, String imageName) {
            if (imageName != null && !imageName.isEmpty()) {
                int resourceId = imageView.getContext().getResources()
                        .getIdentifier(imageName, "drawable", imageView.getContext().getPackageName());
                if (resourceId != 0) {
                    imageView.setImageResource(resourceId);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
    }
}