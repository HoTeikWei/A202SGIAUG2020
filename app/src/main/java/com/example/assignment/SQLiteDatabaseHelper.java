package com.example.assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {


    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;

    private final String RECIPE_TABLE = "recipe";
    private final String KEY_1 = "id";
    private final String KEY_2 = "name";
    private final String KEY_3 = "tag";
    private final String KEY_4 = "ingredient";
    private final String KEY_5 = "step";
    private final String KEY_6 = "user";

    private final String createTableQuery = "CREATE TABLE " + RECIPE_TABLE + "(" + KEY_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_2 + " TEXT, " +
            KEY_3 + " TEXT, " + KEY_4 + " TEXT, " + KEY_5 + " TEXT, " + KEY_6 + " TEXT)";

    private final String dropTableQuery = "DROP TABLE " + RECIPE_TABLE;

    public SQLiteDatabaseHelper(@Nullable Context context) {
        super(context, "local.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteDatabaseHelper.class.getSimpleName(), "Upgrading");
        db.execSQL(dropTableQuery);
        db.execSQL(createTableQuery);
        onCreate(db);
    }

    //used to insert data to sqlite
    public boolean saveRecipe(LocalRecipe lr) {

        if (dbWrite == null)
            dbWrite = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_2, lr.getName());
        contentValues.put(KEY_3, lr.getTag());
        contentValues.put(KEY_4, lr.getIngredient());
        contentValues.put(KEY_5, lr.getStep());
        contentValues.put(KEY_6, lr.getUser());

        try {
            long ins = dbWrite.insert(RECIPE_TABLE, null, contentValues);
            if (ins == -1) {
                Log.e("INSERT ERROR: ", "ins is equal to -1");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            Log.e("INSERT TABLE ERROR: ", e.getMessage());
            return false;
        }
    }

    //used to retrieve all data to cursor
    public Cursor getAllData() {
        Cursor cursor = null;
        if (dbRead == null)
            dbRead = getReadableDatabase();
        cursor = dbRead.rawQuery("SELECT * FROM " + RECIPE_TABLE + " ORDER BY " + KEY_1 + " ASC ", null);
        return cursor;
    }

    //used to retrieve data with selected argument to cursor
    public Cursor getMyData() {
        Cursor cursor = null;
        if (dbRead == null)
            dbRead = getReadableDatabase();
        cursor = dbRead.rawQuery("SELECT * FROM " + RECIPE_TABLE + " WHERE " + KEY_6 + "=? ORDER BY " + KEY_1 + " ASC ", new String[]{"local"});
        return cursor;
    }
    //used to retrieve data with selected argument to cursor
    public Cursor getDownloadData() {
        Cursor cursor = null;
        if (dbRead == null)
            dbRead = getReadableDatabase();
        cursor = dbRead.rawQuery("SELECT * FROM " + RECIPE_TABLE + " WHERE " + KEY_6 + "!=? ORDER BY " + KEY_1 + " ASC ", new String[]{"local"});
        return cursor;
    }

    //used to retrieve all data
    public List<LocalRecipe> localRecipeList() {
        List<LocalRecipe> lrList = new ArrayList<>();
        Cursor cursor = getAllData();
        while (cursor.moveToNext()) {
            LocalRecipe localrecipe = new LocalRecipe();
            localrecipe.setId(cursor.getInt(cursor.getColumnIndex(KEY_1)));
            localrecipe.setName(cursor.getString(cursor.getColumnIndex(KEY_2)));
            localrecipe.setTag(cursor.getString(cursor.getColumnIndex(KEY_3)));
            localrecipe.setIngredient(cursor.getString(cursor.getColumnIndex(KEY_4)));
            localrecipe.setStep(cursor.getString(cursor.getColumnIndex(KEY_5)));
            localrecipe.setUser(cursor.getString(cursor.getColumnIndex(KEY_6)));
            lrList.add(localrecipe);
        }
        return lrList;
    }
    //used to retrieve all selected data
    public List<LocalRecipe> myLocalRecipeList() {
        List<LocalRecipe> lrList = new ArrayList<>();
        Cursor cursor = getMyData();
        while (cursor.moveToNext()) {
            LocalRecipe localrecipe = new LocalRecipe();
            localrecipe.setId(cursor.getInt(cursor.getColumnIndex(KEY_1)));
            localrecipe.setName(cursor.getString(cursor.getColumnIndex(KEY_2)));
            localrecipe.setTag(cursor.getString(cursor.getColumnIndex(KEY_3)));
            localrecipe.setIngredient(cursor.getString(cursor.getColumnIndex(KEY_4)));
            localrecipe.setStep(cursor.getString(cursor.getColumnIndex(KEY_5)));
            localrecipe.setUser(cursor.getString(cursor.getColumnIndex(KEY_6)));
            lrList.add(localrecipe);
        }
        return lrList;
    }
    //used to retrieve all selected data
    public List<LocalRecipe> downloadLocalRecipeList() {
        List<LocalRecipe> lrList = new ArrayList<>();
        Cursor cursor = getDownloadData();
        while (cursor.moveToNext()) {
            LocalRecipe localrecipe = new LocalRecipe();
            localrecipe.setId(cursor.getInt(cursor.getColumnIndex(KEY_1)));
            localrecipe.setName(cursor.getString(cursor.getColumnIndex(KEY_2)));
            localrecipe.setTag(cursor.getString(cursor.getColumnIndex(KEY_3)));
            localrecipe.setIngredient(cursor.getString(cursor.getColumnIndex(KEY_4)));
            localrecipe.setStep(cursor.getString(cursor.getColumnIndex(KEY_5)));
            localrecipe.setUser(cursor.getString(cursor.getColumnIndex(KEY_6)));
            lrList.add(localrecipe);
        }
        return lrList;
    }

    //used to retrieve selected data
    public LocalRecipe getLocalRecipe(int id) {
        LocalRecipe localrecipe = new LocalRecipe();
        try {
            if (dbRead == null)
                dbRead = getReadableDatabase();

            Cursor cursor = dbRead.rawQuery("SELECT * FROM " + RECIPE_TABLE + " WHERE " + KEY_1 + "=?", new String[]{String.valueOf(id)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                localrecipe.setId(cursor.getInt(cursor.getColumnIndex(KEY_1)));
                localrecipe.setName(cursor.getString(cursor.getColumnIndex(KEY_2)));
                localrecipe.setTag(cursor.getString(cursor.getColumnIndex(KEY_3)));
                localrecipe.setIngredient(cursor.getString(cursor.getColumnIndex(KEY_4)));
                localrecipe.setStep(cursor.getString(cursor.getColumnIndex(KEY_5)));
                localrecipe.setUser(cursor.getString(cursor.getColumnIndex(KEY_6)));
            }
        } catch (Exception e) {
            Log.e("Error get: ", e.getMessage());
        } finally {
            return localrecipe;
        }
    }

    //used to update selected data information
    public boolean editLocalRecipe(LocalRecipe lr) {
        try {
            if (dbWrite == null)
                dbWrite = getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_2, lr.getName());
            contentValues.put(KEY_3, lr.getTag());
            contentValues.put(KEY_4, lr.getIngredient());
            contentValues.put(KEY_5, lr.getStep());

            long ins = dbWrite.update(RECIPE_TABLE, contentValues, KEY_1 + "=?", new String[]{String.valueOf(lr.getId())});
            if (ins == -1) {
                Log.e("UPDATE ERROR: ", "ins is equal to -1");
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            Log.e("UPDATE ERROR: ", e.getMessage());
            return false;
        }
    }

    //used to delete selected data
    public boolean deleteLocalRecipe(String id) {
        try {
            if (dbWrite == null)
                dbWrite = getWritableDatabase();

            long ins = dbWrite.delete(RECIPE_TABLE, KEY_1 + "=?", new String[]{id});
            if (ins == -1) {
                Log.e("DELETE ERROE:", "ins is equal to -1!");
                return false;
            } else
                return true;
        } catch (Exception e) {
            Log.e("DELETE ERROR: ", e.getMessage());
            return false;
        }
    }


}
