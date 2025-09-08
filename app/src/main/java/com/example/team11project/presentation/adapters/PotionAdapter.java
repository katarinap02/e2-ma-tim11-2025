package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Potion;

import java.util.List;

public class PotionAdapter extends RecyclerView.Adapter<PotionAdapter.PotionViewHolder> {

    public interface OnBuyClickListener {
        void onBuy(Potion potion);
    }

    private List<Potion> potions;
    private final OnBuyClickListener listener;

    public PotionAdapter(List<Potion> potions, OnBuyClickListener listener) {
        this.potions = potions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_card, parent, false);
        return new PotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PotionViewHolder holder, int position) {
        Potion potion = potions.get(position);
        holder.bind(potion, listener);
    }

    @Override
    public int getItemCount() {
        return potions.size();
    }

    public void updateData(List<Potion> newPotions) {
        this.potions = newPotions;
        notifyDataSetChanged();
    }

    static class PotionViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        Button buyButton;

        public PotionViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtName);
            price = itemView.findViewById(R.id.txtPrice);
            buyButton = itemView.findViewById(R.id.btnBuy);
        }

        void bind(Potion potion, OnBuyClickListener listener) {
            name.setText(potion.getName());
            price.setText("Cena: " + potion.getPrice());
            buyButton.setOnClickListener(v -> listener.onBuy(potion));
        }
    }
}
