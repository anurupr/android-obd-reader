package com.github.pires.obd.reader.trips;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.github.pires.obd.reader.entity.EntityTripFuel;

import java.util.ArrayList;
import java.util.List;


public class TripFuel extends SQLiteOpenHelper {

    /// the database version number
    public static final int DATABASE_VERSION = 1;

    /// the name of the database
    public static final String DATABASE_NAME = "tripsFuel.db";

    /// a tag string for debug logging (the name of this class)
    private static final String TAG = TripFuel.class.getName();

    private static final String TRIP_FUEL_TABLE_NAME = "TripFuel";
    private static final String TRIP_FUEL_TIME = "Time";
    private static final String TRIP_FUEL_PERCENT = "Percent";
    private static final String TRIP_FUEL_LITERS = "Liters";
    //-- -----------------------------------------------------
    //   -- Create TableTripFuel
    //-- -----------------------------------------------------
    public static final String CREATE_TABLE_TRIP_FUEL =
            "CREATE TABLE IF NOT EXISTS " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + " INTEGER NOT NULL," +
                    TRIP_FUEL_PERCENT + " INTEGER NOT NULL," +
                    TRIP_FUEL_LITERS + " INTEGER NOT NULL," +
                    "PRIMARY KEY (" + TRIP_FUEL_TIME + "));";

    //-- -----------------------------------------------------
    //   -- Insert TableFuel
    //-- -----------------------------------------------------
    public static final String INSERT_INTO_TABLE_TRIP_FUEL =
            "INSERT INTO " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + "," +
                    TRIP_FUEL_PERCENT + "," +
                    TRIP_FUEL_LITERS +
                    ") VALUES(?,?,?);";

    /// array of all column names for RECORDS_TABLE
    private static final String[] TRIP_FUEL_COLUMNS = new String[]{
            TRIP_FUEL_TIME,
            TRIP_FUEL_PERCENT,
            TRIP_FUEL_LITERS
    };

    /// singleton instance
    private static TripFuel instance;

    public TripFuel(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static TripFuel getInstance(Context context) {
        if (instance == null) {
            instance = new TripFuel(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL(CREATE_TABLE_TRIP_FUEL);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void stmtInsertIntoTableTripFuel(SQLiteStatement stmt, long time, long percent, long liters) {
        stmt.bindLong(1, time);
        stmt.bindLong(2, percent);
        stmt.bindLong(3, liters);

        stmt.execute();
        stmt.clearBindings();
    }

    public List<EntityTripFuel> readAllRecords() {
        Log.i("readAllRecords", "readAllRecords");

        //update();

        final String tag = TAG + ".readAllRecords()";
        List<EntityTripFuel> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = TRIP_FUEL_TIME;

            cursor = getReadableDatabase().query(
                    TRIP_FUEL_TABLE_NAME,
                    TRIP_FUEL_COLUMNS,
                    null,
                    null, null, null,
                    orderBy,
                    null
            );

            // create a list of TripRecords from the data
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        EntityTripFuel record = getRecordFromCursor(cursor);

                        Log.i("record", "###" + record.toString());


                        list.add(record);
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
            list.clear();
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    /**
     * DESCRIPTION:
     * Convenience method to create a TripRecord instance from values read
     * from the database.
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private EntityTripFuel getRecordFromCursor(Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";

        EntityTripFuel record = null;

        if (c != null) {
            record = new EntityTripFuel();

            long time = c.getLong(c.getColumnIndex(TRIP_FUEL_TIME));
            long percent = c.getLong(c.getColumnIndex(TRIP_FUEL_PERCENT));
            long liters = c.getLong(c.getColumnIndex(TRIP_FUEL_LITERS));

            record.setTime(time);
            record.setPercent(percent);
            record.setLiters(liters);
        }

        return record;
    }
}
