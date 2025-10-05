package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, Integer> quipmentMap = new HashMap<String, Integer>() {{
        put("clothing_boots", R.drawable.clothing_boots);
        put("clothing_gloves", R.drawable.clothing_gloves);
        put("clothing_shield", R.drawable.clothing_shield);
        put("potion_10", R.drawable.potion_10);
        put("potion_20", R.drawable.potion_20);
        put("potion_40", R.drawable.potion_40);
        put("potion_5", R.drawable.potion_5);
        put("weapon_bow", R.drawable.weapon_bow);
        put("weapon_sword", R.drawable.weapon_sword);


    }};

    @Override
    public void onBindViewHolder(@NonNull PotionViewHolder holder, int position) {
        Potion potion = potions.get(position);

        int resId = quipmentMap.get(potion.getImage());
        holder.imgEquipment.setImageResource(resId);

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
        ImageView imgEquipment;


        public PotionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbEquipment);
            imgEquipment = itemView.findViewById(R.id.imgEquipment);

        }
    }
}
