package br.com.pirus.obd2.trips;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class TripOpenHelper extends SQLiteOpenHelper {

    /// the database version number
    public static final int DATABASE_VERSION = 1;
    /// the name of the database
    public static final String DATABASE_NAME = "trips.db";
    /// tag for logging
    private static final String TAG = TripOpenHelper.class.getName();

    public TripOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        execSQL(db, TripLog.DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void execSQL(SQLiteDatabase db, String[] statements) {
        final String tag = TAG + ".execSQL()";
        for (String sql : statements) {
            Log.d(tag, sql);
            db.execSQL(sql);
        }
    }
}