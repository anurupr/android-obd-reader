package br.com.pirus.obd2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.github.pires.obd.reader.entity.EntityTripRecord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.pirus.obd2.R;
import br.com.pirus.obd2.config.ObdConfig;
import br.com.pirus.obd2.io.AbstractGatewayService;
import br.com.pirus.obd2.io.LogCSVWriter;
import br.com.pirus.obd2.io.MockObdGatewayService;
import br.com.pirus.obd2.io.ObdCommandJob;
import br.com.pirus.obd2.io.ObdGatewayService;
import br.com.pirus.obd2.io.ObdProgressListener;
import br.com.pirus.obd2.net.ObdReading;
import br.com.pirus.obd2.net.ObdService;
import br.com.pirus.obd2.trips.TripFuel;
import br.com.pirus.obd2.trips.TripLog;
import br.com.pirus.obd2.util.CalcOBD2;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static br.com.pirus.obd2.activity.ConfigActivity.getGpsDistanceUpdatePeriod;
import static br.com.pirus.obd2.activity.ConfigActivity.getGpsUpdatePeriod;

//import com.google.inject.Inject;
/*import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;*/

// Some code taken from https://github.com/barbeau/gpstest

//@ContentView(R.layout.main)
public class MainActivity extends Activity implements ObdProgressListener, LocationListener, GpsStatus.Listener {

    private static final String TAG = MainActivity.class.getName();
    private static final int NO_BLUETOOTH_ID = 0;
    private static final int BLUETOOTH_DISABLED = 1;
    private static final int START_LIVE_DATA = 2;
    private static final int STOP_LIVE_DATA = 3;
    private static final int SETTINGS = 4;
    private static final int GET_DTC = 5;
    private static final int TABLE_ROW_MARGIN = 7;
    private static final int NO_ORIENTATION_SENSOR = 8;
    private static final int NO_GPS_SUPPORT = 9;
    private static final int TRIPS_LIST = 10;
    private static final int SAVE_TRIP_NOT_AVAILABLE = 11;
    private static final int FUEL_LIST = 12;
    private static final int REQUEST_ENABLE_BT = 13;
    private static final int REQUEST_PERMISSIONS = 14;

    // 1 minute
    // 60000

    // 2 minutes
    // 120000

    // 5 minutes
    // 300000
    public static boolean hasPermissionGps = false;
    //private final long TIME_INTERVAL = 60000;
    private static boolean bluetoothDefaultIsEnable = false;
    private final long TIME_INTERVAL = 120000;
    /*
    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }
    */
    public Map<String, String> commandResult = new HashMap<>();
    boolean mGpsIsStarted = false;

    private int paramSpeed = 0;
    private double paramMaf = 0;
    private int paramRpm = 0;
    private double paramThrottle = 0;

    private LocationManager mLocService;
    private LocationProvider mLocProvider;
    private LogCSVWriter myCSVWriter;
    private Location mLastLocation;
    /// the trip log
    private TripLog triplog;
    private EntityTripRecord currentTrip;

    private String consumptionResult = "0";
    private String consumptionAverage = "0";

    private long paramFuelTimeInterval = 0;
    private long paramFuelTime = 0;
    private long paramFuelPercent = 0;
    private long paramFuelLiters = 0;

    private long paramFuelPercentSum = 0;
    private long paramFuelPercentCount = 0;

    private double consumptionSum = 0.0;
    private int consumptionCount = 0;

    private TripFuel tripFuel;
    //private SQLiteDatabase dbTripFuel;
    private SQLiteStatement stmtTripFuel;

    //private List<Double> consumptionList = new ArrayList<>();

