package com.github.pires.obd.reader.trips;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;


public class TripFuel extends SQLiteOpenHelper {

    private static final String TRIP_FUEL_TABLE_NAME = "TripFuel";
    private static final String TRIP_FUEL_TIME = "Time";
    private static final String TRIP_FUEL_INPUT = "Input";
    private static final String TRIP_FUEL_TANK_CAPACITY = "Tank";

    //-- -----------------------------------------------------
    //   -- Create TableTripFuel
    //-- -----------------------------------------------------
    public static final String CREATE_TABLE_TRIP_FUEL =
            "CREATE TABLE IF NOT EXISTS " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + " INTEGER NOT NULL," +
                    TRIP_FUEL_INPUT + " REAL NOT NULL," +
                    TRIP_FUEL_TANK_CAPACITY + " REAL NOT NULL," +
                    "PRIMARY KEY (" + TRIP_FUEL_TIME + "));";

    //-- -----------------------------------------------------
    //   -- Insert TableFuel
    //-- -----------------------------------------------------
    public static final String INSERT_INTO_TABLE_TRIP_FUEL =
            "INSERT INTO " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + "," +
                    TRIP_FUEL_INPUT + "," +
                    TRIP_FUEL_TANK_CAPACITY +
                    ") VALUES(?,?,?);";


    public TripFuel(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
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

    public void stmtInsertIntoTableTripFuel(SQLiteStatement stmt, long time, float input, double tank) {
        stmt.bindLong(1, time);
        stmt.bindDouble(2, input);
        stmt.bindDouble(3, tank);
        stmt.execute();
        stmt.clearBindings();
    }
}
