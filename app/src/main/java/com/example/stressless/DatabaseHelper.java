package com.example.stressless;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String TABLE_USER_INFO = "user_info";
    private static final String COL_INFO_ID = "id";
    private static final String COL_INFO_USER_ID = "user_id";
    private static final String COL_PHOTO = "photo";
    private static final String COL_AGE = "age";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_HEIGHT = "height";
    private static final String COL_SPORT_PRACTICE = "sport_practice";
    private static final String COL_NOTES = "notes";
    private static final String TABLE_USER_HRV = "user_hrv";
    private static final String COL_HRV_ID = "id";
    private static final String COL_HRV_USER_ID = "user_id";
    private static final String COL_HRV_DATE = "date";
    private static final String COL_HRV_RMSSD = "rmssd";
    private static final String COL_HRV_RR_INTERVALS = "rr_intervals";
    private static final String COL_ECG_DATA = "ecg_data";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_FIRST_NAME + " TEXT, " +
                COL_LAST_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        String createUserInfoTable = "CREATE TABLE " + TABLE_USER_INFO + " (" +
                COL_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_INFO_USER_ID + " INTEGER, " +
                COL_PHOTO + " TEXT, " +
                COL_AGE + " TEXT, " +
                COL_WEIGHT + " TEXT, " +
                COL_HEIGHT + " TEXT, " +
                COL_SPORT_PRACTICE + " INTEGER, " +
                COL_NOTES + " TEXT, " +
                "FOREIGN KEY(" + COL_INFO_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createUserInfoTable);

        String createUserHRVTable = "CREATE TABLE " + TABLE_USER_HRV + " (" +
                COL_HRV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HRV_USER_ID + " INTEGER, " +
                COL_HRV_DATE + " TEXT, " +
                COL_HRV_RMSSD + " REAL, " +
                COL_HRV_RR_INTERVALS + " TEXT, " +
                COL_ECG_DATA + " TEXT, " +
                "FOREIGN KEY(" + COL_HRV_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createUserHRVTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createUserInfoTable = "CREATE TABLE " + TABLE_USER_INFO + " (" +
                    COL_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_INFO_USER_ID + " INTEGER, " +
                    COL_PHOTO + " TEXT, " +
                    COL_AGE + " TEXT, " +
                    COL_WEIGHT + " TEXT, " +
                    COL_HEIGHT + " TEXT, " +
                    COL_SPORT_PRACTICE + " INTEGER, " +
                    COL_NOTES + " TEXT, " +
                    "FOREIGN KEY(" + COL_INFO_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
            db.execSQL(createUserInfoTable);
        }
    }

    public String getFirstName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String firstName = null;

        try (Cursor cursor = db.rawQuery(
                "SELECT " + COL_FIRST_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?",
                new String[]{username}
        )) {
            if (cursor.moveToFirst()) {
                firstName = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return firstName;
    }

    public String getLastName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastName = null;

        try (Cursor cursor = db.rawQuery(
                "SELECT " + COL_LAST_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?",
                new String[]{username}
        )) {
            if (cursor.moveToFirst()) {
                lastName = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastName;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}
        )) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveUserInfo(int userId, String photo, String age, String weight, String height, int sportPractice, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_INFO_USER_ID, userId);
        values.put(COL_PHOTO, photo);
        values.put(COL_AGE, age);
        values.put(COL_WEIGHT, weight);
        values.put(COL_HEIGHT, height);
        values.put(COL_SPORT_PRACTICE, sportPractice);
        values.put(COL_NOTES, notes);

        try {
            long result = db.insert(TABLE_USER_INFO, null, values);
            if (result == -1) {
                Log.e("DatabaseHelper", "Failed to insert user info for userId: " + userId);
            }
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saving user info: " + e.getMessage());
            return false;
        }

    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;

        Log.d("DatabaseHelper", "Searching for user with username: " + username);

        try (Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE username = ?",
                new String[]{username}
        )) {
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);
                Log.d("DatabaseHelper", "User found with ID: " + userId);
            } else {
                Log.d("DatabaseHelper", "User not found in database.");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching user ID", e);
        }

        return userId;
    }

    public Cursor getLastUserInfo(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM user_info WHERE user_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean insertUser(String username, String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", username);
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);

        return result != -1;
    }

    public boolean saveHRVData(int userId, String date, double rmssd, List<Integer> rrIntervals, List<Integer> ecgData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            if (userId <= 0 || date == null || date.isEmpty() || rrIntervals == null || rrIntervals.isEmpty()) {
                Log.e("DatabaseHelper", "Invalid input for HRV data");
                return false;
            }

            values.put(COL_HRV_USER_ID, userId);
            values.put(COL_HRV_DATE, date);
            values.put(COL_HRV_RMSSD, rmssd);

            String rrIntervalsString = rrIntervals.toString().replaceAll("[\\[\\]\\s]", "");
            values.put(COL_HRV_RR_INTERVALS, rrIntervalsString);

            if (ecgData != null && !ecgData.isEmpty()) {
                String ecgDataString = ecgData.toString().replaceAll("[\\[\\]\\s]", "");
                values.put(COL_ECG_DATA, ecgDataString);
            } else {
                values.put(COL_ECG_DATA, "");
            }

            long result = db.insert(TABLE_USER_HRV, null, values);

            if (result == -1) {
                Log.e("DatabaseHelper", "Failed to insert HRV data for userId: " + userId);
                return false;
            }

            Log.d("DatabaseHelper", "HRV data saved successfully for userId: " + userId);
            return true;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saving HRV data: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }


}
