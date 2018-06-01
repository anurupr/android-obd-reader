package br.com.pirus.obd2.io;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.FuelTrim;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class ObdBluetoothService extends IntentService {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    // ===================================================================
    // * IMPLEMENTATION EXEMPLE ACTIVITY
    // ===================================================================

    private Intent myIntent;
    private SampleResultReceiver myResultReceiver;

    myResultReceiver = new SampleResultReceiver(new Handler());
    myIntent = new Intent(Context, ObdBluetoothService.class);

    String myBluetoothDevice = /// get the selected bluetooth device...

    myIntent.putExtra(ObdBluetoothService.OBD_INPUT_DEVICE, myBluetoothDevice);
    myIntent.putExtra(ObdBluetoothService.OBD_INPUT_RECEIVER, myResultReceiver);

    // Start service action
    startService(myIntent);

    // Stop service action
    stopService(myIntent);


    // --------------------------------------------------------------------

    private class SampleResultReceiver extends ResultReceiver {

        public SampleResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case ObdBluetoothService.OBD_OUTPUT_FAILURE:
                    String message = resultData.getString(ObdBluetoothService.OBD_KEY_MESSAGE);

                    // implement your code here...
                    break;

                case ObdBluetoothService.OBD_OUTPUT_SUCCESS:

                    ObdBluetoothService.CommandSerializable
                            Serializable  = (ObdBluetoothService.CommandSerializable)
                            resultData.getSerializable(ObdBluetoothService.OBD_KEY_COMMAND);

                    assert Serializable != null;
                    ObdCommand Command = Serializable.getObdCommand();

                    // implement your code here...
                    break;
            }
        }
     }
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private static final String TAG = ObdBluetoothService.class.getName();
    public static final String OBD_KEY_COMMAND = "command";
    public static final String OBD_KEY_MESSAGE = "message";
    public static final String OBD_INPUT_DEVICE = "device";
    //public static final String OBD_KEY_FAILURE = "failure";
    public static final String OBD_INPUT_RECEIVER = "receiver";
    public static final int OBD_OUTPUT_SUCCESS = 449;
    //public static final int OBD_OUTPUT_MESSAGE = 450;
    public static final int OBD_OUTPUT_FAILURE = 451;
    private static final String NAME = ObdBluetoothService.class.getCanonicalName();

    private BluetoothSocket mSocket;
    private ArrayList<ObdCommand> mBootQueueCommands = new ArrayList<ObdCommand>() {{
        add((new ObdResetCommand()));
        add((new TimeoutCommand(125)));
        add((new EchoOffCommand()));
        add((new LineFeedOffCommand()));
        add((new SelectProtocolCommand(ObdProtocols.valueOf("AUTO"))));
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

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ObdBluetoothService() {
        super(NAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        ResultReceiver receiver = intent.getParcelableExtra(OBD_INPUT_RECEIVER);

        try {
            Log.d(TAG, "start...");

            String device = intent.getStringExtra(OBD_INPUT_DEVICE);

            if (device == null || device.equals("")) {
                throw new Exception("Bluetooth device not has been selected");
            }

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            if (btAdapter == null || !btAdapter.isEnabled()) {
                throw new Exception("Bluetooth is not enable");
            }

            mSocket = btAdapter.getRemoteDevice(device).createInsecureRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

            if (mSocket == null) {
                throw new Exception("Socket is not enable");
            }

            //mSocket.close();
            mSocket.connect();

            if (!mSocket.isConnected()) {
                throw new Exception("Bluetooth is not connected");
            }

            for (ObdCommand cmd : mBootQueueCommands) {
                try {
                    cmd.run(mSocket.getInputStream(), mSocket.getOutputStream());
                } catch (Exception e) {
                    throw new Exception("Failed to run command: " + cmd.getName());
                }
            }

            SharedPreferences mPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            for (ObdCommand Command : mAvailableQueueCommands) {
                if (mPreferences.getBoolean(Command.getName(), true))
                    mLoopQueueCommands.add((Command));
            }

            Bundle bundle = new Bundle();

            while (true) {
                Log.d(TAG, "loop...");

                for (ObdCommand Command : mLoopQueueCommands) {

                    try {
                        Command.run(mSocket.getInputStream(), mSocket.getOutputStream());
                    } catch (IOException e) {
                        throw new Exception("Bluetooth has been disconnected");
                    } catch (Exception e) {
                        //Log.e(TAG, "Failed to run command:" + cmd.getName());
                        continue;
                    }

                    bundle.putSerializable(OBD_KEY_COMMAND, new CommandSerializable(Command));
                    receiver.send(OBD_OUTPUT_SUCCESS, bundle);
                }
            }

        } catch (Exception e) {
            Bundle bundle = new Bundle();
            bundle.putString(OBD_KEY_MESSAGE, e.getMessage());
            receiver.send(OBD_OUTPUT_FAILURE, bundle);
            e.printStackTrace();
        }
    }

    public class CommandSerializable implements Serializable {

        private ObdCommand obdCommand;

        CommandSerializable(ObdCommand obdCommand) {
            this.obdCommand = obdCommand;
        }

        public ObdCommand getObdCommand() {
            return obdCommand;
        }
    }
}