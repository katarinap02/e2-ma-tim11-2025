package com.example.team11project.data.datasource.local.db;

import android.provider.BaseColumns;

public final class AppContract {
    private AppContract() {}

    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_COLOR = "color";
    }

    public static class TaskEntry implements BaseColumns {
        public static  final  String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id"; // Foreign key
        public static final String COLUMN_NAME_IS_RECURRING = "is_recurring";
        public static final String COLUMN_NAME_RECURRENCE_INTERVAL = "recurrence_interval";
        public static final String COLUMN_NAME_RECURRENCE_UNIT = "recurrence_unit";
        public static final String COLUMN_NAME_RECURRENCE_START_DATE = "recurrence_start_date";
        public static final String COLUMN_NAME_RECURRENCE_END_DATE = "recurrence_end_date";
        public static final String COLUMN_NAME_EXECUTION_TIME = "execution_time";
        public static final String COLUMN_NAME_COMPLETION_DATE = "completion_date";
        public static final String COLUMN_NAME_DIFFICULTY = "difficulty";
        public static final String COLUMN_NAME_IMPORTANCE = "importance";
        public static final String COLUMN_NAME_STATUS = "status";

    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String _ID = "id";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_VERIFIED = "isVerified";
        public static final String COLUMN_CLOTHING = "clothing";
        public static final String COLUMN_WEAPON = "weapon";
        public static final String COLUMN_POTION = "potion";
        public static final String COLUMN_COINS = "coins";
        public static final String COLUMN_FRIENDS = "friends";
    }


    public static class LevelInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "level_info";
        public static final String _ID = "id";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_XP = "xp";
        public static final String COLUMN_XP_FOR_NEXT_LEVEL = "xp_for_next_level";
        public static final String COLUMN_XP_TASK_IMPORTANCE = "xp_task_importance";
        public static final String COLUMN_XP_TASK_DIFFICULTY = "xp_task_difficulty";
        public static final String COLUMN_PP = "pp";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_USER_ID = "user_id";
    }

        public static class TaskInstanceEntry implements BaseColumns {
            public static final String TABLE_NAME = "task_instances";
            public static final String COLUMN_NAME_ORIGINAL_TASK_ID = "original_task_id";
            public static final String COLUMN_NAME_USER_ID = "user_id";
            public static final String COLUMN_NAME_ORIGINAL_DATE = "original_date";
            public static final String COLUMN_NAME_NEW_STATUS = "new_status";
            public static final String COLUMN_NAME_COMPLETION_DATE = "completion_date";
        }

    public static class BossEntry implements BaseColumns {
        public static final String TABLE_NAME = "bosses";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_LEVEL = "level";
        public static final String COLUMN_NAME_MAX_HP = "max_hp";
        public static final String COLUMN_NAME_CURRENT_HP = "current_hp";
        public static final String COLUMN_NAME_IS_DEFEATED = "is_defeated";
        public static final String COLUMN_NAME_COINS_REWARD = "coins_reward";
    }

    public static class BossBattleEntry implements BaseColumns {
        public static final String TABLE_NAME = "boss_battles";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_BOSS_ID = "boss_id";
        public static final String COLUMN_NAME_LEVEL = "level";
        public static final String COLUMN_NAME_ATTACKS_USED = "attacks_used";
        public static final String COLUMN_NAME_DAMAGE_DEALT = "damage_dealt";
        public static final String COLUMN_NAME_HIT_CHANCE = "hit_chance";
        public static final String COLUMN_NAME_USER_PP = "user_pp";
        public static final String COLUMN_NAME_BOSS_DEFEATED = "boss_defeated";
        public static final String COLUMN_NAME_ACTIVE_EQUIPMENT = "active_equipment";
    }

    public static class BossRewardEntry implements BaseColumns {
        public static final String TABLE_NAME = "boss_rewards";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BOSS_ID = "boss_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_LEVEL = "level";
        public static final String COLUMN_NAME_COINS_EARNED = "coins_earned";
        public static final String COLUMN_NAME_EQUIPMENT_ID = "equipment_id";
    }


    public static class EquipmentEntry implements BaseColumns {
        public static final String TABLE_NAME = "equipment";

        public static final String _ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type"; // POTION, CLOTHING, WEAPON
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_IMAGE = "image";

        // Potion
        public static final String COLUMN_POWER_BOOST_PERCENT = "power_boost_percent";
        public static final String COLUMN_IS_PERMANENT = "is_permanent";

        // Clothing
        public static final String COLUMN_EFFECT_PERCENT = "effect_percent";
        public static final String COLUMN_CLOTHING_EFFECT_TYPE = "clothing_effect_type";
        public static final String COLUMN_REMAINING_BATTLES = "remaining_battles";

        // Weapon
        public static final String COLUMN_PERMANENT_BOOST_PERCENT = "permanent_boost_percent";
        public static final String COLUMN_UPGRADE_CHANCE = "upgrade_chance";
        public static final String COLUMN_WEAPON_EFFECT_TYPE = "weapon_effect_type";
    }


}
