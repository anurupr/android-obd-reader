package br.com.pirus.obd2.activity;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.util.ArrayList;
import java.util.Set;

import br.com.pirus.obd2.R;
import br.com.pirus.obd2.config.ObdConfig;
import br.com.pirus.obd2.util.CalcOBD2;


public class ConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String UPLOAD_URL_KEY = "upload_url_preference";
    public static final String UPLOAD_DATA_KEY = "upload_data_preference";
    public static final String OBD_UPDATE_PERIOD_KEY = "obd_update_period_preference";
    public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
    public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
    public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
    public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
    public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
    public static final String PROTOCOLS_LIST_KEY = "obd_protocols_preference";
    public static final String FUEL_LIST_KEY = "fuel_type_preference";
    public static final String ENABLE_GPS_KEY = "enable_gps_preference";
    public static final String GPS_UPDATE_PERIOD_KEY = "gps_update_period_preference";
    public static final String GPS_DISTANCE_PERIOD_KEY = "gps_distance_period_preference";
    public static final String ENABLE_BT_KEY = "enable_bluetooth_preference";
    public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
    public static final String CONFIG_READER_KEY = "reader_config_preference";
    public static final String ENABLE_FULL_LOGGING_KEY = "enable_full_logging";
    public static final String DIRECTORY_FULL_LOGGING_KEY = "dirname_full_logging";
    public static final String DEV_EMAIL_KEY = "dev_email";

    private BTStateChangedBroadcastReceiver btStateReceiver;

    private SwitchPreference btSwitchEnable;
    private ListPreference btPareidList;

    /**
     * @param prefs
     * @return
     */
    public static int getObdUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.
                getString(ConfigActivity.OBD_UPDATE_PERIOD_KEY, "4"); // 4 as in seconds
        int period = 4000; // by default 4000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 4000;
        }

        return period;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getEngineDisplacement(SharedPreferences prefs) {
        String edString = prefs.getString(ConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
        double ed = 1.6;
        try {
            ed = Double.parseDouble(edString);
        } catch (Exception e) {
        }
        return ed;
    }

    /**
     * @param prefs
     * @return
     */
    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        ArrayList<ObdCommand> ucmds = new ArrayList<>();
        for (int i = 0; i < cmds.size(); i++) {
            ObdCommand cmd = cmds.get(i);
            boolean selected = prefs.getBoolean(cmd.getName(), true);
            if (selected)
                ucmds.add(cmd);
        }
        return ucmds;
    }

    /**
     * @param prefs
     * @return
     */
    public static double getMaxFuelEconomy(SharedPreferences prefs) {
        String maxStr = prefs.getString(ConfigActivity.MAX_FUEL_ECON_KEY, "70");
        double max = 70;
        try {
            max = Double.parseDouble(maxStr);
        } catch (Exception e) {
        }
        return max;
    }

    /**
     * @param prefs
     * @return
     */
    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
        String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
        String[] cmds = cmdsStr.split("\n");
        return cmds;
    }

    /**
     * Minimum time between location updates, in milliseconds
     *
     * @param prefs
     * @return
     */
    public static int getGpsUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_UPDATE_PERIOD_KEY, "1"); // 1 as in seconds
        int period = 1000; // by default 1000ms

        try {
            period = (int) (Double.parseDouble(periodString) * 1000);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 1000;
        }

        return period;
    }

    /**
     * Min Distance between location updates, in meters
     *
     * @param prefs
     * @return
     */
    public static float getGpsDistanceUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs
                .getString(ConfigActivity.GPS_DISTANCE_PERIOD_KEY, "5"); // 5 as in meters
        float period = 5; // by default 5 meters

        try {
            period = Float.parseFloat(periodString);
        } catch (Exception e) {
        }

        if (period <= 0) {
            period = 5;
        }

        return period;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // Do something here. This is the event fired when up button is pressed.
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        /*
         * Read preferences resources available at res/xml/preferences.xml
         */
        addPreferencesFromResource(R.xml.preferences);

        checkGps();


        ArrayList<CharSequence> protocolStrings = new ArrayList<>();
        ArrayList<CharSequence> FuelStrings = new ArrayList<>();

        ListPreference listProtocols = (ListPreference) getPreferenceScreen()
                .findPreference(PROTOCOLS_LIST_KEY);

        ListPreference listFuels = (ListPreference) getPreferenceScreen()
                .findPreference(FUEL_LIST_KEY);

        // ==============================================================

        String[] prefKeys;

        prefKeys = new String[]{
                ENGINE_DISPLACEMENT_KEY,
                VOLUMETRIC_EFFICIENCY_KEY,
                OBD_UPDATE_PERIOD_KEY,
                MAX_FUEL_ECON_KEY
        };

        for (String prefKey : prefKeys) {

            EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
                    .findPreference(prefKey);

            txtPref.setOnPreferenceChangeListener(this);
        }

