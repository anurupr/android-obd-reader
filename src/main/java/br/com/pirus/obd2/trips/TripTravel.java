package br.com.pirus.obd2.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.pirus.obd2.entity.EntityTripTravel;

/**
 * Some code taken from https://github.com/wdkapps/FillUp
 */
public class TripTravel {


    /// a tag string for debug logging (the name of this class)
    private static final String TAG = TripTravel.class.getName();
    /// database table names
    private static final String RECORDS_TABLE = "Records";
    /// SQL commands to delete the database
    public static final String[] DATABASE_DELETE = new String[]{
            "drop table if exists " + RECORDS_TABLE + ";",
    };
    /// column names for RECORDS_TABLE
    private static final String RECORD_ID = "id";
    private static final String RECORD_START_DATE = "startDate";
    private static final String RECORD_END_DATE = "endDate";
    private static final String RECORD_RPM_MAX = "rmpMax";
    private static final String RECORD_SPEED_MAX = "speedMax";
    private static final String RECORD_ENGINE_RUNTIME = "engineRuntime";
    private static final String RECORD_CONSUMPTION = "consumption";


    /// SQL commands to create the database
    public static final String[] DATABASE_CREATE = new String[]{
            "create table " + RECORDS_TABLE + " ( " +
                    RECORD_ID + " integer primary key autoincrement, " +
                    RECORD_START_DATE + " integer not null, " +
                    RECORD_END_DATE + " integer, " +
                    RECORD_SPEED_MAX + " integer, " +
                    RECORD_RPM_MAX + " integer, " +
                    RECORD_ENGINE_RUNTIME + " text," +
                    RECORD_CONSUMPTION + " real" +
                    ");"
    };
    /// array of all column names for RECORDS_TABLE
    private static final String[] RECORDS_TABLE_COLUMNS = new String[]{
            RECORD_ID,
            RECORD_START_DATE,
            RECORD_END_DATE,
            RECORD_SPEED_MAX,
            RECORD_ENGINE_RUNTIME,
            RECORD_RPM_MAX,
            RECORD_CONSUMPTION
    };

    /// singleton instance
    private static TripTravel instance;
    /// context of the instance creator
    private final Context context;
    /// a helper instance used to open and close the database
    private final TripOpenHelper helper;
    /// the database
    private final SQLiteDatabase db;

    private TripTravel(Context context) {
        this.context = context;
        this.helper = new TripOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
    }

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static TripTravel getInstance(Context context) {
        if (instance == null) {
            instance = new TripTravel(context);
        }
        return instance;
    }

    /**
     * DESCRIPTION:
     * Convenience method to test assertion.
     *
     * @param assertion - an asserted boolean condition.
     * @param tag       - a tag String identifying the calling method.
     * @param msg       - an error message to display/log.
     * @throws RuntimeException if the assertion is false
     */
    private void ASSERT(boolean assertion, String tag, String msg) {
        if (!assertion) {
            String assert_msg = "ASSERT failed: " + msg;
            Log.e(tag, assert_msg);
            throw new RuntimeException(assert_msg);
        }
    }

    public EntityTripTravel startTrip() {
        final String tag = TAG + ".createRecord()";

        try {
            EntityTripTravel record = new EntityTripTravel();
            long rowID = db.insertOrThrow(RECORDS_TABLE, null, getContentValues(record));
            record.setID((int) rowID);
            return record;
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return null;
    }

    /**
     * DESCRIPTION:
     * Updates a trip record in the log.
     *
     * @param record - the TripRecord to update.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean updateRecord(EntityTripTravel record) {
        final String tag = TAG + ".updateRecord()";
        ASSERT((record.getID() != null), tag, "record id cannot be null");
        boolean success = false;
        try {
            ContentValues values = getContentValues(record);
            values.remove(RECORD_ID);
            String whereClause = RECORD_ID + "=" + record.getID();
            int count = db.update(RECORDS_TABLE, values, whereClause, null);
            success = (count > 0);
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return success;
    }

    /**
     * DESCRIPTION:
     * Convenience method to convert a TripRecord instance to a set of key/value
     * pairs in a ContentValues instance utilized by SQLite access methods.
     *
     * @param record - the GasRecord to convert.
     * @return a ContentValues instance representing the specified GasRecord.
     */
    private ContentValues getContentValues(EntityTripTravel record) {
        ContentValues values = new ContentValues();
        values.put(RECORD_ID, record.getID());
        values.put(RECORD_START_DATE, record.getStartDate().getTime());
        if (record.getEndDate() != null)
            values.put(RECORD_END_DATE, record.getEndDate().getTime());
        values.put(RECORD_RPM_MAX, record.getEngineRpmMax());
        values.put(RECORD_SPEED_MAX, record.getSpeedMax());
        values.put(RECORD_CONSUMPTION, record.getConsumption());
        if (record.getEngineRuntime() != null)
            values.put(RECORD_ENGINE_RUNTIME, record.getEngineRuntime());
        return values;
    }

    private void update() {
        String sql = "ALTER TABLE " + RECORDS_TABLE + " ADD COLUMN " + RECORD_ENGINE_RUNTIME + " integer;";
        db.execSQL(sql);
    }

    public List<EntityTripTravel> readAllRecords() {

        //update();

        final String tag = TAG + ".readAllRecords()";
        List<EntityTripTravel> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = RECORD_START_DATE;
            cursor = db.query(
                    RECORDS_TABLE,
                    RECORDS_TABLE_COLUMNS,
                    null,
                    null, null, null,
                    orderBy,
                    null
            );

            // create a list of TripRecords from the data
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        EntityTripTravel record = getRecordFromCursor(cursor);
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
     * Deletes a specified trip record from the log.
     *
     * @param id - the TripRecord to delete.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean deleteTrip(long id) {

        final String tag = TAG + ".deleteRecord()";

        boolean success = false;

        try {
            String whereClause = RECORD_ID + "=" + id;
            String[] whereArgs = null;
            int count = db.delete(RECORDS_TABLE, whereClause, whereArgs);
            success = (count == 1);
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }

        return success;
    }

    /**
     * DESCRIPTION:
     * Convenience method to create a TripRecord instance from values read
     * from the database.
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private EntityTripTravel getRecordFromCursor(Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";
        EntityTripTravel record = null;
        if (c != null) {
            record = new EntityTripTravel();
            int id = c.getInt(c.getColumnIndex(RECORD_ID));
            long startDate = c.getLong(c.getColumnIndex(RECORD_START_DATE));
            long endTime = c.getLong(c.getColumnIndex(RECORD_END_DATE));
            int engineRpmMax = c.getInt(c.getColumnIndex(RECORD_RPM_MAX));
            int speedMax = c.getInt(c.getColumnIndex(RECORD_SPEED_MAX));
            double consumption = c.getDouble(c.getColumnIndex(RECORD_CONSUMPTION));


            record.setID(id);
            record.setStartDate(new Date(startDate));
            record.setEndDate(new Date(endTime));
            record.setEngineRpmMax(engineRpmMax);
            record.setSpeedMax(speedMax);
            record.setConsumption(consumption);


            if (!c.isNull(c.getColumnIndex(RECORD_ENGINE_RUNTIME)))
                record.setEngineRuntime(c.getString(c.getColumnIndex(RECORD_ENGINE_RUNTIME)));
        }
        return record;
    }
}
