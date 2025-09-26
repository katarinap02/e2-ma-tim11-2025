package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Clothing;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
        Clothing clothing = clothingList.get(position);
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

        public ClothingViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbEquipment);
        }
    }
}
