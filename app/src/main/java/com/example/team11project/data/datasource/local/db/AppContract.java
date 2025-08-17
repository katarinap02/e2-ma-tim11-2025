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
        //
    }




    }