    //@InjectView(R.id.compass_text)
    private TextView compass;
    private final SensorEventListener orientListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            String dir = "";
            if (x >= 337.5 || x < 22.5) {
                dir = "N";
            } else if (x >= 22.5 && x < 67.5) {
                dir = "NE";
            } else if (x >= 67.5 && x < 112.5) {
                dir = "E";
            } else if (x >= 112.5 && x < 157.5) {
                dir = "SE";
            } else if (x >= 157.5 && x < 202.5) {
                dir = "S";
            } else if (x >= 202.5 && x < 247.5) {
                dir = "SW";
            } else if (x >= 247.5 && x < 292.5) {
                dir = "W";
            } else if (x >= 292.5 && x < 337.5) {
                dir = "NW";
            }
            updateTextView(compass, dir);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };

    //@InjectView(R.id.BT_STATUS)
    private TextView btStatusTextView;

    //@InjectView(R.id.OBD_STATUS)
    private TextView obdStatusTextView;

    //@InjectView(R.id.GPS_POS)
    private TextView gpsStatusTextView;

    //@InjectView(R.id.vehicle_view)
    private LinearLayout vv;

    //@InjectView(R.id.data_table)
    private TableLayout tl;

    //@Inject
    private SensorManager sensorManager;

    //@Inject
    private PowerManager powerManager;

    //@Inject
    private SharedPreferences prefs;

    private boolean isServiceBound;
    private AbstractGatewayService service;
    private final Runnable mQueueCommands = new Runnable() {
        public void run() {
            if (service != null && service.isRunning() && service.queueEmpty()) {
                queueCommands();

                double lat = 0;
                double lon = 0;
                double alt = 0;
                final int posLen = 7;
                if (mGpsIsStarted && mLastLocation != null) {
                    lat = mLastLocation.getLatitude();
                    lon = mLastLocation.getLongitude();
                    alt = mLastLocation.getAltitude();

                    StringBuilder sb = new StringBuilder();

                    sb.append("Lat: ");
                    sb.append(String.valueOf(mLastLocation.getLatitude()).substring(0, posLen));
                    sb.append(" Lon: ");
                    sb.append(String.valueOf(mLastLocation.getLongitude()).substring(0, posLen));
                    sb.append(" Alt: ");
                    sb.append(String.valueOf(mLastLocation.getAltitude()));

                    gpsStatusTextView.setText(sb.toString());
                }
                if (prefs.getBoolean(ConfigActivity.UPLOAD_DATA_KEY, false)) {
                    // Upload the current reading by http
                    final String vin = prefs.getString(ConfigActivity.VEHICLE_ID_KEY, "UNDEFINED_VIN");
                    Map<String, String> temp = new HashMap<>();
                    temp.putAll(commandResult);
                    ObdReading reading = new ObdReading(lat, lon, alt, System.currentTimeMillis(), vin, temp);
                    new UploadAsyncTask().execute(reading);

                } else if (prefs.getBoolean(ConfigActivity.ENABLE_FULL_LOGGING_KEY, false)) {
                    // Write the current reading to CSV
                    final String vin = prefs.getString(ConfigActivity.VEHICLE_ID_KEY, "UNDEFINED_VIN");
                    Map<String, String> temp = new HashMap<>();
                    temp.putAll(commandResult);
                    ObdReading reading = new ObdReading(lat, lon, alt, System.currentTimeMillis(), vin, temp);
                    if (reading != null) myCSVWriter.writeLineCSV(reading);
                }
                commandResult.clear();
            }
            // run again in period defined in preferences
            new Handler().postDelayed(mQueueCommands, ConfigActivity.getObdUpdatePeriod(prefs));
        }
    };
    private Sensor orientSensor = null;
    private PowerManager.WakeLock wakeLock = null;
    private boolean preRequisites = true;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, className.toString() + " service is bound");
            isServiceBound = true;
            service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(MainActivity.this);
            Log.d(TAG, "Starting live data");
            try {
                service.startService();
                if (preRequisites)
                    btStatusTextView.setText(getString(R.string.status_bluetooth_connected));
            } catch (IOException ioe) {
                Log.e(TAG, "Failure Starting live data");
                btStatusTextView.setText(getString(R.string.status_bluetooth_error_connecting));
                doUnbindService();
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, className.toString() + " service is unbound");
            isServiceBound = false;
        }
    };

    public static String LookUpCommand(String txt) {
        for (AvailableCommandNames item : AvailableCommandNames.values()) {
            if (item.getValue().equals(txt)) return item.name();
        }
        return txt;
    }

    public void updateTextView(final TextView view, final String txt) {
        new Handler().post(new Runnable() {
            public void run() {
                view.setText(txt);
            }
        });
    }

    public void stateUpdate(final ObdCommandJob job) {
        final String cmdName = job.getCommand().getName();
        String cmdResult = "";
        final String cmdID = LookUpCommand(cmdName);

        if (job.getState().equals(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR)) {
            cmdResult = job.getCommand().getResult();
            if (cmdResult != null && isServiceBound) {
                obdStatusTextView.setText(cmdResult.toLowerCase());
            }
        } else if (job.getState().equals(ObdCommandJob.ObdCommandJobState.BROKEN_PIPE)) {
            if (isServiceBound)
                stopLiveData();
        } else if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED)) {
            cmdResult = getString(R.string.status_obd_no_support);
        } else {
            cmdResult = job.getCommand().getFormattedResult();
            if (isServiceBound)
                obdStatusTextView.setText(getString(R.string.status_obd_data));
        }

        if (cmdID != null && cmdName != null && cmdResult != null) {
            if (!cmdID.equals("") && !cmdName.equals("") && !cmdResult.equals("")) {

                Log.e("CMD_ID", "@" + cmdID);
                Log.e("CMD_NAME", "@" + cmdName);
                Log.e("CMD_RESULT", "@" + cmdResult);

                Log.e("***************", "***************");

                    /*
                    Toast.makeText(
                            getBaseContext(),
                            "FUEL_LEVEL: " + ((FuelLevelCommand) job.getCommand()).getFuelLevel(),
                            Toast.LENGTH_SHORT
                    ).show();
                    */


                setConsumptionParams(cmdID, job.getCommand());

                if (vv.findViewWithTag(cmdID) != null) {
                    TextView existingTV = vv.findViewWithTag(cmdID);
                    existingTV.setText(cmdResult);
                } else addTableRow(cmdID, cmdName, cmdResult);


                commandResult.put(cmdID, cmdResult);
                updateTripStatistic(job, cmdID);
            }
        }
    }

