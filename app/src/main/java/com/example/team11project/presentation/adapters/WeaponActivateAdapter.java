package com.example.team11project.presentation.adapters;

import static com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.RequestBuilder.put;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team11project.R;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponActivateAdapter extends RecyclerView.Adapter<WeaponActivateAdapter.WeaponViewHolder> {
        private List<Weapon> weapons = new ArrayList<>();
        private List<Weapon> selectedWeapons = new ArrayList<>();

        public void setWeapons(List<Weapon> weapons) {
            this.weapons = weapons != null ? weapons : new ArrayList<>();;
            notifyDataSetChanged();
        }

    private static final Map<String, Integer> weaponMap = new HashMap<String, Integer>() {{
        put("weapon_bow", R.drawable.weapon_bow);
        put("weapon_sword", R.drawable.weapon_sword);
    }};

        public List<Weapon> getSelectedWeapons() {
            return selectedWeapons;
        }

        @NonNull
        @Override
        public WeaponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_equipment, parent, false);
            return new WeaponViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WeaponViewHolder holder, int position) {

            Weapon weapon = weapons.get(position);
            int resId = weaponMap.get(weapon.getImage());
            holder.imgEquipment.setImageResource(resId);

            holder.checkBox.setText(weapon.getName() + " x" + weapon.getQuantity() +
                    " (" + weapon.getEffectType() + ")");
            holder.checkBox.setChecked(selectedWeapons.contains(weapon));

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedWeapons.contains(weapon)) selectedWeapons.add(weapon);
                } else {
                    selectedWeapons.remove(weapon);
                }
            });
        }

        @Override
        public int getItemCount() {
            return weapons.size();
        }

        static class WeaponViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageView imgEquipment;

            public WeaponViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.cbEquipment);
                imgEquipment = itemView.findViewById(R.id.imgEquipment);
            }
        }
    }


