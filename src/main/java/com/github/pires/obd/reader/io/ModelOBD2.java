package com.github.pires.obd.reader.io;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;


public class ModelOBD2 extends SQLiteOpenHelper {

    private static final String FUEL_TABLE_NAME = "TableFuel";
    private static final String FUEL_ID_FUEL = "FuelIDFuel";
    private static final String FUEL_NAME = "FuelName";
    private static final String FUEL_AIR_RATIO = "FuelAirRatio";
    private static final String FUEL_DENSITY = "FuelDensity";

    private static final String VEHICLE_TABLE_NAME = "TableVehicle";
    private static final String VEHICLE_VIN = "VehicleVIN";
    private static final String VEHICLE_ID_FUEL = "VehicleIDFuel";
    private static final String VEHICLE_NAME = "VehicleName";
    private static final String VEHICLE_TANK_CAPACITY = "VehicleTankCapacity";

    private static final String TRIP_TABLE_NAME = "TableTrip";
    private static final String TRIP_VIN = "TripVIN";
    private static final String TRIP_ID_FUEL = "TripIDFuel";
    private static final String TRIP_TYPE = "TripType";
    private static final String TRIP_DATE_HOUR = "TripDateHour";
    private static final String TRIP_MASS_AIR_FLOW = "TripMassAirFlow";
    private static final String TRIP_VEHICLE_SPEED = "TripVehicleSpeed";
    private static final String TRIP_FUEL_LEVEL_INPUT = "TripFuelLevelInput";

    //-- -----------------------------------------------------
    //   -- Create TableFuel
    //-- -----------------------------------------------------
    private static final String CREATE_TABLE_FUEL =
            "CREATE TABLE IF NOT EXISTS " + FUEL_TABLE_NAME + "(" +
                    FUEL_ID_FUEL + " INTEGER NOT NULL," +
                    FUEL_NAME + " TEXT NULL," +
                    FUEL_AIR_RATIO + " REAL NULL," +
                    FUEL_DENSITY + " INTEGER NULL," +
                    "PRIMARY KEY (" + FUEL_ID_FUEL + "));";

    //-- -----------------------------------------------------
    //   -- Create TableVehicle
    //-- -----------------------------------------------------
    private static final String CREATE_TABLE_VEHICLE =
            "CREATE TABLE IF NOT EXISTS " + VEHICLE_TABLE_NAME + "(" +
                    VEHICLE_VIN + " TEXT NOT NULL," +
                    VEHICLE_ID_FUEL + " INTEGER NOT NULL," +
                    VEHICLE_NAME + " TEXT NULL," +
                    VEHICLE_TANK_CAPACITY + " REAL NULL," +
                    "PRIMARY KEY(" + VEHICLE_VIN + "," + VEHICLE_ID_FUEL + ")," +
                    "FOREIGN KEY(" + VEHICLE_ID_FUEL + ")" +
                    " REFERENCES TableFuel(" + FUEL_ID_FUEL + ")" +
                    " ON DELETE CASCADE" +
                    " ON UPDATE CASCADE);";

    //-- -----------------------------------------------------
    //   -- Create TableTrip
    //-- -----------------------------------------------------
    private static final String CREATE_TABLE_TRIP =
            "CREATE TABLE IF NOT EXISTS " + TRIP_TABLE_NAME + "(" +
                    TRIP_VIN + " TEXT NOT NULL," +
                    TRIP_ID_FUEL + " INTEGER NOT NULL," +
                    TRIP_TYPE + " INTEGER NOT NULL," +
                    TRIP_DATE_HOUR + " INTEGER NULL," +
                    TRIP_MASS_AIR_FLOW + " REAL NULL," +
                    TRIP_VEHICLE_SPEED + " INTEGER NULL," +
                    TRIP_FUEL_LEVEL_INPUT + " REAL NULL," +
                    "CONSTRAINT TRIP_UNIQUE UNIQUE(" + TRIP_VIN + "," + TRIP_TYPE + ")," +
                    "FOREIGN KEY(" + TRIP_VIN + "," + TRIP_ID_FUEL + ")" +
                    " REFERENCES " + VEHICLE_TABLE_NAME + "(" + VEHICLE_VIN + "," + VEHICLE_ID_FUEL + ")" +
                    " ON DELETE CASCADE" +
                    " ON UPDATE CASCADE);";


