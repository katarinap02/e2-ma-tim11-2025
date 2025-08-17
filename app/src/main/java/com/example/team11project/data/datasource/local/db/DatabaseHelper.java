package com.example.team11project.data.datasource.local.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
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


    // SQL komanda za kreiranje tabele za zadatke
    private static final String SQL_CREATE_TASKS_TABLE =
            "CREATE TABLE " + AppContract.TaskEntry.TABLE_NAME + " (" +
                    AppContract.TaskEntry._ID + " TEXT PRIMARY KEY," +
                    AppContract.TaskEntry.COLUMN_NAME_USER_ID + " TEXT NOT NULL," + // Dodato
                    AppContract.TaskEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_IS_RECURRING + " INTEGER NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_INTERVAL + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_UNIT + " TEXT," +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_START_DATE + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE + "INTEGER" +
                    AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_END_DATE + " INTEGER," +
                    AppContract.TaskEntry.COLUMN_NAME_EXECUTION_TIME + " INTEGER NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_DIFFICULTY + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_IMPORTANCE + " TEXT NOT NULL," +
                    AppContract.TaskEntry.COLUMN_NAME_STATUS + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID + ") REFERENCES " +
                    AppContract.CategoryEntry.TABLE_NAME + "(" + AppContract.CategoryEntry._ID + "))";

    // SQL komande za brisanje tabela
    private static final String SQL_DELETE_CATEGORIES_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.CategoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_TASKS_TABLE =
            "DROP TABLE IF EXISTS " + AppContract.TaskEntry.TABLE_NAME;

    //Poziva se kada se baza kreira po prvi put.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_TASKS_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
    }

    // Ova strategija odbacuje sve podatke i kreira tabele iz početka.
    // Za produkcijsku aplikaciju, ovde biste koristili ALTER TABLE komande
    // da sačuvate postojeće podatke korisnika.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TASKS_TABLE);
        db.execSQL(SQL_DELETE_CATEGORIES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS users");
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
                    AppContract.UserEntry.COLUMN_VERIFIED + " INTEGER NOT NULL DEFAULT 0" + // 0 = false, 1 = true
                    ")";

}
