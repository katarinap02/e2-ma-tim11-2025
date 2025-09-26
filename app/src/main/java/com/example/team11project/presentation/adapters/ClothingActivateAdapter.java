package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Clothing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothingActivateAdapter extends RecyclerView.Adapter<ClothingActivateAdapter.ClothingViewHolder> {

    private List<Clothing> clothingList = new ArrayList<>();
    private List<Clothing> selectedClothing = new ArrayList<>();

    public void setClothingList(List<Clothing> clothingList) {
        this.clothingList = clothingList;
        notifyDataSetChanged();
    }

    public List<Clothing> getSelectedClothing() {
        return selectedClothing;
    }

    @NonNull
    @Override
    public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new ClothingViewHolder(view);
    }

    private static final Map<String, Integer> quipmentMap = new HashMap<String, Integer>() {{
        put("clothing_boots", R.drawable.clothing_boots);
        put("clothing_gloves", R.drawable.clothing_gloves);
        put("clothing_shield", R.drawable.clothing_shield);
        put("potion_10", R.drawable.potion_10);
    }};
    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
        Clothing clothing = clothingList.get(position);

        int resId = quipmentMap.get(clothing.getImage());
        holder.imgEquipment.setImageResource(resId);

        holder.checkBox.setText(clothing.getName() + " x" + clothing.getQuantity() +
                " (" + clothing.getEffectPercent() + "% PP)");
        holder.checkBox.setChecked(selectedClothing.contains(clothing));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedClothing.contains(clothing)) selectedClothing.add(clothing);
            } else {
                selectedClothing.remove(clothing);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clothingList.size();
    }

    static class ClothingViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView imgEquipment;

        public ClothingViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbEquipment);
            imgEquipment = itemView.findViewById(R.id.imgEquipment);

        }
    }
}
