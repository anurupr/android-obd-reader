package br.com.pirus.obd2.io;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = BluetoothManager.class.getName();
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    /**
     *
     * @param dev
     * @return
     * @throws Exception
     */
    public static BluetoothSocket connect(BluetoothDevice dev) throws Exception {
        Log.d(TAG, "Starting Bluetooth connection...");

        BluetoothSocket mSocket = dev.createRfcommSocketToServiceRecord(MY_UUID);
        mSocket.connect();

        return mSocket;
    }
}