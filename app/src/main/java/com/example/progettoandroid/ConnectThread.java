//classe per gestire la connessione del client

package com.example.progettoandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    private static final String TAG = GestioneBluetooth.TAG;
    BluetoothAdapter mBluetoothAdapter = GestioneBluetooth.mBluetoothAdapter;
    static final UUID MY_UUID = GestioneBluetooth.MY_UUID;

    public ConnectThread(BluetoothDevice device, UUID myUuid) {
        Log.d(TAG, "ConnectThread: started.");
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(myUuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        Log.i(TAG, "RUN mConnectThread ");

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
                Log.d(TAG, "run: Closed Socket.");
            } catch (IOException closeException) {
                Log.e(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID);
            }
            return;
        }


        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //cambiare activity
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
