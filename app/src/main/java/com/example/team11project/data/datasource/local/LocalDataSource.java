package com.example.team11project.data.datasource.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.team11project.data.datasource.local.db.AppContract;
import com.example.team11project.data.datasource.local.db.DatabaseHelper;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskDifficulty;
import com.example.team11project.domain.model.TaskImportance;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Klasa koja upravlja svim operacijama sa lokalnom SQLite bazom podataka.
 * Služi kao jedina tačka pristupa lokalnim podacima za ostatak aplikacije.
 * Sve operacije su vezane za određenog korisnika (userId).
 */
public class LocalDataSource {

    private DatabaseHelper dbHelper;

    public LocalDataSource(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    //region Category CRUD Methods

    /**
     * CATEGORY DEO BAZE
     */
    public long addCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = categoryToContentValues(category);

        long newRowId = db.insertWithOnConflict(
                AppContract.CategoryEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return newRowId;


    }

    /**
     * Vraća listu svih kategorija ISKLJUČIVO za određenog korisnika.
     */
    public List<Category> getAllCategories(String userId) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = db.query(
                AppContract.CategoryEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            categories.add(cursorToCategory(cursor));
        }
        cursor.close();
        db.close();
        return categories;
    }

    /**
     * Ažurira postojeću kategoriju, samo ako pripada datom korisniku.
     */
    public int updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = categoryToContentValues(category);

        String selection = AppContract.CategoryEntry._ID + " = ? AND " + AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { category.getId(), category.getUserId() };

