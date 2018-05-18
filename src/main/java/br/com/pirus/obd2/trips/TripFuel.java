package br.com.pirus.obd2.trips;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.pirus.obd2.entity.EntityTripFuel;


public class TripFuel {

    /// the database version number
    //public static final int DATABASE_VERSION = 1;

    /// the name of the database
    //public static final String DATABASE_NAME = "tripsFuel.db";
    //public static final String DATABASE_NAME = "tripslog.db";

    /// a tag string for debug logging (the name of this class)
    private static final String TAG = TripFuel.class.getName();

    private static final String TRIP_FUEL_TABLE_NAME = "TripFuel";
    private static final String TRIP_FUEL_TIME = "Time";
    private static final String TRIP_FUEL_PERCENT_BEGIN = "PercentBegin";
    private static final String TRIP_FUEL_PERCENT_END = "PercentEnd";
    private static final String TRIP_FUEL_LITERS = "Liters";

    //-- -----------------------------------------------------
    //   -- Create TableTripFuel
    //-- -----------------------------------------------------
    public static final String CREATE_TABLE_TRIP_FUEL =
            "CREATE TABLE IF NOT EXISTS " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + " INTEGER NOT NULL," +
                    TRIP_FUEL_PERCENT_BEGIN + " INTEGER NOT NULL," +
                    TRIP_FUEL_PERCENT_END + " INTEGER NOT NULL," +
                    TRIP_FUEL_LITERS + " INTEGER NOT NULL," +
                    "PRIMARY KEY (" + TRIP_FUEL_TIME + "));";

    //-- -----------------------------------------------------
    //   -- Insert TableFuel
    //-- -----------------------------------------------------
    public static final String INSERT_INTO_TABLE_TRIP_FUEL =
            "INSERT INTO " + TRIP_FUEL_TABLE_NAME + "(" +
                    TRIP_FUEL_TIME + "," +
                    TRIP_FUEL_PERCENT_BEGIN + "," +
                    TRIP_FUEL_PERCENT_END + "," +
                    TRIP_FUEL_LITERS +
                    ") VALUES(?,?,?,?);";

    private static final String[] COLUMNS = new String[]{
            TRIP_FUEL_TIME,
            TRIP_FUEL_PERCENT_BEGIN,
            TRIP_FUEL_PERCENT_END,
            TRIP_FUEL_LITERS
    };

    /// array of all column names for RECORDS_TABLE
    private static final String[] TRIP_FUEL_COLUMNS = new String[]{
            TRIP_FUEL_TIME,
            TRIP_FUEL_PERCENT_BEGIN,
            TRIP_FUEL_PERCENT_END,
            TRIP_FUEL_LITERS
    };

    /// singleton instance
    private static TripFuel instance;

    /// context of the instance creator
    private final Context context;
    /// a helper instance used to open and close the database
    private final TripLogOpenHelper helper;
    /// the database
    private final SQLiteDatabase db;

    public TripFuel(Context context) {
        this.context = context;
        this.helper = new TripLogOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
        this.createTable(this.db);
    }

    //private static Context mContext;

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static TripFuel getInstance(Context context) {
        if (instance == null) {
            instance = new TripFuel(context);
        }
        return instance;
    }

    public void createTable(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL(CREATE_TABLE_TRIP_FUEL);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

/*    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }*/

    public void stmtInsertIntoTableTripFuel(
            SQLiteStatement stmt, long time, long percentBegin, long percentEnd, long liters) {

        stmt.bindLong(1, time);
        stmt.bindLong(2, percentBegin);
        stmt.bindLong(3, percentEnd);
        stmt.bindLong(4, liters);

        stmt.execute();
        stmt.clearBindings();
    }

    public SQLiteDatabase getWritableDatabase() {
        return db;
    }

    public List<EntityTripFuel> readAllRecords() {
        Log.i("readAllRecords", "readAllRecords");

        //update();

        final String tag = TAG + ".readAllRecords()";
        List<EntityTripFuel> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = TRIP_FUEL_TIME;

            cursor = this.db.query(
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
            long percentBegin = c.getLong(c.getColumnIndex(TRIP_FUEL_PERCENT_BEGIN));
            long percentEnd = c.getLong(c.getColumnIndex(TRIP_FUEL_PERCENT_END));
            long liters = c.getLong(c.getColumnIndex(TRIP_FUEL_LITERS));

            record.setTime(time);
            record.setPercentBegin(percentBegin);
            record.setPercentEnd(percentEnd);
            record.setLiters(liters);
        }

        return record;
    }

    public Cursor queryLastFuel() {
        return this.db.rawQuery(
                "SELECT * FROM " + TRIP_FUEL_TABLE_NAME + " ORDER BY ? DESC LIMIT 1;",
                new String[]{
                        TRIP_FUEL_TIME
                }
        );
    }
}