/*    private double calcConsumption(long timeStamp, float fuelInput, float tank) {

        Toast.makeText(
                getBaseContext(),
                "fuelInput: " + fuelInput,
                Toast.LENGTH_SHORT
        ).show();

        double consumption = ((tank * (paramFuelInput - fuelInput)) / (timeStamp - paramTimeStamp)) * 3600;

        Toast.makeText(
                getBaseContext(),
                "consumption: " + consumption,
                Toast.LENGTH_SHORT
        ).show();

        return consumption;
    }*/


    private void setConsumptionParams(String cmdID, ObdCommand cmd) {
        if (cmdID.equals("MAF")) {
            paramMaf = ((MassAirFlowCommand) cmd).getMAF();

        } else if (cmdID.equals("THROTTLE_POS")) {
            paramThrottle = ((ThrottlePositionCommand) cmd).getPercentage();

        } else if (cmdID.equals("ENGINE_RPM")) {
            paramRpm = ((RPMCommand) cmd).getRPM();

        } else if (cmdID.equals("SPEED")) {
            paramSpeed = ((SpeedCommand) cmd).getMetricSpeed();

            CalcOBD2.Fuel fuelType = CalcOBD2.Fuel.valueOf(
                    prefs.getString("fuel_type_preference", "E27")
            );

            calcConsumption(fuelType);

        } else if (cmdID.equals("FUEL_LEVEL")) {
            calcFuelSupply(cmd);
        }
    }

    private void resetParams() {
        consumptionResult = "0";
        consumptionAverage = "0";

        paramSpeed = 0;
        paramMaf = 0;
        paramRpm = 0;
        paramThrottle = 0;

        paramFuelTimeInterval = 0;
        paramFuelTime = 0;
        paramFuelPercent = 0;
        paramFuelLiters = 0;

        paramFuelPercentSum = 0;
        paramFuelPercentCount = 0;

        consumptionSum = 0.0;
        consumptionCount = 0;

        TextView existingTV;

        existingTV = vv.findViewWithTag("CONSUMPTION");
        existingTV.setText("0");

        existingTV = vv.findViewWithTag("AVERAGE");
        existingTV.setText("0");

        existingTV = vv.findViewWithTag("SPEED");
        existingTV.setText("0");

        existingTV = vv.findViewWithTag("ENGINE_RPM");
        existingTV.setText("0");

        existingTV = vv.findViewWithTag("MAF");
        existingTV.setText("0");

        existingTV = vv.findViewWithTag("THROTTLE_POS");
        existingTV.setText("0");

        //ENGINE_RPM
        //SPEED
        //MAF
        //THROTTLE_POS
    }

    private void initFuelSupply() {
        if (paramFuelPercent == 0) {
            Cursor c = tripFuel.queryLastFuel();

            while (c.moveToNext()) {
                paramFuelTime = c.getLong(0) * 1000;
                paramFuelPercent = c.getLong(1);
                paramFuelLiters = c.getLong(2);

                Log.e("cursorFuelTime", "@@@" + paramFuelTime);
                Log.e("cursorFuelPercent", "@@@" + paramFuelPercent);
                Log.e("cursorFuelLiters", "@@@" + paramFuelLiters);
            }

            c.close();
        }
    }


    // CALC FUEL SUPPLY
    //=========================================================================
    //=========================================================================

    private void calcFuelSupply(ObdCommand cmd) {

        paramFuelTime = System.currentTimeMillis();

        paramFuelPercentSum += Math.round(((FuelLevelCommand) cmd).getFuelLevel() * 100.0) / 100.0;
        paramFuelPercentCount++;

        paramFuelLiters = Long.parseLong(prefs.getString("fuel_tank_preference", "0"));

        Log.e("####################", "paramFuelLiters: " + paramFuelLiters);

        if (paramFuelTime > paramFuelTimeInterval && paramFuelPercentCount > 20) {

            long tempFuelPercent = paramFuelPercentSum / paramFuelPercentCount;


/*            Toast.makeText(
                    getBaseContext(),
                    "TIME: " + paramFuelTime,
                    Toast.LENGTH_SHORT
            ).show();*/
            if ((tempFuelPercent * 0.95) > paramFuelPercent) {

                SQLiteDatabase db = tripFuel.getWritableDatabase();


                try {
                    db.beginTransaction();

                    tripFuel.stmtInsertIntoTableTripFuel(
                            stmtTripFuel,
                            paramFuelTime / 1000,
                            tempFuelPercent,
                            paramFuelLiters
                    );

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {


                    if (db != null && db.inTransaction()) {
                        db.endTransaction();
                    }
                }
            }

            paramFuelPercent = tempFuelPercent;
            paramFuelTimeInterval = paramFuelTime + TIME_INTERVAL;
        }
    }


    private void calcConsumption(CalcOBD2.Fuel fuel) {

        double consumption = 0;

        int bhp = Integer.parseInt(prefs.getString("engine_horse_power_preference", "-1"));

        if (paramMaf > 0) {
            consumption = CalcOBD2.getFuelConsumptionMAF(fuel, paramSpeed, paramMaf);
        } else if (paramThrottle > 0 && bhp > 0) {
            consumption = CalcOBD2.getFuelConsumptionThrottle(fuel, bhp, paramSpeed, paramThrottle);
        } else if (bhp > 0) {
            consumption = CalcOBD2.getFuelConsumptionRPM(fuel, bhp, paramSpeed, paramRpm);
        }

        //consumption = calcConsumption()

        TextView existingTV;

        consumptionResult = new DecimalFormat("0.0").format(consumption);

        existingTV = vv.findViewWithTag("CONSUMPTION");
        existingTV.setText(consumptionResult + "km/l");

        consumptionSum += consumption;
        consumptionCount += 1;

        consumptionAverage =
                new DecimalFormat("0.0").format(
                        CalcOBD2.getAverage(consumptionSum, consumptionCount));

        existingTV = vv.findViewWithTag("AVERAGE");
        existingTV.setText(consumptionAverage + "km/l");
    }


    @SuppressLint("MissingPermission")
    private void gpsInit() {
        if (hasPermissionGps) {
            mLocService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (mLocService != null) {
                mLocProvider = mLocService.getProvider(LocationManager.GPS_PROVIDER);
                if (mLocProvider != null) {
                    mLocService.addGpsStatusListener(this);
                    if (mLocService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsStatusTextView.setText(getString(R.string.status_gps_ready));
                        return;
                    }
                }
            }
        }

        gpsStatusTextView.setText(getString(R.string.status_gps_no_support));
        showDialog(NO_GPS_SUPPORT);
        Log.e(TAG, "Unable to get GPS PROVIDER");
        // todo disable gps controls into Preferences
    }

    private void updateTripStatistic(final ObdCommandJob job, final String cmdID) {

        if (currentTrip != null) {
            if (cmdID.equals(AvailableCommandNames.SPEED.toString())) {
                SpeedCommand command = (SpeedCommand) job.getCommand();
                currentTrip.setSpeedMax(command.getMetricSpeed());
                currentTrip.setConsumption(Double.parseDouble(consumptionAverage.
                        replace(",", ".")));

            } else if (cmdID.equals(AvailableCommandNames.ENGINE_RPM.toString())) {
                RPMCommand command = (RPMCommand) job.getCommand();
                currentTrip.setEngineRpmMax(command.getRPM());
            } else if (cmdID.endsWith(AvailableCommandNames.ENGINE_RUNTIME.toString())) {
                RuntimeCommand command = (RuntimeCommand) job.getCommand();
                currentTrip.setEngineRuntime(command.getFormattedResult());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // -------------------------------------------------------------------

        compass = findViewById(R.id.compass_text);

        btStatusTextView = findViewById(R.id.BT_STATUS);
        obdStatusTextView = findViewById(R.id.OBD_STATUS);
        gpsStatusTextView = findViewById(R.id.GPS_POS);

        vv = findViewById(R.id.vehicle_view);
        tl = findViewById(R.id.data_table);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // -------------------------------------------------------------------

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled();

        // get Orientation sensor
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensors.size() > 0)
            orientSensor = sensors.get(0);
        else
            showDialog(NO_ORIENTATION_SENSOR);

        // create a log instance for use by this application
        triplog = TripLog.getInstance(this.getApplicationContext());

        obdStatusTextView.setText(getString(R.string.status_obd_disconnected));

        tripFuel = TripFuel.getInstance(getBaseContext());

        stmtTripFuel = tripFuel.getWritableDatabase()
                .compileStatement(TripFuel.INSERT_INTO_TABLE_TRIP_FUEL);

        initFuelSupply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Entered onStart...");
        Log.d(TAG, "Verify permissions...");


        if (android.os.Build.VERSION.SDK_INT > 23) {
            if (hasPermissions(REQUEST_PERMISSIONS, this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                hasPermissionGps = true;
                gpsInit();
            }
        } else {
            hasPermissionGps = true;
            gpsInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {

                for (int i = 0; i < permissions.length; i++) {

                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        hasPermissionGps = (grantResult == PackageManager.PERMISSION_GRANTED);
                        gpsInit();
                    }
                }

                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLiveData();

        if (mLocService != null) {
            mLocService.removeGpsStatusListener(this);
            mLocService.removeUpdates(this);
        }

        releaseWakeLockIfHeld();
        if (isServiceBound) {
            doUnbindService();
        }

        endTrip();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && btAdapter.isEnabled() && !bluetoothDefaultIsEnable)
            btAdapter.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing..");
        releaseWakeLockIfHeld();
    }

    /**
     * If lock is held, release. Lock will be held when the service is running.
     */
    private void releaseWakeLockIfHeld() {
        if (wakeLock.isHeld())
            wakeLock.release();
    }

    private boolean hasPermissions(
            int requestCode, Activity activity, String... requestPermission) {

        boolean testPermissions = true;

        for (String permission : requestPermission) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {

                testPermissions = false;
                break;
            }
        }

        if (!testPermissions) {
            ActivityCompat.requestPermissions(activity, requestPermission, requestCode);
        }

        return testPermissions;
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming..");
        sensorManager.registerListener(orientListener, orientSensor,
                SensorManager.SENSOR_DELAY_UI);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "ObdReader");

        // get Bluetooth device
        final BluetoothAdapter btAdapter = BluetoothAdapter
                .getDefaultAdapter();

        preRequisites = btAdapter != null && btAdapter.isEnabled();
        if (!preRequisites && prefs.getBoolean(ConfigActivity.ENABLE_BT_KEY, false)) {
            preRequisites = btAdapter != null && btAdapter.enable();
        }

        if (!preRequisites) {
            showDialog(BLUETOOTH_DISABLED);
            btStatusTextView.setText(getString(R.string.status_bluetooth_disabled));
        } else {
            btStatusTextView.setText(getString(R.string.status_bluetooth_ok));
        }
    }

    private void updateConfig() {
        startActivity(new Intent(this, ConfigActivity.class));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, START_LIVE_DATA, 0, getString(R.string.menu_start_live_data));
        menu.add(0, STOP_LIVE_DATA, 0, getString(R.string.menu_stop_live_data));
        menu.add(0, GET_DTC, 0, getString(R.string.menu_get_dtc));
        menu.add(0, TRIPS_LIST, 0, getString(R.string.menu_trip_list));
        menu.add(0, FUEL_LIST, 0, getString(R.string.menu_fuel_list));
        menu.add(0, SETTINGS, 0, getString(R.string.menu_settings));

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case START_LIVE_DATA:
                resetParams();
                stopLiveData();
                startLiveData();
                return true;
            case STOP_LIVE_DATA:
                stopLiveData();
                return true;
            case SETTINGS:
                updateConfig();
                return true;
            case GET_DTC:
                getTroubleCodes();
                return true;
            case TRIPS_LIST:
                startActivity(new Intent(this, TripListActivity.class));
                return true;
            case FUEL_LIST:
                startActivity(new Intent(this, TripFuelListActivity.class));
                return true;
        }
        return false;
    }

    private void getTroubleCodes() {
        startActivity(new Intent(this, TroubleCodesActivity.class));
    }

    private void startLiveData() {
        Log.d(TAG, "Starting live data..");

        tl.removeAllViews(); //start fresh
        doBindService();

        currentTrip = triplog.startTrip();


        if (currentTrip == null)
            showDialog(SAVE_TRIP_NOT_AVAILABLE);

        // start command execution
        new Handler().post(mQueueCommands);

        if (prefs.getBoolean(ConfigActivity.ENABLE_GPS_KEY, false))
            gpsStart();
        else
            gpsStatusTextView.setText(getString(R.string.status_gps_not_used));

        // screen won't turn off until wakeLock.release()
        wakeLock.acquire();

        if (prefs.getBoolean(ConfigActivity.ENABLE_FULL_LOGGING_KEY, false)) {

            // Create the CSV Logger
            long mils = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");

            try {
                myCSVWriter = new LogCSVWriter("Log" + sdf.format(new Date(mils)).toString() + ".csv",
                        prefs.getString(ConfigActivity.DIRECTORY_FULL_LOGGING_KEY,
                                getString(R.string.default_dirname_full_logging))
                );
            } catch (FileNotFoundException | RuntimeException e) {
                Log.e(TAG, "Can't enable logging to file.", e);
            }
        }
    }

    private void stopLiveData() {
        Log.d(TAG, "Stopping live data..");

        gpsStop();

        doUnbindService();
        endTrip();

        releaseWakeLockIfHeld();

        final String devemail = prefs.getString(ConfigActivity.DEV_EMAIL_KEY, null);
        if (devemail != null && !devemail.isEmpty()) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            ObdGatewayService.saveLogcatToFile(getApplicationContext(), devemail);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Where there issues?\nThen please send us the logs.\nSend Logs?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        if (myCSVWriter != null) {
            myCSVWriter.closeLogCSVWriter();
        }
    }

    protected void endTrip() {
        if (currentTrip != null) {
            currentTrip.setEndDate(new Date());
            triplog.updateRecord(currentTrip);
        }
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        switch (id) {
            case NO_BLUETOOTH_ID:
                build.setMessage(getString(R.string.text_no_bluetooth_id));
                return build.create();
            case BLUETOOTH_DISABLED:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return build.create();
            case NO_ORIENTATION_SENSOR:
                build.setMessage(getString(R.string.text_no_orientation_sensor));
                return build.create();
            case NO_GPS_SUPPORT:
                build.setMessage(getString(R.string.text_no_gps_support));
                return build.create();
            case SAVE_TRIP_NOT_AVAILABLE:
                build.setMessage(getString(R.string.text_save_trip_not_available));
                return build.create();
        }
        return null;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem startItem = menu.findItem(START_LIVE_DATA);
        MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
        MenuItem TripsListItem = menu.findItem(TRIPS_LIST);
        MenuItem TripsFuelListItem = menu.findItem(FUEL_LIST);
        MenuItem settingsItem = menu.findItem(SETTINGS);
        MenuItem getDTCItem = menu.findItem(GET_DTC);

        if (service != null && service.isRunning()) {
            startItem.setEnabled(false);
            stopItem.setEnabled(true);
            getDTCItem.setEnabled(false);
            TripsListItem.setEnabled(true);
            TripsFuelListItem.setEnabled(true);
            settingsItem.setEnabled(false);
        } else {
            stopItem.setEnabled(true);
            startItem.setEnabled(true);
            getDTCItem.setEnabled(true);
            TripsListItem.setEnabled(true);
            TripsFuelListItem.setEnabled(true);
            settingsItem.setEnabled(true);
        }

        return true;
    }

    private void addTableRow(String id, String key, String val) {
        TableRow tr = new TableRow(this);
        MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
                TABLE_ROW_MARGIN);
        tr.setLayoutParams(params);

        TextView name = new TextView(this);
        name.setGravity(Gravity.RIGHT);
        name.setText(key + ": ");
        TextView value = new TextView(this);
        value.setGravity(Gravity.LEFT);
        value.setText(val);
        value.setTag(id);
        tr.addView(name);
        tr.addView(value);
        tl.addView(tr, params);
    }

    /**
     *
     */
    private void queueCommands() {
        if (isServiceBound) {
            for (ObdCommand Command : ObdConfig.getCommands()) {
                if (prefs.getBoolean(Command.getName(), true))
                    service.queueJob(new ObdCommandJob(Command));
            }
        }
    }

    private void doBindService() {
        if (!isServiceBound) {
            Log.d(TAG, "Binding OBD service...");
            if (preRequisites) {
                btStatusTextView.setText(getString(R.string.status_bluetooth_connecting));
                Intent serviceIntent = new Intent(this, ObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            } else {
                btStatusTextView.setText(getString(R.string.status_bluetooth_disabled));
                Intent serviceIntent = new Intent(this, MockObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private void doUnbindService() {
        if (isServiceBound) {
            if (service != null && service.isRunning()) {
                service.stopService();
                if (preRequisites)
                    btStatusTextView.setText(getString(R.string.status_bluetooth_ok));
            }

            Log.d(TAG, "Unbinding OBD service..");
            unbindService(serviceConn);
            isServiceBound = false;
            obdStatusTextView.setText(getString(R.string.status_obd_disconnected));
        }

        // Clear all notification
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (nMgr != null)
            nMgr.cancelAll();
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onGpsStatusChanged(int event) {

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                gpsStatusTextView.setText(getString(R.string.status_gps_started));
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                gpsStatusTextView.setText(getString(R.string.status_gps_stopped));
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                gpsStatusTextView.setText(getString(R.string.status_gps_fix));
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                btStatusTextView.setText(getString(R.string.status_bluetooth_connected));
            } else {
                Toast.makeText(this, R.string.text_bluetooth_disabled, Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("MissingPermission")
    private synchronized void gpsStart() {
        if (!mGpsIsStarted && mLocProvider != null && mLocService != null && mLocService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocService.requestLocationUpdates(mLocProvider.getName(), getGpsUpdatePeriod(prefs), getGpsDistanceUpdatePeriod(prefs), this);
            mGpsIsStarted = true;
        } else {
            gpsStatusTextView.setText(getString(R.string.status_gps_no_support));
        }
    }

    private synchronized void gpsStop() {
        if (mGpsIsStarted) {
            mLocService.removeUpdates(this);
            mGpsIsStarted = false;
            gpsStatusTextView.setText(getString(R.string.status_gps_stopped));
        }
    }

    /**
     * Uploading asynchronous task
     */
    private class UploadAsyncTask extends AsyncTask<ObdReading, Void, Void> {

        @Override
        protected Void doInBackground(ObdReading... readings) {
            Log.d(TAG, "Uploading " + readings.length + " readings..");
            // instantiate reading service client
            final String endpoint = prefs.getString(ConfigActivity.UPLOAD_URL_KEY, "");
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(endpoint)
                    .build();
            ObdService service = restAdapter.create(ObdService.class);
            // upload readings
            for (ObdReading reading : readings) {
                try {
                    Response response = service.uploadReading(reading);
                    assert response.getStatus() == 200;
                } catch (RetrofitError re) {
                    Log.e(TAG, re.toString());
                }

            }
            Log.d(TAG, "Done");
            return null;
        }
    }
}