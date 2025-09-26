package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Potion;

import java.util.ArrayList;
import java.util.List;

public class PotionActivateAdapter extends RecyclerView.Adapter<PotionActivateAdapter.PotionViewHolder> {

    private List<Potion> potions = new ArrayList<>();
    private List<Potion> selectedPotions = new ArrayList<>();

    public void setPotions(List<Potion> potions) {
        this.potions = potions;
        notifyDataSetChanged();
    }

    public List<Potion> getSelectedPotions() {
        return selectedPotions;
    }

    @NonNull
    @Override
    public PotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new PotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PotionViewHolder holder, int position) {
        Potion potion = potions.get(position);
        holder.checkBox.setText(potion.getName() + " x" + potion.getQuantity() +
                " (" + potion.getPowerBoostPercent() + "% PP)");
        holder.checkBox.setChecked(selectedPotions.contains(potion));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedPotions.contains(potion)) selectedPotions.add(potion);
            } else {
                selectedPotions.remove(potion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return potions.size();
    }

    static class PotionViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public PotionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbEquipment);
        }
    }
}