    //-- -----------------------------------------------------
    //   -- Insert TableFuel
    //-- -----------------------------------------------------
    private static final String INSERT_INTO_TABLE_FUEL =
            "INSERT INTO " + FUEL_TABLE_NAME + "(" +
                    FUEL_ID_FUEL + "," +
                    FUEL_NAME + "," +
                    FUEL_AIR_RATIO + "," +
                    FUEL_DENSITY +
                    ") VALUES(?,?,?,?);";

    //-- -----------------------------------------------------
    //   -- Insert TableVehicle
    //-- -----------------------------------------------------
    private static final String INSERT_INTO_TABLE_VEHICLE =
            "INSERT INTO " + VEHICLE_TABLE_NAME + "(" +
                    VEHICLE_VIN + "," +
                    VEHICLE_ID_FUEL + "," +
                    VEHICLE_NAME + "," +
                    VEHICLE_TANK_CAPACITY +
                    ") VALUES(?,?,?,?);";


    //-- -----------------------------------------------------
    //   -- Insert TableTrip
    //-- -----------------------------------------------------
    private static final String INSERT_INTO_TABLE_TRIP =
            "INSERT INTO " + TRIP_TABLE_NAME + "(" +
                    TRIP_VIN + "," +
                    TRIP_ID_FUEL + "," +
                    TRIP_TYPE + "," +
                    TRIP_DATE_HOUR + "," +
                    TRIP_MASS_AIR_FLOW + "," +
                    TRIP_VEHICLE_SPEED + "," +
                    TRIP_FUEL_LEVEL_INPUT +
                    ") VALUES(?,?,?,?,?,?,?);";


    private SQLiteStatement stmtInsertIntoTableFuel;
    private SQLiteStatement stmtInsertIntoTableVehicle;
    private SQLiteStatement stmtInsertIntoTableTrip;


    public ModelOBD2(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.beginTransaction();

        try {
            db.execSQL(CREATE_TABLE_FUEL);
            db.execSQL(CREATE_TABLE_VEHICLE);
            db.execSQL(CREATE_TABLE_TRIP);

            stmtInsertIntoTableFuel = db.compileStatement(INSERT_INTO_TABLE_FUEL);
            stmtInsertIntoTableVehicle = db.compileStatement(INSERT_INTO_TABLE_VEHICLE);
            stmtInsertIntoTableTrip = db.compileStatement(INSERT_INTO_TABLE_TRIP);

            insertIntoTableFuel(1, "Diesel", 14.5, 800);
            insertIntoTableFuel(2, "E27", 13.15, 799);
            insertIntoTableFuel(3, "Ethanol", 9, 789);
            insertIntoTableFuel(4, "GasNatural", 17.2, 712);
            insertIntoTableFuel(5, "Gasoline", 14.68, 803);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertIntoTableFuel(long id, String name, double airFuel, long densityFuel) {
        stmtInsertIntoTableFuel.bindLong(1, id);
        stmtInsertIntoTableFuel.bindString(2, name);
        stmtInsertIntoTableFuel.bindDouble(3, airFuel);
        stmtInsertIntoTableFuel.bindLong(4, densityFuel);
        stmtInsertIntoTableFuel.execute();
        stmtInsertIntoTableFuel.clearBindings();
    }

    public void insertIntoTableVehicle(String vin, long id, String name, double tankCapacity) {
        stmtInsertIntoTableVehicle.bindString(1, vin);
        stmtInsertIntoTableVehicle.bindLong(2, id);
        stmtInsertIntoTableVehicle.bindString(3, name);
        stmtInsertIntoTableVehicle.bindDouble(4, tankCapacity);
        stmtInsertIntoTableVehicle.execute();
        stmtInsertIntoTableVehicle.clearBindings();
    }

    public void insertIntoTableTrip(
            String vin, long id, long type, long dateHour,
            double massAirFlow, long vehicleSpeed, double fuelLevelInput) {

        stmtInsertIntoTableTrip.bindString(1, vin);
        stmtInsertIntoTableTrip.bindLong(2, id);
        stmtInsertIntoTableTrip.bindLong(3, type);
        stmtInsertIntoTableTrip.bindLong(4, dateHour);
        stmtInsertIntoTableTrip.bindDouble(4, massAirFlow);
        stmtInsertIntoTableTrip.bindLong(4, vehicleSpeed);
        stmtInsertIntoTableTrip.bindDouble(4, fuelLevelInput);
        stmtInsertIntoTableTrip.execute();
        stmtInsertIntoTableTrip.clearBindings();
    }
}