package br.com.pirus.obd2.io;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import br.com.pirus.obd2.activity.ConfigActivity;
import br.com.pirus.obd2.activity.MainActivity;
import br.com.pirus.obd2.config.ObdConfig;

public class ObdBluetoothService extends Service {

    private static final String TAG = ObdBluetoothService.class.getName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Context mContext;
    private SharedPreferences mPreferences;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private boolean isRunnig = false;

    private Long queueCounter = 0L;
    private ArrayList<ObdCommand> mQueueCommands = new ArrayList<>();

    private Thread workerThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                onStart();

                //onFinish();
            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }
        }
    });

    private void onStart() throws Exception {
        mContext = MainActivity.instance;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isRunnig = true;

        String device = mPreferences.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);

        if (device == null || device.equals("")) {
            Log.e(TAG, "No Bluetooth device has been selected.");
            onFinish();
            return;
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null || !btAdapter.isEnabled()) {
            Log.e(TAG, "No Bluetooth enable.");
            onFinish();
            return;
        }

        mDevice = btAdapter.getRemoteDevice(device);
        mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        mSocket.connect();

        if (!mSocket.isConnected()) {
            Log.e(TAG, "No Bluetooth connected.");
            onFinish();
            return;
        }

        String protocol = mPreferences.getString(ConfigActivity.PROTOCOLS_LIST_KEY, "AUTO");

        //queueJob(new ObdCommandJob(new ObdResetCommand()));
        mQueueCommands.add((new TimeoutCommand(125)));
        mQueueCommands.add((new EchoOffCommand()));
        mQueueCommands.add((new LineFeedOffCommand()));
        mQueueCommands.add((new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));


        for (ObdCommand cmd : mQueueCommands) {
            try {
                cmd.run(mSocket.getInputStream(), mSocket.getOutputStream());
            } catch (Exception e) {
                Log.e(TAG, "Failed to run command: " + cmd.getName());
                onFinish();
                return;
            }
        }

        //mQueueCommands.clear();
        onExecute();
    }


    private void onExecute() {
        Log.d(TAG, "onExecute...");

        queueCommands();

        while (isRunnig) {
            for (final ObdCommand cmd : mQueueCommands) {

                try {
                    cmd.run(mSocket.getInputStream(), mSocket.getOutputStream());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to run command: " + cmd.getName());
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

    private void onFinish() {
        Log.e("onFinish", "onFinish");

        isRunnig = false;
        workerThread.interrupt();

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopSelf();
    }

/*    public void queueJob(ObdCommand cmd) {

        // This is a good place to enforce the imperial units option
        //cmd.useImperialUnits(mPreferences.getBoolean(ConfigActivity.IMPERIAL_UNITS_KEY, false));


        //Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

        //cmd.setId(queueCounter);

            mQueueCommands.add(cmd);
            //jobsQueue.put(job);
    }*/

    private void queueCommands() {
        mQueueCommands.clear();

        for (ObdCommand Command : ObdConfig.getCommands()) {
            if (mPreferences.getBoolean(Command.getName(), true))
                mQueueCommands.add((Command));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        if (workerThread == null || !workerThread.isAlive()) {
            workerThread.start();
        }

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //return super.onUnbind(intent);

        Log.d("onUnbind", "onUnbind");
        Log.d("onUnbind", "onUnbind");
        Log.d("onUnbind", "onUnbind");

        onFinish();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("onDestroy", "onDestroy");
        Log.d("onDestroy", "onDestroy");
        Log.d("onDestroy", "onDestroy");

        onFinish();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