        int count = db.update(
                AppContract.CategoryEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    /**
     * Briše kategoriju iz baze, samo ako pripada datom korisniku.
     */
    public int deleteCategory(String categoryId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AppContract.CategoryEntry._ID + " = ? AND " + AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { categoryId, userId };

        int deletedRows = db.delete(AppContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        return deletedRows;
    }
    //provera da li se boja vec koristi
    public boolean isColorUsed(String color, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + AppContract.CategoryEntry.TABLE_NAME +
                " WHERE " + AppContract.CategoryEntry.COLUMN_NAME_COLOR + " = ? AND " +
                AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{color, userId});

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0); // COUNT(*) je uvek na prvom indeksu (0)
            }
            cursor.close();
        }
        db.close();

        return count > 0;
    }

    public boolean isColorUsedUpdate(String color, String userId, String categoryIdToIgnore) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + AppContract.CategoryEntry.TABLE_NAME +
                " WHERE " + AppContract.CategoryEntry.COLUMN_NAME_COLOR + " = ? AND " +
                AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                AppContract.CategoryEntry._ID + " != ?";

        String[] selectionArgs = { color, userId, categoryIdToIgnore };
        cursor = db.rawQuery(query, selectionArgs);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0); // COUNT(*) je uvek na prvom indeksu (0)
            }
            cursor.close();
        }
        db.close();

        return count > 0;
    }

    public boolean isCategoryInUse(String categoryId, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            // Definišemo upit koji broji koliko zadataka (Task) koristi dati categoryId
            String sql = "SELECT COUNT(*) FROM " + AppContract.TaskEntry.TABLE_NAME +
                    " WHERE " + AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";

            // Prosleđujemo argumente sigurno kao niz stringova da bismo sprečili SQL Injection.
            String[] selectionArgs = { categoryId, userId };

            cursor = db.rawQuery(sql, selectionArgs);

            // Ako kursor nije null i možemo da se pomerimo na prvi red (koji uvek postoji u COUNT upitu)
            if (cursor != null && cursor.moveToFirst()) {
                // Rezultat COUNT(*) upita se uvek nalazi u prvoj koloni (indeks 0).
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        // Ako je broj veći od 0, to znači da je kategorija u upotrebi.
        return count > 0;
    }


    public int deleteAllCategoriesForUser(String userId) {
        // Dobijamo instancu baze podataka koja je otvorena za pisanje.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;

        try {
            String selection = AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = { userId };
            // Izvršavamo operaciju brisanja. Metoda vraća broj redova koji su obrisani.
            deletedRows = db.delete(
                    AppContract.CategoryEntry.TABLE_NAME, // Naziv tabele
                    selection,                             // WHERE klauzula
                    selectionArgs                          // Vrednosti za WHERE klauzulu
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return deletedRows;
    }

    public Category getCategoryById(String categoryId, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Category category = null;
        try {
            String selection = AppContract.CategoryEntry._ID + " = ? AND " + AppContract.CategoryEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = { categoryId, userId };
            cursor = db.query(AppContract.CategoryEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                category = cursorToCategory(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return category;
    }



    /**
     * TASK DEO
     */
    public long addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = taskToContentValues(task);

        long result = db.insertWithOnConflict(
                AppContract.TaskEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        db.close();
        return result;
    }

    /**
     * Vraća listu svih zadataka ISKLJUČIVO za određenog korisnika.
     */
    public List<Task> getAllTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { userId };

        Cursor cursor = db.query(
                AppContract.TaskEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            tasks.add(cursorToTask(cursor));
        }
        cursor.close();
        db.close();
        return tasks;
    }

    /**
     * Ažurira postojeći zadatak, samo ako pripada datom korisniku.
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = taskToContentValues(task);

        // ISPRAVKA: WHERE klauzula mora da proverava i ID i userId radi sigurnosti
        String selection = AppContract.TaskEntry._ID + " = ? AND " + AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { task.getId(), task.getUserId() };

        int count = db.update(
                AppContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    /**
     * Briše zadatak iz baze, samo ako pripada datom korisniku.
     */
    public int deleteTask(String taskId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // ISPRAVKA: WHERE klauzula mora da proverava i ID i userId radi sigurnosti
        String selection = AppContract.TaskEntry._ID + " = ? AND " + AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = { taskId, userId };

        int deletedRows = db.delete(AppContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        return deletedRows;
    }

    public List<Task> getTasksByGroupId(String groupId, String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selection = AppContract.TaskEntry.COLUMN_NAME_GROUP_ID + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = { groupId, userId };

            cursor = db.query(
                    AppContract.TaskEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    AppContract.TaskEntry.COLUMN_NAME_EXECUTION_TIME + " ASC"
            );

            while (cursor.moveToNext()) {
                tasks.add(cursorToTask(cursor));
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return tasks;
    }

    public int deleteAllTasksForUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;

        try {
            String selection = AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = { userId };
            deletedRows = db.delete(
                    AppContract.TaskEntry.TABLE_NAME, // Naziv tabele
                    selection,                             // WHERE klauzula
                    selectionArgs                          // Vrednosti za WHERE klauzulu
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return deletedRows;
    }

    // U data/datasource/local/LocalDataSource.java

    public Task getTaskById(String taskId, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Task task = null;
        try {
            String selection = AppContract.TaskEntry._ID + " = ? AND " + AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = { taskId, userId };

            cursor = db.query(
                    AppContract.TaskEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                task = cursorToTask(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return task;
    }

    // DEO SA TASKINSTANCES

    public long addTaskInstance(TaskInstance instance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = instanceToContentValues(instance);

        long newRowId = db.insertWithOnConflict(
                AppContract.TaskInstanceEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        db.close();
        return newRowId;
    }

    public List<TaskInstance> getAllTaskInstancesForTask(String userId, String originalTaskId) {
        List<TaskInstance> instances = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selection = AppContract.TaskInstanceEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                    AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID + " = ?";
            String[] selectionArgs = { userId, originalTaskId };

            cursor = db.query(
                    AppContract.TaskInstanceEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                instances.add(cursorToTaskInstance(cursor));
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return instances;
    }

    public int updateTaskInstance(TaskInstance instance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = instanceToContentValues(instance);

        String selection = AppContract.TaskInstanceEntry._ID + " = ?";
        String[] selectionArgs = { instance.getId() };

        int count = db.update(
                AppContract.TaskInstanceEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    public int deleteTaskInstance(String instanceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AppContract.TaskInstanceEntry._ID + " = ?";
        String[] selectionArgs = { instanceId };

        int deletedRows = db.delete(
                AppContract.TaskInstanceEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
        db.close();
        return deletedRows;
    }

    public TaskInstance getTaskInstanceById(String instanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        TaskInstance instance = null;
        try {
            String selection = AppContract.TaskInstanceEntry._ID + " = ?";
            String[] selectionArgs = { instanceId };

            cursor = db.query(
                    AppContract.TaskInstanceEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                instance = cursorToTaskInstance(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return instance;
    }

    public int deleteAllInstancesForTask(String originalTaskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID + " = ?";
        String[] selectionArgs = { originalTaskId };

        int deletedRows = db.delete(
                AppContract.TaskInstanceEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
        db.close();
        return deletedRows;
    }

    private ContentValues instanceToContentValues(TaskInstance instance) {
        ContentValues values = new ContentValues();
        values.put(AppContract.TaskInstanceEntry._ID, instance.getId());
        values.put(AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID, instance.getOriginalTaskId());
        values.put(AppContract.TaskInstanceEntry.COLUMN_NAME_USER_ID, instance.getUserId());
        values.put(AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_DATE, instance.getOriginalDate().getTime());
        values.put(AppContract.TaskInstanceEntry.COLUMN_NAME_NEW_STATUS, instance.getNewStatus().name());

        if (instance.getCompletionDate() != null) {
            values.put(AppContract.TaskInstanceEntry.COLUMN_NAME_COMPLETION_DATE, instance.getCompletionDate().getTime());
        }

        return values;
    }
    private TaskInstance cursorToTaskInstance(Cursor cursor) {
        TaskInstance instance = new TaskInstance();

        instance.setId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskInstanceEntry._ID)));
        instance.setOriginalTaskId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_TASK_ID)));
        instance.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskInstanceEntry.COLUMN_NAME_USER_ID)));
        instance.setOriginalDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.TaskInstanceEntry.COLUMN_NAME_ORIGINAL_DATE))));
        instance.setNewStatus(TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskInstanceEntry.COLUMN_NAME_NEW_STATUS))));

        int completionDateIndex = cursor.getColumnIndex(AppContract.TaskInstanceEntry.COLUMN_NAME_COMPLETION_DATE);
        if (completionDateIndex != -1 && !cursor.isNull(completionDateIndex)) {
            instance.setCompletionDate(new Date(cursor.getLong(completionDateIndex)));
        }

        return instance;
    }

    private ContentValues categoryToContentValues(Category category) {
        ContentValues values = new ContentValues();
        values.put(AppContract.CategoryEntry._ID, category.getId());
        values.put(AppContract.CategoryEntry.COLUMN_NAME_USER_ID, category.getUserId());
        values.put(AppContract.CategoryEntry.COLUMN_NAME_NAME, category.getName());
        values.put(AppContract.CategoryEntry.COLUMN_NAME_COLOR, category.getColor());
        return values;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.CategoryEntry._ID)));
        category.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.CategoryEntry.COLUMN_NAME_USER_ID)));
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.CategoryEntry.COLUMN_NAME_NAME)));
        category.setColor(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.CategoryEntry.COLUMN_NAME_COLOR)));
        return category;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();

        task.setId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry._ID)));
        task.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_USER_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_DESCRIPTION)));
        task.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID)));
        task.setRecurring(cursor.getInt(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_IS_RECURRING)) == 1);
        task.setRecurrenceInterval(cursor.getInt(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_INTERVAL)));
        task.setExecutionTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_EXECUTION_TIME))));

        int groupIdIndex = cursor.getColumnIndex(AppContract.TaskEntry.COLUMN_NAME_GROUP_ID);
        if (groupIdIndex != -1) {
            task.setGroupId(cursor.getString(groupIdIndex));
        }

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_UNIT))) {
            task.setRecurrenceUnit(RecurrenceUnit.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_UNIT))));
        }
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_START_DATE))) {
            task.setRecurrenceStartDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_START_DATE))));
        }
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE)))
        {task.setCompletionDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE))));}

        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_END_DATE))) {
            task.setRecurrenceEndDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_END_DATE))));
        }

        task.setDifficulty(TaskDifficulty.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_DIFFICULTY))));
        task.setImportance(TaskImportance.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_IMPORTANCE))));
        task.setStatus(TaskStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.TaskEntry.COLUMN_NAME_STATUS))));

        return task;
    }

    private ContentValues taskToContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(AppContract.TaskEntry._ID, task.getId());
        values.put(AppContract.TaskEntry.COLUMN_NAME_USER_ID, task.getUserId());
        values.put(AppContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(AppContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(AppContract.TaskEntry.COLUMN_NAME_CATEGORY_ID, task.getCategoryId());
        values.put(AppContract.TaskEntry.COLUMN_NAME_GROUP_ID, task.getGroupId());
        values.put(AppContract.TaskEntry.COLUMN_NAME_IS_RECURRING, task.isRecurring() ? 1 : 0);
        values.put(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_INTERVAL, task.getRecurrenceInterval());
        values.put(AppContract.TaskEntry.COLUMN_NAME_EXECUTION_TIME, task.getExecutionTime() != null ? task.getExecutionTime().getTime() : null);

        if (task.getRecurrenceUnit() != null) {
            values.put(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_UNIT, task.getRecurrenceUnit().name());
        }
        if (task.getRecurrenceStartDate() != null) {
            values.put(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_START_DATE, task.getRecurrenceStartDate().getTime());
        }
        if (task.getCompletionDate() != null) {
            values.put(AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE, task.getCompletionDate().getTime());
        }
        if (task.getRecurrenceEndDate() != null) {
            values.put(AppContract.TaskEntry.COLUMN_NAME_RECURRENCE_END_DATE, task.getRecurrenceEndDate().getTime());
        }

        values.put(AppContract.TaskEntry.COLUMN_NAME_DIFFICULTY, task.getDifficulty().name());
        values.put(AppContract.TaskEntry.COLUMN_NAME_IMPORTANCE, task.getImportance().name());
        values.put(AppContract.TaskEntry.COLUMN_NAME_STATUS, task.getStatus().name());
        return values;
    }

    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userToContentValues(user);

        long newRowId = db.insertWithOnConflict(
                AppContract.UserEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.close();
        return newRowId;
    }



    public void updateLevelInfo(LevelInfo levelInfo, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = levelInfoToContentValues(levelInfo);

        db.update(
                "levelInfo",     // ime tabele
                values,          // kolone i vrednosti
                "userId = ?",    // where clause
                new String[]{userId} // where args
        );
    }

    private ContentValues userToContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry._ID, user.getId());
        values.put(AppContract.UserEntry.COLUMN_USERNAME, user.getUsername());
        values.put(AppContract.UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(AppContract.UserEntry.COLUMN_PASSWORD, user.getPassword());
        values.put(AppContract.UserEntry.COLUMN_AVATAR, user.getAvatar());
        values.put(AppContract.UserEntry.COLUMN_VERIFIED, user.getVerified());

        return values;
    }

    private ContentValues levelInfoToContentValues(LevelInfo levelInfo) {
        ContentValues values = new ContentValues();
        values.put(AppContract.LevelInfoEntry._ID, levelInfo.getId()); // UUID ili auto-generated ID
        values.put(AppContract.LevelInfoEntry.COLUMN_LEVEL, levelInfo.getLevel());
        values.put(AppContract.LevelInfoEntry.COLUMN_XP, levelInfo.getXp());
        values.put(AppContract.LevelInfoEntry.COLUMN_XP_FOR_NEXT_LEVEL, levelInfo.getXpForNextLevel());
        values.put(AppContract.LevelInfoEntry.COLUMN_XP_TASK_IMPORTANCE, levelInfo.getXpTaskImportance());
        values.put(AppContract.LevelInfoEntry.COLUMN_XP_TASK_DIFFICULTY, levelInfo.getXpTaskDifficulty());
        values.put(AppContract.LevelInfoEntry.COLUMN_PP, levelInfo.getPp());
        values.put(AppContract.LevelInfoEntry.COLUMN_TITLE, levelInfo.getTitle().name());

        return values;
    }

    public int countCompletedTasksByDifficulty(String userId, TaskDifficulty difficulty, Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            // SQL upit koji broji zadatke sa datim statusom, težinom i u vremenskom opsegu
            String sql = "SELECT COUNT(*) FROM " + AppContract.TaskEntry.TABLE_NAME +
                    " WHERE " + AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_STATUS + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_DIFFICULTY + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE + " BETWEEN ? AND ?";

            // Argumenti za upit. Redosled je važan!
            String[] selectionArgs = {
                    userId,
                    TaskStatus.COMPLETED.name(), // Brojimo samo one koji su zaista ZAVRŠENI
                    difficulty.name(),
                    String.valueOf(startDate.getTime()), // Datume konvertujemo u milisekunde (long)
                    String.valueOf(endDate.getTime())
            };

            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0); // Rezultat COUNT(*) je uvek na prvoj poziciji
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return count;
    }

    public int countCompletedTasksByImportance(String userId, TaskImportance importance, Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) FROM " + AppContract.TaskEntry.TABLE_NAME +
                    " WHERE " + AppContract.TaskEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_STATUS + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_IMPORTANCE + " = ? AND " +
                    AppContract.TaskEntry.COLUMN_NAME_COMPLETION_DATE + " BETWEEN ? AND ?";

            String[] selectionArgs = {
                    userId,
                    TaskStatus.COMPLETED.name(),
                    importance.name(),
                    String.valueOf(startDate.getTime()),
                    String.valueOf(endDate.getTime())
            };

            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return count;
    }


}