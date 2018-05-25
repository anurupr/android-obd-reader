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

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private Long queueCounter = 0L;
    private BlockingQueue<ObdCommandJob> jobsQueue = new LinkedBlockingQueue<>();

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
        queueJob(new ObdCommandJob(new TimeoutCommand(100)));
        queueJob(new ObdCommandJob(new EchoOffCommand()));
        queueJob(new ObdCommandJob(new LineFeedOffCommand()));
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));

        for (ObdCommandJob job : jobsQueue) {
            try {
                job.getCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
            } catch (Exception e) {
                job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                Log.e(TAG, "Failed to run command: " + job.getCommand().getName());
                onFinish();
                return;
            }
        }

        jobsQueue.clear();
        onExecute();
    }


    private void onExecute() {
        Log.d(TAG, "onExecute...");

        queueCommands();

        while (workerThread.isAlive()) {
            for (final ObdCommandJob job : jobsQueue) {

                try {
                    job.getCommand().run(mSocket.getInputStream(), mSocket.getOutputStream());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to run command: " + job.getCommand().getName());
                }

                ((MainActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) mContext).stateUpdate(job);
                    }
                });
            }
        }
    }

    private void onFinish() {
        Log.e("onFinish", "onFinish");
        workerThread.interrupt();
        stopSelf();
    }

    public void queueJob(ObdCommandJob job) {

        // This is a good place to enforce the imperial units option
        job.getCommand().useImperialUnits(mPreferences.getBoolean(ConfigActivity.IMPERIAL_UNITS_KEY, false));

        queueCounter++;
        Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

        job.setId(queueCounter);

        try {
            jobsQueue.put(job);
            Log.d(TAG, "Job queued successfully.");
        } catch (InterruptedException e) {
            job.setState(ObdCommandJob.ObdCommandJobState.QUEUE_ERROR);
            Log.e(TAG, "Failed to queue job.");
        }
    }

    private void queueCommands() {
        for (ObdCommand Command : ObdConfig.getCommands()) {
            if (mPreferences.getBoolean(Command.getName(), true))
                queueJob(new ObdCommandJob(Command));
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
    public void onDestroy() {
        super.onDestroy();
        onFinish();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
