package com.example.team11project.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Potion;

import java.util.List;

public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ClothingViewHolder> {

    private List<Clothing> clothingList;
    private final OnClothingBuyClickListener listener;

    // interfejs da Activity/Fragment može da reaguje na klik
    public interface OnClothingBuyClickListener {
        void onBuyClick(Clothing clothing);
    }

    public ClothingAdapter(List<Clothing> clothingList, OnClothingBuyClickListener listener) {
        this.clothingList = clothingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_card, parent, false);
        return new ClothingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
        Clothing clothing = clothingList.get(position);
        holder.bind(clothing, listener);
    }

    @Override
    public int getItemCount() {
        return clothingList != null ? clothingList.size() : 0;
    }

    // metoda za osvežavanje liste
    public void setClothingList(List<Clothing> newClothingList) {
        this.clothingList = newClothingList;
        notifyDataSetChanged();
    }

    static class ClothingViewHolder extends RecyclerView.ViewHolder {
        private final TextView clothingName;
        private final TextView clothingPrice;
        private final Button buyButton;

        public ClothingViewHolder(@NonNull View itemView) {
            super(itemView);
            clothingName = itemView.findViewById(R.id.txtName);
            clothingPrice = itemView.findViewById(R.id.txtPrice);
            buyButton = itemView.findViewById(R.id.btnBuy);
        }

        public void bind(final Clothing clothing, final OnClothingBuyClickListener listener) {
            clothingName.setText(clothing.getName());
            clothingPrice.setText("Cena: " + clothing.getPrice());

            buyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBuyClick(clothing);
                }
            });
        }

    }
    public void updateData(List<Clothing> newClothing) {
        this.clothingList = newClothing;
        notifyDataSetChanged();
    }
}

