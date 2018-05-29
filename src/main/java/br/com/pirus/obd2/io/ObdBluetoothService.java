package br.com.pirus.obd2.io;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.DtcNumberCommand;
import com.github.pires.obd.commands.control.EquivalentRatioCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.FuelTrimCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.FuelTrim;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import br.com.pirus.obd2.activity.ConfigActivity;
import br.com.pirus.obd2.activity.MainActivity;

public class ObdBluetoothService extends Service {

    private static final String TAG = ObdBluetoothService.class.getName();

    private volatile boolean running = false;

    private Context mContext;
    private SharedPreferences mPreferences;
    private BluetoothSocket mSocket;


    private ArrayList<ObdCommand> mBootQueueCommands = new ArrayList<ObdCommand>() {{
        add((new TimeoutCommand(125)));
        add((new EchoOffCommand()));
        add((new LineFeedOffCommand()));
    }};

    private ArrayList<ObdCommand> mAvailableQueueCommands = new ArrayList<ObdCommand>() {{

        // Control
        add(new ModuleVoltageCommand());
        add(new EquivalentRatioCommand());
        add(new DistanceMILOnCommand());
        add(new DtcNumberCommand());
        add(new TimingAdvanceCommand());
        add(new TroubleCodesCommand());
        add(new VinCommand());

        // Engine
        add(new LoadCommand());
        add(new RPMCommand());
        add(new RuntimeCommand());
        add(new MassAirFlowCommand());
        add(new ThrottlePositionCommand());

        // Fuel
        add(new FindFuelTypeCommand());
        add(new ConsumptionRateCommand());
        add(new FuelLevelCommand());
        add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_1));
        add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_2));
        add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_1));
        add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_2));
        add(new AirFuelRatioCommand());
        add(new WidebandAirFuelRatioCommand());
        add(new OilTempCommand());

        // Pressure
        add(new BarometricPressureCommand());
        add(new FuelPressureCommand());
        add(new FuelRailPressureCommand());
        add(new IntakeManifoldPressureCommand());

        // Temperature
        add(new AirIntakeTemperatureCommand());
        add(new AmbientAirTemperatureCommand());
        add(new EngineCoolantTemperatureCommand());

        // Misc
        add(new SpeedCommand());
    }};

    private ArrayList<ObdCommand> mLoopQueueCommands = new ArrayList<>();

    private Thread mWorkerThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                start();
                //close();
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        if (mWorkerThread != null && !mWorkerThread.isAlive()) {
            mWorkerThread.setPriority(Thread.MAX_PRIORITY);
            mWorkerThread.start();

        }

        return START_STICKY;
    }


    private void start() throws Exception {
        Log.d(TAG, "start...");

        running = true;

        mContext = MainActivity.instance;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String device = mPreferences.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);

        if (device == null || device.equals("")) {
            throw new Exception("No Bluetooth device has been selected.");
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null || !btAdapter.isEnabled()) {
            throw new Exception("No Bluetooth enable.");
        }

        mSocket = btAdapter.getRemoteDevice(device).createInsecureRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

        if (!mSocket.isConnected()) {
            mSocket.connect();
            //throw new Exception("No Bluetooth connected.");
        }

        if (!mSocket.isConnected()) {
            throw new Exception("No Bluetooth connected.");
        }

        String protocol = mPreferences.getString(ConfigActivity.PROTOCOLS_LIST_KEY, "AUTO");
        mBootQueueCommands.add((new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));

        for (ObdCommand cmd : mBootQueueCommands) {
            try {
                cmd.run(mSocket.getInputStream(), mSocket.getOutputStream());
            } catch (Exception e) {
                throw new Exception("Failed to run command: " + cmd.getName());
            }
        }

        for (ObdCommand Command : mAvailableQueueCommands) {
            if (mPreferences.getBoolean(Command.getName(), true))
                mLoopQueueCommands.add((Command));
        }

        while (running) {
            Log.d(TAG, "loop...");

            for (final ObdCommand cmd : mLoopQueueCommands) {

                try {
                    cmd.run(mSocket.getInputStream(), mSocket.getOutputStream());
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    running = false;
                    break;
                } catch (Exception e) {
                    Log.d(TAG, "Exception");
                    //e.printStackTrace();
                }

                ((MainActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) mContext).stateUpdate(cmd);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("onDestroy", "onDestroy");
        Log.d("onDestroy", "onDestroy");
        Log.d("onDestroy", "onDestroy");

        running = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