/*        prefKeys = new String[] { ENABLE_BT_KEY };

        for (String prefKey : prefKeys) {

            SwitchPreference txtPref = (SwitchPreference) getPreferenceScreen()
                    .findPreference(prefKey);

            txtPref.setOnPreferenceChangeListener(this);
        }*/


        btSwitchEnable = (SwitchPreference) getPreferenceScreen().findPreference(ENABLE_BT_KEY);
        btPareidList = (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);

        btSwitchEnable.setOnPreferenceChangeListener(this);

        // =============================================================

        /*
         * Available OBD commands
         *
         * TODO This should be read from preferences database
         */
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
                .findPreference(COMMANDS_SCREEN_KEY);

        for (ObdCommand cmd : cmds) {
            CheckBoxPreference cpref = new CheckBoxPreference(this);
            cpref.setTitle(cmd.getName());
            cpref.setKey(cmd.getName());
            cpref.setChecked(true);
            cmdScr.addPreference(cpref);
        }

        /*
         * Available OBD protocols
         *
         */
        for (ObdProtocols protocol : ObdProtocols.values()) {
            protocolStrings.add(protocol.name());
        }
        listProtocols.setEntries(protocolStrings.toArray(new CharSequence[0]));
        listProtocols.setEntryValues(protocolStrings.toArray(new CharSequence[0]));



        /*
         * Available Fuel types
         *
         */
        for (CalcOBD2.Fuel fuel : CalcOBD2.Fuel.values()) {
            FuelStrings.add(fuel.name());
        }
        listFuels.setEntries(FuelStrings.toArray(new CharSequence[0]));
        listFuels.setEntryValues(FuelStrings.toArray(new CharSequence[0]));


        loadBtPareidList();
    }

    private void loadBtPareidList() {

        /*
         * Let's use this device Bluetooth adapter to select which paired OBD-II
         * compliant device we'll use.
         */
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBtAdapter != null && !mBtAdapter.isEnabled()) {
            btSwitchEnable.setChecked(false);
        } else {
            btSwitchEnable.setChecked(true);
        }

        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<>();
        ArrayList<CharSequence> vals = new ArrayList<>();

        /*
         * Get paired devices and populate preference list.
         */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }

            btPareidList.setEnabled(true);
        } else {
            btPareidList.setEnabled(false);
        }

        btPareidList.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        btPareidList.setEntryValues(vals.toArray(new CharSequence[0]));
    }


    @Override
    protected void onResume() {
        super.onResume();

        btStateReceiver = new BTStateChangedBroadcastReceiver();
        registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        loadBtPareidList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (btStateReceiver != null) {
            unregisterReceiver(btStateReceiver);
        }
    }

    /**
     * OnPreferenceChangeListener method that will validate a preferencen new
     * value when it's changed.
     *
     * @param preference the changed preference
     * @param newValue   the value to be validated and set if valid
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (OBD_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
                || ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
                || MAX_FUEL_ECON_KEY.equals(preference.getKey())
                || GPS_UPDATE_PERIOD_KEY.equals(preference.getKey())
                || GPS_DISTANCE_PERIOD_KEY.equals(preference.getKey())) {
            try {
                Double.parseDouble(newValue.toString().replace(",", "."));
                return true;
            } catch (Exception e) {
                Toast.makeText(this,
                        "Couldn't parse '" + newValue.toString() + "' as a number.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (preference instanceof SwitchPreference) {

            SwitchPreference item = (SwitchPreference) preference;

            switch (item.getKey()) {
                case ENABLE_BT_KEY:

                    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (newValue.equals(true)) {
                        if (btAdapter != null && !btAdapter.enable()) {
                            btAdapter.enable();
                        }
                    } else {
                        if (btAdapter != null && btAdapter.enable()) {
                            btAdapter.disable();
                        }
                    }

                    break;
            }
        }

        return true;
    }

    private void checkGps() {
        if (MainActivity.hasPermissionGps) {
            LocationManager mLocService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (mLocService != null) {
                LocationProvider mLocProvider = mLocService.getProvider(LocationManager.GPS_PROVIDER);
                if (mLocProvider == null) {
                    hideGPSCategory();
                }
            }
        } else {
            hideGPSCategory();
        }
    }

    private void hideGPSCategory() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getResources().getString(R.string.pref_gps_category));
        if (preferenceCategory != null) {
            preferenceCategory.removeAll();
            preferenceScreen.removePreference(preferenceCategory);
        }
    }

    public class BTStateChangedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            switch (state) {
                case BluetoothAdapter.STATE_CONNECTED:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_CONNECTED",
                            Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_CONNECTING",
                            Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_DISCONNECTED",
                            Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_DISCONNECTING",
                            Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothAdapter.STATE_OFF:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_OFF",
                            Toast.LENGTH_SHORT).show();*/

                    loadBtPareidList();
                    break;
                case BluetoothAdapter.STATE_ON:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_ON",
                            Toast.LENGTH_SHORT).show();*/

                    loadBtPareidList();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_TURNING_OFF",
                            Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
/*                    Toast.makeText(context,
                            "BTStateChangedBroadcastReceiver: STATE_TURNING_ON",
                            Toast.LENGTH_SHORT).show();*/
                    break;
            }
        }
    }
}
