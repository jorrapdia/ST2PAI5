package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {
    }

    public static class clientEntry implements BaseColumns {
        public static final String TABLE_NAME = "clients";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PUBLICKKEY = "public_key";
        public static final String COLUMN_NAME_PRIVATEKEY = "private_key";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + clientEntry.TABLE_NAME + " (" +
                    clientEntry._ID + " INTEGER PRIMARY KEY," +
                    clientEntry.COLUMN_NAME_NAME + " TEXT," +
                    clientEntry.COLUMN_NAME_PUBLICKKEY + " TEXT," +
                    clientEntry.COLUMN_NAME_PRIVATEKEY + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + clientEntry.TABLE_NAME;

    public class ClientDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "clients.db";

        public ClientDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

}