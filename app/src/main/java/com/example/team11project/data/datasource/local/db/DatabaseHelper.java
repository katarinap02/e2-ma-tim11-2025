package com.example.team11project.data.datasource.local.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "MobileApp.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + AppContract.CategoryEntry.TABLE_NAME + " (" +
                    AppContract.CategoryEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," + // Dodato
                    AppContract.CategoryEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                    AppContract.CategoryEntry.COLUMN_NAME_COLOR + " TEXT NOT NULL," +
                    // Ime kategorije mora biti jedinstveno PO KORISNIKU
                    "UNIQUE (" + AppContract.CategoryEntry.COLUMN_NAME_USER_ID + ", " + AppContract.CategoryEntry.COLUMN_NAME_NAME + ")," +
                    // Boja kategorije mora biti jedinstvena PO KORISNIKU
                    "UNIQUE (" + AppContract.CategoryEntry.COLUMN_NAME_USER_ID + ", " + AppContract.CategoryEntry.COLUMN_NAME_COLOR + "))";


    private static final String SQL_CREATE_TASKS_TABLE =
            "CREATE TABLE " + AppContract.TaskEntry.TABLE_NAME + " (" +
                    AppContract.TaskEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.TaskEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID + " TEXT," +

                    AppContract.TaskEntry.COLUMN_NAME_IS_RECURRING + " INTEGER NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_INTERVAL + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_UNIT + " TEXT," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_START_DATE + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE + " INTEGER," + // Dodat razmak i zarez

                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_END_DATE + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_EXECUTION_TIME + " INTEGER NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_DIFFICULTY + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_IMPORTANCE + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_STATUS + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID + ") REFERENCES " +
                    AppContract.CategoryEntry.TABLE_NAME + "(" + AppContract.CategoryEntry._ID + "))";
    private static final String SQL_CREATE_TASK_INSTANCES_TABLE =
            "CREATE TABLE " + AppContract.TaskInstanceEntry.TABLE_NAME + " (" +
                    AppContract.TaskInstanceEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID + " TEXT NOT NULL," +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_DATE + " INTEGER NOT NULL," +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_NEW_STATUS + " TEXT NOT NULL," +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_COMPLETION_DATE + " INTEGER," +
                    "FOREIGN KEY(" + AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID + ") REFERENCES " +
                    AppContract.TaskEntry.TABLE_NAME + "(" + AppContract.TaskEntry._ID + "))";
    private static final String SQL_CREATE_BOSS_TABLE =
            "CREATE TABLE " + AppContract.BossEntry.TABLE_NAME + " (" +
                    AppContract.BossEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.BossBattleEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," +
                    AppContract.BossEntry.COLUMN_NAME_LEVEL + " INTEGER NOT NULL," +
                    AppContract.BossEntry.COLUMN_NAME_MAX_HP + " INTEGER NOT NULL," +
                    AppContract.BossEntry.COLUMN_NAME_CURRENT_HP + " INTEGER NOT NULL," +
                    AppContract.BossEntry.COLUMN_NAME_IS_DEFEATED + " INTEGER NOT NULL," + // 0 = false, 1 = true
                    AppContract.BossEntry.COLUMN_NAME_COINS_REWARD + " INTEGER NOT NULL" +
                    ")";
    private static final String SQL_CREATE_BOSS_BATTLE_TABLE =
            "CREATE TABLE " + AppContract.BossBattleEntry.TABLE_NAME + " (" +
                    AppContract.BossBattleEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.BossBattleEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_BOSS_ID + " TEXT NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_LEVEL+ " INTEGER NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_ATTACKS_USED + " INTEGER NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_DAMAGE_DEALT + " INTEGER NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_HIT_CHANCE + " REAL NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_USER_PP + " INTEGER NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_BOSS_DEFEATED + " INTEGER NOT NULL," +
                    AppContract.BossBattleEntry.COLUMN_NAME_ACTIVE_EQUIPMENT + " TEXT," + // JSON string/list
                    "FOREIGN KEY(" + AppContract.BossBattleEntry.COLUMN_NAME_BOSS_ID + ") REFERENCES " +
                    AppContract.BossEntry.TABLE_NAME + "(" + AppContract.BossEntry._ID + ")," +
                    "FOREIGN KEY(" + AppContract.BossBattleEntry.COLUMN_NAME_USER_ID + ") REFERENCES users(id)" +
                    ")";
    private static final String SQL_CREATE_BOSS_REWARD_TABLE =
            "CREATE TABLE " + AppContract.BossRewardEntry.TABLE_NAME + " (" +
                    AppContract.BossRewardEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.BossRewardEntry.COLUMN_NAME_BOSS_ID + " TEXT NOT NULL," +
                    AppContract.BossRewardEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," +
                    AppContract.BossRewardEntry.COLUMN_NAME_LEVEL + " INTEGER NOT NULL," +
                    AppContract.BossRewardEntry.COLUMN_NAME_COINS_EARNED + " INTEGER NOT NULL," +
                    AppContract.BossRewardEntry.COLUMN_NAME_EQUIPMENT_ID + " TEXT," +
                    "FOREIGN KEY(" + AppContract.BossRewardEntry.COLUMN_NAME_BOSS_ID + ") REFERENCES " +
                    AppContract.BossEntry.TABLE_NAME + "(" + AppContract.BossEntry._ID + ")," +
                    "FOREIGN KEY(" + AppContract.BossRewardEntry.COLUMN_NAME_USER_ID + ") REFERENCES users(id)" +
                    ")";

    // SQL komande za brisanje tabela
    private static final String SQL_DELETE_CATEGORIES_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.CategoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_TASKS_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.TaskEntry.TABLE_NAME;
    private static final String SQL_DELETE_BOSS_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.BossEntry.TABLE_NAME;
    private static final String SQL_DELETE_BOSS_BATTLE_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.BossBattleEntry.TABLE_NAME;
    private static final String SQL_DELETE_BOSS_REWARD_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.BossRewardEntry.TABLE_NAME;

    //Poziva se kada se baza kreira po prvi put.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_TASK_INSTANCES_TABLE);
        db.execSQL(SQL_CREATE_BOSS_TABLE);
        db.execSQL(SQL_CREATE_BOSS_BATTLE_TABLE);
        db.execSQL(SQL_CREATE_BOSS_REWARD_TABLE);
    }

    // Ova strategija odbacuje sve podatke i kreira tabele iz početka.
    // Za produkcijsku aplikaciju, ovde biste koristili ALTER TABLE komande
    // da sačuvate postojeće podatke korisnika.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TASKS_TABLE);
        db.execSQL(SQL_DELETE_CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + AppContract.TaskInstanceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL(SQL_DELETE_BOSS_REWARD_TABLE);
        db.execSQL(SQL_DELETE_BOSS_BATTLE_TABLE);
        db.execSQL(SQL_DELETE_BOSS_TABLE);
        onCreate(db);
    }

    //Poziva se kada je potrebno vratiti bazu na stariju verziju (downgrade).
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + AppContract.UserEntry.TABLE_NAME + " (" +
                    AppContract.UserEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL," +
                    AppContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL," +
                    AppContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL," +
                    AppContract.UserEntry.COLUMN_AVATAR + " TEXT," +
                    AppContract.UserEntry.COLUMN_VERIFIED + " INTEGER NOT NULL DEFAULT 0, " + // 0 = false, 1 = true
                    AppContract.UserEntry.COLUMN_WEAPON + " TEXT, " +
                    AppContract.UserEntry.COLUMN_CLOTHING + " TEXT, " +
                    AppContract.UserEntry.COLUMN_POTION + " TEXT, " +
                    AppContract.UserEntry.COLUMN_COINS + " INTEGER NOT NULL DEFAULT 0" +
                    ")";

    private static final String SQL_CREATE_LEVELINFO_TABLE =
            "CREATE TABLE " + AppContract.LevelInfoEntry.TABLE_NAME + " (" +
                    AppContract.LevelInfoEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.LevelInfoEntry.COLUMN_LEVEL + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_XP + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_XP_FOR_NEXT_LEVEL + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_XP_TASK_IMPORTANCE + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_XP_TASK_DIFFICULTY + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_PP + " INTEGER NOT NULL," +
                    AppContract.LevelInfoEntry.COLUMN_TITLE + " TEXT NOT NULL" +
                    "FOREIGN KEY (" + AppContract.LevelInfoEntry.COLUMN_USER_ID + ") REFERENCES " +
                    AppContract.UserEntry.TABLE_NAME + "(" + AppContract.UserEntry._ID + ")" +
                    ")";




    private static final String SQL_CREATE_EQUIPMENT_TABLE =
            "CREATE TABLE " + AppContract.EquipmentEntry.TABLE_NAME + " (" +
                    AppContract.EquipmentEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.EquipmentEntry.COLUMN_NAME + " TEXT NOT NULL," +
                    AppContract.EquipmentEntry.COLUMN_TYPE + " TEXT NOT NULL," +
                    AppContract.EquipmentEntry.COLUMN_PRICE + " REAL NOT NULL," +
                    AppContract.EquipmentEntry.COLUMN_IS_ACTIVE + " INTEGER," + // 0 = false, 1 = true

                    // Potion
                    AppContract.EquipmentEntry.COLUMN_POWER_BOOST_PERCENT + " INTEGER," +
                    AppContract.EquipmentEntry.COLUMN_IS_PERMANENT + " INTEGER," +

                    // Clothing
                    AppContract.EquipmentEntry.COLUMN_EFFECT_PERCENT + " INTEGER," +
                    AppContract.EquipmentEntry.COLUMN_REMAINING_BATTLES + " INTEGER," +
                    AppContract.EquipmentEntry.COLUMN_CLOTHING_EFFECT_TYPE + " TEXT, " +

                    // Weapon
                    AppContract.EquipmentEntry.COLUMN_PERMANENT_BOOST_PERCENT + " INTEGER," +
                    AppContract.EquipmentEntry.COLUMN_UPGRADE_CHANCE + " REAL," +
                    AppContract.EquipmentEntry.COLUMN_WEAPON_EFFECT_TYPE + " TEXT" +
                    ")";



}
